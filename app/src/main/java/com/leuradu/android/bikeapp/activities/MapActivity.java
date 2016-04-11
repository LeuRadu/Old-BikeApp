package com.leuradu.android.bikeapp.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.leuradu.android.bikeapp.App;
import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.controller.Controller;
import com.leuradu.android.bikeapp.dialogs.EventInfoFragment;
import com.leuradu.android.bikeapp.dialogs.FavoriteFragment;
import com.leuradu.android.bikeapp.model.CustomMenuItem;
import com.leuradu.android.bikeapp.model.Event;
import com.leuradu.android.bikeapp.model.Favorite;
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
import com.skobbler.ngx.positioner.logging.SKPositionLoggingManager;
import com.skobbler.ngx.routing.SKRouteInfo;
import com.skobbler.ngx.routing.SKRouteJsonAnswer;
import com.skobbler.ngx.routing.SKRouteListener;
import com.skobbler.ngx.routing.SKRouteManager;
import com.skobbler.ngx.tracks.SKTrackElement;
import com.skobbler.ngx.tracks.SKTracksPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by radu on 23.03.2016.
 */
public class MapActivity extends AppCompatActivity implements SKMapSurfaceListener, SKRouteListener {

//  --Main application components
//  TODO: completely integrate controller in this activity
    Controller mCtrl;

//  --Constants
    public static final String TAG = "MapActivity";
    public static final String DIALOG_FAVORITES = "dialog_favorites";

    public enum MenuAction {
        NOACTION, SHOW_BIKE_LANES, ROUTE, SET_STARTING_POINT, SET_END_POINT, LOGIN, LOGOUT,
        SHOW_FAVORITES, SHOW_EVENTS, SAVE_TO_FAVORITES, SAVE_TO_EVENTS
    }

    public enum RouteType {
        SHORTEST, FASTEST, QUIET
    }

    public enum AnnotationType {
        NEW, FAVORITE, EVENT, START, END
    }

//    States
    private boolean mBikeLanesShown;
    private boolean mFavoritesShown;
    private boolean mEventsShown;
    private boolean mUserLoggedIn;

    //  --Preset variables
    //    Set these through some config?
    private static int minZoomLevel = 10;
    private static int nextUniqueId = 20;

//  --Used for routing
    private SKAnnotation mStartAnnotation;
    private SKAnnotation mEndAnnotation;

//  --SKMap-specific objects
    private SKMapViewHolder mapViewHolder;
    private SKMapSurfaceView mapView;

//  --Views
    private LinearLayout mRoutingLayout;
    private DrawerLayout mDrawerLayout;
    private ListView mListViewLeft;
    private ListView mListViewRight;

    private ImageButton mButtonCloseRouting;
    private Button mButtonRouteShortest;
    private Button mButtonRouteFastest;
    private Button mButtonRouteQuiet;
    private TextView mTextRoutingDistance;
    private TextView mTextRoutingTime;

//  --Collections
    private ArrayList<CustomMenuItem> mLeftMenu;
    private ArrayList<CustomMenuItem> mRightMenu;

    private List<Integer> mFavoriteAnnotations;
    private List<Integer> mEventAnnotations;
    private HashMap<Integer, AnnotationType> mAnnotationTypes;

//  --Others
    private SKAnnotation mMarkerAnnotation;
    private SKAnnotation mSelectedAnnotation;


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

