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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.databinding.FragmentProfileBinding;
import id.sch.smkn1batukliang.inventory.model.Users;
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
    private DocumentReference documentReferenceUser;
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

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReferenceUsers = firestore.collection("users");

        if (user != null) {
            authId = user.getUid();
            emailVerified = user.isEmailVerified();
        } else {
            reload();
        }
        documentReferenceUser = collectionReferenceUsers.document(authId);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_out_nav, menu);
                if (extraUsers != null) {
                    menu.findItem(R.id.action_update_password).setVisible(false);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_update_password) {
                    Navigation.findNavController(view).navigate(R.id.action_nav_profile_to_update_password);

                }
                return false;
            }

        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        if (getArguments() != null) {
            extraUsers = getArguments().getParcelable(EXTRA_USERS);
        }

        resultLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                imageUrl = result;
                Glide.with(view).load(result).into(binding.imgUsers);
                binding.fabAddImage.setVisibility(View.GONE);
                binding.fabUploadImage.setVisibility(View.VISIBLE);
            } else {
                binding.fabAddImage.setVisibility(View.VISIBLE);
                binding.fabUploadImage.setVisibility(View.GONE);
            }
        });

        binding.fabAddImage.setOnClickListener(v -> selectImage());
        binding.fabUploadImage.setOnClickListener(v -> uploadImage());

        return view;
    }

    private void viewFirestoreUser() {
        progressDialog.ShowProgressDialog();
        documentReferenceUser.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.DismissProgressDialog();
                Log.d(TAG, "viewFirestoreUser: successfully " + documentReferenceUser.getId());
                Users users = task.getResult().toObject(Users.class);
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
                        updateEmailVerification(users);
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
            } else {
                progressDialog.DismissProgressDialog();
                Log.w(TAG, "viewFirestoreUser: failure ", task.getException());
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

    private void reload() {
        startActivity(new Intent(requireActivity(), SignInActivity.class));
        requireActivity().finish();
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
        documentReferenceUser.update("photoLink", downloadUri).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "updatePhotoLink: successfully " + downloadUri);
            Toast.makeText(requireContext(), getString(R.string.successfully), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "updatePhotoLink: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUsername() {
        progressDialog.ShowProgressDialog();
        documentReferenceUser.update("username", username).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "updateUsername: successfully " + username);
            Toast.makeText(requireContext(), getString(R.string.successfully), Toast.LENGTH_SHORT).show();
            binding.tilUsername.setEndIconVisible(false);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "updateUsername: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateEmployeeIdNumber() {
        progressDialog.ShowProgressDialog();
        documentReferenceUser.update("employeeIdNumber", employeeIdNumber).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "updateEmployeeIdNumber: successfully " + employeeIdNumber);
            Toast.makeText(requireContext(), getString(R.string.successfully), Toast.LENGTH_SHORT).show();
            binding.tilEmployeeIdNumber.setEndIconVisible(false);
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "updateEmployeeIdNumber: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
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

    private void updateEmailVerification(Users users) {
        progressDialog.ShowProgressDialog();
        documentReferenceUser.update("emailVerification", true).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "updateEmailVerification: successfully " + true);
            binding.tilEmail.setErrorEnabled(false);
            binding.tilEmail.setEndIconOnClickListener(v -> updateEmail(users));
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "updateEmailVerification: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateWhatsappNumber() {
        progressDialog.ShowProgressDialog();
        documentReferenceUser.update("whatsappNumber", whatsappNumber).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "updateWhatsappNumber: successfully " + whatsappNumber);
            binding.tilWhatsappNumber.setEndIconVisible(false);
            Toast.makeText(requireContext(), getString(R.string.successfully), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "updateWhatsappNumber: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    private void updatePosition() {
        progressDialog.ShowProgressDialog();
        documentReferenceUser.update("position", position).addOnSuccessListener(unused -> {
            progressDialog.DismissProgressDialog();
            Log.d(TAG, "updatePosition: successfully " + position);
            binding.tilPosition.setEndIconVisible(false);
            Toast.makeText(requireContext(), getString(R.string.successfully), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Log.w(TAG, "updatePosition: failure ", e);
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (extraUsers != null) {
            viewExtraUsers(extraUsers);
        } else {
            viewFirestoreUser();
        }
    }
}