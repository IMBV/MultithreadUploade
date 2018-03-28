package com.teckjob.module.multithreaduploade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerview = findViewById(R.id.recyclerview);

        LinearLayoutManager layoutmanager = new LinearLayoutManager(this);
        layoutmanager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerview.setLayoutManager(layoutmanager);
        ArrayList<String> list = new ArrayList<String>();
        ArrayList<AdapterData> datas = new ArrayList<>();
        for (int i=0;i<30;i++){
            AdapterData data = new AdapterData();
            data.field = i + "student";
            datas.add(data);
        }
        Adapter adapter = new Adapter(this,datas);
        recyclerview.setAdapter(adapter);
    }
}
