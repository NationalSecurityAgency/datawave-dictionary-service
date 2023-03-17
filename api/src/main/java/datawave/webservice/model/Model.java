package datawave.webservice.model;

import datawave.query.model.FieldMapping;
import datawave.webservice.HtmlProvider;
import datawave.webservice.result.BaseResponse;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.TreeSet;

@Data
@XmlRootElement(name = "Model")
@XmlAccessorType(XmlAccessType.NONE)
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
    
    // Only used in ModelBeanTest now
    public Model() {};
    
    @XmlAttribute(name = "name", required = true)
    private String name;
    
    @XmlElementWrapper(name = "Mappings")
    @XmlElement(name = "Mapping")
    private TreeSet<FieldMapping> fields = new TreeSet<FieldMapping>();
    
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
        
        for (FieldMapping f : this.getFields()) {
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
