package mypc.bluetoothsender;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView textview,tvdevice;
    private Button Send;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice device;
    public String acc_data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("数据发送端");
        textview = (TextView) findViewById(R.id.textView);
        tvdevice = (TextView) findViewById(R.id.device);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> paired = mBluetoothAdapter.getBondedDevices();
        if(paired.size()>0){
            for(BluetoothDevice device1:paired){
                tvdevice.append("已配对设备:" + device1.getName()+":"+device1.getAddress()+"\n");
                device = mBluetoothAdapter.getRemoteDevice(device1.getAddress().toString());
//                if(device1.getName().equals("Xiaomi cancro")||device1.getName().equals("红米手机")){
//                    device = mBluetoothAdapter.getRemoteDevice(device1.getAddress());
//                    remote.append("当前远程设备地址:"+device1.getAddress()+"\n");
//                }
            }
        }

        //device = mBluetoothAdapter.getRemoteDevice("00:EC:0A:09:74:07");
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
    }
    public void beginSend(View v){
        new ConnectThread(device).start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                String acceleromter = "加速度传感器数据：\n"+"X: "+event.values[0]+"\n"+"Y: "+event.values[1]+"\n"+"Z: "+event.values[2];
//                BigDecimal a = new BigDecimal(event.values[0]);
//                BigDecimal b = new BigDecimal(event.values[1]);
//                BigDecimal c = new BigDecimal(event.values[2]);
                //acc_data = "X: "+  a.setScale(7,BigDecimal.ROUND_HALF_UP).toString() +"\n"+"Y: "+ b.setScale(7,BigDecimal.ROUND_HALF_UP).toString() +"\n"+"Z: "+ c.setScale(7,BigDecimal.ROUND_HALF_UP).toString() +"\n";
                acc_data = acceleromter;
                textview.setText(acceleromter);
                break;
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private class ConnectThread extends Thread {
        public InputStream inputStream ;
        public OutputStream outputStream ;
        private BluetoothSocket mmSocket;
        public ConnectThread(BluetoothDevice device) {
            try {
                mmSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("d7afed6b-43c9-4a36-b63b-d2966aa23a91"));
            } catch (IOException e) { }
        }
        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                inputStream = mmSocket.getInputStream();
                outputStream = mmSocket.getOutputStream();
                while (true){
                    outputStream.write(acc_data.getBytes("utf-8"));
                    int mount = acc_data.length();
                    Log.d("Demo", String.valueOf(mount));
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }
        }
    }
}