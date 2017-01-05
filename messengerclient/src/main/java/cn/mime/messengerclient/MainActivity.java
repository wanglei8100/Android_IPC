package cn.mime.messengerclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView mMsgContentTxt;
    private EditText mMessageEdit;
    private StringBuffer mMsgContent;
    private Messenger mRemoteMessengerService;
    private Messenger mClientMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1025:
                    showReceiveMsgContent(msg.getData().getString("service_msg"));
                    break;
                default:
                    break;
            }
        }
    });

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected  connected success !");
            mRemoteMessengerService = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected  connected break ! try to connect again !");
            bindRemoteService();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMsgContentTxt = (TextView) findViewById(R.id.msg_content_txt);
        mMessageEdit = (EditText) findViewById(R.id.input_message_edit);
        mMsgContent = new StringBuffer();
    }

    public void bindService(View view) {
        bindRemoteService();
    }

    private void bindRemoteService() {
        Intent intent = new Intent();
        intent.setAction("cn.mime.messengerservice.MessengerService");
        intent.setPackage("cn.mime.messengerservice");
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    public void callRemoteMethod1(View view) {
        if (mRemoteMessengerService == null) {
            Toast.makeText(this, "请先绑定远程服务", Toast.LENGTH_SHORT).show();
            return;
        }
        String msg = showSendMsgContent();
        Message message = Message.obtain(null, 1024);
        Bundle data = new Bundle();
        data.putString("client_msg", msg);
        message.setData(data);
        message.replyTo = mClientMessenger;
        try {
            mRemoteMessengerService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private String showSendMsgContent() {
        String msg = mMessageEdit.getText().toString();
        mMsgContent.append("client msg: " + msg + "\n");
        mMsgContentTxt.setText(mMsgContent.toString());
        return msg;
    }

    private void showReceiveMsgContent(String serviceMsg) {
        mMsgContent.append("    service msg: " + serviceMsg + "\n");
        mMsgContentTxt.setText(mMsgContent.toString());
    }
}
