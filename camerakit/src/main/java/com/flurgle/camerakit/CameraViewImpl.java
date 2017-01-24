package com.flurgle.camerakit;

import android.view.View;

public abstract class CameraViewImpl {

    protected CameraListener mCameraListener;

    protected final PreviewImpl mPreview;

    private boolean mCanRecordAudio;

    CameraViewImpl(CameraListener callback, PreviewImpl preview) {
        mCameraListener = callback;
        mPreview = preview;
    }

    View getView() {
        return mPreview.getView();
    }

    abstract void start();

    abstract void stop();

    abstract boolean isCameraOpened();

    abstract void setFacing(int facing);

    abstract int getFacing();

    abstract void setFlash(int flash);

    abstract int getFlash();

    abstract boolean getAutoFocus();

    abstract void capturePicture();

    abstract void captureStill();

    abstract void startVideo();

    abstract void endVideo();

    abstract void setDisplayOrientation(int displayOrientation);

    void setCameraListener(CameraListener cameraListener) {
        this.mCameraListener = cameraListener;
    }

    protected CameraListener getCameraListener() {
        return mCameraListener != null ? mCameraListener : new CameraListener() {};
    }

    public void setCanRecordAudio(boolean canRecordAudio) {
        this.mCanRecordAudio = canRecordAudio;
    }

    protected boolean canRecordAudio() {
        return mCanRecordAudio;
    }

}