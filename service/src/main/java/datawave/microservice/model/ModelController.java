package datawave.microservice.model;

import com.google.common.collect.Sets;
//import datawave.interceptor.RequiredInterceptor;
//import datawave.interceptor.ResponseInterceptor;
//import datawave.security.authorization.DatawavePrincipal;
//import datawave.services.common.cache.AccumuloTableCache;
//import datawave.services.common.connection.AccumuloConnectionFactory;
//import datawave.webservice.common.exception.DatawaveWebApplicationException;
//import datawave.webservice.common.exception.NotFoundException;
//import datawave.webservice.common.exception.PreConditionFailedException;
import datawave.microservice.AccumuloConnectionService;
import datawave.microservice.Connection;
import datawave.microservice.authorization.user.ProxiedUserDetails;
import datawave.microservice.http.converter.protostuff.ProtostuffHttpMessageConverter;
import datawave.microservice.model.config.ModelProperties;
import datawave.webservice.model.FieldMapping;
import datawave.webservice.model.Model;
import datawave.webservice.model.ModelKeyParser;
import datawave.webservice.model.ModelList;
import datawave.webservice.query.exception.DatawaveErrorCode;
import datawave.webservice.query.exception.QueryException;
import datawave.webservice.result.VoidResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
//import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.accumulo.core.iterators.user.RegExFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service that supports manipulation of models. The models are contained in the data dictionary table.
 */
@Slf4j
@RestController
@RequestMapping(path = "/model", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE,
        ProtostuffHttpMessageConverter.PROTOSTUFF_VALUE, MediaType.TEXT_HTML_VALUE, "text/x-yaml", "application/x-yaml"})
@Secured({"AuthorizedUser", "AuthorizedQueryServer", "InternalUser", "Administrator", "JBossAdministrator"})
@EnableConfigurationProperties(ModelProperties.class)
public class ModelController {
    
    private final String dataTablesUri;
    private final String jqueryUri;
    private final AccumuloConnectionService accumloConnectionService;
    
    public static final String DEFAULT_MODEL_TABLE_NAME = "DatawaveMetadata";
    
    private static final long BATCH_WRITER_MAX_LATENCY = 1000L;
    private static final long BATCH_WRITER_MAX_MEMORY = 10845760;
    private static final int BATCH_WRITER_MAX_THREADS = 2;
    
    private static final HashSet<String> RESERVED_COLF_VALUES = Sets.newHashSet("e", "i", "ri", "f", "tf", "m", "desc", "edge", "t", "n", "h");
    
    public ModelController(ModelProperties modelProperties, AccumuloConnectionService accumloConnectionService) {
        this.dataTablesUri = modelProperties.getDefaultTableName();
        this.jqueryUri = modelProperties.getJqueryUri();
        this.accumloConnectionService = accumloConnectionService;
    }
    
    /**
     * Get the names of the models
     *
     * @param modelTableName
     *            name of the table that contains the model
     * @return ModelList
     * @RequestHeader X-ProxiedEntitiesChain use when proxying request for user
     * 
     * @HTTP 200 success
     * @HTTP 500 internal server error
     */
    @GetMapping("/list") // If we get to change to follow true REST standard, this would just be / and remain a GET
    public ModelList listModelNames(@RequestParam(defaultValue = DEFAULT_MODEL_TABLE_NAME) String modelTableName,
                    @AuthenticationPrincipal ProxiedUserDetails currentUser) {
        
        ModelList response = new ModelList(jqueryUri, dataTablesUri, modelTableName);
        HashSet<String> modelNames = new HashSet<>();
        
        try (Scanner scanner = accumloConnectionService.getScanner(modelTableName, currentUser)) {
            for (Map.Entry<Key,Value> entry : scanner) {
                String colf = entry.getKey().getColumnFamily().toString();
                if (!RESERVED_COLF_VALUES.contains(colf) && !modelNames.contains(colf)) {
                    String[] parts = colf.split(ModelKeyParser.NULL_BYTE);
                    if (parts.length == 1) {
                        modelNames.add(colf);
                    } else if (parts.length == 2) {
                        modelNames.add(parts[0]);
                    }
                }
                
            }
        } catch (TableNotFoundException e) {
            QueryException qe = new QueryException(DatawaveErrorCode.MODEL_NAME_LIST_ERROR, e);
            log.error(qe.getMessage());
            response.addException(qe.getBottomQueryException());
        }
        
        response.setNames(modelNames);
        return response;
    }
    
