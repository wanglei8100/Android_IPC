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
public class User implements Parcelable {
    public int userId;
    public String userName;
    public boolean isMale;
    public Book userBook;

    public User(int userId, String userName, boolean isMale, Book userBook) {
        this.userId = userId;
        this.userName = userName;
        this.isMale = isMale;
        this.userBook = userBook;
    }

    protected User(Parcel in) {
        userId = in.readInt();
        userName = in.readString();
        isMale = in.readByte() != 0;
        userBook = in.readParcelable(Book.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeString(userName);
        dest.writeByte((byte) (isMale ? 1 : 0));
        dest.writeParcelable(userBook, flags);
    }
}
