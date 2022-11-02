/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kyhero.sport.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.kyhero.sport.CameraXViewModel;
import com.kyhero.sport.GraphicOverlay;
import com.kyhero.sport.R;
import com.kyhero.sport.VisionImageProcessor;
import com.kyhero.sport.communal.DragScaleView;
import com.kyhero.sport.communal.LimbsStatus;
import com.kyhero.sport.communal.LogcatHelper;
import com.kyhero.sport.communal.MathUtil;
import com.kyhero.sport.communal.PermisionUtil;
import com.kyhero.sport.communal.SportListener;
import com.kyhero.sport.communal.SportParam;
import com.kyhero.sport.communal.Texttospeech;
import com.kyhero.sport.config.Configpullup;
import com.kyhero.sport.config.Configpushup;
import com.kyhero.sport.config.Configsitup;
import com.kyhero.sport.data.Dao;
import com.kyhero.sport.java.PreferenceUtils;
import com.kyhero.sport.java.posedetector.PoseDetectorProcessor;
import com.kyhero.sport.linklist.LinkListJudge;
import com.kyhero.sport.linklist.LinkListLimbs;
import com.kyhero.sport.linklist.LinkListNumerical;
import com.kyhero.sport.linklist.LinkListPointF;
import com.kyhero.sport.linklist.LinkListPos;
import com.kyhero.sport.netutil.DownloadService;
import com.kyhero.sport.netutil.FileUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignExstrom;

import static java.lang.Thread.sleep;

/**
 * Live preview demo app for ML Kit APIs using CameraX.
 */
@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
public final class SportFragment extends Fragment implements OnRequestPermissionsResultCallback, OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "Sport";
    public static final int PERMISSION_REQUESTS = 1;
    private static final String OBJECT_DETECTION = "Object Detection";
    private static final String STATE_SELECTED_MODEL = "selected_model";
    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;
    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable
    private Preview previewUseCase;
    @Nullable
    private ImageAnalysis analysisUseCase;
    @Nullable
    private VisionImageProcessor imageProcessor;
    private boolean needUpdateGraphicOverlayImageSourceInfo;
    private String selectedModel = OBJECT_DETECTION;
    private CameraSelector cameraSelector;

    private ImageView image_view;

    public Texttospeech tts;

    public ToggleButton facingSwitch;
    public PermisionUtil permisionUtil;

    Dao dao = null;

    public RelativeLayout mat_view_l, mat_view_r;
    public ImageView pullup_view;
    public TextView tv_info;
    RelativeLayout hair_dv_parent;
    DragScaleView hair_dv;

    public IirFilterCoefficients iirFilterCoefficients;

    int sportcount = 0;
    int sportcountok = 0;
    int sportcounterror = 0;
    boolean model_left_right = false;
    public Activity activity;
    public Context mcontext;
    InterfaceSport interfaceSport;
    public static boolean debugmode = true;
    long TimeStart = 0;

    public static LinkListJudge lljframe = new LinkListJudge(20);   //2s(20/10)离开画面，不结束运行
    public static LinkListJudge lljInmat = new LinkListJudge();
    LinkListJudge lljAnticipationOK = new LinkListJudge(20);

    int[] id_lc = {R.id.multi_line_Gl_chart0, R.id.multi_line_Gl_chart1, R.id.multi_line_Gl_chart2, R.id.multi_line_Gl_chart3, R.id.multi_line_Gl_chart4, R.id.multi_line_Gl_chart5};
    LineChart lineChart0, lineChart1, lineChart2, lineChart3, lineChart4, lineChart5;
    LineChart[] LClist = {lineChart0, lineChart1, lineChart2, lineChart3, lineChart4, lineChart5};


    int type = -1;
    String TAG_SitUp = "Situp";
    PointF posAnkle;
    PointF noseCenter;

    LinkListNumerical llndst = new LinkListNumerical();
    LinkListNumerical llnloc = new LinkListNumerical();
    LinkListNumerical llnerror0 = new LinkListNumerical();
    LinkListNumerical llnerror1L = new LinkListNumerical();
    LinkListNumerical llnerror1R = new LinkListNumerical();
    LinkListNumerical llnerror2 = new LinkListNumerical();
