package com.kyhero.sport.linklist;

import android.graphics.PointF;

import com.kyhero.sport.communal.MathUtil;

import java.util.LinkedList;

public class LinkListPointF {

    public int Num = 4;
    LinkedList<PointF> linkList;

    public LinkListPointF() {
        linkList = new LinkedList<>();
    }

    public LinkListPointF(int Num) {
        this.Num = Num;
        linkList = new LinkedList<>();
    }


    public void Add(PointF f) {
        if (linkList.size() >= Num) {
            linkList.removeFirst();
        }
        linkList.add(f);
    }

    public void AddFirst(PointF f) {
        if (linkList.size() >= Num) {
            linkList.removeLast();
        }
        linkList.addFirst(f);
    }

    public void Reset() {
        linkList.clear();
    }

    public PointF Center() {
        float  cx = 0, cy = 0;
        for (int i = 0; i < linkList.size(); i++) {
            cx += linkList.get(i).x;
            cy += linkList.get(i).y;
        }
        cx /= linkList.size();
        cy /= linkList.size();
        PointF pointFc = new PointF(cx, cy);
        return pointFc;
    }

    public boolean Judge(int dst) {
        if (linkList.size() < Num) {
            return false;
        }
        return MathUtil.RegressionEquation(linkList, dst);
    }

    public boolean Judge_Pushup(int dst) {
        if (linkList.size() < Num) {
            return false;
        }
        return MathUtil.RegressionEquation_pushup(linkList, dst);
    }


}
