package com.crosby.foodfinderapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.yelp.fusion.client.connection.YelpFusionApi;
import com.yelp.fusion.client.connection.YelpFusionApiFactory;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.SearchResponse;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements RoutingListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private FirebaseAuth mAuth;

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    LatLng userLatLng;


    int i = 0;
    boolean hasValue = false;

    String searchvalue = QueryActivity.query;

    Double lat;
    Double lon;
    String latstring;
    String lonstring;
    LatLng latlng2;
    String businessName;
    ArrayList<Business> businesses = new ArrayList<Business>();
    String address1;
    String city;
    String zip_code;
    String phone;
    LatLng currentLatLng;

    SupportMapFragment supportmapFragment;
    FusedLocationProviderClient client;


    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.quantum_googblue};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        supportmapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        client = LocationServices.getFusedLocationProviderClient(this);

        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        }else{
            //When permission denied
            //Ask for permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
        polylines = new ArrayList<>();

    }

    public void logoutUser(View view) {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        }
        startActivity(intent);
        finish();
        return;

    }

        private void getCurrentLocation() {
        //System.out.println("Hello " + yelpFusionApi.toString());
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                supportmapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        mMap = googleMap;
                        if (location!=null && hasValue==false){

                            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                @Override
                                public View getInfoWindow(Marker marker) {
                                    return null;
                                }

                                @Override
                                public View getInfoContents(Marker marker) {
                                    Context context = getApplicationContext();
                                    LinearLayout info = new LinearLayout(context);
                                    info.setOrientation(LinearLayout.VERTICAL);

                                    TextView title = new TextView(context);
                                    title.setTextColor(Color.BLACK);
                                    title.setGravity(Gravity.CENTER);
                                    title.setTypeface(null, Typeface.BOLD);
                                    title.setText(marker.getTitle());

                                    TextView snippet = new TextView(context);
                                    snippet.setTextColor(Color.GRAY);
                                    snippet.setText(marker.getSnippet());

                                    Button button = new Button(context);
                                    button.setText("Directions");

                                    info.addView(title);
                                    info.addView(snippet);
                                    info.addView(button);

                                    return info;
                                }
                            });

                            final LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());

                            currentLatLng = latlng;

                            MarkerOptions options = new MarkerOptions().position(latlng).title("You").snippet("This is you").zIndex(100).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
                            googleMap.addMarker(options);

                            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                @Override
                                public void onInfoWindowClick(Marker place2) {

                                    TextView mTestTextView = (TextView) findViewById(R.id.test);
                                    TextView mTestTextView2 = (TextView) findViewById(R.id.test2);
                                    mTestTextView = (TextView) findViewById(R.id.test);
                                    mTestTextView2 = (TextView) findViewById(R.id.test2);
                                    String markerName = place2.getTitle();
                                    String markertext = place2.getSnippet();
                                    latlng2 = place2.getPosition();
                                    System.out.println(markertext);
                                    mTestTextView.setText(markerName);

                                    getRouteToMarker(latlng2);


                                }
                            });

                        }

                        if(hasValue==true) {
                            LatLng latlng2 = new LatLng(lat, lon);

                            MarkerOptions place2 = new MarkerOptions().position(latlng2).title(businessName).snippet(address1 + "\n" + city + "\n" + zip_code + "\n" + phone);
                            googleMap.addMarker(place2);
                            i = i + 1;
                        }

                        new locationDisplay().execute();

                        lat = location.getLatitude();
                        lon = location.getLongitude();
                        latstring = lat.toString();
                        lonstring = lon.toString();

                    }
                });
                System.out.println(i);

            }

        });
    }

    private void getRouteToMarker(LatLng latlng2) {
        Routing routing = new Routing.Builder()
                .key("AIzaSyBhajaNUCQPQPXpvIUm0lrFz7hmWpMKtZE")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(currentLatLng, latlng2)
                .build();
        routing.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": Distance: "+ route.get(i).getDistanceValue()/1000+" KM"+", Journey Duration: "+ route.get(i).getDurationValue()/60 + " Minutes",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private class locationDisplay extends AsyncTask<Void, Void, Void> {

        TextView mTestTextView = (TextView) findViewById(R.id.test);
        TextView mTestTextView2 = (TextView) findViewById(R.id.test2);

        @Override
        protected Void doInBackground(Void... voids) {
            YelpFusionApi yelpFusionApi = null;
            YelpFusionApiFactory apiFactory = new YelpFusionApiFactory();
            try {
                hasValue = true;

                mTestTextView = (TextView) findViewById(R.id.test);
                mTestTextView2 = (TextView) findViewById(R.id.test2);
                yelpFusionApi = apiFactory.createAPI("UKYiviWRJJV0GFIK35QDH1i6BJur_tpf4A2hjVtItJwNSHqRhz-U7zKeEjiG5DWVDcP5Ipe6Za3ZvQ5_rSYJhg6TxviDxGxedK8g2qsm6hRZT8bORI91tCiJ2z4cXnYx");
                Map<String, String> params = new HashMap<>();

                // general params
                params.put("term", searchvalue);
                params.put("latitude", latstring);
                params.put("longitude", lonstring);
                params.put("radius", "40000");

                Call<SearchResponse> call = yelpFusionApi.getBusinessSearch(params);
                SearchResponse searchResponse = call.execute().body();

                int totalNumberOfResult = searchResponse.getTotal();  // 3

                ArrayList<Business> businesses = searchResponse.getBusinesses();
                if (i < 20) {
                    businessName = businesses.get(i).getName();
                    lat = businesses.get(i).getCoordinates().getLatitude();
                    lon = businesses.get(i).getCoordinates().getLongitude();
                    address1 = businesses.get(i).getLocation().getAddress1();
                    city = businesses.get(i).getLocation().getCity();
                    zip_code = businesses.get(i).getLocation().getZipCode();
                    phone = businesses.get(i).getPhone();
                    System.out.println(businessName);
                    hasValue = true;
                    getCurrentLocation();
                };


                String businessName = businesses.get(0).getName();  // "JapaCurry Truck"
                Double rating = businesses.get(0).getRating();  // 4.0
                Double lat = businesses.get(0).getCoordinates().getLatitude();
                Double lon = businesses.get(0).getCoordinates().getLongitude();

                mTestTextView.setText(businessName);
                mTestTextView2.setText(lat.toString());


            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }




}
