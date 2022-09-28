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

import java.util.Objects;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.FragmentUpdatePasswordBinding;
import id.sch.smkn1batukliang.inventory.utili.CustomProgressDialog;

public class UpdatePasswordFragment extends Fragment {

    private static final String TAG = "UpdatePasswordFragment";
    boolean isEmptyFields = false;
    private FragmentUpdatePasswordBinding binding;
    private FirebaseUser user;
    private String email, password, newPassword;
    private CustomProgressDialog progressDialog;
    private View view;

    public UpdatePasswordFragment() {
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
        binding = FragmentUpdatePasswordBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        progressDialog = new CustomProgressDialog(requireActivity());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        binding.btnUpdate.setOnClickListener(v -> {
            email = Objects.requireNonNull(binding.tietEmail.getText()).toString().trim();
            password = Objects.requireNonNull(binding.tietPassword.getText()).toString().trim();
            newPassword = Objects.requireNonNull(binding.tietNewPassword.getText()).toString();

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

        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.password_required));
            return false;
        } else {
            binding.tilPassword.setErrorEnabled(false);
        }

        if (newPassword.isEmpty()) {
            binding.tilNewPassword.setError(getString(R.string.new_password_required));
            return false;
        } else {
            binding.tilNewPassword.setErrorEnabled(false);
        }

        updatePassword();
        return true;
    }

    private void updatePassword() {
        progressDialog.ShowProgressDialog();
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPassword).addOnSuccessListener(unused -> {
                    progressDialog.DismissProgressDialog();
                    Log.d(TAG, "updatePassword: successfully " + email);
                    Toast.makeText(requireContext(), getString(R.string.successfully), Toast.LENGTH_LONG).show();
                    Navigation.findNavController(view).navigateUp();
                }).addOnFailureListener(e -> {
                    progressDialog.DismissProgressDialog();
                    Log.w(TAG, "updatePassword : failure ", e);
                    Toast.makeText(requireContext(), getString(R.string.failed) + email, Toast.LENGTH_SHORT).show();
                });
            } else {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "updatePassword: failure ", task.getException());
                Toast.makeText(requireContext(), getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }
}