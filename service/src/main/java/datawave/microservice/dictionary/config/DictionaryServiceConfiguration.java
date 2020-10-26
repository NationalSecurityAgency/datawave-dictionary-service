package datawave.microservice.dictionary.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import datawave.accumulo.util.security.UserAuthFunctions;
import datawave.marking.MarkingFunctions;
import datawave.microservice.config.accumulo.AccumuloProperties;
import datawave.microservice.config.web.DatawaveServerProperties;
import datawave.microservice.dictionary.data.DatawaveDataDictionary;
import datawave.microservice.dictionary.data.DatawaveDataDictionaryImpl;
import datawave.microservice.dictionary.edge.DatawaveEdgeDictionary;
import datawave.microservice.dictionary.edge.DefaultDatawaveEdgeDictionaryImpl;
import datawave.microservice.metadata.MetadataDescriptionsHelper;
import datawave.microservice.metadata.MetadataDescriptionsHelperFactory;
import datawave.query.util.MetadataHelperFactory;
import datawave.webservice.query.result.metadata.DefaultMetadataField;
import datawave.webservice.results.datadictionary.DefaultDataDictionary;
import datawave.webservice.results.datadictionary.DefaultDescription;
import datawave.webservice.results.datadictionary.DefaultDictionaryField;
import datawave.webservice.results.datadictionary.DefaultFields;
import datawave.webservice.results.datadictionary.FieldsBase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@EnableConfigurationProperties(DictionaryServiceProperties.class)
public class DictionaryServiceConfiguration {
    @Bean
    @Qualifier("warehouse")
    public AccumuloProperties warehouseAccumuloProperties(DictionaryServiceProperties dictionaryServiceProperties) {
        return dictionaryServiceProperties.getAccumuloProperties();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public UserAuthFunctions userAuthFunctions() {
        return UserAuthFunctions.getInstance();
    }
    
    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public MetadataDescriptionsHelper<DefaultDescription> metadataHelperWithDescriptions(MarkingFunctions markingFunctions,
                    ResponseObjectFactory<DefaultDescription,DefaultDataDictionary,DefaultMetadataField,DefaultDictionaryField,DefaultFields> responseObjectFactory) {
        return new MetadataDescriptionsHelper<>(markingFunctions, responseObjectFactory);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public DatawaveDataDictionary datawaveDataDictionary(MarkingFunctions markingFunctions,
                    ResponseObjectFactory<DefaultDescription,DefaultDataDictionary,DefaultMetadataField,DefaultDictionaryField,DefaultFields> responseObjectFactory,
                    MetadataHelperFactory metadataHelperFactory, MetadataDescriptionsHelperFactory<DefaultDescription> metadataDescriptionsHelperFactory) {
        return new DatawaveDataDictionaryImpl(markingFunctions, responseObjectFactory, metadataHelperFactory, metadataDescriptionsHelperFactory);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public DatawaveEdgeDictionary datawaveEdgeDictionary(MetadataHelperFactory metadataHelperFactory) {
        return new DefaultDatawaveEdgeDictionaryImpl(metadataHelperFactory);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ResponseObjectFactory<DefaultDescription,DefaultDataDictionary,DefaultMetadataField,DefaultDictionaryField,DefaultFields> responseObjectFactory(
                    DatawaveServerProperties datawaveServerProperties) {
        return new ResponseObjectFactory<DefaultDescription,DefaultDataDictionary,DefaultMetadataField,DefaultDictionaryField,DefaultFields>() {
            @Override
            public DefaultDataDictionary getDataDictionary() {
                return new DefaultDataDictionary(datawaveServerProperties.getCdnUri() + "webjars/jquery/",
                                datawaveServerProperties.getCdnUri() + "webjars/ag-grid-community/");
            }
            
            @Override
            public DefaultDescription getDescription() {
                return new DefaultDescription();
            }
            
            @Override
            public DefaultFields getFields() {
                return new DefaultFields();
            }
        };
    }
    
    /**
     * Provides a {@link Jackson2ObjectMapperBuilderCustomizer} that adds the mix-in {@link FieldsBaseMixIn} for {@link FieldsBase} objects. This is here to
     * override Jackson de-serialization behavior for methods that receive a {@link FieldsBase} as a request body. We cannot de-serialize the payload as an
     * abstract object, so instead override it to the provided concrete type. If you wish to override this to provide an alternate implementation of
     * {@link FieldsBase}, then you can simply re-define this bean to supply a different mix-in class. When re-defining, you must use the same name
     * `dictionaryJacksonCustomizer`.
     *
     * @return a {@link Jackson2ObjectMapperBuilderCustomizer} that adds the mix-in {@link FieldsBaseMixIn} for {@link FieldsBase} objects.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer dictionaryJacksonCustomizer() {
        return c -> c.mixIn(FieldsBase.class, FieldsBaseMixIn.class);
    }
    
    /**
     * A Jackson mix-in to specify that {@link FieldsBase} parameters should be de-serialized as though the parameter were an instance of {@link DefaultFields}
     * instead.
     */
    @JsonDeserialize(as = DefaultFields.class)
    public static class FieldsBaseMixIn {}
}
