package com.kyhero.sport.communal;


import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import androidx.camera.core.CameraSelector;

import com.kyhero.sport.kalman.KalmanFilter;

import java.util.ArrayList;
import java.util.List;

public class SportParam {

    public enum SportStatus {
        WaitforPerson,  //没有人在画面
        DetectPerson,  //检测到人在画面
        PersonInmat,    //人在垫子上
        AnticipationOk, //预备动作ok
        Runing,         //正在运动
        Finish          //运动结束
    }

    public static SportStatus sportStatus = SportStatus.WaitforPerson;

    public static Bitmap bitmapfromImageProxy = null;
    public static int lensFacing = CameraSelector.LENS_FACING_FRONT;
    public static int offset = 0;
    public static int SportDifficulty = 0;
    public static int SportTime = 30;
    public static int TimeContDown = 5+1;
    public static boolean SportHalf= false;
    public static boolean SportEnd= false;
    public static int SportType = 2;
    public static boolean SportDect = false;
    public static int SportMatLocation = 0;


    public static KalmanFilter kalmanFilter=new KalmanFilter();

    public static Point matstar = new Point(0, 0);
    public static Point matend = new Point(1280, 720);


    public static Point personstar = new Point(0, 0);
    public static Point personend = new Point(1280, 720);

    public static Point matstarpx = new Point(0, 0);
    public static Point matendpx = new Point(1957, 1308);

    public static float imageX = 1280;
    public static float imageY = 720;

    public static float imageXPx = 1957;
    public static float imageYPx =1308;

    public static float Xscale = imageX / imageXPx;
    public static float Yscale = imageY / imageYPx;

    public static float imageCenterX = 640;
    public static float imageCenterY = 540;

    public static List<Boolean> LsError0 =new ArrayList<>();
    public static List<Boolean> LsError1 =new ArrayList<>();
    public static List<Boolean> LsError2 =new ArrayList<>();

    public static Boolean CheckError0 =true;
    public static Boolean CheckError1 =true;
    public static Boolean CheckError2 =true;


    public static void setLayoutparams(int imageXPxx, int imageYPxx) {
        imageXPx = imageXPxx;
        imageYPx = imageYPxx;
        Xscale = imageX / imageXPx;
        Yscale = imageY / imageYPx;

        matstar.x = (int) (matstarpx.x * Xscale);
        matstar.y = (int) (matstarpx.y * Yscale);
        matend.x = (int) (matendpx.x * Xscale);
        matend.y = (int) (matendpx.y * Yscale);

        if(SportParam.matstar.x>640){
            SportParam.matstar.x-=(SportParam.matstar.x-640)*(200.0f/1280.0f);
        }else{
            SportParam.matstar.x+=(640- SportParam.matstar.x)*(200.0f/1280.0f);
        }
        if(SportParam.matend.x>640){
            SportParam.matend.x-=(SportParam.matend.x-640)*(200.0f/1280.0f);
        }else{
            SportParam.matend.x+=(640- SportParam.matend.x)*(200.0f/1280.0f);
        }

        Log.d("raydrag", "set " + Xscale + "  " + Yscale);
        Log.d("raydrag", "set " + matstar.x + " " + matstar.y + " " + matend.x + " " + matend.y);

    }


}
