package com.pitechitsolutions.essentialtrailerhire;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder> {

    private List<Map.Entry<String, Integer>> sortedEntries;

    public LeaderBoardAdapter(List<Map.Entry<String, Integer>> sortedEntries) {
        this.sortedEntries = sortedEntries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboard_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<String, Integer> entry = sortedEntries.get(position);
        holder.branchName.setText(entry.getKey());

        // Display the rank instead of the count
        String suffix = getNumberSuffix(position + 1);  // Adding 1 because position is 0-based
        holder.rentalCount.setText((position + 1) + suffix);
    }

    // A helper function to get the suffix for numbers like "1st", "2nd", etc.
    public String getNumberSuffix(int number) {
        if (number >= 11 && number <= 13) {
            return "th";
        }
        switch (number % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }

    @Override
    public int getItemCount() {
        return sortedEntries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView branchName;
        public TextView rentalCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            branchName = itemView.findViewById(R.id.branchNameTextView);
            rentalCount = itemView.findViewById(R.id.rentalCountTextView);

        }
    }
}