        mCtrl = new Controller(this);
        //        TESTING FEATURES:

//        c.initMapGraph();
    }

    @Override
    public void onSurfaceCreated(SKMapViewHolder skMapViewHolder) {
        mapViewHolder = skMapViewHolder;
        mapViewHolder.onResume();
        mapView = mapViewHolder.getMapSurfaceView();

        initialiseMapView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapViewHolder.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BackendlessUser user = Backendless.UserService.CurrentUser();
        Log.d("onResume", "" + ((user == null) ? "null" : "User: " + user.getEmail()));
        if (user != null) {
            mUserLoggedIn = true;
            initDrawers();
        }

    }

    private void initViews() {
        mRoutingLayout = (LinearLayout) findViewById(R.id.layout_routing);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mListViewLeft = (ListView) findViewById(R.id.left_drawer);
        mListViewRight = (ListView) findViewById(R.id.right_drawer);

        mButtonCloseRouting = (ImageButton) findViewById(R.id.button_close_routing);
        mButtonRouteFastest = (Button) findViewById(R.id.button_route_fastest);
        mButtonRouteShortest = (Button) findViewById(R.id.button_route_shortest);
        mButtonRouteQuiet = (Button) findViewById(R.id.button_route_quiet);
        mButtonRouteShortest.setTextColor(getResources().getColor(R.color.red));

        mTextRoutingDistance = (TextView) findViewById(R.id.text_routing_distance);
        mTextRoutingTime = (TextView) findViewById(R.id.text_routing_time);

        mButtonCloseRouting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeRouting();
                clearRoutingAnnotations();
            }
        });

        mButtonRouteFastest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCtrl.getRoutingType() != RouteType.FASTEST) {
                    changeRouteType(RouteType.FASTEST);
                    mCtrl.setRoutingType(RouteType.FASTEST);
                    mCtrl.calculateRoute();
                }
            }
        });

        mButtonRouteShortest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCtrl.getRoutingType() != RouteType.SHORTEST) {
                    changeRouteType(RouteType.SHORTEST);
                    mCtrl.setRoutingType(RouteType.SHORTEST);
                    mCtrl.calculateRoute();
                }
            }
        });

        mButtonRouteQuiet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCtrl.getRoutingType() != RouteType.QUIET) {
                    changeRouteType(RouteType.QUIET);
                    mCtrl.setRoutingType(RouteType.QUIET);
                    mCtrl.calculateRoute();
                }
            }
        });
    }

    private void initDrawers() {
        mLeftMenu = new ArrayList<>();
        mRightMenu = new ArrayList<>();
//        TODO: atempt to use multiple right drawers interchangeably
        int header = CustomMenuItem.TYPE_HEADER;
        int item = CustomMenuItem.TYPE_ITEM;
        int checkbox = CustomMenuItem.TYPE_CHECKBOX;
        int info = CustomMenuItem.TYPE_INFO;
        Drawable courage = getResources().getDrawable(R.drawable.courage);

        mLeftMenu.add(new CustomMenuItem(MenuAction.NOACTION, header, "YOUR ACCOUNT"));
        if (mUserLoggedIn) {
            mLeftMenu.add(new CustomMenuItem(MenuAction.NOACTION, info,
                    "Welcome, " + mCtrl.getCurrentUsername()));
            mLeftMenu.add(new CustomMenuItem(MenuAction.SHOW_FAVORITES, checkbox, "Show favorites"));
            mLeftMenu.add(new CustomMenuItem(MenuAction.LOGOUT, item, "Log out"));
        } else {
            mLeftMenu.add(new CustomMenuItem(MenuAction.LOGIN, item, "Log in"));
        }
        mLeftMenu.add(new CustomMenuItem(MenuAction.NOACTION, header, "OPTIONS"));
        mLeftMenu.add(new CustomMenuItem(MenuAction.SHOW_BIKE_LANES, checkbox, "Show bike lanes"));
        mLeftMenu.add(new CustomMenuItem(MenuAction.SHOW_EVENTS, checkbox, "Show events"));
        mLeftMenu.add(new CustomMenuItem(MenuAction.NOACTION, header, "ACTIONS"));
        mLeftMenu.add(new CustomMenuItem(MenuAction.ROUTE, item, "Route", courage));

        mRightMenu.add(new CustomMenuItem(MenuAction.NOACTION, header, "ROUTING"));
        mRightMenu.add(new CustomMenuItem(MenuAction.SET_STARTING_POINT, item, "Set as starting point"));
        mRightMenu.add(new CustomMenuItem(MenuAction.SET_END_POINT, item, "Set as destination"));
        if (mUserLoggedIn) {
            mRightMenu.add(new CustomMenuItem(MenuAction.NOACTION, header, "SAVE LOCATION"));
            mRightMenu.add(new CustomMenuItem(MenuAction.SAVE_TO_FAVORITES, item, "Save to favorites"));
            mRightMenu.add(new CustomMenuItem(MenuAction.SAVE_TO_EVENTS, item, "Create new event"));
        }
//        We pass 0 because it's irrelevant - in adapter class we inflate 2 different layouts depending
//        on CustomMenuItem type
        //        TODO: rethink this - do you need 2 listeners?
        mListViewLeft.setAdapter(new MenuListAdapter(this, 0, mLeftMenu));
        mListViewLeft.setOnItemClickListener(new LeftMenuItemClickListener());
        mListViewRight.setAdapter(new MenuListAdapter(this, 0, mRightMenu));
        mListViewRight.setOnItemClickListener(new RightMenuItemClickListener());
    }

    private void initVariables() {
        mFavoriteAnnotations = new ArrayList<>();
        mEventAnnotations = new ArrayList<>();
        mAnnotationTypes = new HashMap<>();
        mBikeLanesShown = false;
        mFavoritesShown = false;
        mUserLoggedIn = false;
        mStartAnnotation = null;
        mEndAnnotation = null;
        mMarkerAnnotation = null;
        mSelectedAnnotation = null;
    }

    private void initialiseMapView() {
        mapView.centerMapOnPosition(new SKCoordinate(23.587555, 46.783135));
        mapView.setZoom(15);
    }

    //  Closes left & right drawers
    private void closeDrawers() {
        if (mDrawerLayout.isDrawerOpen(mListViewLeft)) {
            mDrawerLayout.closeDrawer(mListViewLeft);
        }
        if (mDrawerLayout.isDrawerOpen(mListViewRight)) {
            mDrawerLayout.closeDrawer(mListViewRight);
        }
    }

    @Override
    public void onLongPress(SKScreenPoint skScreenPoint) {
        SKCoordinate point = mapView.pointToCoordinate(skScreenPoint);
        SKAnnotation a = createAnnotation(point, AnnotationType.NEW);
        setMarkerAnnotation(a, AnnotationType.NEW);
        showPopup(a);
    }

