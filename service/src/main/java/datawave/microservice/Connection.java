package datawave.microservice;

import lombok.Data;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.security.Authorizations;

import java.util.Set;

@Data
public class Connection {
    
    private AccumuloClient accumuloClient;
    private Set<Authorizations> auths;
    private String metadataTable;
    private String modelTable;
    private String modelName;
}
