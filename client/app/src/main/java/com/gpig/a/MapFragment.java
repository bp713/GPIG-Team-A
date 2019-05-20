
package com.gpig.a;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gpig.a.utils.FileUtils;
import com.gpig.a.utils.MapUtils;
import com.gpig.a.utils.RouteUtils;
import com.gpig.a.utils.StatusUtils;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

public class MapFragment extends Fragment {
    
    private MapView mapView = null;
    private MapLocationListener locationListener = null;
    private LocationManager locationManager = null;
    private GeoPoint currentLocation = null;
    private GeoPoint sourceLocation = null;
    private GeoPoint destinationLocation = null;
    private String TAG = "MapFragment";

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Configuration.getInstance().load(getContext(),
                PreferenceManager.getDefaultSharedPreferences(getContext()));

        mapView = getView().findViewById(R.id.mapView);
        mapView.setMinZoomLevel(7.0);
        mapView.setMaxZoomLevel(18.0);
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setUseDataConnection(true);
        mapView.setMultiTouchControls(true);

        requestAllPermissions();

        FloatingActionButton myFab = getView().findViewById(R.id.loc);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onCenterCall(v);
            }
        });

    }

    private void setCurrentLocation(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationListener = new MapLocationListener();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }

            if (currentLocation != null) {
                mapView.getController().setCenter(currentLocation);
                mapView.getOverlays().add(0, MapUtils.createMarker(mapView, getContext(), "current", currentLocation));
            }
        }
    }
    private void routeCourier(){

        RouteCourierTask asyncTask = new RouteCourierTask((new RouteCourierTask.AsyncResponse(){

            @Override
            public void processFinish(Object[] output){
                Road road = (Road) output[0];
                sourceLocation = (GeoPoint) output[1];
                destinationLocation = (GeoPoint) output[2];
                mapView.zoomToBoundingBox(road.mBoundingBox, true, 75);
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                mapView.getOverlays().add(1, roadOverlay);

                mapView.getOverlays().add(2, MapUtils.createMarker(mapView, getContext(), "src", sourceLocation));
                mapView.getOverlays().add(3, MapUtils.createMarker(mapView, getContext(), "des", destinationLocation));
                mapView.invalidate();
            }
        }));

        String json = null;
        String filename = "Route.json";
        if (StatusUtils.isNetworkAvailable(getActivity())){
            //connect to the server?
            //String jString = BonusPackHelper.requestStringFromUrl(url); use this???
            json = FileUtils.readJsonAsset(getActivity(), "example_route/route3.json");
            // if the route is different from the one stored then update it
            if (RouteUtils.hasRouteChanged(getActivity(), filename, json)) {
                FileUtils.writeToInternalStorage(getActivity(), filename, json);
            }
            asyncTask.execute(json);
        }
        else {
            if (FileUtils.doesFileExist(getActivity(), filename)) {
                json = FileUtils.readFromInternalStorage(getActivity(), filename);
                asyncTask.execute(json);
            }
            else {
                // display a popup saying connect to the internet?
                Log.e(TAG, "No internet and no route downloaded");
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("No Route Downloaded");
                alert.setMessage("Please connect to the internet to download a route");
                alert.setPositiveButton("OK",null);
                alert.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // if the permissions have changed then get the location
        if (requestCode == 1){
            setCurrentLocation();
            routeCourier();
        }
        else {
            Log.e(TAG, "Error");
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    private void requestAllPermissions(){
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_WIFI_STATE
        };

        if(!hasPermissions(getContext(), PERMISSIONS)){
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }
        else{
            setCurrentLocation();
            routeCourier();
        }
    }

    public class MapLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            if (!(location.getLatitude() == currentLocation.getLatitude() && location.getLongitude() == currentLocation.getLongitude()) || currentLocation == null) {
                currentLocation = new GeoPoint(location);
                mapView.getOverlays().remove(0);
                mapView.getOverlays().add(0, MapUtils.createMarker(mapView, getContext(), "current", currentLocation));
                mapView.invalidate();
            }
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    public void onCenterCall(View v){
        mapView.getController().animateTo(currentLocation);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    static class RouteCourierTask extends AsyncTask<String, Integer, Object[]> {

        public interface AsyncResponse {
            void processFinish(Object[] output);
        }

        private AsyncResponse delegate = null;

        private RouteCourierTask(AsyncResponse delegate){
            this.delegate = delegate;
        }

        @Override
        protected Object[] doInBackground(String... params) {
            MyGraphHopperRoadManager mgh = new MyGraphHopperRoadManager();
            Road road = mgh.getRoads(params[0])[0];
            return new Object[]{road, mgh.source, mgh.destination};
        }

        @Override
        protected void onPostExecute(Object[] result) {
            delegate.processFinish(result);
        }
    }
}
