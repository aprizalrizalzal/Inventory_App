package id.sch.smkn1batukliang.inventory.ui.placement;

import android.os.Bundle;
import android.util.Log;
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
import id.sch.smkn1batukliang.inventory.utils.RecyclerViewEmptyData;
import id.sch.smkn1batukliang.inventory.adapter.placement.GridPlacementAdapter;
import id.sch.smkn1batukliang.inventory.utils.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.FragmentGridPlacementBinding;
import id.sch.smkn1batukliang.inventory.model.placement.Placement;

public class GridPlacementFragment extends Fragment {

    public static final String EXTRA_PLACEMENT_FOR_PROCUREMENT = "extra_placement_for_procurement";
    private static final String TAG = "GridPlacementFragment";
    private final ArrayList<Placement> placements = new ArrayList<>();
    private FragmentGridPlacementBinding binding;
    private View view;
    private String authId;
    private DatabaseReference databaseReferencePlacement;
    private GridPlacementAdapter adapter;
    private CustomProgressDialog progressDialog;

    public GridPlacementFragment() {
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
        binding = FragmentGridPlacementBinding
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

        adapter = new GridPlacementAdapter();
        binding.tvEmptyData.setText(getString(R.string.no_data_available_placement_for_procurement));
        binding.tvEmptyData.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_nav_grid_placement_to_nav_help));
        adapter.registerAdapterDataObserver(new RecyclerViewEmptyData(binding.rvPlacementForProcurement, binding.tvEmptyData));

        binding.rvPlacementForProcurement.setHasFixedSize(true);
        binding.rvPlacementForProcurement.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvPlacementForProcurement.setAdapter(adapter);

        binding.refreshLayout.setOnRefreshListener(() -> {
            gridPlacementForProcurementRealtime();
            binding.refreshLayout.setRefreshing(false);
        });

        return view;
    }

    private void gridPlacementForProcurementRealtime() {
        placements.clear();
        progressDialog.ShowProgressDialog();
        databaseReferencePlacement.orderByChild("placementItem/placement").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "onDataChange: placementForProcurementSuccessfully " + databaseReferencePlacement.getKey());
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "onCancelled: placementForProcurementFailure ", error.toException());
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void selectedPlacement(Placement placement) {
        Bundle bundle = new Bundle();

        bundle.putParcelable(EXTRA_PLACEMENT_FOR_PROCUREMENT, placement);
        Navigation.findNavController(view)
                .navigate(R.id.action_nav_grid_placement_to_list_procurement,
                        bundle);
    }

    @Override
    public void onStart() {
        gridPlacementForProcurementRealtime();
        super.onStart();
    }
}