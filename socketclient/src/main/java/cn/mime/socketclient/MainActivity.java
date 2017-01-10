package cn.mime.socketclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private Socket mClientSocket;
    private PrintWriter mPrintWriter;
    private EditText msgEdit;
    private TextView msgTxt;
    private Button sendBtn;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    Toast.makeText(MainActivity.this, "连接成功！", Toast.LENGTH_SHORT).show();
                    sendBtn.setEnabled(true);
                    break;
                case 1002:
                    msgTxt.setText(msgTxt.getText()+(String)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        msgEdit = (EditText) findViewById(R.id.send_msg_edit);
        msgTxt = (TextView) findViewById(R.id.msg_content_txt);
        sendBtn = (Button) findViewById(R.id.send_btn);
    }

    public void startConnection(View view) {
        new Thread() {
            @Override
            public void run() {
                connectServer();
            }
        }.start();
    }

    public void sendMsg(View view) {
        String msgContent = msgEdit.getText().toString();
        if (!TextUtils.isEmpty(msgContent)&&mPrintWriter!=null){
            mPrintWriter.println(msgContent);
            msgEdit.setText("");
            String appendString = "client : "+msgContent+"\n";
            msgTxt.setText(msgTxt.getText()+appendString);
        }
    }

    private void connectServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket("localhost", 8604);
                mClientSocket = socket;

                BufferedWriter responseBw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                mPrintWriter = new PrintWriter(responseBw,true);
                mHandler.sendEmptyMessage(1001);
            } catch (IOException e) {
                SystemClock.sleep(1000);
                e.printStackTrace();
            }
        }

        try {
            BufferedReader recieveBr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!MainActivity.this.isFinishing()){
                String msg = recieveBr.readLine();
                if (msg != null){
                    String appendString = "     service : "+msg+"\n";
                    mHandler.obtainMessage(1002,appendString).sendToTarget();
                }
            }
            mPrintWriter.close();
            recieveBr.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        if (mClientSocket!=null){
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
