package com.example.mpplayer.activities.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mpplayer.R;
import com.example.mpplayer.activities.MainActivity;
import com.example.mpplayer.adapters.TrackListAdapter;
import com.example.mpplayer.customlisteners.ListItemClickListener;
import com.example.mpplayer.model.Audio;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrackListViewFragment extends Fragment implements ListItemClickListener {

    private RecyclerView recyclerView;
    private ArrayList<Audio> audioList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);
        audioList = new ArrayList<>();
        if (getArguments() != null) {
            audioList = (ArrayList<Audio>) getArguments().get("TRACK_LIST");
        }
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initRecyclerView();

    }


    private void initRecyclerView() {
        if (audioList.size() > 0) {
            TrackListAdapter adapter = new TrackListAdapter(audioList, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

    }


    @Override
    public void onItemClick(int position) {
        ((MainActivity)getActivity()).setNowPlaying(audioList,position);
    }
}
