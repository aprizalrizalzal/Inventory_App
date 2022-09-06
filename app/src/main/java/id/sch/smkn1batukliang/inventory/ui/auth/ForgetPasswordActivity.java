package id.sch.smkn1batukliang.inventory.ui.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.ActivityForgetPasswordBinding;

public class ForgetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgetPasswordActivity";
    boolean isEmptyFields = false;
    private ActivityForgetPasswordBinding binding;
    private String email;
    private FirebaseAuth auth;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new CustomProgressDialog(ForgetPasswordActivity.this);

        auth = FirebaseAuth.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.btnReset.setOnClickListener(v -> {
            email = Objects.requireNonNull(binding.tietEmail.getText()).toString().trim();
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
        resetPassword(email);
        return true;
    }

    private void resetPassword(String email) {
        progressDialog.ShowProgressDialog();
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "resetPassword: successfully");
                Toast.makeText(getApplicationContext(), getString(R.string.successfully), Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "resetPassword: failure", task.getException());
                Toast.makeText(getApplicationContext(), getString(R.string.unregistered_email), Toast.LENGTH_SHORT).show();
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