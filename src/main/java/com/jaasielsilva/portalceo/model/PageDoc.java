package com.jaasielsilva.portalceo.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PageDoc {
    private String url;
    private String httpMethod;
    private String template;
    private String title;
    private String purpose;
    private List<String> components = new ArrayList<>();
    private List<String> elements = new ArrayList<>();
    private List<String> navigationLinks = new ArrayList<>();
    private String permissions;
    private Instant lastUpdated;
    private String screenshotPath;
    private List<FieldDoc> fields = new ArrayList<>();

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public List<String> getComponents() { return components; }
    public void setComponents(List<String> components) { this.components = components; }

    public List<String> getElements() { return elements; }
    public void setElements(List<String> elements) { this.elements = elements; }

    public List<String> getNavigationLinks() { return navigationLinks; }
    public void setNavigationLinks(List<String> navigationLinks) { this.navigationLinks = navigationLinks; }

    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    public List<FieldDoc> getFields() { return fields; }
    public void setFields(List<FieldDoc> fields) { this.fields = fields; }
}
