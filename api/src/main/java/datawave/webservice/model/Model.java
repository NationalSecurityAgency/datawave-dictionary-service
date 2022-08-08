package datawave.webservice.model;

import datawave.webservice.result.BaseResponse;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.TreeSet;

@Data
@XmlRootElement(name = "Model")
@XmlAccessorType(XmlAccessType.NONE)
public class Model extends BaseResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public Model() {};
    
    @XmlAttribute(name = "name", required = true)
    private String name;
    
    @XmlElementWrapper(name = "Mappings")
    @XmlElement(name = "Mapping")
    private TreeSet<FieldMapping> fields = new TreeSet<FieldMapping>();
}
