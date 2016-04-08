package com.leuradu.android.bikeapp.model;

import com.leuradu.android.bikeapp.activities.MapActivity;

/**
 * Created by radu on 08.04.2016.
 */
public class Annotation {

    private String title;
    private String description;
    private MapActivity.AnnotationType type;

    public Annotation(String title, String description, MapActivity.AnnotationType type) {
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

    public MapActivity.AnnotationType getType() {
        return type;
    }

    public void setType(MapActivity.AnnotationType type) {
        this.type = type;
    }
}