//  ----------- MENU ----------------

    private void menuAction(MenuAction action) {
        SKCoordinate point;
        switch (action) {
            case NOACTION:
                break;
            case SHOW_BIKE_LANES:
                if (!mBikeLanesShown) {
                    drawGPXTracks();
                }
                else {
                    mapView.clearAllOverlays();
                }
                mBikeLanesShown = !mBikeLanesShown;
                break;
            case ROUTE:
                closeDrawers();
                startRouting();
                break;
            case SET_STARTING_POINT:
                closeDrawers();
                setStartPoint();
                break;
            case SET_END_POINT:
                closeDrawers();
                setEndPoint();
                break;
            case LOGIN:
                startActivity(LoginActivity.newIntent(this));
                break;
            case LOGOUT:
                closeDrawers();
                mCtrl.logout(this);
                mUserLoggedIn = false;
                initDrawers();
                break;
            case SHOW_FAVORITES:
                if (mFavoritesShown) {
                    clearFavorites();
                } else {
                    loadFavorites();
                }
                mFavoritesShown = !mFavoritesShown;
                break;
            case SAVE_TO_FAVORITES:
                closeDrawers();
                point = mSelectedAnnotation.getLocation();
//                TODO: create some method for summoning dialogs
                FragmentManager fm = getSupportFragmentManager();
                FavoriteFragment fragment = FavoriteFragment.newInstance(point,mCtrl);
                fragment.show(fm, DIALOG_FAVORITES);
                clearMarkerAnnotation();
                dismissPopup();
//              TODO: manually add saved favorite to annotations
                break;
            case SHOW_EVENTS:
                if (mEventsShown) {
                    clearEvents();
                } else {
                    loadEvents();
                }
                mEventsShown = !mEventsShown;
                break;
            case SAVE_TO_EVENTS:
                closeDrawers();
                point = mMarkerAnnotation.getLocation();
                Intent intent = EventActivity.newIntent(this,point,mCtrl);
                startActivity(intent);
                clearMarkerAnnotation();
                dismissPopup();
//                TODO: refresh events? at resume
        }
    }

    private class LeftMenuItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MenuAction action = mLeftMenu.get(position).getMenuAction();
            switch (action) {
//                TODO: maybe move this to menuAction()? (would need to have checkbox var somewhere)
                case SHOW_BIKE_LANES:
                case SHOW_FAVORITES:
                case SHOW_EVENTS:
                    CheckBox cb = (CheckBox) view.findViewById(R.id.menu_item_checkbox);
                    cb.setChecked(!cb.isChecked());
                    break;
            }
            menuAction(action);
        }
    }
    private class RightMenuItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mDrawerLayout.isDrawerOpen(mListViewRight)) {
                mDrawerLayout.closeDrawer(mListViewRight);
            }
            menuAction(mRightMenu.get(position).getMenuAction());
        }

    }


