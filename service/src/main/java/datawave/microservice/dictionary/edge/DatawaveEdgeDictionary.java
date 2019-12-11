package datawave.microservice.dictionary.edge;

import datawave.webservice.results.edgedictionary.EdgeDictionaryBase;
import datawave.webservice.results.edgedictionary.MetadataBase;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.security.Authorizations;

import java.util.Set;

public interface DatawaveEdgeDictionary<EDGE extends EdgeDictionaryBase<EDGE,META>,META extends MetadataBase<META>> {
    char COL_SEPARATOR = '/';
    
    EDGE getEdgeDictionary(String metadataTableName, AccumuloClient accumuloClient, Set<Authorizations> auths, int numThreads) throws Exception;
}
