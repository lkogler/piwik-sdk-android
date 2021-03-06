/*
 * Android SDK for Piwik
 *
 * @link https://github.com/piwik/piwik-android-sdk
 * @license https://github.com/piwik/piwik-sdk-android/blob/master/LICENSE BSD-3 Clause
 */

package com.piwik.demo;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.piwik.sdk.PiwikApplication;
import org.piwik.sdk.Tracker;
import org.piwik.sdk.tools.Logy;
import org.piwik.sdk.TrackMe;

import java.util.Arrays;
import java.util.List;


public class DemoActivity extends ActionBarActivity {
    int cartItems = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        initPiwik();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getUserId() {
        String userId;

        try {
            WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            userId = wm.getConnectionInfo().getMacAddress();
            Logy.i("user_id", "wifi mac " + userId);
        } catch (Exception e) {
            Logy.e("user_id", "wifi is not available", e);
            userId = null;
        }

        if (userId == null) {
            long result = Build.ID.hashCode();
            result = 31 * result + Build.DISPLAY.hashCode();
            result = 31 * result + Build.PRODUCT.hashCode();
            result = 31 * result + Build.DEVICE.hashCode();
            result = 31 * result + Build.BOARD.hashCode();
            result = 31 * result + Build.CPU_ABI.hashCode();
            result = 31 * result + Build.CPU_ABI2.hashCode();
            result = 31 * result + Build.MANUFACTURER.hashCode();
            result = 31 * result + Build.BRAND.hashCode();
            result = 31 * result + Build.MODEL.hashCode();
            result = 31 * result + Build.BOOTLOADER.hashCode();

            userId = Long.toString(result);
            Logy.i("user_id", "android.os.Build used " + userId);
        }

        return userId;
    }

    private void initPiwik() {
        // do not send http requests
        ((PiwikApplication) getApplication()).getPiwik().setDryRun(false);

        ((PiwikApplication) getApplication()).getTracker()
                .setDispatchInterval(5000)
                .trackAppDownload()
                .setUserId(getUserId());

        initTrackViewListeners();
    }

    protected void initTrackViewListeners() {
        // simple track view
        Button button = (Button) findViewById(R.id.trackMainScreenViewButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PiwikApplication) getApplication()).getTracker().trackScreenView("/", "Main screen");
            }
        });

        // custom vars track view
        button = (Button) findViewById(R.id.trackCustomVarsButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PiwikApplication) getApplication()).getTracker()
                        .trackScreenView(
                                new TrackMe()
                                        .setScreenCustomVariable(1, "first", "var")
                                        .setScreenCustomVariable(2, "second", "long value"),
                                "/custom_vars", "Custom Vars");
            }
        });

        // exception tracking
        button = (Button) findViewById(R.id.raiseExceptionButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PiwikApplication) getApplication()).getTracker().trackException(new Exception("OnPurposeException"), "Crash button", false);
            }
        });

        // goal tracking
        button = (Button) findViewById(R.id.trackGoalButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int revenue;
                try {
                    revenue = Integer.valueOf(
                            ((EditText) findViewById(R.id.goalTextEditView)).getText().toString()
                    );
                } catch (Exception e) {
                    ((PiwikApplication) getApplication()).getTracker().trackException(e, "wrong revenue", false);
                    revenue = 0;
                }
                ((PiwikApplication) getApplication()).getTracker().trackGoal(1, revenue);
            }
        });

        //ecommerce tracking
        button = (Button) findViewById(R.id.trackEcommerceButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker tracker = ((PiwikApplication) getApplication()).getTracker();
                tracker.trackEcommerceCartUpdate(8600);
            }
        });

        button = (Button) findViewById(R.id.addEcommerceItemButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> skus = Arrays.asList("00001", "00002", "00003", "00004");
                List<String> names = Arrays.asList("Silly Putty", "Fishing Rod", "Rubber Boots", "Cool Ranch Doritos");
                List<String> categories = Arrays.asList("Toys & Games", "Hunting & Fishing", "Footwear", "Grocery");
                List<Integer> prices = Arrays.asList(449, 3495, 2450, 250);

                int index = cartItems % 4;
                int quantity = (cartItems / 4) + 1;

                Tracker tracker = ((PiwikApplication) getApplication()).getTracker();
                tracker.addEcommerceItem(skus.get(index), names.get(index), categories.get(index), prices.get(index), quantity);
                cartItems++;
            }
        });

        button = (Button) findViewById(R.id.completeEcommerceOrderButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker tracker = ((PiwikApplication) getApplication()).getTracker();
                tracker.trackEcommerceOrder(String.valueOf(10000*Math.random()), 10000, 1000, 2000, 3000, 500);
            }
        });

    }
}
