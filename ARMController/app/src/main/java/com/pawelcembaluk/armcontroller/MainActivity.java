package com.pawelcembaluk.armcontroller;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.pawelcembaluk.armcontroller.bluetooth.BluetoothConnection;
import com.pawelcembaluk.armcontroller.bluetooth.SerialService;
import com.pawelcembaluk.armcontroller.interfaces.ConnectionObserver;
import com.pawelcembaluk.armcontroller.interfaces.DrawerEnabler;
import com.pawelcembaluk.armcontroller.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity implements DrawerEnabler, ServiceConnection, ConnectionObserver {

    private AppBarConfiguration appBarConfiguration;
    private DrawerLayout drawer;
    private Drawable RPiIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = findViewById(R.id.drawer_layout);
        appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.nav_controller, R.id.nav_devices)
                        .setDrawerLayout(drawer).build();
        initializeToolbar();
        initializeNavigation();
        readSettings();
        BluetoothConnection.getInstance().addConnectionObserver(this);
    }

    @Override
    protected void onDestroy() {
        BluetoothConnection.getInstance().removeConnectionObserver(this);
        super.onDestroy();
    }

    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initializeNavigation() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void readSettings() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d(getClass().getSimpleName(), "Continuous commands delay: " + sharedPreferences.getInt(
                SettingsFragment.KEY_CONTINUOUS_COMMANDS_DELAY, 50)); //TODO: Use this setting.
    }

    @Override
    protected void onStart() {
        super.onStart();
        BluetoothConnection.getInstance().startService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothConnection.getInstance().bindService(this, this);
    }

    @Override
    protected void onPause() {
        BluetoothConnection.getInstance().unbindService(this, this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        BluetoothConnection.getInstance().detachService(this);
        super.onStop();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration) ||
               super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        RPiIcon = menu.getItem(0).getIcon();
        setRPiIconColorByConnectionStatus();
        return true;
    }

    private void setRPiIconColorByConnectionStatus() {
        int color = BluetoothConnection.getInstance().isConnected() ? R.color.colorIcons : R.color.colorIconsInactive;
        RPiIcon.setTint(getColor(color));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.raspberry_pi:
                return connectToRaspberryPi();
            case R.id.bluetooth_settings:
                return showBluetoothSettings();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean connectToRaspberryPi() {
        Runnable connect = () -> BluetoothConnection.getInstance().connect(this);
        Runnable disconnect = () -> BluetoothConnection.getInstance().disconnect();
        runOnUiThread(BluetoothConnection.getInstance().isConnected() ? disconnect : connect);
        return true;
    }

    private boolean showBluetoothSettings() {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
        return true;
    }

    @Override
    public void setDrawerEnabled(boolean isEnabled) {
        if (drawer == null) return;
        int lockMode =
                isEnabled ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        drawer.setDrawerLockMode(lockMode);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(getClass().getSimpleName(), "onServiceConnected");
        SerialService service = ((SerialService.SerialBinder) iBinder).getService();
        BluetoothConnection.getInstance().setService(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(getClass().getSimpleName(), "onServiceDisconnected");
        BluetoothConnection.getInstance().setService(null);
    }

    @Override
    public void onConnect() {
        RPiIcon.setTint(getColor(R.color.colorIcons));
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed() {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnect() {
        RPiIcon.setTint(getColor(R.color.colorIconsInactive));
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
    }
}
