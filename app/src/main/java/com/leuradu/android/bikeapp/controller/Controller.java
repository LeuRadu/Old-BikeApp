package com.leuradu.android.bikeapp.controller;

import android.content.Context;

import com.leuradu.android.bikeapp.App;
import com.leuradu.android.bikeapp.activities.MapActivity;
import com.leuradu.android.bikeapp.utils.MapGraph;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.routing.SKRouteInfo;
import com.skobbler.ngx.tracks.SKTrackElement;
import com.skobbler.ngx.tracks.SKTracksFile;

/**
 * Created by radu on 02.04.2016.
 */
public class Controller {

    RoutingManager rm;
//  TODO: connect controller to map activity
    public Controller(Context context) {
        rm = new RoutingManager(context);
    }

//  --- Routing manager functionality:

    public SKCoordinate getRoutingStart() {
        return rm.getStart();
    }

    public void setRoutingStart(SKCoordinate start) {
        rm.setStart(start);
    }

    public SKCoordinate getRoutingEnd() {
        return rm.getEnd();
    }

    public void setRoutingEnd(SKCoordinate end) {
        rm.setEnd(end);
    }

    public MapActivity.RouteType getRoutingType() {
        return rm.getRoutingType();
    }

    public void setRoutingType(MapActivity.RouteType type) {
        rm.setRoutingType(type);
    }

    public String getRoutingTime(SKRouteInfo info) {
        return rm.getRoutingTime(info);
    }

    public String getRoutingDistance(SKRouteInfo info) {
        return rm.getRoutingDistance(info);
    }

    public boolean checkRoutingPointsSet() {
        return ((rm.getStart() != null) && (rm.getEnd() != null));
    }

    public void calculateRoute() {
        rm.calculateRoute();
    }

    public SKTrackElement loadGPXTracks() {
        SKTracksFile tracksFile = SKTracksFile.loadAtPath(App.getResourcesDirPath() + "GPXTracks/Cluj3.gpx");
        return tracksFile.getRootTrackElement();
    }

    public void initMapGraph() {
        String path = App.getResourcesDirPath() + "Graphs/langa_gara.txt";
        MapGraph mg = new MapGraph(path);
        mg.computeRoute();
    }

}
