package id.sch.smkn1batukliang.inventory.ui.inventories;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.FragmentListInventoriesBinding;

public class ListInventoriesFragment extends Fragment {

    private FragmentListInventoriesBinding binding;

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
        View view = binding.getRoot();

        binding.refreshLayout.setOnRefreshListener(() -> {
            inventories();
            binding.refreshLayout.setRefreshing(false);
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.reminder)).setMessage("Anda akan dialihkan ke website-nya langsung").setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                    dialog.cancel();
                    back();
                })
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                    inventories();
                    back();
                });
        builder.show();

        return view;

    }

    private void back() {
        requireActivity().onBackPressed();
    }

    private void inventories() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://inventaris.smkn1batukliang.sch.id/"));
        startActivity(intent);
    }
}