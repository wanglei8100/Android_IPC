// IBookManager.aidl
package cn.mime.aidldemo.aidl;

// Declare any non-default types here with import statements
import cn.mime.aidldemo.aidl.Book;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
}
