package datawave.microservice.dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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
import datawave.webservice.metadata.DefaultMetadataField;
import datawave.webservice.metadata.MetadataFieldBase;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Data Dictionary Controller /v1", description = "DataWave Dictionary Operations",
                externalDocs = @ExternalDocumentation(description = "Dictionary Service Documentation",
                                url = "https://github.com/NationalSecurityAgency/datawave-dictionary-service"))
@Controller
@RequestMapping(path = "/data/v1", produces = {MediaType.TEXT_HTML_VALUE})
@EnableConfigurationProperties(DataDictionaryProperties.class)
public class DataDictionaryWebController<DESC extends DescriptionBase<DESC>,DICT extends DataDictionaryBase<DICT,META>,META extends MetadataFieldBase<META,DESC>,FIELD extends DictionaryFieldBase<FIELD,DESC>,FIELDS extends FieldsBase<FIELDS,FIELD,DESC>> {
    
    private static final String EMPTY_STR = "", SEP = ", ";
    
    private final DataDictionaryProperties dataDictionaryConfiguration;
    private final DataDictionary<META,DESC,FIELD> dataDictionary;
    private final ResponseObjectFactory<DESC,DICT,META,FIELD,FIELDS> responseObjectFactory;
    private final AccumuloConnectionService accumuloConnectionService;
    
    private final Consumer<META> TRANSFORM_EMPTY_INTERNAL_FIELD_NAMES = meta -> {
        if (meta.getInternalFieldName() == null || meta.getInternalFieldName().isEmpty()) {
            meta.setInternalFieldName(meta.getFieldName());
        }
    };
    
    public DataDictionaryWebController(DataDictionaryProperties dataDictionaryConfiguration, DataDictionary<META,DESC,FIELD> dataDictionary,
                    ResponseObjectFactory<DESC,DICT,META,FIELD,FIELDS> responseObjectFactory, AccumuloConnectionService accumloConnectionService) {
        this.dataDictionaryConfiguration = dataDictionaryConfiguration;
        this.dataDictionary = dataDictionary;
        this.responseObjectFactory = responseObjectFactory;
        this.accumuloConnectionService = accumloConnectionService;
        dataDictionary.setNormalizationMap(dataDictionaryConfiguration.getNormalizerMap());
    }
    
