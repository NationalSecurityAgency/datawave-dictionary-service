package datawave.webservice.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum Direction implements Serializable {
    
    FORWARD("forward"), REVERSE("reverse");
    
    private final String value;
    
    Direction(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public static Direction getDirection(String value) {
        return Direction.valueOf(value.toUpperCase());
    }
    
}
