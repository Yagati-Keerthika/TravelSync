package com.travelsync.dto;

import java.util.List;

public class SearchRequest {
    private String text;
    private String pattern;
    private List<String> keywords;

    public SearchRequest() {}

    public SearchRequest(String text, String pattern) {
        this.text = text;
        this.pattern = pattern;
    }

    public SearchRequest(String text, List<String> keywords) {
        this.text = text;
        this.keywords = keywords;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
}
