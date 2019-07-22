package com.example.mpplayer.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mpplayer.model.Audio;
import com.example.mpplayer.R;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    List<Audio> songList = Collections.emptyList();
    Context context;

    public RecyclerViewAdapter(List<Audio> songList, Context context) {

        this.songList = songList;
        this.context = context;
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
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
//Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        viewHolder.tvSongName.setText(songList.get(position).getTitle());
        viewHolder.tvArtistName.setText(songList.get(position).getArtist());
    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return songList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

//    class ViewHolder extends RecyclerView.ViewHolder {
//
//        TextView title;
//        ImageView play_pause;
//
//        ViewHolder(View itemView) {
//            super(itemView);
//            title = (TextView) itemView.findViewById(R.id.title);
//            play_pause = (ImageView) itemView.findViewById(R.id.play_pause);
//        }
//
//    }
class ViewHolder extends RecyclerView.ViewHolder {

    ConstraintLayout constraintLayout;
    TextView tvSongName;
    TextView tvArtistName;
    ImageButton songMenuBtn;
    ImageView ivSongCoverArt;
    public ViewHolder(View itemView) {
        super(itemView);
        tvSongName = itemView.findViewById(R.id.tv_song_name);
        tvArtistName=itemView.findViewById(R.id.tv_artist_name);
        songMenuBtn=itemView.findViewById(R.id.bt_song_menu);
        constraintLayout=itemView.findViewById(R.id.cl_song_item);
        ivSongCoverArt=itemView.findViewById(R.id.iv_song_coverart);
    }
}
}
