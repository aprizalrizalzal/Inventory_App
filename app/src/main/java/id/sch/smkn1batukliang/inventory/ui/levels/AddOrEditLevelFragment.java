package id.sch.smkn1batukliang.inventory.ui.levels;

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
import java.util.UUID;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.FragmentAddOrEditLevelBinding;
import id.sch.smkn1batukliang.inventory.model.levels.Levels;
import id.sch.smkn1batukliang.inventory.model.levels.LevelsItem;
import id.sch.smkn1batukliang.inventory.model.users.Users;
import id.sch.smkn1batukliang.inventory.utils.CustomProgressDialog;

public class AddOrEditLevelFragment extends Fragment {

    private static final String TAG = "AddOrEditLevelFragment";
    private final ArrayList<Users> listUser = new ArrayList<>();
    boolean isEmptyFields = false;
    private FragmentAddOrEditLevelBinding binding;
    private ArrayAdapter<Users> stringListUserAdapter;
    private String authId, username;
    private String levelAuthId, levelPosition;
    private String level;
    private View view;
    private DatabaseReference databaseReferenceUsers, databaseReferenceLevels;
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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUsers = database.getReference("users");
        databaseReferenceLevels = database.getReference("levels");

        if (getArguments() != null) {
            extraLevels = getArguments().getParcelable(ListLevelFragment.EXTRA_LEVELS);
        }

        if (extraLevels != null) {
            levelAuthId = extraLevels.getAuthId();
            viewRealtimeExtraLevels(extraLevels);
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
                                binding.mactvUsername.setOnItemClickListener((parent, view, position, id) -> authId = listUser.get(position).getAuthId());
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

        ArrayAdapter<CharSequence> stringListLevelAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.levels, R.layout.list_mactv);
        binding.mactvLevel.setAdapter(stringListLevelAdapter);

        binding.mactvLevel.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    levelPosition = getString(R.string.admin);
                    break;
                case 1:
                    levelPosition = getString(R.string.teacher);
                    break;
                case 2:
                    levelPosition = getString(R.string.team_leader);
                    break;
                case 3:
                    levelPosition = getString(R.string.vice_principal);
                    break;
                case 4:
                    levelPosition = getString(R.string.principal);
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

        if (levelPosition == null) {
            binding.tilLevel.setError(getString(R.string.select_listed_level));
            return false;
        } else {
            binding.tilLevel.setErrorEnabled(false);
        }

        createLevels();

        return true;
    }

    private void viewRealtimeExtraLevels(Levels extraLevels) {
        databaseReferenceUsers.child(levelAuthId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: Users");
                if (snapshot.exists()) {
                    Users users = snapshot.getValue(Users.class);
                    if (users != null) {
                        binding.tilUsername.setEnabled(false);
                        binding.mactvUsername.setText(users.getUsername());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled: Users", error.toException());
            }
        });

        binding.mactvLevel.setText(extraLevels.getLevelsItem().getLevel());
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

        if (levelPosition == null) {
            binding.tilLevel.setError(getString(R.string.select_listed_level));
            return false;
        } else {
            binding.tilLevel.setErrorEnabled(false);
        }

        updateLevels(levelAuthId);

        return true;
    }

    private void createLevels() {
        progressDialog.ShowProgressDialog();
        String levelId = UUID.randomUUID().toString();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatId = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        String dateId = simpleDateFormatId.format(calendar.getTime());

        LevelsItem levelsItem = new LevelsItem(levelId, level, dateId);
        Levels levels = new Levels(authId, levelsItem);
        databaseReferenceLevels.child(levelId).setValue(levels).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "createLevels: successfully " + levelId);
            updateLevelUsers(authId);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "createLevels: failure ", e);
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLevelUsers(String authId) {
        progressDialog.ShowProgressDialog();

        Map<String, Object> mapUsers = new HashMap<>();
        mapUsers.put("level", level);

        databaseReferenceUsers.child(authId).updateChildren(mapUsers).addOnSuccessListener(unused -> {
            Log.d(TAG, "updateLevelUsers: Users");
            progressDialog.DismissProgressDialog();
            Navigation.findNavController(view).navigateUp();
            Toast.makeText(requireContext(), R.string.successfully, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "updateLevelUsers: Users", e);
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLevels(String lAuthId) {
        progressDialog.ShowProgressDialog();

        Map<String, Object> mapLevels = new HashMap<>();
        mapLevels.put("level", level);

        databaseReferenceLevels.child(extraLevels.getLevelsItem().getLevelId()).updateChildren(mapLevels).addOnSuccessListener(unused -> {
            Log.d(TAG, "updateLevels: Users");
            progressDialog.DismissProgressDialog();
            updateLevelUsers(lAuthId);
            Toast.makeText(requireContext(), R.string.successfully, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "updateLevels: Users", e);
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });

        LevelsItem levelsItem = new LevelsItem(extraLevels.getLevelsItem().getLevelId(), level, extraLevels.getLevelsItem().getTimestamp());
        Levels levels = new Levels(lAuthId, levelsItem);
        databaseReferenceLevels.child(extraLevels.getLevelsItem().getLevelId()).setValue(levels).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "updateLevels: successfully " + extraLevels.getLevelsItem().getLevelId());
            updateLevelUsers(lAuthId);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "updateLevels: failure ", e);
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }
}