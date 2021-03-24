package com.example.udpsensors;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private Sensor accelerometer;

    private TextView accel_y;
    private TextView accel_z;
    private TextView accel_x;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Switch enable_udp_switch = findViewById(R.id.enable_udp);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accel_y = findViewById(R.id.accel_y);
        accel_z = findViewById(R.id.accel_z);
        accel_x = findViewById(R.id.accel_x);

        enable_udp_switch.setOnCheckedChangeListener((enable_udp, isChecked) -> {
            EditText addressIP = findViewById(R.id.EnterIPAddress);
            EditText addressPort = findViewById(R.id.EnterPortNumber);
            if (isChecked){
                addressPort.setText(addressIP.getText());
                sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
                Log.d("SENSORS", "Started Listener");
            }else{
                addressPort.setText("");
                sensorManager.unregisterListener(MainActivity.this);
                Log.d("SENSORS", "Stopped Listener");
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

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);


            accel_x.setText(String.format("value: %.5f", accelerometerReading[0]));
            accel_y.setText(String.format("value: %.5f", accelerometerReading[1]));
            accel_z.setText(String.format("value: %.5f", accelerometerReading[2]));

        }
    }

}