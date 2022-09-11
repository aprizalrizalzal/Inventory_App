package id.sch.smkn1batukliang.inventory.ui.inventories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.FragmentListInventoriesBinding;
import id.sch.smkn1batukliang.inventory.ui.users.ProfileActivity;

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
            listInventoriesRealtime();
            binding.refreshLayout.setRefreshing(false);
        });

        return view;

    }

    private void listInventoriesRealtime() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.reminder)).setMessage("Masih Dalam pengembangan, pilih Ya untuk keluar").setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel())
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> requireActivity().finish());
        builder.show();
    }

    @Override
    public void onStart() {
        listInventoriesRealtime();
        super.onStart();
    }
}