package datawave.webservice.model;

import jakarta.xml.bind.annotation.XmlEnum;

import java.io.Serializable;

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
