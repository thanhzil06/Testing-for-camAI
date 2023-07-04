package com.example.sftpconnect;
import com.example.sftpconnect.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.content.Context;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.Toast;
import android.os.Environment;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.Image.Plane;
import android.media.MediaCodec;
import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.*;
import java.util.*;

// You need to add jscraft dependency to gradle.
/* Visit below for more:
*1. https://mvnrepository.com/artifact/com.jcraft/jsch/0.1.55
*2. https://www.youtube.com/watch?v=FWPk0aD3fYk
* */
import com.jcraft.jsch.*;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.api.*;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;

import static org.jcodec.common.Codec.H264;
import static org.jcodec.common.Format.MOV;
import static org.jcodec.scale.BitmapUtil.fromBitmap;

public class MainActivity extends AppCompatActivity {
    /* Variables for 1. SFTP connection */
    private Button btnConnectSftp;
    private Button btnRecordVideo;

    //private static String host = "172.19.31.8";
    private static String host = "192.168.53.107";
    private static String user = "thanh";
    private static String password = "123456aA@";
    private static int port = 22;
    private static String remoteDir = "/home/thanh/";
    private static String localDir = "/home/thanh/Downloads/";
    private static String fileName = "yt_short.mp4";
    public static File file;
    /*------------------------------------------------------------*/

    /* Variables for 2. Recording video
     * */
    private static int CAMERA_PERMISSION_CODE = 100;
    private static int VIDEO_RECORD_CODE = 101;
    private Uri videoPath;
            //= "/data/media/tmp_video";
    /*------------------------------------------------------------*/

    /* Variables for 3. Getting Image in camera's storage */
    ImageView imageView;
    private Button btnGetImage;
    private static int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 102;
    private static int WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 103;
    private static int width = 1000;
    private static int height = 1000;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    /*------------------------------------------------------------*/

    /* Variables for 4. Capturing video */
    private static final int VIDEO_CAPTURE_CODE = 103;
    private String imagePath = "";
    private Button btnCapVideo;
    /*------------------------------------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*---------------------- 1. Button for SFTP connection ----------------------*/
        btnConnectSftp = (Button) findViewById(R.id.buttonConnect);
        btnConnectSftp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Integer, Void, Void>(){
                    @Override
                    protected Void doInBackground(Integer... integers) {
                        try {
                            setUpSFTPConnection(user, password, host, port, fileName);
                        } catch (Exception e){
                            System.out.println(e.getMessage().toString());
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute(1);
            }
        });
        /*---------------------- 1.' End of SFTP connection button doing ---------------------- */


        /*---------------------- 2. Button for Recording video from camera ----------------------*/
        btnRecordVideo = (Button) findViewById(R.id.buttonRecord);
        btnRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // First, check the present of device's camera
                if (isCameraPresent()){
                    Log.i("VIDEO_RECORD_TAG", "Camera is detected");

                    Log.i("VIDEO_RECORD_TAG", "Request permissions for camera");
                    checkCameraPermission();
                }
                else {
                    Log.i("VIDEO_RECORD_TAG", "No camera is detected");
                }

