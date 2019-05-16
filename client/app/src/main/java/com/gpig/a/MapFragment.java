
package com.gpig.a;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

import com.google.android.gms.maps.SupportMapFragment;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class MapFragment extends Fragment {
    
    private MapView mapView = null;
    private MapLocationListener locationListener = null;
    private LocationManager locationManager = null;
    private GeoPoint currentLocation = null;
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
        mapView.setMinZoomLevel(3.0);
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setUseDataConnection(true);

        requestAllPermissions();

        FloatingActionButton myFab = (FloatingActionButton) getView().findViewById(R.id.loc);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onCenterCall(v);
            }
        });

    }

    private void setCurrentLocation(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationListener = new MapLocationListener();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }

            mapView.getController().setCenter(currentLocation);

            Marker currentMarker = new Marker(mapView);
            currentMarker.setPosition(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));

            mapView.getOverlays().add(0, currentMarker);
        }
    }
    private void routeCourier(ArrayList<GeoPoint> points, String manager){
        Road road = null;
        if (manager.equals("OSRM")) {
            OSRMRoadManager rm = new OSRMRoadManager(getContext());
            road = rm.getRoad(points);

        }
        else if (manager.equals("GraphHopper")){
            GraphHopperRoadManager gh = new GraphHopperRoadManager("eff4071c-2659-4d46-ad03-0097a984440c", false);
            road = gh.getRoad(points);
        }
        else if (manager.equals("MyGHRM")){
            MyGraphHopperRoadManager mgh = new MyGraphHopperRoadManager();
            String json = readJsonAsset("example_json/response.json");
            road = mgh.getRoads(points, json)[0];
        }
        else {
            Log.w(TAG,"Invalid Routing Manager Specified");
        }
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        //if (mapView.getOverlays().size() >= 2) {
        //    mapView.getOverlays().remove(1);
        //}
        mapView.getOverlays().add(1, roadOverlay);

    }

    private String readJsonAsset(String location){
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open(location);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // if the permissions have changed then get the location
        if (requestCode == 1){
            setCurrentLocation();
            ArrayList<GeoPoint> points = new ArrayList<>();
            GeoPoint destination = new GeoPoint(-33.865143, 151.209900);
            points.add(currentLocation);
            points.add(destination);

            routeCourier(points, "MyGHRM");

            Marker desMarker = new Marker(mapView);
            desMarker.setPosition(destination);

            mapView.getOverlays().add(desMarker);
            mapView.invalidate();
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

            ArrayList<GeoPoint> points = new ArrayList<>();
            GeoPoint destination = new GeoPoint(-33.865143, 151.209900);
            points.add(currentLocation);
            points.add(destination);

            routeCourier(points, "MyGHRM");

            Marker desMarker = new Marker(mapView);
            desMarker.setPosition(destination);

            mapView.getOverlays().add(desMarker);
            mapView.invalidate();
        }
    }

    public class MapLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            if (!(location.getLatitude() == currentLocation.getLatitude() && location.getLongitude() == currentLocation.getLongitude())) {
                currentLocation = new GeoPoint(location);
                Marker currentMarker = new Marker(mapView);
                currentMarker.setPosition(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));

                mapView.getOverlays().remove(0);
                mapView.getOverlays().add(0, currentMarker);

                ArrayList<GeoPoint> points = new ArrayList<>();
                // This will come from the server
                destinationLocation = new GeoPoint(-33.865143, 151.209900);
                points.add(currentLocation);
                points.add(destinationLocation);

                // route with graphhopper however, later we need to load route from server somehow
                routeCourier(points, "MyGHRM");

                Marker desMarker = new Marker(mapView);
                desMarker.setPosition(destinationLocation);

                mapView.getOverlays().add(desMarker);
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

}
