package id.sch.smkn1batukliang.inventory.ui.home;

import android.graphics.Bitmap;
import android.os.Bundle;
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

    private View view;
    private CustomProgressDialog progressDialog;

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

        progressDialog = new CustomProgressDialog(requireActivity());
        String url = "https://smkn1batukliang.sch.id/";

        WebView webView = binding.webView;
        webView.setWebViewClient(new MyWebViewClient(progressDialog));
        webView.loadUrl(url);

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