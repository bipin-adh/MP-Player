package com.mp.mpplayer.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mp.mpplayer.R;
import com.mp.mpplayer.customlisteners.ListItemClickListener;
import com.mp.mpplayer.model.Artist;


import java.util.Collections;
import java.util.List;

public class ArtistListAdapter extends RecyclerView.Adapter<ArtistListAdapter.ViewHolder> {

    List<Artist> artistList = Collections.emptyList();
    ListItemClickListener listItemClickListener;

    public ArtistListAdapter(List<Artist> artistList, ListItemClickListener listItemClickListener) {
        this.artistList = artistList;
        this.listItemClickListener = listItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Inflate the layout , initialize the view holder
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song, viewGroup, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
//Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        viewHolder.tvSongName.setText(artistList.get(position).getArtist());
        viewHolder.tvArtistName.setText(artistList.get(position).getAlbumCount());

        viewHolder.trackHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listItemClickListener.onItemClick(position);
            }
        });


    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return artistList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout constraintLayout;
        TextView tvSongName;
        TextView tvArtistName;
        ImageButton songMenuBtn;
        ImageView ivSongCoverArt;
        View trackHolder;

        public ViewHolder(View itemView) {
            super(itemView);
            tvSongName = itemView.findViewById(R.id.tv_song_name);
            tvArtistName = itemView.findViewById(R.id.tv_artist_name);
            songMenuBtn = itemView.findViewById(R.id.bt_song_menu);
            constraintLayout = itemView.findViewById(R.id.cl_song_item);
            ivSongCoverArt = itemView.findViewById(R.id.iv_song_coverart);
            trackHolder = itemView.findViewById(R.id.track_holder);

        }
    }
}
