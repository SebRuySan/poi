package me.sebastianrevel.picofinterest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PicAdapter extends RecyclerView.Adapter <PicAdapter.RecyclerViewHolder> {

    ArrayList<String> arrayList = new ArrayList<>();
    private Context context;

    public PicAdapter(ArrayList<String> arrayList) {
        this.arrayList = arrayList;
    }
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        RecyclerViewHolder viewHolder = new RecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder recyclerViewHolder, int i) {
        String url = "https://scontent.xx.fbcdn.net/v/t31.0-8/23592190_1716598371726484_7973335275111326555_o.jpg?_nc_cat=0&_nc_log=1&oh=6ecc70fc3d6108180de16a84a4b36df7&oe=5BE6AF3F";
        // image load
        ImageView imageView = recyclerViewHolder.imageView;
        Glide.with(context)
                .load(url)
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public RecyclerViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
        }
    }

}
