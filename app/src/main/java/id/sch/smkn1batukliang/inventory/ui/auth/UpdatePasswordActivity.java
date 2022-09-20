package id.sch.smkn1batukliang.inventory.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.addition.utilities.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.ActivityUpdatePasswordBinding;
import id.sch.smkn1batukliang.inventory.ui.users.ProfileActivity;

public class UpdatePasswordActivity extends AppCompatActivity {

    private static final String TAG = "UpdatePasswordActivity";
    boolean isEmptyFields = false;
    private FirebaseUser user;
    private String email, password, newPassword;
    private ActivityUpdatePasswordBinding binding;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdatePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new CustomProgressDialog(UpdatePasswordActivity.this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.btnUpdate.setOnClickListener(v -> {
            email = Objects.requireNonNull(binding.tietEmail.getText()).toString().trim();
            password = Objects.requireNonNull(binding.tietPassword.getText()).toString().trim();
            newPassword = Objects.requireNonNull(binding.tietNewPassword.getText()).toString();

            isEmptyFields = validateFields();
        });
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
                    Toast.makeText(getApplicationContext(), getString(R.string.successfully), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(getApplicationContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    progressDialog.DismissProgressDialog();
                    Log.w(TAG, "updatePassword : failure ", e);
                    Toast.makeText(getApplicationContext(), getString(R.string.failed) + email, Toast.LENGTH_SHORT).show();
                });
            } else {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "updatePassword: failure ", task.getException());
                Toast.makeText(getApplicationContext(), getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}