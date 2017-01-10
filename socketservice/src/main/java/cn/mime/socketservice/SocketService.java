package cn.mime.socketservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static android.content.ContentValues.TAG;

public class SocketService extends Service {

    boolean isServiceDestroyed = false;

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //监听1024端口
                    ServerSocket serverSocket = new ServerSocket(8604);

                    while (!isServiceDestroyed){
                        //获取客户端socket连接
                        Socket client = serverSocket.accept();
                        //用于接收客户端消息
                        BufferedReader recieveBr = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        //用于回复收客户端消息
                        BufferedWriter responseBw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                        PrintWriter printWriter = new PrintWriter(responseBw,true);
                        while (!isServiceDestroyed){
                            String recieveMsg = recieveBr.readLine();
                            if (recieveMsg == null){
                                break;
                            }
                            Log.d(TAG,"recieveMsg : "+recieveMsg);
                            SystemClock.sleep(1000);
                            printWriter.println(recieveMsg+"--和你学的！");
                        }

                        recieveBr.close();
                        responseBw.close();
                        client.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        isServiceDestroyed = true;
        super.onDestroy();
    }
}
