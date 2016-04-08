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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.geo.GeoPoint;
import com.leuradu.android.bikeapp.App;
import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.controller.Controller;
import com.leuradu.android.bikeapp.model.Annotation;
import com.leuradu.android.bikeapp.model.CustomMenuItem;
import com.leuradu.android.bikeapp.utils.BackendUtils;
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
import com.skobbler.ngx.routing.SKRouteSettings;
import com.skobbler.ngx.tracks.SKTrackElement;
import com.skobbler.ngx.tracks.SKTracksFile;
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

    public enum MenuAction {
        NOACTION, SHOW_BIKE_LANES, ROUTE, SET_STARTING_POINT, SET_END_POINT, LOGIN, LOGOUT,
        SHOW_FAVORITES, SHOW_EVENTS, SAVE_TO_FAVORITES, SAVE_TO_EVENTS
    }

    public enum PressType {
        NORMAL, SELECT_ROUTE_START, SELECT_ROUTE_DESTINATION
    }

    public enum MapState {
        DEFAULT, ROUTING
    }

    public enum RouteType {
        SHORTEST, FASTEST, QUIET
    }

    public enum AnnotationType {
        NEW, FAVORITE, EVENT, START, END
    }

//    States
    private PressType typeLongPress;
    private MapState mMapState;
    private RouteType mRouteType;
    private boolean mBikeLanesShown;
    private boolean mFavoritesShown;
    private boolean mEventsShown;
    private boolean mUserLoggedIn;

    //  --Preset variables
    //    Set these through some config?
    private static int minZoomLevel = 10;
    private static int nextUniqueId = 20;

//  --Used for routing
    private SKCoordinate mStartPoint;
    private SKCoordinate mEndPoint;
    private SKAnnotation mStartAnnotation;
    private SKAnnotation mEndAnnotation;
    private String mRoutingDistance;
    private String mRoutingTime;

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
    private List<GeoPoint> mFavorites;
//    TODO: save annotation extra info here
    private HashMap<Integer, Annotation> mAnnotationInfo;

//  --Others
    private SKAnnotation mCrtAnnotation;
    private AnnotationType mCrtAnnotationType;
    private int mFirstFavoriteId;
    private int mLastFavoriteId;
    private int mFirstEventId;
    private int mLastEventId;

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

