package mhd3v.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingActivity extends AppCompatActivity {
    EditText cityET;
    JSONObject data= null;
    boolean cityValid = false;
    String unit="";
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        cityET = (EditText)findViewById(R.id.city);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        if(!(sp.getString("city","").equals(""))){
            cityET.setText(sp.getString("city",""));

            Button save = (Button)findViewById(R.id.button);

            save.setText("Update");

        }

        RadioGroup rg = (RadioGroup)findViewById(R.id.radioGroup);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if(i == R.id.cButton)
                    unit = "c";

                if(i == R.id.fButton)
                    unit = "f";

            }
        });

    }


    public void checkCity(final String city) {

        new AsyncTask<Void, Void, Void>() {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    URL url = new URL("http://api.apixu.com/v1/forecast.json?key={APIXU_KEY}&q="+city+"&days=7");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    StringBuffer json = new StringBuffer(1024);
                    String tmp = "";

                    while((tmp = reader.readLine()) != null)
                        json.append(tmp).append("\n");

                    reader.close();

                    data = new JSONObject(json.toString());


                } catch (Exception e) {
                    System.out.println("Exception "+ e.getMessage());
                    return null;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void Void) {
                if(data!=null){
                    try{
                        String fetchedCity = data.getJSONObject("location").getString("name"); //store city name fetched from server (in case of entering China as city, city returned is Beijing)
                        editor.putString("city", fetchedCity);
                        editor.putString("unit", unit);

                        if(editor.commit()){
                            Toast.makeText(SettingActivity.this, "City set! City: "+ fetchedCity + ", Unit: "+ unit, Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }

                    catch(Exception e){

                    }
                }
                else{
                    Toast.makeText(SettingActivity.this, "City not found, enter again!", Toast.LENGTH_SHORT).show();
                }

            }
        }.execute();

    }

    public void onSaveClicked(View view) {

        if(cityET.getText().toString().equals("") && unit.equals("")) {
            Toast.makeText(this, "Please city name and select unit!", Toast.LENGTH_LONG).show();
        }
        else if(cityET.getText().toString().equals("")) {
            Toast.makeText(this, "Please enter city name!", Toast.LENGTH_LONG).show();
        }
        else if(unit.equals("")) {
            Toast.makeText(this, "Please select a unit!", Toast.LENGTH_LONG).show();
        }

        else {
            String city = cityET.getText().toString();

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

            editor= sp.edit();

            checkCity(city);
        }

    }
}
