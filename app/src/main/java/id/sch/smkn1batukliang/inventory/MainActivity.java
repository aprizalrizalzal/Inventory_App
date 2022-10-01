package id.sch.smkn1batukliang.inventory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import id.sch.smkn1batukliang.inventory.databinding.ActivityMainBinding;
import id.sch.smkn1batukliang.inventory.model.levels.Levels;
import id.sch.smkn1batukliang.inventory.model.users.Users;
import id.sch.smkn1batukliang.inventory.ui.auth.SignInActivity;
import id.sch.smkn1batukliang.inventory.utili.CustomProgressDialog;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private CustomProgressDialog progressDialog;
    private NavigationView navigationView;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private FirebaseUser user;
    private FirebaseMessaging messaging;
    private String authId, authEmail, tokenId;
    private ImageView imgNavUser;
    private TextView username, email;
    private DatabaseReference databaseReferenceUsers, databaseReferenceLevels;

    @SuppressLint({"UseCompatLoadingForDrawables", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new CustomProgressDialog(MainActivity.this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        messaging = FirebaseMessaging.getInstance();
        user = auth.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUsers = database.getReference("users");
        databaseReferenceLevels = database.getReference("levels");

        if (user != null) {
            authId = user.getUid();
            authEmail = user.getEmail();
            changeRealtimeDatabaseUsers();
        } else {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
            finish();
        }

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawerLayout = binding.drawerLayout;

        navigationView = binding.navView;
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_grid_placement,
                R.id.nav_inventories,
                R.id.nav_list_report,
                R.id.nav_list_user,
                R.id.nav_list_placement,
                R.id.nav_list_level
        ).setOpenableLayout(drawerLayout).build();

        navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(MainActivity.this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        imgNavUser = navigationView.getHeaderView(0).findViewById(R.id.img_nav_user);
        username = navigationView.getHeaderView(0).findViewById(R.id.tv_nav_username);
        email = navigationView.getHeaderView(0).findViewById(R.id.tv_nav_email);
    }

    private void changeRealtimeDatabaseUsers() {
        databaseReferenceUsers.child(authId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: Users");
                if (snapshot.exists()) {
                    Users users = snapshot.getValue(Users.class);
                    if (users != null) {
                        viewRealtimeDatabaseUsers();
                    }
                } else {
                    createRealtimeDatabaseUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled: Users", error.toException());
            }
        });
    }

    private void refreshTokenIdUser() {
        messaging.getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tokenId = task.getResult();
                updateTokenId();
            } else {
                Log.w(TAG, "createTokenIdUser: failure ", task.getException());
            }
        });
    }

    private void updateTokenId() {
        progressDialog.ShowProgressDialog();
        Map<String, Object> mapUsers = new HashMap<>();
        mapUsers.put("tokenId", tokenId);

        databaseReferenceUsers.child(authId).updateChildren(mapUsers).addOnSuccessListener(unused -> {
            Log.d(TAG, "updateTokenId: Users");
            progressDialog.DismissProgressDialog();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "updateTokenId: Users", e);
            progressDialog.DismissProgressDialog();
        });
    }

    private void createRealtimeDatabaseUsers() {
        progressDialog.ShowProgressDialog();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatId = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        String dateId = simpleDateFormatId.format(calendar.getTime());

        Users users = new Users(authId, authEmail, "", "", "", "", dateId, "", "", "");
        databaseReferenceUsers.child(authId).setValue(users).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "createRealtimeDatabaseUsers: Users");
                progressDialog.DismissProgressDialog();
                viewRealtimeDatabaseUsers();
            } else {
                Log.w(TAG, "createRealtimeDatabaseUsers: Users ", task.getException());
                progressDialog.DismissProgressDialog();
            }
        });
    }

    private void viewRealtimeDatabaseUsers() {
        progressDialog.ShowProgressDialog();
        Menu nav_Menu = navigationView.getMenu();
        databaseReferenceUsers.child(authId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: Users");
                progressDialog.DismissProgressDialog();
                if (snapshot.exists()) {
                    Users users = snapshot.getValue(Users.class);
                    if (users != null) {
                        Glide.with(getApplicationContext())
                                .load(users.getPhotoLink())
                                .placeholder(R.drawable.ic_baseline_account_circle)
                                .into(imgNavUser);
                        username.setText(users.getUsername());
                        email.setText(users.getEmail());

                        if (users.getEmail().equals(getString(R.string.default_email))) {
                            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
                            builder.setTitle(getString(R.string.reminder)).setMessage(R.string.do_not_change_the_default_email).setCancelable(false)
                                    .setNeutralButton(getString(R.string.yes), (dialog, id) -> dialog.cancel());
                            builder.show();
                            menuAdmin(nav_Menu);
                        } else {
                            if (!user.isEmailVerified()) {
                                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
                                builder.setTitle(getString(R.string.reminder)).setMessage(R.string.email_must_be_verified).setCancelable(false)
                                        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                                            dialog.cancel();
                                            finish();
                                        })
                                        .setPositiveButton(getString(R.string.yes), (dialog, id) -> Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_content_main).navigate(R.id.nav_profile));
                                builder.show();
                            }
                            changeRealtimeDatabaseLevel(nav_Menu);
                        }
                        refreshTokenIdUser();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled: Users", error.toException());
                progressDialog.DismissProgressDialog();
            }
        });
    }

    private void menuAdmin(Menu nav_Menu) {
        nav_Menu.findItem(R.id.nav_home).setVisible(true);
        nav_Menu.findItem(R.id.nav_grid_placement).setVisible(true);
        nav_Menu.findItem(R.id.nav_list_report).setVisible(true);
        nav_Menu.findItem(R.id.nav_inventories).setVisible(true);
        nav_Menu.findItem(R.id.nav_list_user).setVisible(true);
        nav_Menu.findItem(R.id.nav_list_placement).setVisible(true);
        nav_Menu.findItem(R.id.nav_list_level).setVisible(true);
        nav_Menu.findItem(R.id.nav_profile).setVisible(true);
        nav_Menu.findItem(R.id.nav_help).setVisible(true);
    }

    private void changeRealtimeDatabaseLevel(Menu nav_Menu) {
        databaseReferenceLevels.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: levelsSuccessfully " + databaseReferenceLevels.getKey());
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Levels levels = dataSnapshot.getValue(Levels.class);
                        if (levels != null) {
                            if (authId.equals(levels.getAuthId())) {
                                nav_Menu.findItem(R.id.nav_home).setVisible(true);
                                nav_Menu.findItem(R.id.nav_grid_placement).setVisible(true);
                                nav_Menu.findItem(R.id.nav_inventories).setVisible(true);
                                nav_Menu.findItem(R.id.nav_list_report).setVisible(true);
                                nav_Menu.findItem(R.id.nav_list_user).setVisible(true);
                                nav_Menu.findItem(R.id.nav_list_placement).setVisible(false);
                                nav_Menu.findItem(R.id.nav_list_level).setVisible(true);
                                nav_Menu.findItem(R.id.nav_profile).setVisible(true);
                                nav_Menu.findItem(R.id.nav_help).setVisible(true);
                                if (levels.getLevelsItem().getLevel().equals(getString(R.string.admin))) {
                                    nav_Menu.findItem(R.id.nav_list_placement).setVisible(true);
                                } else if (levels.getLevelsItem().getLevel().equals(getString(R.string.principal))
                                        || levels.getLevelsItem().getLevel().equals(getString(R.string.team_leader))
                                        || levels.getLevelsItem().getLevel().equals(getString(R.string.vice_principal))) {
                                    nav_Menu.findItem(R.id.nav_list_user).setVisible(false);
                                    nav_Menu.findItem(R.id.nav_list_level).setVisible(false);
                                } else if (levels.getLevelsItem().getLevel().equals(getString(R.string.teacher))) {
                                    nav_Menu.findItem(R.id.nav_inventories).setVisible(false);
                                    nav_Menu.findItem(R.id.nav_list_report).setVisible(true);
                                    nav_Menu.findItem(R.id.nav_list_user).setVisible(false);
                                    nav_Menu.findItem(R.id.nav_list_level).setVisible(false);
                                }
                            } else {
                                nav_Menu.findItem(R.id.nav_home).setVisible(true);
                                nav_Menu.findItem(R.id.nav_grid_placement).setVisible(true);
                                nav_Menu.findItem(R.id.nav_list_report).setVisible(true);
                                nav_Menu.findItem(R.id.nav_profile).setVisible(true);
                                nav_Menu.findItem(R.id.nav_help).setVisible(true);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled: failureLevels", error.toException());
                Toast.makeText(getApplicationContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}