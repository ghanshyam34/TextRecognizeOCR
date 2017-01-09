package com.gs.textrecognizeapiexample;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gs.textrecognizeapiexample.ocrsdkutility.Client;
import com.gs.textrecognizeapiexample.ocrsdkutility.ProcessingSettings;
import com.gs.textrecognizeapiexample.ocrsdkutility.Task;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
/**
 * Created by Ghanshyam on 11/8/2016.
 */
public class ResultsActivity extends AppCompatActivity implements View.OnClickListener{


	public static final String APP_ID  = "textrecognizeappid";
	public static final String PASSWORD  = "qDmbzYMMsH6urtyuhtLq449G5";

	String outputPath;
	ImageView finalImageview;
	String imageUrl;

	String recognizeText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_results);

		finalImageview = (ImageView)findViewById(R.id.final_imageview);

		imageUrl = "unknown";

		Bundle extras = getIntent().getExtras();
		if( extras != null) {
			imageUrl = extras.getString("IMAGE_PATH" );
			outputPath = extras.getString( "RESULT_PATH" );
		}

		finalImageview.setImageURI(Uri.parse(imageUrl));

		new AsyncProcessTask(this).execute(imageUrl, outputPath);
	}


	@Override
	public void onClick(View view) {

	}

	public void updateResults(Boolean success) {
		if (!success)
			return;
		try {
			StringBuffer contents = new StringBuffer();

			FileInputStream fis = openFileInput(outputPath);
			try {
				Reader reader = new InputStreamReader(fis, "UTF-8");
				BufferedReader bufReader = new BufferedReader(reader);
				String text = null;
				while ((text = bufReader.readLine()) != null) {
					contents.append(text).append(System.getProperty("line.separator"));
				}
			} finally {
				fis.close();
			}

			recognizeText = contents.toString();
			showRecognizeTexview();
		} catch (Exception e) {
			recognizeText = "Error: " + e.getMessage();
			showRecognizeTexview();
		}
	}

	public void showRecognizeTexview()
	{

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				TextView tv = new TextView(ResultsActivity.this);
				tv.setTextColor(Color.BLACK);
				tv.setText(recognizeText);
				setContentView(tv);

			}
		});
	}



	public class AsyncProcessTask extends AsyncTask<String, String, Boolean> {

		public AsyncProcessTask(ResultsActivity activity) {


		}

		protected void onPreExecute() {

			showProgress();
		}

		protected void onPostExecute(Boolean result) {

			hideProgress();
			updateResults(result);

		}


		@Override
		protected Boolean doInBackground(String... args) {

			String inputFile = args[0];
			String outputFile = args[1];

			try {

				Client restClient = new Client();


				restClient.applicationId = APP_ID;
				// You should get e-mail from ABBYY Cloud OCR SDK service with the application password
				restClient.password = PASSWORD;

				String language = "English"; // Comma-separated list: Japanese,English or German,French,Spanish etc.

				ProcessingSettings processingSettings = new ProcessingSettings();
				processingSettings.setOutputFormat( ProcessingSettings.OutputFormat.txt );
				processingSettings.setLanguage(language);


				Task task = restClient.processImage(inputFile, processingSettings);

				while( task.isTaskActive() ) {

					Thread.sleep(5000);

					task = restClient.getTaskStatus(task.Id);
				}

				if( task.Status == Task.TaskStatus.Completed ) {
					publishProgress( "Downloading..");
					FileOutputStream fos = openFileOutput(outputFile, Context.MODE_PRIVATE);

					try {
						restClient.downloadResult(task, fos);
					} finally {
						fos.close();
					}

				} else if( task.Status == Task.TaskStatus.NotEnoughCredits ) {
					throw new Exception( "Not enough credits to process task. Add more pages to your application's account." );
				} else {
					throw new Exception( "Task failed" );
				}

				return true;
			} catch (Exception e) {
				final String message = "Error: " + e.getMessage();
				publishProgress(message);
				showRecognizeTexview();
				return false;
			}
		}

	}

	@Override
	public void onBackPressed() {


	}

	ProgressDialog progress;
	/**
	 * this shows the progress on current view
	 */
	public void showProgress() {

		try {

			if (progress == null)
				progress = new ProgressDialog(this);
			progress.setMessage("Please Wait..");
			progress.setCancelable(false);
			progress.show();

		} catch (Exception e) {

			e.printStackTrace();
			try {

				progress = new ProgressDialog(this);
				progress.setMessage("Please Wait..");
				progress.setCancelable(false);
				progress.show();

			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}


	/**
	 * hides the progress on current view
	 */
	public void hideProgress() {

		if (progress != null && progress.isShowing()) {

			progress.dismiss();

		}
	}
}
