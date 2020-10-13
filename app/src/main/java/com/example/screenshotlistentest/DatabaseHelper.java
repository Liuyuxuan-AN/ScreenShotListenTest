package com.example.screenshotlistentest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * author : 刘雨轩
 * e-mail : 1262610086@qq.com
 * date   : 2020/10/12
 * desc   :用于进行数据的建表操作完成记事本的记录
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "NoteBook.db";
    public static int version = 1;
    public static final String TABLE_NAME = "Diary";

    //建表sql语句
    public static final String CREATE_DIARY="create table Diary(" +
            "id integer primary key autoincrement," +
            "title text," +
            "content text)";

    private Context mContext;

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DIARY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
