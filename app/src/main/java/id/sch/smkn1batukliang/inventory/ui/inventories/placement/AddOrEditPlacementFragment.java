package id.sch.smkn1batukliang.inventory.ui.inventories.placement;

import static id.sch.smkn1batukliang.inventory.ui.inventories.placement.ListPlacementFragment.EXTRA_PLACEMENT;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.FragmentAddOrEditPlacementBinding;
import id.sch.smkn1batukliang.inventory.model.inventories.placement.Placement;
import id.sch.smkn1batukliang.inventory.model.inventories.placement.PlacementItem;

public class AddOrEditPlacementFragment extends Fragment {

    private static final String TAG = "AddOrEditPlacementFragment";
    private final ArrayList<String> listUsers = new ArrayList<>();
    boolean isEmptyFields = false;
    private FragmentAddOrEditPlacementBinding binding;
    private View view;
    private CustomProgressDialog progressDialog;
    private Placement extraPlacement;
    private String authId, placement, username, extraAuthId;
    private DatabaseReference referencePlacement, referenceExtraPlacement;
    private ArrayAdapter<String> stringAdapter;

    public AddOrEditPlacementFragment() {
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
        binding = FragmentAddOrEditPlacementBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        progressDialog = new CustomProgressDialog(getActivity());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReferenceUsers = firestore.collection("users");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        referencePlacement = database.getReference("placement");

        stringAdapter = new ArrayAdapter<>(requireActivity(), R.layout.list_mactv, listUsers);

        if (getArguments() != null) {
            extraPlacement = getArguments().getParcelable(EXTRA_PLACEMENT);
        }

        if (extraPlacement != null) {
            referenceExtraPlacement = referencePlacement.child(extraPlacement.getPlacementItem().getPlacementId());
            extraAuthId = extraPlacement.getAuthId();
            viewExtraPlacement();
        } else {
            collectionReferenceUsers.orderBy("username").get().addOnCompleteListener(task -> {
                listUsers.clear();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        listUsers.add(documentSnapshot.getString("username"));
                        binding.mactvUsername.setOnItemClickListener((parent, view, position, id) -> authId = task.getResult().getDocuments().get(position).getId());
                    }
                    binding.mactvUsername.setAdapter(stringAdapter);
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            });
        }

        binding.btnSave.setOnClickListener(v -> {
            placement = Objects.requireNonNull(binding.tietPlacement.getText()).toString().trim();
            username = Objects.requireNonNull(binding.mactvUsername.getText()).toString();

            isEmptyFields = validateFields();
        });

        binding.btnUpdate.setOnClickListener(v -> {
            placement = Objects.requireNonNull(binding.tietPlacement.getText()).toString().trim();
            username = Objects.requireNonNull(binding.mactvUsername.getText()).toString();

            isEmptyFields = validateUpdateFields();
        });

        return view;
    }

    private boolean validateFields() {
        if (placement.isEmpty()) {
            binding.tilPlacement.setError(getString(R.string.placement_required));
            return false;
        } else {
            binding.tilPlacement.setErrorEnabled(false);
        }

        if (username.isEmpty()) {
            binding.tilUsername.setError(getString(R.string.username_required));
            return false;
        } else {
            binding.tilUsername.setErrorEnabled(false);
        }

        if (authId != null) {
            binding.tilUsername.setErrorEnabled(false);
        } else {
            binding.tilUsername.setError(getString(R.string.select_listed_username));
            return false;
        }

        createPlacement(authId);

        return true;
    }

    private void viewExtraPlacement() {
        binding.tietPlacement.setText(extraPlacement.getPlacementItem().getPlacement());
        binding.tilUsername.setEnabled(false);
        binding.mactvUsername.setText(extraPlacement.getPlacementItem().getUsername());
        binding.btnSave.setVisibility(View.GONE);
        binding.btnUpdate.setVisibility(View.VISIBLE);
    }

    private boolean validateUpdateFields() {
        if (placement.isEmpty()) {
            binding.tilPlacement.setError(getString(R.string.placement_required));
            return false;
        } else {
            binding.tilPlacement.setErrorEnabled(false);
        }

        updatePlacement(extraPlacement.getPlacementItem());

        return true;
    }

    private void createPlacement(String authId) {
        progressDialog.ShowProgressDialog();

        String placementId = UUID.randomUUID().toString();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatId = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        String dateId = simpleDateFormatId.format(calendar.getTime());

        PlacementItem modelItem = new PlacementItem("", placementId, placement, dateId, username);
        Placement model = new Placement(authId, modelItem);
        referencePlacement.child(placementId).setValue(model).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Navigation.findNavController(view).navigateUp();
                Toast.makeText(requireContext(), getString(R.string.successfully), Toast.LENGTH_SHORT).show();
            }
            progressDialog.DismissProgressDialog();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updatePlacement(PlacementItem placementItem) {
        PlacementItem modelItem = new PlacementItem(placementItem.getPhotoLink(), placementItem.getPlacementId(), placement, placementItem.getTimestamp(), placementItem.getUsername());
        Placement model = new Placement(extraAuthId, modelItem);
        referenceExtraPlacement.setValue(model).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Navigation.findNavController(view).navigateUp();
                Toast.makeText(requireContext(), getString(R.string.successfully), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}