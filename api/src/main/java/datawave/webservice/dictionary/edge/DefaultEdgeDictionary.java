package datawave.webservice.dictionary.edge;

import datawave.webservice.query.result.util.protostuff.FieldAccessor;
import datawave.webservice.query.result.util.protostuff.ProtostuffField;
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
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = "EdgeDictionary")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class DefaultEdgeDictionary extends EdgeDictionaryBase<DefaultEdgeDictionary,DefaultMetadata>
                implements TotalResultsAware, Message<DefaultEdgeDictionary> {
    
    private static final long serialVersionUID = 1L;
    
    @XmlElementWrapper(name = "EdgeMetadata")
    @XmlElement(name = "Metadata")
    private List<DefaultMetadata> metadataList = null;
    
    @XmlElement(name = "TotalResults")
    private Long totalResults = null;
    
    public DefaultEdgeDictionary() {}
    
    public DefaultEdgeDictionary(Collection<DefaultMetadata> fields) {
        if (fields == null) {
            this.metadataList = null;
            setTotalResults(0);
        } else {
            this.metadataList = new LinkedList<>(fields);
            setTotalResults(this.metadataList.size());
            this.setHasResults(true);
        }
    }
    
    @Override
    public List<? extends MetadataBase<DefaultMetadata>> getMetadataList() {
        return metadataList == null ? null : Collections.unmodifiableList(metadataList);
    }
    
    public static Schema<DefaultEdgeDictionary> getSchema() {
        return SCHEMA;
    }
    
    @Override
    public Schema<DefaultEdgeDictionary> cachedSchema() {
        return SCHEMA;
    }
    
    private enum DICT_BASE implements FieldAccessor {
        METADATA(1, "metadataField"), TOTAL(2, "totalResults"), UNKNOWN(0, "UNKNOWN");
        
        final int fn;
        final String name;
        
        DICT_BASE(int fn, String name) {
            this.fn = fn;
            this.name = name;
        }
        
        public int getFieldNumber() {
            return fn;
        }
        
        public String getFieldName() {
            return name;
        }
    }
    
    private static final ProtostuffField<DICT_BASE> PFIELD = new ProtostuffField<>(DICT_BASE.class);
    
    @XmlTransient
    private static final Schema<DefaultEdgeDictionary> SCHEMA = new Schema<DefaultEdgeDictionary>() {
        public DefaultEdgeDictionary newMessage() {
            return new DefaultEdgeDictionary();
        }
        
        public Class<DefaultEdgeDictionary> typeClass() {
            return DefaultEdgeDictionary.class;
        }
        
        public String messageName() {
            return DefaultEdgeDictionary.class.getSimpleName();
        }
        
        public String messageFullName() {
            return DefaultEdgeDictionary.class.getName();
        }
        
        public boolean isInitialized(DefaultEdgeDictionary message) {
            return true;
        }
        
        public void writeTo(Output output, DefaultEdgeDictionary message) throws IOException {
            if (message.metadataList != null) {
                for (DefaultMetadata metadata : message.metadataList) {
                    output.writeObject(DICT_BASE.METADATA.getFieldNumber(), metadata, DefaultMetadata.getSchema(), true);
                }
            }
            if (message.totalResults != null) {
                output.writeUInt64(DICT_BASE.TOTAL.getFieldNumber(), message.totalResults, false);
            }
        }
        
        public void mergeFrom(Input input, DefaultEdgeDictionary message) throws IOException {
            int number;
            while ((number = input.readFieldNumber(this)) != 0) {
                switch (number) {
                    case 1:
                        message.setTotalResults(input.readUInt64());
                        break;
                    case 2:
                        if (message.metadataList == null) {
                            message.metadataList = new ArrayList<>();
                        }
                        message.metadataList.add(input.mergeObject(null, DefaultMetadata.getSchema()));
                        break;
                    default:
                        input.handleUnknownField(number, this);
                        break;
                }
            }
        }
        
        @Override
        public String getFieldName(int number) {
            DICT_BASE field = PFIELD.parseFieldNumber(number);
            if (field == DICT_BASE.UNKNOWN) {
                return null;
            }
            return field.getFieldName();
        }
        
        @Override
        public int getFieldNumber(String name) {
            DICT_BASE field = PFIELD.parseFieldName(name);
            return field.getFieldNumber();
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
}
