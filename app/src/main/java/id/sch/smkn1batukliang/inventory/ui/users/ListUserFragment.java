package id.sch.smkn1batukliang.inventory.ui.users;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.adapter.users.ListUserAdapter;
import id.sch.smkn1batukliang.inventory.addition.CustomProgressDialog;
import id.sch.smkn1batukliang.inventory.addition.RecyclerViewEmptyData;
import id.sch.smkn1batukliang.inventory.databinding.FragmentListUsersBinding;
import id.sch.smkn1batukliang.inventory.model.users.Users;

public class ListUserFragment extends Fragment {

    public static final String EXTRA_USERS = "extra_user";
    private final ArrayList<Users> users = new ArrayList<>();
    private FragmentListUsersBinding binding;
    private View view;
    private FirebaseAuth auth;
    private CollectionReference collectionReferenceUsers;
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

        auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        collectionReferenceUsers = firestore.collection("users");

        adapter = new ListUserAdapter();
        adapter.registerAdapterDataObserver(new RecyclerViewEmptyData(binding.rvUsers, binding.tvEmptyData));

        binding.rvUsers.setHasFixedSize(true);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUsers.setAdapter(adapter);

        binding.refreshLayout.setOnRefreshListener(() -> {
            listUserFirestore();
            binding.refreshLayout.setRefreshing(false);
        });

        binding.fab.setOnClickListener(v -> {
            auth.signOut();
            Navigation.findNavController(view).navigate(R.id.action_nav_list_user_to_nav_sign_up);
            requireActivity().finish();
        });

        return view;
    }

    private void listUserFirestore() {
        progressDialog.ShowProgressDialog();
        collectionReferenceUsers.orderBy("username").get().addOnSuccessListener(queryDocumentSnapshots -> {
            users.clear();
            if (queryDocumentSnapshots.isEmpty()) {
                Toast.makeText(requireActivity(), getString(R.string.no_data_available), Toast.LENGTH_SHORT).show();
            } else {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    users.add(documentSnapshot.toObject(Users.class));
                    adapter.setListUser(users);
                }
                adapter.setOnItemClickCallback(this::showSelectedUsers);
                progressDialog.DismissProgressDialog();
            }
        }).addOnFailureListener(e -> {
            progressDialog.DismissProgressDialog();
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showSelectedUsers(Users model) {
        Bundle bundle = new Bundle();

        bundle.putParcelable(EXTRA_USERS, model);
        Navigation.findNavController(view).navigate(R.id.action_nav_list_user_to_nav_profile, bundle);
    }

    @Override
    public void onStart() {
        listUserFirestore();
        super.onStart();
    }
}