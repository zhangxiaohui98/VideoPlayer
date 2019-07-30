package com.example.zhang.videoplayer;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.zhang.videoplayer.adapter.FileAdapter;
import com.example.zhang.videoplayer.utile.DividerDecoration;
import com.example.zhang.videoplayer.utile.FileUtile;
import com.example.zhang.videoplayer.utile.TimedOff;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectFileActivity extends AppCompatActivity {
    private static final String TAG = "SelectFileActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);

        //获取权限
        FileUtile.checkPermission(TAG,this,this);
        File src;

        List<File> listFile=new ArrayList<>();
        //接收上一个活动传来的数据
        Intent intent=getIntent();
        String srcStr=intent.getStringExtra("srcStr");
        if (null==srcStr){
            src=new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            Log.d(TAG, "onClick: 进入文件夹失败");
        }else{
            src=new File(srcStr);
            Log.d(TAG, "onClick: 成功进入文件夹："+srcStr);
        }

        //扫描目录下的文件夹
        listFile= FileUtile.scanFile(src,listFile);

        //放入RecyclerView
        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.sel_directory);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //为recycle view的item添加下划线
        recyclerView.addItemDecoration(new DividerDecoration(this));
        FileAdapter fileAdapter=new FileAdapter(listFile,this);
        recyclerView.setAdapter(fileAdapter);
    }

    /**
     * 菜单
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sel_file:
                Intent intent1=new Intent(this,SelectFileActivity.class);
                startActivity(intent1);
                break;
            case R.id.sel_video:
                Intent intent2=new Intent(this,SelectVideoActivity.class);
                startActivity(intent2);
                break;
            case R.id.proceed:
                try{
                    Intent intent=new Intent(this,VideoActivity.class);
                    startActivity(intent);
                }catch (Exception e){
                    Toast.makeText(this,"视频播放异常",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.set:
                Toast.makeText(this,"一键退出",Toast.LENGTH_SHORT).show();
                TimedOff timeOff=new TimedOff();
                timeOff.setTime(0);
                new Thread(timeOff).start();
                break;
        }
        return true;
    }

}
