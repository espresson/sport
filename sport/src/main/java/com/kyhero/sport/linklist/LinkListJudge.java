package com.kyhero.sport.linklist;

import java.util.LinkedList;

public class LinkListJudge {

    public int Num=10;
    long time=0;
    long timelag=5000;
    LinkedList<Integer> linkList;


    public LinkListJudge(){
        linkList = new LinkedList<>();
    }

    public LinkListJudge(int Num){
        this.Num=Num;
        linkList = new LinkedList<>();
        for (int i = 0; i < Num; i++) {
            linkList.add(0);
        }
    }
    public LinkListJudge(int Num, int timelag){
        this.Num=Num;
        this.timelag=timelag;
        linkList = new LinkedList<>();
    }


    public void Add(int f){
        if (linkList.size() >= Num) {
            linkList.removeFirst();
        }
        linkList.add(f);
    }

    public void AddFirst(int f){
        if (linkList.size() >= Num) {
            linkList.removeLast();
            linkList.removeLast();
        }
        linkList.addFirst(f);
        linkList.addFirst(f);
    }

    public void Reset(){
        linkList.clear();
        for (int i = 0; i < Num; i++) {
            linkList.add(0);
        }
        time=0;
    }

    public boolean Judge(long t){
        int No = 0;

        for (int i = 0; i < linkList.size(); i++) {
            No += linkList.get(i);
        }
        if(No==Num){
            return Time(t);
        }
        return false;
    }

    public int Judge(){
        int No = 0;
        for (int i = 0; i < linkList.size(); i++) {
            No += linkList.get(i);
        }
        return No;
    }

    public boolean Time(long t){
        if(t - time > timelag){
            time = t;
            return true;
        }else{
            return false;
        }
    }


}
