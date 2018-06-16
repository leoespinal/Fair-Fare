package com.leoespinal.fairfare;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leoespinal.fairfare.models.RideServiceOption;


import java.util.List;

public class RideServiceOptionListAdapter extends RecyclerView.Adapter<RideServiceOptionListAdapter.RideServiceOptionViewHolder> {
    private List<RideServiceOption> rideServiceOptionList;

    public RideServiceOptionListAdapter(List<RideServiceOption> rideServiceOptionList) {
        this.rideServiceOptionList = rideServiceOptionList;
    }

    @NonNull
    @Override
    public RideServiceOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_option_list_item, parent, false);
        return new RideServiceOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideServiceOptionViewHolder viewHolder, int position) {
        viewHolder.serviceName.setText(rideServiceOptionList.get(viewHolder.getAdapterPosition()).getServiceBaseName());
        viewHolder.estimatePriceRange.setText(rideServiceOptionList.get(viewHolder.getAdapterPosition()).getEstimateRange());
        viewHolder.eta.setText(rideServiceOptionList.get(viewHolder.getAdapterPosition()).getEta() + " MINS");
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Show details activity or book ride via app
            }
        });
    }

    @Override
    public int getItemCount() {
        return rideServiceOptionList.size();
    }

    public static class RideServiceOptionViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView serviceName;
        private TextView estimatePriceRange;
        private TextView eta;

        public RideServiceOptionViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cardViewId);
            serviceName = (TextView) itemView.findViewById(R.id.rideServiceNameTextViewId);
            estimatePriceRange = (TextView) itemView.findViewById(R.id.priceEstimateRangeTextViewId);
            eta = (TextView) itemView.findViewById(R.id.etaTextVIewId);
        }
    }
}
