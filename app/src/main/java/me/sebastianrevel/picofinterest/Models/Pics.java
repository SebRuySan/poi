package me.sebastianrevel.picofinterest.Models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Pics")
public class Pics extends ParseObject {
    private static final String KEY_LOCATION = "location";
    private static final String KEY_PIC = "picture";
    private static final String KEY_USER = "user";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LONG = "long";


    public String getLocation() {
        return getString(KEY_LOCATION);
    }

    public void setLocation (String location) {
        put(KEY_LOCATION, location);
    }

    public double getLat() {
        return getDouble(KEY_LAT);
    }

    public void setLat(double latCoord) {
        put(KEY_LAT, latCoord);
    }

    public double getLong() {
        return getDouble(KEY_LONG);
    }

    public void setLong(double longCoord) {
        put(KEY_LONG, longCoord);
    }


    public ParseFile getPic() {
        return getParseFile(KEY_PIC);
    }

    public void setPic(ParseFile pic) {
        put(KEY_PIC, pic);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser (ParseUser user) {
        put(KEY_USER, user);
    }

    public static class Query extends ParseQuery<Pics> {
        public Query() {
            super(Pics.class);
        }

        public Query getTop() {
            setLimit(20);
            return this;
        }

        public Query withUser() {
            include("user");
            return this;
        }
    }


}
