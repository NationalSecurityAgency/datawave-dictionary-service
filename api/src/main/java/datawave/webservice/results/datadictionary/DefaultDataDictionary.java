package datawave.webservice.results.datadictionary;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.collect.Lists;
import datawave.webservice.HtmlProvider;
import datawave.webservice.query.result.metadata.DefaultMetadataField;
import datawave.webservice.result.TotalResultsAware;

import io.protostuff.Input;
import io.protostuff.Message;
import io.protostuff.Output;
import io.protostuff.Schema;

@XmlRootElement(name = "DefaultDataDictionary")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class DefaultDataDictionary extends DataDictionaryBase<DefaultDataDictionary,DefaultMetadataField>
                implements TotalResultsAware, Message<DefaultDataDictionary>, HtmlProvider {
    
    private static final long serialVersionUID = 1L;
    private static final String TITLE = "Data Dictionary", EMPTY_STR = "", SEP = ", ";
    
    /*
     * Loads jQuery, DataTables, some CSS elements for DataTables, and executes `.dataTables()` on the HTML table in the payload.
     * 
     * Pagination on the table is turned off, we do an ascending sort on the 2nd column (field name) and a cookie is saved in the browser that will leave the
     * last sort in place upon revisit of the page.
     */
    private static final String AG_GRID_TEMPLATE = "<script type=''text/javascript'' src=''{0}jquery.min.js''></script>\n"
                    + "<script type=''text/javascript'' src=''{1}24.0.0/dist/ag-grid-community.min.js''></script>\n" + "<script type=''text/javascript''>\n"
                    + "var columnDefs = [\n" + "  " + " '{'headerName: \"FieldName\", field: \"fieldname\"'}',\n"
                    + " '{'headerName: \"Internal FieldName\", field: \"internalFieldName\"'}',\n" + " '{'headerName: \"Data Type\", field: \"datatype\"'}',\n"
                    + " '{'headerName: \"Index Only\", field: \"indexOnly\"'}',\n" + " '{'headerName: \"Forward Indexed\", field: \"forwardIndexed\"'}',\n"
                    + " '{'headerName: \"Reverse Indexed\", field: \"reverseIndexed\"'}',\n" + " '{'headerName: \"Normalized\", field: \"normalized\"'}',\n"
                    + " '{'headerName: \"Types\", field: \"types\"'}',\n" + " '{'headerName: \"Tokenized\", field: \"tokenized\"'}',\n"
                    + " '{'headerName: \"Description\", field: \"description\"'}',\n" + " '{'headerName: \"Last Updated\", field: \"lastUpdated\"'}'\n" + "];\n"
                    + "</script>\n";
    
    private final String dataTablesHeader;
    
    @XmlElementWrapper(name = "MetadataFields")
    @XmlElement(name = "MetadataField")
    private List<DefaultMetadataField> fields = null;
    
    @XmlElement(name = "TotalResults")
    private Long totalResults = null;
    
    public DefaultDataDictionary() {
        this("/webjars/jquery/", "/webjars/ag-grid-community/");
    }
    
    public DefaultDataDictionary(String jqueryUri, String datatablesUri) {
        this.dataTablesHeader = MessageFormat.format(AG_GRID_TEMPLATE, jqueryUri, datatablesUri);
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
    public String getTitle() {
        return TITLE;
    }
    
    @Override
    public String getHeadContent() {
        return dataTablesHeader;
    }
    
    @Override
    public String getPageHeader() {
        return getTitle();
    }
    
    @Override
    public String getMainContent() {
        StringBuilder builder = new StringBuilder(2048);
        builder.append("<div>");
        builder.append("<p style=\"width:60%; margin-left: auto; margin-right: auto;\">When a value is present in the forward index types, this means that a field is indexed and informs you how your ");
        builder.append("query terms will be treated (e.g. text, number, IPv4 address, etc). The same applies for the reverse index types with ");
        builder.append("the caveat that you can also query these fields using leading wildcards. Fields that are marked as 'Index only' will not ");
        builder.append("appear in a result set unless explicitly queried on. Index only fields are typically composite fields, derived from actual data, ");
        builder.append("created by the software to make querying easier.</p><input type=\"text\" id=\"filter-text-box\" placeholder=\"Filter...\" oninput=\"onFilterTextBoxChanged()\"/>");
        
        builder.append("</div><div id=\"myGrid\" style=\"margin-left: auto;\n" + "        margin-right: auto;" + "        min-width: 60%;"
                        + "        max-width: 95%;" + "        border: 1px #333333 solid;" + "        border-spacing-top: 0;"
                        + "        border-spacing-bottom: 0;" + "        border: 1px #333333 solid;"
                        + "        border: 1px #333333 solid;\" class=\"ag-theme-alpine\"></div>");
        
        builder.append("<script type=''text/javascript''>\n var rowData = [");
        boolean first = true;
        for (DefaultMetadataField f : this.getFields()) {
            if (!first) {
                builder.append(",");
            } else {
                first = false;
            }
            builder.append("{");
            
            String fieldName = (null == f.getFieldName()) ? EMPTY_STR : f.getFieldName();
            String internalFieldName = (null == f.getInternalFieldName()) ? EMPTY_STR : f.getInternalFieldName();
            String datatype = (null == f.getDataType()) ? EMPTY_STR : f.getDataType();
            
            StringBuilder types = new StringBuilder();
            if (null != f.getTypes()) {
                for (String forwardIndexType : f.getTypes()) {
                    if (0 != types.length()) {
                        types.append(SEP);
                    }
                    types.append(forwardIndexType);
                }
            }
            
            builder.append("\"fieldname\":\"").append(fieldName).append("\",");
            builder.append("\"internalFieldName\":\"").append(internalFieldName).append("\",");
            builder.append("\"datatype\":\"").append(datatype).append("\",");
            builder.append("\"indexOnly\":\"").append(f.isIndexOnly()).append("\",");
            builder.append("\"forwardIndexed\":\"").append(f.isForwardIndexed() ? true : "").append("\",");
            builder.append("\"reverseIndexed\":\"").append(f.isReverseIndexed() ? true : "").append("\",");
            builder.append("\"normalized\":\"").append(f.getTypes() != null && f.getTypes().size() > 0 ? "true" : "false").append("\",");
            builder.append("\"types\":\"").append(types).append("\",");
            builder.append("\"tokenized\":\"").append(f.isTokenized() ? true : "").append("\",");
            builder.append("\"description\":\"");
            
            boolean firstDesc = true;
            for (DescriptionBase desc : f.getDescriptions()) {
                if (!firstDesc) {
                    builder.append(", ");
                }
                builder.append(desc.getMarkings()).append(" ").append(desc.getDescription());
                first = false;
            }
            
            builder.append("\",");
            builder.append("\"lastUpdated\":\"").append(f.getLastUpdated()).append("\"").append("}");
        }
        builder.append("];\n");
        builder.append("    \n" + "// specify the data\n" + "// let the grid know which columns and what data to use\n" + "var gridOptions = {\n"
                        + "  columnDefs: columnDefs,\n" + "  rowData: rowData,\ndefaultColDef: { pagination: true, sortable: true, filter: true\n" + "  },"
                        + "};\n" + "\n" + "// setup the grid after the page has finished loading\n" + "\n"
                        + "    new agGrid.Grid($('#myGrid').get(0), gridOptions);\n" + "\n" + "function onFilterTextBoxChanged() {\n"
                        + "    gridOptions.api.setQuickFilter(document.getElementById('filter-text-box').value);\n" + "}");
        builder.append("</script>\n");
        
        return builder.toString();
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
