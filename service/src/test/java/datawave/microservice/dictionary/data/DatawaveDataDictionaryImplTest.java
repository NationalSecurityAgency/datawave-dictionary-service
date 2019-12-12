package datawave.microservice.dictionary.data;

import datawave.accumulo.inmemory.InMemoryAccumuloClient;
import datawave.accumulo.inmemory.InMemoryInstance;
import datawave.webservice.query.result.metadata.DefaultMetadataField;
import datawave.webservice.results.datadictionary.DefaultDescription;
import datawave.webservice.results.datadictionary.DefaultDictionaryField;
import datawave.webservice.results.datadictionary.DefaultFields;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = "spring.main.allow-bean-definition-overriding=true")
public class DatawaveDataDictionaryImplTest {
    
    private static final String model = "model";
    private static final String modelTable = "modelTable";
    private static final String metaTable = "metaTable";
    
    private final String[] auths = {"PRIVATE"};
    
    private Set<Authorizations> setOfAuthObjs = Collections.singleton(new Authorizations(auths));
    
    @Autowired
    @Qualifier("warehouse")
    private AccumuloClient accumuloClient;
    
    @Autowired
    private DatawaveDataDictionary<DefaultMetadataField,DefaultDescription,DefaultDictionaryField> impl;
    
    @BeforeEach
    public void setup() throws AccumuloException, AccumuloSecurityException, TableExistsException {
        accumuloClient.securityOperations().changeUserAuthorizations("root", new Authorizations(auths));
        accumuloClient.tableOperations().create(metaTable);
        accumuloClient.tableOperations().create(modelTable);
    }
    
    @Test
    public void testSetDescription() throws Exception {
        
        Map<String,String> markings = new HashMap<>();
        markings.put("columnVisibility", "PRIVATE");
        
        DefaultDescription desc = new DefaultDescription();
        desc.setMarkings(markings);
        desc.setDescription("my ultra cool description");
        
        Set<DefaultDescription> descs = new HashSet<>();
        descs.add(desc);
        
        DefaultDictionaryField dicField = new DefaultDictionaryField();
        dicField.setDatatype("myType");
        dicField.setFieldName("myField");
        dicField.setDescriptions(descs);
        
        List<DefaultDictionaryField> dicFields = new ArrayList<>();
        dicFields.add(dicField);
        
        DefaultFields fields = new DefaultFields();
        fields.setFields(dicFields);
        
        impl.setDescription(accumuloClient, metaTable, setOfAuthObjs, model, modelTable, dicField);
        
        Scanner s = accumuloClient.createScanner(metaTable, new Authorizations(auths));
        
        for (Map.Entry<Key,Value> entry : s) {
            assertEquals("PRIVATE", entry.getKey().getColumnVisibility().toString());
        }
    }
    
    @ComponentScan(basePackages = "datawave.microservice")
    @Configuration
    public static class DataDictionaryImplTestConfiguration {
        @Bean
        @Qualifier("warehouse")
        public AccumuloClient warehouseClient() throws AccumuloSecurityException, AccumuloException {
            return new InMemoryAccumuloClient("root", new InMemoryInstance());
        }
    }
}
