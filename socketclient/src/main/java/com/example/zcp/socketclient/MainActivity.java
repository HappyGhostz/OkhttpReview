package com.example.zcp.socketclient;

import android.content.Intent;
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
import com.example.zcp.socketclient.server.SocketServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
                    String time = formatDate(System.currentTimeMillis());
                    String message = (String) msg.obj;
                    Transmission transmission = new Transmission();
                    transmission.time =time;
                    transmission.content = message;
                    transmission.itemType = Constants.CHAT_FROM;
                    List<Transmission> defaultData = new ArrayList<>();
                    defaultData.add(transmission);
                    mAdapter.loadMoreComplete();
                    mAdapter.addData(defaultData);
//                    scollViewToShow(mAdapter.getItemCount()-1);
                    smootViewToShow(mAdapter.getItemCount()-1);
                    break;
            }
        }
    };
    private RelativeLayout progressBar;
    private PrintWriter out;
    private LinearLayoutManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(MainActivity.this,SocketServer.class));
        initView();
        initDate();
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        inputString = (EditText) findViewById(R.id.et_content);
        send = (Button) findViewById(R.id.btn_send);
        progressBar = (RelativeLayout) findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(mManager = new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter = new ChatAdapter(getDefaultData()));
    }

    private void initDate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectSocketServer();
            }
        }).start();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sendMessage = inputString.getText().toString().trim();
                String time = formatDate(System.currentTimeMillis());
                Transmission transmission = new Transmission();
                transmission.time =time;
                transmission.content = sendMessage;
                transmission.itemType = Constants.CHAT_SEND;
                transmission.showType = Constants.RECEIVE_MSG;
                List<Transmission> defaultData = new ArrayList<>();
                out.println(sendMessage);
                defaultData.add(transmission);
                mAdapter.loadMoreComplete();
                mAdapter.addData(defaultData);
//                scollViewToShow(mAdapter.getItemCount()-1);
                smootViewToShow(mAdapter.getItemCount()-1);
            }
        });
    }

    private void connectSocketServer() {
        Socket clientSocket =null;
        out = null;
        while (clientSocket==null){
            try {
                clientSocket = new Socket("localhost", 8866);
                mClientSocket = clientSocket;
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                mHandler.sendEmptyMessage(SOCKET_CONNECT);
                System.out.println("连接成功!");
            } catch (IOException e) {
                e.printStackTrace();
                SystemClock.sleep(1000);
                System.out.println("retry to connect...");
            }
        }
        BufferedReader reader=null;
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while (!MainActivity.this.isFinishing()){
                String message = reader.readLine();
                if(message!=null){
                    mHandler.obtainMessage(SOCKET_NEW_MESSAGE,message).sendToTarget();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if(reader!=null){
                    reader.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                if(out !=null){
                    out.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                if(clientSocket!=null){
                    clientSocket.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public List<Transmission> getDefaultData() {
        List<Transmission> datas = new ArrayList<>();
        String time = formatDate(System.currentTimeMillis());
        Transmission trans = new Transmission();
        trans.itemType = Constants.CHAT_FROM;
        trans.transmissionType = Constants.TRANSFER_STR;
        trans.content = "我是服务器";
        trans.time = time;
        datas.add(trans);

        trans = new Transmission();
        trans.itemType = Constants.CHAT_SEND;
        trans.transmissionType = Constants.TRANSFER_STR;
        trans.content = "我是客户端";
        trans.time = time;
        datas.add(trans);

        return datas;
    }
    private String formatDate(long time){
        return new SimpleDateFormat("HH:mm:ss").format(new Date(time));
    }

    @Override
    protected void onDestroy() {
        if(mClientSocket!=null){
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
    private void scollViewToShow(int position){
        int firstVisibleItemPosition = mManager.findFirstVisibleItemPosition();
        int lastVisibleItemPosition = mManager.findLastVisibleItemPosition();
        if(position<=firstVisibleItemPosition){
            recyclerView.scrollToPosition(position);
        }else if(position<=lastVisibleItemPosition){
            int top = recyclerView.getChildAt(position - firstVisibleItemPosition).getTop();
            recyclerView.scrollBy(0,top);
        }else {
            recyclerView.scrollToPosition(position);
        }
    }
    private void smootViewToShow(int position){
        int fristPosition = recyclerView.getChildLayoutPosition(recyclerView.getChildAt(0));
        int lastPosition = recyclerView.getChildLayoutPosition(recyclerView.getChildAt(recyclerView.getChildCount() - 1));
        if(position<=fristPosition){
            recyclerView.smoothScrollToPosition(position);
        }else if (position<=lastPosition){
            int movePosition = position - fristPosition;
            if(movePosition>=0&&movePosition<=recyclerView.getChildCount()-1){
                int top = recyclerView.getChildAt(movePosition).getTop();
                recyclerView.smoothScrollBy(0,top);
            }
        }else {
            // 如果要跳转的位置在最后可见项之后，则先调用smoothScrollToPosition将要跳转的位置滚动到可见位置
            // 再通过onScrollStateChanged控制再次调用smoothMoveToPosition，执行上一个判断中的方法
            recyclerView.smoothScrollToPosition(position);
//            mToPosition = position;
//            mShouldScroll = true;
        }
    }

}
