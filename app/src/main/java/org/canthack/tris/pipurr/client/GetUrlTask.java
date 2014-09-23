package org.canthack.tris.pipurr.client;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;

public class GetUrlTask extends AsyncTask<String, Integer, Void> {
    private Context context;
    private boolean success;

    public void setContext(Context ctx) {
        this.context = ctx;
    }

    protected Void doInBackground(String... urls) {

        HttpClient httpClient = CustomHTTPClient.getHttpClient();
        try {
            HttpGet request = new HttpGet(urls[0]);
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setSoTimeout(params, 60000);   // 1 minute
            request.setParams(params);

            HttpResponse response = httpClient.execute(request);
            response.getEntity().consumeContent();
            success = true;

        } catch (Exception e) {
            Log.e("GetUrlTask", "Error getting URL", e);
        }

        return null;
    }

    protected void onPostExecute(Void v) {
        if (context != null && !success) {
            Toast.makeText(context, context.getString(R.string.error_connecting), Toast.LENGTH_SHORT).show();
        }
    }

}