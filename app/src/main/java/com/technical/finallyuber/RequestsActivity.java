package com.technical.finallyuber;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends AppCompatActivity {


        ArrayList<String> requests = new ArrayList<>();
        ArrayList<Double> requestLatitute = new ArrayList<>();
        ArrayList<Double> requestLongitute = new ArrayList<>();
        ArrayList<String> usernames = new ArrayList<>();

        ListView requestsListView;
        ArrayAdapter adapter;
        LocationManager locationManager;
        LocationListener locationListener;


        public void updateListView(Location location) {

                Log.i("Infoas", "changed");


                if (location != null) {

                        requests.clear();

                        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");

                        final ParseGeoPoint parseGeoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
                        query.whereNear("location", parseGeoPoint);
                        query.setLimit(10);
                        query.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objects, ParseException e) {


                                        if (objects.size() > 0 && e == null) {

                                                for (ParseObject object : objects) {

                                                        double distM = parseGeoPoint.distanceInMilesTo((ParseGeoPoint) object.get("location"));
                                                        double distance = (double) Math.round(distM * 10) / 10;


                                                        requests.add(String.valueOf(distance) + " Miles");
                                                        requestLatitute.add(((ParseGeoPoint) object.get("location")).getLatitude());
                                                        requestLongitute.add(((ParseGeoPoint) object.get("location")).getLongitude());
                                                        usernames.add(object.get("username").toString());

                                                }


                                        } else {

                                                requests.add("No active requests nearby");

                                        }

                                        adapter.notifyDataSetChanged();


                                }
                        });

                } else {

                        Toast.makeText(this, "Can't find locaton", Toast.LENGTH_LONG).show();


                }
        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

                if (requestCode == 1) {


                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, locationListener);
                                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        updateListView(lastKnownLocation);
                                        Log.i("InfoAs", "Location changed by onRequestmethod");
                                }
                        }
                }


        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_requests);


                requestsListView = (ListView) findViewById(R.id.requestsListView);

                adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, requests);
                requestsListView.setAdapter(adapter);


                locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {

                                updateListView(location);
                                ParseUser.getCurrentUser().put("location",new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
                                ParseUser.getCurrentUser().saveInBackground();

                                Log.i("InfoAs", "Location changed by real listener");
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


                if (Build.VERSION.SDK_INT < 23) {

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                } else {

                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                        } else {
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                               /*
                                if(lastKnownLocation!=null){

                                        updateListView(lastKnownLocation);
                                        Log.i("InfoAs","Location changed by last portion");

                                }
                                */
                        }
                }


                requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                                if (Build.VERSION.SDK_INT<23 || ( ActivityCompat.checkSelfPermission(RequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RequestsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)  ) {
                                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                        Intent intent = new Intent(getApplicationContext(),DriverLocationActivity.class);

                                        intent.putExtra("requestLatitude",requestLatitute.get(i));
                                        intent.putExtra("requestLongitude",requestLongitute.get(i));
                                        intent.putExtra("driverLatitude",lastKnownLocation.getLatitude());
                                        intent.putExtra("driverLongitude",lastKnownLocation.getLongitude());
                                        intent.putExtra("username",usernames.get(i));

                                        startActivity(intent);
                                }

                        }
                });

        }

}
