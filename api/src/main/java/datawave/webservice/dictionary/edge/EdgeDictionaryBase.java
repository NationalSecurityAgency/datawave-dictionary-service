package datawave.webservice.dictionary.edge;

import java.util.List;

import io.protostuff.Message;
import datawave.webservice.result.BaseResponse;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSeeAlso;

@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso(DefaultEdgeDictionary.class)
public abstract class EdgeDictionaryBase<T,F extends MetadataBase<F>> extends BaseResponse implements Message<T> {
    
    private static final long serialVersionUID = 1L;
    
    public abstract List<? extends MetadataBase<F>> getMetadataList();
    
    public abstract void setTotalResults(long totalResults);
    
    public abstract long getTotalResults();
    
    public abstract String getTitle();
    
    public abstract String getHeadContent();
    
    public abstract String getPageHeader();
    
    public abstract String getMainContent();
    
}