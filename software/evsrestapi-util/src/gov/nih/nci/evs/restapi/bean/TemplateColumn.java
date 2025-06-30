package gov.nih.nci.evs.restapi.bean;

public class TemplateColumn {
	private Integer columnNumber;
	private String label;
	private String display;
	private String propertyType;
	private String property;
	private String source;
	private String group;
	private String subsource;
    private String attr;

	public Integer getColumnNumber() {
		return columnNumber;
	}
	public void setColumnNumber(Integer columnNumber) {
		this.columnNumber = columnNumber;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
	public String getPropertyType() {
		return propertyType;
	}
	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getSubsource() {
		return subsource;
	}
	public void setSubsource(String subsource) {
		this.subsource = subsource;
	}
	public String getAttr() {
		return attr;
	}
	public void setAttr(String attr) {
		this.attr = attr;
	}

	public String to_string() {
		StringBuffer buf = new StringBuffer();
		buf.append("- columnNumber: " + this.columnNumber).append("\n");
		buf.append("  label: " + this.label).append("\n");
		buf.append("  display: " + this.display).append("\n");
		buf.append("  propertyType: " + this.propertyType).append("\n");
		buf.append("  property: " + this.property).append("\n");
		buf.append("  source: " + this.source).append("\n");
		buf.append("  group: " + this.group).append("\n");
		buf.append("  subsource: " + this.subsource).append("\n");
		buf.append("  attr: " + this.attr);
		return buf.toString();
	}
}
