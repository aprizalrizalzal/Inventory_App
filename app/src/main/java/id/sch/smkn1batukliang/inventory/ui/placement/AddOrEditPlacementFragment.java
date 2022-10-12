package id.sch.smkn1batukliang.inventory.ui.placement;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.FragmentAddOrEditPlacementBinding;
import id.sch.smkn1batukliang.inventory.model.placement.Placement;
import id.sch.smkn1batukliang.inventory.model.placement.PlacementItem;
import id.sch.smkn1batukliang.inventory.model.users.Users;
import id.sch.smkn1batukliang.inventory.utils.CustomProgressDialog;

public class AddOrEditPlacementFragment extends Fragment {

    private static final String TAG = "AddOrEditPlacementFragment";
    private final ArrayList<Users> listUser = new ArrayList<>();
    boolean isEmptyFields = false;
    private ArrayAdapter<Users> stringListUserAdapter;
    private FragmentAddOrEditPlacementBinding binding;
    private View view;
    private CustomProgressDialog progressDialog;
    private Placement extraPlacement;
    private String authId, placement, username, extraAuthId, extraPlacementId;
    private DatabaseReference databaseReferencePlacement;

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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReferenceUsers = database.getReference("users");
        databaseReferencePlacement = database.getReference("placement");

        stringListUserAdapter = new ArrayAdapter<>(requireActivity(), R.layout.list_mactv, listUser);

        if (getArguments() != null) {
            extraPlacement = getArguments().getParcelable(ListPlacementFragment.EXTRA_PLACEMENT);
        }

        if (extraPlacement != null) {
            extraPlacementId = extraPlacement.getPlacementItem().getPlacementId();
            extraAuthId = extraPlacement.getAuthId();
            viewExtraPlacement();
        } else {
            listUser.clear();
            databaseReferenceUsers.orderByChild("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "onDataChange: Users");
                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Users users = dataSnapshot.getValue(Users.class);
                            if (users != null) {
                                listUser.add(users);
                                stringListUserAdapter = new ArrayAdapter<>(requireContext(), R.layout.list_mactv, listUser);
                                binding.mactvUsername.setAdapter(stringListUserAdapter);
                            }
                            binding.mactvUsername.setOnItemClickListener((parent, view, position, id) -> authId = listUser.get(position).getAuthId());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "onCancelled: Users", error.toException());
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

        createPlacement();

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

        updatePlacement();

        return true;
    }

    private void createPlacement() {
        progressDialog.ShowProgressDialog();

        String placementId = UUID.randomUUID().toString();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatId = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        String dateId = simpleDateFormatId.format(calendar.getTime());

        PlacementItem modelItem = new PlacementItem("", placementId, placement, dateId, username);
        Placement model = new Placement(authId, modelItem);
        databaseReferencePlacement.child(placementId).setValue(model).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "createPlacement: successfully " + placementId);
            Navigation.findNavController(view).navigateUp();
            Toast.makeText(requireContext(), R.string.successfully, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "createPlacement: failure ", e);
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }

    private void updatePlacement() {
        progressDialog.ShowProgressDialog();
        Map<String, Object> mapPlacement = new HashMap<>();
        mapPlacement.put("/placementItem/placement", placement);

        databaseReferencePlacement.child(extraPlacementId).updateChildren(mapPlacement).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "updatePlacement: successfully " + extraAuthId);
            Navigation.findNavController(view).navigateUp();
            Toast.makeText(requireContext(), R.string.successfully, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "updatePlacement: failure ", e);
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }
}