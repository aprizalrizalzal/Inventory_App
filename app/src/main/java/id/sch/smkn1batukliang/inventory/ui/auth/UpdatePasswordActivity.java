package id.sch.smkn1batukliang.inventory.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.ActivityUpdatePasswordBinding;
import id.sch.smkn1batukliang.inventory.ui.users.ProfileActivity;

public class UpdatePasswordActivity extends AppCompatActivity {

    boolean isEmptyFields = false;
    private FirebaseUser user;
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
            String email = Objects.requireNonNull(binding.tietEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.tietPassword.getText()).toString().trim();
            String newPassword = Objects.requireNonNull(binding.tietNewPassword.getText()).toString();

            isEmptyFields = validateFields(email, password, newPassword);
        });
    }

    private boolean validateFields(String email, String password, String newPassword) {
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.email_required));
            return false;
        } else {
            binding.tilEmail.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(email)) {
            binding.tilPassword.setError(getString(R.string.password_required));
            return false;
        } else {
            binding.tilPassword.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilNewPassword.setError(getString(R.string.password_required));
            return false;
        } else {
            binding.tilNewPassword.setErrorEnabled(false);
        }

        updatePassword(email, password, newPassword);
        return true;
    }

    private void updatePassword(String email, String password, String newPassword) {
        progressDialog.ShowProgressDialog();
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPassword).addOnCompleteListener(command -> {
                    if (command.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), getString(R.string.successfully), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.DismissProgressDialog();
                }).addOnFailureListener(e -> {
                    progressDialog.DismissProgressDialog();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
            }
            progressDialog.DismissProgressDialog();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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