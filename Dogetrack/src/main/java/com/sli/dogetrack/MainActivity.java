package com.sli.dogetrack;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    public static final String PREFS_NAME = "DogeTrackPrefs";
    private final String ApiUrl = "http://dogecoin.jebem.eu/api/index.php/rates?amount=";
    private EditText etAmount;
    public TextView tvValue, tvSatoshis;
    private Button bUpdate;
    private ProgressBar pbWorking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etAmount = (EditText) findViewById(R.id.etAmount);
        tvValue = (TextView) findViewById(R.id.tvValue);
        tvSatoshis = (TextView) findViewById(R.id.tvSatoshis);
        pbWorking = (ProgressBar) findViewById(R.id.pbWorking);

        // Restore previously saved doge amount
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String amount = settings.getString("DogeAmount", "0");
        etAmount.setText(amount);

        updateValue();

        bUpdate = (Button) findViewById(R.id.bUpdate);
        bUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateValue();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Save current doge value
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("DogeAmount", etAmount.getText().toString());
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateValue();
    }

    private void updateValue() {
        try {
            new LongOperation().execute(ApiUrl + etAmount.getText().toString());
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private class LongOperation extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                String JsonResponse = Grabber.connect(params[0]);
                JSONObject json = new JSONObject(JsonResponse);
                return json;
            } catch (Exception ex) {
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            tvValue.setVisibility(View.INVISIBLE);
            tvSatoshis.setVisibility(View.INVISIBLE);
            pbWorking.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                JSONObject data = result.getJSONObject("data");
                JSONObject btc = data.getJSONObject("btc");
                JSONObject usd = data.getJSONObject("usd");

                float value = Float.parseFloat(usd.get("price").toString());
                float satoshis = Float.parseFloat(btc.get("rate").toString()) * 100000000;

                String display = "$" + String.format("%.02f", value);
                String sat_display = String.format("%.00f", satoshis);

                tvValue.setText(display);
                tvSatoshis.setText(sat_display + " " + getString(R.string.satoshis));

                pbWorking.setVisibility(View.INVISIBLE);
                tvValue.setVisibility(View.VISIBLE);
                tvSatoshis.setVisibility(View.VISIBLE);
            } catch (JSONException ex) {
            }
        }

    }

}
