package com.example.collegeadmission;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class NearMeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private RecyclerView recyclerView;
    private TextView tvBack, tvStatus;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedClient;
    private final ArrayList<NearbyCollege> nearbyList = new ArrayList<>();
    private NearbyAdapter adapter;

    public static class NearbyCollege {
        public String name;
        public String location;
        public String state;
        public String type;
        public double latitude;
        public double longitude;
        public double distanceKm; // computed at runtime

        public NearbyCollege() {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_me);

        recyclerView = findViewById(R.id.recyclerView);
        tvBack       = findViewById(R.id.tvBack);
        tvStatus     = findViewById(R.id.tvStatus);
        progressBar  = findViewById(R.id.progressBar);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        adapter = new NearbyAdapter(nearbyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        tvBack.setOnClickListener(v -> finish());
        checkPermissionAndLocate();
    }

    private void checkPermissionAndLocate() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getLocationAndLoad();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocationAndLoad();
        } else {
            tvStatus.setText("❌ Location permission denied. Cannot show nearby colleges.");
        }
    }

    private void getLocationAndLoad() {
        tvStatus.setText("📡 Getting your location...");
        progressBar.setVisibility(View.VISIBLE);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                tvStatus.setText("📍 Location found! Finding nearby colleges...");
                loadNearbyColleges(location.getLatitude(), location.getLongitude());
            } else {
                progressBar.setVisibility(View.GONE);
                tvStatus.setText("⚠️ Could not get location. Try turning on GPS.");
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            tvStatus.setText("⚠️ Location error: " + e.getMessage());
        });
    }

    private void loadNearbyColleges(double userLat, double userLng) {
        FirebaseDatabase.getInstance().getReference("college_locations")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        nearbyList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            NearbyCollege c = ds.getValue(NearbyCollege.class);
                            if (c != null) {
                                float[] result = new float[1];
                                Location.distanceBetween(
                                        userLat, userLng,
                                        c.latitude, c.longitude, result);
                                c.distanceKm = result[0] / 1000.0;
                                nearbyList.add(c);
                            }
                        }
                        // Sort by distance
                        Collections.sort(nearbyList,
                                Comparator.comparingDouble(a -> a.distanceKm));

                        progressBar.setVisibility(View.GONE);
                        tvStatus.setText("🏫 " + nearbyList.size()
                                + " colleges found near you");
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        tvStatus.setText("⚠️ Error loading data.");
                    }
                });
    }

    static class NearbyAdapter extends RecyclerView.Adapter<NearbyAdapter.VH> {
        private final ArrayList<NearbyCollege> list;

        NearbyAdapter(ArrayList<NearbyCollege> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_near_me, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            NearbyCollege c = list.get(position);
            h.tvRank.setText("#" + (position + 1));
            h.tvName.setText(c.name);
            h.tvLocation.setText("📍 " + c.location + ", " + c.state);
            h.tvDistance.setText(
                    String.format("%.1f km away", c.distanceKm));
            h.tvType.setText(c.type);

            boolean isGovt = "Government".equals(c.type);
            h.tvType.setBackgroundColor(isGovt ? 0xFFE3F2FD : 0xFFFFF3E0);
            h.tvType.setTextColor(isGovt ? 0xFF1565C0 : 0xFFE65100);
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvRank, tvName, tvLocation, tvDistance, tvType;
            VH(View v) {
                super(v);
                tvRank     = v.findViewById(R.id.tvRank);
                tvName     = v.findViewById(R.id.tvName);
                tvLocation = v.findViewById(R.id.tvLocation);
                tvDistance = v.findViewById(R.id.tvDistance);
                tvType     = v.findViewById(R.id.tvType);
            }
        }
    }
}