//        TESTING FEATURES:
        mCtrl = new Controller(this);
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
//          TODO: find a better way to update left menu after login
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
            }
        });

        mButtonRouteFastest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRouteType != RouteType.FASTEST) {
                    changeRouteType(RouteType.FASTEST);
                    mCtrl.setRoutingType(RouteType.FASTEST);
                    mCtrl.calculateRoute();
                }
            }
        });

        mButtonRouteShortest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRouteType != RouteType.SHORTEST) {
                    changeRouteType(RouteType.SHORTEST);
                    mCtrl.setRoutingType(RouteType.SHORTEST);
                    mCtrl.calculateRoute();
                }
            }
        });

        mButtonRouteQuiet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRouteType != RouteType.QUIET) {
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
//          TODO: another way to create/update drawer menus (no complete recreation)
        if (mUserLoggedIn) {
            mLeftMenu.add(new CustomMenuItem(MenuAction.NOACTION, info,
                    "Welcome, " + Backendless.UserService.CurrentUser().getProperty("name")));
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
        typeLongPress = PressType.NORMAL;
        mMapState = MapState.DEFAULT;
        mRouteType = RouteType.SHORTEST;
        mBikeLanesShown = false;
        mFavoritesShown = false;
        mUserLoggedIn = false;
        mStartPoint = null;
        mEndPoint = null;
        mStartAnnotation = null;
        mEndAnnotation = null;
        mCrtAnnotation = null;
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
        switch (typeLongPress) {
            case NORMAL:
                SKAnnotation a = createAnnotation(point, AnnotationType.NEW);
                setCrtAnnotation(a, AnnotationType.NEW);
                showPopup(a);
                break;
//            TODO: eliminate this useless part
            case SELECT_ROUTE_START:
                break;
            case SELECT_ROUTE_DESTINATION:
                break;
        }
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
//                TODO: find a way to update menu
                closeDrawers();
                BackendUtils.logout(this);
                initDrawers();
                break;
            case SHOW_FAVORITES:
                if (mFavoritesShown) {
                    removeFavorites();
                } else {
                    showFavorites();
                }
                mFavoritesShown = !mFavoritesShown;
                break;
            case SAVE_TO_FAVORITES:
                closeDrawers();
//                TODO: use dialog to add name/descr
                point = mCrtAnnotation.getLocation();
                BackendUtils.saveGeoPoint(this, point.getLongitude(), point.getLatitude(),
                        "Placeholder_name", "Placeholder_descr","Favorites");
                if (mFavoritesShown) {
//                    TODO: must sync this, or last item is not added at all
                    refreshFavorites();
                    mapView.deleteAnnotation(mCrtAnnotation.getUniqueID());
                }
                break;
            case SHOW_EVENTS:
                if (mEventsShown) {
                    removeEvents();
                } else {
                    showEvents();
                }
                mEventsShown = !mEventsShown;
                break;
            case SAVE_TO_EVENTS:
                closeDrawers();
                point = mCrtAnnotation.getLocation();
                BackendUtils.saveGeoPoint(this, point.getLongitude(), point.getLatitude(),
                        "Placeholder_name", "Placeholder_descr", "Events");
                if (mEventsShown) {
                    refreshEvents();
                    mapView.deleteAnnotation(mCrtAnnotation.getUniqueID());
                }
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
//        setCrtAnnotation(a);
        int id = a.getUniqueID();
        Toast.makeText(this, "Id: "+id, Toast.LENGTH_SHORT).show();
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
        return a;
    }

    private SKAnnotation getCrtAnnotation() {
        return mCrtAnnotation;
    }

    private void setCrtAnnotation(SKAnnotation a, AnnotationType type) {
        clearCrtAnnotation();
        dismissPopup();
        mCrtAnnotation = a;
        mCrtAnnotationType = type;
        mapView.updateAnnotation(getCrtAnnotation());
    }
    
    private void clearCrtAnnotation() {
        if (mCrtAnnotation != null)
            mapView.deleteAnnotation(mCrtAnnotation.getUniqueID());
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

    //    TODO: different Popup for different annotation type
    //    TODO: show info depending on annotation
    private void showPopup(SKAnnotation a) {
        SKCalloutView view = mapViewHolder.getCalloutView();
        view.setVisibility(View.VISIBLE);
        view.setViewColor(getResources().getColor(R.color.gray));
        view.setLeftImage(getResources().getDrawable(R.drawable.courage));
        view.setVerticalOffset(30f);
        view.setOnRightImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mListViewRight);
            }
        });
        view.showAtLocation(a.getLocation(), true);
    }

    private void dismissPopup() {
        mapViewHolder.getCalloutView().setVisibility(View.GONE);
    }

