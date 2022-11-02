package com.kyhero.sport.communal;

import com.google.mlkit.vision.pose.PoseLandmark;

public interface SportListener {


    void situp(PoseLandmark nose,
               PoseLandmark leftEar, PoseLandmark leftShoulder, PoseLandmark leftElbow, PoseLandmark leftWrist, PoseLandmark leftIndex, PoseLandmark leftHip, PoseLandmark leftKnee, PoseLandmark leftAnkle,
               PoseLandmark rightEar, PoseLandmark rightShoulder, PoseLandmark rightElbow, PoseLandmark rightWrist, PoseLandmark rightIndex, PoseLandmark rightHip, PoseLandmark rightKnee, PoseLandmark rightAnkle);


    void pullup(PoseLandmark nose, PoseLandmark leftMouth, PoseLandmark rightMouth, PoseLandmark leftEye, PoseLandmark rightEye,
                PoseLandmark leftShoulder, PoseLandmark leftElbow, PoseLandmark leftWrist,
                PoseLandmark rightShoulder, PoseLandmark rightElbow, PoseLandmark rightWrist,
                PoseLandmark leftHip, PoseLandmark leftKnee, PoseLandmark leftAnkle,
                PoseLandmark rightHip, PoseLandmark rightKnee, PoseLandmark rightAnkle);

    void pushup(PoseLandmark nose,
                PoseLandmark leftShoulder, PoseLandmark leftElbow, PoseLandmark leftWrist, PoseLandmark leftHip, PoseLandmark leftKnee, PoseLandmark leftAnkle,
                PoseLandmark rightShoulder, PoseLandmark rightElbow, PoseLandmark rightWrist, PoseLandmark rightHip, PoseLandmark rightKnee, PoseLandmark rightAnkle);


}
