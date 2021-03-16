package com.example.udpsensors;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextClock;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Switch enable_udp_switch = findViewById(R.id.enable_udp);
        enable_udp_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EditText addressIP = findViewById(R.id.EnterIPAddress);
                EditText addressPort = findViewById(R.id.EnterPortNumber);
                if (isChecked){
                    addressPort.setText(addressIP.getText());
                }else{
                    addressPort.setText("");
                }
            }
        });
    }

    public void startSending(View view) {

        EditText addressIP = findViewById(R.id.EnterIPAddress);
        EditText addressPort = findViewById(R.id.EnterPortNumber);

        addressPort.setText(addressIP.getText());
    }
}