package id.sch.smkn1batukliang.inventory.ui.report;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.adapter.ListReportAdapter;
import id.sch.smkn1batukliang.inventory.databinding.FragmentListReportBinding;
import id.sch.smkn1batukliang.inventory.model.levels.Levels;
import id.sch.smkn1batukliang.inventory.model.report.Report;
import id.sch.smkn1batukliang.inventory.utils.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.utils.RecyclerViewEmptyData;

public class ListReportFragment extends Fragment {

    public static final String EXTRA_REPORT = "extra_report";
    private static final String TAG = "ListReportFragment";
    private final ArrayList<Report> reports = new ArrayList<>();
    private FragmentListReportBinding binding;
    private View view;
    private String authId;
    private DatabaseReference databaseReferenceLevels, databaseReferenceReport;
    private StorageReference storageReferenceReport;
    private ListReportAdapter adapter;
    private CustomProgressDialog progressDialog;

    public ListReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentListReportBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        progressDialog = new CustomProgressDialog(getActivity());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            authId = user.getUid();
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceLevels = database.getReference("levels");
        databaseReferenceReport = database.getReference("report");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReferenceReport = storage.getReference("users/procurement/");

        binding.refreshLayout.setOnRefreshListener(() -> {
            changeLevel();
            binding.refreshLayout.setRefreshing(false);
        });

        adapter = new ListReportAdapter();
        binding.tvEmptyData.setText(getString(R.string.no_data_available_report));
        adapter.registerAdapterDataObserver(new RecyclerViewEmptyData(binding.rvListReport, binding.tvEmptyData));

        binding.rvListReport.setHasFixedSize(true);
        binding.rvListReport.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvListReport.setAdapter(adapter);

        return view;
    }

    private void changeLevel() {
        databaseReferenceLevels.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: levelsSuccessfully " + databaseReferenceLevels.getKey());
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Levels levels = dataSnapshot.getValue(Levels.class);
                        if (levels != null) {
                            if (levels.getAuthId().equals(authId)) {
                                if (levels.getLevelsItem().getLevel().equals(getString(R.string.admin))
                                        || levels.getLevelsItem().getLevel().equals(getString(R.string.principal))
                                        || levels.getLevelsItem().getLevel().equals(getString(R.string.team_leader))
                                        || levels.getLevelsItem().getLevel().equals(getString(R.string.vice_principal))) {
                                    listAllReportRealtime();
                                } else if (levels.getLevelsItem().getLevel().equals(getString(R.string.teacher))) {
                                    listReportRealtime();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled: failureLevels", error.toException());
                Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void listReportRealtime() {
        reports.clear();
        progressDialog.ShowProgressDialog();
        databaseReferenceReport.orderByChild("reportItem/report").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "onDataChange: reportSuccessfully " + databaseReferenceReport.getKey());
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Report report = dataSnapshot.getValue(Report.class);
                        if (report != null && report.getAuthId().equals(authId)) {
                            reports.add(report);
                            adapter.setListReport(reports);
                        }
                        adapter.setOnItemClickCallback(showReport -> showSelectedReport(showReport));
                        adapter.setOnItemClickCallbackDelete(deleteReport -> deleteSelectedReport(deleteReport));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "onCancelled: reportFailure ", error.toException());
                Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listAllReportRealtime() {
        reports.clear();
        progressDialog.ShowProgressDialog();
        databaseReferenceReport.orderByChild("reportItem/report").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "onDataChange: reportSuccessfully " + databaseReferenceReport.getKey());
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Report report = dataSnapshot.getValue(Report.class);
                        if (report != null) {
                            reports.add(report);
                            adapter.setListReport(reports);
                        }
                        adapter.setOnItemClickCallback(showReport -> showSelectedReport(showReport));
                        adapter.setOnItemClickCallbackDelete(deleteReport -> deleteSelectedReport(deleteReport));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "onCancelled: reportFailure ", error.toException());
                Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSelectedReport(Report report) {
        Bundle bundle = new Bundle();

        bundle.putParcelable(EXTRA_REPORT, report);
        Navigation.findNavController(view).navigate(R.id.action_nav_list_report_to_edit_report, bundle);
    }

    private void deleteSelectedReport(Report report) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.delete)).setMessage(getString(R.string.f_delete_report, report.getReportItem().getReport())).setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel())
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> deleteReport(report));
        builder.show();
    }

    private void deleteReport(Report report) {
        progressDialog.ShowProgressDialog();
        databaseReferenceReport.child(report.getReportItem().getReportId()).removeValue().addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "deleteReport: successfully " + report.getReportItem().getReportId());
            deleteStorageReport(report);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "deleteReport: ", e);
        });
    }

    private void deleteStorageReport(Report report) {
        progressDialog.ShowProgressDialog();
        storageReferenceReport.child(report.getAuthId() + "/report/" + report.getPlacementId() + "/" + report.getReportItem().getReport()).delete().addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "deleteStorageReport: successfully " + storageReferenceReport.getPath());
            changeLevel();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "deleteStorageReport: ", e);
        });
    }

    @Override
    public void onStart() {
        changeLevel();
        super.onStart();
    }
}