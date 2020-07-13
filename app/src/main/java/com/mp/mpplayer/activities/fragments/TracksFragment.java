package com.mp.mpplayer.activities.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.mpplayer.R;

import com.mp.mpplayer.activities.MainActivity;
import com.mp.mpplayer.adapters.TrackListAdapter;
import com.mp.mpplayer.customlisteners.ListItemClickListener;
import com.mp.mpplayer.model.Audio;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class TracksFragment extends Fragment implements ListItemClickListener {
    private RecyclerView recyclerView;

    private ArrayList<Audio> audioList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks, container, false);

        recyclerView = view.findViewById(R.id.recyclerview);
        audioList = new ArrayList<>();
        if (getArguments() != null) {
            audioList = (ArrayList<Audio>) getArguments().get("AUDIO_LIST");
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
//        ((MainActivity) getActivity()).setNowPlaying(audioList, position);
    }
}
