package id.sch.smkn1batukliang.inventory.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import id.sch.smkn1batukliang.inventory.MainActivity;
import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.utils.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    boolean isEmptyFields = false;
    private String email, password;
    private FirebaseAuth auth;
    private ActivitySignInBinding binding;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new CustomProgressDialog(SignInActivity.this);

        auth = FirebaseAuth.getInstance();

        binding.tvForgetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
            startActivity(intent);
        });

        binding.btnSignIn.setOnClickListener(v -> {
            email = Objects.requireNonNull(binding.tietEmail.getText()).toString().trim();
            password = Objects.requireNonNull(binding.tietPassword.getText()).toString();

            isEmptyFields = validateFields();
        });

        binding.tvSignInToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);
        });

    }

    private boolean validateFields() {
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
        } else {
            binding.tilPassword.setErrorEnabled(false);
        }

        signIn();
        return true;
    }

    private void signIn() {
        progressDialog.ShowProgressDialog();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "signIn: successfully " + email);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "signIn: failure ", task.getException());
                Toast.makeText(getApplicationContext(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}