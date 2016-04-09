package com.leuradu.android.bikeapp.repository;

import android.content.Context;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;
import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.activities.MapActivity;
import com.leuradu.android.bikeapp.model.Event;
import com.leuradu.android.bikeapp.utils.LoadingCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by radu on 30.03.2016.
 */
public class BackendManager {

    public interface BackendListener {
        public void onFavoritesLoaded(List<GeoPoint> favorites);
        public void onEventsLoaded(List<GeoPoint> events);
        public void onFavoriteSaved(GeoPoint favorite);
        public void onEventSaved(GeoPoint event);
    }

    public enum DataType {
        FAVORITES, EVENTS
    }

    private BackendListener listener;
    private Context mContext;

    public BackendManager(Context context) {
        mContext = context;
        listener = null;
    }

    public void setListener(BackendListener l) {
        listener = l;
    }

    public void login(String email, String password) {

    }

    public void logout() {
        LoadingCallback<Void> callback = createLogoutCallback(mContext);
        callback.showProgressDialog();
        Backendless.UserService.logout(callback);
    }

    private LoadingCallback<Void> createLogoutCallback(Context context) {
        return new LoadingCallback<Void>(context,
                context.getString(R.string.logout_wait)) {
            @Override
            public void handleResponse(Void response) {
                super.handleResponse(response);
                Toast.makeText(mContext, "Log out successful", Toast.LENGTH_LONG).show();
            }
        };
    }

//    ------- Reimplementation ----------------

    public void saveFavorite(double lon, double lat, String name, String descr) {
        List<String> categories = new ArrayList<>();
        categories.add("Favorites");
        HashMap<String, String> meta = new HashMap<>();
        meta.put("User", Backendless.UserService.CurrentUser().getEmail());
        meta.put("Name", name);
        meta.put("Description", descr);
        GeoPoint point = new GeoPoint(lat, lon, categories, meta);
        LoadingCallback<GeoPoint> callback = createSaveFavoriteCallback(mContext);
        callback.showProgressDialog();
        Backendless.Geo.savePoint(point, callback);
    }

    public void saveEvent(double lon, double lat, String name, String descr, Date date) {
        List<String> categories = new ArrayList<>();
        categories.add("Events");
        HashMap<String, String> meta = new HashMap<>();
//        TODO: find a (built-in) way to do this?
        meta.put("User", Backendless.UserService.CurrentUser().getEmail());
        meta.put("Name", name);
        meta.put("Description", descr);
        meta.put("Datetime", date.toString());
        GeoPoint point = new GeoPoint(lat, lon, categories, meta);
        LoadingCallback<GeoPoint> callback = createSaveEventCallback(mContext);
        callback.showProgressDialog();
        Backendless.Geo.savePoint(point, callback);
    }

//    TODO: rewrite fetch favorites/events to avoid repeating code
    public void fetchFavorites(Context context) {
        mContext = context;
        BackendlessGeoQuery query = new BackendlessGeoQuery();
        String category = "Favorites";
        query.addCategory(category);
        HashMap<String, String> meta = new HashMap<>();
        meta.put("User", Backendless.UserService.CurrentUser().getEmail());
        query.setMetadata(meta);
        LoadingCallback<BackendlessCollection<GeoPoint>> callback = createFavoritesCallback(mContext);
        callback.showProgressDialog();
        Backendless.Geo.getPoints(query, callback);
    }

    public void fetchEvents(Context context) {
        mContext = context;
        BackendlessGeoQuery query = new BackendlessGeoQuery();
        String category = "Events";
        query.addCategory(category);
        HashMap<String, String> meta = new HashMap<>();
        meta.put("User", Backendless.UserService.CurrentUser().getEmail());
        query.setMetadata(meta);
        LoadingCallback<BackendlessCollection<GeoPoint>> callback = createEventsCallback(mContext);
        callback.showProgressDialog();
        Backendless.Geo.getPoints(query, callback);
    }

    private LoadingCallback<BackendlessCollection<GeoPoint>> createFavoritesCallback(Context context) {
        return new LoadingCallback<BackendlessCollection<GeoPoint>>(context, context.getString(R.string.loading_favorites_wait)) {
            @Override
            public void handleResponse(BackendlessCollection<GeoPoint> response) {
                super.handleResponse(response);
                int size = response.getTotalObjects();
                listener.onFavoritesLoaded(response.getData());
                Toast.makeText(mContext, size + " favorites loaded successful", Toast.LENGTH_LONG).show();
            }
        };
    }

    private LoadingCallback<BackendlessCollection<GeoPoint>> createEventsCallback(Context context) {
        return new LoadingCallback<BackendlessCollection<GeoPoint>>(context, context.getString(R.string.loading_events_wait)) {
            @Override
            public void handleResponse(BackendlessCollection<GeoPoint> response) {
                super.handleResponse(response);
                int size = response.getTotalObjects();
                listener.onEventsLoaded(response.getData());
                Toast.makeText(mContext, size + " events loaded successful", Toast.LENGTH_LONG).show();
            }
        };
    }

    private LoadingCallback<GeoPoint> createSaveFavoriteCallback(Context context) {
        return new LoadingCallback<GeoPoint>(context,
                context.getString(R.string.save_favorite_wait)) {
            @Override
            public void handleResponse(GeoPoint response) {
                super.handleResponse(response);
                Toast.makeText(mContext, "Favorite saved successful", Toast.LENGTH_LONG).show();
                listener.onFavoriteSaved(response);
            }
        };
    }

    private LoadingCallback<GeoPoint> createSaveEventCallback(Context context) {
        return new LoadingCallback<GeoPoint>(context,
                context.getString(R.string.save_event_wait)) {
            @Override
            public void handleResponse(GeoPoint response) {
                super.handleResponse(response);
                Toast.makeText(mContext, "Event saved successful", Toast.LENGTH_LONG).show();
                listener.onEventSaved(response);
            }
        };
    }

}
