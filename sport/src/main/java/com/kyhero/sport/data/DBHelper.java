package com.kyhero.sport.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "main.db";
    private static final int DB_VERSION = 1;

    public static final String CREATE_PULLUP = "create table PullupParam("
            + "id integer primary key autoincrement, "
            + "times int not null, "
            + "pose_0_0 float not null, " //NOSE
            + "pose_0_1 float not null, "
            + "pose_0_2 float not null, "

            + "pose_9_0 float not null, " //LEFT_MOUTH
            + "pose_9_1 float not null, "
            + "pose_9_2 float not null, "

            + "pose_10_0 float not null, "//RIGHT_MOUTH
            + "pose_10_1 float not null, "
            + "pose_10_2 float not null, "

            + "pose_11_0 float not null, "//LEFT_SHOULDER
            + "pose_11_1 float not null, "
            + "pose_11_2 float not null, "

            + "pose_13_0 float not null, "//LEFT_ELBOW
            + "pose_13_1 float not null, "
            + "pose_13_2 float not null, "

            + "pose_15_0 float not null, "//LEFT_WRIST
            + "pose_15_1 float not null, "
            + "pose_15_2 float not null, "

            + "pose_12_0 float not null, "//RIGHT_SHOULDER
            + "pose_12_1 float not null, "
            + "pose_12_2 float not null, "

            + "pose_14_0 float not null, "//RIGHT_ELBOW
            + "pose_14_1 float not null, "
            + "pose_14_2 float not null, "

            + "pose_16_0 float not null, "//RIGHT_WRIST
            + "pose_16_1 float not null, "
            + "pose_16_2 float not null, "

            + "pose_23_0 float not null, "//LEFT_HIP
            + "pose_23_1 float not null, "
            + "pose_23_2 float not null, "

            + "pose_25_0 float not null, "//LEFT_KNEE
            + "pose_25_1 float not null, "
            + "pose_25_2 float not null, "

            + "pose_27_0 float not null, "//LEFT_ANKLE
            + "pose_27_1 float not null, "
            + "pose_27_2 float not null, "

            + "pose_24_0 float not null, "//RIGHT_HIP
            + "pose_24_1 float not null, "
            + "pose_24_2 float not null, "

            + "pose_26_0 float not null, "//RIGHT_KNEE
            + "pose_26_1 float not null, "
            + "pose_26_2 float not null, "

            + "pose_28_0 float not null, "//RIGHT_ANKLE
            + "pose_28_1 float not null, "
            + "pose_28_2 float not null) ";



    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PULLUP);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
