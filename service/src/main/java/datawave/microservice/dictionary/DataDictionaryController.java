package datawave.microservice.dictionary;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import datawave.microservice.AccumuloConnectionService;
import datawave.microservice.Connection;
import datawave.microservice.authorization.user.ProxiedUserDetails;
import datawave.microservice.dictionary.config.DataDictionaryProperties;
import datawave.microservice.dictionary.config.ResponseObjectFactory;
import datawave.microservice.dictionary.data.DataDictionary;
import datawave.webservice.dictionary.data.DataDictionaryBase;
import datawave.webservice.dictionary.data.DescriptionBase;
import datawave.webservice.dictionary.data.DictionaryFieldBase;
import datawave.webservice.dictionary.data.FieldsBase;
import datawave.webservice.metadata.MetadataFieldBase;
import datawave.webservice.result.VoidResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import static datawave.microservice.http.converter.protostuff.ProtostuffHttpMessageConverter.PROTOSTUFF_VALUE;

@RestController
@RequestMapping(path = "/data/v1", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, PROTOSTUFF_VALUE,
        MediaType.TEXT_HTML_VALUE, "text/x-yaml", "application/x-yaml"})
@EnableConfigurationProperties(DataDictionaryProperties.class)
public class DataDictionaryController<DESC extends DescriptionBase<DESC>,DICT extends DataDictionaryBase<DICT,META>,META extends MetadataFieldBase<META,DESC>,FIELD extends DictionaryFieldBase<FIELD,DESC>,FIELDS extends FieldsBase<FIELDS,FIELD,DESC>> {
    
    private final DataDictionaryProperties dataDictionaryConfiguration;
    private final DataDictionary<META,DESC,FIELD> dataDictionary;
    private final ResponseObjectFactory<DESC,DICT,META,FIELD,FIELDS> responseObjectFactory;
    private final AccumuloConnectionService accumuloConnectionService;
    
    private final Consumer<META> TRANSFORM_EMPTY_INTERNAL_FIELD_NAMES = meta -> {
        if (meta.getInternalFieldName() == null || meta.getInternalFieldName().isEmpty()) {
            meta.setInternalFieldName(meta.getFieldName());
        }
    };
    
    public DataDictionaryController(DataDictionaryProperties dataDictionaryConfiguration, DataDictionary<META,DESC,FIELD> dataDictionary,
                    ResponseObjectFactory<DESC,DICT,META,FIELD,FIELDS> responseObjectFactory, AccumuloConnectionService accumloConnectionService) {
        this.dataDictionaryConfiguration = dataDictionaryConfiguration;
        this.dataDictionary = dataDictionary;
        this.responseObjectFactory = responseObjectFactory;
        this.accumuloConnectionService = accumloConnectionService;
        dataDictionary.setNormalizationMap(dataDictionaryConfiguration.getNormalizerMap());
    }
    
    @GetMapping("/")
    @Timed(name = "dw.dictionary.data.get", absolute = true)
    public DataDictionaryBase<DICT,META> get(@RequestParam(required = false) String modelName, @RequestParam(required = false) String modelTableName,
                    @RequestParam(required = false) String metadataTableName, @RequestParam(name = "auths", required = false) String queryAuthorizations,
                    @RequestParam(defaultValue = "") String dataTypeFilters, @AuthenticationPrincipal ProxiedUserDetails currentUser) throws Exception {
        
        Connection connection = accumuloConnectionService.getConnection(metadataTableName, modelTableName, modelName, currentUser);
        // If the user provides authorizations, intersect it with their actual authorizations
        connection.setAuths(accumuloConnectionService.getDowngradedAuthorizations(queryAuthorizations, currentUser));
        
        Collection<String> dataTypes = (StringUtils.isBlank(dataTypeFilters) ? Collections.emptyList() : Arrays.asList(dataTypeFilters.split(",")));
        
        Collection<META> fields = dataDictionary.getFields(connection, dataTypes, dataDictionaryConfiguration.getNumThreads());
        DICT dataDictionary = responseObjectFactory.getDataDictionary();
        dataDictionary.setFields(fields);
        // Ensure that empty internal field names will be set to the field name instead.
        dataDictionary.transformFields(TRANSFORM_EMPTY_INTERNAL_FIELD_NAMES);
        return dataDictionary;
    }
    
