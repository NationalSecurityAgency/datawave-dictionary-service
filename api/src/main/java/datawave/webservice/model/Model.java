package datawave.webservice.model;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import datawave.webservice.HtmlProvider;
import datawave.webservice.result.BaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@XmlRootElement
// @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class Model extends BaseResponse implements Serializable, HtmlProvider {
    
    private static final long serialVersionUID = 1L;
    private String jqueryUri;
    private String dataTablesUri;
    private static final String TITLE = "Model Description", EMPTY = "";
    private static final String DATA_TABLES_TEMPLATE = "<script type=''text/javascript'' src=''{0}''></script>\n"
                    + "<script type=''text/javascript'' src=''{1}''></script>\n" + "<script type=''text/javascript''>\n"
                    + "$(document).ready(function() '{' $(''#myTable'').dataTable('{'\"bPaginate\": false, \"aaSorting\": [[3, \"asc\"]], \"bStateSave\": true'}') '}')\n"
                    + "</script>\n";
    
    public Model(String jqueryUri, String datatablesUri) {
        this.jqueryUri = jqueryUri;
        this.dataTablesUri = datatablesUri;
    }
    
    @XmlAttribute(required = true)
    private String name;
    
    @XmlElementWrapper(name = "Mappings")
    @XmlElement(name = "Mappings")
    private TreeSet<Mapping> mappings = new TreeSet<Mapping>();
    
    /*
     * (non-Javadoc)
     * 
     * @see datawave.webservice.HtmlProvider#getTitle()
     */
    @Override
    public String getTitle() {
        return TITLE;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see datawave.webservice.HtmlProvider#getPageHeader()
     */
    @Override
    public String getPageHeader() {
        return TITLE;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see datawave.webservice.HtmlProvider#getHeadContent()
     */
    @Override
    public String getHeadContent() {
        return MessageFormat.format(DATA_TABLES_TEMPLATE, jqueryUri, dataTablesUri);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see datawave.webservice.HtmlProvider#getMainContent()
     */
    @Override
    public String getMainContent() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("<div>\n");
        builder.append("<div id=\"myTable_wrapper\" class=\"dataTables_wrapper no-footer\">\n");
        builder.append("<table id=\"myTable\" class=\"dataTable no-footer\" role=\"grid\" aria-describedby=\"myTable_info\">\n");
        
        builder.append("<thead><tr><th>Visibility</th><th>FieldName</th><th>DataType</th><th>ModelFieldName</th><th>Direction</th></tr></thead>");
        builder.append("<tbody>");
        
        for (Mapping f : this.getMappings()) {
            builder.append("<td>").append(f.getColumnVisibility()).append("</td>");
            builder.append("<td>").append(f.getFieldName()).append("</td>");
            builder.append("<td>").append(f.getDatatype()).append("</td>");
            builder.append("<td>").append(f.getModelFieldName()).append("</td>");
            builder.append("<td>").append(f.getDirection()).append("</td>");
            builder.append("</tr>");
        }
        
        builder.append("</tbody>");
        builder.append("  </table>\n");
        builder.append("  <div class=\"dataTables_info\" id=\"myTable_info\" role=\"status\" aria-live=\"polite\"></div>\n");
        builder.append("</div>\n");
        builder.append("</div>");
        
        return builder.toString();
    }
    
}
