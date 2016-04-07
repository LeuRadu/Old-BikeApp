package com.leuradu.android.bikeapp.utils;

import android.content.Context;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;
import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.activities.MapActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by radu on 30.03.2016.
 */
public class BackendUtils {

    public enum DataType {
        FAVORITES, EVENTS
    }

    private static Context mContext;

    public static void logout(Context context) {
        mContext = context;
        LoadingCallback<Void> callback = createLogoutCallback(context);
        callback.showProgressDialog();
        Backendless.UserService.logout(callback);
    }

    public static void saveGeoPoint(Context context, double lon, double lat, String name, String descr, String category) {
        mContext = context;
        List<String> categories = new ArrayList<>();
        categories.add(category);
        HashMap<String, String> meta = new HashMap<>();
//        TODO: find a (built-in) way to do this?
        meta.put("User", Backendless.UserService.CurrentUser().getEmail());
        meta.put("Name", name);
        meta.put("Description", descr);
        GeoPoint point = new GeoPoint(lat, lon, categories, meta);
        LoadingCallback<GeoPoint> callback = createSaveLocationCallback(mContext);
        callback.showProgressDialog();
        Backendless.Geo.savePoint(point, callback);
    }

    public static void fetchData(Context context, DataType type) {
        mContext = context;
        BackendlessGeoQuery query = new BackendlessGeoQuery();
        String category = "";
        switch (type) {
            case FAVORITES:
                category = "Favorites";
                break;
            case EVENTS:
                category = "Events";
                break;
        }
        query.addCategory(category);
        HashMap<String, String> meta = new HashMap<>();
        meta.put("User", Backendless.UserService.CurrentUser().getEmail());
        query.setMetadata(meta);
        LoadingCallback<BackendlessCollection<GeoPoint>> callback = createFetchDataCallback(mContext, type);
        callback.showProgressDialog();
        Backendless.Geo.getPoints(query, callback);
    }


    private static LoadingCallback<Void> createLogoutCallback(Context context) {
        return new LoadingCallback<Void>(context,
                context.getString(R.string.logout_wait)){
            @Override
            public void handleResponse(Void response) {
                super.handleResponse(response);
                Toast.makeText(mContext, "Log out successful", Toast.LENGTH_LONG).show();
            }
        };
    }

    private static LoadingCallback<GeoPoint> createSaveLocationCallback(Context context) {
        return new LoadingCallback<GeoPoint>(context,
                context.getString(R.string.save_location_wait)){
            @Override
            public void handleResponse(GeoPoint response) {
                super.handleResponse(response);
                Toast.makeText(mContext, "Location saved successful", Toast.LENGTH_LONG).show();
            }
        };
    }

    private static LoadingCallback<BackendlessCollection<GeoPoint>> createFetchDataCallback(Context context, DataType type) {
        String loadingMess = "";
        final DataType fType = type;
        switch (type) {
            case FAVORITES:
                loadingMess = context.getString(R.string.loading_favorites_wait);
                break;
            case EVENTS:
                loadingMess = context.getString(R.string.loading_favorites_wait);
                break;
        }

        return new LoadingCallback<BackendlessCollection<GeoPoint>>(context,loadingMess){
            @Override
            public void handleResponse(BackendlessCollection<GeoPoint> response) {
                super.handleResponse(response);
                String dataType;
                switch (fType) {
                    case FAVORITES:
//                        TODO: use listener
                        dataType = "Favorites";
                        ((MapActivity)mContext).updateFavorites(response.getData());
                        break;
                    default:
                        dataType = "Events";
                        ((MapActivity)mContext).updateEvents(response.getData());
                }
                Toast.makeText(mContext, dataType + " loaded successful", Toast.LENGTH_LONG).show();
            }
        };
    }
}
