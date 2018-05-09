package com.favouriteplacesapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    int index;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void showLocation(Location location,String title)
    {
        LatLng loc=new LatLng(location.getLatitude(),location.getLongitude());

        mMap.clear();
        if(!title.equals("Your Location"))
        {
            mMap.addMarker(new MarkerOptions().position(loc).title(title));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,7));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent=getIntent();
        index=intent.getIntExtra("placesInfo",0);

        if(index==-1)
        {
            mMap.setOnMapLongClickListener(this);
            locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    showLocation(location,"Your Location");
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location lastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                showLocation(lastKnownLocation,"Your Location");
            }
        }
        else
        {
            Location placeLocation=new Location(LocationManager.GPS_PROVIDER);

            placeLocation.setLatitude(MainActivity.locations.get(index).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(index).longitude);

            showLocation(placeLocation, MainActivity.places.get(index));
        }

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder=new Geocoder(this, Locale.getDefault());
        String address="";
        try {
            List<Address> addressList=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addressList!=null && addressList.size()>0)
            {
                if(addressList.get(0).getThoroughfare()!=null)
                {
                    if (addressList.get(0).getSubThoroughfare() != null)
                    {
                        address+=addressList.get(0).getSubThoroughfare()+" ";
                    }
                    address+=addressList.get(0).getThoroughfare()+" ";
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }

        if(address.equals(""))
        {
            SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
            address=sdf.format(new Date());
        }

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);
        MainActivity.placesArrayAdapter.notifyDataSetChanged();

        //store in sharedPreferences
        SharedPreferences sp=this.getSharedPreferences(getPackageName(),Context.MODE_PRIVATE);
        SharedPreferences.Editor ed=sp.edit();

        ArrayList<String> latitudes=new ArrayList<>();
        ArrayList<String> longitudes=new ArrayList<>();

        for(LatLng coordinates:MainActivity.locations)
        {
            latitudes.add(Double.toString(coordinates.latitude));
            longitudes.add(Double.toString(coordinates.longitude));
        }

        try {

            ed.putString("places",ObjectSerializer.serialize(MainActivity.places));
            ed.putString("latitudes",ObjectSerializer.serialize(latitudes));
            ed.putString("longitudes",ObjectSerializer.serialize(longitudes));
            ed.commit();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Place added in the list", Toast.LENGTH_SHORT).show();
    }
}
