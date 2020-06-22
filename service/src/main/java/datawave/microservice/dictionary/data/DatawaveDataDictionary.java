package datawave.microservice.dictionary.data;

import com.google.common.collect.Multimap;
import datawave.webservice.query.result.metadata.MetadataFieldBase;
import datawave.webservice.results.datadictionary.DescriptionBase;
import datawave.webservice.results.datadictionary.DictionaryFieldBase;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface DatawaveDataDictionary<META extends MetadataFieldBase<META,DESC>,DESC extends DescriptionBase<DESC>,FIELD extends DictionaryFieldBase<FIELD,DESC>> {
    
    Map<String,String> getNormalizationMap();
    
    void setNormalizationMap(Map<String,String> normalizationMap);
    
    Collection<META> getFields(ConnectionConfig connectionConfig, Collection<String> dataTypeFilters, int numThreads) throws Exception;
    
    void addDescription(ConnectionConfig connectionConfig, FIELD description) throws Exception;
    
    void addDescription(ConnectionConfig connectionConfig, String fieldName, String datatype, DESC description) throws Exception;
    
    void addDescriptions(ConnectionConfig connectionConfig, String fieldName, String datatype, Set<DESC> descriptions) throws Exception;
    
    Multimap<Entry<String,String>,DESC> getDescriptions(ConnectionConfig connectionConfig) throws Exception;
    
    Multimap<Entry<String,String>,DESC> getDescriptions(ConnectionConfig connectionConfig, String datatype) throws Exception;
    
    Set<DESC> getDescriptions(ConnectionConfig connectionConfig, String fieldName, String datatype) throws Exception;
    
    void deleteDescription(ConnectionConfig connectionConfig, String fieldName, String datatype, DESC description) throws Exception;
}
