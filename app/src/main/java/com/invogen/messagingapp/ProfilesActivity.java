package com.invogen.messagingapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilesActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference profileDBReference;
    private StorageReference storageReference;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<Users, ProfilesViewHolder> adapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);
        this.mContext = this;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profiles");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, AddProfileActivity.class));
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initViews();
        profileDBReference = FirebaseDatabase.getInstance().getReference().child(AppConstants.PROFILES_NODE);
        profileDBReference.keepSynced(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(profileDBReference, Users.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Users, ProfilesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ProfilesViewHolder holder, int position,
                                            @NonNull Users model) {
                holder.username.setText(model.getUser_name());
                String userCircle = model.getUser_circle();
                if (userCircle != null) {
                    holder.userStatus.setText(userCircle);
                }
//                holder.userStatus.setText(model.getUser_status());
                String userImage = model.getUser_image();
                if (userImage != null && !userImage.isEmpty()) {
                    Picasso.get().load(model.getUser_image())
                            .placeholder(R.drawable.default_user_img)
                            .error(R.drawable.default_user_img)
                            .resize(85, 85)
                            .into(holder.profileImageView);
                }
            }

            @NonNull
            @Override
            public ProfilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);

                return new ProfilesViewHolder(view);
            }
        };
        mRecyclerView.setAdapter(adapter);
        adapter.startListening();

        AppUtils.hideKeyboard(this);
    }

    private void initViews() {
//        mToolbar
        mRecyclerView = findViewById(R.id.recycle_view);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    public static class ProfilesViewHolder extends RecyclerView.ViewHolder {

        TextView username, userStatus;
        CircleImageView profileImageView;

        public ProfilesViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.tv_username);
            userStatus = itemView.findViewById(R.id.tv_user_status);
            profileImageView = itemView.findViewById(R.id.users_profile_image);

        }
    }

}
