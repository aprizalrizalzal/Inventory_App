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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import id.sch.smkn1batukliang.inventory.databinding.FragmentHelpBinding;
import id.sch.smkn1batukliang.inventory.model.users.Users;
import id.sch.smkn1batukliang.inventory.utili.CustomProgressDialog;

public class HelpFragment extends Fragment {

    private static final String TAG = "HelpFragment";
    private FragmentHelpBinding binding;
    private CustomProgressDialog progressDialog;
    private DatabaseReference databaseReferenceUsers;

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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUsers = database.getReference("users");

        contactAdmin();

        return view;
    }

    private void contactAdmin() {
        progressDialog.ShowProgressDialog();
        databaseReferenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: Users");
                progressDialog.DismissProgressDialog();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    if (users != null && users.getLevel().equals(getString(R.string.admin))) {
                        Glide.with(requireContext()).load(users.getPhotoLink())
                                .placeholder(R.drawable.ic_baseline_account_circle)
                                .into(binding.imgUsers);
                        binding.tvUsername.setText(users.getUsername());
                        binding.tvEmployeeIdNumber.setText(users.getEmployeeIdNumber());
                        binding.tvEmail.setText(users.getEmail());
                        if (binding.tvWhatsappNumber.getText().equals("")) {
                            binding.tvWhatsappNumber.setText(getString(R.string.no_whatsapp_number));
                        } else {
                            binding.tvWhatsappNumber.setText(users.getWhatsappNumber());
                        }
                        binding.tvWhatsappNumber.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://wa.me/" + users.getWhatsappNumber()));
                            requireActivity().startActivity(intent);
                        });
                        binding.tvLevel.setText(users.getLevel());
                        binding.tvPosition.setText(users.getPosition());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled: Users", error.toException());
                progressDialog.DismissProgressDialog();
            }
        });
    }
}