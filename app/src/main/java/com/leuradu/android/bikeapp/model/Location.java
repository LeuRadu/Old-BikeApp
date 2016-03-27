package com.leuradu.android.bikeapp.model;

import com.skobbler.ngx.map.SKAnnotation;

/**
 * Created by radu on 28.03.2016.
 */
public class Location {

    private String title;
    private String description;
    private String type;

    public Location(String title, String description, String type) {
        this.title = title;
        this.description = description;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
