package cn.mime.binderpoolservice;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * <p>write the description
 *
 * @author wangshan
 * @version 2.0.0
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class PayMoneyImpl extends IPayMoney.Stub {
    @Override
    public boolean pay(String orderNo) throws RemoteException {
        boolean status = !TextUtils.isEmpty(orderNo);
        Log.d(TAG,"pay status : "+status);
        return status;
    }
}
