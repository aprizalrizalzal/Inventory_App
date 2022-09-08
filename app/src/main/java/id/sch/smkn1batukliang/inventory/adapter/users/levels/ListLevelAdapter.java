package id.sch.smkn1batukliang.inventory.adapter.users.levels;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.ListLevelBinding;
import id.sch.smkn1batukliang.inventory.model.users.Users;
import id.sch.smkn1batukliang.inventory.model.users.levels.Levels;

public class ListLevelAdapter extends RecyclerView.Adapter<ListLevelAdapter.ViewHolder> {

    private static final String TAG = "ListReportAdapter";
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
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            CollectionReference collectionReferenceUsers = firestore.collection("users");

            collectionReferenceUsers.document(levels.getAuthId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "bind: tvUsernameSuccessfully " + collectionReferenceUsers.document(levels.getAuthId()));
                    Users users = task.getResult().toObject(Users.class);
                    if (users != null) {
                        Glide.with(itemView)
                                .load(users.getPhotoLink())
                                .placeholder(R.drawable.ic_baseline_label)
                                .into(binding.imgListLevels);
                        binding.tvUsername.setText(users.getUsername());
                    }
                } else {
                    Log.w(TAG, "bind: tvUsernameFailure ", task.getException());
                }
            });
            binding.tvLevel.setText(itemView.getResources().getString(R.string.f_level, levels.getLevelsItem().getLevel()));
        }
    }
}
