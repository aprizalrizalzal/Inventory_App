package id.sch.smkn1batukliang.inventory.adapter.users;

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
import id.sch.smkn1batukliang.inventory.databinding.ListUserBinding;
import id.sch.smkn1batukliang.inventory.model.users.Users;

public class ListUserAdapter extends RecyclerView.Adapter<ListUserAdapter.ViewHolder> {

    private final List<Users> users = new ArrayList<>();
    private OnItemClickCallback onItemClickCallback;
    private OnItemClickCallbackDelete onItemClickCallbackDelete;

    @SuppressLint("NotifyDataSetChanged")
    public void setListUser(List<Users> users) {
        this.users.clear();
        this.users.addAll(users);
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(users.get(position));
        holder.itemView.setOnClickListener(v -> onItemClickCallback.onItemClicked(users.get(holder.getAdapterPosition())));
        holder.binding.imgBtnDelete.setOnClickListener(v -> onItemClickCallbackDelete.onItemClickedDelete(users.get(holder.getAdapterPosition())));

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public interface OnItemClickCallback {
        void onItemClicked(Users users);
    }

    public interface OnItemClickCallbackDelete {
        void onItemClickedDelete(Users users);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ListUserBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ListUserBinding.bind(itemView);
        }

        public void bind(Users users) {
            Glide.with(itemView)
                    .load(users.getPhotoLink())
                    .placeholder(R.drawable.ic_baseline_account_circle)
                    .apply(new RequestOptions().override(128,128))
                    .into(binding.imgListUsers);
            binding.tvUsername.setText(users.getUsername());
            binding.tvEmail.setText(users.getEmail());
            binding.tvWhatsappNumber.setText(users.getWhatsappNumber());
        }
    }

}
