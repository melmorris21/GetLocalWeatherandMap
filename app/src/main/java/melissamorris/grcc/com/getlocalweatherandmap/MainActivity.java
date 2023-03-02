package melissamorris.grcc.com.getlocalweatherandmap;

import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Button buttonGetCurrentAddress = (Button) findViewById(R.id.buttonGetCurrentAddress);
        buttonGetCurrentAddress.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //client (a instance of FusedLocationProviderClient) will provide latitude and longitude
                FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                //if premissions are granted
                //import android.Manifest; for some reason this doesn't automatically happen????
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                //update client with latest location
                client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                public void onSuccess(Location location) { if (location != null) {
                        final List<Address> addresses = getGeocoderInfo(MainActivity.this, location);
                        EditText editTextAddress = (EditText) findViewById(R.id.editTextAddress); //actual address
                        editTextAddress.setText(addresses.get(0).getAddressLine(0)); //actual address
                        TextView textViewLongititude = (TextView) findViewById(R.id.textViewLongitude);
                        textViewLongititude.setText(addresses.get(0).getLongitude() + ""); //actual latitude
                        TextView textViewLatitude = (TextView) findViewById(R.id.textViewLatitude);
                        textViewLatitude.setText(addresses.get(0).getLatitude() + ""); //actual longitude
                }

            }

        });

        Button buttonWeather = (Button)findViewById(R.id.buttonWeather);
        buttonWeather.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Get the current latitude and longitude from the textViews
                TextView textViewLatitude = (TextView) findViewById(R.id.textViewLatitude);
                TextView textViewLongitude = (TextView) findViewById(R.id.textViewLongitude);
                //if the textviews have latitude and longitudes, create an intent to ActivityWeather and pass latitude and longitude
                if (!textViewLatitude.getText().toString().equals("0") & !textViewLongitude.getText().toString().equals("0")) {
                    Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
                    intent.putExtra("passLatitude", Double.parseDouble(textViewLatitude.getText().toString()));
                    intent.putExtra("passLongitude", Double.parseDouble(textViewLongitude.getText().toString()));
                    startActivity(intent);
                } else {
                //if textviews don't have latitude and longitudes, stop and show error in toast
                    Toast.makeText(getApplicationContext(),
                            "You must enter a city or address!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        Button buttonMap = (Button) findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //get the current latitude and longitude from textViews
                TextView textViewLatitude = (TextView) findViewById(R.id.textViewLatitude);
                TextView textViewLongitude = (TextView) findViewById(R.id.textViewLongitude);
                //if the textviews have latitude and longitudes, create uri that will drive an intent to open google map
                if(!textViewLatitude.getText().toString().equals("0") & !textViewLongitude.getText().toString().equals("0")) {
                String location = "geo:" + Double.parseDouble(textViewLatitude.getText().toString()) + ","
                        + Double.parseDouble(textViewLongitude.getText().toString());
                //Uri is Uniform resource identifier used to pass the address to a new intent.
                Uri uri = Uri.parse(location);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                }
                else{
                //if textviews don't have latitude and longitudes, stop and show error in toast
                Toast.makeText(getApplicationContext(),
                "You must enter a city or address!",
                        Toast.LENGTH_LONG).show();
            }

        }
        });
        final EditText editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        editTextAddress.setOnEditorActionListener(new TextView.OnEditorActionListener(){
           @Override
           public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
               if(actionId == EditorInfo.IME_ACTION_DONE) {
                   String strAddress = editTextAddress.getText().toString();
                   Geocoder coder = new Geocoder(MainActivity.this);
                   List<Address> addresses;
                   try {
                       addresses = coder.getFromLocationName(strAddress, 5);
                       if (addresses.size() == 0) {
                           Toast.makeText(getApplicationContext(),
                                   "No such place!",
                                   Toast.LENGTH_LONG).show();
                           editTextAddress.getText().clear();
                       } else {
                           double latitude = addresses.get(0).getLatitude();
                           double longitude = addresses.get(0).getLongitude();
                           TextView textViewLongititude = (TextView) findViewById(R.id.textViewLongitude);
                           textViewLongititude.setText(addresses.get(0).getLongitude() + "");
                           TextView textViewLatitude = (TextView) findViewById(R.id.textViewLatitude);
                           textViewLatitude.setText(addresses.get(0).getLatitude() + "");
                       }
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
               return false;
           }
        });
    }
});
}

    private List<Address> getGeocoderInfo(Activity activity, Location location){
        //https://www.programcreek.com/java-api-examples/android.location.Geocoder // latitude = addresses.get(0).getLatitude();
        // longitude = addresses.get(0).getLongitude();
        // fullAddress = addresses.get(0).getAddressLine(0) + " " + address.get(0).getAddressLine(1) + " " addresses.get(0).getAddressLine(2); // zipcode = addresses.get(0).getPostalCode();
        // country = addresses.get(0).getCountryName();
        // state = addresses.get(0).getAdminArea();
        // city = addresses.get(0).getLocality();
        // streetName = addresses.get(0).getThoroughfare();
        // houseNumber = addresses.get(0).getSubThoroughfare();
        // streetAddress = addresses.get(0).getSubThoroughfare() + " " + addresses.get(0).getThoroughfare();
        List<Address>addresses = null;
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        String stuff = "";
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }

}
