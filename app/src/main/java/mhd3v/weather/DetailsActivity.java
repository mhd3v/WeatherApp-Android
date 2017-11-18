package mhd3v.weather;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent data= getIntent();


        TextView humidity = (TextView)findViewById(R.id.humidity);
        TextView windspeed = (TextView)findViewById(R.id.windspeed);
        TextView precipatation = (TextView)findViewById(R.id.precipatation);
        TextView visibility = (TextView)findViewById(R.id.visibility);
        TextView day = (TextView)findViewById(R.id.day);
        TextView sunrise = (TextView)findViewById(R.id.sunrise);
        TextView sunset = (TextView)findViewById(R.id.sunset);

        humidity.setText("Humidity" + "\n" + data.getStringExtra("avghumidity")+ " %");

        windspeed.setText("Wind Speed" + "\n" + data.getStringExtra("maxwind_kph") + " kph");

        visibility.setText("Visibility" + "\n" + data.getStringExtra("avgvis_km")+ " km");

        precipatation.setText("Precipitation" + "\n" + data.getStringExtra("totalprecip_mm")+ " mm");

        day.setText(data.getStringExtra("day") + ", "+ data.getStringExtra("date"));

        sunrise.setText("Sunrise" + "\n" + data.getStringExtra("sunrise"));
        sunset.setText("Sunset" + "\n" + data.getStringExtra("sunset"));

    }
}
