package com.otaliastudios.cameraview;


import android.graphics.PointF;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

public class MockCameraController extends CameraController {

    MockCameraController(CameraView.CameraCallbacks callback, Preview preview) {
        super(callback, preview);
    }

    void setMockCameraOptions(CameraOptions options) {
        mOptions = options;
    }

    void setMockPreviewSize(Size size) {
        mPreviewSize = size;
    }

    @Override
    void onStart() {

    }

    @Override
    void onStop() {

    }

    @Override
    boolean setZoom(float zoom) {
        return true;
    }

    @Override
    boolean setExposureCorrection(float EVvalue) {
        return true;
    }

    @Override
    void setFacing(Facing facing) {
        mFacing = facing;
    }

    @Override
    void setFlash(Flash flash) {
        mFlash = flash;
    }

    @Override
    void setWhiteBalance(WhiteBalance whiteBalance) {
        mWhiteBalance = whiteBalance;
    }

    @Override
    void setVideoQuality(VideoQuality videoQuality) {
        mVideoQuality = videoQuality;
    }

    @Override
    void setSessionType(SessionType sessionType) {
        mSessionType = sessionType;
    }

    @Override
    void setHdr(Hdr hdr) {
        mHdr = hdr;
    }

    @Override
    void setLocation(Location location) {

    }

    @Override
    boolean capturePicture() {
        return true;
    }

    @Override
    boolean captureSnapshot() {
        return true;
    }

    @Override
    boolean startVideo(@NonNull File file) {
        return true;
    }

    @Override
    boolean endVideo() {
        return true;
    }

    @Override
    boolean shouldFlipSizes() {
        return false;
    }

    @Override
    boolean isCameraOpened() {
        return true;
    }

    @Override
    boolean startAutoFocus(@Nullable Gesture gesture, PointF point) {
        return true;
    }

    @Override
    public void onSurfaceChanged() {

    }

    @Override
    public void onSurfaceAvailable() {

    }
}
