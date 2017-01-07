// IOnNewBookArrivedListener.aidl
package cn.mime.aidlservice_v2;

// Declare any non-default types here with import statements
import cn.mime.aidlservice_v2.Book;

interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}
