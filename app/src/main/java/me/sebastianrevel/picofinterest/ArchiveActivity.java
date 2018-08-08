package me.sebastianrevel.picofinterest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import me.sebastianrevel.picofinterest.Models.Pics;

public class ArchiveActivity extends AppCompatActivity {
    GridLayoutManager gridView;
    Button globe;
    RecyclerView rv;
    static RecyclerView.Adapter adapter;

    static ArrayList<Pics> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);
        gridView = new GridLayoutManager(this, 3);
        rv = findViewById(R.id.geoRecycler);
        rv.setLayoutManager(gridView);
        adapter = new GridAdapter(arrayList);
        rv.setAdapter(adapter);
        loadArchivePics();
        globe = findViewById(R.id.return_globe_btn);
        globe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Log.e("GLOBE", "Button clicked");
            }
        });
    }

    public void loadArchivePics() {
        final ParseQuery<Pics> query = ParseQuery.getQuery(Pics.class).whereEqualTo("user", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Pics>() {
            @Override
            public void done(List<Pics> objects, ParseException e) {
                if (e == null) {
                    if (objects == null) {
                        Log.d("CreateFragment", "Objects is null!");
                    } else {
                        Log.d("CreateFragment", "Adding pics: " + objects.size());
                    }

                    arrayList.clear();
                    arrayList.addAll(objects);
                    adapter.notifyDataSetChanged();
                } else {
                    e.printStackTrace();
                }
            }
        });

    }
}

