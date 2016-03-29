package com.leuradu.android.bikeapp.model;

import android.graphics.drawable.Drawable;

import com.leuradu.android.bikeapp.activities.MapActivity;

/**
 * Created by radu on 28.03.2016.
 */
public class CustomMenuItem {

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_HEADER = 1;

    private int type;
    private String label;
    private Drawable icon;

    public CustomMenuItem(int type, String label) {
        this(type, label, null);
    }

    public CustomMenuItem(int type, String label, Drawable icon) {
        this.type = type;
        this.label = label;
        this.icon = icon;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}
