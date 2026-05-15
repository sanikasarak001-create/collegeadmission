package com.example.collegeadmission;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class BookmarkActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvBack, tvEmpty;
    private ProgressBar progressBar;
    private final ArrayList<College> bookmarkedList = new ArrayList<>();
    private CollegeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        recyclerView  = findViewById(R.id.recyclerView);
        tvBack        = findViewById(R.id.tvBack);
        tvEmpty       = findViewById(R.id.tvEmpty);
        progressBar   = findViewById(R.id.progressBar);

        adapter = new CollegeAdapter(this, bookmarkedList, college -> {
            // Optional: open detail on click
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        tvBack.setOnClickListener(v -> finish());
        loadBookmarks();
    }

    private void loadBookmarks() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        progressBar.setVisibility(View.VISIBLE);

        // Step 1: Get user's saved college IDs
        FirebaseDatabase.getInstance().getReference("bookmarks")
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot bookmarkSnap) {
                        bookmarkedList.clear();
                        if (!bookmarkSnap.exists()) {
                            progressBar.setVisibility(View.GONE);
                            tvEmpty.setVisibility(View.VISIBLE);
                            return;
                        }

                        // Step 2: For each saved ID, fetch college details
                        long total = bookmarkSnap.getChildrenCount();
                        final long[] loaded = {0};

                        for (DataSnapshot ds : bookmarkSnap.getChildren()) {
                            String collegeId = ds.getKey();
                            if (collegeId == null) { loaded[0]++; continue; }

                            FirebaseDatabase.getInstance()
                                    .getReference("colleges")
                                    .child(collegeId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snap) {
                                            College c = snap.getValue(College.class);
                                            if (c != null) bookmarkedList.add(c);
                                            loaded[0]++;
                                            if (loaded[0] >= total) {
                                                progressBar.setVisibility(View.GONE);
                                                adapter.notifyDataSetChanged();
                                                if (bookmarkedList.isEmpty()) {
                                                    tvEmpty.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                            loaded[0]++;
                                        }
                                    });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}