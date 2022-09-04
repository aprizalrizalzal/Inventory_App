package id.sch.smkn1batukliang.inventory.adapter.inventories.report;

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
import id.sch.smkn1batukliang.inventory.databinding.ListReportBinding;
import id.sch.smkn1batukliang.inventory.model.inventories.report.Report;

public class ListReportAdapter extends RecyclerView.Adapter<ListReportAdapter.ViewHolder> {

    private final List<Report> reports = new ArrayList<>();
    private OnItemClickCallback onItemClickCallback;
    private OnItemClickCallbackDelete onItemClickCallbackDelete;

    @SuppressLint("NotifyDataSetChanged")
    public void setListReport(List<Report> reports) {
        this.reports.clear();
        this.reports.addAll(reports);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(reports.get(position));
        holder.itemView.setOnClickListener(v -> onItemClickCallback.onItemClicked(reports.get(holder.getAdapterPosition())));
        holder.binding.imgBtnDelete.setOnClickListener(v -> onItemClickCallbackDelete.onItemClickedDelete(reports.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public interface OnItemClickCallback {
        void onItemClicked(Report report);
    }

    public interface OnItemClickCallbackDelete {
        void onItemClickedDelete(Report report);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ListReportBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ListReportBinding.bind(itemView);
        }

        public void bind(Report model) {
            if (model.getReportItem().isStatus()) {
                Glide.with(itemView)
                        .load(R.drawable.ic_baseline_verified)
                        .into(binding.imgListReport);
                binding.tvPurpose.setText(itemView.getResources().getString(R.string.f_agree, model.getReportItem().getPurpose()));
            } else {
                Glide.with(itemView)
                        .load(R.drawable.ic_baseline_help)
                        .into(binding.imgListReport);
                binding.tvPurpose.setText(itemView.getResources().getString(R.string.f_reviewed, model.getReportItem().getPurpose()));
            }
            binding.tvReport.setText(model.getReportItem().getReport());
        }
    }
}
