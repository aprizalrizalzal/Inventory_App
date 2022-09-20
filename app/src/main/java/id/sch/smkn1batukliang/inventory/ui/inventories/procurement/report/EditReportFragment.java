package id.sch.smkn1batukliang.inventory.ui.inventories.procurement.report;

import static id.sch.smkn1batukliang.inventory.ui.inventories.procurement.report.ListReportFragment.EXTRA_REPORT;

import android.annotation.SuppressLint;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.addition.utilities.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.FragmentEditReportBinding;
import id.sch.smkn1batukliang.inventory.model.inventories.procurement.report.Report;
import id.sch.smkn1batukliang.inventory.model.inventories.procurement.report.ReportItem;
import id.sch.smkn1batukliang.inventory.model.inventories.procurement.report.response.Principal;
import id.sch.smkn1batukliang.inventory.model.inventories.procurement.report.response.TeamLeader;
import id.sch.smkn1batukliang.inventory.model.inventories.procurement.report.response.VicePrincipal;
import id.sch.smkn1batukliang.inventory.model.users.levels.Levels;

public class EditReportFragment extends Fragment {

    private static final String TAG = "EditReportFragment";
    private FragmentEditReportBinding binding;
    private View view;
    private CustomProgressDialog progressDialog;
    private Report extraReport;
    private DatabaseReference databaseReferenceReport;
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

