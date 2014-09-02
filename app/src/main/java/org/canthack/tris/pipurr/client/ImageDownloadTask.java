package org.canthack.tris.pipurr.client;
//Based on Chapter 11 of the book Pro Android 3

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageDownloadTask extends AsyncTask<String, Integer, Bitmap> {
    private Context mContext;  // reference to the calling Activity
    int progress = -1;
    Bitmap downloadedImage = null;
    String lastError = "";

    ImageDownloadTask(Context context) {
        mContext = context;
    }

    // Called from main thread to re-attach
    protected void setContext(Context context) {
        mContext = context;
        if (progress >= 0) {
            publishProgress(this.progress);
        }
    }

    protected void onPreExecute() {
        progress = 0;
        clearImageInView();
    }

    protected Bitmap doInBackground(String... urls) {
        if (BuildConfig.DEBUG) Log.v("PiPurr", "doing download of image...");
        return downloadAndSaveImage(urls);
    }

    protected void onProgressUpdate(Integer... progress) {
        ProgressBar pBar = (ProgressBar)
                ((Activity) mContext).findViewById(R.id.progressBar1);

        if (progress[0] > 0 && progress[0] < 100) {
            pBar.setVisibility(View.VISIBLE);
            pBar.setProgress(progress[0]);
        } else {
            pBar.setVisibility(View.INVISIBLE);
        }
    }

    protected void onPostExecute(Bitmap result) {
        String errorMessage = "";

        if (result != null) {
            downloadedImage = result;
            setImageInView();
        } else {
            errorMessage = "Problem downloading image. Please try later.\n" + lastError;
        }

        TextView errorMsg = (TextView)
                ((Activity) mContext).findViewById(R.id.error_text);

        errorMsg.setText(errorMessage);
    }

    public Bitmap downloadAndSaveImage(String... urls) {
        HttpClient httpClient = CustomHTTPClient.getHttpClient();
        try {
            HttpGet request = new HttpGet(urls[0]);
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setSoTimeout(params, 60000);   // 1 minute
            request.setParams(params);

            setProgress(25);

            HttpResponse response = httpClient.execute(request);

            setProgress(50);

            byte[] image = EntityUtils.toByteArray(response.getEntity());

            setProgress(75);

            saveFile(image);

            Bitmap mBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

            setProgress(100);

            return mBitmap;

        } catch (IOException | IllegalStateException e) {
            Log.e("ImageDownloadTask", "Error downloading image", e);
            lastError = e.getMessage() + ".";
            setProgress(-1);
        }

        return null;
    }

    private boolean saveFile(byte[] image) {
        String filePath = mContext.getExternalFilesDir(null) +
                File.separator + "The Cats.jpg";

        File f = new File(filePath);
        try {
            if (!f.createNewFile()) return false;
            f.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(image);
            fos.close();

        } catch (IOException e) {
            Log.e("PiPurr", "Could not save file");
            return false;
        }
        return true;
    }

    private void setProgress(int progress) {
        this.progress = progress;
        publishProgress(this.progress);
    }

    protected void setImageInView() {
        if (downloadedImage != null) {
            ImageView mImage = (ImageView)
                    ((Activity) mContext).findViewById(R.id.imageView1);
            mImage.setImageBitmap(downloadedImage);
        }
    }

    protected void clearImageInView() {
        ImageView mImage = (ImageView)
                ((Activity) mContext).findViewById(R.id.imageView1);
        mImage.setImageDrawable(null);
    }
}