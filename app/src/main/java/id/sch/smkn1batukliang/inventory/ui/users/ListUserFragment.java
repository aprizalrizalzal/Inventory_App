package id.sch.smkn1batukliang.inventory.ui.users;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.adapter.ListUserAdapter;
import id.sch.smkn1batukliang.inventory.databinding.FragmentListUsersBinding;
import id.sch.smkn1batukliang.inventory.model.users.Users;
import id.sch.smkn1batukliang.inventory.utili.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.utili.RecyclerViewEmptyData;

public class ListUserFragment extends Fragment {

    public static final String EXTRA_USERS = "extra_user";
    private static final String TAG = "ListUserFragment";
    private final ArrayList<Users> listUser = new ArrayList<>();
    private FragmentListUsersBinding binding;
    private View view;
    private DatabaseReference databaseReferenceUsers;
    private ListUserAdapter adapter;
    private CustomProgressDialog progressDialog;

    public ListUserFragment() {
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
        binding = FragmentListUsersBinding.inflate(getLayoutInflater(), container, false);
        view = binding.getRoot();

        progressDialog = new CustomProgressDialog(getActivity());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUsers = database.getReference("users");

        adapter = new ListUserAdapter();
        binding.tvEmptyData.setText(getString(R.string.no_data_available_users));
        adapter.registerAdapterDataObserver(new RecyclerViewEmptyData(binding.rvUsers, binding.tvEmptyData));

        binding.rvUsers.setHasFixedSize(true);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUsers.setAdapter(adapter);

        binding.refreshLayout.setOnRefreshListener(() -> {
            listRealtimeDatabaseUsers();
            binding.refreshLayout.setRefreshing(false);
        });

        return view;
    }

    private void listRealtimeDatabaseUsers() {
        listUser.clear();
        progressDialog.ShowProgressDialog();
        databaseReferenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: Users");
                progressDialog.DismissProgressDialog();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    if (users != null) {
                        listUser.add(users);
                        adapter.setListUser(listUser);
                    }
                    adapter.setOnItemClickCallback(detailUsers -> showSelectedUsers(detailUsers));
                    adapter.setOnItemClickCallbackDelete(deleteUsers -> deleteSelectedUsers(deleteUsers));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled: Users", error.toException());
                progressDialog.DismissProgressDialog();
            }
        });
    }

    private void showSelectedUsers(Users users) {
        Bundle bundle = new Bundle();

        bundle.putParcelable(EXTRA_USERS, users);
        Navigation.findNavController(view).navigate(R.id.action_nav_list_user_to_nav_profile, bundle);
    }

    private void deleteSelectedUsers(Users users) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.delete)).setMessage(getString(R.string.f_delete_users, users.getUsername())).setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel())
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> deleteUsers());
        builder.show();
    }

    private void deleteUsers() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.sorry)).setMessage(getString(R.string.firebase_billing_plans, getString(R.string.spark), getString(R.string.blaze))).setCancelable(false)
                .setNeutralButton(getString(R.string.yes), (dialog, id) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onStart() {
        listRealtimeDatabaseUsers();
        super.onStart();
    }
}