//  ------------------ Annotations ---------------

    @Override
    public void onAnnotationSelected(SKAnnotation a) {
        dismissPopup();
        Log.d("Annotation", " SELECTED");
//        setMarkerAnnotation(a);
        int id = a.getUniqueID();
        Toast.makeText(this, "Id: "+id, Toast.LENGTH_SHORT).show();
        setSelectedAnnotation(a);
        showPopup(a);
    }

//  Creates an Annotation in coordinate point of given type.
    private SKAnnotation createAnnotation(SKCoordinate point, AnnotationType type) {
        int id = nextUniqueId++;
        SKAnnotation a = new SKAnnotation(id);
        a.setLocation(point);
        a.setMininumZoomLevel(minZoomLevel);
//      TODO: use custom images (at least for favorites) and set offset
        switch (type) {
            case NEW:
                a.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_MARKER);
                break;
            case START:
                a.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_GREEN);
                break;
            case END:
                a.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_RED);
                break;
            case EVENT:
                a.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_BLUE);
                break;
            case FAVORITE:
                a.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_PURPLE);
                break;
        }
        mapView.addAnnotation(a, SKAnimationSettings.ANIMATION_NONE);
        mAnnotationTypes.put(a.getUniqueID(), type);
        return a;
    }

    private SKAnnotation getMarkerAnnotation() {
        return mMarkerAnnotation;
    }

    private void setMarkerAnnotation(SKAnnotation a, AnnotationType type) {
        clearMarkerAnnotation();
        setSelectedAnnotation(a);
        dismissPopup();
        mMarkerAnnotation = a;
        mapView.updateAnnotation(getMarkerAnnotation());
    }
    
    private void clearMarkerAnnotation() {
        if (mMarkerAnnotation != null)
            mapView.deleteAnnotation(mMarkerAnnotation.getUniqueID());
    }

    private void setSelectedAnnotation(SKAnnotation a) {
        mSelectedAnnotation = a;
    }

    private SKAnnotation getSelectedAnnotation() {
        return mSelectedAnnotation;
    }

    private void clearSelectedAnnotation() {
        if (mSelectedAnnotation != null)
            mapView.deleteAnnotation(mSelectedAnnotation.getUniqueID());
    }

    public SKAnnotation getStartAnnotation() {
        return mStartAnnotation;
    }

    public void setStartAnnotation(SKAnnotation startAnnotation) {
        mStartAnnotation = startAnnotation;
    }

    public SKAnnotation getEndAnnotation() {
        return mEndAnnotation;
    }

    public void setEndAnnotation(SKAnnotation endAnnotation) {
        mEndAnnotation = endAnnotation;
    }

    public void clearRoutingAnnotations() {
        if (mStartAnnotation != null) {
            mapView.deleteAnnotation(mStartAnnotation.getUniqueID());
        }
        if (mEndAnnotation!= null) {
            mapView.deleteAnnotation(mEndAnnotation.getUniqueID());
        }
        mStartAnnotation = null;
        mEndAnnotation = null;
    }

    private void showPopup(SKAnnotation a) {
        AnnotationType type = mAnnotationTypes.get(a.getUniqueID());
        SKCalloutView view = mapViewHolder.getCalloutView();
        view.setVisibility(View.VISIBLE);
        view.setViewColor(getResources().getColor(R.color.dark_gray_transparent));
//        view.setLeftImage(getResources().getDrawable(R.drawable.courage));
        view.setVerticalOffset(30f);
        view.setOnRightImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mListViewRight);
            }
        });
        switch (type) {
            case FAVORITE:
                Favorite f = mCtrl.getFavorite(a.getUniqueID());
                view.setTitle(f.getName());
                view.setDescription(f.getDescription());
                break;
            case EVENT:
                Event e = mCtrl.getEvent(a.getUniqueID());
                view.setTitle(e.getName());
//                TODO: get user name, not email
                view.setDescription("Added by " + e.getUserId());
                view.setLeftImage(getResources().getDrawable(R.drawable.courage));
                final int annotationId = a.getUniqueID();
                Log.d("Show Popup", e.getDate().toString());
                view.setOnLeftImageClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentManager fm = getSupportFragmentManager();
                        EventInfoFragment fragment = EventInfoFragment.newInstance(mCtrl,annotationId);
                        fragment.show(fm, DIALOG_FAVORITES);
                    }
                });
                break;
            case START:
            case END:
            case NEW:
                view.setTitle("New location");
                view.setDescription("See options on the right");
                view.setLeftImage(null);
