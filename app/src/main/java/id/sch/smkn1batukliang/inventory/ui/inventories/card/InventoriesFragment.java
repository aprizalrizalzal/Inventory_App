package id.sch.smkn1batukliang.inventory.ui.inventories.card;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.FragmentInventoriesBinding;


public class InventoriesFragment extends Fragment {

    private final int[] TAB_TITLES = new int[]{
            R.string.goods,
            R.string.room
    };
    private FragmentInventoriesBinding binding;
    private View view;

    public InventoriesFragment() {
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
        binding = FragmentInventoriesBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(requireActivity());
        binding.viewPager2.setAdapter(sectionsPagerAdapter);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(binding.tabs, binding.viewPager2, (tab, position) -> tab.setText(getResources().getString(TAB_TITLES[position])));
        tabLayoutMediator.attach();

        return view;
    }
}