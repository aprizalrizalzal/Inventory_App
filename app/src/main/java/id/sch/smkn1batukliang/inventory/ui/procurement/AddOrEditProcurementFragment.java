package id.sch.smkn1batukliang.inventory.ui.procurement;

import static id.sch.smkn1batukliang.inventory.ui.placement.GridPlacementFragment.EXTRA_PLACEMENT_FOR_PROCUREMENT;
import static id.sch.smkn1batukliang.inventory.ui.procurement.ListProcurementFragment.EXTRA_PROCUREMENT;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.utils.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.utils.MoneyTextWatcher;
import id.sch.smkn1batukliang.inventory.databinding.FragmentAddOrEditProcurementBinding;
import id.sch.smkn1batukliang.inventory.model.placement.Placement;
import id.sch.smkn1batukliang.inventory.model.procurement.Procurement;
import id.sch.smkn1batukliang.inventory.model.procurement.ProcurementItem;

public class AddOrEditProcurementFragment extends Fragment {

    private static final String TAG = "AddOrEditProcurementFragment";
    boolean isEmptyFields = false;
    private FragmentAddOrEditProcurementBinding binding;
    private View view;
    private CustomProgressDialog progressDialog;
    private Placement extraPlacementForProcurement;
    private Procurement extraProcurement;
    private String authId, placementId;
    private String description, procurement, price, unit, volume;
    private DatabaseReference databaseReferenceExtraProcurement;

