package org.canthack.tris.pipurr.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class PiPurrMain extends Activity {
    private ImageDownloadTask diTask;
    private boolean fullscreen;
    private View mainLayout;
    private Button imageButton, meowButton, feedButton;
    private ImageView catImage;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The following checks to see if we're restarting with a backgrounded
        // AsyncTask. If so, re-establish the connection. Also, since the image
        // did not get saved across the destroy/create cycle, if the AsyncTask
        // has finished, grab the downloaded image again from the AsyncTask.
        if ((diTask = (ImageDownloadTask) getLastNonConfigurationInstance()) != null) {
            diTask.setContext(this);  // Give my AsyncTask the new Activity reference
            if (diTask.getStatus() == AsyncTask.Status.FINISHED)
                diTask.setImageInView();
        }

        catImage = (ImageView) this.findViewById(R.id.imageView1);

        catImage.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return doShare(v);
            }
        });

        mainLayout = this.findViewById(android.R.id.content).getRootView();
        imageButton = (Button) this.findViewById(R.id.button2);
        meowButton = (Button) this.findViewById(R.id.btnMeow);
        feedButton = (Button) this.findViewById(R.id.btnFeed);
    }

    public void doClick(View view) {
        GetUrlTask ut;

        switch (view.getId()) {
            case R.id.button2:
                TextView err = (TextView) this.findViewById(R.id.error_text);
                err.setText("");

                if (diTask != null) {
                    AsyncTask.Status diStatus = diTask.getStatus();
                    Log.v("doClick", "diTask status is " + diStatus);
                    if (diStatus != AsyncTask.Status.FINISHED) {
                        Log.v("doClick", "... no need to start a new task");
                        return;
                    }
                    // Since diStatus must be FINISHED, we can try again.
                }

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                boolean isConnected = !(activeNetwork == null) && activeNetwork.isConnectedOrConnecting();

                if (!isConnected) {
                    Toast.makeText(getApplicationContext(), this.getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                } else {
                    diTask = new ImageDownloadTask(this);

                    try {
                        diTask.execute(Settings.getLocation(this) + "/cats.jpeg");
                    } catch (Exception e) {
                        e.printStackTrace();
                        err.setText(e.getMessage());
                    }
                }

                break;
            case R.id.imageView1:
                if (catImage.getDrawable() != null) toggleFullScreen();
                break;

            case R.id.btnMeow:
                ut = new GetUrlTask();
                ut.setContext(getApplicationContext());
                ut.execute(Settings.getLocation(this) + "/sound");
                break;

            case R.id.btnFeed:
                ut = new GetUrlTask();
                ut.setContext(getApplicationContext());
                ut.execute(Settings.getLocation(this) + "/feed");
                break;
        }
    }

    private void toggleFullScreen() {
        if (fullscreen) {
            imageButton.setVisibility(View.VISIBLE);
            meowButton.setVisibility(View.VISIBLE);
            feedButton.setVisibility(View.VISIBLE);

            getActionBar().show();
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mainLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

            fullscreen = false;
        } else {
            imageButton.setVisibility(View.GONE);
            meowButton.setVisibility(View.GONE);
            feedButton.setVisibility(View.GONE);

            mainLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            getActionBar().hide();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            fullscreen = true;
        }
    }

    public boolean doShare(View view) {
        ImageView catView = (ImageView) view;
        if (catView.getDrawable() == null || diTask.getStatus() != AsyncTask.Status.FINISHED) {
            //no image in the view or asynctask is busy, so makes no sense to share
            return false;
        }

        String filePath = getBaseContext().getExternalFilesDir(null) +
                File.separator + "The Cats.jpg";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri fileUri = FileProvider.getUriForFile(this, "org.canthack.tris.pipurr.client.catprovider", new File(filePath));

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_string)));

        return true;

    }

    // This gets called before onDestroy(). We want to pass forward a reference
    // to our AsyncTask.
    @Override
    public Object onRetainNonConfigurationInstance() {
        return diTask;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    //Handles menu clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, Settings.class));
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (fullscreen) {
            toggleFullScreen();
        } else {
            super.onBackPressed();
        }
    }

}