//    LinkListNumerical llntypeL = new LinkListNumerical();
//    LinkListNumerical llntypeR = new LinkListNumerical();
//    LinkListNumerical llntypeD = new LinkListNumerical();
//    LinkListNumerical llntypeC = new LinkListNumerical();

    LinkListPointF llpfNose = new LinkListPointF(10);
    LinkListNumerical lln_dis_ear_knee = new LinkListNumerical(10);
    LinkListNumerical lln_dis_shoulder_hip = new LinkListNumerical(10);
    LinkListNumerical lln_index_ear_L = new LinkListNumerical(10);
    LinkListNumerical lln_index_ear_R = new LinkListNumerical(10);

    List<Float> listDst = new ArrayList<>();
    List<Float> listLoc = new ArrayList<>();
    List<Float> listEC0 = new ArrayList<>();
    List<Float> listEC00 = new ArrayList<>();

    List<Float> listEC1L = new ArrayList<>();
    List<Float> listEC1R = new ArrayList<>();
    List<Float> listEC11L = new ArrayList<>();
    List<Float> listEC11R = new ArrayList<>();
    List<Float> listEC2 = new ArrayList<>();
    List<Float> listEC22 = new ArrayList<>();


    String TAG_PullUp = "Pullup";
    PointF posl, posr;
    PointF poslc, posrc;
    List<Float> lsdst = new ArrayList<>();
    LinkListPointF llcMounse = new LinkListPointF();
    LinkListNumerical llnShoulder = new LinkListNumerical(5);
    LinkListNumerical llndstpullup = new LinkListNumerical(10);
    LinkListNumerical llndsterrorl = new LinkListNumerical(5);
    LinkListNumerical llndsterrorr = new LinkListNumerical(5);
    LinkListLimbs lllFL = new LinkListLimbs(10);
    LinkListLimbs lllFR = new LinkListLimbs(10);
    LinkListLimbs lllRL = new LinkListLimbs(10);
    LinkListLimbs lllRR = new LinkListLimbs(10);

    List<LimbsStatus> lsFL = new ArrayList<>();
    List<LimbsStatus> lsFR = new ArrayList<>();
    List<LimbsStatus> lsRL = new ArrayList<>();
    List<LimbsStatus> lsRR = new ArrayList<>();

    LinkListPos llpWrist = new LinkListPos(5);
    List<Float> lsposl = new ArrayList<>();
    List<Float> lsposr = new ArrayList<>();
    List<Float> lsposd = new ArrayList<>();


    String TAG_PushUp = "Pushup";
    PointF posw, posa;
    List<Float> lsdstpushup = new ArrayList<>();
    LinkListNumerical llndstpushup = new LinkListNumerical(10);
    LinkListPointF llcShoulder = new LinkListPointF();
    LinkListPos llpWrist_Ankle = new LinkListPos();
    List<Float> lsposw = new ArrayList<>();
    List<Float> lsposa = new ArrayList<>();


    //设置接口回调
    public void setInterface(InterfaceSport Sport) {
        interfaceSport = Sport;
    }

    //设置运动类型  0-仰卧起坐 1-引体向上
    public void setSportType(int sportType) {
        if (sportType > 2) {
            return;
        }
        SportParam.SportType = sportType;
        Log.d(TAG, "setSportType: " + sportType);
        setLineChartTitle(SportParam.SportType);
        LogcatHelper.getInstance(mcontext).stop();
        LogcatHelper.getInstance(mcontext).start();

        setMatLocation(SportParam.SportMatLocation);

    }

    //设置结束时间（s）
    public void setSportTime(int sportTime) {
        SportParam.SportTime = sportTime;
    }

    //设置错误检查 true（检查）/flase（不检查） （默认检查）
    public void setSportErrorCheck(Boolean error0, Boolean error1, Boolean error2) {
        SportParam.CheckError0 = error0;
        SportParam.CheckError1 = error1;
        SportParam.CheckError2 = error2;
    }

    //设置垫子标定  0-不显示 1-显示左 2-显示右
    public void setMatLocation(int sportMatLocation) {
        SportParam.SportMatLocation = sportMatLocation;
        mat_view_l.setVisibility(View.INVISIBLE);
        mat_view_r.setVisibility(View.INVISIBLE);
        pullup_view.setVisibility(View.INVISIBLE);
        if (SportParam.SportType == 0) {
            if (SportParam.SportMatLocation == 0) {
                mat_view_l.setVisibility(View.INVISIBLE);
                mat_view_r.setVisibility(View.INVISIBLE);
            } else if (SportParam.SportMatLocation == 1) {
                mat_view_l.setVisibility(View.VISIBLE);
                mat_view_r.setVisibility(View.INVISIBLE);
            } else if (SportParam.SportMatLocation == 2) {
                mat_view_l.setVisibility(View.INVISIBLE);
                mat_view_r.setVisibility(View.VISIBLE);
            }
        } else if (SportParam.SportType == 1) {
            if (SportParam.SportMatLocation == 0) {
                pullup_view.setVisibility(View.INVISIBLE);
            } else if (SportParam.SportMatLocation == 1) {
                pullup_view.setVisibility(View.VISIBLE);
            } else if (SportParam.SportMatLocation == 2) {
                pullup_view.setVisibility(View.VISIBLE);
            }
        }
    }

    //设置运动难易程度  0-简单 1-中等 2-困难
    public void setSportDifficulty(int sportDifficulty) {
        SportParam.SportDifficulty = sportDifficulty;
    }

    //设置是否实时检测
    public void setPersonDetect(boolean detection) {
        Log.d(TAG, "setPersonDetect: " + detection);
        SportParam.SportDect = detection;
        if (SportParam.SportDect) {
            image_view.setVisibility(View.VISIBLE);
        } else {
            image_view.setVisibility(View.INVISIBLE);
        }
    }

    //更新apk
    public void setApkUpdate() {
        mcontext.startForegroundService(new Intent(mcontext, DownloadService.class));
    }

    List<PoseLandmark> poselist = new ArrayList<>();

    public SportListener sportListener = new SportListener() {

        @Override
        public void situp(PoseLandmark nose,
                          PoseLandmark leftEar, PoseLandmark leftShoulder, PoseLandmark leftElbow, PoseLandmark leftWrist, PoseLandmark leftIndex, PoseLandmark leftHip, PoseLandmark leftKnee, PoseLandmark leftAnkle,
                          PoseLandmark rightEar, PoseLandmark rightShoulder, PoseLandmark rightElbow, PoseLandmark rightWrist, PoseLandmark rightIndex, PoseLandmark rightHip, PoseLandmark rightKnee, PoseLandmark rightAnkle) {

            if (SportParam.SportType != 0) {
                return;
            }

//            PointF nosekalman = SportParam.kalmanFilter.filter(nose.getPosition());

            lljframe.AddFirst(1);
            if (lljframe.Judge() == lljframe.Num) {
                if (SportParam.sportStatus == SportParam.SportStatus.WaitforPerson) {
                    SportParam.sportStatus = SportParam.SportStatus.DetectPerson;
                }
            }

            if (SportParam.sportStatus == SportParam.SportStatus.DetectPerson || SportParam.sportStatus == SportParam.SportStatus.PersonInmat) {
                if ((Configsitup.isInMat(leftHip) || Configsitup.isInMat(rightHip)) &&
                        (MathUtil.Distance(leftShoulder.getPosition(), leftHip.getPosition()) > Configsitup.dis_shoulder_hip_min
                                || MathUtil.Distance(rightShoulder.getPosition(), rightHip.getPosition()) > Configsitup.dis_shoulder_hip_min)) {
                    lljInmat.Add(1);
                } else {
                    lljInmat.Add(0);
                    SportParam.sportStatus = SportParam.SportStatus.WaitforPerson;
                }

                if (lljInmat.Judge(System.currentTimeMillis())) {
                    SportParam.sportStatus = SportParam.SportStatus.PersonInmat;
                    tts.TTS("请做好准备");
                    showText("请做好准备");
                }
            }

            Log.w(TAG_SitUp, " " + SportParam.sportStatus + " " + lljframe.Judge() + " " + lljInmat.Judge() + " " + lljAnticipationOK.Judge());

            if (SportParam.sportStatus == SportParam.SportStatus.PersonInmat || SportParam.sportStatus == SportParam.SportStatus.AnticipationOk) {
                if (Configsitup.isAnticipationOK(leftShoulder, leftHip, leftKnee, leftAnkle) || Configsitup.isAnticipationOK(rightShoulder, rightHip, rightKnee, rightAnkle)) {
                    lljAnticipationOK.Add(1);
                    llpfNose.Add(nose.getPosition());
                    if (leftShoulder.getPosition().x > leftKnee.getPosition().x) {
                        lln_dis_ear_knee.Add(MathUtil.Distance(leftEar.getPosition(), leftKnee.getPosition()));
                        lln_index_ear_L.Add(MathUtil.Distance(leftIndex.getPosition(), leftEar.getPosition()));
                        lln_dis_shoulder_hip.Add(MathUtil.Distance(leftShoulder.getPosition(), leftHip.getPosition()));
                        model_left_right = true;
                    } else {
                        lln_dis_ear_knee.Add(MathUtil.Distance(rightEar.getPosition(), rightKnee.getPosition()));
                        lln_index_ear_R.Add(MathUtil.Distance(rightIndex.getPosition(), rightEar.getPosition()));
                        lln_dis_shoulder_hip.Add(MathUtil.Distance(rightShoulder.getPosition(), rightHip.getPosition()));
                        model_left_right = false;
                    }
                } else {
                    lljAnticipationOK.Add(0);
                }

//                llntypeL.Add((float) MathUtil.calAngle(leftShoulder.getPosition(), leftElbow.getPosition(), leftHip.getPosition()));
//                llntypeR.Add((float) MathUtil.calAngle(rightShoulder.getPosition(), rightElbow.getPosition(), rightHip.getPosition()));

//                PointF pc1 = new PointF(leftWrist.getPosition().x / 2 + rightWrist.getPosition().x / 2, leftWrist.getPosition().y / 2 + rightWrist.getPosition().y / 2);
//                PointF pc2 = new PointF(leftShoulder.getPosition().x / 2 + rightWrist.getPosition().x / 2, leftShoulder.getPosition().y / 2 + rightShoulder.getPosition().y / 2);

//                llntypeD.Add(MathUtil.Distance(pc1, nosekalman));
//                llntypeC.Add(MathUtil.Distance(pc2, nosekalman));

                Configsitup.dis_ear_knee = lln_dis_ear_knee.Average();
                Configsitup.dis_shoulder_hip = lln_dis_shoulder_hip.Average();
                Configsitup.dis_index_ear_l = lln_index_ear_L.Average();
                Configsitup.dis_index_ear_r = lln_index_ear_R.Average();
                Configsitup.setparams();
                noseCenter = llpfNose.Center();

                if (lljAnticipationOK.Judge(System.currentTimeMillis())) {

                    SportParam.sportStatus = SportParam.SportStatus.AnticipationOk;
                    Log.w(TAG_SitUp, "AnticipationOK  " + (model_left_right ? "Letf" : "Right") + "  " + Configsitup.dis_shoulder_hip + " " + type);
                    tts.TTS("请开始");
                    showText("请开始");

//                    int typeL = llntypeL.printlist();
//                    int typeR = llntypeR.printlist();
//                    int typeC = llntypeC.printlist();
//                    int typeD = llntypeD.printlist();
//                    Log.w(TAG_SitUp, " ------------------");
//
//                    if (Configsitup.dis_shoulder_hip * 2 / 3 < typeD && typeD > typeC * 9 / 10) {
//                        type = 1;
////                    tv_type.setText("手抱胸");
//                    } else {
//                        if (typeL + typeR < 210) {
//                            type = 1;
////                        tv_type.setText("手捏耳");
//                        } else {
//                            type = 2;
////                        tv_type.setText("手抱头");
//                        }
//                    }
//                    if (type == -1) {
//                        return;
//                    }
//                    if (model_left_right) {
//                        posAnkle = leftAnkle.getPosition();
//                        Log.d("TADD", "begin: " + MathUtil.Distance(leftEar.getPosition(), leftKnee.getPosition()));
//                    } else {
//                        posAnkle = rightAnkle.getPosition();
//                        Log.d("TADD", "begin: " + MathUtil.Distance(rightEar.getPosition(), rightKnee.getPosition()));
//                    }

                }
            }

            //视频跟踪算法
//            if (SportParam.sportStatus == SportParam.SportStatus.AnticipationOk || SportParam.sportStatus == SportParam.SportStatus.Runing) {
//
//                if (MathUtil.Distance(nosekalman, posAnkle) < Configsitup.dis_shoulder_hip || (!Configsitup.isInMat(leftHip) || !Configsitup.isInMat(rightHip))) {
//                    SportParam.personstar.x = SportParam.matstar.x;
//                    SportParam.personstar.y = SportParam.matstar.y;
//                    SportParam.personend.x = SportParam.matend.x;
//                    SportParam.personend.y = SportParam.matend.y;
//                } else {
//                    if (model_left_right) {
//                        SportParam.personstar.x = (int) posAnkle.x - 250;
//                        SportParam.personstar.y = (int) nose.getPosition().y - 130;
//                        SportParam.personend.x = (int) nose.getPosition().x + 200;
//                        SportParam.personend.y = (int) posAnkle.y + 100;
//                    } else {
//                        SportParam.personstar.x = (int) nose.getPosition().x - 250;
//                        SportParam.personstar.y = (int) nose.getPosition().y - 130;
//                        SportParam.personend.x = (int) posAnkle.x + 50;
//                        SportParam.personend.y = (int) posAnkle.y + 100;
//                    }
//                }
//
//            }


            if (SportParam.sportStatus == SportParam.SportStatus.AnticipationOk || SportParam.sportStatus == SportParam.SportStatus.Runing) {

                //错误处理
                {
                    float dstKnee_Ankle, dstIndex_EarL, dstIndex_EarR, angle;
                    if (model_left_right) {
                        dstKnee_Ankle = MathUtil.VerticalLine(leftElbow.getPosition(), leftKnee.getPosition(), leftAnkle.getPosition());
                        angle = (float) MathUtil.calAngle(leftHip.getPosition(), leftShoulder.getPosition(), leftAnkle.getPosition());
                    } else {
                        dstKnee_Ankle = MathUtil.VerticalLine(rightElbow.getPosition(), rightKnee.getPosition(), rightAnkle.getPosition());
                        angle = (float) MathUtil.calAngle(rightHip.getPosition(), rightShoulder.getPosition(), rightAnkle.getPosition());
                    }
                    dstIndex_EarL = Math.abs(MathUtil.Distance(leftIndex.getPosition(), leftEar.getPosition()) - Configsitup.dis_index_ear_l);
                    dstIndex_EarR = Math.abs(MathUtil.Distance(rightIndex.getPosition(), rightEar.getPosition()) - Configsitup.dis_index_ear_r);


                    Log.w(TAG_SitUp, "Runing  1:" + Configsitup.dis_shoulder_hip + " " + dstKnee_Ankle + " " + dstIndex_EarL + " " + angle);

                    if (dstKnee_Ankle < 0) {
                        dstKnee_Ankle = -dstKnee_Ankle;
                    }
                    if (SportParam.sportStatus != SportParam.SportStatus.Runing) {
                        llnerror0.Add(dstKnee_Ankle);
                        llnerror1L.Add(dstIndex_EarL);
                        llnerror1R.Add(dstIndex_EarR);
                        llnerror2.Add(angle);
                    } else {

                        if (SportParam.sportStatus == SportParam.SportStatus.Runing && listEC0.size() == 0) {
                            for (int i = 0; i < llnerror0.size(); i++) {
                                listEC0.add(llnerror0.get(i));
                                listEC1L.add(llnerror1L.get(i));
                                listEC1R.add(llnerror1R.get(i));
                                listEC2.add(llnerror2.get(i));
                            }
                        }

                        listEC0.add(dstKnee_Ankle);
                        listEC1L.add(dstIndex_EarL);
                        listEC1R.add(dstIndex_EarR);
                        listEC2.add(angle);

                        List fftresult0 = MathUtil.IIRFilter(listEC0, iirFilterCoefficients.a, iirFilterCoefficients.b);
                        listEC00.clear();
                        for (int i = 0; i < fftresult0.size(); i++) {
                            listEC00.add((float) fftresult0.get(i) > Configsitup.error0 ? 1f : 0);
                        }

                        List fftresult1L = MathUtil.IIRFilter(listEC1L, iirFilterCoefficients.a, iirFilterCoefficients.b);
                        listEC11L.clear();
                        for (int i = 0; i < fftresult1L.size(); i++) {
                            listEC11L.add((float) fftresult1L.get(i) > Configsitup.error1 ? 1f : 0);
                        }

                        List fftresult1R = MathUtil.IIRFilter(listEC1R, iirFilterCoefficients.a, iirFilterCoefficients.b);
                        listEC11R.clear();
                        for (int i = 0; i < fftresult1R.size(); i++) {
                            listEC11R.add((float) fftresult1R.get(i) > Configsitup.error1 ? 1f : 0);
                        }

                        List fftresult2 = MathUtil.IIRFilter(listEC2, iirFilterCoefficients.a, iirFilterCoefficients.b);
                        listEC22.clear();
                        for (int i = 0; i < fftresult1L.size(); i++) {
                            listEC22.add((float) fftresult2.get(i) > Configsitup.error2 ? 1f : 0);
                        }

                        Log.w(TAG_SitUp, "Runing  " + fftresult0.size() + " " + fftresult1L.size() + " " + fftresult1R.size() + " " + fftresult2.size());
                        if (debugmode && fftresult0.size() > 10) {
                            //肘到膝距离处理
                            float max0 = (float) Collections.max(fftresult0);
                            float min0 = (float) Collections.min(fftresult0);
                            List<Float> fftresult00 = new ArrayList<>();
                            for (int i = 0; i < fftresult0.size(); i++) {
                                fftresult00.add((float) fftresult0.get(i) > Configsitup.error0 ? max0 : min0);
                            }
                            showlinechart(LClist[1], listEC0, fftresult0, fftresult00, Configsitup.error0);


                            //手到耳距离处理L
                            float max1 = (float) Collections.max(fftresult1L);
                            float min1 = (float) Collections.min(fftresult1L);
                            List<Float> fftresult11L = new ArrayList<>();
                            for (int i = 0; i < fftresult1L.size(); i++) {
                                fftresult11L.add((float) fftresult1L.get(i) > Configsitup.error1 ? max1 : min1);
                            }
                            showlinechart(LClist[2], listEC1L, fftresult1L, fftresult11L, Configsitup.error1);

                            //手到耳距离处理R
                            float max1r = (float) Collections.max(fftresult1R);
                            float min1r = (float) Collections.min(fftresult1R);
                            List<Float> fftresult11R = new ArrayList<>();
                            for (int i = 0; i < fftresult1R.size(); i++) {
                                fftresult11R.add((float) fftresult1R.get(i) > Configsitup.error1 ? max1r : min1r);
                            }
                            showlinechart(LClist[3], listEC1R, fftresult1R, fftresult11R, Configsitup.error1);


                            //肩触垫角度处理
                            float max2 = (float) Collections.max(fftresult2);
                            float min2 = (float) Collections.min(fftresult2);
                            List<Float> fftresult22 = new ArrayList<>();
                            for (int i = 0; i < fftresult2.size(); i++) {
                                fftresult22.add((float) fftresult2.get(i) > Configsitup.error2 ? max2 : min2);
                            }
                            showlinechart(LClist[4], listEC2, fftresult2, fftresult22, Configsitup.error2);

                        }
                    }

                }

                //计数处理
                {
                    float dst;
                    if (model_left_right) {
                        dst = MathUtil.Distance(leftEar.getPosition(), noseCenter);
                    } else {
                        dst = MathUtil.Distance(rightEar.getPosition(), noseCenter);
                    }

                    if (SportParam.sportStatus != SportParam.SportStatus.Runing) {
                        llndst.Add(dst);
                        Log.w(TAG_SitUp, "Runing  " + llndst.Numerical() + " " + Configsitup.disstart);
                        if (llndst.Numerical() > Configsitup.disstart) {
                            SportParam.sportStatus = SportParam.SportStatus.Runing;
                            TimeStart = System.currentTimeMillis();
                            interfaceSport.beginstop(0);
                            listDst.clear();
                            for (int i = 0; i < llndst.size(); i++) {
                                listDst.add(llndst.get(i));
                            }
                        }
                        return;
                    }

                    listDst.add(dst);

                    List fftresult = MathUtil.IIRFilter(listDst, iirFilterCoefficients.a, iirFilterCoefficients.b);


                    if (fftresult.size() > 10) {
                        Log.w(TAG_SitUp, "Runing  " + fftresult.size() + " " + listEC00.size() + " " + listEC11L.size() + " " + listEC22.size());
                        float[] sqtresult;
                        if (model_left_right) {
                            sqtresult = Configsitup.SquarewaveFilter(fftresult, listEC00, listEC11L, listEC11R, listEC22);
                        } else {
                            sqtresult = Configsitup.SquarewaveFilter(fftresult, listEC00, listEC11R, listEC11L, listEC22);
                        }

                        if (sportcount != (int) sqtresult[sqtresult.length - 1]) {
                            if (sportcountok != (int) sqtresult[sqtresult.length - 3] && !SportParam.SportEnd) {
                                tts.TTS((int) sqtresult[sqtresult.length - 3] + " ");
                            }
                            if (sportcounterror != (int) sqtresult[sqtresult.length - 2] && !SportParam.SportEnd) {
                                tts.TTS("犯规");
                            }
                            Log.w(TAG_SitUp, ">>>>>" + sportcount + " " + (int) sqtresult[sqtresult.length - 1] + " " + (int) sqtresult[sqtresult.length - 2] + " " + (int) sqtresult[sqtresult.length - 3]);
                            sportcount = (int) sqtresult[sqtresult.length - 1];
                            sportcounterror = (int) sqtresult[sqtresult.length - 2];
                            sportcountok = (int) sqtresult[sqtresult.length - 3];

                            interfaceSport.data(sportcount, sportcountok, sportcounterror);
                            interfaceSport.result(SportParam.LsError0, SportParam.LsError1, SportParam.LsError2);
                        }

                        if (debugmode) {
                            List<Float> sqtresult1 = new ArrayList<>();
                            for (int i = 0; i < sqtresult.length - 4; i++) {
                                sqtresult1.add(sqtresult[i]);
                            }
                            showlinechart(LClist[0], listDst, fftresult, sqtresult1, sqtresult[sqtresult.length - 4]);
                        }
                    }

                }

                //结束条件处理
                {
                    int Time = (int) ((System.currentTimeMillis() - TimeStart) / 1000);
                    int TimeDiff = SportParam.SportTime - Time;
                    Log.d(TAG_SitUp, "Time  " + Time + "  " + SportParam.SportTime + " " + TimeDiff);

                    if (TimeDiff == (SportParam.SportTime / 2) && !SportParam.SportHalf) {
                        tts.TTS("已用时一半");
                        showText("已用时一半");
                        SportParam.SportHalf = true;
                    }

                    if (TimeDiff <= (SportParam.TimeContDown + 1) && !SportParam.SportEnd) {
                        tts.TTS("倒计时");
                        showText("倒计时");
                        SportParam.SportEnd = true;
                    }

                    if (1 < TimeDiff && TimeDiff <= SportParam.TimeContDown) {
                        tts.TTS((SportParam.TimeContDown - 1) + " ");
                        showText((SportParam.TimeContDown - 1) + " ");
                        SportParam.TimeContDown--;
                    }

                    if (Time > SportParam.SportTime || lljInmat.Judge() < lljInmat.Num / 3) {
                        sportfinish();
                    }

                    /*float dst;
                    if (model_left_right) {
                        dst = MathUtil.Distance(leftAnkle.getPosition(), posAnkle);
                    } else {
                        dst = MathUtil.Distance(rightAnkle.getPosition(), posAnkle);
                    }

                    if (SportParam.sportStatus != SportParam.SportStatus.Runing) {
                        llnloc.Add(dst);
                        return;
                    }

                    if (SportParam.sportStatus == SportParam.SportStatus.Runing && listLoc.size() == 0) {
                        listLoc.clear();
                        for (int i = 0; i < llnloc.size(); i++) {
                            listLoc.add(llnloc.get(i));
                        }
                    }

                    listLoc.add(dst);

                    List fftresult = MathUtil.IIRFilter(listLoc, iirFilterCoefficients.a, iirFilterCoefficients.b);

                    float max = (float) Collections.max(fftresult);
                    Log.w(TAG_SitUp, ">>" + Configsitup.dis_shoulder_hip / 2 + " " + max);
                    if (max > Configsitup.locend) {
                        sportfinish();
                    }

                    if (debugmode) {
                        showlinechart(lineChart1, listLoc, fftresult, fftresult, Configsitup.dis_shoulder_hip);
                    }*/

                }

            }


        }

        @Override
        public void pullup(PoseLandmark nose, PoseLandmark leftMouth, PoseLandmark rightMouth, PoseLandmark leftEye, PoseLandmark rightEye,
                           PoseLandmark leftShoulder, PoseLandmark leftElbow, PoseLandmark leftWrist,
                           PoseLandmark rightShoulder, PoseLandmark rightElbow, PoseLandmark rightWrist,
                           PoseLandmark leftHip, PoseLandmark leftKnee, PoseLandmark leftAnkle,
                           PoseLandmark rightHip, PoseLandmark rightKnee, PoseLandmark rightAnkle) {

            if (SportParam.SportType != 1) {
                return;
            }

            lljframe.AddFirst(1);
            if (lljframe.Judge() == lljframe.Num) {
                if (SportParam.sportStatus == SportParam.SportStatus.WaitforPerson) {
                    SportParam.sportStatus = SportParam.SportStatus.DetectPerson;
                }
            }

            if (SportParam.sportStatus == SportParam.SportStatus.DetectPerson) {
                Log.d(TAG_PullUp, " " + Configpullup.isInMat(leftHip) + " " + Configpullup.isInMat(rightHip) +
                        " " + MathUtil.Distance(leftShoulder.getPosition(), leftHip.getPosition()) +
                        " " + MathUtil.Distance(rightShoulder.getPosition(), rightHip.getPosition()) +
                        " " + Configpullup.dis_shoulder_min);
                if ((Configpullup.isInMat(leftHip) || Configpullup.isInMat(rightHip)) &&
                        (MathUtil.Distance(leftShoulder.getPosition(), leftHip.getPosition()) > Configpullup.dis_shoulder_min
                                || MathUtil.Distance(rightShoulder.getPosition(), rightHip.getPosition()) > Configpullup.dis_shoulder_min)) {
                    lljInmat.Add(1);
                } else {
                    lljInmat.Add(0);
                    SportParam.sportStatus = SportParam.SportStatus.WaitforPerson;
                }

                if (lljInmat.Judge(System.currentTimeMillis())) {
                    SportParam.sportStatus = SportParam.SportStatus.PersonInmat;
                    //dao.Pulltimes++;
                    tts.TTS("请开始");
                }
            }

            Log.d(TAG_PullUp, "pullup:  " + SportParam.sportStatus + " " + lljframe.Judge() + " " + lljInmat.Judge());

            //手位置处理
            if (SportParam.sportStatus == SportParam.SportStatus.PersonInmat || SportParam.sportStatus == SportParam.SportStatus.Runing) {  //手位置处理

                /*{
                    poselist.clear();

                    poselist.add(nose);
                    poselist.add(leftMouth);
                    poselist.add(rightMouth);

                    poselist.add(leftShoulder);
                    poselist.add(leftElbow);
                    poselist.add(leftWrist);

                    poselist.add(rightShoulder);
                    poselist.add(rightElbow);
                    poselist.add(rightWrist);

                    poselist.add(leftHip);
                    poselist.add(leftKnee);
                    poselist.add(leftAnkle);

                    poselist.add(rightHip);
                    poselist.add(rightKnee);
                    poselist.add(rightAnkle);

                    dao.PullupInsertDao(poselist);
                }*/

                llpWrist.Add(leftWrist.getPosition(), rightWrist.getPosition());
                lsposl.add(llpWrist.ErrorL());
                lsposr.add(llpWrist.ErrorR());
                lsposd.add(llpWrist.ErrorD());

                posl = llpWrist.AvgL();
                posr = llpWrist.AvgR();

                if (lsposl.size() > 10) {
                    List fftresultposl = MathUtil.IIRFilter(lsposl, iirFilterCoefficients.a, iirFilterCoefficients.b);
                    List fftresultposr = MathUtil.IIRFilter(lsposr, iirFilterCoefficients.a, iirFilterCoefficients.b);
                    List fftresultposd = MathUtil.IIRFilter(lsposd, iirFilterCoefficients.a, iirFilterCoefficients.b);

                    List resultposl = MathUtil.NumericComparison(fftresultposl, 5f);
                    List resultposr = MathUtil.NumericComparison(fftresultposr, 5f);
                    List resultposd = MathUtil.NumericComparison(fftresultposd, 5f);

                    if (debugmode) {
                        List<Float> posllist = new ArrayList<>();
                        List<Float> posrlist = new ArrayList<>();
                        List<Float> posdlist = new ArrayList<>();
                        for (int i = 0; i < fftresultposl.size(); i++) {
                            posllist.add((boolean) resultposl.get(i) ? 5f : 0);
                            posrlist.add((boolean) resultposr.get(i) ? 5f : 0);
                            posdlist.add((boolean) resultposd.get(i) ? 5f : 0);
                        }
                        showlinechart1(LClist[4], 5f,
                                lsposl, lsposr, null, null,
                                fftresultposl, fftresultposr, null, null,
                                posllist, posrlist, null, null);
                    }

                    if (resultposl.size() > 0) {

                        int Time = (int) ((System.currentTimeMillis() - TimeStart) / 1000);
                        if (SportParam.sportStatus == SportParam.SportStatus.Runing) {

                            int TimeDiff = SportParam.SportTime - Time;
                            Log.d(TAG_SitUp, "Time  " + Time + "  " + SportParam.SportTime + " " + TimeDiff);

                            if (TimeDiff == (SportParam.SportTime / 2) && !SportParam.SportHalf) {
                                tts.TTS("已用时一半");
                                showText("已用时一半");
                                SportParam.SportHalf = true;
                            }

                            if (TimeDiff <= (SportParam.TimeContDown + 1) && !SportParam.SportEnd) {
                                tts.TTS("倒计时");
                                showText("倒计时");
                                SportParam.SportEnd = true;
                            }

                            if (1 < TimeDiff && TimeDiff <= SportParam.TimeContDown) {
                                tts.TTS((SportParam.TimeContDown - 1) + " ");
                                showText((SportParam.TimeContDown - 1) + " ");
                                SportParam.TimeContDown--;
                            }

                            if (Time > SportParam.SportTime) {
                                sportfinish();
                                return;
                            }

                        }

                        if (SportParam.sportStatus == SportParam.SportStatus.Runing) {
                            llndsterrorl.Add(MathUtil.Distance(posl, poslc));
                            llndsterrorr.Add(MathUtil.Distance(posr, posrc));
                            Log.d("CCCD", "END>>>>>> " + llndsterrorl.size() + " " + llndsterrorl.Average() + " " + llndsterrorr.Average() + " " + Configpullup.dis_shoulder);
                            if (llndsterrorl.size() == llndsterrorl.Num && llndsterrorl.Average() > Configpullup.dis_shoulder && llndsterrorr.Average() > Configpullup.dis_shoulder) {
                                sportfinish();
                                return;
                            }
                        }


                        Log.d("CCC", "END>>>>>> " + fftresultposl.get(resultposl.size() - 1) + " " + fftresultposr.get(resultposr.size() - 1) + " "
                                + resultposl.get(resultposl.size() - 1) + " " + resultposr.get(resultposr.size() - 1) + " " + SportParam.sportStatus);
                        if ((boolean) resultposl.get(resultposl.size() - 1) && (boolean) resultposr.get(resultposr.size() - 1) && Time > 1) {
                            Log.d("CCC", "END>>>>>> " + fftresultposl.get(resultposl.size() - 1) + " " + fftresultposr.get(resultposr.size() - 1) + " "
                                    + resultposl.get(resultposl.size() - 1) + " " + resultposr.get(resultposr.size() - 1) + " " + SportParam.sportStatus);
                            if (SportParam.sportStatus == SportParam.SportStatus.Runing) {
                                sportfinish();
                                return;
                            } else {
                                SportParam.sportStatus = SportParam.SportStatus.PersonInmat;
                            }
                        }
                    }
                }

            }

            //计数和错误处理

            float cMounsex = leftMouth.getPosition().x / 2 + rightMouth.getPosition().x / 2;
            float cMounsey = leftMouth.getPosition().y / 2 + rightMouth.getPosition().y / 2;
            float headx, heady;
            if (SportParam.SportDifficulty == 0) { //简单
                headx = cMounsex;
                heady = cMounsey;
            } else if (SportParam.SportDifficulty == 1) { //普通
                float cNosex = nose.getPosition().x;
                float cNosey = nose.getPosition().y;
                headx = 2 * cMounsex - cNosex;
                heady = 2 * cMounsey - cNosey;
            } else { //困难
                float cEyex = leftEye.getPosition().x / 2 + rightEye.getPosition().x / 2;
                float cEyey = leftEye.getPosition().y / 2 + rightEye.getPosition().y / 2;
                headx = 2 * cMounsex - cEyex;
                heady = 2 * cMounsey - cEyey;
            }
            PointF head = new PointF(headx, heady);

//            PointF cWrist = new PointF(leftWrist.getPosition().x / 2 + rightWrist.getPosition().x / 2, leftWrist.getPosition().y / 2 + rightWrist.getPosition().y / 2);
//            PointF mounsekalman = SportParam.kalmanFilter.filter(cMounse);
//            PointF mounsekalman =  new PointF(leftWrist.getPosition().x / 2 + rightWrist.getPosition().x / 2, leftWrist.getPosition().y / 2 + rightWrist.getPosition().y / 2);

            llnShoulder.Add(MathUtil.Distance(leftShoulder.getPosition(), rightShoulder.getPosition()));
            if (SportParam.sportStatus == SportParam.SportStatus.PersonInmat || SportParam.sportStatus == SportParam.SportStatus.Runing) {

                //开始条件判断
                if (SportParam.sportStatus != SportParam.SportStatus.Runing) {
                    lllFL.Add(leftHip.getPosition(), leftShoulder.getPosition(), leftElbow.getPosition(), leftWrist.getPosition());
                    lllFR.Add(rightHip.getPosition(), rightShoulder.getPosition(), rightElbow.getPosition(), rightWrist.getPosition());
                    lllRL.Add(leftShoulder.getPosition(), leftHip.getPosition(), leftKnee.getPosition(), leftAnkle.getPosition());
                    lllRR.Add(rightShoulder.getPosition(), rightHip.getPosition(), rightKnee.getPosition(), rightAnkle.getPosition());

                    if (leftWrist.getPosition().x < rightWrist.getPosition().x) {
                        llndstpullup.Add(MathUtil.VerticalLine(head, leftWrist.getPosition(), rightWrist.getPosition()));
                        Log.d("DST", "   " + MathUtil.VerticalLine(head, leftWrist.getPosition(), rightWrist.getPosition()));
                    } else {
                        llndstpullup.Add(-MathUtil.VerticalLine(head, leftWrist.getPosition(), rightWrist.getPosition()));
                        Log.d("DST", "   " + (-MathUtil.VerticalLine(head, leftWrist.getPosition(), rightWrist.getPosition())));
                    }


                    Configpullup.setparams((int) llnShoulder.Average());

                    PointF cWrist = new PointF(leftWrist.getPosition().x / 2 + rightWrist.getPosition().x / 2, leftWrist.getPosition().y / 2 + rightWrist.getPosition().y / 2);
                    PointF mounsekalman = new PointF(head.x - cWrist.x, head.y - cWrist.y);
                    llcMounse.Add(mounsekalman);

                    //手位置不变-嘴到手的距离变化-手肘角度变化
                    Log.d("START", "1: " + llpWrist.size() + " " + llpWrist.ErrorL() + " " + llpWrist.ErrorR() + " " + llpWrist.ErrorD() + " " + llcMounse.Judge(Configpullup.disstart));

                    if (llpWrist.size() == llpWrist.Num && llpWrist.ErrorL() < 0.5 && llpWrist.ErrorR() < 0.5 && llpWrist.ErrorD() < 0.5 && llcMounse.Judge(Configpullup.disstart)) {

                        SportParam.sportStatus = SportParam.SportStatus.Runing;
                        TimeStart = System.currentTimeMillis();
                        interfaceSport.beginstop(0);

                        Log.d("START", "2: " + llndstpullup.size() + " " + lllFL.size() + " " + llndstpullup.toString());
                        lsdst.clear();
                        for (int i = 0; i < llndstpullup.size(); i++) {
                            lsdst.add(llndstpullup.get(i));
                        }
                        lsFL.clear();
                        lsFR.clear();
                        lsRL.clear();
                        lsRR.clear();
                        for (int i = 0; i < lllFL.size(); i++) {
                            lsFL.add(lllFL.get(i));
                            lsFR.add(lllFR.get(i));
                            lsRL.add(lllRL.get(i));
                            lsRR.add(lllRR.get(i));
                        }

                        poslc = llpWrist.AvgL();
                        posrc = llpWrist.AvgR();

                        lsposl.clear();
                        lsposr.clear();
                        lsposd.clear();
                        for (int i = 0; i < llpWrist.size(); i++) {
                            lsposl.add(llpWrist.getER(i));
                            lsposr.add(llpWrist.getER(i));
                            lsposd.add(llpWrist.getER(i));
                        }
                    }
                    return;
                }

                //四肢角度处理
                lsFL.add(new LimbsStatus(leftHip.getPosition(), leftShoulder.getPosition(), leftElbow.getPosition(), leftWrist.getPosition()));
                lsFR.add(new LimbsStatus(rightHip.getPosition(), rightShoulder.getPosition(), rightElbow.getPosition(), rightWrist.getPosition()));
                lsRL.add(new LimbsStatus(leftShoulder.getPosition(), leftHip.getPosition(), leftKnee.getPosition(), leftAnkle.getPosition()));
                lsRR.add(new LimbsStatus(rightShoulder.getPosition(), rightHip.getPosition(), rightKnee.getPosition(), rightAnkle.getPosition()));

                List<Float> FL0 = new ArrayList<>();
                List<Float> FL1 = new ArrayList<>();
                List<Float> FL2 = new ArrayList<>();

                List<Float> FR0 = new ArrayList<>();
                List<Float> FR1 = new ArrayList<>();
                List<Float> FR2 = new ArrayList<>();

                List<Float> RL0 = new ArrayList<>();
                List<Float> RL1 = new ArrayList<>();
                List<Float> RL2 = new ArrayList<>();

                List<Float> RR0 = new ArrayList<>();
                List<Float> RR1 = new ArrayList<>();
                List<Float> RR2 = new ArrayList<>();

                for (int i = 0; i < lsFL.size(); i++) {
                    FL0.add((float) lsFL.get(i).getAngle012());
                    FL1.add((float) lsFL.get(i).getAngle123());
                    FL2.add((float) lsFL.get(i).getDistance13());

                    FR0.add((float) lsFR.get(i).getAngle012());
                    FR1.add((float) lsFR.get(i).getAngle123());
                    FR2.add((float) lsFR.get(i).getDistance13());

                    RL0.add((float) lsRL.get(i).getAngle012());
                    RL1.add((float) lsRL.get(i).getAngle123());
                    RL2.add((float) lsRL.get(i).getDistance13());

                    RR0.add((float) lsRR.get(i).getAngle012());
                    RR1.add((float) lsRR.get(i).getAngle123());
                    RR2.add((float) lsRR.get(i).getDistance13());
                }

                List fftresultFL0 = MathUtil.IIRFilter(FL0, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultFL1 = MathUtil.IIRFilter(FL1, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultFL2 = MathUtil.IIRFilter(FL2, iirFilterCoefficients.a, iirFilterCoefficients.b);

                List fftresultFR0 = MathUtil.IIRFilter(FR0, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultFR1 = MathUtil.IIRFilter(FR1, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultFR2 = MathUtil.IIRFilter(FR2, iirFilterCoefficients.a, iirFilterCoefficients.b);

                List fftresultRL0 = MathUtil.IIRFilter(RL0, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultRL1 = MathUtil.IIRFilter(RL1, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultRL2 = MathUtil.IIRFilter(RL2, iirFilterCoefficients.a, iirFilterCoefficients.b);

                List fftresultRR0 = MathUtil.IIRFilter(RR0, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultRR1 = MathUtil.IIRFilter(RR1, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultRR2 = MathUtil.IIRFilter(RR2, iirFilterCoefficients.a, iirFilterCoefficients.b);

                List resultFL0 = MathUtil.NumericComparison(FL0, Configpullup.error0);
                List resultFL1 = MathUtil.NumericComparison(FL1, Configpullup.error1);
                List resultFL2 = MathUtil.NumericComparison(FL2, Configpullup.dis_shoulder / 2);

                List resultFR0 = MathUtil.NumericComparison(FR0, Configpullup.error0);
                List resultFR1 = MathUtil.NumericComparison(FR1, Configpullup.error1);
                List resultFR2 = MathUtil.NumericComparison(FR2, Configpullup.dis_shoulder / 2);

                List resultRL0 = MathUtil.NumericComparison(RL0, Configpullup.error2);
                List resultRL1 = MathUtil.NumericComparison(RL1, Configpullup.error2);
                List resultRL2 = MathUtil.NumericComparison(RL2, Configpullup.dis_shoulder / 2);

                List resultRR0 = MathUtil.NumericComparison(RR0, Configpullup.error2);
                List resultRR1 = MathUtil.NumericComparison(RR1, Configpullup.error2);
                List resultRR2 = MathUtil.NumericComparison(RR2, Configpullup.dis_shoulder / 2);

                if (debugmode) {
                    List<Float> FLend0 = new ArrayList<>();
                    List<Float> FLend1 = new ArrayList<>();
                    List<Float> FLend2 = new ArrayList<>();

                    List<Float> FRend0 = new ArrayList<>();
                    List<Float> FRend1 = new ArrayList<>();
                    List<Float> FRend2 = new ArrayList<>();

                    List<Float> RLend0 = new ArrayList<>();
                    List<Float> RLend1 = new ArrayList<>();
                    List<Float> RLend2 = new ArrayList<>();

                    List<Float> RRend0 = new ArrayList<>();
                    List<Float> RRend1 = new ArrayList<>();
                    List<Float> RRend2 = new ArrayList<>();

                    for (int i = 0; i < fftresultFL0.size(); i++) {
                        FLend0.add((boolean) resultFL0.get(i) ? 180f : 0);
                        FLend1.add((boolean) resultFL1.get(i) ? 180f : 0);
                        FLend2.add((boolean) resultFL2.get(i) ? Configpullup.dis_shoulder : 0);
                        FRend0.add((boolean) resultFR0.get(i) ? 180f : 0);
                        FRend1.add((boolean) resultFR1.get(i) ? 180f : 0);
                        FRend2.add((boolean) resultFR2.get(i) ? Configpullup.dis_shoulder : 0);

                        RLend0.add((boolean) resultRL0.get(i) ? 180f : 0);
                        RLend1.add((boolean) resultRL1.get(i) ? 180f : 0);
                        RLend2.add((boolean) resultRL2.get(i) ? Configpullup.dis_shoulder : 0);
                        RRend0.add((boolean) resultRR0.get(i) ? 180f : 0);
                        RRend1.add((boolean) resultRR1.get(i) ? 180f : 0);
                        RRend2.add((boolean) resultRR2.get(i) ? Configpullup.dis_shoulder : 0);
                    }

                    showlinechart1(LClist[1], Configpullup.error1,
                            FL1, FR1, null, null,
                            fftresultFL1, fftresultFR1, null, null,
                            FLend1, FRend1, null, null);

                    showlinechart1(LClist[2], Configpullup.error2,
                            RL0, RL1, RR0, RR1,
                            fftresultRL0, fftresultRL1, fftresultRR0, fftresultRR1,
                            RLend0, RLend1, RRend0, RRend1);

                    showlinechart1(LClist[3], Configpullup.dis_shoulder / 2,
                            FL2, FR2, RL2, RR2,
                            fftresultFL2, fftresultFR2, fftresultRL2, fftresultRR2,
                            FLend2, FRend2, RLend2, RRend2);

                }

                //嘴到两手距离处理

                if (posl.x < posr.x) {
                    lsdst.add(MathUtil.VerticalLine(head, poslc, posrc));
                    Log.d("DST", "   " + MathUtil.VerticalLine(head, poslc, posrc));
                } else {
                    lsdst.add(-MathUtil.VerticalLine(head, poslc, posrc));
                    Log.d("DST", "   " + (-MathUtil.VerticalLine(head, poslc, posrc)));
                }

                if (lsdst.size() > 10) {
                    List fftresultdst = MathUtil.IIRFilter(lsdst, iirFilterCoefficients.a, iirFilterCoefficients.b);

                    List resultdst = MathUtil.NumericComparison(lsdst, 0);

                    float[] sqtresult = Configpullup.SquarewaveFilter(fftresultdst, resultdst,
                            resultFL0, resultFL1, resultFL2,
                            resultFR0, resultFR1, resultFR2,
                            resultRL0, resultRL1, resultRL2,
                            resultRR0, resultRR1, resultRR2);

                    if (sportcount != (int) sqtresult[sqtresult.length - 1]) {
                        if (sportcountok != (int) sqtresult[sqtresult.length - 3] && !SportParam.SportEnd) {
                            tts.TTS((int) sqtresult[sqtresult.length - 3] + " ");
                        }
                        if (sportcounterror != (int) sqtresult[sqtresult.length - 2] && !SportParam.SportEnd) {
                            tts.TTS("犯规");
                        }
                        Log.d(TAG, ">>>>>" + sportcount + " " + (int) sqtresult[sqtresult.length - 1] + " " + (int) sqtresult[sqtresult.length - 2] + " " + (int) sqtresult[sqtresult.length - 3]);
                        sportcount = (int) sqtresult[sqtresult.length - 1];
                        sportcounterror = (int) sqtresult[sqtresult.length - 2];
                        sportcountok = (int) sqtresult[sqtresult.length - 3];

                        interfaceSport.data(sportcount, sportcountok, sportcounterror);
                        interfaceSport.result(SportParam.LsError0, SportParam.LsError1, SportParam.LsError2);

                    }

                    if (debugmode) {
                        List<Float> sqtresult1 = new ArrayList<>();
                        for (int i = 0; i < sqtresult.length - 3; i++) {
                            sqtresult1.add(sqtresult[i]);
                        }
                        showlinechart(LClist[0], lsdst, fftresultdst, sqtresult1, sqtresult[sqtresult.length - 4]);
                    }


                }
            }

        }

        @Override
        public void pushup(PoseLandmark nose,
                           PoseLandmark leftShoulder, PoseLandmark leftElbow, PoseLandmark leftWrist, PoseLandmark leftHip, PoseLandmark leftKnee, PoseLandmark leftAnkle,
                           PoseLandmark rightShoulder, PoseLandmark rightElbow, PoseLandmark rightWrist, PoseLandmark rightHip, PoseLandmark rightKnee, PoseLandmark rightAnkle) {

            if (SportParam.SportType != 2) {
                return;
            }

            lljframe.AddFirst(1);
            if (lljframe.Judge() == lljframe.Num) {
                if (SportParam.sportStatus == SportParam.SportStatus.WaitforPerson) {
                    SportParam.sportStatus = SportParam.SportStatus.DetectPerson;
                }
            }

            if (SportParam.sportStatus == SportParam.SportStatus.DetectPerson || SportParam.sportStatus == SportParam.SportStatus.PersonInmat) {
                if ((Configpushup.isInMat(leftHip) || Configpushup.isInMat(rightHip)) &&
                        (MathUtil.Distance(leftShoulder.getPosition(), leftHip.getPosition()) > Configpushup.dis_shoulder_hip_min
                                || MathUtil.Distance(rightShoulder.getPosition(), rightHip.getPosition()) > Configpushup.dis_shoulder_hip_min)) {
                    lljInmat.Add(1);
                } else {
                    lljInmat.Add(0);
                    SportParam.sportStatus = SportParam.SportStatus.WaitforPerson;
                }

                if (lljInmat.Judge(System.currentTimeMillis())) {
                    SportParam.sportStatus = SportParam.SportStatus.PersonInmat;
                    tts.TTS("请做好准备");
                    showText("请做好准备");
                }
            }

            Log.d(TAG_PushUp, "  " + SportParam.sportStatus + " " + lljframe.Judge() + " " + lljInmat.Judge() + " " + lljAnticipationOK.Judge());

            if (SportParam.sportStatus == SportParam.SportStatus.PersonInmat || SportParam.sportStatus == SportParam.SportStatus.AnticipationOk) {

                if ((Configpushup.isAnticipationOKTop(leftShoulder, leftHip, leftElbow, leftWrist) && Configpushup.isAnticipationOKBottom(leftHip, leftShoulder, leftKnee, leftAnkle)) ||
                        (Configpushup.isAnticipationOKTop(rightShoulder, rightHip, rightElbow, rightWrist) && Configpushup.isAnticipationOKBottom(rightHip, rightShoulder, rightKnee, rightAnkle))) {
                    lljAnticipationOK.Add(1);
                } else {
                    lljAnticipationOK.Add(0);
                }

                if (lljAnticipationOK.Judge(System.currentTimeMillis())) {
                    if (leftShoulder.getPosition().x < leftAnkle.getPosition().x) {
                        if (!Configpushup.setparams(MathUtil.Distance(leftShoulder.getPosition(), leftHip.getPosition()))) {
                            return;
                        }
                        model_left_right = true;
                    } else {
                        if (!Configpushup.setparams(MathUtil.Distance(rightShoulder.getPosition(), rightHip.getPosition()))) {
                            return;
                        }
                        model_left_right = false;
                    }
                    Log.w(TAG_PushUp, "   " + (model_left_right ? "左" : "右 "));
                    SportParam.sportStatus = SportParam.SportStatus.AnticipationOk;
                    sportcount = 0;

                    tts.TTS("请开始");
                    showText("请开始");
                }
            }

            if (SportParam.sportStatus == SportParam.SportStatus.AnticipationOk || SportParam.sportStatus == SportParam.SportStatus.Runing) {

                PointF nosekalman;//= SportParam.kalmanFilter.filter(nose.getPosition());
                float dst;
                if (model_left_right) {
                    nosekalman = leftShoulder.getPosition(); //SportParam.kalmanFilter.filter(leftShoulder.getPosition());
                    llpWrist_Ankle.Add(leftWrist.getPosition(), leftAnkle.getPosition());
                    llcShoulder.Add(leftShoulder.getPosition());
                    dst = MathUtil.VerticalLine(nosekalman, leftWrist.getPosition(), leftAnkle.getPosition());
                } else {
                    nosekalman = rightShoulder.getPosition(); //SportParam.kalmanFilter.filter(rightShoulder.getPosition());
                    llpWrist_Ankle.Add(rightWrist.getPosition(), rightAnkle.getPosition());
                    llcShoulder.Add(rightShoulder.getPosition());
                    dst = -MathUtil.VerticalLine(nosekalman, rightWrist.getPosition(), rightAnkle.getPosition());
                }


                lsposw.add(llpWrist_Ankle.ErrorL());
                lsposa.add(llpWrist_Ankle.ErrorR());
                lsposd.add(llpWrist_Ankle.ErrorD());


                if (SportParam.sportStatus != SportParam.SportStatus.Runing) {
                    llndstpushup.Add(dst);
                    lllFL.Add(leftHip.getPosition(), leftShoulder.getPosition(), leftElbow.getPosition(), leftWrist.getPosition());
                    lllFR.Add(rightHip.getPosition(), rightShoulder.getPosition(), rightElbow.getPosition(), rightWrist.getPosition());
                    lllRL.Add(leftShoulder.getPosition(), leftHip.getPosition(), leftKnee.getPosition(), leftAnkle.getPosition());
                    lllRR.Add(rightShoulder.getPosition(), rightHip.getPosition(), rightKnee.getPosition(), rightAnkle.getPosition());

                    Log.d("TGG", "开始条件> " + llndstpushup.Numericalend5() + " " + Configpushup.disstart + " " + lsposw.get(lsposw.size() - 1) + " " + lsposa.get(lsposa.size() - 1));

                    if (llndstpushup.Numericalend5() > Configpushup.disstart && llcShoulder.Judge_Pushup(10) && lsposw.get(lsposw.size() - 1) < 0.5 && lsposa.get(lsposa.size() - 1) < 0.5) {
                        SportParam.sportStatus = SportParam.SportStatus.Runing;
                        TimeStart = System.currentTimeMillis();
                        interfaceSport.beginstop(0);

                        lsdstpushup.clear();
                        for (int i = 0; i < llndstpushup.size(); i++) {
                            lsdstpushup.add(llndstpushup.get(i));
                        }

                        lsFL.clear();
                        lsFR.clear();
                        lsRL.clear();
                        lsRR.clear();
                        for (int i = 0; i < lllFL.size(); i++) {
                            lsFL.add(lllFL.get(i));
                            lsFR.add(lllFR.get(i));
                            lsRL.add(lllRL.get(i));
                            lsRR.add(lllRR.get(i));
                        }

                        posw = llpWrist_Ankle.AvgL();
                        posa = llpWrist_Ankle.AvgR();

                        lsposw.clear();
                        lsposa.clear();
                        lsposd.clear();
                        for (int i = 0; i < llpWrist_Ankle.size(); i++) {
                            lsposw.add(llpWrist_Ankle.getER(i));
                            lsposa.add(llpWrist_Ankle.getER(i));
                            lsposd.add(llpWrist_Ankle.getER(i));
                        }
                    }
                    return;
                }


                //四肢角度处理
                lsFL.add(new LimbsStatus(leftHip.getPosition(), leftShoulder.getPosition(), leftElbow.getPosition(), leftWrist.getPosition()));
                lsFR.add(new LimbsStatus(rightHip.getPosition(), rightShoulder.getPosition(), rightElbow.getPosition(), rightWrist.getPosition()));
                lsRL.add(new LimbsStatus(leftShoulder.getPosition(), leftHip.getPosition(), leftKnee.getPosition(), leftAnkle.getPosition()));
                lsRR.add(new LimbsStatus(rightShoulder.getPosition(), rightHip.getPosition(), rightKnee.getPosition(), rightAnkle.getPosition()));


                List<Float> FL0 = new ArrayList<>();
                List<Float> FL1 = new ArrayList<>();
                List<Float> FL2 = new ArrayList<>();

                List<Float> FR0 = new ArrayList<>();
                List<Float> FR1 = new ArrayList<>();
                List<Float> FR2 = new ArrayList<>();

                List<Float> RL0 = new ArrayList<>();
                List<Float> RL1 = new ArrayList<>();
                List<Float> RL2 = new ArrayList<>();

                List<Float> RR0 = new ArrayList<>();
                List<Float> RR1 = new ArrayList<>();
                List<Float> RR2 = new ArrayList<>();


                for (int i = 0; i < lsFL.size(); i++) {
                    FL0.add((float) lsFL.get(i).getAngle012());
                    FL1.add((float) lsFL.get(i).getAngle123());
                    FL2.add((float) lsFL.get(i).getDistance13());

                    FR0.add((float) lsFR.get(i).getAngle012());
                    FR1.add((float) lsFR.get(i).getAngle123());
                    FR2.add((float) lsFR.get(i).getDistance13());

                    RL0.add((float) lsRL.get(i).getAngle012());
                    RL1.add((float) lsRL.get(i).getAngle123());
                    RL2.add((float) lsRL.get(i).getDistance13());

                    RR0.add((float) lsRR.get(i).getAngle012());
                    RR1.add((float) lsRR.get(i).getAngle123());
                    RR2.add((float) lsRR.get(i).getDistance13());

                }

               /* List fftresultFL0 = MathUtil.IIRFilter(FL0, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultFL1 = MathUtil.IIRFilter(FL1, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultFL2 = MathUtil.IIRFilter(FL2, iirFilterCoefficients.a, iirFilterCoefficients.b);

                List fftresultFR0 = MathUtil.IIRFilter(FR0, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultFR1 = MathUtil.IIRFilter(FR1, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultFR2 = MathUtil.IIRFilter(FR2, iirFilterCoefficients.a, iirFilterCoefficients.b);

                List fftresultRL0 = MathUtil.IIRFilter(RL0, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultRL1 = MathUtil.IIRFilter(RL1, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultRL2 = MathUtil.IIRFilter(RL2, iirFilterCoefficients.a, iirFilterCoefficients.b);

                List fftresultRR0 = MathUtil.IIRFilter(RR0, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultRR1 = MathUtil.IIRFilter(RR1, iirFilterCoefficients.a, iirFilterCoefficients.b);
                List fftresultRR2 = MathUtil.IIRFilter(RR2, iirFilterCoefficients.a, iirFilterCoefficients.b);*/


                float left, right;
                if (model_left_right) {
                    left = 150f;
                    right = 130f;
                } else {
                    left = 130f;
                    right = 150f;
                }

                List resultFL0 = MathUtil.NumericComparison(FL0, 90f);
                List resultFL1 = MathUtil.NumericComparison(FL1, left);
                List resultFL2 = MathUtil.NumericComparison(FL2, Configpushup.dis_shoulder_hip / 2);

                List resultFR0 = MathUtil.NumericComparison(FR0, 90f);
                List resultFR1 = MathUtil.NumericComparison(FR1, right);
                List resultFR2 = MathUtil.NumericComparison(FR2, Configpushup.dis_shoulder_hip / 2);

                List resultRL0 = MathUtil.NumericComparison(RL0, 160f);
                List resultRL1 = MathUtil.NumericComparison(RL1, 160f);
                List resultRL2 = MathUtil.NumericComparison(RL2, Configpushup.dis_shoulder_hip / 2);

                List resultRR0 = MathUtil.NumericComparison(RR0, 160f);
                List resultRR1 = MathUtil.NumericComparison(RR1, 160f);
                List resultRR2 = MathUtil.NumericComparison(RR2, Configpushup.dis_shoulder_hip / 2);

                if (debugmode) {
                    List<Float> FLend0 = new ArrayList<>();
                    List<Float> FLend1 = new ArrayList<>();
                    List<Float> FLend2 = new ArrayList<>();

                    List<Float> FRend0 = new ArrayList<>();
                    List<Float> FRend1 = new ArrayList<>();
                    List<Float> FRend2 = new ArrayList<>();

                    List<Float> RLend0 = new ArrayList<>();
                    List<Float> RLend1 = new ArrayList<>();
                    List<Float> RLend2 = new ArrayList<>();

                    List<Float> RRend0 = new ArrayList<>();
                    List<Float> RRend1 = new ArrayList<>();
                    List<Float> RRend2 = new ArrayList<>();

                    for (int i = 0; i < resultFL0.size(); i++) {

                        FLend0.add((boolean) resultFL0.get(i) ? 180f : 0);
                        FLend1.add((boolean) resultFL1.get(i) ? 180f : 0);
                        FLend2.add((boolean) resultFL2.get(i) ? Configpushup.dis_shoulder_hip : 0);
                        FRend0.add((boolean) resultFR0.get(i) ? 180f : 0);
                        FRend1.add((boolean) resultFR1.get(i) ? 180f : 0);
                        FRend2.add((boolean) resultFR2.get(i) ? Configpushup.dis_shoulder_hip : 0);

                        RLend0.add((boolean) resultRL0.get(i) ? 180f : 120f);
                        RLend1.add((boolean) resultRL1.get(i) ? 180f : 120f);
                        RLend2.add((boolean) resultRL2.get(i) ? Configpushup.dis_shoulder_hip : 0);
                        RRend0.add((boolean) resultRR0.get(i) ? 180f : 120f);
                        RRend1.add((boolean) resultRR1.get(i) ? 180f : 120f);
                        RRend2.add((boolean) resultRR2.get(i) ? Configpushup.dis_shoulder_hip : 0);

                    }

                    showlinechart1(LClist[1], 90,
                            FL0, FL1, FR0, FR1, null, null, null, null,
                            /*fftresultFL0, fftresultFL1, fftresultFR0, fftresultFR1,*/
                            FLend0, FLend1, FRend0, FRend1);

                    showlinechart1(LClist[2], 160,
                            RL0, RL1, RR0, RR1, null, null, null, null,
                            /*fftresultRL0, fftresultRL1, fftresultRR0, fftresultRR1,*/
                            RLend0, RLend1, RRend0, RRend1);

                    showlinechart1(LClist[3], Configpushup.dis_shoulder_hip / 2,
                            FL2, FR2, RL2, RR2, null, null, null, null,
                            /*fftresultFL2, fftresultFR2, fftresultRL2, fftresultRR2,*/
                            FLend2, FRend2, RLend2, RRend2);

                }

                //距离处理

                lsdstpushup.add(dst);

                List fftresultdst = MathUtil.IIRFilter(lsdstpushup, iirFilterCoefficients.a, iirFilterCoefficients.b);

                List resultdst = MathUtil.NumericComparison(lsdstpushup, 0);

                if (fftresultdst.size() > 10) {
                    float[] sqtresult;
                    if (model_left_right) {
                        sqtresult = Configpushup.SquarewaveFilter(fftresultdst, resultdst,
                                resultFL0, resultFL1, resultFL2,
                                resultRL0, resultRL1, resultRL2,
                                resultFR0, resultFR1, resultFR2,
                                resultRR0, resultRR1, resultRR2);
                    } else {
                        sqtresult = Configpushup.SquarewaveFilter(fftresultdst, resultdst,
                                resultFR0, resultFR1, resultFR2,
                                resultRR0, resultRR1, resultRR2,
                                resultFL0, resultFL1, resultFL2,
                                resultRL0, resultRL1, resultRL2);
                    }


                    if (debugmode) {
                        List<Float> sqtresult1 = new ArrayList<>();
                        for (int i = 0; i < sqtresult.length - 4; i++) {
                            sqtresult1.add(sqtresult[i]);
                        }
                        showlinechart(LClist[0], lsdstpushup, fftresultdst, sqtresult1, sqtresult[sqtresult.length - 4]);
                    }

                    if (sportcount != (int) sqtresult[sqtresult.length - 1]) {
                        if (sportcountok != (int) sqtresult[sqtresult.length - 3] && !SportParam.SportEnd) {
                            tts.TTS((int) sqtresult[sqtresult.length - 3] + " ");
                        }
                        if (sportcounterror != (int) sqtresult[sqtresult.length - 2] && !SportParam.SportEnd) {
                            tts.TTS("犯规");
                        }
                        Log.d("TAD1", ">>>>>" + sportcount + " " + (int) sqtresult[sqtresult.length - 1] + " " + (int) sqtresult[sqtresult.length - 2] + " " + (int) sqtresult[sqtresult.length - 3]);
                        sportcount = (int) sqtresult[sqtresult.length - 1];
                        sportcounterror = (int) sqtresult[sqtresult.length - 2];
                        sportcountok = (int) sqtresult[sqtresult.length - 3];

                        interfaceSport.data(sportcount, sportcountok, sportcounterror);
                        interfaceSport.result(SportParam.LsError0, SportParam.LsError1, SportParam.LsError2);
                    }
                }

                //结束条件处理
                {
                    int Time = (int) ((System.currentTimeMillis() - TimeStart) / 1000);

                    int TimeDiff = SportParam.SportTime - Time;
                    Log.d(TAG_SitUp, "Time  " + Time + "  " + SportParam.SportTime + " " + TimeDiff);

                    if (TimeDiff == (SportParam.SportTime / 2) && !SportParam.SportHalf) {
                        tts.TTS("已用时一半");
                        showText("已用时一半");
                        SportParam.SportHalf = true;
                    }

                    if (TimeDiff <= (SportParam.TimeContDown + 1) && !SportParam.SportEnd) {
                        tts.TTS("倒计时");
                        showText("倒计时");
                        SportParam.SportEnd = true;
                    }

                    if (1 < TimeDiff && TimeDiff <= SportParam.TimeContDown) {
                        tts.TTS((SportParam.TimeContDown - 1) + " ");
                        showText((SportParam.TimeContDown - 1) + " ");
                        SportParam.TimeContDown--;
                    }


                    float dstw = MathUtil.Distance(posw, llpWrist_Ankle.AvgL());
                    float dsta = MathUtil.Distance(posa, llpWrist_Ankle.AvgR());

                    if (Time > SportParam.SportTime || lljInmat.Judge() < lljInmat.Num / 3 || (dstw > Configpushup.error0 || dsta > Configpushup.error0)) {
                        sportfinish();
                        return;
                    }
                }

            }

        }

    };

    public void sportfinish() {
        SportParam.sportStatus = SportParam.SportStatus.Finish;
        SportParam.TimeContDown = 5 + 1;
        tts.TTS("你的成绩是" + sportcountok + "个,犯规" + sportcounterror + "个");
        interfaceSport.beginstop(1);
        FileUtil.capture(activity);
        Log.d(TAG_PullUp, "sportfinish   " + SportParam.sportStatus + " " + lljframe.Judge() + " " + lljInmat.Judge());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SportParam.sportStatus = SportParam.SportStatus.WaitforPerson;
                interfaceSport.beginstop(2);
                Log.d(TAG_PullUp, "sportfinish>>>>   " + SportParam.sportStatus + " " + lljframe.Judge() + " " + lljInmat.Judge());
            }
        }).start();

        SportParam.SportHalf = false;
        SportParam.SportEnd = false;

        sportcount = 0;
        sportcountok = 0;
        sportcounterror = 0;
        model_left_right = false;

        SportParam.personstar.x = SportParam.matstar.x;
        SportParam.personstar.y = SportParam.matstar.y;
        SportParam.personend.x = SportParam.matend.x;
        SportParam.personend.y = SportParam.matend.y;


        lljframe.Reset();
        lljInmat.Reset();
        lljAnticipationOK.Reset();


        llndst.Reset();
        llnloc.Reset();
        llnerror0.Reset();
        llnerror1L.Reset();
        llnerror1R.Reset();
        llnerror2.Reset();
        listDst.clear();
        listLoc.clear();
        listEC0.clear();
        listEC00.clear();
        listEC1L.clear();
        listEC11L.clear();
        listEC1R.clear();
        listEC11R.clear();
        listEC2.clear();
        listEC22.clear();


        llndstpullup.Reset();
        lsdst.clear();
        llndsterrorl.Reset();
        llndsterrorr.Reset();
        lllFL.Reset();
        lllFR.Reset();
        lllRL.Reset();
        lllRR.Reset();
        lsFL.clear();
        lsFR.clear();
        lsRL.clear();
        lsRR.clear();
        llpWrist.Reset();
        lsposl.clear();
        lsposr.clear();
        lsposd.clear();

        llcShoulder.Reset();
        lsdstpushup.clear();
        llndstpushup.Reset();
    }

    public void showlinechart(LineChart lchart, List<Float> data0, List<Float> data1, List<Float> data2, float limit) {

        List<Entry> entries0 = new ArrayList<>();
        for (int i = 0; i < data0.size(); i++) {
            entries0.add(new Entry(i, data0.get(i)));
        }
        LineDataSet lineDataSet = new LineDataSet(entries0, "" + entries0.size());
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setColor(Color.YELLOW);

        List<Entry> entries1 = new ArrayList<>();
        for (int i = 0; i < data1.size(); i++) {
            entries1.add(new Entry(i, data1.get(i)));
        }
        LineDataSet lineDataSet1 = new LineDataSet(entries1, "" + entries1.size());
        lineDataSet1.setDrawCircles(false);
        lineDataSet1.setDrawCircleHole(false);
        lineDataSet1.setDrawValues(false);
        lineDataSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet1.setColor(Color.GREEN);

        List<Entry> entries2 = new ArrayList<>();
        for (int i = 0; i < data2.size(); i++) {
            entries2.add(new Entry(i, data2.get(i)));
        }
        LineDataSet lineDataSet2 = new LineDataSet(entries2, "" + entries2.size());
        lineDataSet2.setDrawCircles(false);
        lineDataSet2.setDrawCircleHole(false);
        lineDataSet2.setDrawValues(false);
        lineDataSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet2.setColor(Color.BLUE);

        YAxis leftAxis = lchart.getAxisLeft();
        LimitLine upLimit = new LimitLine(limit, " " + limit);
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(upLimit);

        LineData ldate = new LineData();
        ldate.addDataSet(lineDataSet);
        ldate.addDataSet(lineDataSet1);
        ldate.addDataSet(lineDataSet2);

        lchart.setData(ldate);
        lchart.notifyDataSetChanged();
        lchart.moveViewToX(100);
    }

    public void showlinechart1(LineChart lchart, float limit,
                               List<Float> data0, List<Float> data1, List<Float> data2, List<Float> data3,
                               List<Float> data4, List<Float> data5, List<Float> data6, List<Float> data7,
                               List<Float> data8, List<Float> data9, List<Float> dataA, List<Float> dataB) {


        YAxis leftAxis = lchart.getAxisLeft();
        LimitLine upLimit = new LimitLine(limit, " " + limit);
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(upLimit);


        LineData ldate = new LineData();
        ldate.addDataSet(createLDS(data0, "#F9F900", ""));
        ldate.addDataSet(createLDS(data1, "#F9F900", ""));
        ldate.addDataSet(createLDS(data2, "#F9F900", ""));
        ldate.addDataSet(createLDS(data3, "#F9F900", ""));

        ldate.addDataSet(createLDS(data4, "#A8FF24", ""));
        ldate.addDataSet(createLDS(data5, "#A8FF24", ""));
        ldate.addDataSet(createLDS(data6, "#A8FF24", ""));
        ldate.addDataSet(createLDS(data7, "#A8FF24", ""));

        ldate.addDataSet(createLDS(data8, "#FF0000", "0："));
        ldate.addDataSet(createLDS(data9, "#FF44FF", "1："));
        ldate.addDataSet(createLDS(dataA, "#B15BFF", "2："));
        ldate.addDataSet(createLDS(dataB, "#0000E3", "3："));

        lchart.setData(ldate);
        lchart.notifyDataSetChanged();
        lchart.moveViewToX(100);
    }

    public LineDataSet createLDS(List<Float> data, String color, String str) {
        if (data == null) {
            return null;
        }
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new Entry(i, data.get(i)));
        }
        LineDataSet lineDataSet = new LineDataSet(entries, str + "" + entries.size());
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setColor(Color.parseColor(color));

        return lineDataSet;
    }

    public void setLineChartTitle(int Type) {
        String[][] ls = new String[][]{
                {"计数", "肘到膝", "手到耳L", "手到耳R", "肩触垫", "停止"},
                {"嘴距离手", "手位置", "上半身角度", "下半身角度", "上半身距离变化"},
                {"嘴距离手", "手位置", "上半身角度", "下半身角度", "上半身距离变化"}};

        for (int i = 0; i < ls[Type].length; i++) {
            Description description = new Description();
            Log.d(TAG, "setLineChartTitle: " + ls[Type][i]);
            description.setText(ls[Type][i]);
            LClist[i].setDescription(description);
        }

    }

    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
        mcontext = getContext();
        permisionUtil = new PermisionUtil(activity, mcontext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camerax_live_preview, container, false);

        dao = new Dao(mcontext);

        // 第一个表示选择滤波器类型（低通，高通，带通，带阻）;
        // 第二个参数表示滤波器的阶数;
        // 第三个参数表示下截止频率;
        // 第四个参数表示上截止频率;
        // 在低通和高通滤波器中只需要一个截止频率，所以在低通和高通中上截止频率是没有作用的。只有在带通或者带阻滤波器中才需要两个截止频率。
        iirFilterCoefficients = IirFilterDesignExstrom.design(FilterPassType.lowpass, 10, 5.0 / 25, 5.0 / 25);


        if (savedInstanceState != null) {
            selectedModel = savedInstanceState.getString(STATE_SELECTED_MODEL, OBJECT_DETECTION);
        }
        SportParam.lensFacing = CameraSelector.LENS_FACING_BACK;

        cameraSelector = new CameraSelector.Builder().requireLensFacing(SportParam.lensFacing).build();

        previewView = view.findViewById(R.id.preview_view);
        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }

        graphicOverlay = view.findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        mat_view_l = view.findViewById(R.id.mat_view_l);
        mat_view_r = view.findViewById(R.id.mat_view_r);
        pullup_view = view.findViewById(R.id.pullup_view);
        pullup_view.setAlpha(0.4f);

        hair_dv_parent = view.findViewById(R.id.hair_dv_parent);
        hair_dv = view.findViewById(R.id.hair_dv);
        tv_info = view.findViewById(R.id.tv_info);

        image_view = view.findViewById(R.id.image_view);
        image_view.setScaleType(ImageView.ScaleType.CENTER_CROP);


        for (int i = 0; i < LClist.length; i++) {
            LClist[i] = view.findViewById(id_lc[i]);
            LClist[i].setDrawBorders(true); //显示边界
            LClist[i].setDrawGridBackground(false); //是否展示网格线
            LClist[i].getAxisRight().setDrawLabels(false);
            LClist[i].getXAxis().setDrawLabels(false);
            LClist[i].getAxisLeft().setDrawLabels(false);
        }


        if (debugmode && !SportParam.SportDect) {
            setLineChartTitle(SportParam.SportType);
            LClist[0].setVisibility(View.VISIBLE);
            LClist[1].setVisibility(View.VISIBLE);
            LClist[2].setVisibility(View.VISIBLE);
            LClist[3].setVisibility(View.VISIBLE);
            LClist[4].setVisibility(View.VISIBLE);
            LClist[5].setVisibility(View.VISIBLE);
            //image_view.setVisibility(View.INVISIBLE);
        } else {
            LClist[0].setVisibility(View.INVISIBLE);
            LClist[1].setVisibility(View.INVISIBLE);
            LClist[2].setVisibility(View.INVISIBLE);
            LClist[3].setVisibility(View.INVISIBLE);
            LClist[4].setVisibility(View.INVISIBLE);
            LClist[5].setVisibility(View.INVISIBLE);
            //image_view.setVisibility(View.VISIBLE);
        }
        //image_view.setVisibility(View.VISIBLE);
        facingSwitch = view.findViewById(R.id.facing_switch);
        facingSwitch.setBackgroundResource(SportParam.lensFacing == CameraSelector.LENS_FACING_FRONT ? R.drawable.btn_fcam : R.drawable.btn_bcam);
        facingSwitch.setOnCheckedChangeListener(this);

        new ViewModelProvider(this, AndroidViewModelFactory.getInstance(activity.getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(
                        getViewLifecycleOwner(),
                        provider -> {
                            cameraProvider = provider;
                            if (permisionUtil.allPermissionsGranted()) {
                                bindAllCameraUseCases();
                            }
                        });

        if (!permisionUtil.allPermissionsGranted()) {
            permisionUtil.getRuntimePermissions();
        }

        bindAllCameraUseCases();

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(STATE_SELECTED_MODEL, selectedModel);
    }

    @Override
    public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selectedModel = parent.getItemAtPosition(pos).toString();
        Log.d(TAG, "Selected model: " + selectedModel);
        bindAnalysisUseCase();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (cameraProvider == null) {
            return;
        }
        int newLensFacing = SportParam.lensFacing == CameraSelector.LENS_FACING_FRONT
                ? CameraSelector.LENS_FACING_BACK
                : CameraSelector.LENS_FACING_FRONT;
        PreferenceUtils.setCameraXFrontOrBack(mcontext, newLensFacing == CameraSelector.LENS_FACING_FRONT);
        facingSwitch.setBackgroundResource(SportParam.lensFacing == CameraSelector.LENS_FACING_FRONT ? R.drawable.btn_fcam : R.drawable.btn_bcam);
        CameraSelector newCameraSelector = new CameraSelector.Builder().requireLensFacing(newLensFacing).build();
        try {
            if (cameraProvider.hasCamera(newCameraSelector)) {
                Log.d(TAG, "lensFacing Set facing to " + newLensFacing);
                SportParam.lensFacing = newLensFacing;
                cameraSelector = newCameraSelector;
                bindAllCameraUseCases();
                return;
            }
        } catch (CameraInfoUnavailableException e) {
            // Falls through
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        bindAllCameraUseCases();
        LogcatHelper.getInstance(mcontext).start();
        tts = new Texttospeech(mcontext);
        hair_dv_parent.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Log.d("raydrag", "onGlobalLayout: " + hair_dv_parent.getWidth() + " " + hair_dv_parent.getHeight());
                        SportParam.setLayoutparams(hair_dv_parent.getWidth(), hair_dv_parent.getHeight());
                        hair_dv.setDragScaleViewLayout(hair_dv);
                        hair_dv_parent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
        tts.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
        tts.onDestroy();
    }

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider.unbindAll();
            bindPreviewUseCase();
            bindAnalysisUseCase();
        }
    }

    private void bindPreviewUseCase() {
        if (!PreferenceUtils.isCameraLiveViewportEnabled(mcontext)) {
            return;
        }
        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        Preview.Builder builder = new Preview.Builder();
        Size targetResolution = PreferenceUtils.getCameraXTargetResolution(mcontext, SportParam.lensFacing);
        Log.e(TAG, ": " + targetResolution.getHeight() + " " + targetResolution + " " + SportParam.lensFacing);
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution);
        }
        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, previewUseCase);
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        if (imageProcessor != null) {
            imageProcessor.stop();
        }

        try {
            PoseDetectorOptionsBase poseDetectorOptions = PreferenceUtils.getPoseDetectorOptionsForLivePreview(mcontext);
            boolean shouldShowInFrameLikelihood = PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(mcontext);
            boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(mcontext);
            boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(mcontext);
            boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(mcontext);
            imageProcessor = new PoseDetectorProcessor(
                    mcontext,
                    poseDetectorOptions,
                    shouldShowInFrameLikelihood,
                    visualizeZ,
                    rescaleZ,
                    runClassification,
                    /* isStreamMode = */ true,
                    sportListener);

        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + selectedModel, e);
            Toast.makeText(
                    mcontext,
                    "Can not create image processor: " + e.getLocalizedMessage(),
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
//        Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
//        if (targetResolution != null) {
//            builder.setTargetResolution(targetResolution);
//        } else {
        builder.setTargetResolution(new Size(1280, 720));
//        }
        analysisUseCase = builder.build();

        needUpdateGraphicOverlayImageSourceInfo = true;
        analysisUseCase.setAnalyzer(
                // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                // thus we can just runs the analyzer itself on main thread.
                ContextCompat.getMainExecutor(mcontext),
                imageProxy -> {

                    lljframe.Add(0);
                    if (lljframe.Judge() == 0) {
                        if (SportParam.sportStatus == SportParam.SportStatus.Runing) {
                            sportfinish();
                        } else if (SportParam.sportStatus != SportParam.SportStatus.Finish) {
                            //沒有物体在画面
                            SportParam.sportStatus = SportParam.SportStatus.WaitforPerson;
                            Log.d(TAG_PullUp, "lljframe.Judge() == 0: " + SportParam.sportStatus);
                        }

                    }

                    if (needUpdateGraphicOverlayImageSourceInfo) {
                        boolean isImageFlipped = SportParam.lensFacing == CameraSelector.LENS_FACING_FRONT;
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        if (rotationDegrees == 0 || rotationDegrees == 180) {
                            graphicOverlay.setImageSourceInfo(imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                        } else {
                            graphicOverlay.setImageSourceInfo(imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                        }
                        needUpdateGraphicOverlayImageSourceInfo = false;
                    }

                    try {
                        imageProcessor.processImageProxy(imageProxy, graphicOverlay);
                    } catch (MlKitException e) {
                        Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                        Toast.makeText(mcontext, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (SportParam.bitmapfromImageProxy != null && image_view != null) {
//                                if (!debugmode || SportParam.SportDect) {
//                                image_view.setImageBitmap(SportParam.bitmapfromImageProxy);
//                                }
                            }
                        }
                    });

                });

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (permisionUtil.allPermissionsGranted()) {
            bindAllCameraUseCases();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void showText(String text) {
        tv_info.setText(text);
        tvinfoHandler.removeMessages(0);
        tvinfoHandler.sendEmptyMessageDelayed(0, 1500);
    }

    Handler tvinfoHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            tv_info.setText("");
        }
    };


}
