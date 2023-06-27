package datawave.microservice.dictionary.config;

import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "datawave.dictionary.data")
@Validated
public class DataDictionaryProperties {
    
    @NotBlank
    private String modelName;
    @NotBlank
    private String modelTableName;
    @NotBlank
    private String metadataTableName;
    @Positive
    private int numThreads;
    private Map<String,String> normalizerMap;
    
}
