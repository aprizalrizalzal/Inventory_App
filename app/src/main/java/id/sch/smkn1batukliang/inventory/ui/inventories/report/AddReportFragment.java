package id.sch.smkn1batukliang.inventory.ui.inventories.report;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static id.sch.smkn1batukliang.inventory.ui.inventories.procurement.GridPlacementForProcurementFragment.EXTRA_PLACEMENT_FOR_PROCUREMENT;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUriExposedException;
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
import androidx.navigation.Navigation;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.addition.MoneyTextWatcher;
import id.sch.smkn1batukliang.inventory.databinding.FragmentAddReportBinding;
import id.sch.smkn1batukliang.inventory.model.inventories.placement.Placement;
import id.sch.smkn1batukliang.inventory.model.inventories.procurement.Procurement;
import id.sch.smkn1batukliang.inventory.model.inventories.report.Report;
import id.sch.smkn1batukliang.inventory.model.inventories.report.ReportItem;
import id.sch.smkn1batukliang.inventory.model.users.Users;

public class AddReportFragment extends Fragment {

    private final Calendar calendar = Calendar.getInstance();
    private final ArrayList<Procurement> procurements = new ArrayList<>();
    private final ArrayList<String> listUser = new ArrayList<>();
    boolean isEmptyFields = false;
    private ArrayAdapter<String> stringAdapter;
    private FragmentAddReportBinding binding;
    private SimpleDateFormat simpleDateFormatId;
    private View viewBinding;
    private CustomProgressDialog progressDialog;
    private DatabaseReference databaseReferenceProcurement, databaseReferenceReport;
    private StorageReference storageReference;
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
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
        viewBinding = binding.getRoot();

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

        stringAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_mactv, listUser);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReferenceUsers = firestore.collection("users");

        collectionReferenceUsers.orderBy("username").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.no_data_available), Toast.LENGTH_SHORT).show();
            } else {
                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                    Users users = snapshot.toObject(Users.class);
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
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());

        collectionReferenceUsers.orderBy("username").get().addOnSuccessListener(queryDocumentSnapshots -> {
            listUser.clear();
            if (queryDocumentSnapshots.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.no_data_available), Toast.LENGTH_SHORT).show();
            } else {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    listUser.add(documentSnapshot.getString("username"));
                    binding.mactvTeamLeader.setOnItemClickListener((parent, view, position, id) -> einTeamLeader = queryDocumentSnapshots.getDocuments().get(position).getString("employeeIdNumber"));
                    binding.mactvVicePrincipal.setOnItemClickListener((parent, view, position, id) -> einVicePrincipal = queryDocumentSnapshots.getDocuments().get(position).getString("employeeIdNumber"));
                    binding.mactvPrincipal.setOnItemClickListener((parent, view, position, id) -> einPrincipal = queryDocumentSnapshots.getDocuments().get(position).getString("employeeIdNumber"));
                }
                binding.mactvTeamLeader.setAdapter(stringAdapter);
                binding.mactvVicePrincipal.setAdapter(stringAdapter);
                binding.mactvPrincipal.setAdapter(stringAdapter);
            }
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceProcurement = database.getReference("procurement");
        databaseReferenceReport = database.getReference("report");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

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

        return viewBinding;
    }

    private void listProcurementRealtime() {
        progressDialog.ShowProgressDialog();
        databaseReferenceProcurement.orderByChild("procurementItem/procurement").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                procurements.clear();
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

        String pathDownload = Environment.getExternalStorageDirectory().getPath() + "/Download/";
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
        String pathReport = "users/procurement/" + authId + "/report/" + placementId + "/" + report;
        storageReference.child(pathReport).putFile(Uri.fromFile(path)).addOnSuccessListener(taskSnapshot -> {
            progressDialog.DismissProgressDialog();
            downloadUriPdf(pathReport);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void downloadUriPdf(String pathReport) {
        progressDialog.ShowProgressDialog();
        storageReference.child(pathReport).getDownloadUrl().addOnSuccessListener(uri -> {
            progressDialog.DismissProgressDialog();
            String pdfLink = uri.toString();
            createReport(pdfLink);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void createReport(String pdfLink) {
        progressDialog.ShowProgressDialog();
        String reportId = UUID.randomUUID().toString();

        ReportItem modelItem = new ReportItem(pdfLink, purpose, report, reportId, false, dateProcurement);
        Report model = new Report(authId, placementId, modelItem);
        databaseReferenceReport.child(reportId).setValue(model).addOnSuccessListener(command -> {
            progressDialog.DismissProgressDialog();
            listProcurementRealtime();
            previewPdf();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void previewPdf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(path), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                requireActivity().startActivity(intent);
            } catch (FileUriExposedException e) {
                Navigation.findNavController(viewBinding).navigateUp();
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStart() {
        listProcurementRealtime();
        super.onStart();
    }
}