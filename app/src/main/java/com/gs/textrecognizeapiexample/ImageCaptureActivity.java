package com.gs.textrecognizeapiexample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
/**
 * Created by Ghanshyam on 1/9/2017.
 */
public class ImageCaptureActivity extends Activity implements SurfaceHolder.Callback,
        View.OnClickListener,Camera.PictureCallback, Camera.ShutterCallback{

    private static final String TAG = ImageCaptureActivity.class.getSimpleName();
    final int PIC_CROP = 33;


    Context mContext;
    SurfaceView surface;
    SurfaceHolder mHolder;
    private Camera mCamera;

    LinearLayout captureButtonLnrl;
    LinearLayout doneButtonLnrl;
    RelativeLayout crossRlayout;
    ImageView capture_imageview;

    TextView tap_msgview;

    private Uri resultImageURI;


    public void keppWindowActive(){
        try{
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        mContext = this;
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {

            public void run() {

                setContentView(R.layout.activity_scan_receipt);

                surface = (SurfaceView) findViewById(R.id.camera_frame);
                captureButtonLnrl = (LinearLayout) findViewById(R.id.capture_button);

                doneButtonLnrl = (LinearLayout) findViewById(R.id.donebt);
                crossRlayout = (RelativeLayout) findViewById(R.id.crossRlayout);
                tap_msgview = (TextView)findViewById(R.id.tap_msgview);

                capture_imageview = (ImageView) findViewById(R.id.capture_imageview);

                captureButtonLnrl.setOnClickListener(ImageCaptureActivity.this);
                doneButtonLnrl.setOnClickListener(ImageCaptureActivity.this);
                crossRlayout.setOnClickListener(ImageCaptureActivity.this);

                setupCamera();
                keppWindowActive();

                surface.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        try {

                            if (mCamera != null && isCameraOn)
                                mCamera.autoFocus(null);

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 10);
    }


    /**
     * this setup the camera on surface view
     */
    public void setupCamera(){
        try{
            mHolder = surface.getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            if(mCamera==null)
                mCamera = Camera.open();
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(mContext,"Unable to connect to Camera",Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try{
            if(mCamera==null){
                mCamera = Camera.open();
            }
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(mContext,"Unable to connect to Camera",Toast.LENGTH_LONG).show();
        }
    }

    public Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : sizes) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                }
                else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea > resultArea) {
                        result = size;
                    }
                } }
        }
        return (result);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {


        int heightw = getWindowManager().getDefaultDisplay().getHeight();
        int widthw = getWindowManager().getDefaultDisplay().getWidth();

        try{
            if(mCamera!=null){
                mCamera.lock();
                mCamera.setDisplayOrientation(90);
                Camera.Parameters parameters = mCamera.getParameters();
//                Size cs = getBestPreviewSize(width, height, parameters);//sizes.get(0);  // You need to choose the most appropriate previewSize for your app. So select one from the list

                Camera.Size cs = getOptimalPreviewSize(width, height);

                parameters.setPreviewSize(cs.width, cs.height);
                mCamera.setParameters(parameters);
                try {
                    mCamera.setPreviewDisplay(mHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            mCamera.startPreview();
            cameraStart();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopCamera();
    }

    /**
     * this release the camera
     */
    public void stopCamera(){
        try{
            if(mCamera!=null){
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    boolean isCameraOn = false;

    public void cameraStart(){

        if(mCamera == null || isCameraOn)
            return;

        mCamera.startPreview();
        isCameraOn= true;

        tap_msgview.setVisibility(View.VISIBLE);

    }

    public void cameraStop(){

        if(mCamera == null || !isCameraOn)
            return;

        mCamera.stopPreview();
        isCameraOn = false;
        tap_msgview.setVisibility(View.GONE);
    }


    @Override
    public void onClick(View v) {

        if (v == captureButtonLnrl) {
            if (mCamera != null && isCameraOn) {
                mCamera.takePicture(this, this, this);
            }
        }


        if (v == doneButtonLnrl) {
//           finish();

            if (resultImageURI == null) {

                Toast.makeText(this, "Please capture image first", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent results = new Intent(this,ResultsActivity.class);
            results.putExtra("IMAGE_PATH", resultImageURI.getPath());
            results.putExtra("RESULT_PATH", resultUrl);
            startActivity(results);
            finish();
        }

        if (v == crossRlayout) {

            if (resultImageURI != null) {

                resultImageURI = null;
                capture_imageview.setVisibility(View.GONE);
                cameraStart();

            } else {

                finish();
            }
        }
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        Log.d(TAG, "Picture taken");

        if (data == null) {
            Log.d(TAG, "Got null data");
            return;
        }

        Log.d(TAG, "Got bitmap");
        try {

            File file = getOutputMediaFile();

            try {

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.close();

            } catch (IOException e) {

                Log.e("PictureDemo", "Exception in photoCallback", e);

            }

            resultImageURI = Uri.fromFile(file);
            new RotateBitmapAsyncTask().execute(resultImageURI.getPath());

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private String resultUrl = "result.txt";

    private class RotateBitmapAsyncTask extends AsyncTask<String, Bitmap, Bitmap> {

        protected Bitmap doInBackground(String... uri) {

            Log.e(TAG, "Rotation Image  Now......");
            Log.e(TAG, "capturedImageURI PATH....." + resultImageURI.getPath());
            Bitmap bitmap = getRotatedImagePath(resultImageURI.getPath());
//            Bitmap bitmap = ImageUtils.PicturePath(ReceiptScanActivity.this,resultImageURI.getPath());
            return bitmap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            cameraStop();

            showProgress();
        }

        protected void onPostExecute(Bitmap result) {

            hideProgress();

//            cameraStart();

            if (result != null) {

                try {

                    capture_imageview.setVisibility(View.VISIBLE);
                    capture_imageview.setImageBitmap(BitmapFactory.decodeFile(resultImageURI.getPath()));
                    capture_imageview.refreshDrawableState();


                } catch (Exception e) {

                    Log.e(TAG, "Error in Image ==>" + e.toString());
                }
            }
        }
    }

    public Bitmap getRotatedImagePath(String imagePath) {

        try {

            Bitmap bitmap = null;
//            BitmapFactory.Options bitoption = new BitmapFactory.Options();
//            bitoption.inSampleSize = 1;
//            Bitmap bitmapPhoto = BitmapFactory.decodeFile(imagePath, bitoption);

            Bitmap bitmapPhoto = decodeSampledBitmapFromFile(imagePath,2000,2000);

            if(bitmapPhoto == null)
            {
                return null;

            }

            bitmap = Bitmap.createScaledBitmap(bitmapPhoto, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), true);
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imagePath);
            } catch (IOException e) {
                // Auto-generated catch block
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Matrix matrix = new Matrix();
            if ((orientation == 3)) {
                matrix.postRotate(180);
                // matrix.postScale((float) bitmapPhoto.getWidth(),
                // (float)bitmapPhoto.getHeight());
                bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);

            } else if (orientation == 6) {
                matrix.postRotate(90);
                // matrix.postScale((float) bitmapPhoto.getWidth(),
                // (float)bitmapPhoto.getHeight());
                bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);

            } else if (orientation == 8) {
                matrix.postRotate(270);
                // matrix.postScale((float)
                // bitmapPhoto.getWidth(),(float)bitmapPhoto.getHeight());
                bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);

            } else if (orientation == 1) {
                matrix.postRotate(90);
                // matrix.postScale((float)
                // bitmapPhoto.getWidth(),(float)bitmapPhoto.getHeight());
                bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);

            }


            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            File myDirectory = new File(imagePath);
            if (!myDirectory.exists()) {
                myDirectory.mkdirs();
            }
            // String imgName = String.valueOf(System.currentTimeMillis()) +".jpg";
            File file = new File(imagePath);


            try {
                file.createNewFile();
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight)
    { // BEST QUALITY MATCH

        try {

            //First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            // Calculate inSampleSize, Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            int inSampleSize = 1;

            if (height > reqHeight) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            }
            int expectedWidth = width / inSampleSize;

            if (expectedWidth > reqWidth) {
                //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            options.inSampleSize = inSampleSize;

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(path, options);

        }catch (Exception e){

            e.printStackTrace();

        }

        return null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (resultImageURI != null) {
            outState.putString("cameraImageUri", resultImageURI.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("cameraImageUri")) {
            resultImageURI = Uri.parse(savedInstanceState.getString("cameraImageUri"));
        }
    }

    @Override
    public void onShutter() {

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


    public static File getOutputMediaFile(){

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/textrecognizegs/Images");

        if (! mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

//        if(mediaFile == null) {

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "temp.jpeg");

//        }

        return mediaFile;
    }


    private Camera.Size getOptimalPreviewSize(int w, int h) {

        List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}