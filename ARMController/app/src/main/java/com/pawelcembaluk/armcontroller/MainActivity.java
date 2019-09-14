package com.pawelcembaluk.armcontroller;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

    private static final String KEY_IS_CONNECTED = "is_connected";

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout mDrawer;
    private boolean isConnected = false; //TODO: Placeholder, replace with calls to Bluetooth class.

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
        loadInstanceState(savedInstanceState);
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

    private void loadInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        isConnected = savedInstanceState.getBoolean(KEY_IS_CONNECTED);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) ||
               super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        Drawable raspberryPiIcon = menu.getItem(0).getIcon();
        setIconColorByConnectionStatus(raspberryPiIcon);
        return true;
    }

    private void setIconColorByConnectionStatus(Drawable raspberryPiIcon) {
        int activeColor = getColor(R.color.colorIcons);
        int inactiveIconColor = getColor(R.color.colorIconsInactive);
        raspberryPiIcon.setTint(isConnected ? activeColor : inactiveIconColor);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.raspberry_pi) {
            isConnected = !isConnected;
            Drawable raspberryPiIcon = item.getIcon();
            setIconColorByConnectionStatus(raspberryPiIcon);
            String connectionText = isConnected ? "Connected to RPi" : "Disconnected from RPi";
            Toast.makeText(getApplicationContext(), connectionText, Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(KEY_IS_CONNECTED, isConnected);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void setDrawerEnabled(boolean isEnabled) {
        if (mDrawer == null) return;
        int lockMode =
                isEnabled ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        mDrawer.setDrawerLockMode(lockMode);
    }
}
