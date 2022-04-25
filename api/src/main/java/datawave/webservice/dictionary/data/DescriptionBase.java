package datawave.webservice.dictionary.data;

import io.protostuff.Message;
import datawave.webservice.query.result.event.HasMarkings;
import datawave.webservice.query.util.StringMapAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.Map;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class DescriptionBase<T> implements HasMarkings, Message<T> {
    
    @XmlElement(name = "description")
    protected String description;
    
    @XmlElement(name = "markings")
    @XmlJavaTypeAdapter(StringMapAdapter.class)
    protected Map<String,String> markings;
    
    public abstract String getDescription();
    
    public abstract void setDescription(String description);
    
}
