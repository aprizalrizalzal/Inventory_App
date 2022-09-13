package id.sch.smkn1batukliang.inventory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.FragmentHelpBinding;
import id.sch.smkn1batukliang.inventory.model.users.Users;

public class HelpFragment extends Fragment {

    private static final String TAG = "HelpFragment";
    private FragmentHelpBinding binding;
    private CustomProgressDialog progressDialog;
    private CollectionReference collectionReferenceUsers;

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

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        collectionReferenceUsers = firestore.collection("users");

        progressDialog = new CustomProgressDialog(requireActivity());

        return view;
    }

    private void contactAdmin() {
        progressDialog.ShowProgressDialog();
        collectionReferenceUsers.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "listUserFirestore: successfully " + collectionReferenceUsers.getId());
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    Users users = documentSnapshot.toObject(Users.class);
                    if (users != null && users.getLevel().equals(getString(R.string.admin))) {
                        viewHelp(users);
                    }
                }
            } else {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "get failed with " + task.getException());
            }
        });
    }

    private void viewHelp(Users users) {
        Glide.with(requireContext()).load(users.getPhotoLink())
                .placeholder(R.drawable.ic_baseline_account_circle)
                .into(binding.imgUsers);
        binding.tvUsername.setText(users.getUsername());
        binding.tvEmployeeIdNumber.setText(users.getEmployeeIdNumber());
        binding.tvEmail.setText(users.getEmail());
        binding.tvWhatsappNumber.setText(users.getWhatsappNumber());
        binding.tvWhatsappNumber.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + users.getWhatsappNumber()));
            requireActivity().startActivity(intent);
        });
        binding.tvLevel.setText(users.getLevel());
        binding.tvPosition.setText(users.getPosition());
    }

    @Override
    public void onStart() {
        contactAdmin();
        super.onStart();
    }
}