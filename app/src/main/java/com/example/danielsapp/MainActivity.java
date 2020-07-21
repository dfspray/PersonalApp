package com.example.danielsapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.util.logging.Logger;

/**
 * Daniel's personal application
 *
 * @author Daniel Spray
 */
public class MainActivity extends AppCompatActivity {
    private static Logger logger = Logger.getLogger(MainActivity.class.getName());
    private SimpleExoPlayer player;
    private PlayerView playerView;
    private static boolean fullscreen = false;
    private Context context = this;
    private static String url = "rtmp://255.255.255.255:1935/live/test";
    Button sourceEditButton;
    ImageView refreshButton;
    ImageView fullscreenButton;

    /**
     * Functions to execute on initialization
     *
     * @param savedInstanceState stored instance state for reopen
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setup main page
        this.setupMain();
        //setup video player and fullscreen functions
        this.setupVideoPlayer();
        //setup source edit button and input
        this.setupEditButton();
    }

    /**
     * Clears activity in background
     */
    @Override
    public void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
    }

    /**
     * Clears activity on shutdown
     */
    @Override
    public void onDestroy() {
        player.release();
        super.onDestroy();
    }

    /**
     * Convert from px to dp units
     * @param px pixels
     * @return density independent pixels
     */
    private int pxToDp (int px) {
        return (int) (px * getApplicationContext().getResources().getDisplayMetrics().density);
    }

    /**
     * Sets video source and refreshes
     *
     * @param source url for rtmp stream
     */
    private void setVideoSource(String source) {
        playerView.setPlayer(player);
        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();

        MediaSource videoSource = new ExtractorMediaSource
                .Factory(rtmpDataSourceFactory)
                .createMediaSource(Uri.parse(source));

        player.prepare(videoSource);
        player.setPlayWhenReady(true);
    }

    /**
     * Initializes interface
     */
    private void setupMain () {
        try {
            setContentView(R.layout.activity_printer_control);
            BottomNavigationView navView = findViewById(R.id.nav_view);

            //Menu
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                    .build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);
        } catch (Exception e) {
            logger.warning("Error loading navigation and views");
        }
    }

    /**
     * Initializes video player and adds fullscreen functionality
     */
    private void setupVideoPlayer() {
        try {
            //Exoplayer setup
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
            playerView = findViewById(R.id.simple_player);

            this.setupRefresh();

            this.setupFullscreen();

            //Setup media source
            playerView.setPlayer(player);
            this.setVideoSource(url);
        } catch (Exception e) {
            logger.warning("Error setting up player or importing rtmp stream");
            e.printStackTrace();
        }
    }

    /**
     * Sets up refresh button functionality
     */
    private void setupRefresh() {
        refreshButton = playerView.findViewById(R.id.exo_refresh_icon);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setVideoSource(url);
            }
        });
    }

    /**
     * Sets up fullscreen button functionality
     */
    private void setupFullscreen() {
        fullscreenButton = playerView.findViewById(R.id.exo_fullscreen_icon);
        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                View bottomNav = findViewById(R.id.nav_view);
                View button1 = findViewById(R.id.buttonPrompt1);
                View button2 = findViewById(R.id.buttonPrompt2);
                View button3 = findViewById(R.id.buttonPrompt3);
                BottomNavigationView navView = findViewById(R.id.nav_view);
                View fragment = findViewById(R.id.nav_host_fragment);

                if(fullscreen) {
                    fullscreenButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fullscreen_open));

                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                    if(getSupportActionBar() != null){
                        getSupportActionBar().show();
                    }

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playerView.getLayoutParams();
                    params.width = pxToDp(384);
                    params.height = pxToDp(216);
                    params.topMargin = pxToDp(5);
                    playerView.setLayoutParams(params);
                    bottomNav.setVisibility(View.VISIBLE);
                    button1.setVisibility(View.VISIBLE);
                    button2.setVisibility(View.VISIBLE);
                    button3.setVisibility(View.VISIBLE);
                    navView.setVisibility(View.VISIBLE);
                    fragment.setVisibility(View.VISIBLE);

                    fullscreen = false;
                } else {
                    fullscreenButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fullscreen_close));

                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

                    if(getSupportActionBar() != null){
                        getSupportActionBar().hide();
                    }

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = params.MATCH_PARENT;
                    params.topMargin = 0;
                    params.bottomMargin = 0;
                    playerView.setLayoutParams(params);

                    fullscreen = true;
                    bottomNav.setVisibility(View.INVISIBLE);
                    button1.setVisibility(View.INVISIBLE);
                    button2.setVisibility(View.INVISIBLE);
                    button3.setVisibility(View.INVISIBLE);
                    navView.setVisibility(View.INVISIBLE);
                    fragment.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    /**
     * Adds url source edit functionality to stream player
     */
    private void setupEditButton() {
        try {
            sourceEditButton = (Button) findViewById(R.id.buttonPrompt1);
            sourceEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater li = LayoutInflater.from(context);
                    View promptView = li.inflate(R.layout.prompt, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setView(promptView);

                    final EditText textInput = (EditText) promptView.findViewById(R.id.editTextDialogUserInput);

                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            url = textInput.getText().toString();
                            setVideoSource(url);
                            logger.info("Input received: " + textInput.getText().toString());
                        }
                    });
                    alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });
        } catch (Exception e) {
            logger.warning("Error displaying source input edit");
        }
    }
}