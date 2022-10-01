package id.sch.smkn1batukliang.inventory.ui.users.profile;

import static id.sch.smkn1batukliang.inventory.ui.users.ListUserFragment.EXTRA_USERS;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.FragmentProfileBinding;
import id.sch.smkn1batukliang.inventory.model.users.Users;
import id.sch.smkn1batukliang.inventory.ui.auth.SignInActivity;
import id.sch.smkn1batukliang.inventory.utili.CustomProgressDialog;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileActivity";
    private FragmentProfileBinding binding;
    private View view;
    private Users extraUsers;
    private ActivityResultLauncher<String> resultLauncher;
    private Uri imageUrl;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseReferenceUsers;
    private StorageReference storageReference;
    private String authId, username, employeeIdNumber, whatsappNumber, position;
    private boolean emailVerified;
    private CustomProgressDialog progressDialog;

    public ProfileFragment() {
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
        binding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        progressDialog = new CustomProgressDialog(requireActivity());

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUsers = database.getReference("users");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if (user != null) {
            authId = user.getUid();
            emailVerified = user.isEmailVerified();
        } else {
            requireActivity().finish();
        }

        if (getArguments() != null) {
            extraUsers = getArguments().getParcelable(EXTRA_USERS);
        }

        if (extraUsers != null) {
            viewExtraUsers(extraUsers);
        } else {
            viewRealtimeDatabaseUsers();
        }

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_out_nav, menu);
                if (extraUsers != null) {
                    menu.findItem(R.id.action_update_password).setVisible(false);
                    menu.findItem(R.id.action_sign_out).setVisible(false);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_update_password) {
                    Navigation.findNavController(view).navigate(R.id.action_nav_profile_to_update_password);

                } else if (menuItem.getItemId() == R.id.action_sign_out) {
                    updateTokenId();
                }
                return false;
            }

        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        resultLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            imageUrl = result;
            if (result != null) {
                Glide.with(view).load(imageUrl).into(binding.imgUsers);
                binding.fabAddImage.setVisibility(View.GONE);
                binding.fabUploadImage.setVisibility(View.VISIBLE);
            } else {
                binding.fabAddImage.setVisibility(View.VISIBLE);
                binding.fabUploadImage.setVisibility(View.GONE);
            }
        });

        binding.imgUsers.setOnClickListener(v -> selectImage());
        binding.fabAddImage.setOnClickListener(v -> selectImage());
        binding.fabUploadImage.setOnClickListener(v -> uploadImage());

        return view;
    }

    private void viewRealtimeDatabaseUsers() {
        progressDialog.ShowProgressDialog();
        databaseReferenceUsers.child(authId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: Users");
                progressDialog.DismissProgressDialog();
                Users users = snapshot.getValue(Users.class);
                if (users != null) {
                    Glide.with(view).load(users.getPhotoLink())
                            .placeholder(R.drawable.ic_baseline_account_circle)
                            .into(binding.imgUsers);

                    binding.tietUsername.setText(users.getUsername());
                    binding.tietUsername.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (s.length() >= users.getUsername().length() || s.length() <= users.getUsername().length()) {
                                binding.tilUsername.setEndIconDrawable(R.drawable.ic_baseline_save);
                                binding.tilUsername.setEndIconOnClickListener(v -> {
                                    username = Objects.requireNonNull(binding.tietUsername.getText()).toString().trim();
                                    updateUsername();
                                });
                            }
                        }
                    });

                    binding.tietEmployeeIdNumber.setText(users.getEmployeeIdNumber());
                    binding.tietEmployeeIdNumber.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (s.length() >= users.getEmployeeIdNumber().length() || s.length() <= users.getEmployeeIdNumber().length()) {
                                binding.tilEmployeeIdNumber.setEndIconDrawable(R.drawable.ic_baseline_save);
                                binding.tilEmployeeIdNumber.setEndIconOnClickListener(v -> {
                                    employeeIdNumber = Objects.requireNonNull(binding.tietEmployeeIdNumber.getText()).toString().trim();
                                    updateEmployeeIdNumber();
                                });
                            }
                        }
                    });

                    binding.tietEmail.setText(users.getEmail());
                    binding.tietEmail.setEnabled(false);

                    if (!emailVerified && !user.isEmailVerified()) {
                        binding.tilEmail.setError(getString(R.string.email_not_verified));
                        binding.tilEmail.setOnClickListener(v -> {
                            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                            builder.setTitle(getString(R.string.verification)).setMessage(getString(R.string.send_verification, users.getEmail())).setCancelable(false)
                                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel())
                                    .setPositiveButton(getString(R.string.send), (dialog, id) -> sendEmailVerification())
                                    .setNeutralButton(getString(R.string.update_email), (dialog, id) -> updateEmail(users));
                            builder.show();
                        });
                    } else {
                        binding.tilEmail.setErrorEnabled(false);
                    }

                    binding.tietWhatsappNumber.setText(users.getWhatsappNumber());
                    binding.tietWhatsappNumber.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (s.length() >= users.getWhatsappNumber().length() || s.length() <= users.getWhatsappNumber().length()) {
                                binding.tilWhatsappNumber.setEndIconDrawable(R.drawable.ic_baseline_save);
                                binding.tilWhatsappNumber.setEndIconOnClickListener(v -> {
                                    whatsappNumber = Objects.requireNonNull(binding.tietWhatsappNumber.getText()).toString().trim();
                                    updateWhatsappNumber();
                                });
                            }
                        }
                    });

                    binding.tietLevel.setText(users.getLevel());
                    binding.tietLevel.setEnabled(false);

                    binding.tietPosition.setText(users.getPosition());
                    binding.tietPosition.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (s.length() >= users.getPosition().length() || s.length() <= users.getPosition().length()) {
                                binding.tilPosition.setEndIconDrawable(R.drawable.ic_baseline_save);
                                binding.tilPosition.setEndIconOnClickListener(v -> {
                                    position = Objects.requireNonNull(binding.tietPosition.getText()).toString().trim();
                                    updatePosition();
                                });
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled: Users", error.toException());
                progressDialog.DismissProgressDialog();
            }
        });
    }

    private void updateEmail(Users users) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.update_email)).setMessage(getString(R.string.request_new_email, users.getEmail())).setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel())
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> Navigation.findNavController(view).navigate(R.id.action_nav_profile_to_update_email));
        builder.show();
    }

    private void viewExtraUsers(Users extraUsers) {
        Glide.with(view).load(extraUsers.getPhotoLink())
                .placeholder(R.drawable.ic_baseline_account_circle)
                .into(binding.imgUsers);
        binding.fabAddImage.setVisibility(View.GONE);
        binding.fabUploadImage.setVisibility(View.GONE);
        binding.tietUsername.setText(extraUsers.getUsername());
        binding.tietUsername.setEnabled(false);
        binding.tietEmployeeIdNumber.setText(extraUsers.getEmployeeIdNumber());
        binding.tietEmployeeIdNumber.setEnabled(false);
        binding.tietEmail.setText(extraUsers.getEmail());
        binding.tietEmail.setEnabled(false);
        binding.tietWhatsappNumber.setText(extraUsers.getWhatsappNumber());
        binding.tietWhatsappNumber.setEnabled(false);
        binding.tietLevel.setText(extraUsers.getLevel());
        binding.tietLevel.setEnabled(false);
        binding.tietPosition.setText(extraUsers.getPosition());
        binding.tietPosition.setEnabled(false);
    }

    private void selectImage() {
        resultLauncher.launch("image/*");
    }

    private void uploadImage() {
        progressDialog.ShowProgressDialog();
        String pathImage = "users/profile/" + authId + "/image/" + authId + ".jpg";
        storageReference.child(pathImage).putFile(imageUrl).addOnSuccessListener(taskSnapshot -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "uploadImage: successfully " + storageReference.getPath());
            getUriPhoto(pathImage);
            binding.fabAddImage.setVisibility(View.VISIBLE);
            binding.fabUploadImage.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "uploadImage: failure ", e);
            binding.fabAddImage.setVisibility(View.GONE);
            binding.fabUploadImage.setVisibility(View.VISIBLE);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void getUriPhoto(String pathImage) {
        progressDialog.ShowProgressDialog();
        storageReference.child(pathImage).getDownloadUrl().addOnSuccessListener(uri -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "getUriPhoto: successfully " + pathImage);
            String downloadUri = uri.toString();
            updatePhotoLink(downloadUri);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "getUriPhoto: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void updatePhotoLink(String downloadUri) {
        progressDialog.ShowProgressDialog();

        Map<String, Object> mapUsers = new HashMap<>();
        mapUsers.put("photoLink", downloadUri);

        databaseReferenceUsers.child(authId).updateChildren(mapUsers).addOnSuccessListener(unused -> {
            Log.d(TAG, "updatePhotoLink: Users");
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), R.string.successfully, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "updatePhotoLink: Users", e);
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUsername() {
        progressDialog.ShowProgressDialog();

        Map<String, Object> mapUsers = new HashMap<>();
        mapUsers.put("username", username);

        databaseReferenceUsers.child(authId).updateChildren(mapUsers).addOnSuccessListener(unused -> {
            Log.d(TAG, "updateUsername: Users");
            progressDialog.DismissProgressDialog();
            binding.tilUsername.setEndIconVisible(false);
            Toast.makeText(requireContext(), R.string.successfully, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "updateUsername: Users", e);
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateEmployeeIdNumber() {
        progressDialog.ShowProgressDialog();

        Map<String, Object> mapUsers = new HashMap<>();
        mapUsers.put("employeeIdNumber", employeeIdNumber);

        databaseReferenceUsers.child(authId).updateChildren(mapUsers).addOnSuccessListener(unused -> {
            Log.d(TAG, "updateEmployeeIdNumber: Users");
            progressDialog.DismissProgressDialog();
            binding.tilEmployeeIdNumber.setEndIconVisible(false);
            Toast.makeText(requireContext(), R.string.successfully, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "updateEmployeeIdNumber: Users", e);
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }

    private void sendEmailVerification() {
        progressDialog.ShowProgressDialog();
        user.sendEmailVerification().addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "sendEmailVerification: successfully " + user.getEmail());
            auth.signOut();
            Toast.makeText(requireContext(), getString(R.string.email_verification_sent, user.getEmail()), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireActivity(), SignInActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "sendEmailVerification: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateTokenId() {
        progressDialog.ShowProgressDialog();
        Map<String, Object> mapUsers = new HashMap<>();
        mapUsers.put("tokenId", "");

        databaseReferenceUsers.child(authId).updateChildren(mapUsers).addOnSuccessListener(unused -> {
            Log.d(TAG, "clearToken: Users");
            progressDialog.DismissProgressDialog();
            auth.signOut();
            Intent intent = new Intent(requireContext(), SignInActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "clearToken: Users", e);
            progressDialog.DismissProgressDialog();
        });
    }


    private void updateWhatsappNumber() {
        progressDialog.ShowProgressDialog();

        Map<String, Object> mapUsers = new HashMap<>();
        mapUsers.put("whatsappNumber", whatsappNumber);

        databaseReferenceUsers.child(authId).updateChildren(mapUsers).addOnSuccessListener(unused -> {
            Log.d(TAG, "updateWhatsappNumber: Users");
            progressDialog.DismissProgressDialog();
            binding.tilWhatsappNumber.setEndIconVisible(false);
            Toast.makeText(requireContext(), R.string.successfully, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "updateWhatsappNumber: Users", e);
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }

    private void updatePosition() {
        progressDialog.ShowProgressDialog();

        Map<String, Object> mapUsers = new HashMap<>();
        mapUsers.put("position", position);

        databaseReferenceUsers.child(authId).updateChildren(mapUsers).addOnSuccessListener(unused -> {
            Log.d(TAG, "updatePosition: Users");
            progressDialog.DismissProgressDialog();
            binding.tilPosition.setEndIconVisible(false);
            Toast.makeText(requireContext(), R.string.successfully, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "updatePosition: Users", e);
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }
}