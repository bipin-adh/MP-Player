package com.example.mpplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    List<Audio> list = Collections.emptyList();
    Context context;

    public RecyclerViewAdapter(List<Audio> list, Context context) {

        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Inflate the layout , initialize the view holder
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_layout, viewGroup, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
//Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        viewHolder.title.setText(list.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView play_pause;

        ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            play_pause = (ImageView) itemView.findViewById(R.id.play_pause);
        }

    }
}
