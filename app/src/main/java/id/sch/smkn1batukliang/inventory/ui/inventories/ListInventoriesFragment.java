package id.sch.smkn1batukliang.inventory.ui.inventories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import id.sch.smkn1batukliang.inventory.databinding.FragmentListInventoriesBinding;
import id.sch.smkn1batukliang.inventory.model.inventories.Inventories;
import id.sch.smkn1batukliang.inventory.model.inventories.InventoriesItem;

public class ListInventoriesFragment extends Fragment {

    private FragmentListInventoriesBinding binding;
    private View view;

    public ListInventoriesFragment() {
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
        binding = FragmentListInventoriesBinding.inflate(getLayoutInflater());
        view = binding.getRoot();

        binding.refreshLayout.setOnRefreshListener(() -> {
//            listReportRealtime();
            binding.refreshLayout.setRefreshing(false);
        });

        return view;

    }
}