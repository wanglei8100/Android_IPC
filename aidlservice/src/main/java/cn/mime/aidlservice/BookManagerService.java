package cn.mime.aidlservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.mime.aidldemo.aidl.Book;
import cn.mime.aidldemo.aidl.IBookManager;

public class BookManagerService extends Service {
    private static final String TAG = "BookManagerService";
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();

    private IBookManager.Stub mBookManager = new IBookManager.Stub() {
        @Override
        public List<Book> getBookList() throws RemoteException {
            Log.d(TAG,"mBookManager getBookList() called mBookList : " +
                    mBookList.toString());
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            Log.d(TAG,"mBookManager addBook() called book : " +
                    book.toString());
            if(!checkBookExist(book)){
                mBookList.add(book);
            }
        }
    };

    private boolean checkBookExist(Book book) {
        boolean isExist = false;
        for (int i = 0; i < mBookList.size(); i++) {
            if (book.bookId == mBookList.get(i).bookId&&book.bookName.equals(mBookList.get(i).bookName)){
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Book firstBook = intent.getParcelableExtra("first_book");
        mBookList.add(firstBook);
        return mBookManager;
    }
}
