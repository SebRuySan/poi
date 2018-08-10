package me.sebastianrevel.picofinterest.Models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.sebastianrevel.picofinterest.MainActivity;

@ParseClassName("Pics")
public class Pics extends ParseObject {
    private static final String KEY_LOCATION = "location";
    private static final String KEY_PIC = "picture";
    private static final String KEY_USER = "user";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LONG = "long";
    private static final String KEY_LIKE = "liked";
    private static final String KEY_NUM_LIKES = "number_of_likes";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_FOLLOWING = "following";
    private static final String KEY_CREATEDAT2 = "createdAt2";

//RuyG
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

    public String getDesc() {return getString(KEY_DESCRIPTION);}

    public void setDesc(String desc) {put(KEY_DESCRIPTION, desc);}

//    public int[] getDate() {
//        // create an array of size 3 that will represent the date
//        // the date will essentially be in the format {MM, DD, YYYY}
//        int[] date = new int[3];
//
//        date[0] = getCreatedAt().getMonth(); // months start at 0, not 1
//        date[1] = getCreatedAt().getDate();
//        date[2] = getCreatedAt().getYear();
//
//        return date;
//    }

    public Date getCreatedDate() {
        return getDate(KEY_CREATEDAT2);
    }

    public List<String> getFollowers() {
        Log.d("PICS", "In getFollowers");

        List<String> followingList = ParseUser.getCurrentUser().getList(KEY_FOLLOWING);

        if (followingList != null) {
            Log.e("SIZE", String.valueOf(followingList.size()));
            return followingList;
        } else {
            return Collections.emptyList();
        }
    }

    public void addFollower(String username) {
        ParseUser user = ParseUser.getCurrentUser();

        List<String> followersList = getFollowers();
        List<String> concatList = new ArrayList<>();
        List<String> singleList = new ArrayList<>();

        singleList.add(username);
        concatList.addAll(singleList);
        concatList.addAll(followersList);

        Log.d("Pics", "Added Follower");

        //  List<String> newList = likeList.add(username);
        user.put(KEY_FOLLOWING, concatList);

        try {
            MainActivity.setScore();
        } catch (ParseException exception) {
            exception.printStackTrace();
        }

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if (e == null) { // no errors

                    Log.e("FOLLOWING", "UPDATED FOLLOWING");

                } else {

                    Log.e("FOLLOWING", "FAILED UPDATING");

                    e.printStackTrace();
                }
            }
        });
    }

    public void deleteFollower(String username) {
        ParseUser user = ParseUser.getCurrentUser();

        Log.e("Pics", "Deleted Follower");
        List<String> followList = getFollowers();

        if (followList != null) {
            followList.remove(username);
            Log.e("FOLLOWDELETE", username);

            user.put(KEY_FOLLOWING, followList);
            Log.e("DELETESIZE", String.valueOf(followList.size()));
        } else {
            followList = Collections.emptyList();
            user.put(KEY_FOLLOWING, followList);
        }

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if (e == null) { // no errors

                    Log.e("FOLLOWING", "UPDATED FOLLOWING");

                } else {

                    Log.e("FOLLOWING", "FAILED UPDATING");

                    e.printStackTrace();
                }
            }
        });
    }

    public List<String> getLike() {
//        if (getList(KEY_LIKE) == null) {
//            List<String> emptyList = Collections.emptyList();
//            return emptyList;
//        } else {
            Log.d("PICS", "In getLike");
            List<String> likedList = getList(KEY_LIKE);
            Log.e("SIZE", String.valueOf(likedList.size()));
           // Log.e("VALUE", String.valueOf(likedList.get(0)));
            return likedList;
        // }
    }

    public void setLike() {
        put(KEY_LIKE, Collections.emptyList());
    }

    public void addLike(String username) {
        List<String> likeList = getLike();
        List<String> concatList = new ArrayList<>();
        List<String> singleList = new ArrayList<>();
        singleList.add(username);
        concatList.addAll(singleList);
        concatList.addAll(likeList);
        Log.d("Pics", "Added Like");
      //  List<String> newList = likeList.add(username);
        put(KEY_LIKE, concatList);
        put(KEY_NUM_LIKES, concatList.size());

        try {
            MainActivity.setScore();
        } catch (ParseException exception) {
            exception.printStackTrace();
        }
    }

    //            for (String userList : likeList) {
//                if (userList == username) {
//
//                }
//            }

    public void deleteLike(String username) {
        Log.e("Pics", "Deleted Like");
        List<String> likeList = getList(KEY_LIKE);
        if (likeList != null) {
            likeList.remove(username);
            Log.e("USERDELETE", username);
            put(KEY_LIKE, likeList);
            put(KEY_NUM_LIKES, likeList.size());
            Log.e("DELETESIZE", String.valueOf(likeList.size()));
        } else {
            likeList = Collections.emptyList();
            put(KEY_LIKE, likeList);
            put(KEY_NUM_LIKES, likeList.size());
        }
    }

    // update the number_of_likes
    public void setNumLikes() {
        List<String> likes = getLike();
        put(KEY_NUM_LIKES, likes.size());
    }

    // initialize the number_of_like column for a new pic with 0
    public void setNumLikeColumn() {
        put(KEY_NUM_LIKES, 0);
    }

    public void setCreatedAt() {
        Date currentTime = Calendar.getInstance().getTime();

        put(KEY_CREATEDAT2, currentTime);
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
