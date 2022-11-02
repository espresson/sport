package com.kyhero.sport.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;

public class Dao {

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public int Pulltimes = 0;

    public Dao(Context context) {
        this.dbHelper = new DBHelper(context);
        this.db = dbHelper.getWritableDatabase();
        Pulltimes = PullupSearchDao();
    }


    public int PullupSearchDao() {
        Cursor cursor;
        cursor = db.query("PullupParam", null, null, null, null, null, null);
        int count = cursor.getCount();
        System.out.println("PullupSearchDao " + count);

        if (count == 0) {
            cursor.close();
            return 0;
        }

        cursor.move(count);
        Integer id = cursor.getInt(cursor.getColumnIndex("id"));
        System.out.println(">>>  id  " + id);
        int times = cursor.getInt(cursor.getColumnIndex("times"));
        System.out.println(">>>  times  " + times);

        cursor.close();
        return times;

    }

    public void PullupChangeDao(int num) {
        ContentValues values = new ContentValues();

        values.put("times", 1 * 10);
        values.put("vol1", 2 * 10);
        values.put("vol2", 1.111 * 10);

        String[] whereArgs = new String[]{String.valueOf(num + 1)};
        db.update("PullupParam", values, "id=?", whereArgs);

    }

    public void PullupInsertDao(List<PoseLandmark> poselist) {
        int i, j;
        System.out.println("PullupInsertDao 插入数据");
        // 向该对象中插入键值对
        ContentValues values = new ContentValues();
        values.put("times", Pulltimes);
        for (i = 0; i < poselist.size(); i++) {
            values.put("pose_"+poselist.get(i).getLandmarkType()+"_0", poselist.get(i).getPosition().x);
            values.put("pose_"+poselist.get(i).getLandmarkType()+"_1", poselist.get(i).getPosition().y);
            values.put("pose_"+poselist.get(i).getLandmarkType()+"_2", poselist.get(i).getInFrameLikelihood());
        }
        db.insert("PullupParam", null, values);
    }

}
