package com.example.zhang.videoplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.zhang.videoplayer.R;
import com.example.zhang.videoplayer.VideoActivity;
import com.example.zhang.videoplayer.utile.FileUtile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhang on 2019/4/29.
 */

public class VedioAdapter extends RecyclerView.Adapter<VedioAdapter.ViewHolder> {
    private static final String TAG = "VedioAdapter";
    private List<File> mFileList;
    private Context mContext;
    private Toast toast;
    protected boolean isScrolling = false;

    public void setScrolling(boolean scrolling) {
        isScrolling = scrolling;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView videoImg;
        TextView videoName;
        TextView videoDuration;
        TextView videoSize;


        public ViewHolder(View view){
            super(view);
            videoImg=view.findViewById(R.id.video_img);
            videoName=view.findViewById(R.id.video_name);
            videoSize=view.findViewById(R.id.video_size);
            videoDuration=view.findViewById(R.id.video_duration);
        }
    }

    public VedioAdapter(List<File> FileList, Context context){
        mFileList=FileList;
        mContext=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_item,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //将text View文字设置成文件或文件夹名
        final File file=mFileList.get(position);
        if(!isScrolling){
            holder.videoImg.setImageBitmap(FileUtile.getVideoPhoto(file.getAbsolutePath()));
        }
        //holder.videoImg.setImageBitmap(FileUtile.getVideoPhoto(file.getAbsolutePath()));
        holder.videoName.setText(file.getName());
        holder.videoSize.setText("文件："+file.length()/(1024*1024)+"MB");
        holder.videoDuration.setText("时长："+FileUtile.getVideoDuration(file.getAbsolutePath())+"分钟");


        //点击text view展示文件名
        holder.videoName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toast.makeText(mContext,file.getName(),Toast.LENGTH_SHORT).show();
            }
        });
        //点击videoImg播放视频
        holder.videoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext,VideoActivity.class);
                //intent.putExtra("videoURI",file.getAbsolutePath());
                intent.putExtra("FilePosition",position);
                mContext.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }
}
