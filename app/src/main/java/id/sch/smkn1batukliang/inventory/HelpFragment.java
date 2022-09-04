package id.sch.smkn1batukliang.inventory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Type;

import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.FragmentHelpBinding;
import id.sch.smkn1batukliang.inventory.model.users.Users;

public class HelpFragment extends Fragment {

    private FragmentHelpBinding binding;
    private CustomProgressDialog progressDialog;
    private DatabaseReference referenceLevels;

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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        referenceLevels = database.getReference("levels");

        progressDialog = new CustomProgressDialog(requireActivity());

        binding.refreshLayout.setOnRefreshListener(() -> {
            contactAdmin();
            binding.refreshLayout.setRefreshing(false);
        });

        return view;
    }

    private void contactAdmin() {
        progressDialog.ShowProgressDialog();
        referenceLevels.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users lUsers = dataSnapshot.child("users").getValue(Users.class);
                        if (lUsers != null && lUsers.getLevel().equals(getString(R.string.admin))) {
                            viewHelp(lUsers);
                        }
                    }
                }
                progressDialog.DismissProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.DismissProgressDialog();
                Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void viewHelp(Users lUsers) {
        Glide.with(requireContext()).load(lUsers.getPhotoLink())
                .placeholder(R.drawable.ic_baseline_account_circle)
                .into(binding.imgUsers);
        binding.tietUsername.setText(lUsers.getUsername());
        binding.tietUsername.setInputType(InputType.TYPE_NULL);
        binding.tietEmployeeIdNumber.setText(lUsers.getEmployeeIdNumber());
        binding.tietEmployeeIdNumber.setInputType(InputType.TYPE_NULL);
        binding.tietEmail.setText(lUsers.getEmail());
        binding.tietEmail.setInputType(InputType.TYPE_NULL);
        binding.tietWhatsappNumber.setText(lUsers.getWhatsappNumber());
        binding.tietWhatsappNumber.setInputType(InputType.TYPE_NULL);
        binding.tietWhatsappNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://wa.me/"+lUsers.getWhatsappNumber()));
                requireActivity().startActivity(intent);
            }
        });

        if (!lUsers.isEmailVerification()) {
            binding.tilEmail.setError(getString(R.string.email_not_verified));
        } else {
            binding.tilEmail.setErrorEnabled(false);
        }

        binding.tietLevel.setText(lUsers.getLevel());
        binding.tietLevel.setInputType(InputType.TYPE_NULL);
        binding.tietPosition.setText(lUsers.getPosition());
        binding.tietPosition.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public void onStart() {
        contactAdmin();
        super.onStart();
    }
}