package cn.mime.multipleprocessstudy.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <p>write the description
 *
 * @author wangshan
 * @version 2.0.0
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class Book implements Parcelable {

    public String bookName;
    public float bookPrice;

    public Book(String bookName, float bookPrice) {
        this.bookName = bookName;
        this.bookPrice = bookPrice;
    }

    protected Book(Parcel in) {
        bookName = in.readString();
        bookPrice = in.readFloat();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookName);
        dest.writeFloat(bookPrice);
    }
}
