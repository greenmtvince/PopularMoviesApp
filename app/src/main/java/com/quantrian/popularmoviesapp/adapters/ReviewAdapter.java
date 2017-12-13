package com.quantrian.popularmoviesapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quantrian.popularmoviesapp.R;
import com.quantrian.popularmoviesapp.models.Review;
import com.quantrian.popularmoviesapp.models.Trailer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Vinnie on 12/10/2017.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder>{
    private ArrayList<Review> reviews;
    private Context context;

    private PosterAdapter.OnItemClickListener listener;
    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(PosterAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public ReviewAdapter(Context context, ArrayList<Review> reviews){
        this.reviews = reviews;
        this.context = context;
    }

    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.review_layout;
        LayoutInflater inflater = LayoutInflater.from(context);

        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Picasso.with(context).load(trailers.get(position).trailerThumbURL).into(holder.imageView);
        holder.tv_author.setText(reviews.get(position).author);
        holder.tv_content.setText(reviews.get(position).content);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_content;
        private TextView tv_author;

        public ViewHolder(View view) {
            super(view);
            tv_content = view.findViewById(R.id.tv_review_content);
            tv_author = view.findViewById(R.id.tv_review_author);

            //Get the position of our click.
            /*view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });*/
        }
    }
}
