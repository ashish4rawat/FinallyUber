package com.technical.finallyuber;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {

        private GoogleMap mMap;
        LocationManager locationManager;
        LocationListener locationListener;
        boolean requestActive = false;
        Button callUberButton;
        Handler handler = new Handler();
        TextView infoLocation;
        Boolean driverActive=false;


        public void checkForUpdates(){

                final ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
                query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                query.whereExists("driverUsername");

                query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {


                                if(e==null && objects.size()>0){

                                        driverActive=true;

                                        ParseQuery<ParseUser> query1 = ParseUser.getQuery();
                                        query1.whereEqualTo("username",objects.get(0).get("driverUsername"));
                                        query1.findInBackground(new FindCallback<ParseUser>() {
                                                @Override
                                                public void done(List<ParseUser> objects, ParseException e) {

                                                        if(e==null && objects.size()>0){


                                                                ParseGeoPoint driverLocat = new ParseGeoPoint(objects.get(0).getParseGeoPoint("location"));

                                                                if (Build.VERSION.SDK_INT<23 ||  ActivityCompat.checkSelfPermission(RiderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)   {
                                                                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                                                        ParseGeoPoint userLocat = new ParseGeoPoint(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());


                                                                        double distM = driverLocat.distanceInMilesTo(userLocat);
                                                                        double distance = (double) Math.round(distM * 10) / 10;

                                                                                infoLocation.setText("Your Driver is " +distance+ " Miles away");

                                                                                ArrayList<Marker> markers = new ArrayList<>();
                                                                                LatLng driverLoc = new LatLng(driverLocat.getLatitude(),driverLocat.getLongitude());
                                                                                LatLng reqLoc = new LatLng(userLocat.getLatitude(), userLocat.getLongitude());

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

                                                                                handler.postDelayed(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {

                                                                                                checkForUpdates();

                                                                                        }
                                                                                },2000);




                                                                }


                                                        }

                                                }
                                        });


                                        callUberButton.setVisibility(View.INVISIBLE);

                                }




                        }
                });






        }


        public void updateMap(Location location) {

                if(driverActive==false){

                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.clear();

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
                        mMap.addMarker(new MarkerOptions().position(userLocation).title("Your location"));
                }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

                if (requestCode == 1) {


                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

                                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        updateMap(lastKnownLocation);
                                }
                        }
                }


        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_rider);


                infoLocation = (TextView)findViewById(R.id.infoLocation);
                callUberButton = (Button) findViewById(R.id.callUberButton);
                checkForUpdates();

                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
        }


        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {

                                updateMap(location);
                        }

                        @Override
                        public void onStatusChanged(String s, int i, Bundle bundle) {

                        }

                        @Override
                        public void onProviderEnabled(String s) {

                        }

                        @Override
                        public void onProviderDisabled(String s) {

                        }
                };



                if(Build.VERSION.SDK_INT<23){

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                }
                else{

                        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

                                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);

                        }
                        else{
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                if(lastKnownLocation!=null){

                                        updateMap(lastKnownLocation);

                                }
                        }
                }





        }

        public void callUber(View view){

                if(requestActive){

                        Toast.makeText(this,"Cancelled Uber",Toast.LENGTH_LONG).show();
                        callUberButton.setText("Call Uber");
                        requestActive=false;
                        //delete the request object from parse

                        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
                        query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                        query.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objects, ParseException e) {

                                        if(objects.size()>0){

                                                for (ParseObject object: objects){

                                                        object.deleteInBackground();

                                                }

                                        }


                                }
                        });


                }else{

                        Toast.makeText(this,"Booked Uber",Toast.LENGTH_LONG).show();


                        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                ParseObject object = new ParseObject("Requests");
                                object.put("username", ParseUser.getCurrentUser().getUsername());

                                ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());

                                object.put("location",parseGeoPoint);
                                object.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {

                                                 callUberButton = (Button)findViewById(R.id.callUberButton);
                                                callUberButton.setText("Cancel Uber");
                                                requestActive=true;

                                        }
                                });

                        }
                        else{

                                Toast.makeText(this,"Could not find location",Toast.LENGTH_LONG).show();

                        }

                }



        }


        public void logout(View view){


                ParseUser.logOut();

                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);

        }
}
