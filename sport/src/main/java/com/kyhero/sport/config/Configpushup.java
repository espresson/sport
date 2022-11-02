package com.kyhero.sport.config;


import android.graphics.PointF;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.mlkit.vision.pose.PoseLandmark;
import com.kyhero.sport.communal.MathUtil;
import com.kyhero.sport.communal.SportParam;
import com.kyhero.sport.communal.StepInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Configpushup {

    public static float dis_shoulder_hip_min = 50;
    public static float dis_shoulder_hip = 0;
    public static float disstart = 1f / 7;
    public static float locend = 1f / 2;

    public static float error0 = 1f / 2;
    public static float error1 = 1;
    public static float error2 = 160;


    public static boolean setparams(float i) {
        dis_shoulder_hip = i;
        disstart = dis_shoulder_hip / 20;
        locend = dis_shoulder_hip / 2;

        error0 = dis_shoulder_hip / 2;
        error1 = dis_shoulder_hip / 2;
        error2 = 160;

        Log.w("PushupActivity", "PUSHUP   " + dis_shoulder_hip + " ");
        return dis_shoulder_hip > dis_shoulder_hip_min;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean isInMat(PoseLandmark poseLandmark) {
        PointF point = poseLandmark.getPosition();
        float Likelihood = poseLandmark.getInFrameLikelihood();
        Log.d("isInMat", point.x + " " + point.y + " " + Likelihood);
        if ((SportParam.matstar.x < point.x && point.x < SportParam.matend.x) && (SportParam.matstar.y < point.y && point.y < SportParam.matend.y) && Likelihood > 0.98) {
            return true;
        }
        return false;
    }

    public static boolean isAnticipationOKTop(PoseLandmark Shoulder, PoseLandmark Hip, PoseLandmark Elbow, PoseLandmark Wrist) {
        double angle0 = MathUtil.calAngle(Shoulder.getPosition(), Hip.getPosition(), Elbow.getPosition());
        double angle1 = MathUtil.calAngle(Shoulder.getPosition(), Hip.getPosition(), Wrist.getPosition());
        double angle2 = MathUtil.calAngle(Elbow.getPosition(), Shoulder.getPosition(), Wrist.getPosition());
        Log.d("TAF", "TOP角度 " + angle0 + " " + angle1 + " " + angle2);
        if (Math.abs(angle0 - 70) < 40 && Math.abs(angle1 - 70) < 40 && Math.abs(angle2 - 180) < 40) {
            return true;
        }
        return false;
    }

    public static boolean isAnticipationOKBottom(PoseLandmark Hip, PoseLandmark Shoulder, PoseLandmark Knee, PoseLandmark Ankle) {
        double angle0 = MathUtil.calAngle(Hip.getPosition(), Shoulder.getPosition(), Knee.getPosition());
        double angle1 = MathUtil.calAngle(Hip.getPosition(), Shoulder.getPosition(), Ankle.getPosition());
        double angle2 = MathUtil.calAngle(Knee.getPosition(), Hip.getPosition(), Ankle.getPosition());
        Log.d("TAF", "BOT角度 " + angle0 + " " + angle1 + " " + angle2);
        if (Math.abs(angle0 - 180) < 40 && Math.abs(angle1 - 180) < 40 && Math.abs(angle2 - 180) < 30) {
            return true;
        }
        return false;
    }


    public static synchronized float[] SquarewaveFilter(List<Float> signal, List<Boolean> resultdst,
                                                        List<Boolean> resultF0, List<Boolean> resultF1, List<Boolean> resultF2,
                                                        List<Boolean> resultR0, List<Boolean> resultR1, List<Boolean> resultR2,
                                                        List<Boolean> resultFF0, List<Boolean> resultFF1, List<Boolean> resultFF2,
                                                        List<Boolean> resultRR0, List<Boolean> resultRR1, List<Boolean> resultRR2) {

        float[] outData = new float[signal.size() + 4];

        List<Float> listmin = new ArrayList<>();
        List<Float> listmax = new ArrayList<>();

        List<Integer> listminindex = new ArrayList<>();
        List<Integer> listmaxindex = new ArrayList<>();

        float distance = dis_shoulder_hip / 4;
        float temphigh = signal.get(0), templow = signal.get(0);
        int temphighindex = 0, templowindex = 0, tempflag = 0;
        boolean temhighflag = true, templowflag = true;

//        Log.d("TAD", signal.size() + " : " + signal.toString());
        for (int i = 1; i < signal.size() - 1; i++) {

            if (signal.get(i - 1) > signal.get(i) && signal.get(i) < signal.get(i + 1)) {
                templow = signal.get(i);
                templowindex = i;
                templowflag = true;
            }

            if (signal.get(i - 1) < signal.get(i) && signal.get(i) > signal.get(i + 1)) {
                temphigh = signal.get(i);
                temphighindex = i;
                temhighflag = true;
            }

            if (templowflag && signal.get(i) - templow > distance) {
                templowflag = false;
                if (tempflag == 0) {
                    continue;
                }
                listmin.add(templow);
                listminindex.add(templowindex);

                if (tempflag == 1) {
                    if (listmin.get(listmin.size() - 1) < listmin.get(listmin.size() - 2)) {
                        listmin.remove(listmin.size() - 2);
                        listminindex.remove(listminindex.size() - 2);
                    } else {
                        listmin.remove(listmin.size() - 1);
                        listminindex.remove(listminindex.size() - 1);
                    }
                }
                tempflag = 1;
            }

            if (temhighflag && temphigh - signal.get(i) > distance) {
                temhighflag = false;
                listmax.add(temphigh);
                listmaxindex.add(temphighindex);

                if (tempflag == 2) {
                    if (listmax.get(listmax.size() - 1) > listmax.get(listmax.size() - 2)) {
                        listmax.remove(listmax.size() - 2);
                        listmaxindex.remove(listmaxindex.size() - 2);
                    } else {
                        listmax.remove(listmax.size() - 1);
                        listmaxindex.remove(listmaxindex.size() - 1);
                    }
                }
                tempflag = 2;
            }

            if (i == signal.size() - 2) {
                if (templowflag && listmax.size() > 0) {
                    if (listmax.get(listmax.size() - 1) - templow > distance) {
                        listmin.add(templow);
                        listminindex.add(templowindex);
                    }
                }
                if (temhighflag && listmin.size() > 0) {
                    if (temphigh - listmin.get(listmin.size() - 1) > distance) {
                        listmax.add(temphigh);
                        listmaxindex.add(temphighindex);
                    }
                }
            }

        }

        if (listmin.size() == 0 || listmax.size() == 0) {
            return outData;
        }

        Log.d("TAD", "distance:" + distance + " size: " + listmaxindex.size() + " / " + listminindex.size() + " " + listmaxindex.toString() + listminindex.toString());
        Log.d("TAD", "distance:" + distance + " size: " + listmax.size() + " / " + listmin.size() + " " + listmax.toString() + " " + listmin.toString());

        List<StepInfo> liststep = new ArrayList<>();
        for (int i = 0; i < listmaxindex.size() - 1 && i < listminindex.size(); i++) {
            int Highstart = listmaxindex.get(i);
            int Lowstart = (listmaxindex.get(i) + listminindex.get(i)) / 2;
            int Low = listminindex.get(i);
            int Lowend = (listmaxindex.get(i + 1) + listminindex.get(i)) / 2;
            int Highend = listmaxindex.get(i + 1);

            float Highstart_Level = listmax.get(i);
            float Low_Level = listmin.get(i);
            float Highend_Level = listmax.get(i + 1);

            StepInfo stepInfo = new StepInfo(Highstart, Lowstart, Low, Lowend, Highend, Highstart_Level, Low_Level, Highend_Level, 0);
            liststep.add(stepInfo);
        }
        if (listmaxindex.size() == listminindex.size()) {
            int endindex = listmaxindex.size() - 1;
            int Highstart = listmaxindex.get(endindex);
            int Lowstart = (listmaxindex.get(endindex) + listminindex.get(endindex)) / 2;
            int Low = listminindex.get(endindex);
            int Lowend = signal.size() - 1;
            int Highend = signal.size() - 1;

            float Highstart_Level = listmax.get(endindex);
            float Low_Level = listmin.get(endindex);
            float Highend_Level = listmin.get(endindex);

            StepInfo stepInfo = new StepInfo(Highstart, Lowstart, Low, Lowend, Highend, Highstart_Level, Low_Level, Highend_Level, 0);
            liststep.add(stepInfo);
        }

        if (liststep.size() == 0) {
            return outData;
        }

        //huatu
        for (int j = 0; j < liststep.get(0).Highstart; j++) {
            outData[j] = liststep.get(0).Highstart_Level;
        }
        for (int i = 0; i < liststep.size(); i++) {
            for (int j = liststep.get(i).Highstart; j <= liststep.get(i).Lowstart; j++) {
                outData[j] = liststep.get(i).Highstart_Level;
            }
            for (int j = liststep.get(i).Lowstart + 1; j <= liststep.get(i).Lowend; j++) {
                outData[j] = liststep.get(i).Low_Level;
            }
            for (int j = liststep.get(i).Lowend + 1; j <= liststep.get(i).Highend; j++) {
                outData[j] = liststep.get(i).Highend_Level;
            }
        }

        Log.d("TAD", "1: " + signal.size() + " ----------" + resultdst.size() + " " + resultF0.size());
        for (int i = 0; i < liststep.size(); i++) {

            int highstart = liststep.get(i).getHighstart();
            int lowstart = liststep.get(i).getLowstart();
            int low = liststep.get(i).getLow();
            int lowend = liststep.get(i).getLowend();
            int highend = liststep.get(i).getHighend();

            //手伸直
            for (int j = highstart; j <= highend && j < resultF1.size(); j++) {
                if (resultF1.get(j)/* && resultFF1.get(j)*/) {
                    liststep.get(i).setNOError0(true);
                    break;
                }
            }

            //脚伸直
            for (int j = highstart; j <= highend && j < resultR1.size(); j++) {
                if (!resultR1.get(j)) {
                    liststep.get(i).setNumError1();
                }
//                if (!resultRR1.get(j)) {
//                    liststep.get(i).setNumError11();
//                }
            }
            if (liststep.get(i).getNumError1() <= (highend - highstart) / 3 /*&& liststep.get(i).getNumError11() <= (lowend - highstart) / 2*/) {
                liststep.get(i).setNOError1(true);
            }

            //腰挺直
            for (int j = highstart; j <= highend && j < resultR0.size(); j++) {
                if (!resultR0.get(j)) {
                    liststep.get(i).setNumError11();
                }
            }
            if (liststep.get(i).getNumError11() <= (highend - highstart) / 3) {
                liststep.get(i).setNOError2(true);
            }

            Log.d("TAD", "one> " + i + " / " + highstart + " " + lowstart + " " + low + " " + lowend + " " + highend
                    + " " + liststep.get(i).getNOError0() + " " + liststep.get(i).getNOError1() + "  " + (highend - highstart)
                    + " " + liststep.get(i).NumError1 + "  " + liststep.get(i).NumError11 + " " + liststep.get(i).getNOError2());
        }

        int countok = 0;
        SportParam.LsError0.clear();
        SportParam.LsError1.clear();
        SportParam.LsError2.clear();
        for (int i = 0; i < liststep.size(); i++) {
            if (liststep.get(i).getNoError()) {
                countok++;
            }
            SportParam.LsError0.add(liststep.get(i).getNOError0() | !SportParam.CheckError0);
            SportParam.LsError1.add(liststep.get(i).getNOError1() | !SportParam.CheckError1);
            SportParam.LsError2.add(liststep.get(i).getNOError2() | !SportParam.CheckError2);
        }

        Log.d("TAD", "end: " + countok + " " + liststep.size());
        Collections.sort(listmin);
        outData[signal.size()] = listmin.get(0) + distance;
        outData[signal.size() + 1] = countok;
        outData[signal.size() + 2] = liststep.size() - countok;
        outData[signal.size() + 3] = liststep.size();
        return outData;
    }

    public static boolean dealsignal(List<Float> signal, float[] outData, List<Float> listmin, List<Float> listmax, List<Integer> listminindex, List<Integer> listmaxindex, List<StepInfo> liststep, float distance) {

        float temphigh = signal.get(0), templow = signal.get(0);
        int temphighindex = 0, templowindex = 0, tempflag = 0;
        boolean temhighflag = true, templowflag = true;

        for (int i = 1; i < signal.size() - 1; i++) {

            if (signal.get(i - 1) > signal.get(i) && signal.get(i) < signal.get(i + 1)) {
                templow = signal.get(i);
                templowindex = i;
                templowflag = true;
            }

            if (signal.get(i - 1) < signal.get(i) && signal.get(i) > signal.get(i + 1)) {
                temphigh = signal.get(i);
                temphighindex = i;
                temhighflag = true;
            }

            if (templowflag && signal.get(i) - templow > distance) {
                templowflag = false;
                if (tempflag == 0) {
                    continue;
                }
                listmin.add(templow);
                listminindex.add(templowindex);
                if (tempflag == 1) {
                    if (listmin.get(listmin.size() - 1) < listmin.get(listmin.size() - 2)) {
                        listmin.remove(listmin.size() - 2);
                        listminindex.remove(listminindex.size() - 2);
                    } else {
                        listmin.remove(listmin.size() - 1);
                        listminindex.remove(listminindex.size() - 1);
                    }
                }
                tempflag = 1;
            }

            if (temhighflag && temphigh - signal.get(i) > distance) {
                temhighflag = false;
                listmax.add(temphigh);
                listmaxindex.add(temphighindex);

                if (tempflag == 2) {
                    if (listmax.get(listmax.size() - 1) > listmax.get(listmax.size() - 2)) {
                        listmax.remove(listmax.size() - 2);
                        listmaxindex.remove(listmaxindex.size() - 2);
                    } else {
                        listmax.remove(listmax.size() - 1);
                        listmaxindex.remove(listmaxindex.size() - 1);
                    }
                }
                tempflag = 2;
            }

            if (i == signal.size() - 2) {
                if (templowflag && listmax.size() > 0 && listmax.get(listmax.size() - 1) - templow > distance) {
                    listmin.add(templow);
                    listminindex.add(templowindex);
                    if (tempflag == 1) {
                        if (listmin.get(listmin.size() - 1) < listmin.get(listmin.size() - 2)) {
                            listmin.remove(listmin.size() - 2);
                            listminindex.remove(listminindex.size() - 2);
                        } else {
                            listmin.remove(listmin.size() - 1);
                            listminindex.remove(listminindex.size() - 1);
                        }
                    }
                }
                if (temhighflag && listmin.size() > 0 && temphigh - listmin.get(listmin.size() - 1) > distance) {
                    listmax.add(temphigh);
                    listmaxindex.add(temphighindex);
                    if (tempflag == 2) {
                        if (listmax.get(listmax.size() - 1) > listmax.get(listmax.size() - 2)) {
                            listmax.remove(listmax.size() - 2);
                            listmaxindex.remove(listmaxindex.size() - 2);
                        } else {
                            listmax.remove(listmax.size() - 1);
                            listmaxindex.remove(listmaxindex.size() - 1);
                        }
                    }
                }
            }
        }

        if (listmin.size() == 0 || listmax.size() == 0) {
            return false;
        }
        Log.d("TAD", "distance:" + distance + " size: " + listmaxindex.size() + " / " + listminindex.size() + " " + listmaxindex.toString() + listminindex.toString());
        Log.d("TAD", "distance:" + distance + " size: " + listmax.size() + " / " + listmin.size() + " " + listmax.toString() + " " + listmin.toString());

        for (int i = 0; i < listmaxindex.size() - 1 && i < listminindex.size(); i++) {
            int Highstart = listmaxindex.get(i);
            int Lowstart = (listmaxindex.get(i) + listminindex.get(i)) / 2;
            int Low = listminindex.get(i);
            int Lowend = (listmaxindex.get(i + 1) + listminindex.get(i)) / 2;
            int Highend = listmaxindex.get(i + 1);

            float Highstart_Level = listmax.get(i);
            float Low_Level = listmin.get(i);
            float Highend_Level = listmax.get(i + 1);

            StepInfo stepInfo = new StepInfo(Highstart, Lowstart, Low, Lowend, Highend, Highstart_Level, Low_Level, Highend_Level, 0);
            liststep.add(stepInfo);
        }
        if (listmaxindex.size() == listminindex.size()) {
            int endindex = listmaxindex.size() - 1;
            int Highstart = listmaxindex.get(endindex);
            int Lowstart = (listmaxindex.get(endindex) + listminindex.get(endindex)) / 2;
            int Low = listminindex.get(endindex);
            int Lowend = signal.size() - 1;
            int Highend = signal.size() - 1;

            float Highstart_Level = listmax.get(endindex);
            float Low_Level = listmin.get(endindex);
            float Highend_Level = listmin.get(endindex);

            StepInfo stepInfo = new StepInfo(Highstart, Lowstart, Low, Lowend, Highend, Highstart_Level, Low_Level, Highend_Level, 0);
            liststep.add(stepInfo);
        }

        if (liststep.size() == 0) {
            return false;
        }

        //huatu
        for (int j = 0; j < liststep.get(0).Highstart; j++) {
            outData[j] = liststep.get(0).Highstart_Level;
        }
        for (int i = 0; i < liststep.size(); i++) {
            for (int j = liststep.get(i).Highstart; j <= liststep.get(i).Lowstart; j++) {
                outData[j] = liststep.get(i).Highstart_Level;
            }
            for (int j = liststep.get(i).Lowstart + 1; j <= liststep.get(i).Lowend; j++) {
                outData[j] = liststep.get(i).Low_Level;
            }
            for (int j = liststep.get(i).Lowend + 1; j <= liststep.get(i).Highend; j++) {
                outData[j] = liststep.get(i).Highend_Level;
            }
        }
        return true;
    }


}
