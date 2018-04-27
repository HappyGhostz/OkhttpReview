package com.example.zcp.socketclient.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * @author Zhao Chenping
 * @creat 2018/4/26.
 * @description
 */
public class SocketServer  extends Service{
    private  static boolean isDestory = false;
    private  static String[] reponse = new String[]{"hah","随机2","随机3","随机4","随机5","随机6","随机7"};
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        new Thread(new TcpServer()).start();
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        isDestory =true;
        super.onDestroy();
    }

    private static class TcpServer implements Runnable {

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                //监听本地8866端口（端口可以随便设置，但不要占用系统端口）
                serverSocket = new ServerSocket(8866);
                Log.e( "serverSocket: ","服务已开启!!!" );
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!isDestory){
                try {
                    final Socket client = serverSocket.accept();
                    Log.e( "serverSocketaccept: ","accept" );
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            responseClient(client);
                        }
                    }).start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        private void responseClient(Socket client) {
            BufferedReader bufferedReader = null;
            PrintWriter printWriter=null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                printWriter.println("已连接。。欢迎来到服务端");
                while (!isDestory){
                    String fromClient = bufferedReader.readLine();
                    if(fromClient==null){
                        break;
                    }
                    int i = new Random().nextInt(reponse.length);
                    String repons = reponse[i];
                    printWriter.println(repons);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(bufferedReader!=null){
                        bufferedReader.close();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                try {
                    if(printWriter!=null){
                        printWriter.close();
                    }
                }catch (Exception e1){
                    e1.printStackTrace();
                }
                try {
                    if(client!=null){
                        client.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
