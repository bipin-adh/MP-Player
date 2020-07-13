package com.example.mpplayer.activities.fragments;


import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mpplayer.R;
import com.example.mpplayer.adapters.ArtistListAdapter;
import com.example.mpplayer.customlisteners.ListItemClickListener;
import com.example.mpplayer.model.Artist;
import com.example.mpplayer.model.Audio;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistsFragment extends Fragment implements ListItemClickListener {
    private RecyclerView recyclerView;

    private ArrayList<Artist> artistList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks, container, false);

        recyclerView = view.findViewById(R.id.recyclerview);
        artistList = new ArrayList<>();
        if (getArguments() != null) {
            artistList = (ArrayList<Artist>) getArguments().get("ARTIST_LIST");
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initRecyclerView();

    }



    private void initRecyclerView() {
        if (artistList.size() > 0) {
            ArtistListAdapter adapter = new ArtistListAdapter(artistList, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

    }


    public ArrayList<Audio> getArtistTrack(String artistId) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        selection += " and artist_id = " + artistId;
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        ArrayList<Audio> audioList = new ArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Audio audio = new Audio(data, title, album, artist);
                audioList.add(audio);
            }
        }

        return audioList;
    }

    @Override
    public void onItemClick(int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("TRACK_LIST", getArtistTrack(artistList.get(position).getArtistId()));
        Navigation.findNavController(getView()).navigate(R.id.action_mainFragment_to_trackListViewFragment, bundle);
    }
}
