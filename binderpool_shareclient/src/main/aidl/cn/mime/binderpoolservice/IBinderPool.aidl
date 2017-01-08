// IBinderPool.aidl
package cn.mime.binderpoolservice;

// Declare any non-default types here with import statements

interface IBinderPool {
    IBinder getPayMoneyBinder();
    IBinder getShareContentBinder();
}
