package com.example.ducks.maracas;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    Boolean isPlayed, last = false;
    MediaPlayer mediaPlayer;
    ImageView maracasImageView;
    Animation maracasAnimation;

    private float[] rotationMatrix; //матрица поворота

    private float[] accelerometer;  //данные с акселерометра
    private float[] geomagnetism;   //данные геомагнитного датчика
    private SensorManager sensorManager; //менеджер сенсоров
    Handler handler;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        rotationMatrix = new float[16];
        accelerometer = new float[3];
        geomagnetism = new float[3];
        setContentView(R.layout.activity_main);

        maracasImageView = findViewById(R.id.im);
        maracasAnimation = AnimationUtils.loadAnimation(this, R.anim.maracas);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    maracasImageView.startAnimation(maracasAnimation);
                }
                else {
                    maracasImageView.clearAnimation();
                }
            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isPlayed != null && !isPlayed) {
            mediaPlayer.start();
            isPlayed = true;
        }
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI );
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isPlayed != null && isPlayed
        ) {
            mediaPlayer.pause();
            isPlayed = false;
        }
        sensorManager.unregisterListener(this);
    }

    private void loadSensorData(SensorEvent event) {
        final int type = event.sensor.getType(); //определяем тип датчика
        if (type == Sensor.TYPE_ACCELEROMETER) { //если акселерометр
            accelerometer = event.values.clone();
        }

        if (type == Sensor.TYPE_MAGNETIC_FIELD) { //если геомагнитный датчик
            geomagnetism = event.values.clone();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        loadSensorData(event); // получаем данные с датчика
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometer, geomagnetism); //получаем матрицу поворота
        if ((Math.round(Math.toDegrees(accelerometer[0]))) > 400 && isPlayed != null) {
            last = true;
        } else if ((Math.round(Math.toDegrees(accelerometer[0]))) < 200 && last) {
            NewThread newThread = new NewThread();
            newThread.execute();
            last = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class NewThread extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
                handler.sendEmptyMessage(1);
                int rand = (int) (1 + Math.random() * 2);
                if (rand == 1) {
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.tsss);
                } else if (rand == 2) {
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.tsss2);
                }
                mediaPlayer.start();
                mediaPlayer.setLooping(true);
                isPlayed = true;
                try {
                    Thread.sleep(2599);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isPlayed)
                    mediaPlayer.stop();
                handler.sendEmptyMessage(2);
                isPlayed = false;
            return null;
        }
    }
}

