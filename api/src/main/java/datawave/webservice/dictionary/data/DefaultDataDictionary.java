package datawave.webservice.dictionary.data;

import com.google.common.collect.Lists;
import datawave.webservice.metadata.DefaultMetadataField;
import datawave.webservice.result.TotalResultsAware;
import io.protostuff.Input;
import io.protostuff.Message;
import io.protostuff.Output;
import io.protostuff.Schema;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@XmlRootElement(name = "DefaultDataDictionary")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class DefaultDataDictionary extends DataDictionaryBase<DefaultDataDictionary,DefaultMetadataField>
                implements TotalResultsAware, Message<DefaultDataDictionary> {
    
    private static final long serialVersionUID = 1L;
    private final String jqueryUri;
    private final String datatablesUri;
    
    @XmlElementWrapper(name = "MetadataFields")
    @XmlElement(name = "MetadataField")
    private List<DefaultMetadataField> fields = null;
    
    @XmlElement(name = "TotalResults")
    private Long totalResults = null;
    
    public DefaultDataDictionary() {
        this("/webjars/jquery/", "/webjars/datatables/");
    }
    
    public DefaultDataDictionary(String jqueryUri, String datatablesUri) {
        this.jqueryUri = jqueryUri;
        this.datatablesUri = datatablesUri;
    }
    
    public DefaultDataDictionary(Collection<DefaultMetadataField> fields) {
        this();
        if (fields == null) {
            this.fields = null;
            setTotalResults(0);
        } else {
            this.fields = new ArrayList<>(fields);
            setTotalResults(this.fields.size());
            this.setHasResults(true);
        }
    }
    
    public String getJqueryUri() {
        return jqueryUri;
    }
    
    public String getDatatablesUri() {
        return datatablesUri;
    }
    
    public List<DefaultMetadataField> getFields() {
        return fields == null ? null : Collections.unmodifiableList(fields);
    }
    
    public void setFields(Collection<DefaultMetadataField> fields) {
        this.fields = Lists.newArrayList(fields);
    }
    
    public static Schema<DefaultDataDictionary> getSchema() {
        return SCHEMA;
    }
    
    @Override
    public Schema<DefaultDataDictionary> cachedSchema() {
        return SCHEMA;
    }
    
    @XmlTransient
    private static final Schema<DefaultDataDictionary> SCHEMA = new Schema<DefaultDataDictionary>() {
        public DefaultDataDictionary newMessage() {
            return new DefaultDataDictionary();
        }
        
        public Class<DefaultDataDictionary> typeClass() {
            return DefaultDataDictionary.class;
        }
        
        public String messageName() {
            return DefaultDataDictionary.class.getSimpleName();
        }
        
        public String messageFullName() {
            return DefaultDataDictionary.class.getName();
        }
        
        public boolean isInitialized(DefaultDataDictionary message) {
            return true;
        }
        
        public void writeTo(Output output, DefaultDataDictionary message) throws IOException {
            if (message.totalResults != null) {
                output.writeUInt64(1, message.totalResults, false);
            }
            
            if (message.fields != null) {
                for (DefaultMetadataField field : message.fields) {
                    if (field != null)
                        output.writeObject(2, field, DefaultMetadataField.getSchema(), true);
                }
            }
        }
        
        public void mergeFrom(Input input, DefaultDataDictionary message) throws IOException {
            int number;
            while ((number = input.readFieldNumber(this)) != 0) {
                switch (number) {
                    case 1:
                        message.setTotalResults(input.readUInt64());
                        break;
                    case 2:
                        if (message.fields == null) {
                            message.fields = new ArrayList<>();
                        }
                        
                        message.fields.add(input.mergeObject(null, DefaultMetadataField.getSchema()));
                        break;
                    default:
                        input.handleUnknownField(number, this);
                        break;
                }
            }
        }
        
        public String getFieldName(int number) {
            switch (number) {
                case 1:
                    return "totalResults";
                case 2:
                    return "fields";
                default:
                    return null;
            }
        }
        
        public int getFieldNumber(String name) {
            final Integer number = fieldMap.get(name);
            return number == null ? 0 : number;
        }
        
        final java.util.HashMap<String,Integer> fieldMap = new java.util.HashMap<>();
        {
            fieldMap.put("totalResults", 1);
            fieldMap.put("fields", 2);
        }
    };
    
    @Override
    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }
    
    @Override
    public long getTotalResults() {
        return this.totalResults;
    }
    
    @Override
    public void transformFields(Consumer<DefaultMetadataField> transformer) {
        fields.forEach(transformer);
    }
    
    @Override
    public String toString() {
        return "DefaultDataDictionary{" + "fields=" + fields + ", totalResults=" + totalResults + "} " + super.toString();
    }
}
