package melissamorris.grcc.com.getlocalweatherandmap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import android.app.ProgressDialog;
import java.io.InputStream;

public class WeatherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        new ExecuteInBackground().execute();
    }

    //extends AsyncTask class allows to do tasks in the background
    private class ExecuteInBackground extends AsyncTask<Void, Void, Void> {
        ProgressDialog mProgressDialog;
        Weather weather = null;

        @Override
        protected void onPreExecute() {
        super.onPreExecute();
        //This onPreExecute() method will only start up the spinner (mProgressDialog)
        // next 5 lines show dialog box with spinning thingie
        mProgressDialog = new ProgressDialog(WeatherActivity.this);
        mProgressDialog.setTitle("Getting Your Weather!");
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.show();
    }
        @Override
        protected Void doInBackground(Void... params) {
        //You can do stuff here that might take some time.
        //The next method onPostExecute() will happen after doInBackground has completed all tasks
        // create an instance of class that gets info from Internet
        Double latitude = getIntent().getExtras().getDouble("passLatitude");
        Double longitude = getIntent().getExtras().getDouble("passLongitude");
        weather = new Weather(latitude, longitude);
        return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        //ok, so we got the HTML stuff, lets take data and do something with it
        TextView textViewLocation = (TextView) findViewById(R.id.textViewLocation);
        textViewLocation.setText(weather.strLocation);
        TextView textViewTemperature = (TextView) findViewById(R.id.textViewTemperature);
        textViewTemperature.setText(weather.strTemperature);
        //TextView textViewWeather = (TextView) findViewById(R.id.textViewWeather);
        //textViewWeather.setText(weather.strWeather);
        TextView textViewHighTemperature = (TextView) findViewById(R.id.textViewHighTemperature);
        textViewHighTemperature.setText(weather.strHighTemps.get(0));
        TextView textViewLowTemperature = (TextView) findViewById(R.id.textViewLowTemperature);
        textViewLowTemperature.setText(weather.strLowTemps.get(0));
        TextView textViewPredictionToday = (TextView) findViewById(R.id.textViewPredictionToday);
        textViewPredictionToday.setText(weather.strShortForecasts.get(0));
        TextView textViewPredictionTonight = (TextView) findViewById(R.id.textViewPredictionTonight);
        textViewPredictionTonight.setText(weather.strShortForecasts.get(1));
        ImageView imageViewWeather = (ImageView) findViewById(R.id.textViewWeather);
        imageViewWeather.setImageBitmap(weather.bitmapWeather);
        //This turns off the loading spinning thingie
        mProgressDialog.dismiss();
        }
}


    //Date: 01/23/2019
    //Written by: Thomas DeJong Adjunct Professor Grand Rapids Community College
    //Purpose: To get current and forecasted weather data at certain latitude and longitude for android app
    // Usage: Constructor takes latitude and longitude as double.
    // Several public vars:
    // strForecastNow: current weather (e.g. Light Rain Fog/Mist)
    // strTemperatureNow: current temperature (e.g. 37�F)
    // List<String> strHighTemps: predicted high temperature for the next 5 days (e.g. 8�F)
    // List<String> strLowTemps: predicted low temperature for the next 5 nights (e.g. 1�F)
    // List<String> strForecasts: predicted forcasts for the next 9 days (e.g. Thursday: Snow showers, mainly after 2pm. High near 26. West southwest wind...)
    // List<String> strShortForecasts: shorter version of strForecasts (e.g. Chance Snow Showers then Snow Showers)
    // example of usage: Weather weather = new Weather(-85.0, 43.07); Somewhere in Grand Rapids MI.
    //System.out.println(weather.strWeather);
    // or textView.setText(weather.strWeather);
    // or textView.setText(weather.strForecasts.get(1))

    public class Weather {
        //Public vars
        public String strWeather, strTemperature, strLocation;
        public List<String> strHighTemps, strLowTemps, strForecasts, strShortForecasts;
        public Bitmap bitmapWeather;

        Weather(double latitude, double longitude) {
        //actual url: https://forecast.weather.gov/MapClick.php?lon=-85.7&lat=42.98#.XEiwElxKiUm
            String url = "https://forecast.weather.gov/MapClick.php?lon=" + longitude + "&lat=" + latitude + "#.XEhwWFxKiUl/";
            try {
            //create a document that holds all the stuff in a webpage
                Document doc = Jsoup.connect(url).get();
                strLocation = doc.getElementsByClass("panel-title").get(2).text().toString();
                strWeather = doc.getElementsByClass("myforecast-current").get(0).text().toString();
                strTemperature = doc.getElementsByClass("myforecast-current-lrg").get(0).text().toString();

                strHighTemps = new ArrayList<>(); // 5 possible
                for (int x = 0; x < doc.getElementsByClass("temp temp-high").size(); x++) {
                    strHighTemps.add(doc.getElementsByClass("temp temp-high").get(x).text().toString().substring(5)
                            .trim());
                }

                strLowTemps = new ArrayList<>(); // 5 possible
                for (int x = 0; x < doc.getElementsByClass("temp temp-low").size(); x++) {
                    strLowTemps.add(doc.getElementsByClass("temp temp-low").get(x).text().substring(4).toString().trim().replaceAll(" �F", "�F"));
                }
                strForecasts = new ArrayList<>(); // 9 possible
                for (int x = 0; x < doc.getElementsByClass("period-name").size(); x++) {
                    strForecasts.add(doc.getElementsByClass("period-name").get(x).nextElementSibling().getAllElements().get(1).attr("alt"));
                }

                strShortForecasts = new ArrayList<>(); // 9 possible
                for (int x = 0; x < doc.getElementsByClass("short-desc").size(); x++) {
                    strShortForecasts.add(doc.getElementsByClass("short-desc").get(x).text().toString());
                }

                //get bitmap
                Elements img = doc.select("img");
                String strImg = img.get(5).attr("src");
                InputStream input = new java.net.URL("https://forecast.weather.gov/" + strImg).openStream();
                bitmapWeather = BitmapFactory.decodeStream(input);

                //https://forecast.weather.gov/MapClick.php?lon=-85.7&lat=42.98#.XEiwElxKiUm/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
