package org.canthack.tris.catcam;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import com.talsockettest.R;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private ImageDownloadTask diTask;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// The following checks to see if we're restarting with a backgrounded
		// AsyncTask. If so, re-establish the connection. Also, since the image
		// did not get saved across the destroy/create cycle, if the AsyncTask
		// has finished, grab the downloaded image again from the AsyncTask.
		if( (diTask = (ImageDownloadTask)getLastNonConfigurationInstance()) != null) {
			diTask.setContext(this);  // Give my AsyncTask the new Activity reference
			if(diTask.getStatus() == AsyncTask.Status.FINISHED)
				diTask.setImageInView();
		}
		
		ImageView catImage = (ImageView)this.findViewById(R.id.imageView1);
		
		catImage.setOnLongClickListener(new OnLongClickListener() { 
	        @Override
	        public boolean onLongClick(View v) {
	            return doShare(v);
	        }
	    });
	}

	public void doClick(View view) {
		TextView err = (TextView)this.findViewById(R.id.error_text);
		err.setText("");
		
		if(diTask != null) {
			AsyncTask.Status diStatus = diTask.getStatus();
			Log.v("doClick", "diTask status is " + diStatus);
			if(diStatus != AsyncTask.Status.FINISHED) {
				Log.v("doClick", "... no need to start a new task");
				return;
			}
			// Since diStatus must be FINISHED, we can try again.
		}

		ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		
		boolean isConnected = !(activeNetwork == null) && activeNetwork.isConnectedOrConnecting();

		if(!isConnected){
			Toast.makeText(getApplicationContext(), this.getString(R.string.no_internet), Toast.LENGTH_LONG).show();
		}
		else{
			diTask = new ImageDownloadTask(this);

			try{
				diTask.execute(getResources().getString(R.string.image_url));
			}
			catch(Exception e){
				e.printStackTrace();
				err.setText(getResources().getString(R.string.error_sharing) + "\n" + e.getMessage());
				err.setText(e.getMessage());
			}
		}
	}
	
	public boolean doShare(View view){
		ImageView catView = (ImageView)view;
		if(catView.getDrawable() == null){
			//no image in the view, so makes no sense to share
			return false;
		}
				
		catView.setDrawingCacheEnabled(true);
		Bitmap catBitmap = (catView.getDrawingCache());
		
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("image/jpeg");
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		catBitmap.compress(Bitmap.CompressFormat.JPEG, 95, bytes);
		
		String filePath = getBaseContext().getExternalFilesDir(null) + 
				File.separator + "The Cats.jpg";
		
		File f = new File(filePath);
		try {
			f.createNewFile();
		    f.deleteOnExit();
		    FileOutputStream fos = new FileOutputStream(f);
		    fos.write(bytes.toByteArray());
		    fos.close();
		    
			shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
			startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_string)));			
			return true;
		} catch (IOException e) {                       
		        e.printStackTrace();   
		        return false;
		}
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
}
