package datawave.microservice.dictionary.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Getter
@Setter
@ConfigurationProperties(prefix = "datawave.dictionary.edge")
@Validated
public class EdgeDictionaryProperties {
    
    @NotBlank
    private String metadataTableName;
    @Positive
    private int numThreads;
}
