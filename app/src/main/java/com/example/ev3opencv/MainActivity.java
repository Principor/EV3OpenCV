package com.example.ev3opencv;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class MainActivity extends AppCompatActivity  {
    public static final String TAG = "EV3DroidCV";
    private CameraBridgeViewBase mOpenCvCameraView;
    private EV3Communicator ev3Communicator;
    SpeechHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.ev3droid_surface_view);

        mOpenCvCameraView = findViewById(R.id.ev3droid_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        CameraHandler cameraHandler = new CameraHandler(this);
        ev3Communicator = new EV3Communicator();
        cameraHandler.setEV3Communicator(ev3Communicator);
        mOpenCvCameraView.setCvCameraViewListener(cameraHandler);
        handler = new SpeechHandler(ev3Communicator, this);

    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                //Log.i(TAG, "OpenCV Manager Connected");
                mOpenCvCameraView.enableView();
                ev3Communicator.execute();
            } else {
                //Log.i(TAG, "OpenCV Manager Install");
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            //Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            //Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


}
