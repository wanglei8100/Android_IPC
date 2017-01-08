package cn.mime.binderpoolservice;

import android.os.IBinder;
import android.os.RemoteException;

/**
 * <p>write the description
 *
 * @author wangshan
 * @version 2.0.0
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
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
