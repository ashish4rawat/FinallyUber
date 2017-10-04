package com.technical.finallyuber;


import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

public class StarterApplication extends Application {

        @Override
        public void onCreate() {
                super.onCreate();

                // Enable Local Datastore.
                Parse.enableLocalDatastore(this);


                // Add your initialization code here
                Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                        .applicationId("3f84db09d9df5e74941e1851d0ff42406fe8052c")
                        .clientKey("840973155bc108a88cc976cb6e9d34202b3f70e4")
                        .server("http://52.66.24.91:80/parse/")
                        .build()
                );



                //ParseUser.enableAutomaticUser();

                ParseACL defaultACL = new ParseACL();
                defaultACL.setPublicReadAccess(true);
                defaultACL.setPublicWriteAccess(true);
                ParseACL.setDefaultACL(defaultACL, true);

        }
}

