package com.example.administrator.download;

import java.io.InputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownUtil
{
    private String url_path;
    //下载地址
    private String target_path;
    //目标地址
    private int threadNum;
    //线程数目
    private DownThread[] threads;
    //线程对象
    private int fileSize;
    //文件大小

    public DownUtil(String url_path,String target_path,int threadNum)
    {
        this.url_path=url_path;
        this.target_path=target_path;
        this.threadNum=threadNum;
        threads=new DownThread[threadNum];
        //初始化
    }

    public void download() throws Exception
    {
        URL url=new URL(url_path);
        //传入下载地址
        HttpURLConnection connection=(HttpURLConnection) url.openConnection();
        //获取HttpURLConnection的实例
        connection.setRequestMethod("GET");
        //从服务器获取数据
        connection.setConnectTimeout(8000);
        //设置连接超时
        connection.setRequestProperty(
                "Accept",
                "image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
                        + "application/x-shockwave-flash, application/xaml+xml, "
                        + "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
                        + "application/x-ms-application, application/vnd.ms-excel, "
                        + "application/vnd.ms-powerpoint, application/msword, */*");
        connection.setRequestProperty("Accept-Language", "zh-CN");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Connection", "Keep-Alive");
        //补充设置
        fileSize=connection.getContentLength();
        //得到文件大小
        connection.disconnect();
        //关闭连接
        int currentPartSize=fileSize/threadNum+1;
        RandomAccessFile file=new RandomAccessFile(target_path,"rw");
        //读写目标地址文件
        file.setLength(fileSize);
        //设置本地文件的大小
        file.close();
        for(int i=0;i<threadNum;i++)
        {
            int startPos=i*currentPartSize;
            //计算每条线程的下载的开始位置
            RandomAccessFile currentPart=new RandomAccessFile(target_path,"rw");
            //每个线程使用一个RandomAccessFile进行下载
            currentPart.seek(startPos);
            //定位该线程的下载位置
            threads[i]=new DownThread(startPos,currentPartSize,currentPart);
            //创建下载线程
            threads[i].start();
            //启动下载线程
        }
    }

    public double getCompleteRate()
    {
        int sumSize = 0;
        //多条线程已经下载的大小
        for (int i=0;i<threadNum;i++)
        {
            sumSize+=threads[i].length;
        }
        return sumSize*1.0/fileSize;
        //返回下载的完成百分比
    }

    private class DownThread extends Thread
    {
        private int startPos;
        //当前线程的下载位置
        private int currentPartSize;
        //定义当前线程负责下载的文件大小
        private RandomAccessFile currentPart;
        //当前线程需要下载的文件块
        public int length;
        //定义已经该线程已下载的字节数
        public DownThread(int startPos,int currentPartSize,RandomAccessFile currentPart)
        {
            this.startPos=startPos;
            this.currentPartSize=currentPartSize;
            this.currentPart=currentPart;
        }

        @Override
        public void run()
        {
            try{
                URL url = new URL(url_path);
                HttpURLConnection connection=(HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setRequestProperty(
                        "Accept",
                        "image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
                                + "application/x-shockwave-flash, application/xaml+xml, "
                                + "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
                                + "application/x-ms-application, application/vnd.ms-excel, "
                                + "application/vnd.ms-powerpoint, application/msword, */*");
                connection.setRequestProperty("Accept-Language", "zh-CN");
                connection.setRequestProperty("Charset", "UTF-8");
                InputStream inputStream=connection.getInputStream();
                skipFully(inputStream,this.startPos);
                //该线程只下载自己负责的那部分文件
                byte[] buffer=new byte[1024];
                int hasRead=0;
                while(length<currentPartSize&&(hasRead=inputStream.read(buffer))>0)
                {
                    currentPart.write(buffer,0,hasRead);
                    length+=hasRead;
                }
                //写入目标文件
                currentPart.close();
                inputStream.close();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void skipFully(InputStream in,long bytes) throws IOException
    {
        long remainning=bytes;
        long len=0;
        while(remainning>0)
        {
            len=in.skip(remainning);
            remainning-=len;
        }
    }
}
