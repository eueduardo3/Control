package com.example.eduardo.control;

import android.media.RingtoneManager;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private SectionsPageAdapter mSectionsPageAdapter;
    private ViewPager mViewpager;

    public boolean alarmeAtivado;

        // Declarações e inicializações para o MQTT
        private String clientId = MqttClient.generateClientId();
        private MqttAndroidClient client;
        boolean assinou = false;

        private MqttCallback ClientCallBack = new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "Perda de conexão... Reconectando...");
                connectMQTT();
                assinou = false;
            }





            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String msg = new String(message.getPayload());
                Log.d(TAG, topic + ": " + msg);
                if (topic.equals("eueduardoCorrente")) { // Apresentada graficamente
                    int correnteComoInteiro = Integer.parseInt(msg);
                }
            }




            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "Entregue!");

            }
        };

        private IMqttActionListener MqttCallBackApp = new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d(TAG, "onSuccess");
                if (!assinou) {
                    subscribeMQTT();
                    assinou = true;
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken,
                                  Throwable exception) {
                // The subscription could not be performed, maybe the user was not
                // authorized to subscribe on the specified topic e.g. using wildcards
                Log.d(TAG, "onFailure");

            }
        };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        initMQTT();
        connectMQTT();


        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate iniciando");
        mSectionsPageAdapter = new

                SectionsPageAdapter(getSupportFragmentManager());

        mViewpager = (ViewPager)

                findViewById(R.id.container);

        setupViewPager(mViewpager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewpager);
    }


    private void initMQTT() {
        client = new MqttAndroidClient(this.getApplicationContext(),
                "tcp://iot.eclipse.org:1883", clientId);
        client.setCallback(ClientCallBack);
    }

    // Inicialização do MQTT e conexão inicial
    private void connectMQTT() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("eueduardo");
            options.setPassword("123".toCharArray());
            IMqttToken token = client.connect(options);
            token.setActionCallback(MqttCallBackApp);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // Assina as mensagens MQTT desejadas
    public void subscribeMQTT() {
        int qos = 1;
        try {
            if (!client.isConnected()) {
                connectMQTT();
            }
            IMqttToken subTokenU = client.subscribe("eueduardoCorrente", qos);
            subTokenU.setActionCallback(MqttCallBackApp);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // Trata o clique do botão, publicando as mensagens
    public void botaocima(View v) { // Evento disparado no Clique do botão
        String topic = "eueduardoVentilador";
        String payload = "VL";
        byte[] encodedPayload = new byte[0];
        try {
            if (!client.isConnected()) {
                connectMQTT();
            }
            // client.publish("correnteMaxima",new MqttMessage(CMaxima.getText().toString().getBytes("UTF-8")));
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);

            Toast.makeText(MainActivity.this, "publicou por mqtt", Toast.LENGTH_LONG).show();

        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }

    }


    private void setupViewPager(ViewPager viewPager){
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new Tab1Fragment(), "MOTORES");
        adapter.addFragment(new Tab2Fragment(), "ALARME");
        viewPager.setAdapter(adapter);
    }


}


