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

public class Configsitup {

    public static float dis_shoulder_hip_min = 100;
    public static float dis_shoulder_hip = 0;
    public static float dis_ear_knee = 0;

    public static float dis_index_ear_l = 0;
    public static float dis_index_ear_r = 0;

    public static float disstart = 1f / 5;
    public static float locend = 1f / 2;

    public static float error0 = 1f / 2;
    public static float error1 = 1;
    public static float error2 = 160;


    public static void setparams() {
        disstart = dis_shoulder_hip / 5;
        locend = dis_shoulder_hip / 2;

        error0 = dis_shoulder_hip * 2 / 3;
        error1 = dis_shoulder_hip / 2;
        error2 = 160;
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

    public static boolean isAnticipationOK(PoseLandmark Shoulder, PoseLandmark Hip, PoseLandmark Knee, PoseLandmark Ankle) {
        double angle0 = MathUtil.calAngle(Hip.getPosition(), Shoulder.getPosition(), Knee.getPosition());
        double angle1 = MathUtil.calAngle(Knee.getPosition(), Hip.getPosition(), Ankle.getPosition());
        double angle2 = MathUtil.calAngle(Hip.getPosition(), Shoulder.getPosition(), Ankle.getPosition());

        double angle3 = MathUtil.calAngle(Hip.getPosition(), Shoulder.getPosition(), new PointF(Shoulder.getPosition().x, Hip.getPosition().y)); //平躺判断

        Log.d("TAF", "角度 " + angle0 + " " + angle1 + " " + angle2 + " " + angle3);
        if (Math.abs(angle0 - 135) < 40 && Math.abs(angle2 - 180) < 80 && Math.abs(angle3) < 50) {
            return true;
        }
        return false;
    }

    public static synchronized float[] SquarewaveFilter(List<Float> signal, List<Float> error0, List<Float> error1, List<Float> error11, List<Float> error2) {

        float[] outData = new float[signal.size() + 4];

        List<Float> listmin = new ArrayList<>();
        List<Float> listmax = new ArrayList<>();
        List<Integer> listminindex = new ArrayList<>();
        List<Integer> listmaxindex = new ArrayList<>();
        List<StepInfo> liststep = new ArrayList<>();
        float distance = dis_shoulder_hip * 3 / 5;

        if (!dealsignal(signal, outData, listmin, listmax, listminindex, listmaxindex, liststep, distance)) {
            return outData;
        }

        Log.d("TAD", "1:" + signal.size() + " " + error0.size() + " " + error1.size() + " " + error2.size() + " ");
        for (int i = 0; i < liststep.size(); i++) {
            int lowstart = liststep.get(i).getLowstart();
            int highstart = liststep.get(i).getHighstart();
            int high = liststep.get(i).getHigh();
            int highend = liststep.get(i).getHighend();
            int lowend = liststep.get(i).getLowend();

            //High 判断肩触垫
            for (int j = lowstart; j <= lowend && j < error2.size(); j++) {
                if (error2.get(j) == 1) {
                    liststep.get(i).setNOError2(true);
                    break;
                }
            }

            // 判断手触耳
            for (int j = lowstart; j <= high && j < error1.size(); j++) {

                if (error1.get(j) == 1) {
                    liststep.get(i).setNumError1();
                }

                if (error11.get(j) == 1) {
                    liststep.get(i).setNumError11();
                }
            }

            if (liststep.get(i).getNumError1() <= (high - lowstart) / 4 && liststep.get(i).getNumError11() <= (high - lowstart) * 3 / 4) {
                liststep.get(i).setNOError1(true);
            }

            //High 判断肘触膝
            for (int k = highstart; k <= highend && k < error0.size(); k++) {
                if (error0.get(k) == 0) {
                    liststep.get(i).setNOError0(true);
                    break;
                }
            }

            Log.d("TAD", "one> " + i + " / " + lowstart + " " + highstart + " " + high + " " + highend + " " + lowend
                    + " " + liststep.get(i).getNOError0() + " " + liststep.get(i).getNOError1() + "  " + (lowend - highstart) + " " + liststep.get(i).NumError1
                    + "  " + liststep.get(i).NumError11 + " " + liststep.get(i).getNOError2());
        }


        int countok = 0;
        SportParam.LsError0.clear();
        SportParam.LsError1.clear();
        SportParam.LsError2.clear();
        for (int i = 0; i < liststep.size(); i++) {
            if (liststep.get(i).getNoError() || SportParam.SportDect) {
                countok++;
            }
            SportParam.LsError0.add(liststep.get(i).getNOError0() | !SportParam.CheckError0 || SportParam.SportDect);
            SportParam.LsError1.add(liststep.get(i).getNOError1() | !SportParam.CheckError1 || SportParam.SportDect);
            SportParam.LsError2.add(liststep.get(i).getNOError2() | !SportParam.CheckError2 || SportParam.SportDect);
        }

        Log.d("TAD", "3:" + countok + " " + liststep.size());
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

        Log.d("TADD", "dis_shoulder:" + dis_shoulder_hip + " size:" + listmin.size() + " / " + listmax.size() + " " + listminindex.toString() + " " + listmaxindex.toString());

        for (int i = 0; i < listminindex.size() - 1 && i < listmaxindex.size(); i++) {
            int Lowstart = listminindex.get(i);
            int Highstart = (listminindex.get(i) + listmaxindex.get(i)) / 2;
            int High = listmaxindex.get(i);
            int Highend = (listminindex.get(i + 1) + listmaxindex.get(i)) / 2;
            int Lowend = listminindex.get(i + 1);

            float Lowstart_Level = listmin.get(i);
            float High_Level = listmax.get(i);
            float Lowend_Level = listmin.get(i + 1);

            StepInfo stepInfo = new StepInfo(Lowstart, Highstart, High, Highend, Lowend, Lowstart_Level, High_Level, Lowend_Level);
            liststep.add(stepInfo);
        }

        if (listminindex.size() == listmaxindex.size()) {
            int endindex = listminindex.size() - 1;
            int Lowstart = listminindex.get(endindex);
            int Highstart = (listminindex.get(endindex) + listmaxindex.get(endindex)) / 2;
            int High = listmaxindex.get(endindex);
            int Highend = signal.size() - 1;
            int Lowend = signal.size() - 1;

            float Lowstart_Level = listmin.get(endindex);
            float High_Level = listmax.get(endindex);
            float Lowend_Level = listmax.get(endindex);

            StepInfo stepInfo = new StepInfo(Lowstart, Highstart, High, Highend, Lowend, Lowstart_Level, High_Level, Lowend_Level);
            liststep.add(stepInfo);
        }

        if (liststep.size() == 0) {
            return false;
        }

        for (int j = 0; j < liststep.get(0).Lowstart; j++) {
            outData[j] = liststep.get(0).Lowstart_Level;
        }
        for (int i = 0; i < liststep.size(); i++) {
            for (int j = liststep.get(i).Lowstart; j <= liststep.get(i).Highstart; j++) {
                outData[j] = liststep.get(i).Lowstart_Level;
            }
            for (int j = liststep.get(i).Highstart + 1; j <= liststep.get(i).Highend; j++) {
                outData[j] = liststep.get(i).High_Level;
            }
            for (int j = liststep.get(i).Highend + 1; j <= liststep.get(i).Lowend; j++) {
                outData[j] = liststep.get(i).Lowend_Level;
            }
        }
        return true;
    }


    public static synchronized float[] SquarewaveFilter1(List<Float> signal, List<Float> error0, List<Float> error1, List<Float> error11, List<Float> error2) {

        float[] outData = new float[signal.size() + 4];


        List<Float> listmin = new ArrayList<>();
        List<Float> listmax = new ArrayList<>();

        List<Integer> listminindex = new ArrayList<>();
        List<Integer> listmaxindex = new ArrayList<>();

        float distance = dis_shoulder_hip * 3 / 5;
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

        Log.d("TADD", "dis_shoulder:" + dis_shoulder_hip + " size:" + listmin.size() + " / " + listmax.size() + " " + listminindex.toString() + " " + listmaxindex.toString());

        List<StepInfo> liststep = new ArrayList<>();
        for (int i = 0; i < listminindex.size() - 1 && i < listmaxindex.size(); i++) {
            int Lowstart = listminindex.get(i);
            int Highstart = (listminindex.get(i) + listmaxindex.get(i)) / 2;
            int High = listmaxindex.get(i);
            int Highend = (listminindex.get(i + 1) + listmaxindex.get(i)) / 2;
            int Lowend = listminindex.get(i + 1);

            float Lowstart_Level = listmin.get(i);
            float High_Level = listmax.get(i);
            float Lowend_Level = listmin.get(i + 1);

            StepInfo stepInfo = new StepInfo(Lowstart, Highstart, High, Highend, Lowend, Lowstart_Level, High_Level, Lowend_Level);
            liststep.add(stepInfo);
        }

        if (listminindex.size() == listmaxindex.size()) {
            int endindex = listminindex.size() - 1;
            int Lowstart = listminindex.get(endindex);
            int Highstart = (listminindex.get(endindex) + listmaxindex.get(endindex)) / 2;
            int High = listmaxindex.get(endindex);
            int Highend = signal.size() - 1;
            int Lowend = signal.size() - 1;

            float Lowstart_Level = listmin.get(endindex);
            float High_Level = listmax.get(endindex);
            float Lowend_Level = listmax.get(endindex);

            StepInfo stepInfo = new StepInfo(Lowstart, Highstart, High, Highend, Lowend, Lowstart_Level, High_Level, Lowend_Level);
            liststep.add(stepInfo);
        }

        if (liststep.size() == 0) {
            return outData;
        }

        for (int j = 0; j < liststep.get(0).Lowstart; j++) {
            outData[j] = liststep.get(0).Lowstart_Level;
        }
        for (int i = 0; i < liststep.size(); i++) {
            for (int j = liststep.get(i).Lowstart; j <= liststep.get(i).Highstart; j++) {
                outData[j] = liststep.get(i).Lowstart_Level;
            }
            for (int j = liststep.get(i).Highstart + 1; j <= liststep.get(i).Highend; j++) {
                outData[j] = liststep.get(i).High_Level;
            }
            for (int j = liststep.get(i).Highend + 1; j <= liststep.get(i).Lowend; j++) {
                outData[j] = liststep.get(i).Lowend_Level;
            }
        }


        Log.d("TAD", "1:" + signal.size() + " " + error0.size() + " " + error1.size() + " " + error2.size() + " ");
        for (int i = 0; i < liststep.size(); i++) {
            int lowstart = liststep.get(i).getLowstart();
            int highstart = liststep.get(i).getHighstart();
            int high = liststep.get(i).getHigh();
            int highend = liststep.get(i).getHighend();
            int lowend = liststep.get(i).getLowend();

            //High 判断肩触垫
            for (int j = lowstart; j <= lowend && j < error2.size(); j++) {
                if (error2.get(j) == 1) {
                    liststep.get(i).setNOError2(true);
                    break;
                }
            }

            // 判断手触耳
            for (int j = lowstart; j <= high && j < error1.size(); j++) {

                if (error1.get(j) == 1) {
                    liststep.get(i).setNumError1();
                }

                if (error11.get(j) == 1) {
                    liststep.get(i).setNumError11();
                }
            }

            if (liststep.get(i).getNumError1() <= (high - lowstart) / 4 && liststep.get(i).getNumError11() <= (high - lowstart) * 3 / 4) {
                liststep.get(i).setNOError1(true);
            }

            //High 判断肘触膝
            for (int k = highstart; k <= highend && k < error0.size(); k++) {
                if (error0.get(k) == 0) {
                    liststep.get(i).setNOError0(true);
                    break;
                }
            }

            Log.d("TAD", "one> " + i + " / " + lowstart + " " + highstart + " " + high + " " + highend + " " + lowend
                    + " " + liststep.get(i).getNOError0() + " " + liststep.get(i).getNOError1() + "  " + (lowend - highstart) + " " + liststep.get(i).NumError1
                    + "  " + liststep.get(i).NumError11 + " " + liststep.get(i).getNOError2());
        }


        int countok = 0;
        SportParam.LsError0.clear();
        SportParam.LsError1.clear();
        SportParam.LsError2.clear();
        for (int i = 0; i < liststep.size(); i++) {
            if (liststep.get(i).getNoError() || SportParam.SportDect) {
                countok++;
            }
            SportParam.LsError0.add(liststep.get(i).getNOError0() | !SportParam.CheckError0 || SportParam.SportDect);
            SportParam.LsError1.add(liststep.get(i).getNOError1() | !SportParam.CheckError1 || SportParam.SportDect);
            SportParam.LsError2.add(liststep.get(i).getNOError2() | !SportParam.CheckError2 || SportParam.SportDect);
        }

        Log.d("TAD", "3:" + countok + " " + liststep.size());
        Collections.sort(listmin);
        outData[signal.size()] = listmin.get(0) + distance;
        outData[signal.size() + 1] = countok;
        outData[signal.size() + 2] = liststep.size() - countok;
        outData[signal.size() + 3] = liststep.size();
        return outData;
    }


}
