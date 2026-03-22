package com.example.lottery.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lottery.R;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ModerationAdapter extends RecyclerView.Adapter<ModerationAdapter.ViewHolder> {

    private List<ModerationItem> items;

    public ModerationAdapter(List<ModerationItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_moderation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModerationItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDetail.setText(item.getDetail());
        
        holder.btnOption1.setOnClickListener(v -> {
            // Handle Option 1 click
        });
        
        holder.btnOption2.setOnClickListener(v -> {
            // Handle Option 2 click
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetail;
        MaterialButton btnOption1, btnOption2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvModerationTitle);
            tvDetail = itemView.findViewById(R.id.tvModerationDetail);
            btnOption1 = itemView.findViewById(R.id.btnOption1);
            btnOption2 = itemView.findViewById(R.id.btnOption2);
        }
    }
}
