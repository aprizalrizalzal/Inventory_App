package id.sch.smkn1batukliang.inventory.ui.inventories.card.room;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.FragmentListRoomBinding;

public class ListRoomFragment extends Fragment {

    private FragmentListRoomBinding binding;
    private View view;

    public ListRoomFragment() {
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
        binding = FragmentListRoomBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        return view;
    }
}