package com.leuradu.android.bikeapp.controller;

import android.content.Context;

import com.backendless.geo.GeoPoint;
import com.leuradu.android.bikeapp.App;
import com.leuradu.android.bikeapp.activities.MapActivity;
import com.leuradu.android.bikeapp.model.Event;
import com.leuradu.android.bikeapp.model.Favorite;
import com.leuradu.android.bikeapp.repository.BackendManager;
import com.leuradu.android.bikeapp.repository.LocalRepository;
import com.leuradu.android.bikeapp.utils.MapGraph;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.routing.SKRouteInfo;
import com.skobbler.ngx.tracks.SKTrackElement;
import com.skobbler.ngx.tracks.SKTracksFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by radu on 02.04.2016.
 */
public class Controller {

    private RoutingManager rm;
    private BackendManager bm;
    private LocalRepository rep;
    private MapActivity map;
    private Context mContext;

    public Controller(MapActivity m) {
        map = m;
        mContext = map;
        rm = new RoutingManager(mContext);
        bm = new BackendManager(mContext);
        rep = new LocalRepository();
        bm.setListener(new BackendManager.BackendListener() {
            @Override
            public void onFavoritesLoaded(List<GeoPoint> favorites) {
                List<Favorite> list = new ArrayList<Favorite>();
                for (GeoPoint point : favorites) {
                    list.add(new Favorite(point));
                }
                rep.setFavorites(list);
                map.updateFavoriteAnnotations();
            }

            @Override
            public void onEventsLoaded(List<GeoPoint> events) {
                List<Event> list = new ArrayList<>();
                for (GeoPoint point : events) {
                    list.add(new Event(point));
                }
                rep.setEvents(list);
                map.updateEventAnnotations();
            }

            @Override
            public void onFavoriteSaved(GeoPoint favorite) {
                String id = favorite.getObjectId();
                SKCoordinate location = new SKCoordinate(favorite.getLongitude(), favorite.getLatitude());
                String name = favorite.getMetadata("Name");
                String descr = favorite.getMetadata("Description");
                Favorite f = new Favorite(id, location, name, descr);
                rep.addFavorite(f);
//                TODO: only add newest favorite
            }

            @Override
            public void onEventSaved(GeoPoint event) {
                String id = event.getObjectId();
                SKCoordinate location = new SKCoordinate(event.getLongitude(), event.getLatitude());
                String name = event.getMetadata("Name");
                String descr = event.getMetadata("Description");
                String date = event.getMetadata("Datetime");
                String user = event.getMetadata("User");
                Event e = new Event(id, location, user, name, descr, date);
                rep.addEvent(e);
//                TODO: only add newest event
            }
        });
    }

//  -------- Backend manager functionality:

    public void loadFavorites() {
        bm.fetchFavorites(mContext);
    }

    public void loadEvents() {
        bm.fetchEvents(mContext);
    }

    public void logout(Context context) {
        bm.logout();
    }

    public void saveFavorite(double lon, double lat, String name, String descr) {
        bm.saveFavorite(lon, lat, name, descr);
    }

    public void saveEvent(double lon, double lat, String name, String descr, Date date) {
        bm.saveEvent(lon, lat, name, descr, date);
    }

    public String getCurrentUsername() {
        return bm.getCurrentUsername();
    }

//  -------- Local repository functionality:

    public List<Favorite> getFavorites() {
        return rep.getFavorites();
    }

    public Favorite getFavorite(int id) {
        return rep.getFavorite(id);
    }

    public Event getEvent(int id) {
        return rep.getEvent(id);
    }

    public void setFavorites(List<Favorite> favorites) {
        rep.setFavorites(favorites);
    }

    public void addFavorite(Favorite f) {
        rep.addFavorite(f);
    }

    public void removeFavorite(String id) {
        rep.removeFavorite(id);
    }

    public void clearFavorites() {
        rep.clearFavorites();
    }

    public List<Event> getEvents() {
        return rep.getEvents();
    }

    public void setEvents(List<Event> events) {
        rep.setEvents(events);
    }

    public void addEvent(Event e) {
        rep.addEvent(e);
    }

    public void removeEvent(String id) {
        rep.removeEvent(id);
    }

    public void clearEvents() {
        rep.clearEvents();
    }

//  -------- Routing manager functionality:

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

//   ----------- Others ------------------

    public SKTrackElement loadGPXTracks() {
        SKTracksFile tracksFile = SKTracksFile.loadAtPath(App.getResourcesDirPath() + "GPXTracks/Cluj3.gpx");
        return tracksFile.getRootTrackElement();
    }

//    FOR TESTING
    public void initMapGraph() {
        String path = App.getResourcesDirPath() + "Graphs/langa_gara.txt";
        MapGraph mg = new MapGraph(path);
//        mg.computeRoute();
    }

}
