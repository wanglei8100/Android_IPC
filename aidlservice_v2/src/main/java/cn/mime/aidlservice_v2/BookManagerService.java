package cn.mime.aidlservice_v2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BookManagerService extends Service {
    private static final String TAG = "BookManagerService";
    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(true);
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();

    private RemoteCallbackList mRemoteListenerList = new RemoteCallbackList();

    private IBookManager.Stub mBookManager = new IBookManager.Stub() {
        @Override
        public List<Book> getBookList() throws RemoteException {
            Log.d(TAG, "mBookManager getBookList() called mBookList : " +
                    mBookList.toString());
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            Log.d(TAG, "mBookManager addBook() called book : " +
                    book.toString());
            if (!checkBookExist(book)) {
                mBookList.add(book);
            }
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            Log.d(TAG,"registerListener listener : "+listener.toString());

            mRemoteListenerList.register(listener);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            Log.d(TAG,"unregisterListener listener : "+listener.toString());

            mRemoteListenerList.unregister(listener);
        }
    };

    private boolean checkBookExist(Book book) {
        boolean isExist = false;
        for (int i = 0; i < mBookList.size(); i++) {
            if (book.bookId == mBookList.get(i).bookId && book.bookName.equals(mBookList.get(i).bookName)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsServiceDestroyed.get()) {
                    onNewBookArrived(new Book(mBookList.size() + 1, "武侠小说" + (mBookList.size() + 1)));
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void onNewBookArrived(Book book) {
        mBookList.add(book);
        int broadcastNumber = mRemoteListenerList.beginBroadcast();
        for (int i = 0; i < broadcastNumber; i++) {
            IOnNewBookArrivedListener listener = (IOnNewBookArrivedListener) mRemoteListenerList.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onNewBookArrived(book);
                    Log.d(TAG,"onNewBookArrived book : "+book.toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mRemoteListenerList.finishBroadcast();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Book firstBook = intent.getParcelableExtra("first_book");
        mBookList.add(firstBook);
        return mBookManager;
    }

    @Override
    public void onDestroy() {
        mIsServiceDestroyed.set(false);
        super.onDestroy();
    }
}
