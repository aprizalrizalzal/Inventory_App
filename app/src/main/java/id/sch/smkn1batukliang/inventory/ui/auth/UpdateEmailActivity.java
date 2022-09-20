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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.addition.utilities.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.ActivityUpdateEmailBinding;
import id.sch.smkn1batukliang.inventory.ui.users.ProfileActivity;

public class UpdateEmailActivity extends AppCompatActivity {

    private static final String TAG = "UpdateEmailActivity";
    boolean isEmptyFields = false;
    private FirebaseUser user;
    private String authId, email, newEmail, password;
    private FirebaseFirestore firestore;
    private ActivityUpdateEmailBinding binding;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new CustomProgressDialog(UpdateEmailActivity.this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        authId = user.getUid();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.btnUpdate.setOnClickListener(v -> {
            email = Objects.requireNonNull(binding.tietEmail.getText()).toString().trim();
            newEmail = Objects.requireNonNull(binding.tietNewEmail.getText()).toString().trim();
            password = Objects.requireNonNull(binding.tietPassword.getText()).toString();

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
                    updateNewEmail(newEmail);
                    Toast.makeText(getApplicationContext(), getString(R.string.successfully) + newEmail, Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
                    progressDialog.DismissProgressDialog();
                    Log.w(TAG, "updateEmail: failure ", e);
                    Toast.makeText(getApplicationContext(), getString(R.string.failed) + email, Toast.LENGTH_SHORT).show();
                });
            } else {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "updateEmail: failure ", task.getException());
                Toast.makeText(getApplicationContext(), getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNewEmail(String newEmail) {
        progressDialog.ShowProgressDialog();
        DocumentReference documentReference = firestore.collection("users").document(authId);
        documentReference.update("email", newEmail).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "updateNewEmail: successfully " + newEmail);
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "updateNewEmail: failure", e);
            Toast.makeText(getApplicationContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
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