        if (extraReport != null) {
            databaseReferenceLevels.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "onDataChange: levelsSuccessfully " + databaseReferenceLevels.getKey());
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Levels levels = dataSnapshot.getValue(Levels.class);
                        if (levels != null && authId.equals(levels.getAuthId())) {
                            if (levels.getLevelsItem().getLevel().equals(getString(R.string.admin))) {
                                if (!extraReport.getReportItem().getVicePrincipal().isKnown()
                                        && !extraReport.getReportItem().getTeamLeader().isApproved()
                                        && !extraReport.getReportItem().getPrincipal().isApproved()) {
                                    binding.btnKnown.setVisibility(View.VISIBLE);
                                    binding.btnApproved.setVisibility(View.VISIBLE);
                                }
                            } else if (levels.getLevelsItem().getLevel().equals(getString(R.string.vice_principal))) {
                                if (!extraReport.getReportItem().getVicePrincipal().isKnown()
                                        && !extraReport.getReportItem().getTeamLeader().isApproved()
                                        && !extraReport.getReportItem().getPrincipal().isApproved()) {
                                    binding.btnKnown.setVisibility(View.VISIBLE);
                                }
                            } else if (levels.getLevelsItem().getLevel().equals(getString(R.string.team_leader))) {
                                if (extraReport.getReportItem().getVicePrincipal().isKnown()
                                        && !extraReport.getReportItem().getTeamLeader().isApproved()
                                        && !extraReport.getReportItem().getPrincipal().isApproved()) {
                                    binding.btnApproved.setVisibility(View.VISIBLE);
                                }
                            } else if (levels.getLevelsItem().getLevel().equals(getString(R.string.principal))) {
                                if (extraReport.getReportItem().getVicePrincipal().isKnown()
                                        && extraReport.getReportItem().getTeamLeader().isApproved()
                                        && !extraReport.getReportItem().getPrincipal().isApproved()) {
                                    binding.btnApproved.setVisibility(View.VISIBLE);
                                } else if (extraReport.getReportItem().getVicePrincipal().isKnown()
                                        && extraReport.getReportItem().getTeamLeader().isApproved()
                                        && !extraReport.getReportItem().getPrincipal().isApproved()) {
                                    binding.btnApproved.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (extraReport.getReportItem().getVicePrincipal().isKnown()
                                        && extraReport.getReportItem().getTeamLeader().isApproved()
                                        && extraReport.getReportItem().getPrincipal().isApproved()
                                        && !extraReport.getReportItem().isReceived()) {
                                    binding.btnReceived.setVisibility(View.VISIBLE);
                                }
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

        binding.btnKnown.setOnClickListener(v -> knownProcurement());
        binding.btnApproved.setOnClickListener(v -> procurementApproved());
        binding.btnReceived.setOnClickListener(v -> procurementReceived());

        return view;
    }

    private void knownProcurement() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.verification))
                .setMessage(getString(R.string.f_known_procurement, extraReport.getReportItem().getReport()))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.reject), (dialog, id) -> alertRejected())
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> knownReport())
                .setNeutralButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel());
        builder.show();
    }

    private void knownReport() {
        progressDialog.ShowProgressDialog();
        Principal principal = new Principal(false, "");
        TeamLeader teamLeader = new TeamLeader(false, "");
        VicePrincipal vicePrincipal = new VicePrincipal(getString(R.string.known), true);

        ReportItem reportItem = new ReportItem(extraReport.getReportItem().getPdfLink(), principal, extraReport.getReportItem().getPurpose(), extraReport.getReportItem().getReport(), extraReport.getReportItem().getReportId(), teamLeader, extraReport.getReportItem().getTimestamp(), extraReport.getReportItem().isReceived(), vicePrincipal);
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

    private void procurementApproved() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.verification))
                .setMessage(getString(R.string.f_procurement_approved, extraReport.getReportItem().getReport()))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.reject), (dialog, id) -> alertRejected())
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> reportApproved(extraReport.getReportItem()))
                .setNeutralButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel());
        builder.show();
    }

    private void reportApproved(ReportItem _reportItem) {
        progressDialog.ShowProgressDialog();
        Principal principal = null;
        TeamLeader teamLeader = null;
        VicePrincipal vicePrincipal = new VicePrincipal(extraReport.getReportItem().getVicePrincipal().getDescription(), true);
        if (!_reportItem.getTeamLeader().isApproved() && !_reportItem.getPrincipal().isApproved()) {
            teamLeader = new TeamLeader(true, getString(R.string.approved));
            principal = new Principal(extraReport.getReportItem().getPrincipal().isApproved(), extraReport.getReportItem().getTeamLeader().getDescription());
        } else if (_reportItem.getTeamLeader().isApproved() && !_reportItem.getPrincipal().isApproved()) {
            teamLeader = new TeamLeader(extraReport.getReportItem().getTeamLeader().isApproved(), extraReport.getReportItem().getTeamLeader().getDescription());
            principal = new Principal(true, getString(R.string.approved));
        }


        ReportItem reportItem = new ReportItem(extraReport.getReportItem().getPdfLink(), principal, extraReport.getReportItem().getPurpose(), extraReport.getReportItem().getReport(), extraReport.getReportItem().getReportId(), teamLeader, extraReport.getReportItem().getTimestamp(), extraReport.getReportItem().isReceived(), vicePrincipal);
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

    @SuppressLint("InflateParams")
    private void alertRejected() {
        View customView;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        customView = LayoutInflater.from(requireContext()).inflate(R.layout.report_reject, null, false);
        builder.setTitle(getString(R.string.reject))
                .setMessage(getString(R.string.f_report_rejected))
                .setView(customView)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                    TextInputEditText _description = customView.findViewById(R.id.tiet_description);
                    String description = Objects.requireNonNull(_description.getText()).toString();
                    reportRejected(description);
                });
        builder.show();
    }

    private void reportRejected(String description) {
        progressDialog.ShowProgressDialog();
        Principal principal = new Principal(false, description);
        TeamLeader teamLeader = new TeamLeader(false, description);
        VicePrincipal vicePrincipal = new VicePrincipal(description, false);

        ReportItem reportItem = new ReportItem(extraReport.getReportItem().getPdfLink(), principal, extraReport.getReportItem().getPurpose(), extraReport.getReportItem().getReport(), extraReport.getReportItem().getReportId(), teamLeader, extraReport.getReportItem().getTimestamp(), extraReport.getReportItem().isReceived(), vicePrincipal);
        Report model = new Report(extraReport.getAuthId(), extraReport.getPlacementId(), reportItem);
        databaseReferenceReport.child(extraReport.getReportItem().getReportId()).setValue(model).addOnSuccessListener(command -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "rejectedReport: successfully " + extraReport.getReportItem().getReportId());
            Navigation.findNavController(view).navigateUp();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "rejectedReport: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void procurementReceived() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.verification))
                .setMessage(getString(R.string.f_procurement_received, extraReport.getReportItem().getReport()))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel())
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> reportReceived());
        builder.show();
    }

    private void reportReceived() {
        progressDialog.ShowProgressDialog();
        Principal principal = new Principal(extraReport.getReportItem().getPrincipal().isApproved(), extraReport.getReportItem().getPrincipal().getDescription());
        TeamLeader teamLeader = new TeamLeader(extraReport.getReportItem().getTeamLeader().isApproved(), extraReport.getReportItem().getTeamLeader().getDescription());
        VicePrincipal vicePrincipal = new VicePrincipal(extraReport.getReportItem().getVicePrincipal().getDescription(), extraReport.getReportItem().getVicePrincipal().isKnown());

        ReportItem reportItem = new ReportItem(extraReport.getReportItem().getPdfLink(), principal, extraReport.getReportItem().getPurpose(), extraReport.getReportItem().getReport(), extraReport.getReportItem().getReportId(), teamLeader, extraReport.getReportItem().getTimestamp(), true, vicePrincipal);
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