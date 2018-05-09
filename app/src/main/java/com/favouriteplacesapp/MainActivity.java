package com.favouriteplacesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    AppCompatButton addPlacesButton;
    static ArrayList<String> places;
    static ArrayList<LatLng> locations;
    static ArrayAdapter placesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addPlacesButton=(AppCompatButton) findViewById(R.id.addPlacesButton);
        listView=(ListView) findViewById(R.id.placesView);

        places=new ArrayList<>();
        locations=new ArrayList<>();

        //get stored values from shared preferences
        SharedPreferences sp=this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        ArrayList<String> latitudes=new ArrayList<>();
        ArrayList<String> longitudes=new ArrayList<>();

        places.clear();
        latitudes.clear();
        longitudes.clear();
        locations.clear();

        try {
            places=(ArrayList<String>) ObjectSerializer.deserialize(sp.getString("places",ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes=(ArrayList<String>) ObjectSerializer.deserialize(sp.getString("latitudes",ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes=(ArrayList<String>) ObjectSerializer.deserialize(sp.getString("longitudes",ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(places.size()>0 && latitudes.size()>0 && longitudes.size()>0)
        {
            if(places.size()==latitudes.size() && latitudes.size()==longitudes.size())
            {
                for(int i=0;i<latitudes.size();i++)
                {
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i))));
                }
            }
        }

        placesArrayAdapter=new ArrayAdapter(this,R.layout.listview_text,places);
        listView.setAdapter(placesArrayAdapter);

        addPlacesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("placesInfo",-1);
                startActivity(intent);

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("placesInfo",i);
                startActivity(intent);
            }
        });

    }
}
