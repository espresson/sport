package com.kyhero.sport.linklist;

import android.graphics.PointF;
import android.util.Log;

import com.kyhero.sport.communal.MathUtil;

import java.util.LinkedList;

public class LinkListPos {

    public int Num = 10;
    float Avrdst = 0;
    LinkedList<PointF> linkListL;
    LinkedList<PointF> linkListR;
    LinkedList<Float> linkListD;
    LinkedList<Float> linkListEL;
    LinkedList<Float> linkListER;
    LinkedList<Float> linkListED;


    public LinkListPos() {
        linkListL = new LinkedList<>();
        linkListR = new LinkedList<>();
        linkListD = new LinkedList<>();
        linkListEL = new LinkedList<>();
        linkListER = new LinkedList<>();
        linkListED = new LinkedList<>();
    }

    public LinkListPos(int Num) {
        this.Num = Num;
        linkListL = new LinkedList<>();
        linkListR = new LinkedList<>();
        linkListD = new LinkedList<>();
        linkListEL = new LinkedList<>();
        linkListER = new LinkedList<>();
        linkListED = new LinkedList<>();
    }

    public void Add(PointF l, PointF r) {
        if (linkListL.size() >= Num) {
            linkListL.removeFirst();
            linkListR.removeFirst();
            linkListD.removeFirst();

            linkListEL.removeFirst();
            linkListER.removeFirst();
            linkListED.removeFirst();
        }
        linkListL.add(l);
        linkListR.add(r);
        linkListD.add(MathUtil.Distance(l, r));
        Avrdst = getAvrdst();

        linkListEL.add(ErrorL());
        linkListER.add(ErrorR());
        linkListED.add(ErrorD());
    }

    public int size() {
        return linkListL.size();
    }

    public float getEL(int i) {
        return linkListEL.get(i);
    }

    public float getER(int i) {
        return linkListER.get(i);
    }

    public float getED(int i) {
        return linkListED.get(i);
    }

    public float getAvrdst() {
        float alldst = 0;
        for (int i = 0; i < linkListD.size(); i++) {
            alldst += linkListD.get(i);
        }
        return alldst / linkListD.size();
    }

    public void Reset() {
        linkListL.clear();
        linkListR.clear();
        linkListD.clear();
        Avrdst = 0;
    }

    public float ErrorL() {
        float alldst = 0, cx = 0, cy = 0;
        for (int i = 0; i < linkListL.size(); i++) {
            cx += linkListL.get(i).x;
            cy += linkListL.get(i).y;
        }
        cx /= linkListL.size();
        cy /= linkListL.size();
        PointF pointFc = new PointF(cx, cy);
        for (int i = 0; i < linkListL.size(); i++) {
            alldst += MathUtil.Distance(linkListL.get(0), pointFc);
        }
        Log.d("TAG", "Position ErrorL: " + alldst + " " + Avrdst + " " + alldst / Avrdst);

        return alldst / Avrdst;
    }

    public float ErrorR() {
        float alldst = 0, cx = 0, cy = 0;
        for (int i = 0; i < linkListR.size(); i++) {
            cx += linkListR.get(i).x;
            cy += linkListR.get(i).y;
        }
        cx /= linkListR.size();
        cy /= linkListR.size();
        PointF pointFc = new PointF(cx, cy);
        for (int i = 0; i < linkListR.size(); i++) {
            alldst += MathUtil.Distance(linkListR.get(0), pointFc);
        }
        Log.d("TAG", "Position ErrorR: " + alldst + " " + Avrdst + " " + alldst / Avrdst);
        return alldst / Avrdst;
    }


    public PointF AvgL() {
        float cx = 0, cy = 0;
        for (int i = 0; i < linkListL.size(); i++) {
            cx += linkListL.get(i).x;
            cy += linkListL.get(i).y;
        }
        cx /= linkListL.size();
        cy /= linkListL.size();
        PointF pointFc = new PointF(cx, cy);
        return pointFc;
    }

    public PointF AvgR() {
        float  cx = 0, cy = 0;
        for (int i = 0; i < linkListR.size(); i++) {
            cx += linkListR.get(i).x;
            cy += linkListR.get(i).y;
        }
        cx /= linkListR.size();
        cy /= linkListR.size();
        PointF pointFc = new PointF(cx, cy);
        return pointFc;
    }


    public float ErrorD() {
        float Avrdstd = MathUtil.StandardDiviation(linkListD);
        Log.d("TAG", "Position ErrorD: " + Avrdstd + " " + Avrdst + " " + Avrdstd / Avrdst);
        return Avrdstd / Avrdst;
    }

}
