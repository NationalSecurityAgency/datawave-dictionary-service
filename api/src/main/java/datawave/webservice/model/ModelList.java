package datawave.webservice.model;

import datawave.webservice.result.BaseResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;

@Data
@NoArgsConstructor
@XmlRootElement(name = "ModelList")
public class ModelList extends BaseResponse implements Serializable {
    
    private String modelTableName;
    
    private static final long serialVersionUID = 1L;
    
    public ModelList(String modelTableName) {
        this.modelTableName = modelTableName;
    }
    
    @XmlElementWrapper(name = "ModelNames")
    @XmlElement(name = "ModelName")
    private HashSet<String> names;
}
