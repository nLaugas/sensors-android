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
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.example.niko.performance.DispositivosBT.*;
/**
 * Created by niko on 16/10/17.
 */

public class SensorServices extends Service{

    public static final String SENSOR_EXTRA = "Sensor extra";
    private SensorManager mSensorManager;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private ConnectedThread myConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address;
    SensorsThread accelerometer;
    SensorsThread magnetometer;
    SensorsThread gyroscopeHard;
    GyroThread gyroscopeSoft;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapte

    }

    public int onStartCommand(Intent intent, int flag, int idProcess) {
        if (myConexionBT == null) {
            address = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);

            //Setea la direccion MAC
            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            try
            {
                btSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
            }
            // Establece la conexión con el socket Bluetooth.
            try
            {
                btSocket.connect();
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {}
            }
            myConexionBT = new ConnectedThread(btSocket);
            myConexionBT.start();
            Toast.makeText(getBaseContext(), device.getName(), Toast.LENGTH_LONG).show();

        }

        String type = intent.getStringExtra(SENSOR_EXTRA);
        switch (type) {
            case Sensor.STRING_TYPE_ACCELEROMETER:
                    accelerometer = new SensorsThread(Sensor.STRING_TYPE_ACCELEROMETER, mSensorManager,myConexionBT.bluetoothIn);
                    if(gyroscopeSoft!=null)
                            accelerometer.sethSensor(gyroscopeSoft.mHandler);
                    accelerometer.start();
                    Toast.makeText(getBaseContext(), "acelerometro", Toast.LENGTH_SHORT).show();
                        //myConexionBT.write("acelerometro");
                break;
            case Sensor.STRING_TYPE_MAGNETIC_FIELD:
                    magnetometer = new SensorsThread(Sensor.STRING_TYPE_MAGNETIC_FIELD, mSensorManager,myConexionBT.bluetoothIn);
                    if(gyroscopeSoft!=null)
                        magnetometer.sethSensor(gyroscopeSoft.mHandler);
                    magnetometer.start();
                    Toast.makeText(getBaseContext(), "magnetometro", Toast.LENGTH_SHORT).show();
                    //myConexionBT.write("magnetometro");
                break;
            case Sensor.STRING_TYPE_GYROSCOPE:
                    gyroscopeHard = new SensorsThread(Sensor.STRING_TYPE_GYROSCOPE,mSensorManager,myConexionBT.bluetoothIn);
                    gyroscopeHard.start();
                    Toast.makeText(getBaseContext(), "gyros hard", Toast.LENGTH_SHORT).show();
                break;
            default:
                    gyroscopeSoft = new GyroThread(mSensorManager, accelerometer, magnetometer,myConexionBT.bluetoothIn);
                    gyroscopeSoft.start();
                    Toast.makeText(getBaseContext(), "gyros soft", Toast.LENGTH_SHORT).show();

                //myConexionBT.write("giroscopio \n \n");

        }
        return START_STICKY;


    }

    public void onDestroy() {
        if ( accelerometer!= null)
            accelerometer.exit();
        if ( magnetometer != null)
            magnetometer.exit();
        if (gyroscopeSoft != null)
            gyroscopeSoft.exit();
        if (gyroscopeHard != null)
            gyroscopeHard.exit();
        myConexionBT.exit();
        Toast.makeText(this,"fin de threads", Toast.LENGTH_SHORT).show();

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    /*------------------------------ CLASE  CONNECTEDTHREAD----------------------------------*/
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public Handler bluetoothIn;
        private Looper looper;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }



        public void run() {
            Looper.prepare();
            looper = Looper.myLooper();
            bluetoothIn = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        float[] mGravity = (float[]) msg.obj;
                        write(String.valueOf(" Acelerometro : X= "+mGravity[0]+" Y= "+mGravity[1]+" Z= "+mGravity[2]+"\n"));
                    }else
                    if(msg.what == 2){
                        float[] mGeomagnetic = (float[]) msg.obj;
                        write(String.valueOf(" Magnetometro : X= "+mGeomagnetic[0]+" Y= "+mGeomagnetic[1]+" Z= "+mGeomagnetic[2]+"\n"));
                    }else
                    if(msg.what == 3){
                        float[] mGiroscopy = (float[]) msg.obj;
                        write(String.valueOf(" Giroscopio Soft : X= "+mGiroscopy[0]+" Y= "+mGiroscopy[1]+" Z= "+mGiroscopy[2]+"\n"));
                    }else
                    if(msg.what == 4){
                        float[] mGiroscopyHard = (float[]) msg.obj;
                        write(String.valueOf(" Giroscopio Hard : X= "+mGiroscopyHard[0]+" Y= "+mGiroscopyHard[1]+" Z= "+mGiroscopyHard[2]+"\n"));
                    }
                }
            };

            Looper.loop();
           /* byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }*/
        }

        //Envio de trama
        public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            } catch (IOException e) {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();

            }
        }
        public void exit()  {
            if(looper!=null) {
                looper.quit();
                try {
                    mmOutStream.close();
                } catch (IOException e) {}

            }
        }
    }
}