    /**
     * <strong>Administrator credentials required.</strong> Delete a model with the supplied name
     *
     * @param name
     *            model name to delete
     * @param modelTableName
     *            name of the table that contains the model
     * @return datawave.webservice.result.VoidResponse
     * @RequestHeader X-ProxiedEntitiesChain use when proxying request for user
     *
     * @HTTP 200 success
     * @HTTP 404 model not found
     * @HTTP 500 internal server error
     */
    @DeleteMapping("/{name}")
    @Secured({"Administrator", "JBossAdministrator"})
    public VoidResponse deleteModel(@RequestParam String name, @RequestParam(defaultValue = DEFAULT_MODEL_TABLE_NAME) String modelTableName,
                    @AuthenticationPrincipal ProxiedUserDetails currentUser) {
        VoidResponse response = new VoidResponse();
        ModelList models = listModelNames(modelTableName, currentUser);
        if (models.getNames().contains(name)) {
            // the specified model exists, so we can proceed with deleting it
            Model model = getModel(name, modelTableName, currentUser);
            deleteMapping(model, modelTableName, currentUser);
        }
        
        return response;
    }
    
    /**
     * <strong>Administrator credentials required.</strong> Copy a model
     *
     * @param name
     *            model to copy
     * @param newName
     *            name of copied model
     * @param modelTableName
     *            name of the table that contains the model
     * @return datawave.webservice.result.VoidResponse
     * @RequestHeader X-ProxiedEntitiesChain use when proxying request for user
     *
     * @HTTP 200 success
     * @HTTP 204 model not found
     * @HTTP 500 internal server error
     */
    @PostMapping("/clone")
    @Secured({"Administrator", "JBossAdministrator"})
    public VoidResponse cloneModel(@RequestParam String name, @RequestParam String newName,
                    @RequestParam(defaultValue = DEFAULT_MODEL_TABLE_NAME) String modelTableName, @AuthenticationPrincipal ProxiedUserDetails currentUser) {
        VoidResponse response = new VoidResponse();
        
        Model model = getModel(name, modelTableName, currentUser);
        // Set the new name
        model.setName(newName);
        insertMapping(model, modelTableName, currentUser);
        return response;
    }
    
    /**
     * Retrieve the model and all of its mappings
     *
     * @param name
     *            model name
     * @param modelTableName
     *            name of the table that contains the model
     * @return Model
     * @RequestHeader X-ProxiedEntitiesChain use when proxying request for user
     *
     * @HTTP 200 success
     * @HTTP 404 model not found
     * @HTTP 500 internal server error
     */
    @GetMapping("/{name}")
    public Model getModel(@RequestParam String name, @RequestParam(defaultValue = DEFAULT_MODEL_TABLE_NAME) String modelTableName,
                    @AuthenticationPrincipal ProxiedUserDetails currentUser) {
        Model response = new Model(jqueryUri, dataTablesUri);
        try (Scanner scanner = accumloConnectionService.getScannerWithRegexIteratorSetting(name, modelTableName, currentUser)) {
            for (Map.Entry<Key,Value> entry : scanner) {
                // FieldMapping mapping = ModelKeyParser.parseKey(entry.getKey(), cbAuths);
                // response.getFields().add(mapping);
            }
        } catch (TableNotFoundException e) {
            QueryException qe = new QueryException(DatawaveErrorCode.MODEL_NAME_LIST_ERROR, e);
            log.error(qe.getMessage());
            response.addException(qe.getBottomQueryException());
        }
        response.setName(name);
        return response;
    }
    
    /**
     * <strong>Administrator credentials required.</strong> Insert a new field mapping into an existing model
     *
     * @param model
     *            list of new field mappings to insert
     * @param modelTableName
     *            name of the table that contains the model
     * @return datawave.webservice.result.VoidResponse
     * @RequestHeader X-ProxiedEntitiesChain use when proxying request for user
     *
     * @HTTP 200 success
     * @HTTP 500 internal server error
     */
    @PostMapping(value = {"/insert", "/import"}) // If we get to change to standard REST, this would just be / and rely on the
    @Secured({"Administrator", "JBossAdministrator"})
    public VoidResponse insertMapping(@RequestBody Model model, @RequestParam(defaultValue = DEFAULT_MODEL_TABLE_NAME) String modelTableName,
                    @AuthenticationPrincipal ProxiedUserDetails currentUser) {
        if (log.isDebugEnabled()) {
            log.debug("modelTableName: " + (null == modelTableName ? "" : modelTableName));
        }
        
        VoidResponse response = new VoidResponse();
        ModelList models = listModelNames(modelTableName, currentUser);
        if (models.getNames().contains(model.getName())) {
            // the model already exists -- nothing to do
            return response;
        }
        
        BatchWriter writer = null;
        
        Connection connection = accumloConnectionService.getConnection(modelTableName, model.getName(), currentUser);
        try {
            // Is the BatchWriterConfig already a spring bean?
            writer = connection.getConnector().createBatchWriter(modelTableName,
                            new BatchWriterConfig().setMaxLatency(BATCH_WRITER_MAX_LATENCY, TimeUnit.MILLISECONDS).setMaxMemory(BATCH_WRITER_MAX_MEMORY)
                                            .setMaxWriteThreads(BATCH_WRITER_MAX_THREADS));
        } catch (TableNotFoundException e) {
            log.error("The " + modelTableName + " could not be found to write to ", e);
            QueryException qe = new QueryException(DatawaveErrorCode.TABLE_NOT_FOUND, e);
            response.addException(qe.getBottomQueryException());
        }
        
        if (writer != null) {
            for (FieldMapping mapping : model.getFields()) {
                Mutation m = ModelKeyParser.createMutation(mapping, model.getName());
                try {
                    writer.addMutation(m);
                } catch (MutationsRejectedException e) {
                    // So that we know exactly which mapping causes the error, catch here, but break out of loop
                    // Unfortunately, this does mean if there are multiple mappings that cause this error, only the first will be logged.
                    log.error("Could not insert mapping  -- " + mapping, e);
                    QueryException qe = new QueryException(DatawaveErrorCode.INSERT_MAPPING_ERROR, e);
                    response.addException(qe.getBottomQueryException());
                } finally {
                    // we know the writer isn't null since we've already checked.
                    try {
                        writer.close();
                    } catch (MutationsRejectedException e) {
                        log.error("Error closing the BatchWriter; ", e);
                    }
                }
                
            }
        }
        
        // Do we already have an AccumuloTableCache bean somewhere?
        // cache.reloadTableCache(tableName);
        return response;
    }
    
