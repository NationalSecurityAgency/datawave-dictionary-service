package datawave.webservice.dictionary.edge;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import datawave.webservice.HtmlProvider;
import datawave.webservice.query.result.util.protostuff.FieldAccessor;
import datawave.webservice.query.result.util.protostuff.ProtostuffField;
import datawave.webservice.result.TotalResultsAware;
import io.protostuff.Input;
import io.protostuff.Message;
import io.protostuff.Output;
import io.protostuff.Schema;

@XmlRootElement(name = "EdgeDictionary")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class DefaultEdgeDictionary extends EdgeDictionaryBase<DefaultEdgeDictionary,DefaultMetadata>
                implements TotalResultsAware, Message<DefaultEdgeDictionary>, HtmlProvider {
    
    private static final long serialVersionUID = 1L;
    private final String jqueryUri;
    private final String dataTablesUri;
    private static final String TITLE = "Edge Dictionary", SEP = ", ";
    private static final String DATA_TABLES_TEMPLATE = "<script type=''text/javascript'' src=''{0}jquery.min.js''></script>\n"
                    + "<script type=''text/javascript'' src=''{1}jquery.dataTables.min.js''></script>\n" + "<script type=''text/javascript''>\n"
                    + "$(document).ready(function() '{' $(''#myTable'').dataTable('{'\"bPaginate\": false, \"aaSorting\": [[3, \"asc\"]], \"bStateSave\": true'}') '}')\n"
                    + "</script>\n";
    
    @XmlElementWrapper(name = "EdgeMetadata")
    @XmlElement(name = "Metadata")
    private List<DefaultMetadata> metadataList = null;
    
    @XmlElement(name = "TotalResults")
    private Long totalResults = null;
    
    public DefaultEdgeDictionary() {
        this.jqueryUri = "/dictionary/webjars/jquery/";
        this.dataTablesUri = "/dictionary/webjars/datatables/js/";
    }
    
    public DefaultEdgeDictionary(Collection<DefaultMetadata> fields, String jqueryUri, String dataTablesUri) {
        if (fields == null) {
            this.metadataList = null;
            setTotalResults(0);
        } else {
            this.metadataList = new LinkedList<>(fields);
            setTotalResults(this.metadataList.size());
            this.setHasResults(true);
        }
        this.jqueryUri = jqueryUri;
        this.dataTablesUri = dataTablesUri;
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
    
    @Override
    public String getTitle() {
        return TITLE;
    }
    
    @Override
    public String getHeadContent() {
        return MessageFormat.format(DATA_TABLES_TEMPLATE, jqueryUri, dataTablesUri);
    }
    
    public String getPageHeader() {
        return getTitle();
    }
    
    @Override
    public String getMainContent() {
        StringBuilder builder = new StringBuilder(2048);
        
        builder.append("<div>\n");
        builder.append("<div id=\"myTable_wrapper\" class=\"dataTables_wrapper no-footer\">\n");
        builder.append("<table id=\"myTable\" class=\"dataTable no-footer\" role=\"grid\" aria-describedby=\"myTable_info\">\n")
                        .append("<thead><tr><th title=\"Defined either by Datawave Configuration files or Edge enrichment field.\">Edge Type  &#9432;</th>")
                        .append("<th title=\"Defined by Datawave Configuration files.\">Edge Relationship  &#9432;</th>")
                        .append("<th title=\"Defined by Datawave Configuration files and optional attributes Attribute2 and Attribute3.\">Edge Attribute1 Source  &#9432;</th>")
                        .append("<th title=\"List of Field Name pairs used to generate this edge type. Format: [Source Field, Target Field | Enrichment Field=Enrichment Field Value]\">Fields  &#9432;</th>")
                        .append("<th title=\"Start date of edge type creation. Format: yyyyMMdd\">Date  &#9432;</th>").append("</tr></thead>");
        
        builder.append("<tbody>");
        int x = 0;
        for (MetadataBase<DefaultMetadata> metadata : this.getMetadataList()) {
            String type = metadata.getEdgeType();
            String relationship = metadata.getEdgeRelationship();
            String collect = metadata.getEdgeAttribute1Source();
            StringBuilder fieldBuilder = new StringBuilder();
            for (EventField field : metadata.getEventFields()) {
                fieldBuilder.append(field).append(SEP);
            }
            
            String fieldNames = fieldBuilder.toString().substring(0, fieldBuilder.length() - 2);
            String date = metadata.getStartDate();
            
            builder.append("<td>").append(type).append("</td>");
            builder.append("<td>").append(relationship).append("</td>");
            builder.append("<td>").append(collect).append("</td>");
            builder.append("<td>").append(fieldNames).append("</td>");
            builder.append("<td>").append(date).append("</td>");
            
            builder.append("</td>").append("</tr>");
        }
        builder.append("</tbody>");
        
        builder.append("</table>\n");
        builder.append("  <div class=\"dataTables_info\" id=\"myTable_info\" role=\"status\" aria-live=\"polite\"></div>\n");
        builder.append("</div>\n");
        builder.append("</div>");
        
        return builder.toString();
    }
}
