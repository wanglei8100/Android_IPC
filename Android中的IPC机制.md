# IPC机制知识梳理 #

## 写在前面 ##

本篇博客大部分是对Android中IPC知识的一个梳理，博客内容编写逻辑参考了任玉刚前辈的《Android开发艺术探索》一书中第二章节，博客中的demo源码是基于android studio 2.2.3所写，本内容中有部分源码分析，适合具有较好android基础知识的开发人员阅读。此外，因个人能力有限，如有错误遗漏之处欢迎指正。

## 简介 ##

IPC是Inter-Process Communication的缩写，含义为进程间通信或跨进程通信，是指两个进程之间进程数据交换的过程。 
 
进程和线程的解释：  
在Android系统中进程是指一个应用，一个进程可以包含多个线程，也可以只有一个线程即UI线程也叫主线程其主要功能是更新界面元素，相应的子线程中一般是用来执行大量耗时任务。

Linux上可以通过命名管道、共享内存、信号量等来实现进程间通信，Android虽然是基于Linux内核的移动操作系统，但是他的进程间通信方式与其并不完全相同，Android有自己的进程通信方式，如可以轻松实现进程间通信的Binder和不仅可以实现同一个设备两个应用进程间通信还可以实现两个终端之间的通信的Socket。

进程间通信应用场景：  

1. 某些模块由于特殊原因需要运行在单独进程中  
2. 为了加大一个应用可使用的内存，才有多进程来获取多份内存空间  
3. 当前应用需要向其他应用获取数据

## 多进程模式 ##

先来热热身，看下同一个应用开启多进程模式的方式及运行机制。

### 1. 开启方式 ###

manifest文件中配置4大组件的process属性，默认进程名称是当前应用包名。  
命名方式：“ 包名:进程名称 ”——私有进程（包名可省略），“ 包名.进程名称 ”——全局进程。

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity android:name=".SecondActivity"
            android:process=":secondProcessName"/>
        <activity android:name=".ThirdActivity"
            android:process="cn.mime.multipleprocessstudy.thirdProcessName"/>
    </application>

除了可以在Studio的Monitor选项卡中看到多个进程运行，还可以通过命令查看：adb shell ——> ps | grep (应用包名)

    C:\Users\wangshan>adb shell
	shell@angler:/ $ ps | grep cn.mime.multipleprocessstudy
	u0_a302   744   530   1497292 75148 SyS_epoll_ 0000000000 S cn.mime.multipleprocessstudy
	u0_a302   1471  530   1477016 64480 SyS_epoll_ 0000000000 S cn.mime.multipleprocessstudy:secondProcessName
	u0_a302   1571  530   1477016 64568 SyS_epoll_ 0000000000 S cn.mime.multipleprocessstudy.thirdProcessName

### 2. 运行机制 ###

多进程的开启方式固然简单，但是带来各种各样的问题，问题的根源要从其运行机制说起：  
Android为每个进程都分配了一个独立的虚拟机，不同的虚拟机在内存分配上有不同的地址空间，所以运行在不同进程的四大组件不能通过共享内存来共享数据。  
一般使用多进程会有下面几方面的问题：
  
- 静态成员和单例模式完全失效  
- 线程同步机制完全失效  
- SharedPerferences的可靠性下降  
- Application会多次创建  

## 基础概念介绍 ##

虽然多进程的使用与会带来与常规开发时不同的各种问题，但是我们仍旧可以通过特有的进程间通信方式来解决上面的问题。  
在此之前我们先来了解下IPC中的一些基础概念：Serializable、Parcelable和Binder。  
Serializable和Parcelable接口可以完成对象的序列化过程，当通过Intent和Binder传输数据时就需要使用Serializable和Parcelable，当需要把对象持久化到存储设备上或者通过网络传输时需要使用Serialzable来完成对象的持久化。

### 1. Serializable ###

java提供，实现简单（实现 Serializable 接口，生成serialVersionUID），可以通过ObjectOutputStream序列化到文件中，通过ObjectInputStream完成反序列化； serialVersionUID作用是在反序列化时判断类成员变量的数量及类型有没有发生变化。  
需要注意的是：静态成员变量属于类不属于对象，所以不会参与序列化过程；用transient关键字标记的成员变量也不参与序列化过程。

