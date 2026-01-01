package com.jaasielsilva.portalceo.model;

public class FieldDoc {
    private String id;
    private String name;
    private String label;
    private String type;
    private String placeholder;
    private Integer maxLength;
    private boolean required;
    private String disabledExpr;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPlaceholder() { return placeholder; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
    public Integer getMaxLength() { return maxLength; }
    public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public String getDisabledExpr() { return disabledExpr; }
    public void setDisabledExpr(String disabledExpr) { this.disabledExpr = disabledExpr; }
}

