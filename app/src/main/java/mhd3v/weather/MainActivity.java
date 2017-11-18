package mhd3v.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    String[] days = new String[7];
    String[] temperatures = new String[7];
    String[] condition = new String[7];
    DetailedData[] extraDetails = new DetailedData[7];

    String cityName, countryName, unit = "";

    TextView mainTemp, mainDay, mainCondition, location;

    ImageView mainImage;

    ImageButton setting,refresh;

    Boolean refreshClicked = false;

    JSONObject data = null;

    ListView lv;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        if(sp.getString("city","").equals("")){
            startActivity(new Intent(this,SettingActivity.class));
            Toast.makeText(this, "City not set", Toast.LENGTH_SHORT).show();
        }
        else if(!(sp.getString("unit", "").equals(unit) && sp.getString("city", "").equals(cityName)) || refreshClicked) //don't reload data if coming back from any other activity. Only when refresh when refresh clicked or city/unit changed
        {
            unit = sp.getString("unit", "");

            lv = (ListView) findViewById(R.id.listView);

            mainTemp = (TextView) findViewById(R.id.mainTemperature);
            mainDay = (TextView)findViewById(R.id.mainDay);
            location = (TextView)findViewById(R.id.location);
            mainCondition =(TextView)findViewById(R.id.mainCondition);

            mainImage = (ImageView)findViewById(R.id.mainImage);

            progressBar = (ProgressBar)findViewById(R.id.progressBar);

            setting = (ImageButton)findViewById(R.id.settingsButton);
            refresh = (ImageButton)findViewById(R.id.refreshButton);

            progressBar.setVisibility(View.VISIBLE);
            mainImage.setVisibility(View.GONE);
            setting.setVisibility(View.GONE);
            refresh.setVisibility(View.GONE);

            setDays();

            FetchWeather(sp.getString("city",""));

            refreshClicked = false;
        }
    }

    class customAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return days.length-1; //set array adapter next six days from today
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            view = getLayoutInflater().inflate(R.layout.customlist,null);

            TextView day= view.findViewById(R.id.day);
            TextView status= view.findViewById(R.id.status);
            TextView temp= view.findViewById(R.id.temp);

            String conditionIcon= condition[i+1]; // conditionIcon = Partly cloudy, Light drizzle, etc
            conditionIcon = conditionIcon.replaceAll("\\s","").toLowerCase(); //conditionIcon -> conditionIcon
            Log.d("cond", conditionIcon);
            ImageView weatherIcon = view.findViewById(R.id.imageView);

            setIcon(conditionIcon, weatherIcon);

            day.setText(days[i+1]);
            temp.setText(temperatures[i+1]);
            status.setText(condition[i+1]);

            return view;
        }
    }


    public void FetchWeather(final String city) {

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

                    //Toast.makeText(getApplicationContext(), "asd", Toast.LENGTH_LONG).show();

                }

                return null;
            }

            @Override
            protected void onPostExecute(Void Void) {
                if(data!=null){
                    try{

                        cityName = data.getJSONObject("location").getString("name");
                        countryName = data.getJSONObject("location").getString("country");

                        Log.d("unit", unit);

                        if(unit.equals("c")){
                            for(int i=0; i< temperatures.length; i++){
                                temperatures[i] = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day").getString("avgtemp_c")+"°C";
                            }
                        }

                        else{
                            for(int i=0; i< temperatures.length; i++){
                                temperatures[i] = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day").getString("avgtemp_f")+"°F";
                            }
                        }

                        for(int i=0; i< condition.length; i++){
                            condition[i] = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day").getJSONObject("condition").getString("text");
                        }

                        for(int i=0; i< extraDetails.length; i++){

                            String date = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getString("date");

                            String maxtemp_c = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day").getString("maxtemp_c");
                            String mintemp_c = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day").getString("mintemp_c");;

                            String maxwind_mph = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day").getString("maxwind_mph");
                            String maxwind_kph = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day").getString("maxwind_kph");

                            String totalprecip_mm = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day").getString("totalprecip_mm");
                            String totalprecip_in = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day").getString("totalprecip_in");

                            String avgvis_km = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day").getString("avgvis_km");
                            String avgvis_miles = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day").getString("avgvis_miles");

                            String avghumidity = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day").getString("avghumidity");

                            String sunrise = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("astro").getString("sunrise");
                            String sunset = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("astro").getString("sunset");

                            DetailedData detailedData = new DetailedData(date, maxtemp_c, mintemp_c, maxwind_mph, maxwind_kph, totalprecip_mm, totalprecip_in, avgvis_km, avgvis_miles, avghumidity, sunrise, sunset);

                            extraDetails[i] = detailedData;
                        }


                        setMainPanel();

                        customAdapter ca = new customAdapter();
                        lv.setAdapter(ca);

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);

                                setIntentData(intent, position);

                                startActivity(intent);

                                //Log.d("mahad", Integer.toString(position));
                                //Log.d("mahad", Long.toString(id));

                            }
                        });

                    }

                    catch(Exception e){


                    }
                }

            }
        }.execute();

    }

    void setMainPanel(){
        mainCondition.setText(condition[0]);
        mainTemp.setText(temperatures[0]);

        location.setText(cityName + ", " + countryName);

        String conditionIcon= condition[0]; // conditionIcon = Partly cloudy, Light drizzle, etc
        conditionIcon = conditionIcon.replaceAll("\\s","").toLowerCase(); //Partlycloudy -> partlycloudy
        Log.d("cond", conditionIcon);

        progressBar.setVisibility(View.GONE);
        mainImage.setVisibility(View.VISIBLE);
        mainDay.setVisibility(View.VISIBLE);
        setting.setVisibility(View.VISIBLE);
        refresh.setVisibility(View.VISIBLE);

        setIcon(conditionIcon, mainImage);

    }

    void setDays(){

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        String currentDay = (String) DateFormat.format("EEEE", today);

        days[0] = currentDay;

        for(int i = 1; i <= 6; i++){
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Date tomorrow = calendar.getTime();
            String dayOfTheWeek = (String) DateFormat.format("EEEE", tomorrow);
            days[i] = dayOfTheWeek;
        }

        mainDay.setText(currentDay);

        mainDay.setVisibility(View.GONE);

    }

    void setIcon(String condition, ImageView image){

        switch(condition){
            case "blizzard":
                image.setImageResource(R.drawable.blizzard);
                break;
            case "cloudy":
                image.setImageResource(R.drawable.cloudy);
                break;
            case "lightdrizzle":
                image.setImageResource(R.drawable.lightdrizzle);
                break;
            case "mist":
                image.setImageResource(R.drawable.blizzard);
                break;
            case "lightrain":
                image.setImageResource(R.drawable.lightdrizzle);
                break;
            case "overcast":
                image.setImageResource(R.drawable.overcast);
                break;
            case "partlycloudy":
                image.setImageResource(R.drawable.partlycloudy);
                break;
            case "patchyrainpossible":
                image.setImageResource(R.drawable.patchrain);
                break;
            case "rain":
                image.setImageResource(R.drawable.rain);
                break;
            case "moderaterain":
                image.setImageResource(R.drawable.rain);
                break;
            case "storm":
                image.setImageResource(R.drawable.storm);
                break;
            case "sunny":
                image.setImageResource(R.drawable.sunny);
                break;
            case "heavysnow":
                image.setImageResource(R.drawable.snow);
                break;
            case "lightsnow":
                image.setImageResource(R.drawable.snow);
                break;
            default:
                image.setImageResource(R.drawable.stock);

        }
    }

    void setIntentData(Intent intent, int i){

        intent.putExtra("day",days[i+1]);

        intent.putExtra("date",extraDetails[i+1].date);

        intent.putExtra("mintemp_c",extraDetails[i+1].mintemp_c);
        intent.putExtra("maxtemp_c",extraDetails[i+1].maxtemp_c);

        intent.putExtra("maxwind_mph",extraDetails[i+1].maxwind_mph);
        intent.putExtra("maxwind_kph",extraDetails[i+1].maxwind_kph);

        intent.putExtra("totalprecip_mm",extraDetails[i+1].totalprecip_mm);
        intent.putExtra("totalprecip_in",extraDetails[i+1].totalprecip_in);

        intent.putExtra("avgvis_km",extraDetails[i+1].avgvis_km);
        intent.putExtra("avgvis_miles",extraDetails[i+1].avgvis_miles);

        intent.putExtra("avghumidity",extraDetails[i+1].avghumidity);

        intent.putExtra("sunrise",extraDetails[i+1].sunrise);
        intent.putExtra("sunset",extraDetails[i+1].sunset);
    }

    public void launchDetailsActivity(View view) {

        Intent intent = new Intent(this, DetailsActivity.class);

        setIntentData(intent, -1);

        startActivity(intent);
    }


    public void locationClicked(View view) {
        startActivity(new Intent(this, SettingActivity.class));
    }

    public void refreshClicked(View view) {
        refreshClicked = true;
        onStart();
        Toast.makeText(this, "Data refreshed!", Toast.LENGTH_SHORT).show();
    }

    public void settingsClicked(View view){

        startActivity(new Intent(this, SettingActivity.class));

    }


}

