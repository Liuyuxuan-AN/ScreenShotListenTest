package com.example.screenshotlistentest.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.screenshotlistentest.DatabaseHelper;
import com.example.screenshotlistentest.NoteChangeMessage;
import com.example.screenshotlistentest.R;
import com.example.screenshotlistentest.fragment.Page1Fragment;

import org.greenrobot.eventbus.EventBus;

import static com.example.screenshotlistentest.DatabaseHelper.TABLE_NAME;
import static com.example.screenshotlistentest.fragment.Page1Fragment.TAG_INSERT;
import static com.example.screenshotlistentest.fragment.Page1Fragment.TAG_UPDATE;
import static com.example.screenshotlistentest.fragment.Page1Fragment.getDbHelper;
/**
 *    author : 刘雨轩
 *    e-mail : 1262610086@qq.com
 *    date   : 2020/10/13
 *    desc   :进行笔记的编写、保存和删除
 */
public class NoteActivity extends AppCompatActivity implements View.OnClickListener {

    EditText title;
    EditText content;
    TextView save;
    TextView delete;

    private SQLiteDatabase db;
    public DatabaseHelper deHelper = getDbHelper();
    private int tag;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        title = (EditText) findViewById(R.id.note_title);
        content = (EditText) findViewById(R.id.note_content);
        save = (TextView) findViewById(R.id.save);
        delete = (TextView) findViewById(R.id.delete);
        save.setOnClickListener(this);
        delete.setOnClickListener(this);
        title.setSelection(title.getText().length());
        content.setSelection(content.getText().length());
        db = deHelper.getWritableDatabase();
        Intent intent = getIntent();
        tag = intent.getIntExtra("TAG", -1);
        switch (tag) {
            case Page1Fragment.TAG_UPDATE:
                id = intent.getIntExtra("ID", -1);
                Cursor cursor = db.query(TABLE_NAME, null, "id=?",
                        new String[]{String.valueOf(id)}, null, null, null);
                if (cursor.moveToFirst()) {
                    String select_title = cursor.getString(cursor.getColumnIndex("title"));
                    String select_content = cursor.getString(cursor.getColumnIndex("content"));
                    title.setText(select_title);
                    content.setText(select_content);
                }
                break;
            default:
                break;
        }
    }

    //将menu中设置好的actionbar进行加载
    //测试后发现并不适合，该方法只在按下menu键后才会进行执行，并不符合预期
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }*/

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.save:
                if(tag == TAG_INSERT){
                    ContentValues values = new ContentValues();
                    values.put("title",title.getText().toString());
                    values.put("content",content.getText().toString());
                    //将数据插入到数据库中
                    db.insert(TABLE_NAME,null,values);
                    publishChange();
                    values.clear();
                    Toast.makeText(this,"保存成功",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                }else if(tag == TAG_UPDATE){
                    String update_title = title.getText().toString();
                    String update_content = content.getText().toString();
                    ContentValues values = new ContentValues();
                    values.put("title",update_title);
                    values.put("content",update_content);
                    db.update(TABLE_NAME,values,"id = ",new String[] {String.valueOf(id)});
                    publishChange();
                    finish();
                    break;
                }
            case R.id.delete:
                if(tag == TAG_UPDATE){
                    db.delete(TABLE_NAME,"id = ?",new String[] {String.valueOf(id)});
                    publishChange();
                    Toast.makeText(this,"删除成功",Toast.LENGTH_SHORT).show();
                }else if(tag == TAG_INSERT){
                    Toast.makeText(this,"别逗了，都没保存你还删个啥，去去去",Toast.LENGTH_SHORT).show();
                }
                finish();
                break;
            default:
        }
    }

    private void publishChange(){
        EventBus.getDefault().post(NoteChangeMessage.getInstance("Changed!"));
    }
}