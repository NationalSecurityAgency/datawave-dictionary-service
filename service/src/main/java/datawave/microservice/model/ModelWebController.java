package datawave.microservice.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.security.Authorizations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Sets;

import datawave.microservice.AccumuloConnectionService;
import datawave.microservice.authorization.user.ProxiedUserDetails;
import datawave.microservice.http.converter.protostuff.ProtostuffHttpMessageConverter;
import datawave.microservice.model.config.ModelProperties;
import datawave.webservice.model.FieldMapping;
import datawave.webservice.model.Model;
import datawave.webservice.model.ModelKeyParser;
import datawave.webservice.model.ModelList;
import datawave.webservice.query.exception.DatawaveErrorCode;
import datawave.webservice.query.exception.QueryException;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * Service that supports manipulation of models. The models are contained in the data dictionary table.
 */
@Tag(name = "Model Controller /v1", description = "DataWave Model Operations",
                externalDocs = @ExternalDocumentation(description = "Dictionary Service Documentation",
                                url = "https://github.com/NationalSecurityAgency/datawave-dictionary-service"))
@Slf4j
@Controller
@RequestMapping(path = "/model", produces = {MediaType.TEXT_HTML_VALUE})
@Secured({"AuthorizedUser", "AuthorizedQueryServer", "InternalUser", "Administrator", "JBossAdministrator"})
@EnableConfigurationProperties(ModelProperties.class)
public class ModelWebController {
    
    private final String dataTablesUri;
    private final String jqueryUri;
    private final AccumuloConnectionService accumloConnectionService;
    
    public static final String DEFAULT_MODEL_TABLE_NAME = "DatawaveMetadata";
    private static final HashSet<String> RESERVED_COLF_VALUES = Sets.newHashSet("e", "i", "ri", "f", "tf", "m", "desc", "edge", "t", "n", "h");
    
    public ModelWebController(ModelProperties modelProperties, AccumuloConnectionService accumloConnectionService) {
        this.dataTablesUri = modelProperties.getDefaultTableName();
        this.jqueryUri = modelProperties.getJqueryUri();
        this.accumloConnectionService = accumloConnectionService;
    }
    
    /**
     * Get the names of the models
     *
     * @param modelTableName
     *            name of the table that contains the model
     * @return the ModelAndView
     * @RequestHeader X-ProxiedEntitiesChain use when proxying request for user
     *            
     * @HTTP 200 success
     * @HTTP 500 internal server error
     */
    @GetMapping("/list") // If we get to change to follow true REST standard, this would just be / and remain a GET
    public ModelAndView listModelNames(@RequestParam(defaultValue = DEFAULT_MODEL_TABLE_NAME) String modelTableName,
                    @AuthenticationPrincipal ProxiedUserDetails currentUser) {
        
        ModelList modelList = new ModelList(modelTableName);
        ModelAndView mav = new ModelAndView();
        HashSet<String> modelNames = new HashSet<>();
        List<Key> keys = null;
        
        mav.setViewName("modelnames");
        
        try {
            keys = accumloConnectionService.getKeys(modelTableName, currentUser, "");
        } catch (TableNotFoundException e) {
            QueryException qe = new QueryException(DatawaveErrorCode.MODEL_NAME_LIST_ERROR, e);
            log.error(qe.getMessage());
            modelList.addException(qe.getBottomQueryException());
            return mav; // Return the MAV without data
        }
        
        Set<String> colFamilies = keys.stream().map(k -> k.getColumnFamily().toString()).filter(colFamily -> !RESERVED_COLF_VALUES.contains(colFamily))
                        .collect(Collectors.toSet());
        
        for (String colf : colFamilies) {
            String[] parts = colf.split(ModelKeyParser.NULL_BYTE);
            if (parts.length == 1) {
                modelNames.add(colf);
            } else if (parts.length == 2) {
                modelNames.add(parts[0]);
            }
        }
        
        modelList.setNames(modelNames);
        
        mav.addObject("names", modelList.getNames());
        mav.addObject("modelTableName", modelList.getModelTableName());
        
        return mav;
    }
    
    /**
     * Retrieve the model and all of its mappings
     *
     * @param name
     *            model name
     * @param modelTableName
     *            name of the table that contains the model
     * @return the ModelAndView
     * @RequestHeader X-ProxiedEntitiesChain use when proxying request for user
     *            
     * @HTTP 200 success
     * @HTTP 404 model not found
     * @HTTP 500 internal server error
     */
    @GetMapping("/{name}")
    public ModelAndView getModel(@RequestParam String name, @RequestParam(defaultValue = DEFAULT_MODEL_TABLE_NAME) String modelTableName,
                    @AuthenticationPrincipal ProxiedUserDetails currentUser) {
        Model model = new Model();
        ModelAndView mav = new ModelAndView();
        List<List<String>> tableContent = new ArrayList<List<String>>();
        
        mav.setViewName("modeldescription");
        
        List<Key> keys = null;
        try {
            keys = accumloConnectionService.getKeys(modelTableName, currentUser, name);
        } catch (TableNotFoundException e) {
            QueryException qe = new QueryException(DatawaveErrorCode.MODEL_NAME_LIST_ERROR, e);
            log.error(qe.getMessage());
            model.addException(qe.getBottomQueryException());
            return mav; // Return the MAV without data
        }
        
        Set<Authorizations> auths = accumloConnectionService.getConnection(modelTableName, name, currentUser).getAuths();
        TreeSet<FieldMapping> fields = model.getFields();
        keys.forEach(k -> fields.add(ModelKeyParser.parseKey(k, auths)));
        model.setName(name);
        
        for (FieldMapping fieldMapping : fields) {
            List<String> row = new ArrayList<String>();
            row.add(fieldMapping.getColumnVisibility());
            row.add(fieldMapping.getFieldName());
            row.add(fieldMapping.getDatatype());
            row.add(fieldMapping.getModelFieldName());
            row.add(fieldMapping.getDirection().getValue().toUpperCase());
            tableContent.add(row);
        }
        
        mav.addObject("tableContent", tableContent);
        
        return mav;
    }
}
