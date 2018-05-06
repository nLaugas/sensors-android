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
import java.util.ArrayList;


/**
 * Created by niko on 17/10/17.
 */

public class SensorThread extends Thread implements SensorEventListener, Closeable {
    private Looper looper;
    private volatile ArrayList<Handler> handlers;
    private Message message;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    public void setHandler(Handler handler) {
        handlers.add(handler);
    }

    public SensorThread(String typeSensor, SensorManager mSensorManager){//, Handler hBluetooth){
        this.mSensorManager = mSensorManager;
        if (typeSensor == Sensor.STRING_TYPE_ACCELEROMETER)
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (typeSensor == Sensor.STRING_TYPE_MAGNETIC_FIELD)
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (typeSensor == Sensor.STRING_TYPE_GYROSCOPE)
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        message = new Message();
        handlers = new ArrayList<>();
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            message.what = 1;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            message.what = 2;
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
            message.what = 4;
        message.obj = event.values;

        //envia mensaje a todos los handles registrados
        for (Handler h: handlers)
            h.dispatchMessage(message);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void run() {
       // Looper.prepare();
        //looper = Looper.myLooper();
            //regristrar el sensor listner
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        //Looper.loop();
    }

    public void exit(){
       // if(looper!=null) {
            mSensorManager.unregisterListener(this);
         //   looper.quit();
        //}
    }

    @Override
    public void close() throws IOException {
        if(looper!=null) {
            mSensorManager.unregisterListener(this);
            looper.quit();
        }
        handlers.clear();
    }
}
