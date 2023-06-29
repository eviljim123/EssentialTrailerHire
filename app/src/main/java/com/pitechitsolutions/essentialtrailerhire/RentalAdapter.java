package com.pitechitsolutions.essentialtrailerhire;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class RentalAdapter extends RecyclerView.Adapter<RentalAdapter.RentalViewHolder> {

    private List<Rental> rentalList;

    class RentalViewHolder extends RecyclerView.ViewHolder {

        TextView clientNameView, trailerBarcodeView, invoiceNumberView, bookingTimeView, returnTimeView;

        RentalViewHolder(View itemView) {
            super(itemView);
            clientNameView = itemView.findViewById(R.id.clientName);
            trailerBarcodeView = itemView.findViewById(R.id.trailerBarcode);
            invoiceNumberView = itemView.findViewById(R.id.invoiceNumber);
            bookingTimeView = itemView.findViewById(R.id.bookingTime);
            returnTimeView = itemView.findViewById(R.id.returnTime);
        }
    }

    RentalAdapter(List<Rental> rentalList) {
        this.rentalList = rentalList;
    }

    @NonNull
    @Override
    public RentalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rental_report_item, parent, false);
        return new RentalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RentalViewHolder holder, int position) {
        Rental rental = rentalList.get(position);
        holder.clientNameView.setText("Client ID: " + rental.getCustomerId());
        holder.trailerBarcodeView.setText("Trailer Barcode: " + rental.getTrailerBarcode());
        holder.invoiceNumberView.setText("Invoice Number: " + rental.getInvoiceNumber());
        holder.bookingTimeView.setText("Booking Time: " + rental.getRentalDateTime());
        holder.returnTimeView.setText("Return Time: " + rental.getSelectedDeliveryDateTime());
    }

    @Override
    public int getItemCount() {
        return rentalList.size();
    }
}
