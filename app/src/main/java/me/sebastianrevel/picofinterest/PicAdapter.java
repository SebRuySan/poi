package me.sebastianrevel.picofinterest;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class PicAdapter extends RecyclerView.Adapter <PicAdapter.RecyclerViewHolder> {

    ArrayList<String> arrayList = new ArrayList<>();

    public PicAdapter(ArrayList<String> arrayList) {
        this.arrayList = arrayList;
    }
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        RecyclerViewHolder viewHolder = new RecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder recyclerViewHolder, int i) {

        recyclerViewHolder.textView.setText(arrayList.get(i));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public RecyclerViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.tx_item);
        }
    }

}
//
//        import android.content.Context;
//        import android.support.v7.widget.RecyclerView;
//        import android.view.LayoutInflater;
//        import android.view.View;
//        import android.view.ViewGroup;
//        import android.widget.ImageView;
//        import android.widget.TextView;
//
//        import java.util.ArrayList;
//        import java.util.List;
//
//        import me.sebastianrevel.picofinterest.Models.Pics;
//
//public class PicAdapter extends RecyclerView.Adapter<PicAdapter.ViewHolder> {
//
//    private ArrayList<Pics> pics;
//    private Context context;
//
//    public PicAdapter(ArrayList<Pics> posts) {
//        this.pics = posts;
//    }
//    // creates and inflates a new view.
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        // receive context
//        context = parent.getContext();
//        LayoutInflater inflater = LayoutInflater.from(context);
//        View postView = inflater.inflate(R.layout.post_item, parent, false);
//        return new ViewHolder(postView);
//    }
//
//    // associates a view with items
//    @Override
//    public void onBindViewHolder(final ViewHolder holder, int position) {
//
//    }
//
//    // returns total number of items.
//    @Override
//    public int getItemCount() {
//        return pics.size();
//    }
//    public void clear() {
//        pics.clear();
//        notifyDataSetChanged();
//    }
//
//    // Add a list of items -- change to type used
//    public void addAll(List<Pics> list) {
//        pics.addAll(list);
//        notifyDataSetChanged();
//    }
//
//
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//
//        //  tracking view objects
//        ImageView ivPost;
//        TextView tvDescription;
//        TextView tvUser;
//        ImageView ivProfile;
//        TextView tvLikebar;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            ivPost = itemView.findViewById(R.id.post_iv);
//            tvDescription = itemView.findViewById(R.id.description_tv);
//            tvLikebar = itemView.findViewById(R.id.like_comment_tv);
//            tvUser = itemView.findViewById(R.id.profile_tv);
//            ivProfile = itemView.findViewById(R.id.profile_iv);
//        }
//    }
//}
