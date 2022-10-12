package id.sch.smkn1batukliang.inventory.ui.inventories.card.room;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.adapter.placement.ListPlacementAdapter;
import id.sch.smkn1batukliang.inventory.databinding.FragmentListRoomBinding;
import id.sch.smkn1batukliang.inventory.model.placement.Placement;
import id.sch.smkn1batukliang.inventory.utils.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.utils.RecyclerViewEmptyData;

public class ListRoomFragment extends Fragment {

    public static final String EXTRA_ROOM_FOR_PROCUREMENT = "extra_placement_for_procurement";
    private static final String TAG = "ListRoomFragment";
    private final ArrayList<Placement> placements = new ArrayList<>();
    private DatabaseReference databaseReferenceRoom;
    private ListPlacementAdapter adapter;
    private CustomProgressDialog progressDialog;
    private FragmentListRoomBinding binding;
    private View view;

    public ListRoomFragment() {
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
        binding = FragmentListRoomBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        progressDialog = new CustomProgressDialog(requireActivity());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceRoom = database.getReference("room");

        adapter = new ListPlacementAdapter();
        binding.tvEmptyData.setText(getString(R.string.no_data_available_room));
        adapter.registerAdapterDataObserver(new RecyclerViewEmptyData(binding.rvListRoom, binding.tvEmptyData));

        binding.rvListRoom.setHasFixedSize(true);
        binding.rvListRoom.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvListRoom.setAdapter(adapter);

        binding.refreshLayout.setOnRefreshListener(() -> {
            listRoomRealtime();
            binding.refreshLayout.setRefreshing(false);
        });

        return view;
    }

    private void listRoomRealtime() {
        placements.clear();
        progressDialog.ShowProgressDialog();
        databaseReferenceRoom.orderByChild("placementItem/placement").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "onDataChange: placementSuccessfully " + databaseReferenceRoom.getKey());
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Placement placement = dataSnapshot.getValue(Placement.class);
                        if (placement != null) {
                            placements.add(placement);
                            adapter.setListPlacement(placements);
                            adapter.setActivateButtons(false);
                        }
                        adapter.setOnItemClickCallback(selectedPlacement -> selectedPlacement(selectedPlacement));
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

    private void selectedPlacement(Placement placement) {
        Bundle bundle = new Bundle();

        bundle.putParcelable(EXTRA_ROOM_FOR_PROCUREMENT, placement);
        Navigation.findNavController(view).navigate(R.id.action_list_rooms_to_list_procurement, bundle);
    }

    @Override
    public void onStart() {
        super.onStart();
        listRoomRealtime();
    }
}