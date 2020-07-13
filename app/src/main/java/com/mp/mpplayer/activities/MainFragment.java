package com.example.mpplayer.activities;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.example.mpplayer.R;
import com.example.mpplayer.activities.fragments.AlbumsFragment;
import com.example.mpplayer.activities.fragments.ArtistsFragment;
import com.example.mpplayer.activities.fragments.TracksFragment;
import com.example.mpplayer.customlisteners.AudioListsProvider;
import com.example.mpplayer.model.Album;
import com.example.mpplayer.model.Artist;
import com.example.mpplayer.model.Audio;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;

    private ArrayList<Audio> audioList = new ArrayList<>();
    private ArrayList<Artist> artistList = new ArrayList<>();
    private ArrayList<Album> albumList = new ArrayList<>();

    private AudioListsProvider audioListsProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        tabLayout = view.findViewById(R.id.tablayout);
        viewPager = view.findViewById(R.id.viewpager);
        return view;
    }


    private void populateAudioList() {
        audioList = audioListsProvider.getAudioList();
        artistList = audioListsProvider.getArtistList();
        albumList = audioListsProvider.getAlbumList();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewPager();
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }


    private void initViewPager() {
        pagerAdapter = new PagerAdapter(getChildFragmentManager());
        TracksFragment tracksFragment = new TracksFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("AUDIO_LIST", audioList);
        tracksFragment.setArguments(bundle);

        AlbumsFragment albumsFragment = new AlbumsFragment();
        Bundle bundleAlbum = new Bundle();
        bundleAlbum.putSerializable("ALBUM_LIST", albumList);
        albumsFragment.setArguments(bundleAlbum);

        ArtistsFragment artistsFragment = new ArtistsFragment();
        Bundle bundleArtist = new Bundle();
        bundleArtist.putSerializable("ARTIST_LIST", artistList);
        artistsFragment.setArguments(bundleArtist);

        pagerAdapter.addFragment(tracksFragment, "Tracks");
        pagerAdapter.addFragment(albumsFragment, "Albums");
        pagerAdapter.addFragment(artistsFragment, "Artists");

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        audioListsProvider = (AudioListsProvider) context;
        populateAudioList();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        audioListsProvider = null;
    }
}
