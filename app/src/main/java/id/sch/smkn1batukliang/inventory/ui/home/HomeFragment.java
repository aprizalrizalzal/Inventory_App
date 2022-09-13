package id.sch.smkn1batukliang.inventory.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.FragmentHomeBinding;
import id.sch.smkn1batukliang.inventory.ui.auth.SignInActivity;

public class HomeFragment extends Fragment {

    private FirebaseAuth auth;
    private View view;

    public HomeFragment() {
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
        FragmentHomeBinding binding = FragmentHomeBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        auth = FirebaseAuth.getInstance();

        WebView webView = binding.webView;
        webView.loadUrl("https://smkn1batukliang.sch.id/");

        binding.cardMenuProcurement.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_grid_placement_for_procurement));

        binding.cardMenuInventories.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_list_inventories));

        binding.cardMenuHelp.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_help));

        return view;
    }

}