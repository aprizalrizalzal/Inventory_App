package id.sch.smkn1batukliang.inventory.ui.users.levels;

import static id.sch.smkn1batukliang.inventory.ui.users.levels.ListLevelFragment.EXTRA_LEVELS;

import android.os.Bundle;
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
import java.util.UUID;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.FragmentAddOrEditLevelBinding;
import id.sch.smkn1batukliang.inventory.model.users.levels.Levels;
import id.sch.smkn1batukliang.inventory.model.users.Users;

public class AddOrEditLevelFragment extends Fragment {

    private final ArrayList<String> listUser = new ArrayList<>();
    boolean isEmptyFields = false;
    private FragmentAddOrEditLevelBinding binding;
    private ArrayAdapter<String> stringListUserAdapter;
    private String authId, username, levelUsers, level, extraAuthId;
    private View view;
    private CollectionReference collectionReferenceUsers;
    private DatabaseReference databaseReferenceLevels;
    private CustomProgressDialog progressDialog;
    private Levels extraLevels;

    public AddOrEditLevelFragment() {
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
        binding = FragmentAddOrEditLevelBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        progressDialog = new CustomProgressDialog(requireActivity());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        collectionReferenceUsers = firestore.collection("users");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceLevels = database.getReference("levels");

        if (getArguments() != null) {
            extraLevels = getArguments().getParcelable(EXTRA_LEVELS);
        }

        if (extraLevels != null) {
            viewFirestoreExtraLevel(extraLevels);
            extraAuthId = extraLevels.getUsers().getAuthId();
        } else {
            collectionReferenceUsers.orderBy("username").get().addOnSuccessListener(queryDocumentSnapshots -> {
                listUser.clear();
                if (queryDocumentSnapshots.isEmpty()){
                    Toast.makeText(requireContext(), getString(R.string.no_data_available), Toast.LENGTH_SHORT).show();
                } else {
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        listUser.add(snapshot.getString("username"));
                        stringListUserAdapter = new ArrayAdapter<>(requireContext(), R.layout.list_mactv, listUser);
                        binding.mactvUsername.setAdapter(stringListUserAdapter);
                    }
                    binding.mactvUsername.setOnItemClickListener((parent, view, position, id) -> authId = queryDocumentSnapshots.getDocuments().get(position).getString("authId"));
                }
            }).addOnFailureListener(e -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        ArrayAdapter<CharSequence> stringListLevelAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.levels, R.layout.list_mactv);
        binding.mactvLevel.setAdapter(stringListLevelAdapter);

        binding.mactvLevel.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    levelUsers = getString(R.string.admin);
                    break;
                case 1:
                    levelUsers = getString(R.string.study_program_leader);
                    break;
                case 2:
                    levelUsers = getString(R.string.team_leader);
                    break;
                case 3:
                    levelUsers = getString(R.string.vice_principal);
                    break;
                case 4:
                    levelUsers = getString(R.string.principal);
                    break;
            }

        });

        binding.btnSave.setOnClickListener(v -> {
            username = binding.mactvUsername.getText().toString().trim();
            level = binding.mactvLevel.getText().toString().trim();

            isEmptyFields = validateFields();

        });

        binding.btnUpdate.setOnClickListener(v -> {
            username = binding.mactvUsername.getText().toString().trim();
            level = binding.mactvLevel.getText().toString().trim();

            isEmptyFields = validateUpdateFields();

        });

        return view;
    }

    private boolean validateFields() {
        if (username.isEmpty()) {
            binding.tilUsername.setError(getString(R.string.username_required));
            return false;
        } else {
            binding.tilUsername.setErrorEnabled(false);
        }

        if (level.isEmpty()) {
            binding.tilLevel.setError(getString(R.string.level_required));
            return false;
        } else {
            binding.tilLevel.setErrorEnabled(false);
        }

        if (authId == null) {
            binding.tilUsername.setError(getString(R.string.select_listed_username));
            return false;
        } else {
            binding.tilUsername.setErrorEnabled(false);
        }

        if (levelUsers == null) {
            binding.tilLevel.setError(getString(R.string.select_listed_level));
            return false;
        } else {
            binding.tilLevel.setErrorEnabled(false);
        }

        changeFirestoreUsers(authId);

        return true;
    }

    private void viewFirestoreExtraLevel(Levels extraLevel) {
        binding.tilUsername.setEnabled(false);
        binding.mactvUsername.setText(extraLevel.getUsers().getUsername());
        binding.mactvLevel.setText(extraLevel.getUsers().getLevel());
        binding.btnSave.setVisibility(View.GONE);
        binding.btnUpdate.setVisibility(View.VISIBLE);
    }

    private boolean validateUpdateFields() {

        if (level.isEmpty()) {
            binding.tilLevel.setError(getString(R.string.level_required));
            return false;
        } else {
            binding.tilLevel.setErrorEnabled(false);
        }

        if (levelUsers == null) {
            binding.tilLevel.setError(getString(R.string.select_listed_level));
            return false;
        } else {
            binding.tilLevel.setErrorEnabled(false);
        }

        changeFirestoreUsers(extraAuthId);

        return true;
    }

    private void changeFirestoreUsers(String authId) {
        collectionReferenceUsers.document(authId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Users users = snapshot.toObject(Users.class);
                if (users != null) {
                    if (extraLevels != null) {
                        updateLevels(users, extraAuthId);
                    } else {
                        createLevels(users);
                    }
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createLevels(Users users) {
        progressDialog.ShowProgressDialog();
        String levelId = UUID.randomUUID().toString();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatId = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        String dateId = simpleDateFormatId.format(calendar.getTime());

        Users l_user = new Users(users.getAuthId(), users.getEmail(), users.isEmailVerification(), users.getEmployeeIdNumber(), users.getPhotoLink(), level, users.getPosition(), dateId, users.getUsername());
        Levels levels = new Levels(levelId, l_user);
        databaseReferenceLevels.child(levelId).setValue(levels).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateLevelUsers(users.getAuthId());
            } else {
                Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
            progressDialog.DismissProgressDialog();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLevels(Users users, String userAuthId) {
        progressDialog.ShowProgressDialog();

        Users modelUser = new Users(userAuthId, users.getEmail(), users.isEmailVerification(), users.getEmployeeIdNumber(), users.getPhotoLink(), level, users.getPosition(), extraLevels.getUsers().getTimestamp(), users.getUsername());
        Levels model = new Levels(extraLevels.getLevelId(), modelUser);
        databaseReferenceLevels.child(extraLevels.getLevelId()).setValue(model).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateLevelUsers(userAuthId);
            } else {
                Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
            progressDialog.DismissProgressDialog();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLevelUsers(String userAuthId) {
        collectionReferenceUsers.document(userAuthId).update("level", level).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Navigation.findNavController(view).navigateUp();
                Toast.makeText(requireContext(), getString(R.string.update_level), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_SHORT).show());
    }
}