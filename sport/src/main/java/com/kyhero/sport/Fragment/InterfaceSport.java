package com.kyhero.sport.Fragment;

import java.util.List;

public interface InterfaceSport {

    void data(int allnum, int oknum, int errornum);

    void result(List<Boolean> a, List<Boolean> b, List<Boolean> c);

    void beginstop(int isstart);


}
