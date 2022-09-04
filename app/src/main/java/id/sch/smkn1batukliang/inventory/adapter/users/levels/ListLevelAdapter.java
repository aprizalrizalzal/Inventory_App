package id.sch.smkn1batukliang.inventory.adapter.users.levels;

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
import id.sch.smkn1batukliang.inventory.databinding.ListLevelBinding;
import id.sch.smkn1batukliang.inventory.model.users.levels.Levels;

public class ListLevelAdapter extends RecyclerView.Adapter<ListLevelAdapter.ViewHolder> {

    private final List<Levels> levels = new ArrayList<>();
    private OnItemClickCallbackEdit onItemClickCallbackEdit;
    private OnItemClickCallbackDelete onItemClickCallbackDelete;

    @SuppressLint("NotifyDataSetChanged")
    public void setListLevel(List<Levels> levels) {
        this.levels.clear();
        this.levels.addAll(levels);
        notifyDataSetChanged();
    }

    public void setOnItemClickCallbackDelete(OnItemClickCallbackDelete onItemClickCallbackDelete) {
        this.onItemClickCallbackDelete = onItemClickCallbackDelete;
    }

    public void setOnItemClickCallbackEdit(OnItemClickCallbackEdit onItemClickCallbackEdit) {
        this.onItemClickCallbackEdit = onItemClickCallbackEdit;
    }

    @NonNull
    @Override
    public ListLevelAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_level, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListLevelAdapter.ViewHolder holder, int position) {
        holder.bind(levels.get(position));
        holder.itemView.setOnClickListener(v -> onItemClickCallbackEdit.onItemClickedEdit(levels.get(holder.getAdapterPosition())));
        holder.binding.imgBtnDelete.setOnClickListener(v -> onItemClickCallbackDelete.onItemClickedDelete(levels.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    public interface OnItemClickCallbackEdit {
        void onItemClickedEdit(Levels levels);
    }

    public interface OnItemClickCallbackDelete {
        void onItemClickedDelete(Levels levels);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ListLevelBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ListLevelBinding.bind(itemView);
        }

        public void bind(Levels levels) {
            Glide.with(itemView)
                    .load(levels.getUsers().getPhotoLink())
                    .placeholder(R.drawable.ic_baseline_label)
                    .into(binding.imgListLevels);
            binding.tvUsername.setText(levels.getUsers().getUsername());
            binding.tvLevel.setText(itemView.getResources().getString(R.string.f_level,levels.getUsers().getLevel()));
        }
    }
}
