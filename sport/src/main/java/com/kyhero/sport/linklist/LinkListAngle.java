package com.kyhero.sport.linklist;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LinkListAngle {

    int Num = 10;
    LinkedList<Integer> linkList1;
    LinkedList<Integer> linkList2;
    LinkedList<Integer> linkList3;

    LinkedList<Integer> linkList1r;
    LinkedList<Integer> linkList2r;
    LinkedList<Integer> linkList3r;

    public LinkListAngle() {
        linkList1 = new LinkedList<>();
        linkList2 = new LinkedList<>();
        linkList3 = new LinkedList<>();

        linkList1r = new LinkedList<>();
        linkList2r = new LinkedList<>();
        linkList3r = new LinkedList<>();

    }

    public LinkListAngle(int Num) {
        this.Num = Num;
        linkList1 = new LinkedList<>();
        linkList2 = new LinkedList<>();
        linkList3 = new LinkedList<>();

        linkList1r = new LinkedList<>();
        linkList2r = new LinkedList<>();
        linkList3r = new LinkedList<>();

    }

    public void Add(int i1, int i2, int i3,int i1r, int i2r, int i3r) {
        if (linkList1.size() >= Num) {
            linkList1.removeFirst();
            linkList2.removeFirst();
            linkList3.removeFirst();

            linkList1r.removeFirst();
            linkList2r.removeFirst();
            linkList3r.removeFirst();
        }
        linkList1.add(i1);
        linkList2.add(i2);
        linkList3.add(i3);

        linkList1r.add(i1r);
        linkList2r.add(i2r);
        linkList3r.add(i3r);

    }

    public int size() {
        return linkList1.size();
    }

    public int get1(int i) {
        return linkList1.get(i);
    }

    public int get2(int i) {
        return linkList2.get(i);
    }

    public int get3(int i) {
        return linkList3.get(i);
    }

    public int get1r(int i) {
        return linkList1r.get(i);
    }

    public int get2r(int i) {
        return linkList2r.get(i);
    }

    public int get3r(int i) {
        return linkList3r.get(i);
    }

    public void Reset() {
        linkList1.clear();
        linkList2.clear();
        linkList3.clear();

        linkList1r.clear();
        linkList2r.clear();
        linkList3r.clear();

    }

    public float Numerical() {
        List<PointF> signal = new ArrayList<>();
        /*for (int i = 0; i < linkListR.size(); i++) {
            signal.add(linkListR.get(i));
        }*/
        return 0;
    }


}
