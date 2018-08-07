package me.sebastianrevel.picofinterest;

import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import java.util.List;

import me.sebastianrevel.picofinterest.Models.Pics;

public class PicAdapter extends RecyclerView.Adapter<PicAdapter.RecyclerViewHolder> {


    ArrayList<Pics> arrayList = new ArrayList<>();
    Context context;
    Boolean expanded = false;
    static Dialog d;
    static boolean following;

    public PicAdapter(ArrayList<Pics> arrayList) {
        this.arrayList = arrayList;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int i) {

        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        RecyclerViewHolder viewHolder = new RecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder recyclerViewHolder, int i) {
        recyclerViewHolder.getAdapterPosition();
        // image load
        final Pics pic = arrayList.get(i);

        // set text of the imageviews for each "pics" object with it's poster's username and userscore
        try {
            recyclerViewHolder.tvUsername.setText( /*"@" + */ pic.getUser().fetchIfNeeded().getString("username"));
            recyclerViewHolder.tvLikeCount.setText(String.valueOf(pic.getLike().size()));
            recyclerViewHolder.tvCreatedAt.setText(pic.getDate().toString());
            recyclerViewHolder.tvDesc.setText(pic.getDesc());
        } catch (NullPointerException e) {

        } catch (ParseException e) {
            e.printStackTrace();
        }

        final String currentUserId = ParseUser.getCurrentUser().getObjectId();
        final String currentUsername = ParseUser.getCurrentUser().getUsername();

        final Boolean liked = pic.getLike().contains(currentUserId);

        if (liked) {
            Log.e("LIKED", "ALREADY LIKED");
            recyclerViewHolder.btnLike.setBackgroundResource(R.drawable.ic_star_on);
        } else {
            Log.e("LIKED", "NOT");
            recyclerViewHolder.btnLike.setBackgroundResource(R.drawable.ic_star_off);
        }

        recyclerViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d = new Dialog(context);

                d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                d.setContentView(R.layout.expanded_layout);

                ImageView imageView = d.findViewById(R.id.expanded_pic);
                final Button ivFollow = d.findViewById(R.id.ivFollow);
                final TextView userView = d.findViewById(R.id.expanded_user);
                TextView descView = d.findViewById(R.id.expanded_desc);

                final String picUsername = pic.getUser().getUsername();

                userView.setText(String.valueOf(picUsername));
                descView.setText(pic.getDesc());

                List<String> followingList = ParseUser.getCurrentUser().getList("following");

                if (followingList != null) {
                    following = followingList.contains(picUsername);
                }

                if (following) {
                    Log.e("FOLLOWING", "ALREADY FOLLOWING");
                    ivFollow.setBackgroundResource(R.drawable.following_icon_trans);
                } else {
                    Log.e("FOLLOWING", "NOT");
                    ivFollow.setBackgroundResource(R.drawable.follow_icon_trans);
                }

                ivFollow.setVisibility(View.GONE);

                if (!currentUsername.equals(picUsername)) {
                    ivFollow.setVisibility(View.VISIBLE);

                    ivFollow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            List<String> followingList = ParseUser.getCurrentUser().getList("following");

                            if (followingList != null) {
                                following = followingList.contains(picUsername);
                            }

                            if (following) {
                                ivFollow.setBackgroundResource(R.drawable.follow_icon_trans);

                                pic.deleteFollower(picUsername);
                                following = false;

                                Toast.makeText(context, "No longer following", Toast.LENGTH_SHORT).show();
                            } else {
                                ivFollow.setBackgroundResource(R.drawable.following_icon_trans);

                                pic.addFollower(picUsername);
                                following = true;

                                Toast.makeText(context, "followed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                ParseFile geoPic = pic.getPic();
                String url = geoPic.getUrl();

                Glide.with(context)
                        .load(url)
                        .into(imageView);

                d.show();

            }
        });


        recyclerViewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (liked) {
                    recyclerViewHolder.btnLike.setBackgroundResource(R.drawable.ic_star_off);
                    pic.deleteLike(currentUserId);
                    Toast.makeText(context, "No longer liked", Toast.LENGTH_SHORT).show();
                    recyclerViewHolder.tvLikeCount.setText(String.valueOf(pic.getLike().size()));
                    pic.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            if (e == null) { // no errors

                                Log.e("LIKES", "UPDATED LIKES");

                            } else {

                                Log.e("LIKES", "FAILED UPDATING");

                                e.printStackTrace();
                            }
                        }
                    });
                    notifyDataSetChanged();
                } else {
                    recyclerViewHolder.btnLike.setBackgroundResource(R.drawable.ic_star_on);
                    pic.addLike(currentUserId);
                    Toast.makeText(context, "liked", Toast.LENGTH_SHORT).show();
                    recyclerViewHolder.tvLikeCount.setText(String.valueOf(pic.getLike().size()));
                    pic.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            if (e == null) { // no errors

                                Log.e("LIKES", "UPDATED LIKES");

                            } else {

                                Log.e("LIKES", "FAILED UPDATING");

                                e.printStackTrace();
                            }
                        }
                    });
                    notifyDataSetChanged();
                }
            }
        });


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
        TextView tvLikeCount;
        TextView tvCreatedAt;
        TextView tvDesc;
        Button btnLike;

        public RecyclerViewHolder(View view) {
            super(view);
            tvDesc = view.findViewById(R.id.card_desc);
            imageView = view.findViewById(R.id.imageView);
            tvUsername = view.findViewById(R.id.tvUsername);
            btnLike = view.findViewById(R.id.like_btn);
            tvLikeCount = view.findViewById(R.id.likeCounter);
            tvCreatedAt = view.findViewById(R.id.tvCreatedAt);
        }
    }
}
