package com.kyhero.sport.linklist;

import com.kyhero.sport.communal.MathUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LinkListNumerical {

    public int Num = 20;
    LinkedList<Float> linkList;

    public LinkListNumerical() {
        linkList = new LinkedList<>();
    }

    public LinkListNumerical(int Num) {
        this.Num = Num;
        linkList = new LinkedList<>();
    }

    public void Add(Float f) {
        if (linkList.size() >= Num) {
            linkList.removeFirst();
        }
        linkList.add(f);
    }

    public int size() {
        return linkList.size();
    }

    public float get(int i) {
        return linkList.get(i);
    }

    public void Reset() {
        linkList.clear();
    }

    public float Numerical() {
        List<Float> signal = new ArrayList<>();
        for (int i = 0; i < linkList.size(); i++) {
            signal.add(linkList.get(i));
        }
        return MathUtil.StandardDiviation(signal);
    }

    public float Average() {
        float average = 0;
        for (int i = 0; i < linkList.size(); i++) {
            average += linkList.get(i);
        }
        return average / linkList.size();
    }

    public float Numericalend5() {
        if (linkList.size() < 5) {
            return 0;
        }
        List<Float> signal = new ArrayList<>();
        for (int i = linkList.size() - 5; i < linkList.size(); i++) {
            signal.add(linkList.get(i));
        }
        return MathUtil.StandardDiviation(signal);
    }


    public int printlist() {
        int[] signal = new int[linkList.size()];
        int all = 0;
        for (int i = 0; i < linkList.size(); i++) {
            signal[i] = (int) ((float) linkList.get(i));
            all += (int) ((float) linkList.get(i));
        }
//        Log.d("printlist", all / linkList.size() + " " + Arrays.toString(signal));
        return all / linkList.size();
    }

}
