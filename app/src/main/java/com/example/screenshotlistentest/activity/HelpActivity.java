package com.example.screenshotlistentest.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.screenshotlistentest.Item;
import com.example.screenshotlistentest.R;
import com.example.screenshotlistentest.adapter.ItemAdapter;

import java.util.ArrayList;
import java.util.List;

@Route(path = "/com/HelpActivity")
public class HelpActivity extends AppCompatActivity {

    private List<Item> itemList = new ArrayList<>();

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("求助反馈");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        initItems();
        ItemAdapter adapter = new ItemAdapter(HelpActivity.this,R.layout.listview_item,itemList);
        ListView listView = (ListView)findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        Intent intent = new Intent(HelpActivity.this,WebViewActivity.class);
                        startActivity(intent);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initItems(){
        Item item1 = new Item("咨询求助","前往我的客服",R.drawable.pic1);
        Item item2 = new Item("意见反馈","功能异常或建议",R.drawable.pic2);
        itemList.add(item1);
        itemList.add(item2);
    }
}