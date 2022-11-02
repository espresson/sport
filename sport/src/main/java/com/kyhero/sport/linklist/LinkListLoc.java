package com.kyhero.sport.linklist;

import android.graphics.PointF;
import android.util.Log;

import com.kyhero.sport.communal.MathUtil;

import java.util.LinkedList;

public class LinkListLoc {

    int Num = 10;
    LinkedList<PointF> linkListL;
    LinkedList<PointF> linkListR;

    public LinkListLoc() {
        linkListL = new LinkedList<>();
        linkListR = new LinkedList<>();
    }

    public LinkListLoc(int Num) {
        this.Num = Num;
        linkListL = new LinkedList<>();
        linkListR = new LinkedList<>();
    }

    public void Add(PointF l, PointF r) {
        if (linkListL.size() >= Num) {
            linkListL.removeFirst();
            linkListR.removeFirst();
        }
        linkListL.add(l);
        linkListR.add(r);
    }

    public int size() {
        return linkListL.size();
    }

    public PointF getL(int i) {
        return linkListL.get(i);
    }

    public PointF getR(int i) {
        return linkListR.get(i);
    }

    public void Reset() {
        linkListL.clear();
        linkListR.clear();
    }

    public float Error() {

        int alldstl = 0,alldstr = 0;

        for (int i = 0; i < linkListL.size(); i++) {
            alldstl += MathUtil.Distance(linkListL.get(0), linkListL.get(i));
        }

        for (int i = 1; i < linkListR.size(); i++) {
            alldstr += MathUtil.Distance(linkListR.get(0), linkListR.get(i));
        }
        Log.d("PULLUP", "Error: "+(alldstl+alldstr)/2/(linkListL.size()-1));
        return (alldstl+alldstr)/2/(linkListL.size()-1);
    }


}