    /**
     * <strong>Administrator credentials required.</strong> Delete field mappings from an existing model
     *
     * @param model
     *            list of field mappings to delete
     * @param modelTableName
     *            name of the table that contains the model
     * @return datawave.webservice.result.VoidResponse
     * @RequestHeader X-ProxiedEntitiesChain use when proxying request for user
     *
     * @HTTP 200 success
     * @HTTP 500 internal server error
     */
    @DeleteMapping("/delete")
    @Secured({"Administrator", "JBossAdministrator"})
    public VoidResponse deleteMapping(@RequestParam Model model, @RequestParam String modelTableName, @AuthenticationPrincipal ProxiedUserDetails currentUser) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting model name: " + model.getName() + "from modelTableName " + modelTableName);
        }
        VoidResponse response = new VoidResponse();
        //
        // Connector connector = null;
        // BatchWriter writer = null;
        // try {
        // Map<String,String> trackingMap = connectionFactory.getTrackingMap(Thread.currentThread().getStackTrace());
        // connector = connectionFactory.getConnection(getCurrentUserDN(), getCurrentProxyServers(), AccumuloConnectionFactory.Priority.LOW, trackingMap);
        // writer = connector.createBatchWriter(modelTableName, new BatchWriterConfig().setMaxLatency(BATCH_WRITER_MAX_LATENCY, TimeUnit.MILLISECONDS)
        // .setMaxMemory(BATCH_WRITER_MAX_MEMORY).setMaxWriteThreads(BATCH_WRITER_MAX_THREADS));
        // for (FieldMapping mapping : model.getFields()) {
        // Mutation m = ModelKeyParser.createDeleteMutation(mapping, model.getName());
        // writer.addMutation(m);
        // }
        // } catch (Exception e) {
        // log.error("Could not delete mapping.", e);
        // QueryException qe = new QueryException(DatawaveErrorCode.MAPPING_DELETION_ERROR, e);
        // response.addException(qe.getBottomQueryException());
        // throw new DatawaveWebApplicationException(qe, response);
        // } finally {
        // if (null != writer) {
        // try {
        // writer.close();
        // } catch (MutationsRejectedException e1) {
        // QueryException qe = new QueryException(DatawaveErrorCode.WRITER_CLOSE_ERROR, e1);
        // log.error(qe);
        // response.addException(qe);
        // throw new DatawaveWebApplicationException(qe, response);
        // }
        // }
        // if (null != connector) {
        // try {
        // connectionFactory.returnConnection(connector);
        // } catch (Exception e) {
        // log.error("Error returning connection to factory", e);
        // }
        // }
        // }
        // if (reloadCache)
        // cache.reloadTableCache(tableName);
        return response;
    }
    
    // public String getCurrentUserDN() {
    // String currentUserDN = null;
    // Principal p = ctx.getCallerPrincipal();
    //
    // if (p != null && p instanceof DatawavePrincipal) {
    // currentUserDN = ((DatawavePrincipal) p).getUserDN().subjectDN();
    // }
    //
    // return currentUserDN;
    // }
    //
    // public Collection<String> getCurrentProxyServers() {
    // Set<String> currentProxyServers = null;
    // Principal p = ctx.getCallerPrincipal();
    //
    // if (p != null && p instanceof DatawavePrincipal) {
    // currentProxyServers = ((DatawavePrincipal) p).getProxyServers();
    // }
    //
    // return currentProxyServers;
    // }
    
}
