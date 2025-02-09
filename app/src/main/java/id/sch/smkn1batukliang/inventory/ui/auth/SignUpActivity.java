package id.sch.smkn1batukliang.inventory.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.ActivitySignUpBinding;
import id.sch.smkn1batukliang.inventory.utils.CustomProgressDialog;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    boolean isEmptyFields = false;
    private String email, password, confirmPassword;
    private FirebaseAuth auth;
    private ActivitySignUpBinding binding;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progressDialog = new CustomProgressDialog(SignUpActivity.this);

        auth = FirebaseAuth.getInstance();

        binding.btnSignUp.setOnClickListener(v -> {
            email = Objects.requireNonNull(binding.tietEmail.getText()).toString().trim();
            password = Objects.requireNonNull(binding.tietPassword.getText()).toString();
            confirmPassword = Objects.requireNonNull(binding.tietConfirmPassword.getText()).toString();

            isEmptyFields = validateFields(email, password, confirmPassword);
        });

    }

    private boolean validateFields(String email, String password, String confirmPassword) {
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.email_required));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.email_format));
            return false;
        } else {
            binding.tilEmail.setErrorEnabled(false);
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.password_required));
            return false;
        } else if (password.length() < 8) {
            binding.tilPassword.setError(getString(R.string.password_length));
            return false;
        } else {
            binding.tilPassword.setErrorEnabled(false);
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.setError(getString(R.string.password_confirm_required));
            return false;
        } else if (!confirmPassword.equals(password)) {
            binding.tilConfirmPassword.setError(getString(R.string.invalid_confirm_password));
            return false;
        } else {
            binding.tilConfirmPassword.setErrorEnabled(false);
        }

        signUp();
        return true;
    }

    private void signUp() {
        progressDialog.ShowProgressDialog();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "signUp: successfully " + email);
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                finish();
            } else {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "signUp: failure ", task.getException());
                Toast.makeText(getApplicationContext(), R.string.registration_failed, Toast.LENGTH_SHORT).show();
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
