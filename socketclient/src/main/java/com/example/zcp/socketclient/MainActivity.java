package com.example.zcp.socketclient;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.example.zcp.socketclient.adapter.ChatAdapter;
import com.example.zcp.socketclient.bean.Transmission;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SOCKET_CONNECT = 0;
    private static final int SOCKET_NEW_MESSAGE = 1;
    private RecyclerView recyclerView;
    private EditText inputString;
    private Button send;
    private ChatAdapter mAdapter;
    private Socket mClientSocket;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what){
                case SOCKET_CONNECT:
                    progressBar.setVisibility(View.GONE);
                    break;
                case SOCKET_NEW_MESSAGE:
                    break;
            }
        }
    };
    private RelativeLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initDate();
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        inputString = (EditText) findViewById(R.id.et_content);
        send = (Button) findViewById(R.id.btn_send);
        progressBar = (RelativeLayout) findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter = new ChatAdapter(getDefaultData()));
    }

    private void initDate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectSocketServer();
            }
        }).start();
    }

    private void connectSocketServer() {
        Socket clientSocket =null;
        while (clientSocket==null){
            try {
                clientSocket = new Socket("localhost", 8866);
                mClientSocket = clientSocket;
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                mHandler.sendEmptyMessage(SOCKET_CONNECT);
                System.out.println("连接成功!");
            } catch (IOException e) {
                e.printStackTrace();
                SystemClock.sleep(1000);
                System.out.println("retry to connect...");
            }
        }

    }

    public List<Transmission> getDefaultData() {
        List<Transmission> datas = new ArrayList<>();

        Transmission trans = new Transmission();
        trans.itemType = Constants.CHAT_FROM;
        trans.transmissionType = Constants.TRANSFER_STR;
        trans.content = "昆仑";
        datas.add(trans);

        trans = new Transmission();
        trans.itemType = Constants.CHAT_SEND;
        trans.transmissionType = Constants.TRANSFER_STR;
        trans.content = "英雄志";
        datas.add(trans);

        return datas;
    }
}