//                TODO: reverse geocoding for street name
                break;
        }
//        TODO: still need this?
        view.redraw();
        view.showAtLocation(a.getLocation(), true);
    }

    private void dismissPopup() {
        mapViewHolder.getCalloutView().setVisibility(View.GONE);
    }

//  ------------- Routing methods -------------------

    //    Sets routing type to new type and changes button text color to reflect change
    private void changeRouteType(RouteType type) {
        switch (mCtrl.getRoutingType()) {
            case SHORTEST:
                mButtonRouteShortest.setTextColor(getResources().getColor(R.color.black));
                break;
            case FASTEST:
                mButtonRouteFastest.setTextColor(getResources().getColor(R.color.black));
                break;
            case QUIET:
                mButtonRouteQuiet.setTextColor(getResources().getColor(R.color.black));
                break;
        }
        switch (type) {
            case SHORTEST:
                mButtonRouteShortest.setTextColor(getResources().getColor(R.color.red));
                break;
            case FASTEST:
                mButtonRouteFastest.setTextColor(getResources().getColor(R.color.red));
                break;
            case QUIET:
                mButtonRouteQuiet.setTextColor(getResources().getColor(R.color.red));
                break;
        }
        mCtrl.setRoutingType(type);
    }

    public void setStartPoint() {
        if (getStartAnnotation() != null) {
            mapView.deleteAnnotation(getStartAnnotation().getUniqueID());
        }
        SKCoordinate location = SKCoordinate.copyOf(getSelectedAnnotation().getLocation());
        mCtrl.setRoutingStart(location);
        SKAnnotation a = createAnnotation(location, AnnotationType.START);
        setStartAnnotation(a);
        clearSelectedAnnotation();
        dismissPopup();
    }

    private void setEndPoint() {
        if (getEndAnnotation() != null) {
            mapView.deleteAnnotation(getEndAnnotation().getUniqueID());
        }
        SKCoordinate location = SKCoordinate.copyOf(getSelectedAnnotation().getLocation());
        mCtrl.setRoutingEnd(location);
        SKAnnotation a = createAnnotation(location, AnnotationType.END);
        setEndAnnotation(a);
        clearSelectedAnnotation();
        dismissPopup();
    }

    public void startRouting() {
        if (!mCtrl.checkRoutingPointsSet()) {
            Toast.makeText(this,"Start and end must be set!",Toast.LENGTH_LONG).show();
            return;
        }
        mCtrl.calculateRoute();
        mRoutingLayout.setVisibility(View.VISIBLE);
    }

    public void closeRouting() {
        SKRouteManager.getInstance().clearCurrentRoute();
        findViewById(R.id.layout_routing).setVisibility(View.GONE);
        mCtrl.setRoutingStart(null);
        mCtrl.setRoutingEnd(null);
    }

    @Override
    public void onRouteCalculationCompleted(SKRouteInfo info) {
//          TODO: rewrite this when computing alternative routes!
        mTextRoutingTime.setText(mCtrl.getRoutingTime(info));
        mTextRoutingDistance.setText(mCtrl.getRoutingDistance(info));
    }

    @Override
    public void onAllRoutesCompleted() {
//      TODO: do something when implementing alternative routes
    }


