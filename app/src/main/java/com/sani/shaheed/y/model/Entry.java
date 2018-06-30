package com.sani.shaheed.y.model;

/**
 * Created by Umar Saidu Auna on 06/26/18.
 */

public class Entry {

    private String the_title, contents, picture, date, uid;



    public Entry(String title, String contents, String picture, String date, String uid) {
        this.the_title = title;
        this.contents = contents;
        this.picture = picture;
        this.date = date;
        this.uid = uid;
    }

    public void setDate(String date) { this.date = date; }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return the_title;
    }

    public void setTitle(String the_title) {
        this.the_title = the_title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
