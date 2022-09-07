package id.sch.smkn1batukliang.inventory.ui.inventories.report;

import static id.sch.smkn1batukliang.inventory.ui.inventories.report.ListReportFragment.EXTRA_REPORT;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.FragmentEditReportBinding;
import id.sch.smkn1batukliang.inventory.model.inventories.report.Report;
import id.sch.smkn1batukliang.inventory.model.inventories.report.ReportItem;
import id.sch.smkn1batukliang.inventory.model.users.Users;

public class EditReportFragment extends Fragment {

    private static final String TAG = "EditReportFragment";
    private FragmentEditReportBinding binding;
    private View view;
    private CustomProgressDialog progressDialog;
    private Report extraReport;
    private DatabaseReference databaseReferenceReport;
    private StorageReference storageReferenceReport;
    private PDFView pdfView;
    private String authId, pdfLink;

    public EditReportFragment() {
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
        binding = FragmentEditReportBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            authId = user.getUid();
        }

        if (getArguments() != null) {
            extraReport = getArguments().getParcelable(EXTRA_REPORT);
        }

        progressDialog = new CustomProgressDialog(requireActivity());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReferenceLevels = database.getReference("levels");
        databaseReferenceReport = database.getReference("report");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReferenceReport = storage.getReference("users/procurement/");

        if (extraReport != null) {
            databaseReferenceLevels.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "onDataChange: levelsSuccessfully " + databaseReferenceLevels.getKey());
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users users = dataSnapshot.child("users").getValue(Users.class);
                        if (users != null && authId.equals(users.getAuthId())) {
                            if (users.getLevel().equals(getString(R.string.admin))
                                    || users.getLevel().equals(getString(R.string.team_leader))
                                    || users.getLevel().equals(getString(R.string.vice_principal))
                                    || users.getLevel().equals(getString(R.string.principal))) {
                                binding.btnReject.setVisibility(View.VISIBLE);
                                binding.btnAgree.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "onCancelled: levelsFailure ", error.toException());
                    Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
                }
            });

            pdfLink = extraReport.getReportItem().getPdfLink();
        }

        pdfView = binding.pdfView;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        progressDialog.ShowProgressDialog();
        executor.execute(() -> {
            InputStream inputStream = null;
            try {
                URL url = new URL(pdfLink);
                HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.w(TAG, "onCreateView: executeFailure ", e);
                Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }

            InputStream finalInputStream = inputStream;
            handler.post(() -> pdfView.fromStream(finalInputStream)
                    .scrollHandle(new DefaultScrollHandle(requireContext()))
                    .onLoad(loadPages -> {
                        Log.d(TAG, "onCreateView: pdfSuccessfully " + finalInputStream);
                        progressDialog.DismissProgressDialog();
                    })
                    .onError(e -> {
                        Log.w(TAG, "onCreateView: pdfFailure ", e);
                        Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
                    })
                    .load());
        });

        binding.btnReject.setOnClickListener(v -> rejectProcurement());

        binding.btnAgree.setOnClickListener(v -> agreeProcurement());

        return view;
    }

    private void rejectProcurement() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.reject))
                .setMessage(getString(R.string.f_reject_report, extraReport.getReportItem().getReport()))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel())
                .setPositiveButton(getString(R.string.reject), (dialog, id) -> rejectReport())
                .setNeutralButton(getString(R.string.delete), (dialog, id) -> deleteReport(extraReport.getReportItem()));
        builder.show();
    }

    private void rejectReport() {
        progressDialog.ShowProgressDialog();
        ReportItem reportItem = new ReportItem(extraReport.getReportItem().getPdfLink(), extraReport.getReportItem().getPurpose(), extraReport.getReportItem().getReport(), extraReport.getReportItem().getReportId(), false, extraReport.getReportItem().getTimestamp());
        Report model = new Report(extraReport.getAuthId(), extraReport.getPlacementId(), reportItem);
        databaseReferenceReport.child(extraReport.getReportItem().getReportId()).setValue(model).addOnSuccessListener(command -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "rejectReport: successfully " + extraReport.getReportItem().getReportId());
            Navigation.findNavController(view).navigateUp();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "rejectReport: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteReport(ReportItem reportItem) {
        progressDialog.ShowProgressDialog();
        databaseReferenceReport.child(reportItem.getReportId()).removeValue().addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "deleteReport: successfully " + reportItem.getReportId());
            deleteStorageReport(reportItem);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "deleteReport: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteStorageReport(ReportItem reportItem) {
        progressDialog.ShowProgressDialog();
        String pathReport = "users/procurement/" + extraReport.getAuthId() + "/report/" + extraReport.getPlacementId() + "/" + reportItem.getReport();
        storageReferenceReport.child(pathReport).delete().addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "deleteStorageReport: successfully " + pathReport);
            Navigation.findNavController(view).navigateUp();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "deleteStorageReport: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void agreeProcurement() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.agree))
                .setMessage(getString(R.string.f_agree_report, extraReport.getReportItem().getReport()))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel())
                .setPositiveButton(getString(R.string.agree), (dialog, id) -> agreeReport());
        builder.show();
    }

    private void agreeReport() {
        progressDialog.ShowProgressDialog();
        ReportItem reportItem = new ReportItem(extraReport.getReportItem().getPdfLink(), extraReport.getReportItem().getPurpose(), extraReport.getReportItem().getReport(), extraReport.getReportItem().getReportId(), true, extraReport.getReportItem().getTimestamp());
        Report model = new Report(extraReport.getAuthId(), extraReport.getPlacementId(), reportItem);
        databaseReferenceReport.child(extraReport.getReportItem().getReportId()).setValue(model).addOnSuccessListener(command -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "agreeReport: successfully " + extraReport.getReportItem().getReportId());
            Navigation.findNavController(view).navigateUp();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "agreeReport: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }
}