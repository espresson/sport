package com.kyhero.sport.communal;

import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MathUtil {

    /**
     * 根据余弦定理计算线段1到线段2的夹角，线段1：起始点到原点，线段2：原点到结束点）
     *
     * @param o 原点
     * @param s 起始点
     * @param e 结束点
     * @return
     */
    public static int calAngle(PointF o, PointF s, PointF e) {
        Double cosfi = 0.0;
        Double fi = 0.0;
        Double norm = 0.0;
        float dsx = s.x - o.x;
        float dsy = s.y - o.y;
        float dex = e.x - o.x;
        double dey = e.y - o.y;
        cosfi = dsx * dex + dsy * dey;
        norm = (dsx * dsx + dsy * dsy) * (dex * dex + dey * dey);
        cosfi /= Math.sqrt(norm);
        if (cosfi >= 1.0) return 0;
        if (cosfi <= -1.0) return (int) (Math.PI);
        fi = Math.acos(cosfi);
        if (180 * fi / Math.PI < 180) {
            return (int) (180 * fi / Math.PI);
        } else {
            return (int) (360 - 180 * fi / Math.PI);
        }
    }


    public static synchronized List IIRFilter(List signal, double[] a, double[] b) {

        float[] in = new float[b.length];
        float[] out = new float[a.length - 1];

        List<Float> outData = new ArrayList<>();

        for (int i = 0; i < signal.size(); i++) {

            System.arraycopy(in, 0, in, 1, in.length - 1);
            in[0] = (float) signal.get(i);

            //calculate y based on a and b coefficients
            //and in and out.
            float y = 0;
            for (int j = 0; j < b.length; j++) {
                y += b[j] * in[j];
            }

            for (int j = 0; j < a.length - 1; j++) {
                y -= a[j + 1] * out[j];
            }

            //shift the out array
            System.arraycopy(out, 0, out, 1, out.length - 1);
            out[0] = y;
            if (i >= 5) {
                outData.add(y);
            }
        }

        for (int i = 0; i < signal.size() - 10 - 1; i++) {
            if (outData.get(i) > outData.get(i + 1)) {
                for (int j = 0; j < i; j++) {
                    outData.set(j, outData.get(i));
                }
                break;
            }
        }
        return outData;
    }

    //两点间距离
    public static float Distance(PointF p1, PointF p2) {//两点间距离
        float a, d;
        a = ((p1.x - p2.x) * (p1.x - p2.x)) + ((p1.y - p2.y) * (p1.y - p2.y));
        d = (float) Math.sqrt(a);
        return d;
    }

    public static float VerticalLine(PointF p0, PointF p1, PointF p2) {//p0到 p1/p2两点间垂线距离
        float A, B, C, D;
        A = p2.y - p1.y;
        B = p1.x - p2.x;
        C = p2.x * p1.y - p1.x * p2.y;

        D = Math.abs(A * p0.x + B * p0.y + C) / (float) Math.sqrt(A * A + B * B);

        if (A * p0.x + B * p0.y + C < 0) {
            return -D;
        }
        return D;
    }

    public static float Position(PointF p1) {
        float a, d;
        a = ((p1.x * p1.x) + (p1.y * p1.y));
        d = (float) Math.sqrt(a);
        return d;
    }

    //标准差σ=sqrt(s^2)
    public static float StandardDiviation(List<Float> signal) {
        int m = signal.size();
        float sum = 0;
        for (int i = 0; i < m; i++) {//求和
            sum += signal.get(i);
        }
        float dAve = sum / m;//求平均值
        float dVar = 0;
        for (int i = 0; i < m; i++) {//求方差
            dVar += (signal.get(i) - dAve) * (signal.get(i) - dAve);
        }
        //reture Math.sqrt(dVar/(m-1));
        return (float) Math.sqrt(dVar / m);
    }

    public static synchronized List NumericComparison(List signal, float num) {
        List<Boolean> outData = new ArrayList<>();
        for (int i = 0; i < signal.size(); i++) {
            if ((float) signal.get(i) > num) {
                outData.add(true);
            } else {
                outData.add(false);
            }
        }
        return outData;
    }


    public static boolean RegressionEquation(LinkedList<PointF> points, int dst) {

        int[] angles = new int[2];

        int angle1, angle2;
        float meanx = 0, meany = 0;
        float sigmax = 0, sigmaxy = 0;
        for (int i = 0; i < points.size(); i++) {
            meanx += points.get(i).x;
            meany += points.get(i).y;
            sigmax += points.get(i).x * points.get(i).x;
            sigmaxy += points.get(i).x * points.get(i).y;
        }
        meanx /= points.size();
        meany /= points.size();


        float Coefficient_B = (sigmaxy - 4 * meanx * meany) / (sigmax - 4 * meanx * meanx);
        int angle = (int) Math.toDegrees(Math.atan(Coefficient_B));


        angle1 = angle;
        angle2 = angle;

        float dstx = points.get(points.size() - 1).x - points.get(0).x;
        float dsty = points.get(points.size() - 1).y - points.get(0).y;
        Log.d("CCCC", "dst: " + (int) points.get(points.size() - 1).x + " " + (int) points.get(points.size() - 1).y + " " + (int) dst + " " + (int) -dsty);
        if (-dsty < dst) {
            return false;
        }

        boolean x_ = dstx < 0;
        boolean y_ = dsty < 0;

        if (x_) {
            angle1 += 180;
        } else if (y_) {
            angle1 += 360;
        }

        if (x_) {
            angle2 += 270;
        } else {
            angle2 += 90;
        }

        angles[0] = angle1;
        angles[1] = angle2;

        Log.d("CCCC", "ang: " + Coefficient_B + " " + angle1 + " " + angle2);
        return Math.abs(angle1 - 270) < 20;

    }

    public static boolean RegressionEquation_pushup(LinkedList<PointF> points, int dst) {

        int[] angles = new int[2];

        int angle1, angle2;
        float meanx = 0, meany = 0;
        float sigmax = 0, sigmaxy = 0;
        for (int i = 0; i < points.size(); i++) {
            meanx += points.get(i).x;
            meany += points.get(i).y;
            sigmax += points.get(i).x * points.get(i).x;
            sigmaxy += points.get(i).x * points.get(i).y;
        }
        meanx /= points.size();
        meany /= points.size();

        float Coefficient_B = (sigmaxy - 4 * meanx * meany) / (sigmax - 4 * meanx * meanx);

        float dstx = points.get(points.size() - 1).x - points.get(0).x;
        float dsty = points.get(points.size() - 1).y - points.get(0).y;
        Log.d("TGG", ">>>> " + (int) points.get(points.size() - 1).x + " " + (int) points.get(points.size() - 1).y + " " + (int) dst + " " + (int) dsty);
        if (dsty < dst) {
            return false;
        }

        boolean x_ = dstx < 0;
        boolean y_ = dsty < 0;

        int angle = (int) Math.toDegrees(Math.atan(Coefficient_B));

        angle1 = angle;
        angle2 = angle;

        if (x_) {
            angle1 += 180;
        } else if (y_) {
            angle1 += 360;
        }

        if (x_) {
            angle2 += 270;
        } else {
            angle2 += 90;
        }

        angles[0] = angle1;
        angles[1] = angle2;

        Log.d("TGG", "RegressionEquation: " + angle1 + " " + angle2 + " " + Coefficient_B);
        return Math.abs(angle1 - 90) < 30;

    }


}
