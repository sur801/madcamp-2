package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// 처음 이 데이터베이스 헬퍼가 호출되면, 자동으로 디비/테이블을 생성해준다 (백그라운드에)
public class DatabaseHelper extends SQLiteOpenHelper {
    // 데이터베이스 이름이다 (테이블 이름과 무관)
    public static String NAME = "memo.db";
    public static int VERSION = 1;

    // 내가 SQLite를 실행하려고 하는 context를 설정해준다. 그걸 경우, 백그라운드에 있는 SQLite가 자동으로 설정된다.
    public DatabaseHelper(Context context){
        // context, DB이름, 커서, 버전  순서
        // 커서는 나중에 데이터 접근할때 쓰인다
        super(context, NAME, null, VERSION);
    }

    // 디비가 생성되면, 테이블도 자동으로 생성해준다
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "create table if not exists noteData(time PRIMARY KEY, title text)";
        sqLiteDatabase.execSQL(sql);
    }

    // 만약 이 테이블이 예전에 다른 어플에 의해 사용되었었다면, 해당 테이블을 버려준다.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if(i1 > 1){
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS noteData");
        }
    }
}