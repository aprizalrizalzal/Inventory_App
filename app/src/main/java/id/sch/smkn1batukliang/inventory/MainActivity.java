package id.sch.smkn1batukliang.inventory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;
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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.databinding.ActivityMainBinding;
import id.sch.smkn1batukliang.inventory.model.users.Users;
import id.sch.smkn1batukliang.inventory.model.users.levels.Levels;
import id.sch.smkn1batukliang.inventory.ui.auth.SignInActivity;

public class MainActivity extends AppCompatActivity {

    private CustomProgressDialog progressDialog;
    private MenuItem itemUpDownReport, itemReport, itemSignOut;
    private NavigationView navigationView;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String authId, authEmail;
    private ImageView imgNavUser;
    private TextView username, email;
    private DocumentReference documentReferenceUser;
    private DatabaseReference referenceLevels;

    @SuppressLint({"UseCompatLoadingForDrawables", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new CustomProgressDialog(MainActivity.this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReferenceUsers = firestore.collection("users");

        if (user != null) {
            authId = user.getUid();
            authEmail = user.getEmail();
            documentReferenceUser = collectionReferenceUsers.document(authId);
        } else {
            reload();
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        referenceLevels = database.getReference("levels");

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawerLayout = binding.drawerLayout;
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        navigationView = binding.navView;
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_grid_placement_for_procurement,
                R.id.nav_list_placement,
                R.id.nav_home).setOpenableLayout(drawerLayout).build();

        navController = Navigation.findNavController(
                MainActivity.this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(
                MainActivity.this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        imgNavUser = navigationView.getHeaderView(0).findViewById(R.id.img_nav_user);
        username = navigationView.getHeaderView(0).findViewById(R.id.tv_nav_username);
        email = navigationView.getHeaderView(0).findViewById(R.id.tv_nav_email);
        ImageButton imgBtnSettings = navigationView.getHeaderView(0)
                .findViewById(R.id.img_btn_nav_settings);
        imgBtnSettings.setOnClickListener(v -> navController.navigate(R.id.nav_profile));

    }

    private void changeFirestoreUser() {
        documentReferenceUser.get().addOnSuccessListener(snapshot -> {
            Users users = snapshot.toObject(Users.class);
            if (users != null) {
                viewFirestoreUsers();
            } else {
                createFirestoreUsers();
            }
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createFirestoreUsers() {
        progressDialog.ShowProgressDialog();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatId =
                new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        String dateId = simpleDateFormatId.format(calendar.getTime());

        Users users = new Users(authId, authEmail, false, "", "", "", "", dateId, "");
        documentReferenceUser
                .set(users)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.DismissProgressDialog();
                    viewFirestoreUsers();
                })
                .addOnFailureListener(e -> {
                    progressDialog.DismissProgressDialog();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void viewFirestoreUsers() {
        progressDialog.ShowProgressDialog();
        Menu nav_Menu = navigationView.getMenu();
        documentReferenceUser.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Users users = snapshot.toObject(Users.class);
                if (users != null) {
                    Glide.with(getApplicationContext())
                            .load(users.getPhotoLink())
                            .placeholder(R.drawable.ic_baseline_account_circle)
                            .into(imgNavUser);
                    username.setText(users.getUsername());
                    email.setText(users.getEmail());
                    if (users.getEmail().equals(getString(R.string.default_email))) {
                        nav_Menu.findItem(R.id.nav_sub_data).setVisible(true);
                        nav_Menu.findItem(R.id.nav_sub_manage).setVisible(true);
                        nav_Menu.findItem(R.id.nav_sub_addition).setVisible(true);
                    } else {
                        changeLevel(nav_Menu, users);
                    }
                }
            }
            progressDialog.DismissProgressDialog();
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void changeLevel(Menu nav_Menu, Users users) {
        referenceLevels.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Levels levels = dataSnapshot.getValue(Levels.class);
                        if (levels != null) {
                            if (users.getAuthId().equals(levels.getUsers().getAuthId())) {
                                if (users.getLevel() != null && users.getLevel().equals(getString(R.string.admin))) {
                                    nav_Menu.findItem(R.id.nav_sub_data).setVisible(true);
                                    nav_Menu.findItem(R.id.nav_sub_manage).setVisible(true);
                                    nav_Menu.findItem(R.id.nav_sub_addition).setVisible(true);
                                } else if (users.getLevel() != null && users.getLevel().equals(getString(R.string.teacher))) {
                                    nav_Menu.findItem(R.id.nav_sub_data).setVisible(true);
                                    nav_Menu.findItem(R.id.nav_list_placement).setVisible(false);
                                    nav_Menu.findItem(R.id.nav_sub_manage).setVisible(false);
                                    nav_Menu.findItem(R.id.nav_sub_addition).setVisible(true);
                                } else if (users.getLevel() != null
                                        && users.getLevel().equals(getString(R.string.team_leader))
                                        || users.getLevel().equals(getString(R.string.vice_principal))
                                        || users.getLevel().equals(getString(R.string.principal))) {
                                    nav_Menu.findItem(R.id.nav_sub_data).setVisible(true);
                                    nav_Menu.findItem(R.id.nav_list_placement).setVisible(false);
                                    nav_Menu.findItem(R.id.nav_sub_manage).setVisible(true);
                                    nav_Menu.findItem(R.id.nav_list_user).setVisible(false);
                                    nav_Menu.findItem(R.id.nav_list_level).setVisible(false);
                                    nav_Menu.findItem(R.id.nav_sub_addition).setVisible(true);
                                }
                            }
                        }else {
                            nav_Menu.findItem(R.id.nav_sub_data).setVisible(true);
                            nav_Menu.findItem(R.id.nav_list_placement).setVisible(false);
                            nav_Menu.findItem(R.id.nav_sub_addition).setVisible(true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.DismissProgressDialog();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void reload() {
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflaterNav = getMenuInflater();
        inflaterNav.inflate(R.menu.menu_main_nav, menu);

        changeNavDestination(menu);

        return true;
    }

    private void changeNavDestination(Menu menu) {
        itemUpDownReport = menu.findItem(R.id.action_up_down_report);
        itemReport = menu.findItem(R.id.action_report);
        itemSignOut = menu.findItem(R.id.action_sign_out);

        navController.addOnDestinationChangedListener((navController, navDestination, bundle) -> {
            int id = navDestination.getId();
            if (id == R.id.nav_grid_placement_for_procurement) {
                navigationView.setCheckedItem(R.id.nav_grid_placement_for_procurement);
            }
            if (id == R.id.nav_home
                    || id == R.id.nav_grid_placement_for_procurement
                    || id == R.id.nav_list_placement
                    || id == R.id.nav_list_user
                    || id == R.id.nav_list_report
                    || id == R.id.nav_list_level) {
                itemSignOut.setVisible(true);
                itemSignOut.setOnMenuItemClickListener(item -> {
                    auth.signOut();
                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    startActivity(intent);
                    finish();
                    return false;
                });
            } else if (id == R.id.nav_list_procurement) {
                itemReport.setVisible(true);
                itemUpDownReport.setVisible(true);
            } else if (id == R.id.nav_edit_report
                    || id == R.id.nav_add_or_edit_placement
                    || id == R.id.nav_add_or_edit_level) {
                itemSignOut.setVisible(false);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(
                MainActivity.this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        if (user != null) {
            changeFirestoreUser();
        } else {
            reload();
        }
        super.onStart();
    }
}