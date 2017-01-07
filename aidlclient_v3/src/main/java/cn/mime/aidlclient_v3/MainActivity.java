package cn.mime.aidlclient_v3;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import cn.mime.aidlservice_v3.Book;
import cn.mime.aidlservice_v3.IBookManager;
import cn.mime.aidlservice_v3.IOnNewBookArrivedListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private IBookManager mRemoteService;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Book newBook = (Book) msg.obj;
            Toast.makeText(MainActivity.this,newBook.toString(),Toast.LENGTH_SHORT).show();
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"onServiceConnected  connected success !");
            mRemoteService = IBookManager.Stub.asInterface(service);
//            try {
//                service.linkToDeath(mDeathRecipient,0);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"onServiceDisconnected  connected break ! try to connect again !");
            bindRemoteService();
        }
    };

    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub(){

        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            Message message = Message.obtain();
            message.obj = newBook;
            mHandler.sendMessage(message);
        }
    };

    public void registerRemoteListener(View view){
        if (checkRemoteService()){
            try {
                mRemoteService.registerListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void unregisterRemoteListener(View view){
        unregisterRemoteListener();
    }

//    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
//        @Override
//        public void binderDied() {
//            Log.d(TAG,"mDeathRecipient binderDied() service death !");
//            if (mRemoteService!=null){
//                mRemoteService.asBinder().unlinkToDeath(mDeathRecipient,0);
//                mRemoteService = null;
//                bindRemoteService();
//            }
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void bindService(View view){
        bindRemoteService();
    }

    private void bindRemoteService() {
        Intent intent = new Intent();
        intent.setAction("cn.mime.aidlservice_v3.BookManagerService");
        intent.setPackage("cn.mime.aidlservice_v3");
        intent.putExtra("first_book",new Book(99,"第一本书"));
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
    }

    public void callRemoteMethod1(View view){
        if (checkRemoteService()){
            try {
                Book book = new Book(100, "独孤九剑");
                mRemoteService.addBook(book);
                Toast.makeText(this,book.toString(),Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void callRemoteMethod2(View view){
        if (checkRemoteService()){
            try {
                List<Book> bookList = mRemoteService.getBookList();
                Toast.makeText(this,bookList.toString(),Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterRemoteListener();
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    private void unregisterRemoteListener() {
        if (checkRemoteService()){
            try {
                mRemoteService.unregisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkRemoteService() {
        return mRemoteService !=null&&mRemoteService.asBinder().isBinderAlive();
    }
}
