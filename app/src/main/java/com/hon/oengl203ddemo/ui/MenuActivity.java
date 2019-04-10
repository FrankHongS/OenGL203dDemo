package com.hon.oengl203ddemo.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.hon.oengl203ddemo.R;
import com.hon.oengl203ddemo.utils.ContentUtil;
import com.hon.oengl203ddemo.utils.FileUtil;
import com.hon.oengl203ddemo.utils.ToastUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * Created by Frank_Hon on 2017/4/23.
 * e-mail:frank_hon@foxmail.com
 */

public class MenuActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_OPEN_FILE=1000;

    private ListView mListView;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        final RxPermissions rxPermissions=new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted->{
                    if (granted){

                    }else {
                        finish();
                    }
                });

        mListView=findViewById(R.id.lv_content);
        BaseAdapter adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,addItems());
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        startActivity(SimpleClientTrackingActivity.class);
                        break;
                    case 1:
                        Intent target= FileUtil.createGetContentIntent();
                        Intent intent=Intent.createChooser(target,"select a file");

                        startActivityForResult(intent,REQUEST_CODE_OPEN_FILE);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private String[] addItems(){
        return new String[]{
                "Simple Client Recognition",
                "Load Local Models"
        };
    }

    private void startActivity(Class<? extends Activity> target){
        Intent intent=new Intent(this,target);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case REQUEST_CODE_OPEN_FILE:
                if(resultCode == RESULT_OK){
                    Uri uri=data.getData();
                    String path= ContentUtil.getPath(getApplicationContext(),uri);

                    if(path!=null){
                        launchModelRendererActivity(path);
                    }else{
                        ToastUtil.showToast(getApplicationContext(),"Problem loading '" + uri.toString() + "'");
                    }
                }else{
                    ToastUtil.showToast(getApplicationContext(), "Failed...Result when loading file was '" + resultCode + "'");
                }
                break;
            default:
                break;
        }
    }
    private void launchModelRendererActivity(String filename){
        Intent intent=new Intent(getApplicationContext(),DisplayActivity.class);

        Bundle b = new Bundle();
        b.putString("uri", filename);
        intent.putExtras(b);
        startActivity(intent);
    }
}
