package id.sch.smkn1batukliang.inventory.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import id.sch.smkn1batukliang.inventory.databinding.FragmentHomeBinding;


public class HomeFragment extends Fragment {

    private View view;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentHomeBinding binding = FragmentHomeBinding.inflate(inflater, container, false);
        view = binding.getRoot();

//        WebView webView = binding.webView;
//        webView.loadUrl("https://smkn1batukliang.sch.id/");
//
//        binding.cardMenuProcurement.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_grid_placement_for_procurement));
//
//        binding.cardMenuInventories.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_inventories));
//
//        binding.cardMenuHelp.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_help));

        return view;
    }

}