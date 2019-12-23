package com.example.mymusic.Adapter;

import android.content.Context;
import android.net.sip.SipSession;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusic.Model.Song;
import com.example.mymusic.R;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private Context mContext;
    private ArrayList<Song> mSongList;
    private RecyclerItemClickListener mListener;
    private int selectedPosition=-1;

    public SongAdapter(Context context, ArrayList<Song> songList, RecyclerItemClickListener listener) {
        mContext = context;
        mSongList = songList;
        mListener = listener;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {

        private ImageView cover;
        private TextView title, artist, duration;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = (ImageView) itemView.findViewById(R.id.cover);
            title = (TextView) itemView.findViewById(R.id.title);
            artist = (TextView) itemView.findViewById(R.id.artist);
            duration = (TextView) itemView.findViewById(R.id.duration);
        }

        public void bind(Song song, final RecyclerItemClickListener listener){
            itemView.setOnClickListener(v -> listener.onClickListener(song, getLayoutPosition()));
        }
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song,parent,false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = mSongList.get(position);
        if (song!=null){
            holder.title.setText(song.getTitle());
            holder.artist.setText(song.getArtist()+" - "+song.getAlbum());
            holder.duration.setText(song.getDuratonString());
            holder.cover.setImageURI(song.getCoverUri());
            holder.bind(song, mListener);
        }
    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    public interface RecyclerItemClickListener{
        void onClickListener(Song song,int position);
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}
