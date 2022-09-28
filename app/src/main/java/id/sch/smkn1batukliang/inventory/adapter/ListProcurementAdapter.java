package id.sch.smkn1batukliang.inventory.adapter;

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
import id.sch.smkn1batukliang.inventory.utili.MoneyTextWatcher;
import id.sch.smkn1batukliang.inventory.databinding.ListProcurementBinding;
import id.sch.smkn1batukliang.inventory.model.procurement.Procurement;

public class ListProcurementAdapter extends RecyclerView.Adapter<ListProcurementAdapter.ViewHolder> {

    private final List<Procurement> procurements = new ArrayList<>();
    private OnItemClickCallbackEdit onItemClickCallbackEdit;
    private OnItemClickCallbackDelete onItemClickCallbackDelete;


    @SuppressLint("NotifyDataSetChanged")
    public void setListProcurement(List<Procurement> procurements) {
        this.procurements.clear();
        this.procurements.addAll(procurements);
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_procurement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(procurements.get(position));
        holder.itemView.setOnClickListener(v -> onItemClickCallbackEdit.onItemClickedEdit(procurements.get(holder.getAdapterPosition())));
        holder.binding.imgBtnDelete.setOnClickListener(v -> onItemClickCallbackDelete.onItemClickedDelete(procurements.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return procurements.size();
    }

    public interface OnItemClickCallbackEdit {
        void onItemClickedEdit(Procurement procurement);
    }

    public interface OnItemClickCallbackDelete {
        void onItemClickedDelete(Procurement procurement);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ListProcurementBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ListProcurementBinding.bind(itemView);
        }

        public void bind(Procurement model) {
            Glide.with(itemView)
                    .load(model.getProcurementItem().getPhotoLink())
                    .placeholder(R.drawable.ic_baseline_procurement)
                    .apply(new RequestOptions().override(128,128))
                    .into(binding.imgListItem);
            binding.tvNameOfGoods.setText(model.getProcurementItem().getProcurement());
            binding.tvPrice.setText(MoneyTextWatcher.formatCurrency(model.getProcurementItem().getPrice()));
            binding.tvVolume.setText(String.valueOf(model.getProcurementItem().getVolume()));
            binding.tvUnit.setText(model.getProcurementItem().getUnit());
            binding.tvAmount.setText(MoneyTextWatcher.formatCurrency(model.getProcurementItem().getAmount()));
        }
    }
}