//  ------------- Routing methods -------------------

    //    Sets mRouteType to new type and changes button text color to reflect change
    private void changeRouteType(RouteType type) {
        switch (mRouteType) {
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
        mRouteType = type;
    }

    public void setStartPoint() {
        if (getStartAnnotation() != null) {
            mapView.deleteAnnotation(getStartAnnotation().getUniqueID());
        }
        SKCoordinate location = SKCoordinate.copyOf(getCrtAnnotation().getLocation());
        mCtrl.setRoutingStart(location);
        SKAnnotation a = createAnnotation(location, AnnotationType.START);
        setStartAnnotation(a);
        clearCrtAnnotation();
        dismissPopup();
    }

    private void setEndPoint() {
        if (getEndAnnotation() != null) {
            mapView.deleteAnnotation(getEndAnnotation().getUniqueID());
        }
        SKCoordinate location = SKCoordinate.copyOf(getCrtAnnotation().getLocation());
        mCtrl.setRoutingEnd(location);
        SKAnnotation a = createAnnotation(location, AnnotationType.END);
        setEndAnnotation(a);
        clearCrtAnnotation();
        dismissPopup();
    }

    public void startRouting() {
        Log.d("ROUTE start", mCtrl.getRoutingStart().toString());
        Log.d("ROUTE end", mCtrl.getRoutingEnd().toString());
        Log.d("ROUTE check", mCtrl.checkRoutingPointsSet()+"");

        if (!mCtrl.checkRoutingPointsSet()) {
            Toast.makeText(this,"Start and end must be set!",Toast.LENGTH_LONG).show();
            return;
        }
        mCtrl.calculateRoute();
        mMapState = MapState.ROUTING;
        mRoutingLayout.setVisibility(View.VISIBLE);
    }

    public void closeRouting() {
        mMapState = MapState.DEFAULT;
        SKRouteManager.getInstance().clearCurrentRoute();
        findViewById(R.id.layout_routing).setVisibility(View.GONE);
    }

    @Override
    public void onRouteCalculationCompleted(SKRouteInfo skRouteInfo) {
//          TODO: rewrite this when computing alternative routes!
        mRoutingDistance = skRouteInfo.getDistance() + "m";
        int seconds = skRouteInfo.getEstimatedTime();
        int minutes = (seconds / 60) % 60;
        int hours = (seconds / 3600);
        if (hours != 0) {
            mRoutingTime = hours + "h " + minutes + "min";
        } else {
            mRoutingTime = minutes + "min";
        }
        mTextRoutingTime.setText(mRoutingTime);
        mTextRoutingDistance.setText(mRoutingDistance);
    }

    @Override
    public void onAllRoutesCompleted() {
//      TODO: do something when implementing alternative routes
    }


//  ---------------- Tracks -----------------


    //    Draws the tracks in a gpx file as polylines
    private void drawGPXTracks() {
//        TODO: Use a better GPX file for presentation!
//        TODO: Find a way to draw routes over the tracks!
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

    private void showEvents() {
        BackendUtils.fetchData(this, BackendUtils.DataType.EVENTS);
    }

    private void removeEvents() {
        for (int i = mFirstEventId; i<= mLastEventId; i++) {
            mapView.deleteAnnotation(i);
        }
    }

    private void showFavorites() {
        BackendUtils.fetchData(this, BackendUtils.DataType.FAVORITES);
    }

//    TODO: merge with removeEvents
    private void removeFavorites() {
        for (int i = mFirstFavoriteId; i<= mLastFavoriteId; i++) {
            mapView.deleteAnnotation(i);
        }
    }

    private void refreshFavorites() {
        removeFavorites();
        showFavorites();
    }

    private void refreshEvents() {
        removeEvents();
        showEvents();
    }

    public void updateFavorites(List<GeoPoint> points) {
        Log.d("updateFavorites", "# of points: " + points.size());
        mFavorites = points;
        mFirstFavoriteId = nextUniqueId;
        for (GeoPoint point : points) {
            createAnnotation(new SKCoordinate(point.getLongitude(), point.getLatitude()),AnnotationType.EVENT);
        }
        mLastFavoriteId = nextUniqueId - 1;
    }

//    TODO: merge updateFavorites & updateEvents
    public void updateEvents(List<GeoPoint> points) {
        Log.d("updateEvents", "# of points: " + points.size());
        mFavorites = points;
        mFirstEventId = nextUniqueId;
        for (GeoPoint point : points) {
            createAnnotation(new SKCoordinate(point.getLongitude(), point.getLatitude()),AnnotationType.EVENT);
        }
        mLastEventId = nextUniqueId - 1;
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
