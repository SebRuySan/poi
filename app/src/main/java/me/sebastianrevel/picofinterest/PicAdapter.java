package me.sebastianrevel.picofinterest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseUser;

import com.parse.ParseException;
import java.util.ArrayList;

import me.sebastianrevel.picofinterest.Models.Pics;

public class PicAdapter extends RecyclerView.Adapter <PicAdapter.RecyclerViewHolder> {

    ArrayList<Pics> arrayList = new ArrayList<>();
    private Context context;

    public PicAdapter(ArrayList<Pics> arrayList) {
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
        // image load
        Pics pic = arrayList.get(i);
        ParseUser user = pic.getUser();

        // set text of the imageviews for each "pics" object with it's poster's username and userscore
        try {
           recyclerViewHolder.tvUsername.setText("@" + pic.getUser().fetchIfNeeded().getString("username"));
            recyclerViewHolder.tvUserScore.setText("User Score: " + pic.getUser().fetchIfNeeded().getNumber("UserScore"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // add image of this pics object to recycler view and display with glide
        ParseFile geoPic = pic.getPic();
        String url = geoPic.getUrl();
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
        TextView tvUsername;
        TextView tvUserScore;

        public RecyclerViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
            tvUsername = view.findViewById(R.id.tvUsername);
            tvUserScore = view.findViewById(R.id.tvUserScore);
        }
    }

}
