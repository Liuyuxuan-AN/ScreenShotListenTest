package com.example.screenshotlistentest.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.screenshotlistentest.DatabaseHelper;
import com.example.screenshotlistentest.R;
import com.example.screenshotlistentest.activity.NoteActivity;

import java.util.ArrayList;
import java.util.List;

import static com.example.screenshotlistentest.DatabaseHelper.TABLE_NAME;

public class Page1Fragment extends Fragment {
    public static DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private List<String> note = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private SwipeRefreshLayout swipeRefresh;
    private Button add;

    private String select_item;
    private int Id;
    //设置标签，区别是第一次创建还是后续进行修改
    public static final int TAG_INSERT=1;
    public static final int TAG_UPDATE=0;

    public static DatabaseHelper getDbHelper(){
        return dbHelper;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page1_frag,container,false);
        add = (Button)view.findViewById(R.id.add);
        swipeRefresh = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh);
        listView=(ListView)view.findViewById(R.id.list_view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        //初始化建表
        dbHelper=new DatabaseHelper(getContext(),DatabaseHelper.DB_NAME,null,DatabaseHelper.version);
        dbHelper.getWritableDatabase();
        init();

        //添加笔记,此时标签置为INSERT
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NoteActivity.class);
                intent.putExtra("TAG",TAG_INSERT);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(),NoteActivity.class);
                Id = getId(i);
                intent.putExtra("ID",Id);
                intent.putExtra("TAG",TAG_UPDATE);
                startActivity(intent);
            }
        });

    }

    //进行表数据的查询并置入ListView
    private void init(){
        db=dbHelper.getWritableDatabase();
        note.clear();
        //查询数据库，将title一列添加到列表项目中
        Cursor cursor=db.query(TABLE_NAME,null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            String note_item;
            do{
                note_item=cursor.getString(cursor.getColumnIndex("title"));
                note.add(note_item);
            }while(cursor.moveToNext());
        }
        cursor.close();
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,note);
        listView.setAdapter(adapter);
    }

    //下拉刷新事件，注意在Ui线程上进行Ui操作
    public void refresh(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        init();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    //查询所点击笔记的序列号
    private int getId(int position){
        int id;
        select_item = note.get(position);
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME,new String[] {"id"},"title = ?", new String[] {select_item},null,null,null);
        cursor.moveToFirst();
        id = cursor.getInt(cursor.getColumnIndex("id"));
        return id;
    }
}
