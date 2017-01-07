// IBookManager.aidl
package cn.mime.aidlservice_v2;

// Declare any non-default types here with import statements
import cn.mime.aidlservice_v2.Book;
import cn.mime.aidlservice_v2.IOnNewBookArrivedListener;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
    void registerListener(IOnNewBookArrivedListener listener);
    void unregisterListener(IOnNewBookArrivedListener listener);
}
