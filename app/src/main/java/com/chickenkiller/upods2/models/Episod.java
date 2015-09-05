package com.chickenkiller.upods2.models;

import android.text.format.Formatter;

import com.chickenkiller.upods2.controllers.UpodsApplication;
import com.chickenkiller.upods2.interfaces.IPlayableTrack;

/**
 * Created by alonzilberman on 8/31/15.
 */
public class Episod extends Track implements IPlayableTrack{
    private String summary;
    private String length;
    private String duration;
    private String btnDownloadText;
    private String date;

    public Episod(){
        super();
        this.summary="";
        this.length="";
        this.duration="";
        this.btnDownloadText="";
        this.date="";
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSubTitle() {
        return date;
    }

    @Override
    public String getAudeoUrl() {
        return mp3Url;
    }

    @Override
    public String getData() {
        String size = Formatter.formatShortFileSize(UpodsApplication.getContext(), Long.valueOf(length));
        return duration + " / " + size;
    }

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
