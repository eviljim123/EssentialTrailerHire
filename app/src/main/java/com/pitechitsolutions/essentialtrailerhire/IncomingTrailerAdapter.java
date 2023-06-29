package com.pitechitsolutions.essentialtrailerhire;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class IncomingTrailerAdapter extends ArrayAdapter<IncomingTrailer> {

    public IncomingTrailerAdapter(@NonNull Context context, int resource, @NonNull List<IncomingTrailer> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_incoming_trailer, parent, false);
        }

        IncomingTrailer trailer = getItem(position);

        TextView tvOriginBranch = convertView.findViewById(R.id.tv_origin_branch);
        TextView tvEstimatedArrival = convertView.findViewById(R.id.tv_estimated_arrival);
        TextView tvTrailerId = convertView.findViewById(R.id.tv_trailer_id);

        if (trailer != null) {
            tvOriginBranch.setText("Origin Branch: " + trailer.getOriginBranch());
            tvEstimatedArrival.setText("Estimated Delivery Date: " + trailer.getEstimatedArrivalDateTime());
            tvTrailerId.setText("Trailer Barcode: " + trailer.getTrailerId());
        }

        return convertView;
    }
}
