package cn.mime.aidldemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import cn.mime.aidldemo.aidl.Book;
import cn.mime.aidldemo.aidl.IBookManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private IBookManager mRemoteService;

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
        intent.setAction("cn.mime.aidlservice.BookManagerService");
        intent.setPackage("cn.mime.aidlservice");
        intent.putExtra("first_book",new Book(99,"第一本书"));
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
    }

    public void callRemoteMethod1(View view){
        if (mRemoteService !=null){
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
        if (mRemoteService !=null){
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
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}
