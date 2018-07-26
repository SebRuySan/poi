package me.sebastianrevel.picofinterest;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import me.sebastianrevel.picofinterest.Models.Pics;

public class ParseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Pics.class);
        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("poi")
                .clientKey("POI2018!")
                .server("http://picofinterestv2.herokuapp.com/parse")
                .build();

        Parse.initialize(configuration);
    }
}
