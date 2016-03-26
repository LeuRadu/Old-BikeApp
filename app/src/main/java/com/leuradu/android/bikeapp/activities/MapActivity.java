package com.leuradu.android.bikeapp.activities;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.leuradu.android.bikeapp.App;
import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.utils.Util;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.SKPrepareMapTextureThread;
import com.skobbler.ngx.map.SKAnnotation;
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
import com.skobbler.ngx.tracks.SKGPXElementType;
import com.skobbler.ngx.tracks.SKTrackElement;
import com.skobbler.ngx.tracks.SKTrackElementType;
import com.skobbler.ngx.tracks.SKTracksFile;
import com.skobbler.ngx.tracks.SKTracksPoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by radu on 23.03.2016.
 */
public class MapActivity extends AppCompatActivity implements SKMapSurfaceListener {

    public static final String TAG = "MapActivity";

    private SKMapViewHolder mapViewHolder;
    private SKMapSurfaceView mapView;

    private SKTracksFile mTracksFile;
    private SKTrackElement mTrackElement;


    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, MapActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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
                drawMultipleTracks();
                mapView.centerMapOnPosition(new SKCoordinate(23.587555, 46.783135));
                break;
        }
        return true;
    }

    private void drawMultipleTracks() {
        mTracksFile = SKTracksFile.loadAtPath(App.getResourcesDirPath() + "GPXTracks/Cluj3.gpx");
        mTrackElement = mTracksFile.getRootTrackElement();

        for (SKTrackElement child : mTrackElement.getChildElements()) {
            {
                Log.d(TAG, "Found child of type: " + typeToString(child.getType()));
                mapView.drawTrackElement(mTrackElement);
            }
        }
    }

    private String typeToString(SKTrackElementType type) {
        switch (type) {
            case COLLECTION:
                return "collection";
            case TRACK_POINT:
                return "track_point";
        }
        return "WHA?";
    }

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
        polyline.setIdentifier(12);
        mapView.addPolyline(polyline);
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
    public void onLongPress(SKScreenPoint skScreenPoint) {

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
    public void onAnnotationSelected(SKAnnotation skAnnotation) {

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
