package com.example.lottery.admin;

public class ModerationItem {
    private String title;
    private String detail;

    public ModerationItem(String title, String detail) {
        this.title = title;
        this.detail = detail;
    }

    public String getTitle() { return title; }
    public String getDetail() { return detail; }
}
