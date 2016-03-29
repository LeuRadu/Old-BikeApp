package com.leuradu.android.bikeapp.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.leuradu.android.bikeapp.App;
import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.model.CustomMenuItem;
import com.leuradu.android.bikeapp.model.Location;
import com.leuradu.android.bikeapp.utils.MenuListAdapter;
import com.skobbler.ngx.SKCoordinate;
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
import com.skobbler.ngx.map.SKPOICluster;
import com.skobbler.ngx.map.SKPolyline;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.routing.SKRouteInfo;
import com.skobbler.ngx.routing.SKRouteJsonAnswer;
import com.skobbler.ngx.routing.SKRouteListener;
import com.skobbler.ngx.routing.SKRouteManager;
import com.skobbler.ngx.routing.SKRouteSettings;
import com.skobbler.ngx.tracks.SKTrackElement;
import com.skobbler.ngx.tracks.SKTracksFile;
import com.skobbler.ngx.tracks.SKTracksPoint;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by radu on 23.03.2016.
 */
public class MapActivity extends AppCompatActivity implements SKMapSurfaceListener, SKRouteListener {

//  --Constants
    public static final String TAG = "MapActivity";

    public enum MenuAction {
        HEADER, SHOW_BIKE_LANES, ROUTE, SET_STARTING_POINT, SET_END_POINT
    }

    public enum PressType {
        NORMAL, SELECT_ROUTE_START, SELECT_ROUTE_DESTINATION
    }

//    States
    private PressType stateLongPress;

    //  --Preset variables
    //    Set this through some config?
    private static int minZoomLevel = 10;
    private static int nextUniqueId = 20;


//  --Used to compute route
    private SKCoordinate mStartPoint;
    private SKCoordinate mEndPoint;
    private SKAnnotation mStartPointAnnotation;
    private SKAnnotation mEndPointAnnotation;
//    TODO: don't use these anymore
    private static final int mStartAnnotationId = 0;
    private static final int mEndAnnotationId = 1;

//  --SKMap-specific objects
    private SKMapViewHolder mapViewHolder;
    private SKMapSurfaceView mapView;

//  --Views
    private DrawerLayout mDrawerLayout;
    private ListView mListViewLeftDrawer;
    private ListView mListViewRightDrawer;

//  --Collections
//    Holds info for annotations
    private HashMap<Integer,Location> mLocations;
    private ArrayList<CustomMenuItem> mLeftDrawerList;
    private ArrayList<CustomMenuItem> mRightDrawerList;
//    TODO: find a more elegant solution to associate an action to a menu item
    private ArrayList<MenuAction> mLeftMenuActionList;
    private ArrayList<MenuAction> mRightMenuActionList;

//  --Others
    private SKAnnotation mCurrentAnnotation;

    public static Intent newIntent(Context context) {
        return new Intent(context, MapActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initViews();
        initDrawers();
        initVariables();

        SKRouteManager.getInstance().setRouteListener(this);
        SKMapFragment mapFragment = (SKMapFragment)getFragmentManager().findFragmentById( R.id.map_fragment);
        mapFragment.initialise();
        mapFragment.setMapSurfaceListener(this);
    }

    private void initViews() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mListViewLeftDrawer = (ListView) findViewById(R.id.left_drawer);
        mListViewRightDrawer = (ListView) findViewById(R.id.right_drawer);
    }

    private void initDrawers() {
        mLeftDrawerList = new ArrayList<>();
        mLeftMenuActionList = new ArrayList<>();
        mRightDrawerList = new ArrayList<>();
        mRightMenuActionList = new ArrayList<>();
//        TODO: atempt to use multiple right drawers interchangeably

        int header = CustomMenuItem.TYPE_HEADER;
        int item = CustomMenuItem.TYPE_ITEM;
        Drawable courage = getResources().getDrawable(R.drawable.courage);

        mLeftDrawerList.add(new CustomMenuItem(header, "ACTIONS"));
        mLeftDrawerList.add(new CustomMenuItem(item, "Show bike lanes", courage));
        mLeftDrawerList.add(new CustomMenuItem(item, "Route", courage));
//        mLeftDrawerList.add(new CustomMenuItem(item, "...", courage));
        mLeftMenuActionList.add(MenuAction.HEADER);
        mLeftMenuActionList.add(MenuAction.SHOW_BIKE_LANES);
        mLeftMenuActionList.add(MenuAction.ROUTE);

        mRightDrawerList.add(new CustomMenuItem(header, "ROUTING"));
        mRightDrawerList.add(new CustomMenuItem(item, "Set as starting point"));
        mRightDrawerList.add(new CustomMenuItem(item, "Set as destination"));

        mRightMenuActionList.add(MenuAction.HEADER);
        mRightMenuActionList.add(MenuAction.SET_STARTING_POINT);
        mRightMenuActionList.add(MenuAction.SET_END_POINT);

//        We pass 0 because in adapter class we inflate 2 different layouts depending
//        on CustomMenuItem type
        mListViewLeftDrawer.setAdapter(new MenuListAdapter(this, 0, mLeftDrawerList));
        mListViewLeftDrawer.setOnItemClickListener(new LeftMenuItemClickListener());
        mListViewRightDrawer.setAdapter(new MenuListAdapter(this, 0, mRightDrawerList));
//        TODO: rethink this - do you need 2 listeners?
        mListViewRightDrawer.setOnItemClickListener(new RightMenuItemClickListener());
    }

