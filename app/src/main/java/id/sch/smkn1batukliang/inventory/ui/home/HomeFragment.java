package id.sch.smkn1batukliang.inventory.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import id.sch.smkn1batukliang.inventory.databinding.FragmentHomeBinding;


public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentHomeBinding binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        String url = "https://smkn1batukliang.sch.id/";

        WebView webView = binding.webView;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);

        webView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        requireActivity().finish();
                    }
                    return true;
                }
            }
            return false;
        });

        binding.refreshLayout.setOnRefreshListener(() -> {
            webView.loadUrl(url);
            binding.refreshLayout.setRefreshing(false);
        });

        return view;
    }
}