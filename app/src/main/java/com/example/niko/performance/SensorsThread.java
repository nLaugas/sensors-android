package com.example.niko.performance;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import java.io.Closeable;
import java.io.IOException;


/**
 * Created by niko on 17/10/17.
 */

public class SensorsThread extends Thread implements SensorEventListener, Closeable {
    private Looper looper;
    private volatile Handler hSensor;
    private volatile Handler hBluetooth;
    private Message message = new Message();
    private SensorManager mSensorManager;
    private Sensor mSensor;

    public void sethSensor(Handler hSensor) {
        this.hSensor = hSensor;
    }

    public SensorsThread(String typeSensor, SensorManager mSensorManager, Handler hBluetooth){
        this.mSensorManager = mSensorManager;
        this.hBluetooth = hBluetooth;
        if (typeSensor == Sensor.STRING_TYPE_ACCELEROMETER)
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (typeSensor == Sensor.STRING_TYPE_MAGNETIC_FIELD)
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (typeSensor == Sensor.STRING_TYPE_GYROSCOPE)
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            message.what = 1;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            message.what = 2;
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
            message.what = 4;
        if (hSensor!=null) {
            hSensor.dispatchMessage(message);
        }
        message.obj = event.values;
        hBluetooth.dispatchMessage(message);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void run() {
        Looper.prepare();
        looper = Looper.myLooper();
        //regristrar el sensor listner
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Looper.loop();
    }

    public void exit(){
        if(looper!=null) {
            mSensorManager.unregisterListener(this);
            looper.quit();
        }
    }

    @Override
    public void close() throws IOException {
        if(looper!=null) {
            mSensorManager.unregisterListener(this);
            looper.quit();
        }
    }
}
