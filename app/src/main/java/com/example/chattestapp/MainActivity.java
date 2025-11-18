package com.example.chattestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chattestapp.chatbo.BOMessage;
import com.example.chattestapp.chatbo.BOUser;
import com.google.gson.Gson;

import java.net.URISyntaxException;
import java.util.Calendar;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private Socket mSocket;
    {
        try {
            IO.Options opt = new IO.Options();
            opt.secure = true;
            opt.transports  =new String[]{"websocket"};

            //mSocket = IO.socket("http://10.0.2.2:8000",opt);
            mSocket = IO.socket("http://192.168.86.248:8000",opt);
            //mSocket = IO.socket("http://cht.marvy.tech:8000",opt);
            //mSocket = IO.socket("http://99.62.189.116:8000",opt);

        } catch (URISyntaxException e) {
            Log.e("socket", String.valueOf(e.getStackTrace()));
        }
    }

    Button btn_send;
    EditText et_message;
    String user_name = "clark@kent.com";
    String room_name = "21d22a2b81b744c3aaae55a76a0789ad";
    String property_uuid = "afosdfaj-adsf-asdf-a354";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSocket.connect();

        et_message = (EditText)findViewById(R.id.ET_message);
        btn_send = (Button)findViewById(R.id.BT_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BOMessage boMessage = new BOMessage();
                boMessage.messageContent = et_message.getText().toString();
                boMessage.messageDate = android.text.format.DateFormat.format("yyyyMMddHHmmssz", Calendar.getInstance().getTime()).toString();
                boMessage.roomId = room_name;
                boMessage.userId = user_name;
                SendMessage(boMessage);
                et_message.getText().clear();
            }
        });
        SetSocketConnection();

    }


    private void SetSocketConnection(){
        mSocket.on("user_id",on_user_id);
        //mSocket.on("user_in", on_user_in);
        //mSocket.on("user_out", on_user_out);
        mSocket.on("socket_error", on_socket_error);

        mSocket.on("receive_message",on_receive_message);

        if (mSocket.connected()){

        }else{
            mSocket.connect();
        }
        BOUser boUser = new BOUser();
        boUser.userId = user_name;
        boUser.roomId = room_name;
        JoinChatRoom(boUser);
    }

    public void JoinChatRoom(BOUser boUser){
        Gson gson = new Gson();
        try{
            String json = gson.toJson(boUser);
            Log.e("join_chat",json);
            Object args[] = new Object[]{json};

            mSocket.emit("join_chat",args);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void SendMessage(BOMessage boMessage){
        Gson gson = new Gson();
        try{
            String json = gson.toJson(boMessage);
            Log.e("chat",json);
            Object args[] = new Object[]{json,boMessage.roomId};

            mSocket.emit("send_message",args);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    Emitter.Listener on_receive_message = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(args.length == 1){
                        Log.d("run",args[0].toString());
                        Toast.makeText(MainActivity.this,args[0].toString(),Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    };

    Emitter.Listener on_user_id = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(args.length == 1){
                        Log.d("user_id",args[0].toString());
                    }
                }
            });
        }
    };

    Emitter.Listener on_socket_error = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(args.length == 1){
                        Log.d("socket_error",args[0].toString());
                    }
                }
            });
        }
    };
}