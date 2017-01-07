// IOnNewBookArrivedListener.aidl
package cn.mime.aidlservice_v3;

// Declare any non-default types here with import statements
import cn.mime.aidlservice_v3.Book;

interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}
