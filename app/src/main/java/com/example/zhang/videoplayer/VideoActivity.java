package com.example.zhang.videoplayer;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhang.videoplayer.utile.FileUtile;
import com.example.zhang.videoplayer.utile.TimedOff;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.R.drawable.ic_media_pause;
import static android.R.drawable.ic_media_play;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener,SurfaceHolder.Callback,MediaPlayer.OnPreparedListener,SeekBar.OnSeekBarChangeListener{
    private static final String TAG = "VideoActivity";
    private LinearLayout controlView;
    private LinearLayout controlView2;
    private LinearLayout controlView3;
    private TextView startTime,countTime,setting,up,speed,down,videoName;
    private SeekBar seekBar;
    private ImageView backward,previous,play,next,forward;
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private SurfaceHolder surfaceHolder;
    private String path="";
    //扫描目录下的文件夹
    private List<File> listFile=new ArrayList<>();
    private TimedOff timeOff=new TimedOff();
    private int FilePosition;
    private float [] speedFlot=new float[]{0.5f,0.75f,1.0f,1.25f,1.5f,1.75f,2.0f,2.25f,2.5f,3.0f};
    private int speedTemp=2;
    //控制台是否可见
    private boolean isControlVis=true;
    //视频是否被播放
    private boolean isPlay = false;
    //是否销毁activity
    private boolean isOnDestroy = false;
    //是否正在拖动seekBar
    private boolean isSetProgress = false;
    //是否是第一次加载视频
    private boolean isFirstLoadVideo = true;
    //是否播放完毕后继续播放下一个
    private boolean isGoing = true;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isOnDestroy = true;
        //saveTime();
        if(mediaPlayer!=null){
            if(isPlay){
                mediaPlayer.stop();//停止播放
            }
            mediaPlayer.release();//释放资源
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mediaPlayer!=null){
            mediaPlayer.pause();//暂停此视频
            isPlay=false;//表示视频处于暂停状态
            play.setImageResource(ic_media_play);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mediaPlayer!=null){
            mediaPlayer.start(); //播放此视频
            isPlay=true;//表示视频处于播放状态
            play.setImageResource(ic_media_pause);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (!mKeyguardManager.inKeyguardRestrictedInputMode()) {//屏幕不锁屏时才运行
            // keyguard on
            //接受父活动的指示，选择文件进行播放
            Intent intent=getIntent();
            FilePosition=intent.getIntExtra("FilePosition",-1);
            String src=getSharedPreferences("data",MODE_PRIVATE).getString("selectFile",null);
            Log.d(TAG, "onCreate: src:"+src);
            if((src!=null)&&(FilePosition>=0)){
                listFile=FileUtile.selectVideo(new File(src));
                if(listFile!=null){
                    path= listFile.get(FilePosition).getAbsolutePath();
                }
            }else {
                Log.d(TAG, "onCreate:继续播放上次视频");
                path=readFile();
            }

            //获取权限
            FileUtile.checkPermission(TAG,this,this);
            //初始化窗口为横屏全屏
            initWindow();
            //初始化控件
            initView();
            //初始化SurfaceView和MediaPlayer
            initSurface();
            //初始化监听器
            initListener();
        }



    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backward:
                //后退十秒
                if(mediaPlayer != null){
                    int position = mediaPlayer.getCurrentPosition();
                    if(position > 5000){
                        position-=5000;
                    }else{
                        position = 0;
                    }
                    mediaPlayer.seekTo(position);
                }
                break;
            case R.id.forward:
                //快进十秒
                if(mediaPlayer != null) {
                    int position = mediaPlayer.getCurrentPosition();
                    mediaPlayer.seekTo(position + 15000);
                }
                break;
            case R.id.previous:
                //播放上一个
                playPrevious();
                break;
            case R.id.play:
                if(!isPlay){
                    mediaPlayer.start(); //播放此视频
                    isPlay=true;//表示视频处于播放状态
                    play.setImageResource(ic_media_pause);
                }else {
                    mediaPlayer.pause();//暂停此视频
                    isPlay=false;//表示视频处于暂停状态
                    play.setImageResource(ic_media_play);
                }
                break;
            case R.id.next:
                //播放下一个
                playNext();
                break;
            case R.id.setting:
                //Toast.makeText(this,"设置",Toast.LENGTH_SHORT).show();
                timedOffDialog();
                break;
            case R.id.up:
                //Toast.makeText(this,"加速",Toast.LENGTH_SHORT).show();
                if(speedTemp+1<speedFlot.length){
                    speed.setText(""+speedFlot[++speedTemp]);
                    setPlayerSpeed(speedFlot[speedTemp]);
                }else {
                    Toast.makeText(VideoActivity.this,"已经为最高速度",Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.down:
                //Toast.makeText(this,"减速",Toast.LENGTH_SHORT).show();
                if(speedTemp-1>=0){
                    speed.setText(""+speedFlot[--speedTemp]);
                    setPlayerSpeed(speedFlot[speedTemp]);
                }else {
                    Toast.makeText(VideoActivity.this,"已经为最低速度",Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //SurfaceCreated 创建成功才可以播放视频
        play();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    //准备完成
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //设置最大进度
        seekBar.setMax(mediaPlayer.getDuration());
        //设置视频最大时间
        countTime.setText(formatTime(mediaPlayer.getDuration()));
        videoName.setText( new File(path).getName());
        //开启线程更新进度
        speed.setText(""+speedFlot[speedTemp]);
        updateSeekBar();
        //更改状态
        if (isFirstLoadVideo)
            isFirstLoadVideo = false;

    }
    //进度改变
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        startTime.setText(formatTime(seekBar.getProgress()));
        if (isSetProgress) {
            Log.e("TAG", "onProgressChanged:refreshControlLayout");
        }
    }

    //开始拖动
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        startTime.setText(formatTime(seekBar.getProgress()));
        isSetProgress = true;
    }

    //停止拖动
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isSetProgress = false;
        if (isFirstLoadVideo) {
            return;
        }
        mediaPlayer.seekTo(seekBar.getProgress());
        startTime.setText(formatTime(seekBar.getProgress()));
    }

    /**
     * 初始化SurfaceView和MediaPlayer
     */
    public void initSurface(){
        surfaceHolder=surfaceView.getHolder();//获取surfaceHolder
        mediaPlayer=new MediaPlayer();//创建一个MediaPlay对象
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置多媒体类型
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Toast.makeText(VideoActivity.this,"视频播放完毕！",Toast.LENGTH_SHORT).show();
                if(isGoing){
                    playNext();
                }else {
                    timeOff.setTime(0);
                    new Thread(timeOff).start();
                }
            }
        });
    }

    /**
     * 播放视频
     */
    public void play(){
        mediaPlayer.reset();//重置MediaPlayer
        mediaPlayer.setDisplay(surfaceHolder);//把视频画面输出到surfaceView
        try {
            mediaPlayer.setDataSource(path);//设置要播放的视频
            mediaPlayer.prepare();//预加载
        } catch (IOException e) {
            Log.d(TAG, "play: 文件未找到!!!!!!!!!!!!!");
            e.printStackTrace();
        }
        mediaPlayer.start(); //播放视频
        //继续之前的进度
        if((readTime()!=0)&&(readFile().equals(path))){
            mediaPlayer.seekTo(readTime());
        }
        isPlay=true;
    }

    /**
     * 初始化窗口为横屏全屏
     */
    public void initWindow(){
        //**隐藏系统状态栏**//
        Window window = getWindow();
        //定义全屏参数
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        //设置为横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //不息屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //**隐藏系统状态栏**//
    }

    /**
     * 初始化控件
     */
    public void initView(){
        controlView =(LinearLayout)findViewById(R.id.control_view);
        controlView2 =(LinearLayout)findViewById(R.id.control_view2);
        controlView3 =(LinearLayout)findViewById(R.id.control_view3);
        startTime=(TextView)findViewById(R.id.start_time);
        countTime=(TextView)findViewById(R.id.count_time);
        setting =(TextView)findViewById(R.id.setting);
        up=(TextView)findViewById(R.id.up);
        speed=(TextView)findViewById(R.id.speed);
        down=(TextView)findViewById(R.id.down);
        seekBar=(SeekBar) findViewById(R.id.seek_bar);
        backward=(ImageView)findViewById(R.id.backward);
        previous=(ImageView)findViewById(R.id.previous);
        play =(ImageView)findViewById(R.id.play);
        next=(ImageView)findViewById(R.id.next);
        forward=(ImageView)findViewById(R.id.forward);
        surfaceView=(SurfaceView)findViewById(R.id.surface_view);
        videoName=(TextView)findViewById(R.id.video_name);
    }

    /**
     * 初始化监听器
     */
    public void initListener(){
        backward.setOnClickListener(this);
        previous.setOnClickListener(this);
        play.setOnClickListener(this);
        next.setOnClickListener(this);
        forward.setOnClickListener(this);
        surfaceHolder.addCallback(this);
        mediaPlayer.setOnPreparedListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        setting.setOnClickListener(this);
        up.setOnClickListener(this);
        down.setOnClickListener(this);

        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        refreshControl();
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 刷新控制台是否显示
     */
    public  void refreshControl(){
        if(isControlVis){
            controlView.setVisibility(View.INVISIBLE);
            controlView2.setVisibility(View.INVISIBLE);
            controlView3.setVisibility(View.INVISIBLE);
        }else {
            controlView.setVisibility(View.VISIBLE);
            controlView2.setVisibility(View.VISIBLE);
            controlView3.setVisibility(View.VISIBLE);
        }
        isControlVis=!isControlVis;
    }


    /**
     * 时间格式化
     * @param time
     * @return
     */
    private String formatTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
        return format.format(time);
    }

    /**
     * 更新进度
     */
    private void updateSeekBar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isOnDestroy) { //结束线程标示
                    if (isPlay) {
                        try {
                            int time = mediaPlayer.getCurrentPosition();
                            saveTime(time);
                            startTime.setText(formatTime(time));
                            seekBar.setProgress(time);
                            Log.e("TAG", "while");
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * /保存上次播放的进度
     */
    public void saveTime(int time){
        SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
        editor.putString("file",path);//文件
        editor.putInt("time",time);//进度
        editor.apply();
    }

    /**
     * 读取上次播放进度
     * @return
     */
    public int readTime(){
        SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);
        return pref.getInt("time",0);
    }

    /**
     * 读取文件
     * @return
     */
    public String readFile(){
        SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);
        return pref.getString("file",null);
    }

    /**
     * 修改播放速度
     * @param speed
     */
    private void setPlayerSpeed(float speed){
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.M) {
            PlaybackParams playbackParams = mediaPlayer.getPlaybackParams();
            playbackParams .setSpeed(speed);
            mediaPlayer.setPlaybackParams(playbackParams);
        }
    }

    /**
     * 定时关闭选项框
     */
    int yourChoice;
    private void timedOffDialog(){
        final String[] items = {"不开启","播完当前","10分钟","15分钟","30分钟","60分钟"};
        yourChoice = -1;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(VideoActivity.this);
        singleChoiceDialog.setTitle("定时关闭");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yourChoice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (yourChoice>0) {
                            Toast.makeText(VideoActivity.this, items[yourChoice]+"后关闭视频",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(VideoActivity.this,"不开启定时关闭功能",
                                    Toast.LENGTH_SHORT).show();
                            isGoing=true;
                        }
                        switch (yourChoice){
                            case 1:
                                isGoing=false;
                                break;
                            case 2:
                                timeOff.setTime(10);
                                new Thread(timeOff).start();
                                break;
                            case 3:
                                timeOff.setTime(15);
                                new Thread(timeOff).start();
                                break;
                            case 4:
                                timeOff.setTime(30);
                                new Thread(timeOff).start();
                                break;
                            case 5:
                                timeOff.setTime(60);
                                new Thread(timeOff).start();
                                break;
                        }

                    }
                });
        singleChoiceDialog.show();
    }

    /**
     * 播放上一个
     */
    public  void playPrevious(){
        Intent intent=new Intent(this,VideoActivity.class);
        if (0==FilePosition){
            //intent.getIntExtra("FilePosition",listFile.size()-0);
            intent.putExtra("FilePosition",listFile.size()-1);
        }else {
            //intent.getIntExtra("FilePosition",FilePosition--);
            intent.putExtra("FilePosition",FilePosition-1);
        }
        startActivity(intent);
        finish();
    }

    /**
     * 播放下一个
     */
    public  void playNext(){
        Intent intent=new Intent(this,VideoActivity.class);
        if (listFile.size()==(FilePosition+1)){
            intent.putExtra("FilePosition",0);
        }else {
            intent.putExtra("FilePosition",FilePosition+1);
        }
        startActivity(intent);
        finish();
    }

}
