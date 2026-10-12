package com.travelsync.dto;

public class CompareRequest {
    private String destination1;
    private String destination2;
    private String text1;
    private String text2;

    public CompareRequest() {}

    public CompareRequest(String destination1, String destination2) {
        this.destination1 = destination1;
        this.destination2 = destination2;
    }

    public String getDestination1() { return destination1; }
    public void setDestination1(String destination1) { this.destination1 = destination1; }

    public String getDestination2() { return destination2; }
    public void setDestination2(String destination2) { this.destination2 = destination2; }

    public String getText1() { return text1; }
    public void setText1(String text1) { this.text1 = text1; }

    public String getText2() { return text2; }
    public void setText2(String text2) { this.text2 = text2; }
}
