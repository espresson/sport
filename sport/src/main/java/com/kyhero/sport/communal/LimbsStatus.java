package com.kyhero.sport.communal;

import android.graphics.PointF;

public class LimbsStatus {

    PointF point0;
    PointF point1;
    PointF point2;
    PointF point3;

    int Angle012=0;
    int Angle123=0;
    float distance13=0;

    public LimbsStatus(PointF point0, PointF point1, PointF point2, PointF point3) {
        this.point0=point0;
        this.point1=point1;
        this.point2=point2;
        this.point3=point3;

        Angle012=MathUtil.calAngle(point1,point0,point2);
        Angle123= MathUtil.calAngle(point2,point1,point3);
        distance13=MathUtil.Distance(point1,point3);
    }

    public PointF getPoint0() {
        return point0;
    }

    public void setPoint0(PointF point0) {
        this.point0 = point0;
    }

    public PointF getPoint1() {
        return point1;
    }

    public void setPoint1(PointF point1) {
        this.point1 = point1;
    }

    public PointF getPoint2() {
        return point2;
    }

    public void setPoint2(PointF point2) {
        this.point2 = point2;
    }

    public PointF getPoint3() {
        return point3;
    }

    public void setPoint3(PointF point3) {
        this.point3 = point3;
    }

    public int getAngle012() {
        return Angle012;
    }

    public void setAngle012(int angle012) {
        Angle012 = angle012;
    }

    public int getAngle123() {
        return Angle123;
    }

    public void setAngle123(int angle123) {
        Angle123 = angle123;
    }

    public float getDistance13() {
        return distance13;
    }

    public void setDistance13(float distance13) {
        this.distance13 = distance13;
    }

}