//  ---------------- Tracks -----------------


    //    Draws the tracks in a gpx file as polylines
    private void drawGPXTracks() {
//        TODO: Use a better GPX file for presentation - maybe parse OSM?
//        TODO: Find a way to draw routes over the tracks!
        SKTrackElement root = mCtrl.loadGPXTracks();
        for (SKTrackElement child : root.getChildElements()) {
                drawPolyline(child);
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

    private void startTrackRecording() {
        SKPositionLoggingManager manager = SKPositionLoggingManager.getInstance();
        String path = App.getResourcesDirPath() + "tracklog.gpx";
        SKPositionLoggingManager.SPositionLoggingType type =
                SKPositionLoggingManager.SPositionLoggingType.SK_POSITION_LOGGING_TYPE_GPX;
        manager.startLoggingPositions(path, type);
    }

    private void stopTrackRecording() {
        SKPositionLoggingManager manager = SKPositionLoggingManager.getInstance();
        manager.stopLoggingPositions();
    }


//  ---------- User Data ---------------------

    private void loadFavorites() {
        mCtrl.loadFavorites();
    }

    private void loadEvents() {
        mCtrl.loadEvents();
    }

    public void updateFavoriteAnnotations() {
        clearFavorites();
        List<Favorite> favorites = mCtrl.getFavorites();
        for (Favorite f : favorites) {
            SKAnnotation a = createAnnotation(f.getLocation(), AnnotationType.FAVORITE);
            int id = a.getUniqueID();
            mFavoriteAnnotations.add(id);
            f.setAnnotationId(id);
//            TODO: check if ids update
        }
    }

    public void updateEventAnnotations() {
        clearEvents();
        List<Event> events = mCtrl.getEvents();
        for (Event e : events) {
            SKAnnotation a = createAnnotation(e.getLocation(), AnnotationType.EVENT);
            int id = a.getUniqueID();
            mEventAnnotations.add(id);
            e.setAnnotationId(id);
        }
    }

    private void clearFavorites() {
        if (mFavoriteAnnotations != null) {
            for (int i : mFavoriteAnnotations) {
                mapView.deleteAnnotation(i);
            }
        }
    }

    private void clearEvents() {
        if (mEventAnnotations != null) {
            for (int i : mEventAnnotations) {
                mapView.deleteAnnotation(i);
            }
        }
    }

    //   ------------ Unused (yet) interface methods --------------

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
