package datawave.microservice.model;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import datawave.accumulo.inmemory.InMemoryInstance;
import datawave.marking.MarkingFunctions;
import datawave.microservice.ControllerIT;
import datawave.microservice.model.config.ModelProperties;
import datawave.webservice.dictionary.data.DefaultDescription;
import datawave.webservice.dictionary.data.DefaultFields;
import datawave.webservice.model.FieldMapping;
import datawave.webservice.model.Model;
import datawave.webservice.model.ModelKeyParser;
import datawave.webservice.model.ModelList;
import datawave.webservice.result.VoidResponse;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.data.Mutation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelControllerTest extends ControllerIT {
    
    private static final long BATCH_WRITER_MAX_LATENCY = 1000L;
    private static final long BATCH_WRITER_MAX_MEMORY = 10845760;
    private static final int BATCH_WRITER_MAX_THREADS = 2;
    
    @Autowired
    private ModelProperties modelProperties;
    
    @ComponentScan(basePackages = "datawave.microservice")
    @Configuration
    public static class DataDictionaryImplTestConfiguration {
        @Bean
        @Qualifier("warehouse")
        public Connector warehouseConnector() throws AccumuloSecurityException, AccumuloException {
            return new InMemoryInstance().getConnector("root", new PasswordToken(""));
        }
    }
    
    @Value("classpath:TestModel_1.xml")
    private Resource model1;
    
    @Value("classpath:TestModel_2.xml")
    private Resource model2;
    
    private Model MODEL_ONE;
    private Model MODEL_TWO;
    
    @BeforeEach
    public void setUp() throws Exception {
        try {
            connector.tableOperations().create(modelProperties.getDefaultTableName());
        } catch (TableExistsException e) {
            // ignore
        }
        
        JAXBContext ctx = JAXBContext.newInstance(Model.class);
        Unmarshaller u = ctx.createUnmarshaller();
        MODEL_ONE = (datawave.webservice.model.Model) u.unmarshal(model1.getInputStream());
        MODEL_TWO = (datawave.webservice.model.Model) u.unmarshal(model2.getInputStream());
        
        BatchWriter writer = connector.createBatchWriter(modelProperties.getDefaultTableName(),
                        new BatchWriterConfig().setMaxLatency(BATCH_WRITER_MAX_LATENCY, TimeUnit.MILLISECONDS).setMaxMemory(BATCH_WRITER_MAX_MEMORY)
                                        .setMaxWriteThreads(BATCH_WRITER_MAX_THREADS));
        
        // Seed MODEL_ONE, and leave MODEL_TWO for testing import and other "addition" capabilities
        for (FieldMapping mapping : MODEL_ONE.getFields()) {
            Mutation m = ModelKeyParser.createMutation(mapping, MODEL_ONE.getName());
            try {
                writer.addMutation(m);
            } catch (MutationsRejectedException e) {
                // ignore
            }
        }
    }
    
    @AfterEach
    public void teardown() {
        try {
            connector.tableOperations().delete(modelProperties.getDefaultTableName());
        } catch (Exception e) {}
    }
    
    @Test
    public void testList() {
        // There is 1 model that is guaranteed to be there because it was seeded by the @BeforeEach
        // @formatter:off
        UriComponents uri = UriComponentsBuilder.newInstance()
                .scheme("https").host("localhost").port(webServicePort)
                .path("/dictionary/model/list")
                .build();
         // @formatter:on
        
        ResponseEntity<ModelList> response = jwtRestTemplate.exchange(adminUser, HttpMethod.GET, uri, ModelList.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getNames().size());
    }
    
    @Test
    public void testImport() {
        // Verify that there is only one model so far (the model that is seeded before the test)
        // @formatter:off
        UriComponents uri = UriComponentsBuilder.newInstance()
                .scheme("https").host("localhost").port(webServicePort)
                .path("/dictionary/model/list")
                .build();
        // @formatter:on
        
        ResponseEntity<ModelList> modelListResponse = jwtRestTemplate.exchange(adminUser, HttpMethod.GET, uri, ModelList.class);
        assertEquals(HttpStatus.OK, modelListResponse.getStatusCode());
        assertEquals(1, modelListResponse.getBody().getNames().size());
        
        // Now import a model
        // @formatter:off
        uri = UriComponentsBuilder.newInstance()
                .scheme("https").host("localhost").port(webServicePort)
                .path("/dictionary/model/import")
                .build();
        // @formatter:on
        
        HttpHeaders additionalHeaders = new HttpHeaders();
        additionalHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        RequestEntity<Model> postEntity = jwtRestTemplate.createRequestEntity(adminUser, MODEL_TWO, additionalHeaders, HttpMethod.POST, uri);
        ResponseEntity<VoidResponse> imprtResponse = jwtRestTemplate.exchange(postEntity, VoidResponse.class);
        
        assertEquals(HttpStatus.OK, imprtResponse.getStatusCode());
        
        // Now call list again, and there should be 2 models
        // @formatter:off
        uri = UriComponentsBuilder.newInstance()
                .scheme("https").host("localhost").port(webServicePort)
                .path("/dictionary/model/list")
                .build();
        // @formatter:on
        
        ResponseEntity<ModelList> modelListResponse2 = jwtRestTemplate.exchange(adminUser, HttpMethod.GET, uri, ModelList.class);
        assertEquals(HttpStatus.OK, modelListResponse2.getStatusCode());
        assertEquals(2, modelListResponse2.getBody().getNames().size());
    }
    
     @Test
    public void testGet() {
        // Verify that there is only one model so far (the model that is seeded before the test)
        // @formatter:off
        UriComponents uri = UriComponentsBuilder.newInstance()
                .scheme("https").host("localhost").port(webServicePort)
                .path("/dictionary/model/list")
                .build();
        // @formatter:on

        ResponseEntity<ModelList> modelListResponse = jwtRestTemplate.exchange(adminUser, HttpMethod.GET, uri, ModelList.class);
        assertEquals(HttpStatus.OK, modelListResponse.getStatusCode());
        assertEquals(1, modelListResponse.getBody().getNames().size());

        // Verify that we can get that model by name
        // @formatter:off
        uri = UriComponentsBuilder.newInstance()
                .scheme("https").host("localhost").port(webServicePort)
                .path("/dictionary/model/{name}")
                .queryParam("name", MODEL_ONE.getName())
                .build();
        // @formatter:on
        ResponseEntity<Model> modelResponse = jwtRestTemplate.exchange(adminUser, HttpMethod.GET, uri, Model.class);
        assertEquals(HttpStatus.OK, modelResponse.getStatusCode());
        assertEquals(MODEL_ONE.getName(), modelResponse.getBody().getName());
    }
    
//     @Test
    public void testDelete() {
        // Verify that there is only one model so far (the model that is seeded before the test)
        // @formatter:off
        UriComponents uri = UriComponentsBuilder.newInstance()
                .scheme("https").host("localhost").port(webServicePort)
                .path("/dictionary/model/list")
                .build();
        // @formatter:on
        
        ResponseEntity<ModelList> modelListResponse = jwtRestTemplate.exchange(adminUser, HttpMethod.GET, uri, ModelList.class);
        assertEquals(HttpStatus.OK, modelListResponse.getStatusCode());
        assertEquals(1, modelListResponse.getBody().getNames().size());
        
        // Call delete
        // @formatter:off
        uri = UriComponentsBuilder.newInstance()
                .scheme("https").host("localhost").port(webServicePort)
                .path("/dictionary/model/delete")
                .build();
        // @formatter:on
    }
}
