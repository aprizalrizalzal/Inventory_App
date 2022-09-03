package id.sch.smkn1batukliang.inventory;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.FragmentHelpBinding;

public class HelpFragment extends Fragment {

    private FragmentHelpBinding binding;
    private CustomProgressDialog progressDialog;

    public HelpFragment() {
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
        binding = FragmentHelpBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

        progressDialog = new CustomProgressDialog(requireActivity());

        binding.refreshLayout.setOnRefreshListener(() -> {
            viewHelp();
            binding.refreshLayout.setRefreshing(false);
        });

        return view;
    }

    private void viewHelp() {
        WebView webView = binding.webView;
        webView.setWebViewClient(new MyWebViewClient(progressDialog));
        webView.loadUrl("https://github.com/aprizalrizalzal");
    }

    private static class MyWebViewClient extends WebViewClient {

        private final CustomProgressDialog progressDialog;

        public MyWebViewClient(CustomProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressDialog.ShowProgressDialog();
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressDialog.DismissProgressDialog();
            super.onPageFinished(view, url);
        }
    }

    @Override
    public void onStart() {
        viewHelp();
        super.onStart();
    }
}