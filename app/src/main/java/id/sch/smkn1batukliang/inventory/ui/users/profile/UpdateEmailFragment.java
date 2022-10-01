package id.sch.smkn1batukliang.inventory.ui.users.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.FragmentUpdateEmailBinding;
import id.sch.smkn1batukliang.inventory.utili.CustomProgressDialog;

public class UpdateEmailFragment extends Fragment {

    private static final String TAG = "UpdateEmailFragment";
    boolean isEmptyFields = false;
    private FragmentUpdateEmailBinding binding;
    private FirebaseUser user;
    private DatabaseReference databaseReferenceUsers;
    private String authId, email, newEmail, password;
    private CustomProgressDialog progressDialog;
    private View view;

    public UpdateEmailFragment() {
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
        binding = FragmentUpdateEmailBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        progressDialog = new CustomProgressDialog(requireActivity());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUsers = database.getReference("users");

        if (user != null) {
            authId = user.getUid();
        } else {
            requireActivity().finish();
        }

        binding.btnUpdate.setOnClickListener(v -> {
            email = Objects.requireNonNull(binding.tietEmail.getText()).toString().trim();
            newEmail = Objects.requireNonNull(binding.tietNewEmail.getText()).toString().trim();
            password = Objects.requireNonNull(binding.tietPassword.getText()).toString();

            isEmptyFields = validateFields();
        });

        return view;
    }

    private boolean validateFields() {
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.email_required));
            return false;
        } else {
            binding.tilEmail.setErrorEnabled(false);
        }

        if (newEmail.isEmpty()) {
            binding.tilNewEmail.setError(getString(R.string.new_email_required));
            return false;
        } else {
            binding.tilNewEmail.setErrorEnabled(false);
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.password_required));
            return false;
        } else {
            binding.tilPassword.setErrorEnabled(false);
        }

        updateEmail();
        return true;
    }

    private void updateEmail() {
        progressDialog.ShowProgressDialog();
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updateEmail(newEmail).addOnSuccessListener(unused -> {
                    progressDialog.DismissProgressDialog();
                    Log.d(TAG, "updateEmail: successfully " + email);
                    updateNewEmail();
                    Toast.makeText(requireContext(), getString(R.string.successfully) + newEmail, Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
                    progressDialog.DismissProgressDialog();
                    Log.w(TAG, "updateEmail: failure ", e);
                    Toast.makeText(requireContext(), getString(R.string.failed) + email, Toast.LENGTH_SHORT).show();
                });
            } else {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "updateEmail: failure ", task.getException());
                Toast.makeText(requireContext(), getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNewEmail() {
        progressDialog.ShowProgressDialog();
        Map<String, Object> mapUsers = new HashMap<>();
        mapUsers.put("email", newEmail);

        databaseReferenceUsers.child(authId).updateChildren(mapUsers).addOnSuccessListener(unused -> {
            Log.d(TAG, "clearToken: Users");
            progressDialog.DismissProgressDialog();
            Navigation.findNavController(view).navigateUp();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "clearToken: Users", e);
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }
}