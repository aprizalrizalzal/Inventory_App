package id.sch.smkn1batukliang.inventory.ui.levels;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.adapter.ListLevelAdapter;
import id.sch.smkn1batukliang.inventory.utili.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.utili.RecyclerViewEmptyData;
import id.sch.smkn1batukliang.inventory.databinding.FragmentListLevelBinding;
import id.sch.smkn1batukliang.inventory.model.levels.Levels;

public class ListLevelFragment extends Fragment {

    public static final String EXTRA_LEVELS = "extra_levels_users";
    private static final String TAG = "ListLevelFragment";
    private final ArrayList<Levels> listLevel = new ArrayList<>();
    private FragmentListLevelBinding binding;
    private View view;
    private CollectionReference collectionReferenceUsers;
    private DatabaseReference databaseReferenceLevels;
    private ListLevelAdapter adapter;
    private CustomProgressDialog progressDialog;

    public ListLevelFragment() {
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
        binding = FragmentListLevelBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        collectionReferenceUsers = firestore.collection("users");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceLevels = database.getReference("levels");

        progressDialog = new CustomProgressDialog(getActivity());

        adapter = new ListLevelAdapter();
        binding.tvEmptyData.setText(getString(R.string.no_data_available_levels));
        adapter.registerAdapterDataObserver(new RecyclerViewEmptyData(binding.rvListLevel, binding.tvEmptyData));

        binding.rvListLevel.setHasFixedSize(true);
        binding.rvListLevel.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvListLevel.setAdapter(adapter);

        binding.refreshLayout.setOnRefreshListener(() -> {
            listLevelRealtime();
            binding.refreshLayout.setRefreshing(false);
        });

        binding.fab.setOnClickListener(v -> Navigation.createNavigateOnClickListener(R.id.action_nav_list_level_to_add_or_edit_level).onClick(v));

        return view;
    }

    private void listLevelRealtime() {
        listLevel.clear();
        progressDialog.ShowProgressDialog();
        databaseReferenceLevels.orderByChild("levelsItem/level").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "onDataChange: LevelsSuccessfully " +databaseReferenceLevels.getKey());
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Levels levels = dataSnapshot.getValue(Levels.class);
                        if (levels != null) {
                            listLevel.add(levels);
                            adapter.setListLevel(listLevel);
                        }
                    }
                    adapter.setOnItemClickCallbackEdit(editLevel -> editSelectedLevels(editLevel));
                    adapter.setOnItemClickCallbackDelete(deleteLevel -> deleteSelectedLevels(deleteLevel));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "onCancelled: LevelsFailure ", error.toException());
                Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void editSelectedLevels(Levels levels) {
        Bundle bundle = new Bundle();

        bundle.putParcelable(EXTRA_LEVELS, levels);
        Navigation.findNavController(view).navigate(R.id.action_nav_list_level_to_add_or_edit_level, bundle);
    }

    private void deleteSelectedLevels(Levels model) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.delete)).setMessage(getString(R.string.f_delete_level, "model.getUsers().getLevel()")).setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel())
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> deleteLevel(model));
        builder.show();
    }

    private void deleteLevel(Levels levels) {
        databaseReferenceLevels.child(levels.getLevelsItem().getLevelId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "deleteLevel: successfully " + levels.getLevelsItem().getLevelId());
                updateLevelUsers(levels.getAuthId());
            } else {
                Log.w(TAG, "deleteLevel: failure ", task.getException());
                Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateLevelUsers(String authId) {
        collectionReferenceUsers.document(authId).update("level", "").addOnSuccessListener(unused -> {
            Log.d(TAG, "updateLevelUsers: successfully " + authId);
            listLevelRealtime();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "updateLevelUsers: failure ", e);
            Toast.makeText(requireActivity(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onStart() {
        listLevelRealtime();
        super.onStart();
    }
}