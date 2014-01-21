package com.sli.dogetrack;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    public static final String PREFS_NAME = "DogeTrackPrefs";
    private final String ApiUrl = "http://dogecoin.jebem.eu/api/index.php/rates?amount=";
    private EditText etAmount;
    private TextView tvValue, tvSatoshis;
    private Spinner spCurrency;
    private Button bUpdate;
    private ProgressBar pbWorking;

    private static String[] CurrencySymbols = {"$", "£", "€"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etAmount = (EditText) findViewById(R.id.etAmount);
        tvValue = (TextView) findViewById(R.id.tvValue);
        tvSatoshis = (TextView) findViewById(R.id.tvSatoshis);
        spCurrency = (Spinner) findViewById(R.id.spCurrency);
        pbWorking = (ProgressBar) findViewById(R.id.pbWorking);

        // Setup currency spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_dropdown_item);
        spCurrency.setAdapter(adapter);

        // Restore previously saved doge amount
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String amount = settings.getString("DogeAmount", "0");
        int currency = settings.getInt("Currency", 0);
        etAmount.setText(amount);
        spCurrency.setSelection(currency);

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
        savePrefs();
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePrefs();
    }

    private void updateValue() {
        try {
            new LongOperation().execute(ApiUrl + etAmount.getText().toString());
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void savePrefs() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("DogeAmount", etAmount.getText().toString());
        editor.putInt("Currency", spCurrency.getSelectedItemPosition());
        editor.commit();
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
                JSONObject currency = data.getJSONObject(spCurrency.getSelectedItem().toString().toLowerCase());

                float value = Float.parseFloat(currency.get("price").toString());
                float satoshis = Float.parseFloat(btc.get("rate").toString()) * 100000000;

                String display = CurrencySymbols[spCurrency.getSelectedItemPosition()] + String.format("%.02f", value);

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
