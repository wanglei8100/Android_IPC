package cn.mime.multipleprocessstudy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import cn.mime.multipleprocessstudy.bean.WalletBankCardBean;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openSecondActivity(View view){
        long currentTimeMillis = System.currentTimeMillis();
        Log.d(TAG,currentTimeMillis+"");
        ArrayList<WalletBankCardBean> datas = getExtraData();
        long timeConsume = System.currentTimeMillis() - currentTimeMillis;
        Log.d(TAG,"timeConsume = "+timeConsume);
        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra("data",datas);
        startActivity(intent);
    }

    private ArrayList<WalletBankCardBean> getExtraData() {
        ArrayList<WalletBankCardBean> userArrayList = new ArrayList<>();
        for (int i = 0;i<100;i++){
            userArrayList.add(new WalletBankCardBean("269556684","622248597425474588","中国建设银行","http://192.48.52:8080/imges/bankimge1",
                    "http://192.48.52:8080/imges/bankimge1","http://192.48.52:8080/imges/bankimge1",
                    "622248597425474588","type1","130123456789","张三","315666498455648855",i));
        }
        return userArrayList;
    }

    public void openThirdActivity(View view){
        startActivity(new Intent(this,ThirdActivity.class));
    }
}
