package me.sebastianrevel.picofinterest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

import me.sebastianrevel.picofinterest.Models.Pics;

public class GridAdapter extends RecyclerView.Adapter <GridAdapter.RecyclerViewHolder> {
    ArrayList<Pics> arrayList;
    private Context context;
    public GridAdapter(ArrayList<Pics> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public GridAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.archive_item, parent, false);
        GridAdapter.RecyclerViewHolder viewHolder = new GridAdapter.RecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder recyclerViewHolder, int i) {
        recyclerViewHolder.getAdapterPosition();
        final Pics pic = arrayList.get(i);
        recyclerViewHolder.tvLocation.setText(pic.getLocation());
        //recyclerViewHolder.tvLikeCount.setText(String.valueOf(pic.getLike().size()));


        // add image of this pics object to recycler view and display with glide
        ParseFile geoPic = pic.getPic();
        String url = geoPic.getUrl();
        ImageView imageView = recyclerViewHolder.picView;
        Glide.with(context)
                .load(url)
                .into(imageView);
    }

    @Override
    public int getItemCount() {

        return arrayList.size();

    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView picView;
        TextView tvLocation;
   //     TextView tvLikeCount;

        public RecyclerViewHolder(View view) {
            super(view);
            picView = view.findViewById(R.id.picture_archive);
            tvLocation = view.findViewById(R.id.location_archive);
         //   tvLikeCount = view.findViewById(R.id.tvRadius);

        }
    }

}