    /**
     * Returns the DataDictionary for the given parameters.
     *
     * @param modelName
     *            Optional model name
     * @param modelTableName
     *            Optional model table name
     * @param metadataTableName
     *            Optional metadata table name
     * @param queryAuthorizations
     *            Optional query authorizations
     * @param dataTypeFilters
     *            Optional data type filters
     * @param currentUser
     *            the current user
     * @return The ModelAndView for datadictionary.html which contains the data dictionary fields.
     * @throws Exception
     *             if there is any problem fetching the entries
     */
    @GetMapping("/")
    @Timed(name = "dw.dictionary.data.get", absolute = true)
    public ModelAndView get(@RequestParam(required = false) String modelName, @RequestParam(required = false) String modelTableName,
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
        
        ModelAndView mav = new ModelAndView();
        mav.setViewName("datadictionary");
        mav.addObject("jqueryUri", dataDictionary.getJqueryUri() + "jquery.min.js");
        mav.addObject("datatablesUri", dataDictionary.getDatatablesUri() + "jquery.dataTables.min.js");
        
        // To be passed to the MAV. Contains all the table content
        List<List<String>> tableContent = new ArrayList<List<String>>();
        for (META f : fields) {
            List<String> row = new ArrayList<String>();
            
            String fieldName = (null == f.getFieldName()) ? EMPTY_STR : f.getFieldName();
            String internalFieldName = (null == f.getInternalFieldName()) ? EMPTY_STR : f.getInternalFieldName();
            String datatype = (null == f.getDataType()) ? EMPTY_STR : f.getDataType();
            
            StringBuilder types = new StringBuilder();
            if (null != f.getTypes()) {
                for (String forwardIndexType : f.getTypes()) {
                    if (0 != types.length()) {
                        types.append(SEP);
                    }
                    types.append(forwardIndexType);
                }
            }
            row.add(fieldName);
            row.add(internalFieldName);
            row.add(datatype);
            row.add(f.isIndexOnly().toString());
            row.add(f.isForwardIndexed() ? "true" : "");
            row.add(f.isReverseIndexed() ? "true" : "");
            row.add(f.getTypes() != null && f.getTypes().size() > 0 ? "true" : "false");
            row.add(types.toString());
            row.add(((DefaultMetadataField) f).isTokenized() ? "true" : "");
            StringBuilder descriptionSB = new StringBuilder();
            boolean first = true;
            for (DescriptionBase desc : f.getDescriptions()) {
                if (!first) {
                    descriptionSB.append(", ");
                }
                descriptionSB.append(desc.getMarkings()).append(" ").append(desc.getDescription());
                first = false;
            }
            row.add(descriptionSB.toString());
            row.add(f.getLastUpdated());
            
            tableContent.add(row);
        }
        mav.addObject("tableContent", tableContent);
        
        return mav;
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
    public ModelAndView allDescriptions(@RequestParam(required = false) String modelName, @RequestParam(required = false) String modelTable,
                    @AuthenticationPrincipal ProxiedUserDetails currentUser) throws Exception {
        Connection connection = accumuloConnectionService.getConnection(modelTable, modelName, currentUser);
        Multimap<Entry<String,String>,DESC> descriptions = dataDictionary.getDescriptions(connection);
        FIELDS fields = this.responseObjectFactory.getFields();
        fields.setDescriptions(descriptions);
        
        return getDescriptionsMAV(fields);
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
    public ModelAndView datatypeDescriptions(@PathVariable("datatype") String datatype, @RequestParam(required = false) String modelName,
                    @RequestParam(required = false) String modelTable, @AuthenticationPrincipal ProxiedUserDetails currentUser) throws Exception {
        Connection connection = accumuloConnectionService.getConnection(modelTable, modelName, currentUser);
        Multimap<Entry<String,String>,DESC> descriptions = dataDictionary.getDescriptions(connection, datatype);
        FIELDS fields = this.responseObjectFactory.getFields();
        fields.setDescriptions(descriptions);
        
        return getDescriptionsMAV(fields);
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
    public ModelAndView fieldNameDescription(@PathVariable String fieldName, @PathVariable String datatype, @RequestParam(required = false) String modelName,
                    @RequestParam(required = false) String modelTable, @AuthenticationPrincipal ProxiedUserDetails currentUser) throws Exception {
        Connection connection = accumuloConnectionService.getConnection(modelTable, modelName, currentUser);
        Set<DESC> descriptions = dataDictionary.getDescriptions(connection, fieldName, datatype);
        FIELDS fields = responseObjectFactory.getFields();
        if (!descriptions.isEmpty()) {
            Multimap<Entry<String,String>,DESC> mmap = HashMultimap.create();
            for (DESC desc : descriptions) {
                mmap.put(Maps.immutableEntry(fieldName, datatype), desc);
            }
            fields.setDescriptions(mmap);
        }
        return getDescriptionsMAV(fields);
    }
    
    private ModelAndView getDescriptionsMAV(FIELDS fields) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("descriptions");
        
        int x = 0;
        String highlight;
        List<List<String>> tableContent = new ArrayList<List<String>>();
        for (FIELD field : fields.getFields()) {
            List<String> row = new ArrayList<String>(); // "highlight" or "", Datatype, FieldName, and Description
            for (DESC desc : field.getDescriptions()) {
                // highlight alternating rows
                if (x % 2 == 0) {
                    highlight = "highlight";
                } else {
                    highlight = "";
                }
                x++;
                row.add(highlight);
                row.add(field.getDatatype());
                row.add(field.getFieldName());
                row.add(desc.getDescription());
            }
            tableContent.add(row);
            
        }
        mav.addObject("tableContent", tableContent);
        
        return mav;
    }
}
