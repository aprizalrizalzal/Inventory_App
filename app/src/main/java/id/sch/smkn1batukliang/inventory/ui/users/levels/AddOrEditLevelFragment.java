package id.sch.smkn1batukliang.inventory.ui.users.levels;

import static id.sch.smkn1batukliang.inventory.ui.users.levels.ListLevelFragment.EXTRA_LEVELS;

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
import java.util.UUID;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.FragmentAddOrEditLevelBinding;
import id.sch.smkn1batukliang.inventory.model.users.Users;
import id.sch.smkn1batukliang.inventory.model.users.levels.Levels;

public class AddOrEditLevelFragment extends Fragment {

    private static final String TAG = "AddOrEditLevelFragment";
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
            collectionReferenceUsers.orderBy("username").get().addOnCompleteListener(task -> {
                listUser.clear();
                Log.d(TAG, "onCreateView: usersSuccessfully " + collectionReferenceUsers.getId());
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        listUser.add(snapshot.getString("username"));
                        stringListUserAdapter = new ArrayAdapter<>(requireContext(), R.layout.list_mactv, listUser);
                        binding.mactvUsername.setAdapter(stringListUserAdapter);
                    }
                    binding.mactvUsername.setOnItemClickListener((parent, view, position, id) -> authId = task.getResult().getDocuments().get(position).getString("authId"));
                } else {
                    Log.w(TAG, "onCreateView: usersFailure ", task.getException());
                }
            });
        }

        ArrayAdapter<CharSequence> stringListLevelAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.levels, R.layout.list_mactv);
        binding.mactvLevel.setAdapter(stringListLevelAdapter);

        binding.mactvLevel.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    levelUsers = getString(R.string.admin);
                    break;
                case 1:
                    levelUsers = getString(R.string.teacher);
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
        collectionReferenceUsers.document(authId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "changeFirestoreUsers: successfully " + authId);
                Users users = task.getResult().toObject(Users.class);
                if (users != null) {
                    if (extraLevels != null) {
                        updateLevels(users, extraAuthId);
                    } else {
                        createLevels(users);
                    }
                }
            } else {
                Log.w(TAG, "changeFirestoreUsers: failure ", task.getException());
            }
        });
    }

    private void createLevels(Users users) {
        progressDialog.ShowProgressDialog();
        String levelId = UUID.randomUUID().toString();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatId = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        String dateId = simpleDateFormatId.format(calendar.getTime());

        Users lUser = new Users(users.getAuthId(), users.getEmail(), users.isEmailVerification(), users.getEmployeeIdNumber(), users.getPhotoLink(), level, users.getPosition(), dateId, users.getTokenId(), users.getUsername(), users.getWhatsappNumber());
        Levels levels = new Levels(levelId, lUser);
        databaseReferenceLevels.child(levelId).setValue(levels).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "createLevels: successfully " + levelId);
            updateLevelUsers(users.getAuthId());
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "createLevels: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLevels(Users users, String userAuthId) {
        progressDialog.ShowProgressDialog();

        Users lUsers = new Users(userAuthId, users.getEmail(), users.isEmailVerification(), users.getEmployeeIdNumber(), users.getPhotoLink(), level, users.getPosition(), extraLevels.getUsers().getTimestamp(), users.getTokenId(), users.getUsername(), users.getWhatsappNumber());
        Levels levels = new Levels(extraLevels.getLevelId(), lUsers);
        databaseReferenceLevels.child(extraLevels.getLevelId()).setValue(levels).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "updateLevels: successfully " + extraLevels.getLevelId());
            updateLevelUsers(userAuthId);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "updateLevels: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLevelUsers(String userAuthId) {
        collectionReferenceUsers.document(userAuthId).update("level", level).addOnSuccessListener(unused -> {
            Log.d(TAG, "updateLevelUsers: successfully " + userAuthId);
            Navigation.findNavController(view).navigateUp();
            Toast.makeText(requireContext(), getString(R.string.update_level), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "updateLevelUsers: failure ", e);
            Toast.makeText(requireActivity(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }
}