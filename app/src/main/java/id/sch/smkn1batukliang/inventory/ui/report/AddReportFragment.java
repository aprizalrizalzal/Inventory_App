package id.sch.smkn1batukliang.inventory.ui.report;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static id.sch.smkn1batukliang.inventory.ui.placement.GridPlacementFragment.EXTRA_PLACEMENT_FOR_PROCUREMENT;
import static id.sch.smkn1batukliang.inventory.utili.InventoryMessagingService.NOTIFICATION_URL;
import static id.sch.smkn1batukliang.inventory.utili.InventoryMessagingService.SERVER_KEY;

import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.FragmentAddReportBinding;
import id.sch.smkn1batukliang.inventory.model.placement.Placement;
import id.sch.smkn1batukliang.inventory.model.procurement.Procurement;
import id.sch.smkn1batukliang.inventory.model.report.Report;
import id.sch.smkn1batukliang.inventory.model.report.ReportItem;
import id.sch.smkn1batukliang.inventory.model.report.item.Principal;
import id.sch.smkn1batukliang.inventory.model.report.item.TeamLeader;
import id.sch.smkn1batukliang.inventory.model.report.item.VicePrincipal;
import id.sch.smkn1batukliang.inventory.model.users.Users;
import id.sch.smkn1batukliang.inventory.utili.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.utili.MoneyTextWatcher;

public class AddReportFragment extends Fragment {

    private static final String TAG = "AddReportFragment";
    private final Calendar calendar = Calendar.getInstance();
    private final ArrayList<Procurement> procurements = new ArrayList<>();
    private final ArrayList<Users> listUser = new ArrayList<>();
    private ArrayAdapter<Users> stringListUserAdapter;
    boolean isEmptyFields = false;
    private FragmentAddReportBinding binding;
    private SimpleDateFormat simpleDateFormatId;
    private CustomProgressDialog progressDialog;
    private DatabaseReference databaseReferenceUsers, databaseReferenceProcurement, databaseReferenceReport;
    private StorageReference storageReferenceReport;
    private double total, totalAmount = 0.0;
    private Placement extraPlacementForReport;
    private String placementId, placement;
    private String authId, purpose, dateProcurement, teamLeader, einTeamLeader, vicePrincipal, einVicePrincipal, principal, einPrincipal;
    private String report;
    private File path;

