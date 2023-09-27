package com.datachecker.hybridocr;

import static com.datachecker.hybridocr.db.DBHelper.COLUMN_NAME;
import static com.datachecker.hybridocr.db.DBHelper.TABLE_NAME;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.datachecker.hybridocr.db.DBHelper;
import com.datachecker.hybridocr.ui.DocumentCaptureActivity;
import com.datachecker.hybridocrlib.HybridOCRLib;
import com.datachecker.hybridocrlib.model.Constants;
import com.google.gson.Gson;

import org.jmrtd.lds.icao.MRZInfo;


public class MainActivity extends AppCompatActivity{

    private Button documentCaptureButton;

    ActivityResultLauncher documentCaptureResultListner;
    public static final String IMAGE_DATA = "IMAGE_DATA";

    private ActivityResultContracts.StartActivityForResult startActivityForResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivityForResult = new ActivityResultContracts.StartActivityForResult();


        setDocumentCaptureResultListner();
        documentCaptureButton = findViewById(R.id.btn_document_capture);
        documentCaptureButton.setOnClickListener(view -> {
            openDocumentCaptureActivity();
        });

    }

    private void openDocumentCaptureActivity() {
        Intent intent = new Intent(this, DocumentCaptureActivity.class);
        documentCaptureResultListner.launch(intent);
    }

    void setDocumentCaptureResultListner() {
        documentCaptureResultListner = this.registerForActivityResult(startActivityForResult, new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Log.d("activityResultLauncher", "setScanNFCDocumentActivityResultLauncher activityResultLauncher called");
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent returnIntent = result.getData();
                    long imageData = returnIntent.getLongExtra(IMAGE_DATA, 1);
                    Log.d("MainActivity", "Received image data is : " + imageData);
                }
            }
        });
    }

}