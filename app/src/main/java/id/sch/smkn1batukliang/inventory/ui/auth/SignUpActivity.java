package id.sch.smkn1batukliang.inventory.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.ActivitySignUpBinding;

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

        progressDialog = new CustomProgressDialog(SignUpActivity.this);

        auth = FirebaseAuth.getInstance();

        binding.btnSignUp.setOnClickListener(v -> {
            email = Objects.requireNonNull(binding.tietEmail.getText()).toString().trim();
            password = Objects.requireNonNull(binding.tietPassword.getText()).toString();
            confirmPassword = Objects.requireNonNull(binding.tietConfirmPassword.getText()).toString();

            isEmptyFields = validateFields(email, password, confirmPassword);
        });

        binding.tvSignUpToSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateFields(String email, String password, String confirmPassword) {
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.email_required));
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
                Toast.makeText(getApplicationContext(), getString(R.string.registration_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
