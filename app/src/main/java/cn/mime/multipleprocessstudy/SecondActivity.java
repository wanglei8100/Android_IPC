package cn.mime.multipleprocessstudy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

import cn.mime.multipleprocessstudy.bean.WalletBankCardBean;

public class SecondActivity extends AppCompatActivity {
    private static final String TAG = "SecondActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        ArrayList<WalletBankCardBean> datas = (ArrayList<WalletBankCardBean>) getIntent().getSerializableExtra("data");
        Log.d(TAG,System.currentTimeMillis()+"");
    }
}
