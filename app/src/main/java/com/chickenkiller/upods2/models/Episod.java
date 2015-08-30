package com.chickenkiller.upods2.models;

/**
 * Created by alonzilberman on 8/31/15.
 */
public class Episod extends Track{
    private String summary;
    private String length;
    private String duration;
    private String btnDownloadText;
    private String date;

    public String getBtnDownloadText() {
        return btnDownloadText;
    }

    public void setBtnDownloadText(String btnDownloadText) {
        this.btnDownloadText = btnDownloadText;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getDate() {
        return this.date;
    }
}
