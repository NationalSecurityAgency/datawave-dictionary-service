package datawave.microservice.dictionary.edge;

import datawave.webservice.dictionary.edge.EdgeDictionaryBase;
import datawave.webservice.dictionary.edge.MetadataBase;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.security.Authorizations;

import java.util.Set;

public interface EdgeDictionary<EDGE extends EdgeDictionaryBase<EDGE,META>,META extends MetadataBase<META>> {
    char COL_SEPARATOR = '/';
    
    EDGE getEdgeDictionary(String metadataTableName, AccumuloClient accumuloClient, Set<Authorizations> auths, int numThreads) throws Exception;
}
