package datawave.webservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.builder.CompareToBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.NONE)
public class FieldMapping implements Serializable, Comparable<FieldMapping> {
    
    private static final long serialVersionUID = 1L;
    
    @XmlAttribute(required = true)
    private String datatype;
    
    @XmlAttribute(required = true)
    private String fieldName;
    
    @XmlAttribute(required = true)
    private String modelFieldName;
    
    @XmlAttribute(required = true)
    private Direction direction;
    
    @XmlAttribute(required = true)
    private String columnVisibility;
    
    @Override
    public int compareTo(FieldMapping obj) {
        
        if (obj == null) {
            throw new IllegalArgumentException("can not compare null");
        }
        
        if (obj == this)
            return 0;
        
        return new CompareToBuilder().append(datatype, ((FieldMapping) obj).datatype).append(fieldName, ((FieldMapping) obj).fieldName)
                        .append(modelFieldName, ((FieldMapping) obj).modelFieldName).append(direction, ((FieldMapping) obj).direction)
                        .append(columnVisibility, ((FieldMapping) obj).columnVisibility).toComparison();
    }
    
}
