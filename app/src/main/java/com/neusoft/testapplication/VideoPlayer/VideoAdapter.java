package com.neusoft.testapplication.VideoPlayer;

import static com.neusoft.testapplication.utils.playbackup.formatDuration;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.neusoft.testapplication.R;
import com.neusoft.testapplication.VideoPlayer.ROOM.Entity.Video;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private static final String TAG = "Video";
    private static Context context;
    private static AdapterListener listener;
    List<Video> videos;
    VideoPlayerManager videomanager;

    public VideoAdapter(Context context, List<Video> videoList) {
        VideoAdapter.context = context;
        this.videos = videoList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 创建布局文件的视图
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.videolist, parent, false);

        videomanager = new VideoPlayerManager();


        // 创建ViewHolder并返回
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Video video = videos.get(position);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(video.getData()); // 设置视频文件的路径
            // 获取视频的某一帧作为缩略图，这里以第一帧为例
            Bitmap thumbnail = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            // 现在你可以将thumbnail设置到ImageView中了
            holder.thumbnail.setImageBitmap(thumbnail);
        } catch (IllegalArgumentException ex) {
            // 处理异常
        } finally {
            try {
                retriever.release(); // 释放资源
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        holder.title.setText(video.getTitle());
        holder.author.setText(video.getArtist());

        // 转换总时长（毫秒）为更易读的格式（如00:00:00）
        String durationFormatted = formatDuration(video.getDuration());
        holder.duration.setText(durationFormatted);

        holder.bookmark.setMax(20);
        holder.bookmark.setProgress((int) (video.getBookmark()*20/video.getDuration()));


    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    //删除item
    public void deleteItem(int position) {
        listener.dbdelete(position);
        videos.remove(position);
        notifyItemRemoved(position);
    }

    //交换item
    public void changeItem(int srcPosition, int dstPosition) {
        // 交换list中两个item位置
        listener.dbswap(srcPosition, dstPosition);
        Collections.swap(videos, srcPosition, dstPosition);
        notifyItemMoved(srcPosition, dstPosition);
    }

    public void setAdapterListener(AdapterListener listener) {
        VideoAdapter.listener = listener;
    }


    public interface AdapterListener {
        void play(int i);
        void dbdelete(int id);

        void dbswap( int srcPosition, int dstPosition);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        TextView author;
        TextView duration;
        SeekBar bookmark;

        public ViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.title);
            author = itemView.findViewById(R.id.author);
            duration = itemView.findViewById(R.id.duration);
            bookmark = itemView.findViewById(R.id.bookmark);

            title.setSingleLine(true); // 确保是单行
            title.setEllipsize(TextUtils.TruncateAt.MARQUEE); // 设置省略号位置
            title.setMarqueeRepeatLimit(-1); // 设置滚动次数，-1为无限次
            title.setSelected(true); // 必须设置为选中状态才能滚动
            title.setHorizontallyScrolling(true); // 设置水平滚动


            itemView.setOnClickListener(v -> {
                // 如果需要，可以在这里处理点击事件，但通常最好通过接口回调到Activity/Fragment
                // 这里我们假设点击监听器是通过接口传递的，所以不在这里直接处理
                listener.play(click());
            });
        }

        private int click() {
            return getAdapterPosition();
        }


    }


}
