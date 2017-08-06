package com.flurgle.camerakit;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.flurgle.camerakit.CameraConstants.WHITE_BALANCE_AUTO;
import static com.flurgle.camerakit.CameraConstants.WHITE_BALANCE_INCANDESCENT;
import static com.flurgle.camerakit.CameraConstants.WHITE_BALANCE_FLUORESCENT;
import static com.flurgle.camerakit.CameraConstants.WHITE_BALANCE_DAYLIGHT;
import static com.flurgle.camerakit.CameraConstants.WHITE_BALANCE_CLOUDY;

@Retention(RetentionPolicy.SOURCE)
@IntDef({WHITE_BALANCE_AUTO, WHITE_BALANCE_INCANDESCENT, WHITE_BALANCE_FLUORESCENT,
        WHITE_BALANCE_DAYLIGHT, WHITE_BALANCE_CLOUDY})
public @interface WhiteBalance {
}