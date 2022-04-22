package datawave.webservice.dictionary.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.protostuff.Message;
import datawave.webservice.HtmlProvider;
import datawave.webservice.metadata.MetadataFieldBase;
import datawave.webservice.result.BaseResponse;
import datawave.webservice.result.TotalResultsAware;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({@JsonSubTypes.Type(value = DefaultDataDictionary.class, name = "DefaultDataDictionary")})
public abstract class DataDictionaryBase<T,M extends MetadataFieldBase> extends BaseResponse implements TotalResultsAware, Message<T>, HtmlProvider {
    
    public abstract List<M> getFields();
    
    public abstract void setFields(Collection<M> fields);
    
    public abstract void setTotalResults(long totalResults);
    
    public abstract long getTotalResults();
    
    public abstract String getTitle();
    
    public abstract String getHeadContent();
    
    public abstract String getPageHeader();
    
    public abstract String getMainContent();
    
    public abstract void transformFields(final Consumer<M> transformer);
}
