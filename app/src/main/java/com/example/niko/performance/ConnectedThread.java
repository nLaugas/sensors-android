package com.example.niko.performance;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by niko on 4/26/18.
 */

public class ConnectedThread extends Thread {
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    public Handler hBluetoothIn;
    private Looper looper;
    private byte power ;

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
        hBluetoothIn = new Handler() {


            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 5) {
                    power = (byte) msg.obj;
                    //power[1]=(byte)(100-U);
                    try {
                        mmOutStream.write(power);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }
        };
            /*public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    mGravity = (float[]) msg.obj;
                    //write(String.valueOf(" Acelerometro : X= "+mGravity[0]+" Y= "+mGravity[1]+" Z= "+mGravity[2]+"\n"));
                    //write(String.valueOf(mGravity[1]));
                    //Log.i("tag",String.valueOf(mGravity[1]));

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
                    //float[] mGiroscopyHard = (float[]) msg.obj;
                    //write(String.valueOf(" Giroscopio Hard : X= "+mGiroscopyHard[0]+" Y= "+mGiroscopyHard[1]+" Z= "+mGiroscopyHard[2]+"\n"));
                    Log.i("GIROSCOPIO",String.valueOf(msg.obj));
                }
            }*/



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
            //Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();

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

