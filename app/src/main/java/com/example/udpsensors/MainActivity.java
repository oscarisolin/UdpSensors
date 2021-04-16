package com.example.udpsensors;



import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;


import org.json.JSONException;
import org.json.JSONObject;

import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    public class Sender implements Runnable {

        private String ip;
        private int port;
        private byte[] buf;
        private DatagramSocket udpSocket;


        public Sender() {
            ip = "127.0.0.1";
            port = 7000;
            buf = ("something to send").getBytes();
            try {
                udpSocket = new DatagramSocket();
            }catch (SocketException e) {
                Log.e("Udp:", "Socket Error:", e);
            }

        }

        public void update(String myip, int myport, String mymessage){
            ip = myip;
            port = myport;
            buf = mymessage.getBytes();
        }

        @Override
        public void run() {
            try {

                InetAddress serverAddr = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(buf, buf.length,serverAddr, port);
                udpSocket.send(packet);
            } catch (IOException e) {
                Log.e("Udp Send:", "IO Error:", e);
            }
        }
    }

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] gyroReading = new float[3];
    private Sensor accelerometer;
    private Sensor gyro;

    private TextView accel_y;
    private TextView accel_z;
    private TextView accel_x;

    private TextView gyro_x;
    private TextView gyro_y;
    private TextView gyro_z;

    private SurfaceView mSurfaceView;
    private InetAddress udpAddress;
    private Intent cameraRtsp;
    private int udpPort;
    private Sender saender;
    public static byte[] floatToByteArray(float value) {
        int intBits =  Float.floatToIntBits(value);
        return new byte[] {
                (byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
    }

    JSONObject jsobj = new JSONObject();
    private String jsonMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        saender = new Sender();
        mSurfaceView = findViewById(R.id.surfaceView);

        Switch enable_udp_switch = findViewById(R.id.enable_udp);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        accel_y = findViewById(R.id.accel_y);
        accel_z = findViewById(R.id.accel_z);
        accel_x = findViewById(R.id.accel_x);

        gyro_x = findViewById((R.id.gyro_x));
        gyro_y = findViewById((R.id.gyro_y));
        gyro_z = findViewById((R.id.gyro_z));

        SessionBuilder.getInstance()
                .setSurfaceView(mSurfaceView)
//                .setPreviewOrientation(90)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setVideoEncoder(SessionBuilder.VIDEO_H264);

        cameraRtsp = new Intent(this, RtspServer.class);

        enable_udp_switch.setOnCheckedChangeListener((enable_udp, isChecked) -> {
            EditText addressIP = findViewById(R.id.EnterIPAddress);
            EditText addressPort = findViewById(R.id.EnterPortNumber);
            EditText streamingPort = findViewById(R.id.EnterStreamingPort);

            if (isChecked){
                sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(MainActivity.this,gyro,SensorManager.SENSOR_DELAY_NORMAL);
                Log.d("SENSORS", "Started Listener");

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putString(RtspServer.KEY_PORT, String.valueOf(streamingPort.getText()));
                editor.apply();

                Log.d("RTSP CAMERA", String.format("Started RTSP Server on Port %s",streamingPort.getText()));
                MainActivity.this.startService(cameraRtsp);


                try {
                    udpAddress = InetAddress.getByName(String.valueOf(addressIP.getText()));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

                udpPort = Integer.parseInt(String.valueOf(addressPort.getText()));


            }else{
                sensorManager.unregisterListener(MainActivity.this);
                sensorManager.unregisterListener(MainActivity.this);
                Log.d("SENSORS", "Stopped Listener");
                MainActivity.this.stopService(cameraRtsp);
                Log.d("RTSP CAMERA", "Stopped RTSP Server");
            }
        });
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);

            accel_x.setText(String.valueOf(accelerometerReading[0]));
            accel_y.setText(String.valueOf(accelerometerReading[1]));
            accel_z.setText(String.valueOf(accelerometerReading[2]));

            JSONObject jsobj = new JSONObject();
            String jsonMsg;
            saender = new Sender();

            try {
                jsobj.put("type","acceleration");
                jsobj.put("timestamp",event.timestamp);
                jsobj.put("ax",accelerometerReading[0]);
                jsobj.put("ay",accelerometerReading[1]);
                jsobj.put("az",accelerometerReading[2]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jsonMsg = jsobj.toString();
            saender.update(udpAddress.getHostAddress(), udpPort, jsonMsg);
            Thread mythread = new Thread(saender);
            mythread.start();

        }

        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            System.arraycopy(event.values, 0, gyroReading,
                    0, gyroReading.length);

            gyro_x.setText(String.valueOf(gyroReading[0]));
            gyro_y.setText(String.valueOf(gyroReading[1]));
            gyro_z.setText(String.valueOf(gyroReading[2]));

            JSONObject jsobj = new JSONObject();
            String jsonMsg;
            saender = new Sender();


            try {
                jsobj.put("type","gyro");
                jsobj.put("timestamp",event.timestamp);
                jsobj.put("rotx",gyroReading[0]);
                jsobj.put("roty",gyroReading[1]);
                jsobj.put("rotz",gyroReading[2]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jsonMsg = jsobj.toString();
            saender.update(udpAddress.getHostAddress(), udpPort, jsonMsg);
            Thread mythread = new Thread(saender);
            mythread.start();

        }
    }

}