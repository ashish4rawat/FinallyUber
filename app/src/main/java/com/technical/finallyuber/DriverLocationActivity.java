package com.technical.finallyuber;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class DriverLocationActivity extends FragmentActivity implements OnMapReadyCallback {

        private GoogleMap mMap;
        ArrayList<Marker> markers = new ArrayList<>();
        Intent newIntent;


        public void acceptRequest(View view){

                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
                query.whereEqualTo("username",newIntent.getStringExtra("username"));

                query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {

                                if(objects.size()>0 && e == null){

                                        for(ParseObject object : objects){

                                                object.put("driverUsername", ParseUser.getCurrentUser().getUsername());
                                                object.saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(ParseException e) {

                                                                if(e==null){

                                                                        Intent Dirintent = new Intent(android.content.Intent.ACTION_VIEW,

                                                                                Uri.parse("http://maps.google.com/maps?saddr="+newIntent.getDoubleExtra("driverLatitude",0)+","+newIntent.getDoubleExtra("driverLongitude",0)+"&daddr="+newIntent.getDoubleExtra("requestLatitude",0)+","+newIntent.getDoubleExtra("requestLongitude",0)));
                                                                        startActivity(Dirintent);

                                                                }

                                                        }
                                                });

                                        }


                                }

                        }
                });



        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_driver_location);
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
        }



        @Override
        public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;


                 newIntent = getIntent();

                LatLng driverLoc = new LatLng(newIntent.getDoubleExtra("driverLatitude",0), newIntent.getDoubleExtra("driverLongitude",0));
                LatLng reqLoc = new LatLng(newIntent.getDoubleExtra("requestLatitude",0), newIntent.getDoubleExtra("requestLongitude",0));

                markers.add(mMap.addMarker(new MarkerOptions().position(driverLoc).title("Driver's Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));
                markers.add(mMap.addMarker(new MarkerOptions().position(reqLoc).title("Request Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));


                final LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                        builder.include(marker.getPosition());
                }
                final LatLngBounds bounds = builder.build();


                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);



                mMap.moveCamera(cu);
                mMap.animateCamera(cu);

/*
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
                        }
                });

                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                        @Override
                        public void onCameraChange(CameraPosition arg0) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
                        }
                });


*/
        }
}
