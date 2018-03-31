package com.example.niko.performance;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by niko on 17/10/17.
 */

public class GyroThread extends Thread {

    private volatile Handler hBluetooth;
    public Handler mHandler;
    private Looper looper;
    private Message message = new Message();
    private float[] mGravity ;
    private float[] mGeomagnetic ;
    private float RotationMatrix[];
    private float[] iMat ;
    private float orientation[];
    private SensorManager mSensorManager;
    private SensorsThread accelerometer;
    private SensorsThread magnet;

    public GyroThread(SensorManager mSensorManager, SensorsThread accelerometer, SensorsThread magnetometer, Handler hBluetooth){
        mGravity = new float[3];
        mGeomagnetic = new float[3];
        RotationMatrix = new float[9];
        iMat = new float[9];
        orientation = new float[3];
        this.mSensorManager = mSensorManager;
        this.accelerometer = accelerometer;
        this.magnet= magnetometer;
        this.hBluetooth = hBluetooth;

    }
    @Override
    public void run() {

        Looper.prepare();
        looper = Looper.myLooper();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1)
                     mGravity = (float[]) msg.obj;
                else if(msg.what == 2)
                     mGeomagnetic = (float[]) msg.obj;


                if (mGravity != null && mGeomagnetic != null) {
                    boolean success = mSensorManager.getRotationMatrix(RotationMatrix, iMat, mGravity, mGeomagnetic);

                    if (success) {
                        SensorManager.getOrientation(RotationMatrix, orientation);
                        message.obj = orientation;
                        message.what = 3;
                        hBluetooth.dispatchMessage(message);
                    }
                }
            }
        };
       if(this.accelerometer!=null) {
            this.accelerometer.sethSensor(this.mHandler);
            this.accelerometer=null;
        }
        if (this.magnet != null) {
            this.magnet.sethSensor(this.mHandler);
            this.magnet=null;
        }
        Looper.loop();
    }

    public void exit(){
        if(looper!=null) {
            //Desregirtrar
            looper.quit();
        }
    }
}
