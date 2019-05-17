
package com.gpig.a;

import android.Manifest;
import android.content.Context;
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

import com.gpig.a.utils.IconUtils;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
        mapView.setMinZoomLevel(8.0);
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
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationListener = new MapLocationListener();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }

            mapView.getController().setCenter(currentLocation);

            Marker currentMarker = new Marker(mapView);
            currentMarker.setIcon(IconUtils.getMapIcon(getContext(), "current"));
            currentMarker.setPosition(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));

            mapView.getOverlays().add(0, currentMarker);
        }
    }
    private void routeCourier(){
        MyGraphHopperRoadManager mgh = new MyGraphHopperRoadManager();
        String json = readJsonAsset("example_json/response.json");
        Road road = mgh.getRoads(json)[0];
        sourceLocation = mgh.source;
        destinationLocation = mgh.destination;
        mapView.zoomToBoundingBox(road.mBoundingBox, true); // might need to calc this when its split up
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        mapView.getOverlays().add(1, roadOverlay);

        Marker srcMarker = new Marker(mapView);
        Marker desMarker = new Marker(mapView);
        srcMarker.setIcon(IconUtils.getMapIcon(getContext(), "src"));
        desMarker.setIcon(IconUtils.getMapIcon(getContext(), "des"));
        srcMarker.setPosition(sourceLocation);
        desMarker.setPosition(destinationLocation);
        mapView.getOverlays().add(2, srcMarker);
        mapView.getOverlays().add(3, desMarker);
        mapView.invalidate();
    }

    private String readJsonAsset(String location){
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open(location);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
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
            if (!(location.getLatitude() == currentLocation.getLatitude() && location.getLongitude() == currentLocation.getLongitude())) {
                currentLocation = new GeoPoint(location);
                Marker currentMarker = new Marker(mapView);
                currentMarker.setIcon(IconUtils.getMapIcon(getContext(), "current"));
                currentMarker.setPosition(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));

                mapView.getOverlays().remove(0);
                mapView.getOverlays().add(0, currentMarker);
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
}
