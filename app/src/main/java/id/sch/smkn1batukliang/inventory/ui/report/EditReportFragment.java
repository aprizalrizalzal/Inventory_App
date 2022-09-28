package id.sch.smkn1batukliang.inventory.ui.report;

import static id.sch.smkn1batukliang.inventory.utili.InventoryMessagingService.NOTIFICATION_URL;
import static id.sch.smkn1batukliang.inventory.utili.InventoryMessagingService.SERVER_KEY;
import static id.sch.smkn1batukliang.inventory.ui.report.ListReportFragment.EXTRA_REPORT;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.utili.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.FragmentEditReportBinding;
import id.sch.smkn1batukliang.inventory.model.report.Report;
import id.sch.smkn1batukliang.inventory.model.Users;
import id.sch.smkn1batukliang.inventory.model.levels.Levels;

public class EditReportFragment extends Fragment {

    private static final String TAG = "EditReportFragment";
    private FragmentEditReportBinding binding;
    private View view;
    private CustomProgressDialog progressDialog;
    private Report extraReport;
    private CollectionReference collectionReferenceUsers;
    private DatabaseReference databaseReferenceReport;
    private PDFView pdfView;
    private String extraReportId;
    private Map<String, Object> mapReport;
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

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_main_nav, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_download_report) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReferenceReport = storage.getReference("users/procurement/");

                    String pathReport = extraReport.getAuthId() + "/report/" + extraReport.getPlacementId() + "/" + extraReport.getReportItem().getReport();

                    progressDialog.ShowProgressDialog();
                    storageReferenceReport.child(pathReport).getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d(TAG, "storageReferenceReport: Successfully");
                        progressDialog.DismissProgressDialog();
                        String url = uri.toString();
                        downloadFiles(url);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "storageReferenceReport: failure", e);
                        progressDialog.DismissProgressDialog();
                    });
                }

                return false;
            }

            private void downloadFiles(String url) {

                DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(url);
                DownloadManager.Request request = new DownloadManager.Request(uri);

                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, extraReport.getReportItem().getReport());

                downloadManager.enqueue(request);

            }

        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            authId = user.getUid();
        }

        if (getArguments() != null) {
            extraReport = getArguments().getParcelable(EXTRA_REPORT);
        }

        if (extraReport != null) {
            extraReportId = extraReport.getReportItem().getReportId();
            mapReport = new HashMap<>();
        }

        progressDialog = new CustomProgressDialog(requireActivity());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        collectionReferenceUsers = firestore.collection("users");

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
                                if (!extraReport.getReportItem().getVicePrincipal().isApproved()
                                        && !extraReport.getReportItem().getTeamLeader().isApproved()
                                        && !extraReport.getReportItem().getPrincipal().isApproved()) {
                                    binding.btnApprovedVicePrincipal.setVisibility(View.VISIBLE);
                                    binding.btnApprovedTeamLeader.setVisibility(View.VISIBLE);
                                    binding.btnApprovedPrincipal.setVisibility(View.VISIBLE);
                                }
                            } else if (levels.getLevelsItem().getLevel().equals(getString(R.string.vice_principal))) {
                                if (!extraReport.getReportItem().getVicePrincipal().isApproved()
                                        && !extraReport.getReportItem().getTeamLeader().isApproved()
                                        && !extraReport.getReportItem().getPrincipal().isApproved()) {
                                    binding.btnApprovedVicePrincipal.setVisibility(View.VISIBLE);
                                }
                            } else if (levels.getLevelsItem().getLevel().equals(getString(R.string.team_leader))) {
                                if (extraReport.getReportItem().getVicePrincipal().isApproved()
                                        && !extraReport.getReportItem().getTeamLeader().isApproved()
                                        && !extraReport.getReportItem().getPrincipal().isApproved()) {
                                    binding.btnApprovedTeamLeader.setVisibility(View.VISIBLE);
                                }
                            } else if (levels.getLevelsItem().getLevel().equals(getString(R.string.principal))) {
                                if (extraReport.getReportItem().getVicePrincipal().isApproved()
                                        && extraReport.getReportItem().getTeamLeader().isApproved()
                                        && !extraReport.getReportItem().getPrincipal().isApproved()) {
                                    binding.btnApprovedPrincipal.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (extraReport.getReportItem().getVicePrincipal().isApproved()
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
                        progressDialog.DismissProgressDialog();
                        Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
                    })
                    .load());
        });

        binding.btnApprovedVicePrincipal.setOnClickListener(v -> procurementApprovedVicePrincipal());
        binding.btnApprovedTeamLeader.setOnClickListener(v -> procurementApprovedTeamLeader());
        binding.btnApprovedPrincipal.setOnClickListener(v -> procurementApprovedPrincipal());
        binding.btnReceived.setOnClickListener(v -> procurementReceived());

        return view;
    }

    private void procurementApprovedVicePrincipal() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.verification))
                .setMessage(getString(R.string.f_procurement_approved, extraReport.getReportItem().getReport()))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.reject), (dialog, id) -> alertRejected())
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> reportApprovedVicePrincipal())
                .setNeutralButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel());
        builder.show();
    }

    private void reportApprovedVicePrincipal() {
        progressDialog.ShowProgressDialog();

        mapReport.put("/reportItem/vicePrincipal/approved", true);
        mapReport.put("/reportItem/vicePrincipal/description", getString(R.string.approved));

        databaseReferenceReport.child(extraReportId).updateChildren(mapReport).addOnSuccessListener(command -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "agreeReport: successfully " + extraReport.getReportItem().getReportId());
            Navigation.findNavController(view).navigateUp();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "agreeReport: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void procurementApprovedTeamLeader() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.verification))
                .setMessage(getString(R.string.f_procurement_approved, extraReport.getReportItem().getReport()))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.reject), (dialog, id) -> alertRejected())
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> reportApprovedTeamLeader())
                .setNeutralButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel());
        builder.show();
    }

    private void reportApprovedTeamLeader() {
        progressDialog.ShowProgressDialog();

        mapReport.put("/reportItem/teamLeader/approved", true);
        mapReport.put("/reportItem/teamLeader/description", getString(R.string.approved));

        databaseReferenceReport.child(extraReportId).updateChildren(mapReport).addOnSuccessListener(command -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "agreeReport: successfully " + extraReport.getReportItem().getReportId());
            Navigation.findNavController(view).navigateUp();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "agreeReport: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void procurementApprovedPrincipal() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.verification))
                .setMessage(getString(R.string.f_procurement_approved, extraReport.getReportItem().getReport()))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.reject), (dialog, id) -> alertRejected())
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> reportApprovedPrincipal())
                .setNeutralButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel());
        builder.show();
    }

    private void reportApprovedPrincipal() {
        progressDialog.ShowProgressDialog();

        mapReport.put("/reportItem/principal/approved", true);
        mapReport.put("/reportItem/principal/description", getString(R.string.approved));

        databaseReferenceReport.child(extraReportId).updateChildren(mapReport).addOnSuccessListener(command -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "agreeReport: successfully " + extraReport.getReportItem().getReportId());
            getTokenForNotificationApproved(extraReport);
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

        mapReport.put("/reportItem/vicePrincipal/approved", false);
        mapReport.put("/reportItem/vicePrincipal/description", description);

        mapReport.put("/reportItem/teamLeader/approved", false);
        mapReport.put("/reportItem/teamLeader/description", description);

        mapReport.put("/reportItem/principal/approved", false);
        mapReport.put("/reportItem/principal/description", description);

        databaseReferenceReport.child(extraReportId).updateChildren(mapReport).addOnSuccessListener(command -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "rejectedReport: successfully " + extraReport.getReportItem().getReportId());
            getTokenForNotificationRejected(extraReport);
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

        mapReport.put("/reportItem/received", true);

        databaseReferenceReport.child(extraReportId).updateChildren(mapReport).addOnSuccessListener(command -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "agreeReport: successfully " + extraReport.getReportItem().getReportId());
            Navigation.findNavController(view).navigateUp();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "agreeReport: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void getTokenForNotificationApproved(Report report) {
        collectionReferenceUsers.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "getTokenForNotification: successfully " + collectionReferenceUsers.getId());
                for (DocumentSnapshot snapshot : task.getResult()) {
                    Users users = snapshot.toObject(Users.class);
                    String tokenId = snapshot.getString("tokenId");
                    if (users != null && users.getAuthId().equals(report.getAuthId())) {
                        Log.d(TAG, "getTokenForNotification: teamLeader" + tokenId);
                        sendDataReportApproved(report, tokenId);
                    }
                }
            } else {
                Log.w(TAG, "getTokenForNotification: failure ", task.getException());
                Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendDataReportApproved(Report report, String tokenId) {
        JSONObject to = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put("title", getString(R.string.approved));
            data.put("message", report.getReportItem().getReport());

            to.put("to", tokenId);
            to.put("data", data);

            sendNotification(to);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getTokenForNotificationRejected(Report report) {
        collectionReferenceUsers.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "getTokenForNotification: successfully " + collectionReferenceUsers.getId());
                for (DocumentSnapshot snapshot : task.getResult()) {
                    Users users = snapshot.toObject(Users.class);
                    String tokenId = snapshot.getString("tokenId");
                    if (users != null && users.getAuthId().equals(report.getAuthId())) {
                        Log.d(TAG, "getTokenForNotification: teamLeader" + tokenId);
                        sendDataReportRejected(report, tokenId);
                    }
                }
            } else {
                Log.w(TAG, "getTokenForNotification: failure ", task.getException());
                Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendDataReportRejected(Report report, String tokenId) {
        JSONObject to = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put("title", getString(R.string.rejected));
            data.put("message", report.getReportItem().getReport());

            to.put("to", tokenId);
            to.put("data", data);

            sendNotification(to);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(JSONObject to) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                NOTIFICATION_URL, to,
                response -> Log.d(TAG, "sendNotification: " + response),
                error -> Log.e(TAG, "sendNotification: ", error)) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> map = new HashMap<>();
                map.put("Authorization", "key=" + SERVER_KEY);
                map.put("Content-type", "application/json");
                return map;
            }

            @Override
            public String getBodyContentType() {

                return "application/json";
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );
        requestQueue.add(request);
        Navigation.findNavController(view).navigateUp();
    }


}