package datawave.webservice.dictionary.data;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import datawave.webservice.metadata.DefaultMetadataField;
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
    private static final String TITLE = "Data Dictionary", EMPTY_STR = "", SEP = ", ", NEWLINE = "\n";
    
    /*
     * Loads jQuery, DataTables, some CSS elements for DataTables, and executes `.dataTables()` on the HTML table in the payload.
     * 
     * Pagination on the table is turned off, we do an ascending sort on the 1st column (field name) and a cookie is saved in the browser that will leave the
     * last sort in place upon revisit of the page.
     */
    private static final String DATA_TABLES_TEMPLATE = "<script type=''text/javascript'' src=''{0}jquery.min.js''></script>\n"
                    + "<script type=''text/javascript'' src=''{1}jquery.dataTables.min.js''></script>\n" + "<script type=''text/javascript''>\n"
                    + "$(document).ready(function() '{' $(''#myTable'').dataTable('{'\"bPaginate\": false, \"aaSorting\": [[0, \"asc\"]], \"bStateSave\": true'}'); $(''#myTable'').find(\"td\").css(\"word-break\", \"break-word\"); '}');\n"
                    + "</script>\n";
    // JS script to make the table column widths resizable
    private static final String RESIZEABLE_TABLE = resizeableTable();
    // JS script to display how the table is being sorted (i.e., by which column and in ascending or descending order)
    private static final String SORTED_BY = sortedBy();
    
    private final String dataTablesHeader;
    
    @XmlElementWrapper(name = "MetadataFields")
    @XmlElement(name = "MetadataField")
    private List<DefaultMetadataField> fields = null;
    
    @XmlElement(name = "TotalResults")
    private Long totalResults = null;
    
    public DefaultDataDictionary() {
        this("/webjars/jquery/", "/webjars/datatables/");
    }
    
    public DefaultDataDictionary(String jqueryUri, String datatablesUri) {
        this.dataTablesHeader = MessageFormat.format(DATA_TABLES_TEMPLATE, jqueryUri, datatablesUri) + RESIZEABLE_TABLE + SORTED_BY;
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
        builder.append("created by the software to make querying easier.</p>");
        builder.append("</div>");
        builder.append("<table id=\"myTable\">\n");
        
        builder.append("<thead><tr><th>FieldName</th><th>Internal FieldName</th><th>DataType</th>");
        builder.append("<th>Index only</th><th>Forward Indexed</th><th>Reverse Indexed</th><th>Normalized</th><th>Types</th><th>Tokenized</th><th>Description</th><th>LastUpdated</th></tr></thead>");
        
        builder.append("<tbody>");
        for (DefaultMetadataField f : this.getFields()) {
            builder.append("<tr>");
            
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
            
            builder.append("<td>").append(fieldName).append("</td>");
            builder.append("<td>").append(internalFieldName).append("</td>");
            builder.append("<td>").append(datatype).append("</td>");
            builder.append("<td>").append(f.isIndexOnly()).append("</td>");
            builder.append("<td>").append(f.isForwardIndexed() ? true : "").append("</td>");
            builder.append("<td>").append(f.isReverseIndexed() ? true : "").append("</td>");
            builder.append("<td>").append(f.getTypes() != null && f.getTypes().size() > 0 ? "true" : "false").append("</td>");
            builder.append("<td>").append(types).append("</td>");
            builder.append("<td>").append(f.isTokenized() ? true : "").append("</td>");
            builder.append("<td>");
            
            boolean first = true;
            for (DescriptionBase desc : f.getDescriptions()) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(desc.getMarkings()).append(" ").append(desc.getDescription());
                first = false;
            }
            
            builder.append("</td>");
            builder.append("<td>").append(f.getLastUpdated()).append("</td>").append("</tr>");
        }
        builder.append("</tbody>");
        
        builder.append("</table>\n");
        
        return builder.toString();
    }
    
    @Override
    public void transformFields(Consumer<DefaultMetadataField> transformer) {
        fields.forEach(transformer);
    }
    
    private static String resizeableTable() {
        StringBuilder script = new StringBuilder();
        
        script.append("<script>");
        
        script.append("$( document ).ready(function() {" + NEWLINE);
        // Function to resize the divs that are used to resize the columns (make divs same height as table).
        // This needs to be done when a search filter is applied or the column widths are changed (both may change table height)
        script.append("function resizeColumnResizers() {" + NEWLINE);
        script.append("let divsToResize = document.getElementsByClassName('column-resizer-div');" + NEWLINE);
        script.append("for (let i = 0; i < divsToResize.length; i++) {" + NEWLINE);
        script.append("divsToResize[i].style.height = document.getElementById('myTable').offsetHeight - 5 + 'px';" + NEWLINE);
        script.append("}" + NEWLINE);
        script.append("}" + NEWLINE);
        // Function to make the table columns resizeable
        script.append("function resizeableTable() {" + NEWLINE);
        script.append("var table = document.getElementById('myTable');" + NEWLINE); // get the table
        script.append("var row = table.getElementsByTagName('tr')[0];" + NEWLINE); // get the first row (the table headers)
        script.append("var cols = row.children;" + NEWLINE);
        script.append("var tableHeight = table.offsetHeight;" + NEWLINE);
        script.append("for (let i = 0; i < cols.length; i++) {" + NEWLINE);
        script.append("var div = createDiv(tableHeight);" + NEWLINE); // create a div to the right of each column
        script.append("cols[i].appendChild(div);" + NEWLINE);
        script.append("cols[i].style.position = 'relative';" + NEWLINE);
        script.append("setListeners(div);" + NEWLINE); // add the event listeners to each div
        script.append("}" + NEWLINE);
        script.append("}" + NEWLINE);
        // Function to add the event listeners for mousedown, mouseup, moveover, mouseout, and mousemove events
        script.append("function setListeners(div) {" + NEWLINE);
        script.append("var pageX, curCol, nxtCol, curColWidth, nxtColWidth;" + NEWLINE);
        script.append("div.addEventListener('mousedown', function (e) {" + NEWLINE); // mousedown event
        script.append("curCol = e.target.parentElement;" + NEWLINE); // current column
        script.append("nxtCol = curCol.nextElementSibling;" + NEWLINE); // sibling column
        script.append("pageX = e.pageX;" + NEWLINE); // x coord of mouse pointer
        script.append("var padding = paddingDiff(curCol);" + NEWLINE);
        script.append("curColWidth = curCol.offsetWidth - padding;" + NEWLINE);
        script.append("if (nxtCol)" + NEWLINE);
        script.append("nxtColWidth = nxtCol.offsetWidth - padding;" + NEWLINE);
        script.append("});" + NEWLINE);
        script.append("div.addEventListener('mouseover', function (e) {" + NEWLINE); // mouseover event: creates a line to indicate that it can be resized
        script.append("e.target.style.borderRight = '1.5px dashed #000000';" + NEWLINE);
        script.append("})" + NEWLINE);
        script.append("div.addEventListener('mouseout', function (e) {" + NEWLINE); // mouseout event: removes line
        script.append("e.target.style.borderRight = '';" + NEWLINE);
        script.append("})" + NEWLINE);
        script.append("document.addEventListener('mousemove', function (e) {" + NEWLINE); // mousemove event
        script.append("resizeColumnResizers();" + NEWLINE); // Resize all of the column resizer divs to be the same size as the table
        script.append("if (curCol) {" + NEWLINE);
        script.append("var diffX = e.pageX - pageX;" + NEWLINE);
        script.append("if (nxtCol)" + NEWLINE);
        script.append("nxtCol.style.width = (nxtColWidth - (diffX)) + 'px';" + NEWLINE); // set the new sibling column width
        script.append("curCol.style.width = (curColWidth + diffX) + 'px';" + NEWLINE); // set the new current column width
        script.append("}" + NEWLINE);
        script.append("});" + NEWLINE);
        script.append("document.addEventListener('mouseup', function (e) { " + NEWLINE); // mouseup event: clear all values
        script.append("curCol = undefined;" + NEWLINE);
        script.append("nxtCol = undefined;" + NEWLINE);
        script.append("pageX = undefined;" + NEWLINE);
        script.append("nxtColWidth = undefined;" + NEWLINE);
        script.append("curColWidth = undefined;" + NEWLINE);
        script.append("});" + NEWLINE);
        script.append("}" + NEWLINE);
        script.append("function createDiv(height) {" + NEWLINE); // Creates a div which can be interacted with to change the column size
        script.append("var div = document.createElement('div');" + NEWLINE);
        script.append("div.setAttribute('class', 'column-resizer-div');" + NEWLINE);
        script.append("div.style.top = 0;" + NEWLINE);
        script.append("div.style.right = 0;" + NEWLINE);
        script.append("div.style.width = '5px';" + NEWLINE);
        script.append("div.style.position = 'absolute';" + NEWLINE);
        script.append("div.style.cursor = 'col-resize';" + NEWLINE);
        script.append("div.style.userSelect = 'none';" + NEWLINE);
        script.append("div.style.height = height - 5 + 'px';" + NEWLINE);
        script.append("return div;" + NEWLINE);
        script.append("}" + NEWLINE);
        script.append("function paddingDiff(col) {" + NEWLINE);
        script.append("if (getStyleVal(col, 'box-sizing') == 'border-box') {" + NEWLINE);
        script.append("return 0;" + NEWLINE);
        script.append("}" + NEWLINE);
        script.append("var padLeft = getStyleVal(col, 'padding-left');" + NEWLINE);
        script.append("var padRight = getStyleVal(col, 'padding-right');" + NEWLINE);
        script.append("return (parseInt(padLeft) + parseInt(padRight));" + NEWLINE);
        script.append("}" + NEWLINE);
        script.append("function getStyleVal(elm, css) {" + NEWLINE);
        script.append("return (window.getComputedStyle(elm, null).getPropertyValue(css));" + NEWLINE);
        script.append("}" + NEWLINE);
        script.append("resizeableTable();" + NEWLINE);
        script.append("});" + NEWLINE);
        
        script.append("</script>");
        
        return script.toString();
    }
    
    private static String sortedBy() {
        StringBuilder script = new StringBuilder();
        
        script.append("<script>");
        
        script.append("$( document ).ready(function() {" + NEWLINE);
        script.append("function createSortedByDiv() {" + NEWLINE); // create a new div under the filter search div. Contains the info for how the table is
                                                                   // sorted
        script.append("var sortedByDiv = document.createElement('div');" + NEWLINE);
        script.append("const textContent = document.createTextNode('Table sorted by ');" + NEWLINE);
        script.append("const sortingInfo = document.createElement('span');" + NEWLINE);
        script.append("sortingInfo.setAttribute('id', 'sortedBy');" + NEWLINE);
        script.append("sortedByDiv.appendChild(textContent);" + NEWLINE);
        script.append("sortedByDiv.appendChild(sortingInfo);" + NEWLINE);
        script.append("sortedByDiv.setAttribute('style', 'padding: 10px;');" + NEWLINE);
        script.append("const tableFilterDiv = document.getElementById('myTable_filter');" + NEWLINE);
        script.append("tableFilterDiv.parentNode.insertBefore(sortedByDiv, tableFilterDiv.nextSibling);" + NEWLINE);
        script.append("}" + NEWLINE);
        script.append("window.updateSortingInfo = function updateSortingInfo() {" + NEWLINE); // Update the text to display which column is currently being used
                                                                                              // to sort the table
        script.append("var table = document.getElementById('myTable');" + NEWLINE);
        script.append("var tableHeaders = table.getElementsByTagName('tr')[0];" + NEWLINE);
        script.append("var cols = tableHeaders.children;" + NEWLINE);
        script.append("for (let i = 0; i < cols.length; i++) {" + NEWLINE);
        script.append("let className = cols[i].getAttribute('class');" + NEWLINE);
        script.append("let header = cols[i].innerHTML;" + NEWLINE);
        script.append("if (className === 'sorting sorting_asc') {" + NEWLINE);
        script.append("document.getElementById('sortedBy').innerHTML = `${header} in ascending order:`;" + NEWLINE);
        script.append("break;" + NEWLINE);
        script.append("}" + NEWLINE);
        script.append("else if (className === 'sorting sorting_desc') {" + NEWLINE);
        script.append("document.getElementById('sortedBy').innerHTML = `${header} in descending order:`;" + NEWLINE);
        script.append("break;" + NEWLINE);
        script.append("}" + NEWLINE);
        script.append("}" + NEWLINE);
        script.append("}" + NEWLINE);
        script.append("function addOnClickEvents() {" + NEWLINE); // Add onclick events for each of the table headers to call updateSortingInfo()
        script.append("var table = document.getElementById('myTable');" + NEWLINE);
        script.append("var tableHeaders = table.getElementsByTagName('tr')[0];" + NEWLINE);
        script.append("var cols = tableHeaders.children;" + NEWLINE);
        script.append("for (let i = 0; i < cols.length; i++){" + NEWLINE);
        script.append("cols[i].setAttribute('onclick', 'updateSortingInfo();');" + NEWLINE);
        script.append("}" + NEWLINE);
        script.append("}" + NEWLINE);
        script.append("createSortedByDiv();" + NEWLINE);
        script.append("addOnClickEvents();" + NEWLINE);
        script.append("updateSortingInfo();" + NEWLINE);
        script.append("});" + NEWLINE);
        
        script.append("</script>");
        
        return script.toString();
    }
    
    @Override
    public String toString() {
        return "DefaultDataDictionary{" + "fields=" + fields + ", totalResults=" + totalResults + "} " + super.toString();
    }
}
