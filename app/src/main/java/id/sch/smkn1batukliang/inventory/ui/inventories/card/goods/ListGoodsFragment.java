package id.sch.smkn1batukliang.inventory.ui.inventories.card.goods;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.adapter.ListProcurementAdapter;
import id.sch.smkn1batukliang.inventory.databinding.FragmentListGoodsBinding;
import id.sch.smkn1batukliang.inventory.model.procurement.Procurement;
import id.sch.smkn1batukliang.inventory.utils.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.utils.RecyclerViewEmptyData;


public class ListGoodsFragment extends Fragment {

    private static final String TAG = "ListGoodsFragment";
    private final ArrayList<Procurement> procurements = new ArrayList<>();
    private double total, totalAmount = 0.0;
    private DatabaseReference databaseReferenceGoods;
    private ListProcurementAdapter adapter;
    private CustomProgressDialog progressDialog;
    private FragmentListGoodsBinding binding;
    private View view;

    public ListGoodsFragment() {
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
        binding = FragmentListGoodsBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        progressDialog = new CustomProgressDialog(requireActivity());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceGoods = database.getReference("goods");

        adapter = new ListProcurementAdapter();
        binding.tvEmptyData.setText(getString(R.string.no_data_available_goods));
        adapter.registerAdapterDataObserver(new RecyclerViewEmptyData(binding.rvListGoods, binding.tvEmptyData));

        binding.rvListGoods.setHasFixedSize(true);
        binding.rvListGoods.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvListGoods.setAdapter(adapter);

        binding.refreshLayout.setOnRefreshListener(() -> {
            listGoodsRealtime();
            binding.refreshLayout.setRefreshing(false);
        });

        return view;
    }

    private void listGoodsRealtime() {
        procurements.clear();
        progressDialog.ShowProgressDialog();
        databaseReferenceGoods.orderByChild("procurementItem/procurement").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "onDataChange: procurementSuccessfully " + databaseReferenceGoods.getKey());
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Procurement procurement = dataSnapshot.getValue(Procurement.class);
                        if (procurement != null) {
                            total = procurement.getProcurementItem().getAmount();
                            totalAmount = totalAmount + total;
                            procurements.add(procurement);
                            adapter.setListProcurement(procurements);
                            adapter.setActivateButtons(false);
                        }
                        adapter.setOnItemClickCallback(selectedProcurement -> selectedProcurement(selectedProcurement));
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

    private void selectedProcurement(Procurement selectedProcurement) {

    }

    @Override
    public void onStart() {
        listGoodsRealtime();
        super.onStart();
    }
}