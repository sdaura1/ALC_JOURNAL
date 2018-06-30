package com.sani.shaheed.y.model;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import com.sani.shaheed.y.R;

/**
 * Created by shaheed on 4/21/18.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    public List<Entry> mList;
    public Context context;

    public ListAdapter(List<Entry> mList, Context context) {

        this.mList = mList;
        this.context = context;

    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ListAdapter.ViewHolder holder, int position) {

        holder.the_title.setText(mList.get(position).getTitle());
        holder.content.setText(mList.get(position).getContents());
        holder.date.setText(mList.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView the_title, content, date;
        public ImageView picture;
        View theView;

        public ViewHolder(View itemView) {
            super(itemView);

            theView = itemView;

            the_title = theView.findViewById(R.id.entry_title);
            content = theView.findViewById(R.id.entry_content);
            date = theView.findViewById(R.id.entry_date);

            picture = theView.findViewById(R.id.entry_picture);
        }
    }
}