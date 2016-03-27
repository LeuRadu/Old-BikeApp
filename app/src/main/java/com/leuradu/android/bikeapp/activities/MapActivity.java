package com.leuradu.android.bikeapp.activities;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.leuradu.android.bikeapp.App;
import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.model.Location;
import com.leuradu.android.bikeapp.utils.Util;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.SKPrepareMapTextureThread;
import com.skobbler.ngx.map.SKAnimationSettings;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKCalloutView;
import com.skobbler.ngx.map.SKCoordinateRegion;
import com.skobbler.ngx.map.SKMapCustomPOI;
import com.skobbler.ngx.map.SKMapFragment;
import com.skobbler.ngx.map.SKMapPOI;
import com.skobbler.ngx.map.SKMapSurfaceListener;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKMapViewHolder;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.map.SKPOICluster;
import com.skobbler.ngx.map.SKPolyline;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.navigation.SKAdvisorSettings;
import com.skobbler.ngx.reversegeocode.SKReverseGeocoderManager;
import com.skobbler.ngx.search.SKSearchResult;
import com.skobbler.ngx.tracks.SKGPXElementType;
import com.skobbler.ngx.tracks.SKTrackElement;
import com.skobbler.ngx.tracks.SKTrackElementType;
import com.skobbler.ngx.tracks.SKTracksFile;
import com.skobbler.ngx.tracks.SKTracksPoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by radu on 23.03.2016.
 */
public class MapActivity extends AppCompatActivity implements SKMapSurfaceListener {

    public static final String TAG = "MapActivity";

    private SKMapViewHolder mapViewHolder;
    private SKMapSurfaceView mapView;

    private static int nextUniqueId = 0;

    private HashMap<Integer,Location> mLocations;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, MapActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mLocations = new HashMap<>();

        SKMapFragment mapFragment = (SKMapFragment)getFragmentManager().findFragmentById( R.id.map_fragment);
        mapFragment.initialise();
        mapFragment.setMapSurfaceListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapViewHolder.onPause();
    }

    @Override
    public void onSurfaceCreated(SKMapViewHolder skMapViewHolder) {
        mapViewHolder = skMapViewHolder;
        mapViewHolder.onResume();
        mapView = mapViewHolder.getMapSurfaceView();

        initialiseMapView();
    }

    private void initialiseMapView() {
        mapView.centerMapOnPosition(new SKCoordinate(23.587555, 46.783135));
        mapView.setZoom(15);
    }

    //    TODO: work on this placeholder menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_show_track:
                drawGPXTracks();
                break;
        }
        return true;
    }

//    Draws the tracks in a gpx file as polylines
    private void drawGPXTracks() {
//        TODO: Use a better GPX file for presentation!
        SKTracksFile tracksFile = SKTracksFile.loadAtPath(App.getResourcesDirPath() + "GPXTracks/Cluj3.gpx");
        SKTrackElement root =tracksFile.getRootTrackElement();

        for (SKTrackElement child : root.getChildElements()) {
            {
//                Log.d(TAG, "Found child of type: " + typeToString(child.getType()));
//                for (SKTracksPoint point : child.getPointsOnTrackElement()) {
//                    Log.d(TAG, "Point: " + point.getLongitude() + ", " + point.getLatitude());
//                }
                drawPolyline(child);
            }
        }
    }

//    Draws an SKTrackElement as a polyline
    private void drawPolyline(SKTrackElement track) {

        SKPolyline polyline = new SKPolyline();
        ArrayList<SKCoordinate> nodes = new ArrayList<SKCoordinate>();

        for (SKTracksPoint point : track.getPointsOnTrackElement()) {
            nodes.add(new SKCoordinate(point.getLongitude(), point.getLatitude()));
        }
        polyline.setNodes(nodes);
        polyline.setColor(new float[]{0f, 0f, 1f, 0.5f});
        polyline.setOutlineColor(new float[]{0.1f, 1f, 0.1f, 1f});
//        polyline.setLineSize(20);
        polyline.setOutlineSize(15);
        int id = nextUniqueId++;
        polyline.setIdentifier(id);
        mapView.addPolyline(polyline);
    }

    @Override
    public void onLongPress(SKScreenPoint skScreenPoint) {

        Log.d(TAG, "Long pressed!");
        SKCoordinate point = mapView.pointToCoordinate(skScreenPoint);
//        Why do this?
        SKSearchResult result = SKReverseGeocoderManager.getInstance()
                .reverseGeocodePosition(point);

        addAnnotation(point);
    }

//    Adds a annotation to the map with a corresponding "Location" object in mLocations
    private void addAnnotation(SKCoordinate point) {
        int id = nextUniqueId++;
        SKAnnotation annotation = new SKAnnotation(id);
//        TODO: check if using point instead of reverse geocode result causes problems
        annotation.setLocation(point);
        annotation.setMininumZoomLevel(5);
        annotation.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_GREEN);
        mLocations.put(id,new Location("Mock title","Description","Type"));
        mapView.addAnnotation(annotation, SKAnimationSettings.ANIMATION_PIN_DROP);
    }

    @Override
    public void onAnnotationSelected(SKAnnotation skAnnotation) {
        int id = skAnnotation.getUniqueID();
        Toast.makeText(this, "Id: "+id, Toast.LENGTH_SHORT).show();
        Location loc = mLocations.get(id);
        createPopup(skAnnotation,loc);
    }

    private void createPopup(SKAnnotation annotation, Location loc) {
        SKCalloutView view = mapViewHolder.getCalloutView();
        view.setTitle(loc.getTitle()).setDescription(loc.getDescription())
                .setVisibility(View.VISIBLE);
//        TODO: find a way to use android colors?
//        TODO: find alternative to deprecated use?
        view.setViewColor(getResources().getColor(R.color.gray));
        view.setLeftImage(getResources().getDrawable(R.drawable.courage));
//        TODO: tweak offset if necessary
//        view.setVerticalOffset(-annotation.getOffset().getY());
        view.setVerticalOffset(30f);
        view.showAtLocation(annotation.getLocation(), true);
    }

    //    Unused (yet) interface methods --------------

    @Override
    public void onActionPan() {

    }

    @Override
    public void onActionZoom() {

    }

    @Override
    public void onMapRegionChanged(SKCoordinateRegion skCoordinateRegion) {

    }

    @Override
    public void onMapRegionChangeStarted(SKCoordinateRegion skCoordinateRegion) {

    }

    @Override
    public void onMapRegionChangeEnded(SKCoordinateRegion skCoordinateRegion) {

    }

    @Override
    public void onDoubleTap(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onSingleTap(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onRotateMap() {

    }

    @Override
    public void onInternetConnectionNeeded() {

    }

    @Override
    public void onMapActionDown(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onMapActionUp(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onPOIClusterSelected(SKPOICluster skpoiCluster) {

    }

    @Override
    public void onMapPOISelected(SKMapPOI skMapPOI) {

    }


    @Override
    public void onCustomPOISelected(SKMapCustomPOI skMapCustomPOI) {

    }

    @Override
    public void onCompassSelected() {

    }

    @Override
    public void onCurrentPositionSelected() {

    }

    @Override
    public void onObjectSelected(int i) {

    }

    @Override
    public void onInternationalisationCalled(int i) {

    }

    @Override
    public void onBoundingBoxImageRendered(int i) {

    }

    @Override
    public void onGLInitializationError(String s) {

    }

    @Override
    public void onScreenshotReady(Bitmap bitmap) {

    }
}
