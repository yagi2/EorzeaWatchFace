package com.yagi2.eorzeawatchface;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity {

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        final SharedPreferences preferences = getSharedPreferences("background", MODE_PRIVATE);
        String tags = preferences.getString("tag", "");

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.frame_radio_buttons);
        RadioButton radioButton;

        Log.d("pref", tags);

        switch(tags) {
            case "ishgald" :
                radioButton = (RadioButton) findViewById(R.id.frame_ishgald);
                break;
            case "gridania" :
                radioButton = (RadioButton) findViewById(R.id.frame_gridania);
                break;
            case "limsa" :
                radioButton = (RadioButton) findViewById(R.id.frame_limsa);
                break;
            case "uldah" :
                radioButton = (RadioButton) findViewById(R.id.frame_uldah);
                break;
            case "none" :
                radioButton = (RadioButton) findViewById(R.id.frame_no);
                break;
            default :
                radioButton = (RadioButton) findViewById(R.id.frame_ishgald);
                break;
        }

        radioButton.setChecked(true);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton = (RadioButton) findViewById(i);

                preferences.edit().putString("tag", radioButton.getTag().toString()).apply();

                syncConfiguration(radioButton.getTag().toString());
            }
        });


    }

    private void syncConfiguration(String tag) {
        if(mGoogleApiClient == null) {
            return;
        }



        PutDataMapRequest dataMap = PutDataMapRequest.create("/config");
        dataMap.getDataMap().putString("TAG", tag);

        Wearable.DataApi.putDataItem(mGoogleApiClient, dataMap.asPutDataRequest());
    }
}