    //    Initialize some member variables
    private void initVariables() {
        mLocations = new HashMap<>();
        mStartPoint = null;
        mEndPoint = null;
        stateLongPress = PressType.NORMAL;
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
        switch (stateLongPress) {
            case NORMAL:
                addAnnotation(point,SKAnnotation.SK_ANNOTATION_TYPE_MARKER);
                break;
            case SELECT_ROUTE_START:
                break;
            case SELECT_ROUTE_DESTINATION:
                break;
        }
    }

//    Adds a annotation in coordinates "point" of given type and saves in mCurrentAnnotation
    private void addAnnotation(SKCoordinate point, int type) {
        int id = nextUniqueId++;
        if (mCurrentAnnotation != null) {
            mapView.deleteAnnotation(mCurrentAnnotation.getUniqueID());
            dismissPopup();
        }
        mCurrentAnnotation= new SKAnnotation(id);
        mCurrentAnnotation.setLocation(point);
        mCurrentAnnotation.setMininumZoomLevel(minZoomLevel);
        mCurrentAnnotation.setAnnotationType(type);
        mapView.addAnnotation(mCurrentAnnotation, SKAnimationSettings.ANIMATION_NONE);
    }

    @Override
    public void onAnnotationSelected(SKAnnotation annotation) {
        int id = annotation.getUniqueID();
        Toast.makeText(this, "Id: "+id, Toast.LENGTH_SHORT).show();
        createPopup(annotation);
    }

//    TODO: different Popup for different annotation type
    private void createPopup(SKAnnotation annotation) {
        SKCalloutView view = mapViewHolder.getCalloutView();
        view.setVisibility(View.VISIBLE);
//        view.setTitle(loc.getTitle()).setDescription(loc.getDescription());
        view.setViewColor(getResources().getColor(R.color.gray));
        view.setLeftImage(getResources().getDrawable(R.drawable.courage));
        view.setVerticalOffset(30f);
        view.setOnRightImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mListViewRightDrawer);
            }
        });
        view.showAtLocation(annotation.getLocation(), true);
    }

    private void dismissPopup() {
        mapViewHolder.getCalloutView().setVisibility(View.GONE);
    }

    public void calculateRoute() {
        if (mStartPoint == null || mEndPoint == null) {
            Toast.makeText(this,"Start and end must be set!",Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "Calculating route!");
        SKRouteSettings route = new SKRouteSettings();

        route.setStartCoordinate(mStartPoint);
        route.setDestinationCoordinate(mEndPoint);

        route.setNoOfRoutes(1);
//        TODO: way of setting route mode
        route.setRouteMode(SKRouteSettings.SKRouteMode.BICYCLE_SHORTEST);
        route.setRouteExposed(true);

        SKRouteManager.getInstance().calculateRoute(route);
    }

    @Override
    public void onAllRoutesCompleted() {
//      TODO: do something?
    }

    private class LeftMenuItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mDrawerLayout.isDrawerOpen(mListViewLeftDrawer)) {
                mDrawerLayout.closeDrawer(mListViewLeftDrawer);
            }
            menuAction(mLeftMenuActionList.get(position));
        }
    }

    private class RightMenuItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mDrawerLayout.isDrawerOpen(mListViewRightDrawer)) {
                mDrawerLayout.closeDrawer(mListViewRightDrawer);
            }
            menuAction(mRightMenuActionList.get(position));
        }
    }

//    TODO: close drawers accordingly
    private void menuAction(MenuAction action) {
        switch (action) {
            case HEADER:
                break;
            case SHOW_BIKE_LANES:
//                TODO: use checkbox instead
                drawGPXTracks();
                break;
            case ROUTE:
                calculateRoute();
                break;
            case SET_STARTING_POINT:
                setStartPoint();
                break;
            case SET_END_POINT:
                setEndPoint();
                break;
        }
    }

    public void setStartPoint() {
        if (mStartPointAnnotation != null) {
            mapView.deleteAnnotation(mStartPointAnnotation.getUniqueID());
        }
        mStartPoint = mCurrentAnnotation.getLocation();
        mStartPointAnnotation = mCurrentAnnotation;
        mStartPointAnnotation.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_GREEN);
        mCurrentAnnotation = null;
        mapView.updateAnnotation(mStartPointAnnotation);
        dismissPopup();
    }

    private void setEndPoint() {
        if (mEndPointAnnotation != null) {
            mapView.deleteAnnotation(mEndPointAnnotation.getUniqueID());
        }
        mEndPoint = mCurrentAnnotation.getLocation();
        mEndPointAnnotation = mCurrentAnnotation;
        mEndPointAnnotation.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_RED);
        mCurrentAnnotation = null;
        mapView.updateAnnotation(mEndPointAnnotation);
        dismissPopup();
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

//    Unimplemented route listener methods

    @Override
    public void onRouteCalculationCompleted(SKRouteInfo skRouteInfo) {

    }

    @Override
    public void onRouteCalculationFailed(SKRoutingErrorCode skRoutingErrorCode) {

    }


    @Override
    public void onServerLikeRouteCalculationCompleted(SKRouteJsonAnswer skRouteJsonAnswer) {

    }

    @Override
    public void onOnlineRouteComputationHanging(int i) {

    }
}
