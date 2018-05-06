package com.example.niko.performance;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.UUID;

import static com.example.niko.performance.DispositivosBT.*;
/**
 * Created by niko on 16/10/17.
 */

public class FlyControlServices extends Service {
    private long TIME = 10000000;
    public static final String SENSOR_EXTRA = "Sensor extra";
    private SensorManager mSensorManager;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private ConnectedThread myConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address;
    SensorThread accelerometer;
    SensorThread magnetometer;
    SensorThread gyroscopeHard;
    GyroThread gyroscopeSoft;
    ControllerThread loop;
    ClientListen client;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapte
        loop = new ControllerThread();
        //client = new ClientListen();

    }

    public int onStartCommand(Intent intent, int flag, int idProcess) {
        if (myConexionBT == null) {
            address = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);

            //Setea la direccion MAC
            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            try {
                btSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
            }
            // Establece la conexión con el socket Bluetooth.
            try {
                btSocket.connect();
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                }
            }
            myConexionBT = new ConnectedThread(btSocket);
            myConexionBT.start();
            loop.start();
            Toast.makeText(getBaseContext(), device.getName(), Toast.LENGTH_LONG).show();
            //client.setHandler(loop.angle);
            //client.start();
        }

        String type = intent.getStringExtra(SENSOR_EXTRA);
        switch (type) {
            case Sensor.STRING_TYPE_ACCELEROMETER:
                accelerometer = new SensorThread(Sensor.STRING_TYPE_ACCELEROMETER, mSensorManager);
                //accelerometer.setHandler(myConexionBT.hBluetoothIn);
                accelerometer.setHandler(loop.angle);
                if (gyroscopeSoft != null)
                    accelerometer.setHandler(gyroscopeSoft.mHandler);
                accelerometer.start();
                Toast.makeText(getBaseContext(), "acelerometro", Toast.LENGTH_SHORT).show();
                //myConexionBT.write("acelerometro");
                break;
            case Sensor.STRING_TYPE_MAGNETIC_FIELD:
                magnetometer = new SensorThread(Sensor.STRING_TYPE_MAGNETIC_FIELD, mSensorManager);
                magnetometer.setHandler(myConexionBT.hBluetoothIn);
                if (gyroscopeSoft != null)
                    magnetometer.setHandler(gyroscopeSoft.mHandler);
                magnetometer.start();
                Toast.makeText(getBaseContext(), "magnetometro", Toast.LENGTH_SHORT).show();
                //myConexionBT.write("magnetometro");
                break;
            case Sensor.STRING_TYPE_GYROSCOPE:
                gyroscopeHard = new SensorThread(Sensor.STRING_TYPE_GYROSCOPE, mSensorManager);
                gyroscopeHard.setHandler(myConexionBT.hBluetoothIn);
                gyroscopeHard.start();
                Toast.makeText(getBaseContext(), "gyros hard", Toast.LENGTH_SHORT).show();
                break;
            default:
                gyroscopeSoft = new GyroThread(mSensorManager, accelerometer, magnetometer, myConexionBT.hBluetoothIn);
                gyroscopeSoft.start();
                Toast.makeText(getBaseContext(), "gyros soft", Toast.LENGTH_SHORT).show();

                //myConexionBT.write("giroscopio \n \n");

        }
        return START_STICKY;


    }

    public void onDestroy() {
        if (accelerometer != null)
            accelerometer.exit();
        if (magnetometer != null)
            magnetometer.exit();
        if (gyroscopeSoft != null)
            gyroscopeSoft.exit();
        if (gyroscopeHard != null)
            gyroscopeHard.exit();
        myConexionBT.exit();
        Toast.makeText(this, "fin de threads", Toast.LENGTH_SHORT).show();

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }
    public class ClientListen extends Thread {
        private Message message = new Message();
        private volatile Handler handler;
        private boolean error = false;
        public void setHandler(Handler handler){this.handler = handler;}
        public void run() {


            try {
                DatagramSocket clientsocket=new DatagramSocket(1212);
                byte[] receivedata = new byte[8];
                while(!error)
                {
                    DatagramPacket recv_packet = new DatagramPacket(receivedata, receivedata.length);

                    //Log.d("UDP", "S: Receiving...");
                    clientsocket.receive(recv_packet);

                    String rec_str = new String(recv_packet.getData());
                    float a = Float.parseFloat(rec_str);
                    //Log.i(" UDP ", String.valueOf(a));
                    if (clientsocket.getReceiveBufferSize() > 0){
                        message.what= 6;
                        message.obj = a;

                    }else
                        message.what = 7;
                    handler.dispatchMessage(message);
                }
            } catch (Exception e) {
                Log.i("EEEEEEEEERRRRRRRRRRROOOOOORRR", "S: Error", e);
                error = true;
                message.what = 8;
                handler.dispatchMessage(message);
            }


        }
    }
    public class ControllerThread extends Thread {
        private Looper looper;
        public Handler angle;
        private float error, lastError;
        private PidAngle pitchPid = new PidAngle(7, 2.3f, 1.0f);
        public void run() {

            Looper.prepare();
            looper = Looper.myLooper();

            angle = new Handler() {
                private long currentTime;
                private long lastTime = SystemClock.elapsedRealtimeNanos();
                private int U;
                private boolean active = true;
                private Message message = new Message();
                public float range(float value){
                    if (value > 0.3)
                        return 0.3f;
                    if (value < -0.25)
                        return -0.25f;
                    return value;
                }
                @Override
                public void handleMessage(Message msg) {
                    currentTime = SystemClock.elapsedRealtimeNanos();

                    if ((currentTime > lastTime + TIME)){ //muestreo cada 10ms

                        if (msg.what == 1 ) {
                            float[] mGravity = (float[]) msg.obj;
                            error = mGravity[1] / 10;
                            //Log.d("SENSOR", String.valueOf(error));
                        }

                        U = (int) pitchPid.getInput(0, error, 0.010f);
                        //Log.i("tag", String.valueOf(U+128));

                        //Log.i("tag", String.valueOf(U+128)+" "+String.valueOf(255-(U+128)));

                        message.what = 5;
                        message.obj = (byte) (U + 128);
                        myConexionBT.hBluetoothIn.dispatchMessage(message);
                    }

                    lastTime = currentTime;

                }


            };

        }
    }

}




