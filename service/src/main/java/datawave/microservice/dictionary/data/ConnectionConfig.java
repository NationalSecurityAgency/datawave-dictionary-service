package datawave.microservice.dictionary.data;

import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Objects;
import java.util.Set;

public class ConnectionConfig {
    
    private AccumuloClient connector;
    
    private Set<Authorizations> auths;
    
    private String metadataTable;
    
    private String modelTable;
    
    private String modelName;
    
    public AccumuloClient getClient() {
        return connector;
    }
    
    public void setClient(AccumuloClient connector) {
        this.connector = connector;
    }
    
    public Set<Authorizations> getAuths() {
        return auths;
    }
    
    public void setAuths(Set<Authorizations> authorizations) {
        this.auths = authorizations;
    }
    
    public String getMetadataTable() {
        return metadataTable;
    }
    
    public void setMetadataTable(String metadataTable) {
        this.metadataTable = metadataTable;
    }
    
    public String getModelTable() {
        return modelTable;
    }
    
    public void setModelTable(String modelTable) {
        this.modelTable = modelTable;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConnectionConfig that = (ConnectionConfig) o;
        return Objects.equals(connector, that.connector) && Objects.equals(auths, that.auths) && Objects.equals(metadataTable, that.metadataTable)
                        && Objects.equals(modelTable, that.modelTable) && Objects.equals(modelName, that.modelName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(connector, auths, metadataTable, modelTable, modelName);
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("connector", connector).append("auths", auths).append("metadataTable", metadataTable)
                        .append("modelTable", modelTable).append("modelName", modelName).toString();
    }
}
