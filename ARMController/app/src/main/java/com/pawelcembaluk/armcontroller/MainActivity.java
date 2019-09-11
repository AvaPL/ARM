package com.pawelcembaluk.armcontroller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.pawelcembaluk.armcontroller.interfaces.DrawerEnabler;
import com.pawelcembaluk.armcontroller.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity implements DrawerEnabler {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawer = findViewById(R.id.drawer_layout);
        mAppBarConfiguration =
                new AppBarConfiguration.Builder(R.id.nav_controller, R.id.nav_devices)
                        .setDrawerLayout(mDrawer).build();
        initializeToolbar();
        initializeNavigation();
        readSettings();
    }

    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initializeNavigation() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void readSettings() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d(getClass().getSimpleName(), "Continuous commands delay: " + sharedPreferences.getInt(
                SettingsFragment.KEY_CONTINUOUS_COMMANDS_DELAY, 50)); //TODO: Use this setting.
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) ||
               super.onSupportNavigateUp();
    }

    @Override
    public void setDrawerEnabled(boolean isEnabled) {
        if (mDrawer == null) return;
        int lockMode =
                isEnabled ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        mDrawer.setDrawerLockMode(lockMode);
    }
}
