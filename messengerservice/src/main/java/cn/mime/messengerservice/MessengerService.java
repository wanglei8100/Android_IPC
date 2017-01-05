package cn.mime.messengerservice;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class MessengerService extends Service {
    private Handler mHandle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1024:
                    String clientMsg = msg.getData().getString("client_msg");
                    Log.d(TAG,"handleMessage() receive client msg : "+clientMsg);
                    Messenger replyTo = msg.replyTo;
                    if (replyTo != null){
                        Message replyMessage = Message.obtain(null,1025);
                        Bundle replyData = new Bundle();
                        if (TextUtils.isEmpty(clientMsg)){
                            replyData.putString("service_msg","你好，我是服务端妹子,请说人话！");
                        }else {
                            replyData.putString("service_msg","你讲的啥？我听不懂啊。。。");
                        }
                        replyMessage.setData(replyData);
                        try {
                            SystemClock.sleep(1000);
                            replyTo.send(replyMessage);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private Messenger mMessenger = new Messenger(mHandle);

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
