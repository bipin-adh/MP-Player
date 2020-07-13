package com.mp.mpplayer.activities.fragments;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.mpplayer.R;
import com.mp.mpplayer.adapters.AlbumListAdapter;
import com.mp.mpplayer.customlisteners.AudioListsProvider;
import com.mp.mpplayer.customlisteners.ListItemClickListener;
import com.mp.mpplayer.model.Album;
import com.mp.mpplayer.model.Audio;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumsFragment extends Fragment implements ListItemClickListener {
    private RecyclerView recyclerView;

    private ArrayList<Album> albumsList;

    private AudioListsProvider audioListsProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks, container, false);

        recyclerView = view.findViewById(R.id.recyclerview);
        albumsList = new ArrayList<>();
        if (getArguments() != null) {
            albumsList = (ArrayList<Album>) getArguments().get("ALBUM_LIST");
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initRecyclerView();

    }


    private void initRecyclerView() {
        if (albumsList.size() > 0) {
            AlbumListAdapter adapter = new AlbumListAdapter(albumsList, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        audioListsProvider = (AudioListsProvider) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        audioListsProvider = null;
    }

    @Override
    public void onItemClick(int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("TRACK_LIST", getAlbumTracks(albumsList.get(position).getAlbumId()));
        Navigation.findNavController(getView()).navigate(R.id.action_mainFragment_to_trackListViewFragment, bundle);
    }


    public ArrayList<Audio> getAlbumTracks(String albumId) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        selection += " and album_id = " + albumId;
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
}
