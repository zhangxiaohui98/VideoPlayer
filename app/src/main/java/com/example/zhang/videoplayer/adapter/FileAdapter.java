package com.example.zhang.videoplayer.adapter;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhang.videoplayer.R;
import com.example.zhang.videoplayer.SelectFileActivity;
import com.example.zhang.videoplayer.SelectVideoActivity;
import com.example.zhang.videoplayer.utile.FileUtile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * 文件适配器
 * Created by zhang on 2019/4/25.
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    private static final String TAG = "FileAdapter";
    private List<File> mFileList;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView file_name;
        Button scanButton;

        public ViewHolder(View view){
            super(view);
            file_name=view.findViewById(R.id.file_name);
            scanButton=view.findViewById(R.id.scan_button);
        }
    }

    public FileAdapter(List<File> FileList, Context context){
        mFileList=FileList;
        mContext=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_item,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(ViewHolder holder,final int position) {
        //将text View文字设置成文件或文件夹名
        final File file=mFileList.get(position);
        holder.file_name.setText(file.getName());

        //点击text view进入子文件夹
        holder.file_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //检查下方是否有文件夹
                if(FileUtile.isAllFile(file)){
                    Toast.makeText(mContext,"已到达最后一级文件夹",Toast.LENGTH_SHORT).show();
                }else{
                    //选中的文件路径
                    Intent intent=new Intent(mContext,SelectFileActivity.class);
                    intent.putExtra("srcStr",file.getAbsolutePath());
                    mContext.startActivity(intent);
                    Log.d(TAG, "onClick: 进入文件夹："+file.getAbsolutePath());
                }

            }
        });
        holder.scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(0==FileUtile.selectVideo(file).size()){
                    Toast.makeText(mContext,"此文件夹下无视频文件",Toast.LENGTH_SHORT).show();
                }else {
                    //存储到【常用文件夹】
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("data", MODE_PRIVATE).edit();
                    Log.d(TAG, "选中文件的路径: " + file.getAbsolutePath());
                    editor.putString("selectFile", file.getAbsolutePath());
                    editor.apply();
                    Intent intent = new Intent(mContext, SelectVideoActivity.class);
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }
}