    /**
     * Upload a collection of descriptions to load into the database. Apply a query model to the provided FieldDescriptions before storing.
     *
     * @param fields
     *            a FieldDescriptions to load
     * @param modelName
     *            Optional model name
     * @param modelTable
     *            Optional model table name
     * @param currentUser
     *            The user sending the request
     * @return a VoidResponse
     * @throws Exception
     *             if there is any problem uploading the descriptions
     */
    @PostMapping(path = "/Descriptions", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Timed(name = "dw.dictionary.data.uploadDescriptions", absolute = true)
    public VoidResponse uploadDescriptions(@RequestBody FIELDS fields, @RequestParam(required = false) String modelName,
                    @RequestParam(required = false) String modelTable, @AuthenticationPrincipal ProxiedUserDetails currentUser) throws Exception {
        Connection connection = accumuloConnectionService.getConnection(modelTable, modelName, currentUser);
        List<FIELD> list = fields.getFields();
        for (FIELD desc : list) {
            dataDictionary.setDescription(connection, desc);
        }
        // TODO: reload model table cache?
        // cache.reloadCache(modelTable);
        return new VoidResponse();
    }
    
    /**
     * Set a description for a field in a datatype, optionally applying a model to the field name.
     *
     * @param fieldName
     *            Name of field
     * @param datatype
     *            Name of datatype
     * @param description
     *            Description of field
     * @param modelName
     *            Optional model name
     * @param modelTable
     *            Optional model table name
     * @param columnVisibility
     *            ColumnVisibility of the description
     * @param currentUser
     *            The user sending the request
     * @return a {@link VoidResponse}
     * @throws Exception
     *             if there is any problem updating the description
     */
    @Secured({"Administrator", "JBossAdministrator"})
    @PutMapping("/Descriptions/{datatype}/{fieldName}/{description}")
    @Timed(name = "dw.dictionary.data.setDescriptionPut", absolute = true)
    public VoidResponse setDescriptionPut(@PathVariable String fieldName, @PathVariable String datatype, @PathVariable String description,
                    @RequestParam(required = false) String modelName, @RequestParam(required = false) String modelTable, @RequestParam String columnVisibility,
                    @AuthenticationPrincipal ProxiedUserDetails currentUser) throws Exception {
        return setDescriptionPost(fieldName, datatype, description, modelName, modelTable, columnVisibility, currentUser);
    }
    
    /**
     * Set a description for a field in a datatype, optionally applying a model to the field name.
     *
     * @param fieldName
     *            Name of field
     * @param datatype
     *            Name of datatype
     * @param description
     *            Description of field
     * @param modelName
     *            Optional model name
     * @param modelTable
     *            Optional model table name
     * @param columnVisibility
     *            ColumnVisibility of the description
     * @param currentUser
     *            The user sending the request
     * @return Description of fields
     * @throws Exception
     *             if there is any problem updating the dictionary item description
     */
    @Secured({"Administrator", "JBossAdministrator"})
    @PostMapping("/Descriptions")
    @Timed(name = "dw.dictionary.data.setDescriptionPost", absolute = true)
    public VoidResponse setDescriptionPost(@RequestParam String fieldName, @RequestParam String datatype, @RequestParam String description,
                    @RequestParam(required = false) String modelName, @RequestParam(required = false) String modelTable, @RequestParam String columnVisibility,
                    @AuthenticationPrincipal ProxiedUserDetails currentUser) throws Exception {
        DESC desc = this.responseObjectFactory.getDescription();
        Map<String,String> markings = Maps.newHashMap();
        markings.put("columnVisibility", columnVisibility);
        desc.setMarkings(markings);
        desc.setDescription(description);
        
        Connection connection = accumuloConnectionService.getConnection(modelTable, modelName, currentUser);
        dataDictionary.setDescription(connection, fieldName, datatype, desc);
        // TODO: reload model table cache?
        // cache.reloadCache(modelTable);
        return new VoidResponse();
    }
    
    /**
     * Fetch all descriptions stored in the database, optionally applying a model.
     *
     * @param modelName
     *            Optional model name
     * @param modelTable
     *            Optional model table name
     * @param currentUser
     *            The user sending the request
     * @return the dictionary descriptions
     * @throws Exception
     *             if there is any problem retrieving the descriptions from Accumulo
     */
    @GetMapping("/Descriptions")
    @Timed(name = "dw.dictionary.data.allDescriptions", absolute = true)
    public FIELDS allDescriptions(@RequestParam(required = false) String modelName, @RequestParam(required = false) String modelTable,
                    @AuthenticationPrincipal ProxiedUserDetails currentUser) throws Exception {
        Connection connection = accumuloConnectionService.getConnection(modelTable, modelName, currentUser);
        Multimap<Entry<String,String>,DESC> descriptions = dataDictionary.getDescriptions(connection);
        FIELDS response = this.responseObjectFactory.getFields();
        response.setDescriptions(descriptions);
        return response;
    }
    
    /**
     * Fetch all descriptions for a datatype, optionally applying a model to the field names.
     *
     * @param datatype
     *            Name of datatype
     * @param modelName
     *            Optional model name
     * @param modelTable
     *            Optional model table name
     * @param currentUser
     *            The user sending the request
     * @return the dictionary descriptions for {@code datatype}
     * @throws Exception
     *             if there is any problem retrieving the descriptions from Accumulo
     */
    @GetMapping("/Descriptions/{datatype}")
    @Timed(name = "dw.dictionary.data.datatypeDescriptions", absolute = true)
    public FIELDS datatypeDescriptions(@PathVariable("datatype") String datatype, @RequestParam(required = false) String modelName,
                    @RequestParam(required = false) String modelTable, @AuthenticationPrincipal ProxiedUserDetails currentUser) throws Exception {
        Connection connection = accumuloConnectionService.getConnection(modelTable, modelName, currentUser);
        Multimap<Entry<String,String>,DESC> descriptions = dataDictionary.getDescriptions(connection, datatype);
        FIELDS response = this.responseObjectFactory.getFields();
        response.setDescriptions(descriptions);
        return response;
    }
    
    /**
     * Fetch the description for a field in a datatype, optionally applying a model.
     *
     * @param fieldName
     *            Name of field
     * @param datatype
     *            Name of datatype
     * @param modelName
     *            Optional model name
     * @param modelTable
     *            Optional model table name
     * @param currentUser
     *            The user sending the request
     * @return the dictionary descriptions for field {@code fieldName} in the type {@code dataType}
     * @throws Exception
     *             if there is any problem retrieving the descriptions from Accumulo
     */
    @GetMapping("/Descriptions/{datatype}/{fieldName}")
    @Timed(name = "dw.dictionary.data.fieldNameDescription", absolute = true)
    public FIELDS fieldNameDescription(@PathVariable String fieldName, @PathVariable String datatype, @RequestParam(required = false) String modelName,
                    @RequestParam(required = false) String modelTable, @AuthenticationPrincipal ProxiedUserDetails currentUser) throws Exception {
        Connection connection = accumuloConnectionService.getConnection(modelTable, modelName, currentUser);
        Set<DESC> descriptions = dataDictionary.getDescriptions(connection, fieldName, datatype);
        FIELDS response = responseObjectFactory.getFields();
        if (!descriptions.isEmpty()) {
            Multimap<Entry<String,String>,DESC> mmap = HashMultimap.create();
            for (DESC desc : descriptions) {
                mmap.put(Maps.immutableEntry(fieldName, datatype), desc);
            }
            response.setDescriptions(mmap);
        }
        return response;
    }
    
    /**
     * Delete a description for a field in a datatype, optionally applying a model to the field name.
     *
     * @param fieldName
     *            Name of field
     * @param datatype
     *            Name of datatype
     * @param modelName
     *            Optional model name
     * @param modelTable
     *            Optional model table name
     * @param columnVisibility
     *            the column visibility
     * @param currentUser
     *            The user sending the request
     * @return a {@link VoidResponse} with operation time and error information
     * @throws Exception
     *             if there is any problem removing the description from Accumulo
     */
    @Secured({"Administrator", "JBossAdministrator"})
    @DeleteMapping("/Descriptions/{datatype}/{fieldName}")
    @Timed(name = "dw.dictionary.data.deleteDescription", absolute = true)
    public VoidResponse deleteDescription(@PathVariable String fieldName, @PathVariable String datatype, @RequestParam(required = false) String modelName,
                    @RequestParam(required = false) String modelTable, @RequestParam String columnVisibility,
                    @AuthenticationPrincipal ProxiedUserDetails currentUser) throws Exception {
        Map<String,String> markings = Maps.newHashMap();
        markings.put("columnVisibility", columnVisibility);
        DESC desc = this.responseObjectFactory.getDescription();
        desc.setDescription("");
        desc.setMarkings(markings);
        
        Connection connection = accumuloConnectionService.getConnection(modelTable, modelName, currentUser);
        
        dataDictionary.deleteDescription(connection, fieldName, datatype, desc);
        // TODO: reload model table cache?
        // cache.reloadCache(modelTable);
        return new VoidResponse();
    }
    
}
