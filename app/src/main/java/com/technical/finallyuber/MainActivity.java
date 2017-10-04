package com.technical.finallyuber;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends AppCompatActivity {

        Switch aswitch;
        String name;
        Intent intent;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);

                aswitch = (Switch)findViewById(R.id.switchButton);

                /*
                ParseObject object = new ParseObject("sdf");
                object.put("myNumber", "9835131231");
                object.put("myString", "Ashish Rawat");

                object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException ex) {
                                if (ex == null) {
                                        Log.i("Parse Result", " Successful!");
                                } else {
                                        Log.i("Parse Result", " Failed" + ex.toString());
                                }
                        }
                });

                */


                if(ParseUser.getCurrentUser()==null){

                   ParseAnonymousUtils.logIn(new LogInCallback() {
                           @Override
                           public void done(ParseUser user, ParseException e) {

                                   if(e==null){
                                           Log.i("Info","Anonymous Login Successful");
                                   }
                                   else{
                                           Log.i("Info","Anonymous Login Failed");
                                   }



                           }
                   });

                }
                else{

                        if(ParseUser.getCurrentUser().get("riderOrDriver")!=null){

                                Log.i("Info","Redirecting as "+ParseUser.getCurrentUser().get("riderOrDriver").toString());
                                redirectActivity();

                        }


                }

                ParseAnalytics.trackAppOpenedInBackground(getIntent());

        }



        public void loginButton(View view){


                String name = "rider";
                if(aswitch.isChecked()){
                        name="driver";
                }



                //Toast.makeText(this,"Logging In as "+name,Toast.LENGTH_LONG).show();

                ParseUser.getCurrentUser().put("riderOrDriver",name);
                ParseUser.getCurrentUser().saveInBackground();

                Log.i("Info","Redirecting as "+name);
                redirectActivity();


        }


        public void redirectActivity(){

                if(ParseUser.getCurrentUser().get("riderOrDriver").equals("rider")){

                        Intent intent = new Intent(this,RiderActivity.class);
                        startActivity(intent);

                }else{

                        Intent intent = new Intent(this,RequestsActivity.class);
                        startActivity(intent);


                }


        }


}
