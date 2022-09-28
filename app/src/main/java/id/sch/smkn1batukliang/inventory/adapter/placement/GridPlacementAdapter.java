package id.sch.smkn1batukliang.inventory.adapter.placement;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.GridPlacementBinding;
import id.sch.smkn1batukliang.inventory.model.placement.Placement;

public class GridPlacementAdapter extends RecyclerView.Adapter<GridPlacementAdapter.ViewHolder> {

    private final List<Placement> placements = new ArrayList<>();
    private OnItemClickCallback onItemClickCallback;

    @SuppressLint("NotifyDataSetChanged")
    public void setGridPlacementForProcurement(List<Placement> placements) {
        this.placements.clear();
        this.placements.addAll(placements);
        notifyDataSetChanged();
    }

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public GridPlacementAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_placement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridPlacementAdapter.ViewHolder holder, int position) {
        holder.bind(placements.get(position));
        holder.itemView.setOnClickListener(v -> onItemClickCallback.onItemClicked(placements.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return placements.size();
    }

    public interface OnItemClickCallback {
        void onItemClicked(Placement placement);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final GridPlacementBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = GridPlacementBinding.bind(itemView);
        }

        public void bind(Placement model) {
            Glide.with(itemView)
                    .load(model.getPlacementItem().getPhotoLink())
                    .placeholder(R.drawable.ic_baseline_placement)
                    .apply(new RequestOptions().override(128,128))
                    .into(binding.imgGridPlacement);
            binding.tvPlacement.setText(model.getPlacementItem().getPlacement());
            binding.tvUsername.setText(model.getPlacementItem().getUsername());
        }
    }

}
