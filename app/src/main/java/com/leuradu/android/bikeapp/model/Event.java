package com.leuradu.android.bikeapp.model;

import android.util.Log;

import com.backendless.geo.GeoPoint;
import com.skobbler.ngx.SKCoordinate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by radu on 09.04.2016.
 */
public class Event {

    private int annotationId;
    private String backendId;
    private SKCoordinate location;
    private String userId;
    private String name;
    private String description;
    private Date date;

    public Event(GeoPoint point) {
        backendId = point.getObjectId();
        location = new SKCoordinate(point.getLongitude(), point.getLatitude());
        name = point.getMetadata("Name");
        description = point.getMetadata("Description");
        userId = point.getMetadata("User");
        String d = point.getMetadata("Datetime");
        date = parseDateString(d);
    }

    public Event(String id, SKCoordinate location, String user, String name, String description,
                 String date) {
        this.backendId = id;
        this.location = location;
        this.userId = user;
        this.name = name;
        this.description = description;
        this.date = parseDateString(date);
    }

    private Date parseDateString(String date) {
        Date result = null;
        try {
            DateFormat df = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy", Locale.ENGLISH);
            result = df.parse(date);
            Log.d("EVENT", result.toString());
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return result;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
