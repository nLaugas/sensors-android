package com.example.niko.performance;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import static com.example.niko.performance.DispositivosBT.*;




public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button btnAcelerometro, btnMagnetometro, btnGiroscopioSoft, btnGiroscopioHard ,btnDetener;
    private Context thisContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        btnAcelerometro   = (Button) findViewById(R.id.btnAcelerometro);
        btnMagnetometro   = (Button) findViewById(R.id.btnMagnetometro);
        btnGiroscopioSoft = (Button) findViewById(R.id.btnGiroscopioSoft);
        btnGiroscopioHard = (Button) findViewById(R.id.btnGiroscopioHard);
        btnDetener        = (Button) findViewById(R.id.btnDetener);
        btnGiroscopioSoft.setEnabled(false);

        //obtiene la Mac que fue seleccionada en la actividad "DispositivosBT"
        Intent intent = getIntent() ;
        final String address = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);


        btnAcelerometro .setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent i = new Intent(thisContext, SensorServices.class);
                i.putExtra(SensorServices.SENSOR_EXTRA, Sensor.STRING_TYPE_ACCELEROMETER);
                i.putExtra(EXTRA_DEVICE_ADDRESS, address);
                startService(i);

                btnAcelerometro.setEnabled(false);
                if (!btnMagnetometro.isEnabled())
                    btnGiroscopioSoft.setEnabled(true);
            }
        });

        btnMagnetometro .setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent i = new Intent(thisContext, SensorServices.class);
                i.putExtra(SensorServices.SENSOR_EXTRA, Sensor.STRING_TYPE_MAGNETIC_FIELD);
                i.putExtra(EXTRA_DEVICE_ADDRESS, address);
                startService(i);

                btnMagnetometro.setEnabled(false);
                if (!btnAcelerometro.isEnabled())
                    btnGiroscopioSoft.setEnabled(true);
            }
        });

        btnGiroscopioSoft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent i = new Intent(thisContext, SensorServices.class);
                i.putExtra(SensorServices.SENSOR_EXTRA, Sensor.STRING_TYPE_GAME_ROTATION_VECTOR);
                startService(i);
                btnGiroscopioSoft.setEnabled(false);
            }
        });

        btnGiroscopioHard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent i = new Intent(thisContext, SensorServices.class);
                i.putExtra(SensorServices.SENSOR_EXTRA, Sensor.STRING_TYPE_GYROSCOPE);
                i.putExtra(EXTRA_DEVICE_ADDRESS, address);
                startService(i);
                btnGiroscopioHard.setEnabled(false);
            }
        });

        btnDetener.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                stopService(new Intent(thisContext, SensorServices.class));
                btnGiroscopioHard.setEnabled(true);
                btnGiroscopioSoft.setEnabled(true);
                btnMagnetometro.setEnabled(true);
                btnAcelerometro.setEnabled(true);
            }
                      });

    }







}
