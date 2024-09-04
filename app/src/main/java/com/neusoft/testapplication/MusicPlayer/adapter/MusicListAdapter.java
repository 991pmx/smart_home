package com.neusoft.testapplication.MusicPlayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.neusoft.testapplication.MusicPlayer.utils.MusicDatabaseHelper;
import com.neusoft.testapplication.R;

import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicViewHolder> {

    private final List<MusicDatabaseHelper.Music> musicList;
    private final OnMusicItemClickListener listener;

    public interface OnMusicItemClickListener {
        void onMusicItemClick(MusicDatabaseHelper.Music music);
    }

    public MusicListAdapter(List<MusicDatabaseHelper.Music> musicList, OnMusicItemClickListener listener) {
        this.musicList = musicList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item_view, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        MusicDatabaseHelper.Music music = musicList.get(position);
        holder.titleTextView.setText(music.getTitle());
        holder.artistTextView.setText(music.getArtist());

        holder.itemView.setOnClickListener(v -> listener.onMusicItemClick(music));
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView artistTextView;

        MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.music_item_title);
            artistTextView = itemView.findViewById(R.id.music_item_artist);
        }
    }
}