    private final ActivityResultLauncher<String> resultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (result) {
            try {
                purposeProcurement();
            } catch (IOException | DocumentException e) {
                Log.w(TAG, "createPurposeProcurement: failure ", e);
                Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(requireContext(), getString(R.string.download_report), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), getString(R.string.download_report_denied), Toast.LENGTH_SHORT).show();
        }
    });

    public AddReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddReportBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

        progressDialog = new CustomProgressDialog(getActivity());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            authId = user.getUid();
        }

        if (getArguments() != null) {
            extraPlacementForReport = getArguments().getParcelable(EXTRA_PLACEMENT_FOR_PROCUREMENT);
        }

        if (extraPlacementForReport != null) {
            placementId = extraPlacementForReport.getPlacementItem().getPlacementId();
            placement = extraPlacementForReport.getPlacementItem().getPlacement();
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUsers = database.getReference("users");
        databaseReferenceProcurement = database.getReference("procurement");
        databaseReferenceReport = database.getReference("report");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReferenceReport = storage.getReference("users/procurement/");

        stringListUserAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_mactv, listUser);

        automaticTextInputEditText();
        selectManualTextInputEditText();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                updateTietDateProcurement();
            }

            private void updateTietDateProcurement() {
                simpleDateFormatId = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
                binding.tietDateProcurement.setText(simpleDateFormatId.format(calendar.getTime()));
            }
        };

        binding.tietDateProcurement.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                new DatePickerDialog(requireContext(), date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        binding.btnDownload.setOnClickListener(v -> {
            purpose = Objects.requireNonNull(binding.tietProcurementPurpose.getText()).toString();
            teamLeader = binding.mactvTeamLeader.getText().toString();
            dateProcurement = Objects.requireNonNull(binding.tietDateProcurement.getText()).toString();
            vicePrincipal = binding.mactvVicePrincipal.getText().toString();
            principal = binding.mactvPrincipal.getText().toString();

            isEmptyFields = validateFields();

        });

        return view;
    }

    private void automaticTextInputEditText() {
        databaseReferenceUsers.orderByChild("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: Users");
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users users = dataSnapshot.getValue(Users.class);
                        if (users != null) {
                            if (users.getLevel() != null && users.getLevel().equals(getString(R.string.team_leader))) {
                                binding.mactvTeamLeader.setText(users.getUsername());
                                einTeamLeader = users.getEmployeeIdNumber();
                            }
                            if (users.getLevel() != null && users.getLevel().equals(getString(R.string.vice_principal))) {
                                binding.mactvVicePrincipal.setText(users.getUsername());
                                einVicePrincipal = users.getEmployeeIdNumber();
                            }
                            if (users.getLevel() != null && users.getLevel().equals(getString(R.string.principal))) {
                                binding.mactvPrincipal.setText(users.getUsername());
                                einPrincipal = users.getEmployeeIdNumber();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled: Users", error.toException());
            }
        });
    }

    private void selectManualTextInputEditText() {
        listUser.clear();
        databaseReferenceUsers.orderByChild("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: Users");
                if (snapshot.exists()) {
                    listUser.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users users = dataSnapshot.getValue(Users.class);
                        if (users != null) {
                            listUser.add(users);
                            stringListUserAdapter = new ArrayAdapter<>(requireContext(), R.layout.list_mactv, listUser);
                            binding.mactvTeamLeader.setAdapter(stringListUserAdapter);
                            binding.mactvVicePrincipal.setAdapter(stringListUserAdapter);
                            binding.mactvPrincipal.setAdapter(stringListUserAdapter);
                        }
                        binding.mactvTeamLeader.setOnItemClickListener((parent, view, position, id) -> einTeamLeader = listUser.get(position).getEmployeeIdNumber());
                        binding.mactvVicePrincipal.setOnItemClickListener((parent, view, position, id) -> einVicePrincipal = listUser.get(position).getEmployeeIdNumber());
                        binding.mactvPrincipal.setOnItemClickListener((parent, view, position, id) -> einPrincipal = listUser.get(position).getEmployeeIdNumber());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled: Users", error.toException());
            }
        });
    }

    private void listDatabaseProcurementReference() {
        procurements.clear();
        progressDialog.ShowProgressDialog();
        databaseReferenceProcurement.orderByChild("procurementItem/procurement").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Procurement model = dataSnapshot.getValue(Procurement.class);
                        if (model != null && authId.equals(model.getAuthId()) && placementId.equals(model.getPlacementId())) {
                            total = model.getProcurementItem().getAmount();
                            totalAmount = totalAmount + total;

                            procurements.add(model);
                        }
                    }
                }
                progressDialog.DismissProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.DismissProgressDialog();
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateFields() {
        if (purpose.isEmpty()) {
            binding.tilProcurementPurpose.setError(getString(R.string.procurement_purpose_required));
            return false;
        } else {
            binding.tilProcurementPurpose.setErrorEnabled(false);
        }

        if (teamLeader.isEmpty()) {
            binding.tilTeamLeader.setError(getString(R.string.team_leader_required));
            return false;
        } else {
            binding.tilTeamLeader.setErrorEnabled(false);
        }

        if (dateProcurement.isEmpty()) {
            binding.tilDateProcurement.setError(getString(R.string.date_procurement_required));
            return false;
        } else {
            binding.tilDateProcurement.setErrorEnabled(false);
        }

        if (vicePrincipal.isEmpty()) {
            binding.tilVicePrincipal.setError(getString(R.string.vice_principal_required));
            return false;
        } else {
            binding.tilVicePrincipal.setErrorEnabled(false);
        }

        if (principal.isEmpty()) {
            binding.tilPrincipal.setError(getString(R.string.principal_required));
            return false;
        } else {
            binding.tilPrincipal.setErrorEnabled(false);
        }

        if (einTeamLeader == null) {
            binding.tilTeamLeader.setError(getString(R.string.select_listed_ein_team_leader));
            return false;
        } else {
            binding.tilTeamLeader.setErrorEnabled(false);
        }

        if (einVicePrincipal == null) {
            binding.tilVicePrincipal.setError(getString(R.string.select_listed_ein_vice_principal));
            return false;
        } else {
            binding.tilVicePrincipal.setErrorEnabled(false);
        }

        if (einPrincipal == null) {
            binding.tilPrincipal.setError(getString(R.string.select_listed_ein_principal));
            return false;
        } else {
            binding.tilPrincipal.setErrorEnabled(false);
        }

        if (requireContext().checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            resultLauncher.launch(READ_EXTERNAL_STORAGE);
        } else if (requireContext().checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            resultLauncher.launch(WRITE_EXTERNAL_STORAGE);
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        }

        return true;

    }

    private void purposeProcurement() throws IOException, DocumentException {

        String pathDownload = Environment.getExternalStorageDirectory().getPath() + "/Documents/";
        report = String.format("%s inventaris %s.pdf", dateProcurement.toLowerCase(), placement.toLowerCase());
        path = new File(pathDownload);

        boolean directoryFile = path.exists();

        if (!directoryFile) {
            directoryFile = path.mkdirs();
        }

        if (directoryFile) {
            path = new File(pathDownload, report);
        }

        FileOutputStream fileOutputStream = new FileOutputStream(path);

        Document document = new Document();
        document.setPageSize(PageSize.A4);

        document.addAuthor("Program Studi S1 Sistem Informasi");
        document.addCreator("Muhamad Aprizal Hardi");

        document.addCreationDate();

        SimpleDateFormat simpleYearFormatId = new SimpleDateFormat("yyyy", new Locale("id", "ID"));
        String yearProcurement = simpleYearFormatId.format(calendar.getTime());

        Font fontNormal = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.BLACK);
        Font fontBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLACK);

        Chunk title = new Chunk("DAFTAR USULAN\n" + purpose.toUpperCase() + " TAHUN " + yearProcurement + "\nBIDANG SARANA DAN PRASARANA", fontBold);
        Paragraph paragraphTitle = new Paragraph(title);
        paragraphTitle.setAlignment(Element.ALIGN_CENTER);

        PdfPTable tableHeader = new PdfPTable(7);
        tableHeader.setWidthPercentage(100);
        tableHeader.setWidths(new float[]{1, 5, 2, 2, 3, 4, 2});

        PdfPCell cellHeader;
        cellHeader = new PdfPCell(new Phrase("No", fontBold));
        cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellHeader.setRowspan(2);
        tableHeader.addCell(cellHeader);

        cellHeader = new PdfPCell(new Phrase("Uraian Usulan", fontBold));
        cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellHeader.setRowspan(2);
        tableHeader.addCell(cellHeader);

        cellHeader = new PdfPCell(new Phrase("Rincian Perhitungan", fontBold));
        cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellHeader.setColspan(3);
        tableHeader.addCell(cellHeader);

        cellHeader = new PdfPCell(new Phrase("Jumlah", fontBold));
        cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellHeader.setRowspan(2);
        tableHeader.addCell(cellHeader);

        cellHeader = new PdfPCell(new Phrase("Ket.", fontBold));
        cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellHeader.setRowspan(2);
        tableHeader.addCell(cellHeader);

        cellHeader = new PdfPCell(new Phrase("Volume", fontBold));
        cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellHeader.setRowspan(2);
        tableHeader.addCell(cellHeader);

        cellHeader = new PdfPCell(new Phrase("Satuan", fontBold));
        cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellHeader.setRowspan(2);
        tableHeader.addCell(cellHeader);

        cellHeader = new PdfPCell(new Phrase("Harga", fontBold));
        cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellHeader.setRowspan(2);
        tableHeader.addCell(cellHeader);

        PdfPTable tableContent = new PdfPTable(7);
        tableContent.setWidthPercentage(100);
        tableContent.setWidths(new float[]{1, 5, 2, 2, 3, 4, 2});

        PdfPCell cellContent;
        for (int i = 0; i < procurements.size(); i++) {
            Procurement procurement = procurements.get(i);

            String no = String.valueOf(i + 1);
            String nameOfGoods = procurement.getProcurementItem().getProcurement();
            String volume = String.valueOf(procurement.getProcurementItem().getVolume());
            String unit = procurement.getProcurementItem().getUnit();
            String price = MoneyTextWatcher.formatCurrency(procurement.getProcurementItem().getPrice());
            String amount = MoneyTextWatcher.formatCurrency(procurement.getProcurementItem().getAmount());
            String description = procurement.getProcurementItem().getDescription();

            cellContent = new PdfPCell(new Phrase(no + ".", fontNormal));
            cellContent.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellContent.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableContent.addCell(cellContent);

            cellContent = new PdfPCell(new Phrase(nameOfGoods, fontNormal));
            cellContent.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellContent.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableContent.addCell(cellContent);

            cellContent = new PdfPCell(new Phrase(volume, fontNormal));
            cellContent.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellContent.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableContent.addCell(cellContent);

            cellContent = new PdfPCell(new Phrase(unit, fontNormal));
            cellContent.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellContent.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableContent.addCell(cellContent);

            cellContent = new PdfPCell(new Phrase(price, fontNormal));
            cellContent.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellContent.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableContent.addCell(cellContent);

            cellContent = new PdfPCell(new Phrase(amount, fontNormal));
            cellContent.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellContent.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableContent.addCell(cellContent);

            cellContent = new PdfPCell(new Phrase(description, fontNormal));
            cellContent.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellContent.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tableContent.addCell(cellContent);

        }

        PdfPTable tableTotal = new PdfPTable(3);
        tableTotal.setWidthPercentage(100);
        tableTotal.setWidths(new float[]{13, 4, 2});

        PdfPCell cellTotal;

        cellTotal = new PdfPCell(new Phrase("Total", fontBold));
        cellTotal.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableTotal.addCell(cellTotal);

        cellTotal = new PdfPCell(new Phrase(MoneyTextWatcher.formatCurrency(totalAmount), fontBold));
        cellTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableTotal.addCell(cellTotal);

        cellTotal = new PdfPCell(new Phrase("", fontBold));
        cellTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableTotal.addCell(cellTotal);

        PdfPTable tableBottom = new PdfPTable(3);
        tableBottom.setWidths(new float[]{6, 5, 6});
        tableBottom.setWidthPercentage(100);

        PdfPCell cellBottom;
        cellBottom = new PdfPCell(new Phrase("Mengetahui,\nKetua Tim Pelaksana BOS" + "\n\n\n\n\n\n"));
        cellBottom.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);
        cellBottom = new PdfPCell(new Phrase(""));
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);
        cellBottom = new PdfPCell(new Phrase("Batukliang," + dateProcurement + "\nWakasek Sarana dan Prasarana" + "\n\n\n\n\n\n"));
        cellBottom.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);

        cellBottom = new PdfPCell(new Phrase(teamLeader, fontBold));
        cellBottom.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);
        cellBottom = new PdfPCell(new Phrase(""));
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);
        cellBottom = new PdfPCell(new Phrase(vicePrincipal, fontBold));
        cellBottom.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);

        cellBottom = new PdfPCell(new Phrase("NIP. " + einTeamLeader));
        cellBottom.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);
        cellBottom = new PdfPCell(new Phrase(""));
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);
        cellBottom = new PdfPCell(new Phrase("NIP. " + einVicePrincipal));
        cellBottom.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);

        cellBottom = new PdfPCell(new Phrase(""));
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);
        cellBottom = new PdfPCell(new Phrase("\n\nMenyetujui,\nKepala Sekolah" + "\n\n\n\n\n\n"));
        cellBottom.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);
        cellBottom = new PdfPCell(new Phrase(""));
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);

        cellBottom = new PdfPCell(new Phrase(""));
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);
        cellBottom = new PdfPCell(new Phrase(principal, fontBold));
        cellBottom.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);
        cellBottom = new PdfPCell(new Phrase(""));
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);

        cellBottom = new PdfPCell(new Phrase(""));
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);
        cellBottom = new PdfPCell(new Phrase("NIP. " + einPrincipal));
        cellBottom.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);
        cellBottom = new PdfPCell(new Phrase(""));
        tableBottom.addCell(cellBottom).setBorder(Rectangle.NO_BORDER);

        PdfWriter.getInstance(document, fileOutputStream);

        if (getActivity() != null) {
            Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.raw.kop_smk_n_1_batukliang);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            Image image = Image.getInstance(byteArray);
            image.scaleAbsolute(524, 97);

            document.open();

            document.add(image);
            document.add(paragraphTitle);
            document.add(new Paragraph("\n"));
            document.add(tableHeader);
            document.add(tableContent);
            document.add(tableTotal);
            document.add(new Paragraph("\n"));
            document.add(tableBottom);

            document.close();

            downloadAndUploadPdf();

        }
    }

    private void downloadAndUploadPdf() {
        progressDialog.ShowProgressDialog();
        String pathReport = authId + "/report/" + placementId + "/" + report;
        storageReferenceReport.child(pathReport).putFile(Uri.fromFile(path)).addOnSuccessListener(taskSnapshot -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "downloadAndUploadPdf: successfully " + storageReferenceReport.getPath());
            downloadUriPdf(pathReport);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "downloadAndUploadPdf: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void downloadUriPdf(String pathReport) {
        progressDialog.ShowProgressDialog();
        storageReferenceReport.child(pathReport).getDownloadUrl().addOnSuccessListener(uri -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "downloadUriPdf: successfully " + pathReport);
            String pdfLink = uri.toString();
            createReport(pdfLink);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "downloadUriPdf: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void createReport(String pdfLink) {
        progressDialog.ShowProgressDialog();
        String reportId = UUID.randomUUID().toString();

        Principal principal = new Principal(false, "");
        TeamLeader teamLeader = new TeamLeader(false, "");
        VicePrincipal vicePrincipal = new VicePrincipal(false, "");
        ReportItem modelItem = new ReportItem(pdfLink, principal, purpose, report, reportId, teamLeader, dateProcurement, false, vicePrincipal);
        Report model = new Report(authId, placementId, modelItem);
        databaseReferenceReport.child(reportId).setValue(model).addOnSuccessListener(command -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "createReport: successfully " + reportId);
            listDatabaseProcurementReference();
            getTokenForNotification(model);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "createReport: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void getTokenForNotification(Report model) {
        databaseReferenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Users users = dataSnapshot.getValue(Users.class);
                        String tokenId = dataSnapshot.child("tokenId").getValue(String.class);
                        if (users != null && users.getLevel().equals(getString(R.string.admin))) {
                            Log.d(TAG, "getTokenForNotification: admin " + tokenId);
                            sendDataReportAndUser(model, tokenId);
                        } else if (users != null && users.getLevel().equals(getString(R.string.team_leader))) {
                            Log.d(TAG, "getTokenForNotification: teamLeader " + tokenId);
                            sendDataReportAndUser(model, tokenId);
                        } else if (users != null && users.getLevel().equals(getString(R.string.vice_principal))) {
                            Log.d(TAG, "getTokenForNotification: vicePrincipal " + tokenId);
                            sendDataReportAndUser(model, tokenId);
                        } else if (users != null && users.getLevel().equals(getString(R.string.principal))) {
                            Log.d(TAG, "getTokenForNotification: principal " + tokenId);
                            sendDataReportAndUser(model, tokenId);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "getTokenForNotification: failure ", error.toException());
                Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
        });
