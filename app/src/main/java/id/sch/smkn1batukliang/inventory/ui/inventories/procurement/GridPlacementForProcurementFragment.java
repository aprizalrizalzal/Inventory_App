package id.sch.smkn1batukliang.inventory.ui.inventories.procurement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.adapter.inventories.placement.GridPlacementForProcurementAdapter;
import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.addition.RecyclerViewEmptyData;
import id.sch.smkn1batukliang.inventory.databinding.FragmentGridPlacementForProcurementBinding;
import id.sch.smkn1batukliang.inventory.model.inventories.placement.Placement;

public class GridPlacementForProcurementFragment extends Fragment {

    public static final String EXTRA_PLACEMENT_FOR_PROCUREMENT = "extra_placement_for_procurement";
    private final ArrayList<Placement> placements = new ArrayList<>();
    private FragmentGridPlacementForProcurementBinding binding;
    private View view;
    private String authId;
    private DatabaseReference databaseReferencePlacement;
    private GridPlacementForProcurementAdapter adapter;
    private CustomProgressDialog progressDialog;

    public GridPlacementForProcurementFragment() {
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
        binding = FragmentGridPlacementForProcurementBinding
                .inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        progressDialog = new CustomProgressDialog(getActivity());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            authId = user.getUid();
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferencePlacement = database.getReference("placement");

        adapter = new GridPlacementForProcurementAdapter();
        binding.tvEmptyData.setText(getString(R.string.no_data_available_placement_for_procurement));
        binding.tvEmptyData.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_nav_grid_placement_for_procurement_to_nav_help));
        adapter.registerAdapterDataObserver(new RecyclerViewEmptyData(binding.rvPlacementForProcurement, binding.tvEmptyData));

        binding.rvPlacementForProcurement.setHasFixedSize(true);
        binding.rvPlacementForProcurement.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.rvPlacementForProcurement.setAdapter(adapter);

        binding.refreshLayout.setOnRefreshListener(() -> {
            gridPlacementForProcurementRealtime();
            binding.refreshLayout.setRefreshing(false);
        });

        return view;
    }

    @Override
    public void onStart() {
        gridPlacementForProcurementRealtime();
        super.onStart();
    }

    private void gridPlacementForProcurementRealtime() {
        progressDialog.ShowProgressDialog();
        databaseReferencePlacement
                .orderByChild("placementItem/placement")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        placements.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Placement placement = dataSnapshot.getValue(Placement.class);
                                if (placement != null && placement.getAuthId().equals(authId)) {
                                    placements.add(placement);
                                    adapter.setGridPlacementForProcurement(placements);
                                }
                                adapter.setOnItemClickCallback(placemenForProcurement -> selectedPlacement(placemenForProcurement));
                            }
                        }
                        progressDialog.DismissProgressDialog();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.DismissProgressDialog();
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });

    }

    private void selectedPlacement(Placement placement) {
        Bundle bundle = new Bundle();

        bundle.putParcelable(EXTRA_PLACEMENT_FOR_PROCUREMENT, placement);
        Navigation.findNavController(view)
                .navigate(R.id.action_nav_grid_placement_for_procurement_to_nav_list_procurement,
                        bundle);
    }
}