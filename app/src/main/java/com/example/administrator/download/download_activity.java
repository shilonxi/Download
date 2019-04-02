package com.example.administrator.download;

import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;

public class download_activity extends AppCompatActivity implements View.OnClickListener
{
    EditText url;
    EditText target;
    Button downBt;
    ProgressBar bar;
    DownUtil downUtil;
    private int mDownStatus;
    private MediaPlayer mediaPlayer=new MediaPlayer();
    //建立变量

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_activity_layout);
        url=(EditText)findViewById(R.id.url_edit);
        target=(EditText)findViewById(R.id.target_edit);
        downBt=(Button)findViewById(R.id.down_bt);
        bar=(ProgressBar)findViewById(R.id.bar);
        //获取实例
        final Handler handler=new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if(msg.what==0x123)
                {
                    bar.setProgress(mDownStatus);
                }
            }
        };

        downBt.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                downUtil=new DownUtil(url.getText().toString(),target.getText().toString(),7);
                //传入参数
                new Thread()
                {
                    @Override
                    public void run()
                    {
                        try{
                            downUtil.download();
                            //下载
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        // 定义每秒调度获取一次系统的完成进度
                        final Timer timer=new Timer();
                        timer.schedule(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                double completeRate=downUtil.getCompleteRate();
                                // 获取下载任务的完成比例
                                mDownStatus=(int)(completeRate*100);
                                // 发送消息通知界面更新进度条
                                handler.sendEmptyMessage(0x123);
                                // 下载完全后取消任务调度
                                if(mDownStatus>=100)
                                {
                                    timer.cancel();
                                    initMediaPlayer();
                                    //初始化
                                }
                            }
                        }, 0, 100);
                    }
                }.start();
            }
        });
        //点击监听

        Button play=(Button)findViewById(R.id.play_bt);
        Button pause=(Button)findViewById(R.id.pause_bt);
        Button stop=(Button)findViewById(R.id.stop_bt);
        //获取实例
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        //点击监听

        if(ContextCompat.checkSelfPermission(download_activity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(download_activity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE },1);
        }
        //运行时权限处理
    }

    private void initMediaPlayer()
    {
        try{
            File file=new File(target.getText().toString());
            mediaPlayer.setDataSource(file.getPath());
            //指定音频文件的路径
            mediaPlayer.prepare();
            //让MediaPlayer进入到准备状态
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults)
    {
        switch(requestCode)
        {
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    initMediaPlayer();
                }else
                {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.play_bt:
                if(!mediaPlayer.isPlaying())
                {
                    mediaPlayer.start();
                    //开始播放
                }
                break;
            case R.id.pause_bt:
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.pause();
                    //暂停播放
                }
                break;
            case R.id.stop_bt:
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.reset();
                    //停止播放
                    initMediaPlayer();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

}