tip：在android studio中可通过设置来辅助生成serialVersionUID，步骤如下： 
 
1. File -> Settings... -> Editor -> Inspections -> Serialization issues -> Serializable class without ‘serialVersionUID‘（选中）-> apply  
2. 进入实现了Serializable中的类，选中类名，Alt+Enter弹出提示，然后直接导入完成

### 2. Parcelable ###

Android提供，实现相对复杂（实现Parcelable 接口，写 构造方法(Parcel in)、describeContents()、writeToParcel（Pacel dest，int flags）、Parcelable.Creator<T> CREATOR）

	public class User implements Parcelable {
	    public int userId;
	    public String userName;
	    public boolean isMale;
	    public Book userBook;
	
	    protected User(Parcel in) {
	        userId = in.readInt();
	        userName = in.readString();
	        isMale = in.readByte() != 0;
			//由于book是另一个可序列化对象，所以它的反序列化过程需要传递当前线程的上下文类加载器，否则会报ClassNotFound错误。
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


Serializable 使用简单，开销大；Parcelable 实现相对复杂，效率高；建议内存序列化使用Parcelable，序列化到存储设备或网络传输用Serialzable。 
 
tip：在Android Studio中只需要先将你的成员变量声明好之后，一路Alt + Enter就可以轻松实现Parcelable中的所有方法。

测试（100个WalletBankCardBean对象序列化反序列化过程的内存开销及耗时）：  

- Parcelable   30.37MB ——> 30.73MB 0.36MB 37ms ; 30.42MB ——> 30.77MB  0.35MB 35ms ; 30.49MB ——> 30.85MB  0.36MB 36ms  
- Serializable 30.59MB ——> 31.97MB 1.38MB 76ms ; 30.64MB ——> 32.01MB  1.37MB 71ms ; 30.65MB ——> 32.02MB  1.37MB 70ms 

### 3. Binder ###

Android开发中，Binder主要用在Service中，包括AIDL和Messenger，其中普通Service中的Binder不涉及进程间通信，无法触及Binder核心，而Messenger的底层就是AIDL，所以我们这里可以通过AIDL来分析Binder的工作机制。

首先，回顾下Android Studio中的aidl文件目录结构，这里与最早的Eclipse工程目录不同：  
![as中的aidl项目结构](https://github.com/wanglei8100/ProjectImageForMarkdownPad2/blob/master/aidl_project_structure.png)

tip：建议将aidl文件及其中所使用的Java Bean文件均放在aidl目录下，方便客户端和服务端一起拷贝使用，以免遗漏，  
如果aidl目录中有非aidl类且外部有使用，则需要在项目的build.gradle中做“sourceSets”路径配置：  
     
	sourceSets{
        main{
            java.srcDirs = ['src/main/java','src/main/aidl']
        }
     }

其中studio根据.aidl文件自动生成的对应java文件的目录位置也发生了变化，在build\generated\source\aidl\debug\cn\mime\aidldemo\aidl目录下。  
下面就来分析下.aidl对应的.java文件：  
 
	package cn.mime.aidldemo.aidl;

	public interface IBookManager extends android.os.IInterface {

	    public static abstract class Stub extends android.os.Binder implements cn.mime.aidldemo.aidl.IBookManager {
	        //Binder的唯一标识，一般用当前Binder的类名全路径表示
	        private static final java.lang.String DESCRIPTOR = "cn.mime.aidldemo.aidl.IBookManager";
	
	        static final int TRANSACTION_getBookList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
	        static final int TRANSACTION_addBook = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
	
	        public Stub() {
	            this.attachInterface(this, DESCRIPTOR);
	        }
	
	        /**
	         * 将服务端的Binder对象转换成客户端所需的AIDL接口类型的对象
	         * 如果客户端和服务端位于同一进程，那么返回的就是服务端的Stub对象本身
	         * 否则返回的是系统封装后的Stub.Proxy对象
	         * @param obj onServiceConnected()方法中返回的服务端Binder
	         * @return 客户端所需的AIDL接口类型的对象
	         */
	        public static cn.mime.aidldemo.aidl.IBookManager asInterface(android.os.IBinder obj) {
	            if ((obj == null)) {
	                return null;
	            }
	            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
	            if (((iin != null) && (iin instanceof cn.mime.aidldemo.aidl.IBookManager))) {
	                return ((cn.mime.aidldemo.aidl.IBookManager) iin);
	            }
	            return new cn.mime.aidldemo.aidl.IBookManager.Stub.Proxy(obj);
	        }
	
	        @Override
	        public android.os.IBinder asBinder() {
	            return this;
	        }
	
	        /**
	         * 运行在服务端的Binder线程池中，当客户端发起跨进程请求时，远程请求会通过系统底层封装后交由此方法来处理。
	         * 1.服务端通过code确定客户端请求的目标方法
	         * 2.从data中取出目标方法所需要的参数（如果目标方法有参数的话）
	         * 3.执行目标方法
	         * 4.目标方法执行完毕后，向reply中写入返回值（如果目标方法有返回值的话）
	         * 注意：如此方法返回false则客户端的请求会失败，所以我们可以在服务端中重写该方法，
	         *       然后利用这个特性来做权限验证。
	         */
	        @Override
	        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
	            switch (code) {
	                case INTERFACE_TRANSACTION: {
	                    reply.writeString(DESCRIPTOR);
	                    return true;
	                }
	                case TRANSACTION_getBookList: {
	                    data.enforceInterface(DESCRIPTOR);
	                    java.util.List<cn.mime.aidldemo.aidl.Book> _result = this.getBookList();
	                    reply.writeNoException();
	                    reply.writeTypedList(_result);
	                    return true;
	                }
	                case TRANSACTION_addBook: {
	                    data.enforceInterface(DESCRIPTOR);
	                    cn.mime.aidldemo.aidl.Book _arg0;
	                    if ((0 != data.readInt())) {
	                        _arg0 = cn.mime.aidldemo.aidl.Book.CREATOR.createFromParcel(data);
	                    } else {
	                        _arg0 = null;
	                    }
	                    this.addBook(_arg0);
	                    reply.writeNoException();
	                    return true;
	                }
	            }
	            return super.onTransact(code, data, reply, flags);
	        }
	
	        /**
	         * 上面Stub的asInterface()中，如果客户端和服务端不是位于同一进程，就会将此对象返回给客户端调用
	         */
	        private static class Proxy implements cn.mime.aidldemo.aidl.IBookManager {
	
	            private android.os.IBinder mRemote;
	
	            Proxy(android.os.IBinder remote) {
	                mRemote = remote;
	            }
	
	            @Override
	            public android.os.IBinder asBinder() {
	                return mRemote;
	            }
	
	            public java.lang.String getInterfaceDescriptor() {
	                return DESCRIPTOR;
	            }
	
	            /**
	             * 运行在客户端
	             * 1.创建该方法需要的输入型Parcel对象 _data
	             * 2.创建该方法需要的输出型Parcel对象 _reply
	             * 3.把该方法的参数信息写入 _data 中（如果有参数的话）
	             * 4.调用transact()方法来发起RPC（远程过程调用）请求，同时当前线程挂起
	             * 5.服务端的onTransact()方法被调用，直到RPC过程返回，当前线程继续执行
	             * 6.从 _reply 中读取RPC过程返回的结果（如果有返回值的话）
	             */
	            @Override
	            public java.util.List<cn.mime.aidldemo.aidl.Book> getBookList() throws android.os.RemoteException {
	                android.os.Parcel _data = android.os.Parcel.obtain();
	                android.os.Parcel _reply = android.os.Parcel.obtain();
	                java.util.List<cn.mime.aidldemo.aidl.Book> _result;
	                try {
	                    _data.writeInterfaceToken(DESCRIPTOR);
	                    mRemote.transact(Stub.TRANSACTION_getBookList, _data, _reply, 0);
	                    _reply.readException();
	                    _result = _reply.createTypedArrayList(cn.mime.aidldemo.aidl.Book.CREATOR);
	                } finally {
	                    _reply.recycle();
	                    _data.recycle();
	                }
	                return _result;
	            }
	            @Override
	            public void addBook(cn.mime.aidldemo.aidl.Book book) throws android.os.RemoteException {
	                android.os.Parcel _data = android.os.Parcel.obtain();
	                android.os.Parcel _reply = android.os.Parcel.obtain();
	                try {
	                    _data.writeInterfaceToken(DESCRIPTOR);
	                    if ((book != null)) {
	                        _data.writeInt(1);
	                        book.writeToParcel(_data, 0);
	                    } else {
	                        _data.writeInt(0);
	                    }
	                    mRemote.transact(Stub.TRANSACTION_addBook, _data, _reply, 0);
	                    _reply.readException();
	                } finally {
	                    _reply.recycle();
	                    _data.recycle();
	                }
	            }
	
	        }
	    }
	
	    public java.util.List<cn.mime.aidldemo.aidl.Book> getBookList() throws android.os.RemoteException;
	
	    public void addBook(cn.mime.aidldemo.aidl.Book book) throws android.os.RemoteException;
	}

通过上面的分析我们需要注意：   

- 当客户端发起请求时，当前线程会被挂起，直至服务端进程返回数据，所以如果远程方法很耗时，一定要放在子线程中调用。
- 服务端的Binder方法运行在Binder线程池中，所以Binder方法不管是否耗时都应该采用同步的方式实现。

总结下Binder工作机制：  
可以大致归结为三个对象（Client对象、Service对象及连接两个对象的Binder对象）和两个方法（在Client中调用的transact()方法和运行在Service中的onTransact()方法）  
调用顺序大致是：  
Client发起远程请求 ——> Binder写入参数data ——> 调用transact()，同时Client线程被挂起 ——> Service调用Binder线程池中的onTransact() ——>写入结果reply，通过Binder返回数据后唤醒Client线程

tip：相关demo源码见[https://github.com/wanglei8100/Android_IPC](https://github.com/wanglei8100/Android_IPC "Demo源码") 中的aidlclient和aidlservice

## 使用方式 ##

跨进程通信方式有很多种，如可以通过Intent中附加extras来传递信息、通过共享文件的方式来共享数据、通过Binder、ContentProvider、Socket，不同方式适合不同的场景，下面来一一说明。

### 1. Bundle ###

Bundle适用与不同进程中的四大组件间的进程通信，一般是单向通信，即在某个进程中打开另外一个进程中的组件时通过Intent传递。  
Bundle支持的数据类型有基本类型、实现了Parcelable或Serialzable接口的对象、List、Size等等具体可见Bundle类。

### 2. 共享文件 ###

通过将数据序列化到外部存储设备上，然后再从外部设备上进行反序列化来达到通过共享文件进行进程间通信。  
因为会发生并发读写问题，所以这种方式适合在对数据同步要求不高的进程间进行通信。

### 3. Messenger ###

Messenger译为信使，通过它可以在不同进程中传递Message对象，在Message中放入我们需要传递的数据。Messenger是一种轻量级IPC方案，它的底层实现是AIDL，它对AIDL做了封装使用起来更简单。由于它一次处理一个请求，因此在服务端我们不用考虑线程同步问题。  
关于Messenger的使用和AIDL类似，设计的类有传递数据的Messenger，数据载体Message，接收处理数据的Handler，其工作机制流程可以理解为：  


1. 绑定服务:将Service返回的Binder转换成IMessenger的同时得到封装好的Messenger 
2. Client发起请求:通过Messenger将封装好数据的Message发送到Service,其底层调用的是IMessenger的send()方法
3. Service在Handler的handleMessage()方法中接收到请求并处理
4. 如果需要Service的相应则发起请求时要在Client创建属于自己的Messenger并通过message.replyTo传递给Service
5. Service将返回信息封装到Message中并通过message.replyTo携带过来的Client的Messenger发送相应数据
6. Client在Handler的handleMessage()方法中接收到返回信息并处理

tip：相关demo源码见[https://github.com/wanglei8100/Android_IPC](https://github.com/wanglei8100/Android_IPC "Demo源码") 中的messengerclient和messengerservice

### 4. AIDL ###

上面讲到的Messenger的底层实现也是AIDL，使得调用更加简单，但是由于Messenger是以串行的方式处理客户端发来的消息，如果有大量的并发请求使用Messenger是不合适的，而且Messenger的作用主要是为了传递消息，很多时候我们可能需要跨进程调用服务端的方法，所以这时我们可以使用AIDL来实现。

AIDL的使用在上面讲Binder时就有提过，这里简单回顾总结下使用流程，可以分为服务端和客户端两块：  

- Service：首先创建一个Service来监听客户端的连接请求，然后创建一个AIDL文件，将暴露给客户端的接口在这个AIDL文件中声明，最后在Service中实现这个AIDL接口即可
- Client：首先绑定服务端的Service，绑定成功后将服务端返回的Binder对象转化成AIDL接口所属的类型，最后就可以调用AIDL中的方法了

AIDL文件支持的数据类型：

- 8种基本类型（byte、short、int、long、char、boolean、float、double）
- String和CharSequence
- List：只支持ArrayList，切里面的每个元素必须都能够被AIDL支持
- Map：只支持HashMap，切里面的每个元素必须都能够被AIDL支持，包括key和Value
- Parcelable：所有实现Parcelable接口的对象
- AIDL：所有的AIDL接口本身也可以在AIDL文件中使用

相关demo源码见[https://github.com/wanglei8100/Android_IPC](https://github.com/wanglei8100/Android_IPC "Demo源码") 中的aidlclient和aidlservice。  
关于AIDL的使用需要特殊注意的有几点：

1. demo中的Book.aidl 文件

		// Book.aidl
		package cn.mime.aidldemo.aidl;

		// Declare any non-default types here with import statements
		parcelable Book;
	AIDL中每个实现了Parcelable接口的类都需要按照上面这种方式去创建相应的AIDL文件并声明那个类为parcelable
2. 在demo中的IBookManger.adil中有两处需要加以说明

		// IBookManager.aidl
		package cn.mime.aidldemo.aidl;

		// Declare any non-default types here with import statements
		import cn.mime.aidldemo.aidl.Book;

		interface IBookManager {
	    	List<Book> getBookList();
	    	void addBook(in Book book);
		}
	
	- import cn.mime.aidldemo.aidl.Book; Book类的导入，我们知道在java类编写规范时，同一个包名下的类文件引用是不需要导包的，但是AIDL文件编写规则是需要手动导入的，否则编译时就会报 ProcessException。
	- void addBook(in Book book); 在AIDL中除了基本数据类型，其他类型的参数必须标上定向tag：in、out或inout。
		- in 服务端接收客户端流向的完整对象，但是服务端的修改不影响客户端该对象；
		- out 服务端接收客户端流向对象的空对象，但服务端的修改影响客户端该对象；
		- inout 服务端接收客户端流向的完整对象，且服务端的修改影响客户端该对象；
		- 定向tag在aidl方法接口的方法中修饰序列化对象，用out/inout修饰的序列化对象必须写readFromParcel(Parcel dest)方法

3. 在源码的Service中我们可以看到用到的List是CopyOnWriteArrayList它不仅实现了List接口，而且支持并发读/写，这是因为考虑到AIDL方法是在服务端的Binder线程池中执行的，因此当有多个客户端同时连接时会存在多个线程同时访问的情形，所以我们要在AIDL方法中处理线程同步。虽然这里使用是CopyOnWriteArrayList但是Binder中会按照List的规范去访问数据并最终形成一个ArrayList传递给客户端，这也印证了我们上面提到的AIDL支持的数据类型中List只有ArrayList。类似的还有ConcurrentHashMap。
4. AIDL中只支持方法，不支持声明静态常量

上面讲的是AIDL的基础用法，所写的demo案例也都是客户端向服务发出远程调用，然后服务端给出响应。但是，有时候还有一种业务场景就是客户端需要监听服务端的某个动作，然后做响应的操作。对于这种场景我们需要做进一步操作了，我们在上面demo的基础上新建一对案列aidlclient_v2和aidlservice_v2进行分析。

首先，新建一个AIDL文件IOnNewBookArrivedListener.aidl：

    // IOnNewBookArrivedListener.aidl
	package cn.mime.aidlservice_v2;
	
	// Declare any non-default types here with import statements
	import cn.mime.aidlservice_v2.Book;
	
	interface IOnNewBookArrivedListener {
	    void onNewBookArrived(in Book newBook);
	}

此aidl是服务端对客户端提供的用来监听接口，在案例demo中的作用是服务端如果有新书到来就会调用此接口中的方法通知客户端。

其次，改造IBookManager.aidl,新增注册监听及取消监听的方法：

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
这里需要注意的是IOnNewBookArrivedListener是aidl文件，且与IBookManager.aidl位于同级目录下也要手动导入。

然后，就是服务端Service中的逻辑改造了：

- 实现IBookManager接口中新增的两个方法，注册监听，取消监听
- 在onCreate中创建一个线程，每隔五秒钟创建一本新书并通过监听通知客户端

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
		            mRemoteListenerList.register(listener);
		        }
		
		        @Override
		        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
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
		                    onNewBookArrived(new Book(mBookList.size() + 1, "武侠小说" + mBookList.size() + 1));
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
		        int broadcastNumber = mRemoteListenerList.beginBroadcast();
		        for (int i = 0; i < broadcastNumber; i++) {
		            IOnNewBookArrivedListener listener = (IOnNewBookArrivedListener) mRemoteListenerList.getBroadcastItem(i);
		            if (listener != null) {
		                try {
		                    listener.onNewBookArrived(book);
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

这里需要注意的是RemoteCallbackList这个类，它是系统专门提供用于注册/删除跨进程listener的接口，它是一个泛型，支持管理任意的AIDL接口。
    
	public class RemoteCallbackList<E extends IInterface> {
为什么要引用它来进行注册/删除跨进程listener的接口？因为Binder会把客户端传递过来的监听对象反序列化后变成一个新的对象，所以无论是在注册还是取消注册时传递过来的监听对象均不是同一个对象，所以传统的List并不能完成注册/删除跨进程listener的接口。而RemoteCallbackList的特性则可以，它的内部有一个Map结构专门用来保存所有的AIDL回调，这个Map的key是IBinder类型，value是CallBack类型：  
	
	ArrayMap<IBinder, Callback> mCallbacks = new ArrayMap<IBinder, Callback>();
而CallBack中封装了真正的远程listener，虽然多次跨进程传输客户端的同一个对象会在服务端生成不同的对象，但是它们底层的Binder对象是同一个，而RemoteCallbackList的Key存储的就是相同的Binder，所以它可以在取消监听时通过Binder准确取消所监听的对象，此外它还会在客户端进程终止后，自动移除客户端所注册的Listener，并且它内部自动实现了线程同步的功能，所以我们使用它来进行注册/解注册时不需要做额外的线程同步工作。  

RemoteCallbackList常用方法： 
 
- register(listener) ：注册监听
- unregister(listener)：取消监听
- int beginBroadcast()：准备开始调用监听方法，和finishBroadcast()配对使用，返回监听的数量
- Listenr getBroadcastItem(index)：获取监听对象，必须在beginBroadcast()方法后调用
- finishBroadcast()：结束监听调用，和beginBroadcast()配对使用

最后，客户端改造，客户端改造较为简单：

- 创建IOnNewBookArrivedListener

	 	private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub(){

	        @Override
	        public void onNewBookArrived(Book newBook) throws RemoteException {
	            Message message = Message.obtain();
	            message.obj = newBook;
	            mHandler.sendMessage(message);
	        }
	    };

- 注册远程监听

		if (checkRemoteService()){
            try {
                mRemoteService.registerListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

- 处理监听回调

		 private Handler mHandler = new Handler(){
	        @Override
	        public void handleMessage(Message msg) {
	            Book newBook = (Book) msg.obj;
	            Toast.makeText(MainActivity.this,newBook.toString(),Toast.LENGTH_SHORT).show();
	        }
	    };

- 取消远程监听
		
		if (checkRemoteService()){
		    try {
		        mRemoteService.unregisterListener(mOnNewBookArrivedListener);
		    } catch (RemoteException e) {
		        e.printStackTrace();
		    }
		}
这里需要注意的是因为远程监听的回调是运行在客户端的Binder线程池中，所以不能有直接更新UI的操作。另外，无论是注册还是取消注册的操作之前都要判断远程服务的链接是否存在，否则会报警告：

		private boolean checkRemoteService() {
	        return mRemoteService !=null&&mRemoteService.asBinder().isBinderAlive();
	    }	    


在上面的业务场景的基础上我们还需要做最后一步，权限验证，因为默认情况下我们的远程服务任何人都可以连接，这并不是我们想看到的，所以我们需要给服务加入权限验证功能，只有验证通过才可以调用我们服务中的方法。在AIDL中进行权限验证发方法有很多，这里介绍常用的两种方法。

1. 在onBind中进行验证，验证不通过直接返回null，则验证不通过客户端就无法绑定服务。验证方式可以有很多种，比如通过intent取出客户端和服务端协商好的唯一性keySecret进行校验，也可以通过自定义permission验证，如果通过自定义permission验证，我们需要在服务端的AndroidMenifest中声明所需权限，比如：

	 	<permission android:name="cn.mime.aidlservice_v3.permission.ACCESS_BOOK_SERVICE"
        			android:protectionLevel="normal"/>

然后在服务端的onBind中校验此权限：

		 @Override
	    public IBinder onBind(Intent intent) {
	        int checkStatus = checkCallingOrSelfPermission("cn.mime.aidlservice_v3.permission.ACCESS_BOOK_SERVICE");
	        if (checkStatus == PackageManager.PERMISSION_DENIED){
	            return null;
	        }
	        return mBookManager;
	    }

则调用我们服务的客户端需要在AndroidMenifest中使用permission即可：

	<uses-permission android:name="cn.mime.aidlservice_v3.permission.ACCESS_BOOK_SERVICE" />

2. 在服务端的onTransact方法中进行权限验证，如果验证失败直接返回false，这样服务端就不会终止执行AIDL中个的方法，从而达到保护服务端的效果。具体验证方式除和上面一样的外，还可以采用Uid和Pid来验证，比如：

		@Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int callingUid = getCallingUid();
            String[] packages = getPackageManager().getPackagesForUid(callingUid);
            if (packages!=null&&packages.length>0){
                if (!packages[0].startsWith("cn.mime.aidlclient")){
                    return false;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

tip：关于权限验证的demo源码见[https://github.com/wanglei8100/Android_IPC](https://github.com/wanglei8100/Android_IPC "Demo源码") 中的aidlclient_v3和aidlservice_v3


### 5. ContentProvider ###

ContentProvider是Android中提供的专门用于不同应用间进行数据共享的方式，所以它天生适合进程间通信，底层实现也是Binder。下面是自定义Provider时的一些注意事项：

- 服务端定义ContentProvider时新建类extends ContentProvider然后重写onCreate（）、insert（）、delete（）、update（）、query（）和getType（）方法。onCreate（）是运行在主线程，其它CRUD方法运行在Binder线程
- 然后在Manifest中注册provider，注意两个属性：android:authorities是指定provider唯一属性的，android: permission可选是指定访问此provider必须要申请的相应权限，也可以单独指定android:readPermission或android:writePermission
- 可以使用SQLiteOpneHelper结合数据库完成ContentProvider对数据的操作
- 可以使用UriMatcher来匹配查询的Uri，UriMatcher.addURI（）初始化添加支持的URI，UriMatcher.match（）匹配查询的uri。
- 在insert，delete和update方法中因为数据发生了变化要调用ContentResolver方法的notifyChange（）
- 要观察一个ContentProvider中的数据改变情况，可以通过ContentResolver的registerContentObserver方法来注册观察者，通过unregisterContentObserver方法来解除观察者。
- 客户端使用定义好的android:authorities属性值指定Uri，然后利用ContentResolver进行CRUD操作，如果服务端的provider注册时有声明权限，客户端必须在manifest中申请相应权限

### 6. Socket ###

Socket也称为“套接字”，是网络通信中的概念，它分为流失套接字和用户数据报套接字两种，分别对应于网络的传输控制层的TCP和UDP协议。  
TCP协议：面向连接的协议，提供稳定的双向通信功能，连接的建立需要经过“三次握手”才能完成，提供了超时重传机制，稳定性很高。  
UDP协议：无连接的，提供不稳定的单向通信功能，性能上效率更好，但是不能保证数据一定能正确传输，特别是在网络拥塞的情况下。

Socket不仅仅可以实现进程间的通信，而且还可以实现设备间的通信，不过需要设备之间的IP地址互相可见。虽然如此，但是使用Socket的业务场景一般是在客户端和服务端通信比较频繁，需要建立长时间稳定的连接的时候，所以在Android跨进程通信的实际运用场景较少。 

## Binder连接池 ##

需求场景，当应用中有多个模块都需要使用AIDL，并且各个模块之间没有耦合，则是不是我们要在服务端把每个模块对应的建立一个Service，显然这样是不可取的，因为Service本身就是一种系统资源，太多的Service使得应用看起来很重量级。这时我们可以创建一个统一的Aidl的管理类，然后统一在一个service中去管理各个模块。我们可以简单称之为Binder连接池，举个例子：假设服务端有分享和支付两个模块，这两个模块相互独立，现在需要两个模块都对外提供调用接口。此时我们可以在服务端这样设计：

- 创建分享和支付aidl文件
	
		interface IShareContent {
		    boolean share(String content);
		}

		interface IPayMoney {
		    boolean pay(String orderNo);
		}

- 创建分享和支付的对应实现类
		
		public class ShareContentImpl extends IShareContent.Stub {
		    @Override
		    public boolean share(String content) throws RemoteException {
		        boolean status = !TextUtils.isEmpty(content);
		        Log.d(TAG,"share status : "+status);
		        return status;
		    }
		}


		public class PayMoneyImpl extends IPayMoney.Stub {
		    @Override
		    public boolean pay(String orderNo) throws RemoteException {
		        boolean status = !TextUtils.isEmpty(orderNo);
		        Log.d(TAG,"pay status : "+status);
		        return status;
		    }
		}

	
- 创建管理分享和支付功能的BinderPool及其实现类

		interface IBinderPool {
		    IBinder getPayMoneyBinder();
		    IBinder getShareContentBinder();
		}


		public class BinderPoolImpl extends IBinderPool.Stub {
		    @Override
		    public IBinder getPayMoneyBinder() throws RemoteException {
		        return new PayMoneyImpl();
		    }
		
		    @Override
		    public IBinder getShareContentBinder() throws RemoteException {
		        return new ShareContentImpl();
		    }
		}


- 在Service中onBinder方法中将BinderPool返回

		public class BinderPoolService extends Service {
		    @Override
		    public IBinder onBind(Intent intent) {
		        return new BinderPoolImpl();
		    }
		}


在客户端对应的aidl文件只需要导入IBinderPool和使用的业务模块的aidl，然后在绑定服务成功时多了一次转换：

		@Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"onServiceConnected  connected success !");
            IBinderPool binderPool = IBinderPool.Stub.asInterface(service);
            try {
                mRemoteService = IShareContent.Stub.asInterface(binderPool.getShareContentBinder());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

tip：具体demo源码见[https://github.com/wanglei8100/Android_IPC](https://github.com/wanglei8100/Android_IPC "Demo源码") 中的binderpoolservice和binderpool_shareclient


## IPC方式适用场景总结 ##

- Bundle:四大组件间的进程间通信，简单易用，但是只能传输Bundle支持的数据类型
- 共享文件：无并发访问情形，交换简单的数据实时性不高的场景，简单易用，但是不适合高并发场景，且无法做到进程间的即时通信
- AIDL：一对多通信且有RPC（远程过程调用）需求，功能强大，支持一对多并发通信，支持实时通信，但是使用稍复杂，需注意处理好线程同步
- Messenger：低并发的一对多即时通信，无RPC需求，或者无需返回结果的RPC需求，功能一般，支持一对多串行通信，支持试试通信，但不能很好处理高并发情形，不支持RPC，数据通过Message传输，因此只能传输Bundle支持的数据类型
- ContentProvider：一对多的进程间的数据共享，在数据访问方面功能强大，支持一对多并发数据共享，可以通过Call方法扩展其他操作，但是可以理解为受约束的AIDL，主要提供数据源的CRUD操作
- Socket:网络数据交互，功能强大，可以通过网络传输字节流，支持一对多并发实时通信，但是实现细节稍微有点麻烦，不支持直接的RPC