package com.leoespinal.fairfare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.leoespinal.fairfare.models.RideServiceOption;
import com.leoespinal.fairfare.services.RideOptionsService;

import java.util.List;

public class RideEstimatesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_estimates);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewId);
        List<RideServiceOption> rideServiceOptionList = RideOptionsService.getUniqueInstance().getRideServiceOptionList();
        RideServiceOptionListAdapter rideServiceOptionListAdapter = new RideServiceOptionListAdapter(rideServiceOptionList);
        recyclerView.setAdapter(rideServiceOptionListAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getParent());
        recyclerView.setLayoutManager(layoutManager);
    }
}
