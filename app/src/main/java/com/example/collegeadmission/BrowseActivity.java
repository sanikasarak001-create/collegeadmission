package com.example.collegeadmission;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Locale;

public class BrowseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvBack;
    private TextInputEditText etSearch;
    private Spinner spinnerFilter;
    private ProgressBar progressBar;

    private final ArrayList<College> allColleges    = new ArrayList<>();
    private final ArrayList<College> filteredList   = new ArrayList<>();
    private CollegeAdapter adapter;

    private final String[] filterOptions =
            {"All", "Government", "Private"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        recyclerView  = findViewById(R.id.recyclerView);
        tvBack        = findViewById(R.id.tvBack);
        etSearch      = findViewById(R.id.etSearch);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        progressBar   = findViewById(R.id.progressBar);

        adapter = new CollegeAdapter(this, filteredList, college -> {
            Intent intent = new Intent(this, CollegeDetailActivity.class);
            intent.putExtra("collegeId", college.getId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Filter spinner
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, filterOptions);
        filterAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                applyFilter();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Live search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                applyFilter();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        tvBack.setOnClickListener(v -> finish());
        loadAllColleges();
    }

    private void loadAllColleges() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance()
                .getReference("colleges")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        allColleges.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            College c = ds.getValue(College.class);
                            if (c != null) allColleges.add(c);
                        }
                        progressBar.setVisibility(View.GONE);
                        applyFilter();
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void applyFilter() {
        String query = etSearch.getText() != null
                ? etSearch.getText().toString().toLowerCase(Locale.getDefault()).trim()
                : "";
        String selectedType = spinnerFilter.getSelectedItem() != null
                ? spinnerFilter.getSelectedItem().toString()
                : "All";

        filteredList.clear();
        for (College c : allColleges) {
            boolean matchesSearch = c.getName().toLowerCase(Locale.getDefault())
                    .contains(query)
                    || c.getLocation().toLowerCase(Locale.getDefault()).contains(query);
            boolean matchesType = selectedType.equals("All")
                    || selectedType.equals(c.getType());
            if (matchesSearch && matchesType) filteredList.add(c);
        }
        adapter.notifyDataSetChanged();
    }
}