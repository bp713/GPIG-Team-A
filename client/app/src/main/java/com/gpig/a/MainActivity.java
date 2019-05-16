package com.gpig.a;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.LinearLayout;

import com.gpig.a.tickets.Ticket;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TicketFragment.OnListFragmentInteractionListener {

    private MapFragment mapFragment;
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the fragment_map is ready to be used.
        mapFragment = new MapFragment();
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, mapFragment, "MapFragment");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // this seems like a clunky way of doing things is there a better way?
    public void onTicketClick(View v){
        LinearLayout layout = (LinearLayout)v.getParent();
        for(int index=0; index<layout.getChildCount(); ++index) {
            View nextChild = layout.getChildAt(index);
            if(nextChild.toString().contains("detailed_info")){
                if(nextChild.getVisibility() != View.VISIBLE) {
                    nextChild.setVisibility(View.VISIBLE);
                }else {
                    nextChild.setVisibility(View.GONE);
                }
            }
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, mapFragment, "MapFragment");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        } else if (id == R.id.nav_tickets) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, new TicketFragment(), "TicketFragment");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        } else if (id == R.id.nav_check_in) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, new CheckInFragment(), "CheckInFragment");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onListFragmentInteraction(Ticket item) {

    }

    public void onDistressCall(View v){
        //TODO confirm user is in distress?
        //TODO tell server user is in distress
    }

}