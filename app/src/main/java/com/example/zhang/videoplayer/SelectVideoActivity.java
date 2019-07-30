package com.example.zhang.videoplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.zhang.videoplayer.adapter.VedioAdapter;
import com.example.zhang.videoplayer.utile.DividerDecoration;
import com.example.zhang.videoplayer.utile.FileUtile;
import com.example.zhang.videoplayer.utile.TimedOff;

import java.io.File;
import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

public class SelectVideoActivity extends AppCompatActivity {
    private static final String TAG = "SelectVideoActivity";
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_video);
        //获取权限
        FileUtile.checkPermission(TAG,this,this);

        List<File> listFile;
        //获取以前选择的文件路径下所有视频文件
        SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);

        if(null==pref.getString("selectFile",null)){
            Toast.makeText(this,"请先选择需扫描的文件",Toast.LENGTH_LONG).show();
            Intent intent=new Intent(SelectVideoActivity.this,SelectFileActivity.class);
            startActivity(intent);

        }else{
            File src=new File(pref.getString("selectFile",null));
            //扫描根目录下的文件夹
            listFile= FileUtile.selectVideo(src);

            //放入RecyclerView
            recyclerView=(RecyclerView)findViewById(R.id.sel_video_directory);
            LinearLayoutManager layoutManager=new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            //为recycle view的item添加下划线
            recyclerView.addItemDecoration(new DividerDecoration(this));
            final VedioAdapter vedioAdapter=new VedioAdapter(listFile, this);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == SCROLL_STATE_IDLE) { // 滚动静止时才加载图片资源，极大提升流畅度
                        vedioAdapter.setScrolling(false);
                        vedioAdapter.notifyDataSetChanged(); // notify调用后onBindViewHolder会响应调用
                    } else{
                        vedioAdapter.setScrolling(true);
                    }
                }
            });
            recyclerView.setAdapter(vedioAdapter);
        }


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