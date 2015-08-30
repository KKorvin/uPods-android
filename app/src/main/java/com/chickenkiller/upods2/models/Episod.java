package com.chickenkiller.upods2.models;

/**
 * Created by alonzilberman on 8/31/15.
 */
public class Episod extends Track{
    private String summary;
    private String lengthDate;
    private String duration;
    private String btnDownloadText;

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

    public String getLengthDate() {
        return lengthDate;
    }

    public void setLengthDate(String lengthDate) {
        this.lengthDate = lengthDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
