package com.otaliastudios.cameraview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * - The android camera will stream image to the given {@link SurfaceTexture}.
 *
 * - in the SurfaceTexture constructor we pass the GL texture handle that we have created.
 *
 * - The SurfaceTexture is linked to the Camera1 object. It will pass down buffers of data with
 *   a specified size (that is, the Camera1 preview size).
 *
 * - When SurfaceTexture.updateTexImage() is called, it will take the latest image from the camera stream
 *   and update it into the GL texture that was passed.
 *
 * - Now we have a GL texture referencing data. It must be drawn.
 *  [Note: it must be drawn using a transformation matrix taken from SurfaceTexture]
 *
 * - The easy way to render an OpenGL texture is using the {@link GLSurfaceView} class.
 *   It manages the gl context, hosts a surface and runs a separated rendering thread that will perform
 *   the rendering.
 *
 * - As per docs, we ask the GLSurfaceView to delegate rendering to us, using
 *   {@link GLSurfaceView#setRenderer(GLSurfaceView.Renderer)}. We request a render on the SurfaceView
 *   anytime the SurfaceTexture notifies that it has new data available (see OnFrameAvailableListener below).
 *
 * - Everything is linked:
 *   - The SurfaceTexture has buffers of data of mInputStreamSize
 *   - The SurfaceView hosts a view (and surface) of size mOutputSurfaceSize
 *   - We have a GL rich texture to be drawn (in the given method & thread).
 *
 * TODO
 * CROPPING: Managed to do this using Matrix transformation.
 * UPDATING: Still bugged: if you change the surface size on the go, the stream is not updated.
 *           I guess we should create a new texture...
 * TAKING PICTURES: Sometime the snapshot takes ages...
 * TAKING VIDEOS: Still have not tried...
 */
class GLCameraPreview extends CameraPreview<GLSurfaceView, SurfaceTexture> implements GLSurfaceView.Renderer {

    private boolean mDispatched;
    private final float[] mTransformMatrix = new float[16];
    private int mOutputTextureId = -1;
    private SurfaceTexture mInputSurfaceTexture;
    private GLViewport mOutputViewport;

    GLCameraPreview(Context context, ViewGroup parent, SurfaceCallback callback) {
        super(context, parent, callback);
    }

    @NonNull
    @Override
    protected GLSurfaceView onCreateView(Context context, ViewGroup parent) {
        View root = LayoutInflater.from(context).inflate(R.layout.cameraview_gl_view, parent, false);
        parent.addView(root, 0);
        GLSurfaceView glView = root.findViewById(R.id.gl_surface_view);
        glView.setEGLContextClientVersion(2);
        glView.setRenderer(this);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        // glView.getHolder().setFixedSize(600, 300);
        glView.getHolder().addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {}
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.e("GlCameraPreview", "width: " + width + ", height: " + height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                onSurfaceDestroyed();
            }
        });
        return glView;
    }

    @Override
    void onResume() {
        super.onResume();
        getView().onResume();
    }

    @Override
    void onPause() {
        super.onPause();
        getView().onPause();
    }

    @Override
    void onDestroy() {
        super.onDestroy();
        if (mInputSurfaceTexture != null) {
            mInputSurfaceTexture.release();
            mInputSurfaceTexture = null;
        }
        if (mOutputViewport != null) {
            mOutputViewport.release();
            mOutputViewport = null;
        }
    }

    // Renderer thread
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mOutputViewport = new GLViewport();
        mOutputTextureId = mOutputViewport.createTexture();
        mInputSurfaceTexture = new SurfaceTexture(mOutputTextureId);

        // Since we are using GLSurfaceView.RENDERMODE_WHEN_DIRTY, we must notify the SurfaceView
        // of dirtyness, so that it draws again. This is how it's done.
        mInputSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                // requestRender is thread-safe.
                getView().requestRender();
            }
        });
    }

    // Renderer thread
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (!mDispatched) {
            onSurfaceAvailable(width, height);
            mDispatched = true;
        } else {
            onSurfaceSizeChanged(width, height);
        }
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        // Latch the latest frame.  If there isn't anything new,
        // we'll just re-use whatever was there before.
        mInputSurfaceTexture.updateTexImage();
        if (mInputStreamWidth <= 0 || mInputStreamHeight <= 0) {
            // Skip drawing. Camera was not opened.
            return;
        }

        // Draw the video frame.
        mInputSurfaceTexture.getTransformMatrix(mTransformMatrix);
        Matrix.scaleM(mTransformMatrix, 0, mScaleX, mScaleY, 1);
        mOutputViewport.drawFrame(mOutputTextureId, mTransformMatrix);
    }

    @Override
    Class<SurfaceTexture> getOutputClass() {
        return SurfaceTexture.class;
    }

    @Override
    SurfaceTexture getOutput() {
        return mInputSurfaceTexture;
    }


    @Override
    boolean supportsCropping() {
        return true;
    }

    private float mScaleX = 1F;
    private float mScaleY = 1F;

    /**
     * To crop in GL, we could actually use view.setScaleX and setScaleY, but only from Android N onward.
     * See documentation: https://developer.android.com/reference/android/view/SurfaceView
     *
     *   Note: Starting in platform version Build.VERSION_CODES.N, SurfaceView's window position is updated
     *   synchronously with other View rendering. This means that translating and scaling a SurfaceView on
     *   screen will not cause rendering artifacts. Such artifacts may occur on previous versions of the
     *   platform when its window is positioned asynchronously.
     *
     * But to support older platforms, this seem to work - computing scale values and requesting a new frame,
     * then drawing it with a scaled transformation matrix. See {@link #onDrawFrame(GL10)}.
     */
    @Override
    protected void crop() {
        mCropTask.start();
        if (mInputStreamWidth > 0 && mInputStreamHeight > 0 && mOutputSurfaceWidth > 0 && mOutputSurfaceHeight > 0) {
            float scaleX = 1f, scaleY = 1f;
            AspectRatio current = AspectRatio.of(mOutputSurfaceWidth, mOutputSurfaceHeight);
            AspectRatio target = AspectRatio.of(mInputStreamWidth, mInputStreamHeight);
            if (current.toFloat() >= target.toFloat()) {
                // We are too short. Must increase height.
                scaleY = current.toFloat() / target.toFloat();
            } else {
                // We must increase width.
                scaleX = target.toFloat() / current.toFloat();
            }
            mScaleX = 1F / scaleX;
            mScaleY = 1F / scaleY;
            getView().requestRender();
        }
        mCropTask.end(null);
    }
}
