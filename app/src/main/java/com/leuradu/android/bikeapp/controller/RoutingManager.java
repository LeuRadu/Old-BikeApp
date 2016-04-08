package com.leuradu.android.bikeapp.controller;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.activities.MapActivity;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.routing.SKRouteInfo;
import com.skobbler.ngx.routing.SKRouteJsonAnswer;
import com.skobbler.ngx.routing.SKRouteListener;
import com.skobbler.ngx.routing.SKRouteManager;
import com.skobbler.ngx.routing.SKRouteSettings;

/**
 * Created by radu on 02.04.2016.
 */
public class RoutingManager {

    private Context context;
    private MapActivity.RouteType routingType;
    private SKCoordinate start;
    private SKCoordinate end;

    public RoutingManager(Context context){
        this.context = context;
        routingType = MapActivity.RouteType.SHORTEST;
        start = null;
        end = null;
    }

    public SKCoordinate getStart() {
        return start;
    }

    public void setStart(SKCoordinate start) {
        this.start = start;
    }

    public SKCoordinate getEnd() {
        return end;
    }

    public void setEnd(SKCoordinate end) {
        this.end = end;
    }

    public MapActivity.RouteType getRoutingType() {
        return routingType;
    }

    public void setRoutingType(MapActivity.RouteType type) {
        this.routingType = type;
    }

//  Initiates routing from start to end using SDK. Start & end must be set!
    public void calculateRoute() {
        SKRouteSettings route = new SKRouteSettings();
        route.setStartCoordinate(start);
        route.setDestinationCoordinate(end);
        route.setNoOfRoutes(1);
        switch (routingType) {
            case SHORTEST:
                route.setRouteMode(SKRouteSettings.SKRouteMode.BICYCLE_SHORTEST);
                break;
            case FASTEST:
                route.setRouteMode(SKRouteSettings.SKRouteMode.BICYCLE_FASTEST);
                break;
            case QUIET:
                route.setRouteMode(SKRouteSettings.SKRouteMode.BICYCLE_QUIETEST);
                break;
        }
        route.setRouteExposed(true);
        SKRouteManager.getInstance().calculateRoute(route);
    }

    public String getRoutingTime(SKRouteInfo info) {
        int seconds = info.getEstimatedTime();
        int minutes = (seconds / 60) % 60;
        int hours = (seconds / 3600);
        if (hours != 0) {
            return hours + "h " + minutes + "min";
        } else {
            return minutes + "min";
        }
    }

    public String getRoutingDistance(SKRouteInfo info) {
        return info.getDistance() + "m";
    }

}
