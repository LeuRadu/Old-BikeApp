package com.leuradu.android.bikeapp.model;

import com.backendless.geo.GeoPoint;
import com.skobbler.ngx.SKCoordinate;

/**
 * Created by radu on 09.04.2016.
 */
public class Favorite {

    private int annotationId;
    private String backendId;
    private SKCoordinate location;
    private String name;
    private String description;

    public Favorite(GeoPoint point) {
        backendId = point.getObjectId();
        location = new SKCoordinate(point.getLongitude(), point.getLatitude());
        name = point.getMetadata("Name");
        description = point.getMetadata("Description");
    }

    public Favorite(String id, SKCoordinate location, String name, String description) {
        this.backendId = id;
        this.location = location;
        this.name = name;
        this.description = description;
    }

    public int getAnnotationId() {
        return annotationId;
    }

    public void setAnnotationId(int annotationId) {
        this.annotationId = annotationId;
    }

    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }

    public SKCoordinate getLocation() {
        return location;
    }

    public void setLocation(SKCoordinate location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
