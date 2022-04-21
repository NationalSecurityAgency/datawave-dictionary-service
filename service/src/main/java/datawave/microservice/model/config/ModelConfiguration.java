package datawave.microservice.model.config;

import datawave.microservice.config.accumulo.AccumuloProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ModelProperties.class)
public class ModelConfiguration {
    @Bean
    @Qualifier("warehouse")
    public AccumuloProperties warehouseAccumuloProperties() {
        return new AccumuloProperties();
    }
    
}
