package id.sch.smkn1batukliang.inventory.ui.inventories.card.goods;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import id.sch.smkn1batukliang.inventory.databinding.FragmentListGoodsBinding;


public class ListGoodsFragment extends Fragment {

    private FragmentListGoodsBinding binding;
    private View view;

    public ListGoodsFragment() {
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
        binding = FragmentListGoodsBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        return view;
    }
}