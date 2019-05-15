
package com.gpig.a;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mapsforge.core.graphics.Color;
import org.mapsforge.map.android.layers.MyLocationOverlay;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

//TODO: check conditions and update check in display
public class MapFragment extends Fragment {

    private int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1001;
    private MapView mapView = null;
    private MapLocationListener locationListener = null;
    private LocationManager locationManager = null;
    private GeoPoint currentLocation = null;
    private ArrayList<OverlayItem> items = null;

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
        // TODO: Use the ViewModel

        Configuration.getInstance().load(getContext(),
                PreferenceManager.getDefaultSharedPreferences(getContext()));

        mapView = getView().findViewById(R.id.mapView);
        mapView.setMinZoomLevel(5.0);
        //mapView.setScrollableAreaLimitDouble(mapView.getBoundingBox());
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setUseDataConnection(true);

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);

        }
        else {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationListener = new MapLocationListener();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if( location != null ) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }

            mapView.getController().setCenter(currentLocation);

            items = new ArrayList<OverlayItem>();
            items.add(new OverlayItem("Your Location", "This is where you are", new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()))); // Lat/Lon decimal degrees

            ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(
                    getActivity(), items, new CurrentLocationMarker());
            mOverlay.setFocusItemsOnTap(true);

            mapView.getOverlays().add(mOverlay);
        }




    }

    private class CurrentLocationMarker implements ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
        @Override
        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
            return false;
        }

        @Override
        public boolean onItemLongPress(final int index, final OverlayItem item) {
            return false;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // if the permissions have changed then get the location
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationListener = new MapLocationListener();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if( location != null ) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }
        }
    }

    public class MapLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            GeoPoint newLoc = new GeoPoint(location);

            if (newLoc.getLatitude() != currentLocation.getLatitude() && newLoc.getLongitude() != currentLocation.getLongitude()) {
                currentLocation = newLoc;
                //mapView.getController().setCenter(currentLocation);
                mapView.getController().animateTo(currentLocation);

                items = new ArrayList<OverlayItem>();
                items.add(new OverlayItem("Your Location", "This is where you are",
                        new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude())));

                ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(
                        getActivity(), items, new CurrentLocationMarker());
                mOverlay.setFocusItemsOnTap(true);

                mapView.getOverlays().add(mOverlay);
                mOverlay.setFocusItemsOnTap(true);

                items.clear();
                mapView.getOverlays().clear();
                mapView.getOverlays().add(mOverlay);

            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

}
