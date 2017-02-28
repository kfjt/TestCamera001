package jp.ne.home.jcom.kfujita.testcamera001;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private Camera camera;
    private CameraPreview cameraPreview;
    private boolean isTake = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = Camera.open();
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.cameraPreview);
        cameraPreview = new CameraPreview(this, camera);
        frameLayout.addView(cameraPreview);

        cameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d("Debug","motionEvent.getAction()");
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    if(!isTake){
                        isTake = true;
                        camera.autoFocus(autoFocusListener);
//                        camera.takePicture(null, null, picJpgListener);

                    }

                }
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(camera != null){
            camera.release();
            camera = null;

        }
    }

    private Camera.PictureCallback picJpgListener = new Camera.PictureCallback(){
        public void onPictureTaken(byte[] data, Camera camera){
            if(data == null){
                return;

            }

            String saveDir = Environment.getExternalStorageDirectory().getPath() + "/test";
            File file = new File(saveDir);

            if(!file.exists()){
                if(!file.mkdir()){

                }

            }

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String imgPath = saveDir + "/" + simpleDateFormat.format(calendar.getTime()) + ".jpg";

            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(imgPath, true);
                fileOutputStream.write(data);
                fileOutputStream.close();

                registAndroidDB(imgPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileOutputStream = null;

            camera.startPreview();
            isTake = false;

        }
    };

    private void registAndroidDB(String path){
        ContentValues contentValues = new ContentValues();
        ContentResolver contentResolver = MainActivity.this.getContentResolver();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put("_data", path);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }

    private Camera.AutoFocusCallback autoFocusListener = new Camera.AutoFocusCallback(){

        @Override
        public void onAutoFocus(boolean b, Camera camera) {
            camera.takePicture(null, null, picJpgListener);
        }
    };
}
