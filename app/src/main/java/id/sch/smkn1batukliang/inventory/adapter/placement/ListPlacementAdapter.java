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
import id.sch.smkn1batukliang.inventory.databinding.ListPlacementBinding;
import id.sch.smkn1batukliang.inventory.model.placement.Placement;

public class ListPlacementAdapter extends RecyclerView.Adapter<ListPlacementAdapter.ViewHolder> {

    private final List<Placement> placements = new ArrayList<>();
    private OnItemClickCallback onItemClickCallback;
    private OnItemClickCallbackDelete onItemClickCallbackDelete;
    private boolean activate = true;

    @SuppressLint("NotifyDataSetChanged")
    public void setActivateButtons(boolean activate) {
        this.activate = activate;
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void setListPlacement(List<Placement> placements) {
        this.placements.clear();
        this.placements.addAll(placements);
        notifyDataSetChanged();
    }

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
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
        if (activate) {
            holder.binding.imgBtnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.binding.imgBtnDelete.setVisibility(View.INVISIBLE);
        }
        holder.itemView.setOnClickListener(v -> onItemClickCallback.onItemClickedEdit(placements.get(holder.getAdapterPosition())));
        holder.binding.imgBtnDelete.setOnClickListener(v -> onItemClickCallbackDelete.onItemClickedDelete(placements.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return placements.size();
    }

    public interface OnItemClickCallback {
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
                    .apply(new RequestOptions().override(128,128))
                    .into(binding.imgListPlacement);
            binding.tvPlacement.setText(itemView.getResources().getString(R.string.f_placement, model.getPlacementItem().getPlacement()));
            binding.tvUsername.setText(itemView.getResources().getString(R.string.f_username, model.getPlacementItem().getUsername()));
        }
    }

}
