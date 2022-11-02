package com.kyhero.sport.communal;

public class StepInfo {

    public int Highstart;
    public int High;
    public int Highend;
    public int Lowstart;
    public int Low;
    public int Lowend;

    public float Lowstart_Level;
    public float High_Level;
    public float Lowend_Level;

    public float Highstart_Level;
    public float Low_Level;
    public float Highend_Level;

    boolean NOError0 = false;
    boolean NOError0_0 = false;
    boolean NOError0_1 = false;

    boolean NOError1 = false;
    public int NumError1 = 0;
    public int NumError11 = 0;

    boolean NOError2 = false;

    public StepInfo(int Highstart, int High, int Highend, int Lowstart, int Low, int Lowend) {
        this.Highstart = Highstart;
        this.High = High;
        this.Highend = Highend;
        this.Lowstart = Lowstart;
        this.Low = Low;
        this.Lowend = Lowend;
    }

    public StepInfo(int Lowstart, int Highstart, int High, int Highend, int Lowend, float Lowstart_Level, float High_Level, float Lowend_Level) {
        this.Lowstart = Lowstart;
        this.Highstart = Highstart;
        this.High = High;
        this.Highend = Highend;
        this.Lowend = Lowend;

        this.Lowstart_Level = Lowstart_Level;
        this.High_Level = High_Level;
        this.Lowend_Level = Lowend_Level;
    }

    public StepInfo(int Highstart, int Lowstart, int Low, int Lowend, int Highend, float Highstart_Level, float Low_Level, float Highend_Level, int none) {
        this.Highstart = Highstart;
        this.Lowstart = Lowstart;
        this.Low = Low;
        this.Lowend = Lowend;
        this.Highend = Highend;

        this.Highstart_Level = Highstart_Level;
        this.Low_Level = Low_Level;
        this.Highend_Level = Highend_Level;
    }

    public int getHighstart() {
        return Highstart;
    }

    public void setHighstart(int highstart) {
        Highstart = highstart;
    }

    public int getHighend() {
        return Highend;
    }

    public void setHighend(int highend) {
        Highend = highend;
    }

    public int getLowstart() {
        return Lowstart;
    }

    public void setLowstart(int lowstart) {
        Lowstart = lowstart;
    }

    public int getLowend() {
        return Lowend;
    }

    public void setLowend(int lowend) {
        Lowend = lowend;
    }

    public boolean getNOError0() {
        return NOError0;
    }

    public void setNOError0(boolean NOError0) {
        this.NOError0 = NOError0;
    }

    public boolean isNOError0_0() {
        return NOError0_0;
    }

    public void setNOError0_0(boolean NOError0_0) {
        this.NOError0_0 = NOError0_0;
    }

    public boolean isNOError0_1() {
        return NOError0_1;
    }

    public void setNOError0_1(boolean NOError0_1) {
        this.NOError0_1 = NOError0_1;
    }

    public boolean getNOError1() {
        return NOError1;
    }

    public void setNOError1(boolean NOError1) {
        this.NOError1 = NOError1;
    }

    public int getNumError1() {
        return NumError1;
    }

    public void setNumError1() {
        NumError1++;
    }

    public int getNumError11() {
        return NumError11;
    }

    public void setNumError11() {
        NumError11++;
    }

    public boolean getNOError2() {
        return NOError2;
    }

    public void setNOError2(boolean NOError2) {
        this.NOError2 = NOError2;
    }

    public boolean getNoError() {
        return (NOError0 | !SportParam.CheckError0) &&
                (NOError1 | !SportParam.CheckError1) &&
                (NOError2 | !SportParam.CheckError2);
    }

    public int getHigh() {
        return High;
    }

    public void setHigh(int high) {
        High = high;
    }

    public int getLow() {
        return Low;
    }

    public void setLow(int low) {
        Low = low;
    }

}
