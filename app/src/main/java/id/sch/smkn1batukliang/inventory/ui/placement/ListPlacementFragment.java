package id.sch.smkn1batukliang.inventory.ui.placement;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.adapter.placement.ListPlacementAdapter;
import id.sch.smkn1batukliang.inventory.utils.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.utils.RecyclerViewEmptyData;
import id.sch.smkn1batukliang.inventory.databinding.FragmentListPlacementBinding;
import id.sch.smkn1batukliang.inventory.model.placement.Placement;
import id.sch.smkn1batukliang.inventory.model.placement.PlacementItem;

public class ListPlacementFragment extends Fragment {

    public static final String EXTRA_PLACEMENT = "extra_placement";
    private static final String TAG = "ListPlacementFragment";
    private final ArrayList<Placement> placements = new ArrayList<>();
    private FragmentListPlacementBinding binding;
    private View view;
    private DatabaseReference databaseReferencePlacement;
    private ListPlacementAdapter adapter;
    private CustomProgressDialog progressDialog;

    public ListPlacementFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ResourceType")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentListPlacementBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        progressDialog = new CustomProgressDialog(getActivity());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferencePlacement = database.getReference("placement");

        binding.refreshLayout.setOnRefreshListener(() -> {
            listPlacementRealtime();
            binding.refreshLayout.setRefreshing(false);
        });

        adapter = new ListPlacementAdapter();
        binding.tvEmptyData.setText(getString(R.string.no_data_available_placement));
        adapter.registerAdapterDataObserver(new RecyclerViewEmptyData(binding.rvListPlacement, binding.tvEmptyData));

        binding.rvListPlacement.setHasFixedSize(true);
        binding.rvListPlacement.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvListPlacement.setAdapter(adapter);

        binding.fab.setOnClickListener(v -> Navigation.createNavigateOnClickListener(R.id.action_nav_list_placement_to_add_or_edit_placement).onClick(v));

        return view;
    }

    private void listPlacementRealtime() {
        placements.clear();
        progressDialog.ShowProgressDialog();
        databaseReferencePlacement.orderByChild("placementItem/placement").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "onDataChange: placementSuccessfully " + databaseReferencePlacement.getKey());
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Placement placement = dataSnapshot.getValue(Placement.class);
                        if (placement != null) {
                            placements.add(placement);
                            adapter.setListPlacement(placements);
                        }
                        adapter.setOnItemClickCallback(editPlacement -> editSelectedPlacement(editPlacement));
                        adapter.setOnItemClickCallbackDelete(deletePlacement -> deleteSelectedPlacement(deletePlacement));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "onCancelled: placementFailure ", error.toException());
                Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editSelectedPlacement(Placement placement) {
        Bundle bundle = new Bundle();

        bundle.putParcelable(EXTRA_PLACEMENT, placement);
        Navigation.findNavController(view).navigate(R.id.action_nav_list_placement_to_add_or_edit_placement, bundle);
    }

    private void deleteSelectedPlacement(Placement placement) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.delete)).setMessage(getString(R.string.f_delete_placement, placement.getPlacementItem().getPlacement())).setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel())
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> deletePlacement(placement.getPlacementItem()));
        builder.show();
    }

    private void deletePlacement(PlacementItem placementItem) {
        progressDialog.ShowProgressDialog();
        databaseReferencePlacement.child(placementItem.getPlacementId()).removeValue().addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "deletePlacement: successfully " + placementItem.getPlacementId());
            listPlacementRealtime();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "deletePlacement: failure ", e);
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onStart() {
        listPlacementRealtime();
        super.onStart();
    }
}