package com.kyhero.sport.linklist;

import android.graphics.PointF;
import android.util.Log;

import com.kyhero.sport.communal.LimbsStatus;

import java.util.LinkedList;

public class LinkListLimbs {

    int Num = 10;
    LinkedList<LimbsStatus> linkList;

    public LinkListLimbs() {
        linkList = new LinkedList<>();
    }

    public LinkListLimbs(int Num) {
        this.Num = Num;
        linkList = new LinkedList<>();
    }

    public void Add(PointF p0, PointF p1, PointF p2, PointF p3) {
        if (linkList.size() >= Num) {
            linkList.removeFirst();
        }
        linkList.add(new LimbsStatus(p0, p1, p2, p3));
    }

    public int size() {
        return linkList.size();
    }

    public LimbsStatus get(int i) {
        return linkList.get(i);
    }


    public void Reset() {
        linkList.clear();
    }

    public float Error() {

        int alldstl = 0, alldstr = 0;

        for (int i = 0; i < linkList.size(); i++) {
            alldstl += 0;//MathUtil.Distance(linkList.get(0), linkList.get(i));
        }


        Log.d("PULLUP", "Error: " + (alldstl + alldstr) / 2 / (linkList.size() - 1));
        return (alldstl + alldstr) / 2 / (linkList.size() - 1);
    }


}
