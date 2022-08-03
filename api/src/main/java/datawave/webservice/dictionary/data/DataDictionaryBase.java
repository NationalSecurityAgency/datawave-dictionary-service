package datawave.webservice.dictionary.data;

import datawave.webservice.metadata.MetadataFieldBase;
import datawave.webservice.result.BaseResponse;
import datawave.webservice.result.TotalResultsAware;
import io.protostuff.Message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso(DefaultDataDictionary.class)
public abstract class DataDictionaryBase<T,M extends MetadataFieldBase> extends BaseResponse implements TotalResultsAware, Message<T> {
    
    public abstract List<M> getFields();
    
    public abstract void setFields(Collection<M> fields);
    
    public abstract void setTotalResults(long totalResults);
    
    public abstract long getTotalResults();
    
    public abstract void transformFields(final Consumer<M> transformer);
}
