package com.example.ev3opencv;

import android.app.Activity;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.net.SocketException;
import java.util.List;

import static org.opencv.core.Core.FONT_HERSHEY_DUPLEX;
import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;

/**
 * Created by rics on 2017.01.10..
 */

public class CameraHandler implements CvCameraViewListener2 {

    private Mat                  mRgba, mRgbaF, mRgbaT;
    private ColorBlobDetector    mDetector;
    private Scalar               mBlobColorHsv;
    private Scalar CONTOUR_COLOR;
    private Scalar MARKER_COLOR;
    private Scalar TEXT_COLOR;
    private String ipAddress;
    private EV3Communicator ev3Communicator;
    private Point org;
    private Activity parent;
    private long lastTime;

    public CameraHandler(Activity parent) {
        this.parent = parent;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);  // NOTE width,width is NOT a typo
        mDetector = new ColorBlobDetector();
        mBlobColorHsv = new Scalar(280/2,0.65*255,0.75*255,255); // hue in [0,180], saturation in [0,255], value in [0,255]
        mDetector.setHsvColor(mBlobColorHsv);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        MARKER_COLOR = new Scalar(0,0,255,255);
        TEXT_COLOR = new Scalar(255,255,255,255);
        org = new Point(1,20);
        try {
            ipAddress = EV3Communicator.getIPAddress(true);
        } catch (SocketException e) {
            //Log.e(MainActivity.TAG, "Cannot get IP address");
            e.printStackTrace();
        }
        lastTime = System.nanoTime();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
        Core.flip(mRgbaF, mRgba, 1 );

        mDetector.process(mRgba);
        List<MatOfPoint> contours = mDetector.getContours();
        if( ev3Communicator.isConnected() ) {
            //Log.i(MainActivity.TAG, "Contours count: " + contours.size());
        }
        Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);
        Point center = mDetector.getCenterOfMaxContour();
        String result = "";
        if( center != null ) {
            Imgproc.drawMarker(mRgba, center, MARKER_COLOR);
            double xdirection = (center.x - mRgba.cols()/2)/mRgba.cols();
            double ydirection = (center.y - mRgba.rows()/2)/mRgba.rows();
            result = shortenString(xdirection) + " " + shortenString(ydirection);
        }
        int font = FONT_HERSHEY_SIMPLEX;
        if( ev3Communicator.isConnected() && System.nanoTime() - 1e8 > lastTime) {
            if( center == null ) {
                result = "NULL";
            }
            ev3Communicator.sendMessage("DIRECTION " + result);
            font = FONT_HERSHEY_DUPLEX;
            lastTime = System.nanoTime();
        }
        Imgproc.putText(mRgba,ipAddress,org,font,1,TEXT_COLOR);


        return mRgba;
    }

    private String shortenString(double string){
        return shortenString(Double.toString(string));
    }

    private String shortenString(String string){
        int length = 6;
        if (string.length() > 6){
            return string.substring(0, length);
        }
        return string;
    }

    void setEV3Communicator(EV3Communicator ev3Communicator) {
        this.ev3Communicator = ev3Communicator;
    }
}
