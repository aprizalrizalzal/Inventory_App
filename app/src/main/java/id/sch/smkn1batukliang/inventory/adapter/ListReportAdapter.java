package id.sch.smkn1batukliang.inventory.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.ListReportBinding;
import id.sch.smkn1batukliang.inventory.model.report.Report;
import id.sch.smkn1batukliang.inventory.model.Users;

public class ListReportAdapter extends RecyclerView.Adapter<ListReportAdapter.ViewHolder> {

    private static final String TAG = "ListReportAdapter";
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

        public void bind(Report report) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            CollectionReference collectionReferenceUsers = firestore.collection("users");

            collectionReferenceUsers.document(report.getAuthId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "bind: tvUsernameSuccessfully " + collectionReferenceUsers.document(report.getAuthId()));
                    Users users = task.getResult().toObject(Users.class);
                    if (users != null) {
                        binding.tvUsername.setText(users.getUsername());
                    }
                } else {
                    Log.w(TAG, "bind: tvUsernameFailure ", task.getException());
                }
            });

            if (!report.getReportItem().getVicePrincipal().isApproved()
                    && !report.getReportItem().getTeamLeader().isApproved()
                    && !report.getReportItem().getPrincipal().isApproved()
                    && !report.getReportItem().isReceived()) {
                Glide.with(itemView)
                        .load(R.drawable.ic_baseline_report)
                        .apply(new RequestOptions().override(64, 64))
                        .into(binding.imgListReport);
                binding.tvDescription.setText(report.getReportItem().getPrincipal().getDescription());
            } else if (report.getReportItem().getVicePrincipal().isApproved()
                    && !report.getReportItem().getTeamLeader().isApproved()
                    && !report.getReportItem().getPrincipal().isApproved()
                    && !report.getReportItem().isReceived()) {
                Glide.with(itemView)
                        .load(R.drawable.ic_baseline_known)
                        .apply(new RequestOptions().override(64, 64))
                        .into(binding.imgListReport);
                binding.tvDescription.setText(report.getReportItem().getVicePrincipal().getDescription());
            } else if (report.getReportItem().getVicePrincipal().isApproved()
                    && report.getReportItem().getTeamLeader().isApproved()
                    && !report.getReportItem().getPrincipal().isApproved()
                    && !report.getReportItem().isReceived()) {
                Glide.with(itemView)
                        .load(R.drawable.ic_baseline_approved)
                        .apply(new RequestOptions().override(64, 64))
                        .into(binding.imgListReport);
                binding.tvDescription.setText(report.getReportItem().getTeamLeader().getDescription());
            } else if (report.getReportItem().getVicePrincipal().isApproved()
                    && report.getReportItem().getTeamLeader().isApproved()
                    && report.getReportItem().getPrincipal().isApproved()
                    && !report.getReportItem().isReceived()) {
                Glide.with(itemView)
                        .load(R.drawable.ic_baseline_verified)
                        .apply(new RequestOptions().override(64, 64))
                        .into(binding.imgListReport);
                binding.tvDescription.setText(report.getReportItem().getPrincipal().getDescription());
            } else if (report.getReportItem().getVicePrincipal().isApproved()
                    && report.getReportItem().getTeamLeader().isApproved()
                    && report.getReportItem().getPrincipal().isApproved()
                    && report.getReportItem().isReceived()) {
                Glide.with(itemView)
                        .load(R.drawable.ic_baseline_received)
                        .apply(new RequestOptions().override(64, 64))
                        .into(binding.imgListReport);
                binding.tvDescription.setText(report.getReportItem().getPrincipal().getDescription());
            }
            binding.tvPurpose.setText(report.getReportItem().getPurpose());
            binding.tvReport.setText(report.getReportItem().getReport());
        }
    }
}
