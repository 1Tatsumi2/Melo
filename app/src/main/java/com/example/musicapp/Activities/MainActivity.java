package com.example.musicapp.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.musicapp.Fragments.HomeFragment;
import com.example.musicapp.Fragments.LibraryFragment;
import com.example.musicapp.Fragments.NotificationFragment;
import com.example.musicapp.Fragments.SearchFragment;
import com.example.musicapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavView);

        // Load fragment đầu tiên
        loadFragment(new HomeFragment(), true);

        // Xử lý sự kiện khi chọn item trong BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.navHome) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.navSearch) {
                    selectedFragment = new SearchFragment();
                } else if (itemId == R.id.navNotification) {
                    selectedFragment = new NotificationFragment();
                } else if (itemId == R.id.navLibrary) {
                    selectedFragment = new LibraryFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment, false);
                }
                return true; // Trả về true để hiển thị trạng thái chọn
            }
        });
    }

    private void loadFragment(Fragment fragment, boolean isAppInitialized) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (isAppInitialized) {
            fragmentTransaction.add(R.id.frameLayout, fragment);
        } else {
            fragmentTransaction.replace(R.id.frameLayout, fragment);
        }

        fragmentTransaction.commit();
    }
}

