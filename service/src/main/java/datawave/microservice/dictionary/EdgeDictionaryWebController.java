package datawave.microservice.dictionary;

import static datawave.microservice.http.converter.protostuff.ProtostuffHttpMessageConverter.PROTOSTUFF_VALUE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.codahale.metrics.annotation.Timed;

import datawave.accumulo.util.security.UserAuthFunctions;
import datawave.microservice.AccumuloConnectionService;
import datawave.microservice.authorization.user.ProxiedUserDetails;
import datawave.microservice.dictionary.config.EdgeDictionaryProperties;
import datawave.microservice.dictionary.edge.EdgeDictionary;
import datawave.webservice.dictionary.edge.EdgeDictionaryBase;
import datawave.webservice.dictionary.edge.EventField;
import datawave.webservice.dictionary.edge.MetadataBase;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Edge Dictionary Controller /v1", description = "DataWave Edge Dictionary Operations",
                externalDocs = @ExternalDocumentation(description = "Dictionary Service Documentation",
                                url = "https://github.com/NationalSecurityAgency/datawave-dictionary-service"))
@Slf4j
@Controller
@RequestMapping(path = "/edge/v1", produces = {MediaType.TEXT_HTML_VALUE})
@EnableConfigurationProperties(EdgeDictionaryProperties.class)
public class EdgeDictionaryWebController<EDGE extends EdgeDictionaryBase<EDGE,META>,META extends MetadataBase<META>> {
    
    private static final String SEP = ", ";
    private final EdgeDictionaryProperties edgeDictionaryProperties;
    private final EdgeDictionary<EDGE,META> edgeDictionary;
    private final UserAuthFunctions userAuthFunctions;
    private final AccumuloConnectionService accumuloConnectionService;
    
    public EdgeDictionaryWebController(EdgeDictionaryProperties edgeDictionaryProperties, EdgeDictionary<EDGE,META> edgeDictionary,
                    UserAuthFunctions userAuthFunctions, AccumuloConnectionService accumloConnectionService) {
        this.edgeDictionaryProperties = edgeDictionaryProperties;
        this.edgeDictionary = edgeDictionary;
        this.userAuthFunctions = userAuthFunctions;
        this.accumuloConnectionService = accumloConnectionService;
    }
    
    /**
     * Returns the EdgeDictionary given a metadata table and authorizations
     *
     * @param metadataTableName
     *            Name of metadata table (Optional)
     * @param queryAuthorizations
     *            Authorizations to use
     * @return The ModelAndView for edgedictionary.html which contains the edge dictionary fields.
     * @throws Exception
     *             if there is any problem retrieving the edge dictionary from Accumulo
     */
    @GetMapping("/")
    @Timed(name = "dw.dictionary.edge.get", absolute = true)
    public ModelAndView get(@RequestParam(required = false) String metadataTableName,
                    @RequestParam(name = "auths", required = false) String queryAuthorizations, @AuthenticationPrincipal ProxiedUserDetails currentUser)
                    throws Exception {
        log.info("EDGEDICTIONARY: entered rest endpoint");
        if (null == metadataTableName || StringUtils.isBlank(metadataTableName)) {
            metadataTableName = edgeDictionaryProperties.getMetadataTableName();
        }
        
        EDGE edgeDict = edgeDictionary.getEdgeDictionary(metadataTableName, accumuloConnectionService.getConnection().getConnector(),
                        accumuloConnectionService.getDowngradedAuthorizations(queryAuthorizations, currentUser), edgeDictionaryProperties.getNumThreads());
        
        log.info("EDGEDICTIONARY: returning edge dictionary");
        
        ModelAndView mav = new ModelAndView();
        mav.setViewName("edgedictionary");
        
        int x = 0;
        String highlight;
        // To be passed to the MAV. Contains all the table content
        List<List<String>> tableContent = new ArrayList<List<String>>();
        for (MetadataBase<META> metadata : edgeDict.getMetadataList()) {
            List<String> row = new ArrayList<String>();
            // highlight alternating rows
            if (x % 2 == 0) {
                highlight = "highlight";
            } else {
                highlight = "";
            }
            x++;
            
            String type = metadata.getEdgeType();
            String relationship = metadata.getEdgeRelationship();
            String collect = metadata.getEdgeAttribute1Source();
            StringBuilder fieldBuilder = new StringBuilder();
            for (EventField field : metadata.getEventFields()) {
                fieldBuilder.append(field).append(SEP);
            }
            
            String fieldNames = fieldBuilder.toString().substring(0, fieldBuilder.length() - 2);
            String date = metadata.getStartDate();
            
            // "highlight" if row should be highlighted, "" otherwise
            row.add(highlight);
            // The data for the row
            row.add(type);
            row.add(relationship);
            row.add(collect);
            row.add(fieldNames);
            row.add(date);
            
            tableContent.add(row);
        }
        mav.addObject("tableContent", tableContent);
        
        return mav;
    }
}
