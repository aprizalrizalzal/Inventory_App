package id.sch.smkn1batukliang.inventory.ui.home;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import id.sch.smkn1batukliang.inventory.databinding.FragmentHomeBinding;
import id.sch.smkn1batukliang.inventory.utili.CustomProgressDialog;


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

        CustomProgressDialog progressDialog = new CustomProgressDialog(requireActivity());
        String url = "https://smkn1batukliang.sch.id/";

        WebView webView = binding.webView;
        webView.setWebViewClient(new MyWebViewClient(progressDialog));
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

    private static class MyWebViewClient extends WebViewClient {
        private final CustomProgressDialog progressDialog;

        public MyWebViewClient(CustomProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressDialog.ShowProgressDialog();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressDialog.DismissProgressDialog();
        }
    }
}