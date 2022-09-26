package id.sch.smkn1batukliang.inventory.ui.inventories.procurement;

import static id.sch.smkn1batukliang.inventory.ui.inventories.GridPlacementFragment.EXTRA_PLACEMENT_FOR_PROCUREMENT;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.adapter.inventories.procurement.ListProcurementAdapter;
import id.sch.smkn1batukliang.inventory.addition.utilities.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.adapter.RecyclerViewEmptyData;
import id.sch.smkn1batukliang.inventory.databinding.FragmentListProcurementBinding;
import id.sch.smkn1batukliang.inventory.model.inventories.placement.Placement;
import id.sch.smkn1batukliang.inventory.model.inventories.procurement.Procurement;
import id.sch.smkn1batukliang.inventory.model.inventories.procurement.ProcurementItem;

public class ListProcurementFragment extends Fragment {

    public static final String EXTRA_PROCUREMENT = "extra_procurement";
    private static final String TAG = "ListProcurementFragment";
    private final ArrayList<Procurement> procurements = new ArrayList<>();
    private double total, totalAmount = 0.0;
    private Placement extraPlacementForProcurement;
    private FragmentListProcurementBinding binding;
    private View view;
    private String authId;
    private DatabaseReference databaseReferenceProcurement;
    private ListProcurementAdapter adapter;
    private CustomProgressDialog progressDialog;

    public ListProcurementFragment() {
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
        binding = FragmentListProcurementBinding.inflate(getLayoutInflater(), container,
                false);
        view = binding.getRoot();

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_main_nav, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_up_down_report) {
                    if (procurements.isEmpty()) {
                        Toast.makeText(requireContext(), getString(R.string.no_data_available), Toast.LENGTH_SHORT).show();
                    } else {
                        Bundle bundle = new Bundle();

                        bundle.putParcelable(EXTRA_PLACEMENT_FOR_PROCUREMENT, extraPlacementForProcurement);
                        Navigation.findNavController(view).navigate(R.id.action_nav_list_procurement_to_nav_add_report, bundle);
                    }
                }
                if (menuItem.getItemId() == R.id.action_report) {
                    if (procurements.isEmpty()) {
                        Toast.makeText(requireContext(), getString(R.string.no_data_available), Toast.LENGTH_SHORT).show();
                    } else {
                        Bundle bundle = new Bundle();

                        bundle.putParcelable(EXTRA_PLACEMENT_FOR_PROCUREMENT, extraPlacementForProcurement);
                        Navigation.findNavController(view).navigate(R.id.action_nav_list_procurement_to_nav_list_report, bundle);
                    }
                }

                return false;
            }

        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        if (getArguments() != null) {
            extraPlacementForProcurement = getArguments()
                    .getParcelable(EXTRA_PLACEMENT_FOR_PROCUREMENT);
        }

        progressDialog = new CustomProgressDialog(requireActivity());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            authId = user.getUid();
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceProcurement = database.getReference("procurement");

        adapter = new ListProcurementAdapter();
        binding.tvEmptyData.setText(getString(R.string.no_data_available_procurement));
        adapter.registerAdapterDataObserver(new RecyclerViewEmptyData(binding.rvListProcurement, binding.tvEmptyData));

        binding.rvListProcurement.setHasFixedSize(true);
        binding.rvListProcurement.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvListProcurement.setAdapter(adapter);

        binding.refreshLayout.setOnRefreshListener(() -> {
            listProcurementRealtime();
            binding.refreshLayout.setRefreshing(false);
        });

        binding.fab.setOnClickListener(v -> {
            Bundle bundle = new Bundle();

            bundle.putParcelable(EXTRA_PLACEMENT_FOR_PROCUREMENT, extraPlacementForProcurement);
            Navigation.findNavController(view)
                    .navigate(R.id.action_nav_list_procurement_to_nav_add_or_edit_procurement,
                            bundle);
        });

        return view;
    }

    private void listProcurementRealtime() {
        procurements.clear();
        progressDialog.ShowProgressDialog();
        databaseReferenceProcurement.orderByChild("procurementItem/procurement").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "onDataChange: procurementSuccessfully " + databaseReferenceProcurement.getKey());
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Procurement procurement = dataSnapshot.getValue(Procurement.class);
                        String placementId = extraPlacementForProcurement
                                .getPlacementItem().getPlacementId();
                        if (procurement != null
                                && authId.equals(procurement.getAuthId())
                                && placementId.equals(procurement.getPlacementId())) {
                            total = procurement.getProcurementItem().getAmount();
                            totalAmount = totalAmount + total;
                            procurements.add(procurement);
                            adapter.setListProcurement(procurements);
                        }
                        adapter.setOnItemClickCallbackEdit(procurementEdit
                                -> editSelectedProcurement(procurementEdit));
                        adapter.setOnItemClickCallbackDelete(procurementDelete
                                -> deleteSelectedProcurement(procurementDelete));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "onCancelled: procurementFailure ", error.toException());
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editSelectedProcurement(Procurement procurement) {
        Bundle bundle = new Bundle();

        bundle.putParcelable(EXTRA_PROCUREMENT, procurement);
        Navigation.findNavController(view)
                .navigate(R.id.action_nav_list_procurement_to_nav_add_or_edit_procurement, bundle);
    }

    private void deleteSelectedProcurement(Procurement procurement) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.f_delete_procurement,
                        procurement.getProcurementItem().getProcurement()))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel),
                        (dialog, id) -> dialog.cancel())
                .setPositiveButton(getString(R.string.yes),
                        (dialog, id) -> deleteProcurement(procurement.getProcurementItem()));
        builder.show();
    }

    private void deleteProcurement(ProcurementItem model) {
        progressDialog.ShowProgressDialog();
        databaseReferenceProcurement.child(model.getProcurementId()).removeValue().addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "deleteGoods: successfully " + model.getProcurementId());
            listProcurementRealtime();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "deleteGoods: failure ", e);
            Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onStart() {
        listProcurementRealtime();
        super.onStart();
    }
}