                // Starting to record video from device's camera
                Log.i("VIDEO_RECORD_TAG", "Starting to record video");
                recordVideo();
            }
        });
        /*---------------------- 2.' End of Recording video button doing ---------------------- */


        /*---------------------- 3. Button for getting images from camera ----------------------*/
        imageView = findViewById(R.id.imageInCameraStorage);

        btnGetImage = (Button) findViewById(R.id.buttonCamWatching);
        btnGetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* ============ Old code ============ */               

                /* Test getting image from snapshot of webui on camera AI */
                /*File file = new File(Environment.getExternalStorageDirectory(), "videocapture/000…00-0000-000000000000/");
                File file_00 = new File(file,"snapshot.jpg");
                imageView.setImageDrawable(Drawable.createFromPath(file_00.toString()));
                System.out.println("Load1 image successfully");*/


                /*========= Read image in SAMSUNG A50: Successfully =========*/
               /* File file = new File(Environment.getExternalStorageDirectory(), "DCIM/TestAndroid/");
                File file_00 = new File(file,"3"+".jpg");
                imageView.setImageDrawable(Drawable.createFromPath(file_00.toString()));
                System.out.println("Load image successfully at " + file_00.getAbsolutePath());*/

                createVideoFile();            

            }

        });
        /*---------------------- 3. 'End of button getting images from camera ---------------------- */


        /*---------------------- 4. Button for video capturing ----------------------*/
        btnCapVideo = (Button) findViewById(R.id.buttonCaptureVideo);
        btnCapVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCameraPresent()){
                    Log.i("VIDEO_CAPTURE_TAG", "Camera is detected");

                    Log.i("VIDEO_CAPTURE_TAG", "Request permissions for camera");
                    checkCameraPermission();
                }
                else {
                    Log.i("VIDEO_CAPTURE_TAG", "No camera is detected");
                }
                verifyStoragePermissions();
                captureVideo();

                //Bitmap myBitMap = BitmapFactory.decodeFile(imagePath);
                System.out.println("image/video from capturing video" + imagePath);
                imageView.setImageDrawable(imagePath.toString());
                imageView.setImageURI(imagePath);
                imageView.setImageBitmap(myBitMap);
                System.out.println("Save image/video successfully at " + imagePath);
            }
        });
        /*---------------------- 4.' End of button video capturing ---------------------- */

    }


    /*--------------------------- 2. Functions for Recording video from camera --------------------------- */
    private boolean isCameraPresent(){
        // Detect camera of device: check for the availability of the camera at runtime
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            return true;
        }
        else {
            return false;
        }
    }

    private void checkCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED){
            Log.i("VIDEO_RECORD_TAG", "Permission is denied. Requesting permissions for camera");

            ActivityCompat.requestPermissions(this,
                                                    new String[] {Manifest.permission.CAMERA},
                                                    CAMERA_PERMISSION_CODE);
        }
    }

    /*
    * -------------------------- Reading for 2 below functions --------------------------
    * https://developer.android.com/training/basics/intents/result
    * https://viblo.asia/p/huong-dan-lay-ket-qua-tra-ve-tu-mot-activity-MgNeWWnZeYx
    */
    private void recordVideo(){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        /*
        * In several fields, especially computing, deprecation is the discouragement of use of some terminology, feature, design, or practice,
        * typically because it has been superseded or is no longer considered efficient or safe,
        * without completely removing it or prohibiting its use
        * */
        startActivityForResult(intent, VIDEO_RECORD_CODE);
    }

    // Override a callback method for receiving recording status and video saving path
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        /*
        * The Android Camera application returns the video in the Intent delivered to
        * onActivityResult() as a Uri pointing to the video location in storage.
        * The following code retrieves this video.
        * */

        if (requestCode == VIDEO_CAPTURE_CODE){
            if (resultCode == RESULT_OK){
                Toast.makeText(this, "Video Recorded", Toast.LENGTH_LONG).show();
                Uri imageUri = data.getData();
                imagePath = getRealPathFromURI(imageUri);
            }
        }

        if (requestCode == VIDEO_RECORD_CODE){
            if (resultCode == RESULT_OK){
                videoPath = data.getData();
                Log.i("VIDEO_RECORD_TAG", "Video is recorded and available at this path " + videoPath);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                VideoView videoView = new VideoView(this);
                videoView.setVideoURI(videoPath);
                videoView.start();
                builder.setView(videoView).show();
            }

            else if (requestCode == RESULT_CANCELED){
                Log.i("VIDEO_RECORD_TAG", "Video recording is cancelled");
                Toast.makeText(this, "Video recording is cancelled.",  Toast.LENGTH_LONG).show();
            }

            else {
                Log.i("VIDEO_RECORD_TAG", "Video recording got some unknown errors");
                Toast.makeText(this, "Video recording got some unknown errors",  Toast.LENGTH_LONG).show();
            }
        }
    }
    /*--------------------------- 2.'' End of functions for Recording video from camera --------------------------- */


    /*--------------------------- 4. Functions for Capturing video --------------------------- */
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private void captureVideo(){

        //File file_test = new File(Environment.getExternalStorageDirectory() + "/DCIM/TestAndroid/", "test.txt");
        File file_test = new File("/DCIM/TestAndroid/", "test.txt");
        if(file_test.exists()){
            try {
                FileWriter fw = new FileWriter(file_test);
                Log.i("VIDEO_CAPTURE_TAG", "Writing something to test file...");
                fw.write("Files in Java might be tricky, but it is fun enough!");
                fw.close();
                Log.i("VIDEO_CAPTURE_TAG", "Successfully wrote to the file.");
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
        else {
            Log.i("VIDEO_CAPTURE_TAG", "Fail to create new test file at " + file_test.getAbsolutePath());
        }

        /* Test load created image to the screen */

        /* Test creating video files from a list of created images */
        //createVideoFile();

        /* Test creating video files only */
       /* File path = new File(Environment.getExternalStorageDirectory(), "test/"); // test on emulator
        File file1 = new File(path, "video_02.mp4");
        Log.i("GET_IMAGE_TAG", "Creating a new video file");
        System.out.println("Creating a new video file at " + file1.getAbsolutePath());*/

        /* ========== Main code: Video capturing ==========*/
        //Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        //Toast.makeText(this, "Video capture starting",  Toast.LENGTH_LONG).show();
        //startActivityForResult(intent, VIDEO_CAPTURE_CODE);
    }
    /*--------------------------- 4.'' End of functions for Capturing video --------------------------- */


    /*-------------------------------------------- 1. Functions for SFTP connection --------------------------------------------*/
    /* Params used in SFTP
     * @param server
     * @param user
     * @param openSSHPrivateKey
     * @param remoteDir
     * @param localDir
     * @param fileName
     * @throws IOException
     */
    public static void setUpSFTPConnection(String user, String password, String host, int port, String fileName)
            throws Exception {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            boolean status = false;

            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);
            session.setPassword(password);
            System.out.println("----- Connecting ----- ");
            session.connect();
            status = session.isConnected();
            Log.i("Session is", " "+ status);
            System.out.println("Established session");

            // SSH Channel
            Channel channel = (Channel) session.openChannel("sftp");
            ChannelSftp channelSFTP = (ChannelSftp) channel;
            channelSFTP.connect();
            System.out.println("Opened SFTP Channel");

            /**
            * Send file from local path to remote server
            * */
            System.out.println("Sending file " + fileName + " to sftp server");
            channelSFTP.lcd("/root");
            System.out.println(channelSFTP.lpwd());
            System.out.println(channelSFTP.ls(channelSFTP.lpwd()));

            //channelSFTP.put(localDir + "/" + fileName, remoteDir);
            System.out.println("Sent file successfully");

//            System.out.println("Getting file" + fileName + "from sftp server");
//            channelSFTP.get(remoteDir + "/" + fileName, localDir);
//            System.out.println("Downloaded file successfully");

            /*
            *  Disconnect SFTP connection when file transferring completed
            */
            channelSFTP.disconnect();
            session.disconnect();
            System.out.println("Disconnected from sftp");

            } catch (JSchException e){
                System.out.println(e.getMessage().toString());
                e.printStackTrace();
            }
    }
    /*-------------------------------------------- 1.'' End of functions for SFTP connection --------------------------------------------*/

    /*--------------------------- 3. Functions for Getting image from device storage --------------------------- */
    private void checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION_CODE);
        }
    }
    private void checkWriteExternalStorage(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
        }
    }
    public static SequenceEncoder create15Fps(File out) throws IOException {
        return new SequenceEncoder(NIOUtils.writableChannel(out), Rational.R(15, 1), MOV, H264, null);
    }

    private void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void createVideoFile(){

        checkReadExternalStoragePermission();
        checkWriteExternalStorage();
        verifyStoragePermissions();

        /*========= Create video.mp4 from image you've just got =========*/
        try {
            File path = new File(Environment.getExternalStorageDirectory(), "DCIM/TestAndroid/");
            //File path = new File("/sdcard/DCIM/TestAndroid/"); // test_00 on emulator
            //File path = new File(Environment.getExternalStorageDirectory(), "test/"); // test_01 on emulator
            File file1 = new File(path, "video_00.mp4");
            Log.i("GET_IMAGE_TAG", "Creating a new video file");

            SequenceEncoder encoder = SequenceEncoder.create25Fps(file1);
            //create15Fps(file1);
            //SequenceEncoder.create25Fps(file1);

            for (int i = 1; i<=5000; i++) {
                //File file = new File(path, "/img0000" + Integer.toString(i) + ".jpg");
                File imgFile = new File(path, "/" + Integer.toString(i) + ".jpg");
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                org.jcodec.common.model.Picture picture = fromBitmap(bitmap);
                encoder.encodeNativeFrame(picture);
            }
            encoder.finish();
            Log.i("GET_IMAGE_TAG", "Create video file successfully");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("GET_IMAGE_TAG", "Fail to create video file");
        }
    }
    /*-------------------------------------------- 3.'' End of functions for Getting image from device storage --------------------------------------------*/

}
