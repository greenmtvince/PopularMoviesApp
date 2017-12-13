package com.quantrian.popularmoviesapp.adapters;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quantrian.popularmoviesapp.R;
import com.quantrian.popularmoviesapp.models.Movie;
import com.quantrian.popularmoviesapp.models.Trailer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Vinnie on 12/9/2017.
 */

public class TrailerAdapter  extends RecyclerView.Adapter<TrailerAdapter.ViewHolder>{
    private ArrayList trailers;
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

    public TrailerAdapter(Context context, ArrayList trailers){
        this.trailers = trailers;
        this.context = context;
    }

    @Override
    public TrailerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.trailer_layout;
        LayoutInflater inflater = LayoutInflater.from(context);

        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ArrayList<Trailer> trailerList = trailers;
        Picasso.with(context).load(trailerList.get(position).trailerThumbURL).into(holder.imageView);
        holder.textView.setText(trailerList.get(position).name);
    }

    @Override
    public int getItemCount() {
        return trailers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView textView;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.iv_Trailer);
            textView = view.findViewById(R.id.tv_TrailerTitle);

            //Get the position of our click.
            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });
        }
    }
}
