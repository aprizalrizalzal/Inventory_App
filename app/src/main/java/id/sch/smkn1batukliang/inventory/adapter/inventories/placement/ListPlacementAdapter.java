package id.sch.smkn1batukliang.inventory.adapter.inventories.placement;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.ListPlacementBinding;
import id.sch.smkn1batukliang.inventory.model.inventories.placement.Placement;

public class ListPlacementAdapter extends RecyclerView.Adapter<ListPlacementAdapter.ViewHolder> {

    private final List<Placement> placements = new ArrayList<>();
    private OnItemClickCallbackEdit onItemClickCallbackEdit;
    private OnItemClickCallbackDelete onItemClickCallbackDelete;

    @SuppressLint("NotifyDataSetChanged")
    public void setListPlacement(List<Placement> placements) {
        this.placements.clear();
        this.placements.addAll(placements);
        notifyDataSetChanged();
    }

    public void setOnItemClickCallbackEdit(OnItemClickCallbackEdit onItemClickCallbackEdit) {
        this.onItemClickCallbackEdit = onItemClickCallbackEdit;
    }

    public void setOnItemClickCallbackDelete(OnItemClickCallbackDelete onItemClickCallbackDelete) {
        this.onItemClickCallbackDelete = onItemClickCallbackDelete;
    }

    @NonNull
    @Override
    public ListPlacementAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_placement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListPlacementAdapter.ViewHolder holder, int position) {
        holder.bind(placements.get(position));
        holder.itemView.setOnClickListener(v -> onItemClickCallbackEdit.onItemClickedEdit(placements.get(holder.getAdapterPosition())));
        holder.binding.imgBtnDelete.setOnClickListener(v -> onItemClickCallbackDelete.onItemClickedDelete(placements.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return placements.size();
    }

    public interface OnItemClickCallbackEdit {
        void onItemClickedEdit(Placement placement);
    }

    public interface OnItemClickCallbackDelete {
        void onItemClickedDelete(Placement placement);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ListPlacementBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ListPlacementBinding.bind(itemView);
        }

        public void bind(Placement model) {
            Glide.with(itemView)
                    .load(model.getPlacementItem().getPhotoLink())
                    .placeholder(R.drawable.ic_baseline_placement)
                    .into(binding.imgListPlacement);
            binding.tvPlacement.setText(itemView.getResources().getString(R.string.f_placement, model.getPlacementItem().getPlacement()));
            binding.tvUsername.setText(itemView.getResources().getString(R.string.f_username, model.getPlacementItem().getUsername()));
        }
    }

}