//        collectionReferenceUsers.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                Log.d(TAG, "getTokenForNotification: successfully " + collectionReferenceUsers.getId());
//                for (DocumentSnapshot snapshot : task.getResult()) {
//                    Users users = snapshot.toObject(Users.class);
//                    String tokenId = snapshot.getString("tokenId");
//                    if (users != null && users.getLevel().equals(getString(R.string.admin))) {
//                        Log.d(TAG, "getTokenForNotification: admin" + tokenId);
//                        sendDataReportAndUser(model, tokenId);
//                    } else if (users != null && users.getLevel().equals(getString(R.string.team_leader))) {
//                        Log.d(TAG, "getTokenForNotification: teamLeader" + tokenId);
//                        sendDataReportAndUser(model, tokenId);
//                    } else if (users != null && users.getLevel().equals(getString(R.string.vice_principal))) {
//                        Log.d(TAG, "getTokenForNotification: vicePrincipal" + tokenId);
//                        sendDataReportAndUser(model, tokenId);
//                    } else if (users != null && users.getLevel().equals(getString(R.string.principal))) {
//                        Log.d(TAG, "getTokenForNotification: principal" + tokenId);
//                        sendDataReportAndUser(model, tokenId);
//                    }
//                }
//            } else {
//                Log.w(TAG, "getTokenForNotification: failure ", task.getException());
//                Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void sendDataReportAndUser(Report report, String tokenId) {
        JSONObject to = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put("title", report.getReportItem().getPurpose());
            data.put("message", report.getReportItem().getReport());

            to.put("to", tokenId);
            to.put("data", data);

            sendNotification(to);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(JSONObject to) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, NOTIFICATION_URL, to, response -> Log.d(TAG, "sendNotification: " + response), error -> Log.e(TAG, "sendNotification: ", error)) {
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
        request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    @Override
    public void onStart() {
        listDatabaseProcurementReference();
        super.onStart();
    }
}