    public AddOrEditProcurementFragment() {
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
        binding = FragmentAddOrEditProcurementBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        progressDialog = new CustomProgressDialog(getActivity());

        if (getArguments() != null) {
            extraPlacementForProcurement = getArguments().getParcelable(EXTRA_PLACEMENT_FOR_PROCUREMENT);
            extraProcurement = getArguments().getParcelable(EXTRA_PROCUREMENT);
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceExtraProcurement = database.getReference("procurement");

        if (extraPlacementForProcurement != null) {
            authId = extraPlacementForProcurement.getAuthId();
            placementId = extraPlacementForProcurement.getPlacementItem().getPlacementId();
        }

        if (extraProcurement != null) {
            viewExtraProcurement();
        }

        binding.tietPrice.addTextChangedListener(new MoneyTextWatcher(binding.tietPrice));

        binding.btnSave.setOnClickListener(v -> {
            description = Objects.requireNonNull(binding.tietDescription.getText()).toString();
            procurement = Objects.requireNonNull(binding.tietProcurement.getText()).toString();
            price = Objects.requireNonNull(binding.tietPrice.getText()).toString();
            unit = Objects.requireNonNull(binding.tietUnit.getText()).toString();
            volume = Objects.requireNonNull(binding.tietVolume.getText()).toString();

            isEmptyFields = validateFields();
        });

        binding.btnUpdate.setOnClickListener(v -> {
            description = Objects.requireNonNull(binding.tietDescription.getText()).toString();
            procurement = Objects.requireNonNull(binding.tietProcurement.getText()).toString();
            price = Objects.requireNonNull(binding.tietPrice.getText()).toString();
            unit = Objects.requireNonNull(binding.tietUnit.getText()).toString();
            volume = Objects.requireNonNull(binding.tietVolume.getText()).toString();

            isEmptyFields = validateUpdateFields();
        });

        return view;
    }

    private boolean validateFields() {

        if (procurement.isEmpty()) {
            binding.tilProcurement.setError(getString(R.string.procurement_required));
            return false;
        } else {
            binding.tilProcurement.setErrorEnabled(false);
        }

        if (volume.isEmpty()) {
            binding.tilVolume.setError(getString(R.string.volume_required));
            return false;
        } else {
            binding.tilVolume.setErrorEnabled(false);
        }

        if (unit.isEmpty()) {
            binding.tilUnit.setError(getString(R.string.unit_required));
            return false;
        } else {
            binding.tilUnit.setErrorEnabled(false);
        }

        if (price.isEmpty()) {
            binding.tilPrice.setError(getString(R.string.price_required));
            return false;
        } else {
            binding.tilPrice.setErrorEnabled(false);
        }


        createProcurement();

        return true;
    }

    private void viewExtraProcurement() {
        binding.tietProcurement.setText(extraProcurement.getProcurementItem().getProcurement());
        binding.tietVolume.setText(String.valueOf(extraProcurement.getProcurementItem().getVolume()));
        binding.tietUnit.setText(extraProcurement.getProcurementItem().getUnit());
        binding.tietPrice.setText(MoneyTextWatcher.formatCurrency(extraProcurement.getProcurementItem().getPrice()));
        binding.tietDescription.setText(extraProcurement.getProcurementItem().getDescription());
        binding.btnSave.setVisibility(View.GONE);
        binding.btnUpdate.setVisibility(View.VISIBLE);
    }

    private boolean validateUpdateFields() {
        if (procurement.isEmpty()) {
            binding.tilProcurement.setError(getString(R.string.procurement_required));
            return false;
        } else {
            binding.tilProcurement.setErrorEnabled(false);
        }

        if (volume.isEmpty()) {
            binding.tilVolume.setError(getString(R.string.volume_required));
            return false;
        } else {
            binding.tilVolume.setErrorEnabled(false);
        }

        if (unit.isEmpty()) {
            binding.tilUnit.setError(getString(R.string.unit_required));
            return false;
        } else {
            binding.tilUnit.setErrorEnabled(false);
        }

        if (price.isEmpty()) {
            binding.tilPrice.setError(getString(R.string.price_required));
            return false;
        } else {
            binding.tilPrice.setErrorEnabled(false);
        }

        updateProcurement(extraProcurement.getProcurementItem());

        return true;
    }

    private void createProcurement() {
        progressDialog.ShowProgressDialog();

        String procurementId = UUID.randomUUID().toString();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatId = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        String dateId = simpleDateFormatId.format(calendar.getTime());

        BigDecimal priceValue = MoneyTextWatcher.parseCurrencyValue(price);
        String valuePrice = String.valueOf(priceValue);

        Double valueAmount = Integer.parseInt(volume) * Double.parseDouble(valuePrice);

        ProcurementItem procurementItem = new ProcurementItem(valueAmount, description, "", Double.parseDouble(valuePrice), procurementId, procurement, dateId, unit, Integer.parseInt(volume));
        Procurement procurement = new Procurement(authId, placementId, procurementItem);

        databaseReferenceExtraProcurement.child(procurementId).setValue(procurement).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "createProcurement: successfully " + procurementId);
            Navigation.findNavController(view).navigateUp();
            Toast.makeText(requireContext(), R.string.successfully, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "createProcurement: failure ", e);
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateProcurement(ProcurementItem extraProcurementItem) {
        progressDialog.ShowProgressDialog();
        BigDecimal priceValue = MoneyTextWatcher.parseCurrencyValue(price);
        String valuePrice = String.valueOf(priceValue);

        Double valueAmount = Integer.parseInt(volume) * Double.parseDouble(valuePrice);

        ProcurementItem procurementItem = new ProcurementItem(valueAmount, description, "", Double.parseDouble(valuePrice), extraProcurementItem.getProcurementId(), procurement, extraProcurementItem.getTimestamp(), unit, Integer.parseInt(volume));
        Procurement procurement = new Procurement(extraProcurement.getAuthId(), extraProcurement.getPlacementId(), procurementItem);

        databaseReferenceExtraProcurement.child(extraProcurement.getProcurementItem().getProcurementId()).setValue(procurement).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "updateProcurement: successfully " + extraProcurement.getProcurementItem().getProcurementId());
            Navigation.findNavController(view).navigateUp();
            Toast.makeText(requireContext(), R.string.successfully, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "updateProcurement: failure ", e);
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }
}