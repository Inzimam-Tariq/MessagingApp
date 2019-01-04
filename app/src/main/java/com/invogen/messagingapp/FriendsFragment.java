package com.invogen.messagingapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private DatabaseReference dbReference;
    private FirebaseRecyclerAdapter<Users, FindFriendsViewHolder> adapter;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        dbReference = FirebaseDatabase.getInstance().getReference().child(AppConstants.USERS_NODE);


        initViews(view);
//        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(dbReference, Users.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Users, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position,
                                            @NonNull Users model) {
                holder.username.setText(model.getUser_name());
                holder.userStatus.setText(model.getUser_status());
                if (!model.getUser_image().isEmpty())
                    Picasso.get().load(model.getUser_image())
                            .placeholder(R.drawable.default_user_img)
                            .error(R.drawable.default_user_img)
                            .resize(85, 85)
                            .into(holder.profileImageView);
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);

                return new FindFriendsViewHolder(view);
            }
        };
        mRecyclerView.setAdapter(adapter);
        adapter.startListening();

        return view;
    }

    private void initViews(View view) {
        mRecyclerView = view.findViewById(R.id.recycle_view);
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

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {

        TextView username, userStatus;
        CircleImageView profileImageView;

        public FindFriendsViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.tv_username);
            userStatus = itemView.findViewById(R.id.tv_user_status);
            profileImageView = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
