package com.datachecker.hybridocrlib;

import static android.content.ContentValues.TAG;

import static com.datachecker.hybridocrlib.mlkit.text.TextRecognitionProcessor.ID_CARD_TD_1_LINE_1_REGEX;
import static com.datachecker.hybridocrlib.mlkit.text.TextRecognitionProcessor.ID_CARD_TD_1_LINE_2_REGEX;
import static com.datachecker.hybridocrlib.mlkit.text.TextRecognitionProcessor.PASSPORT_TD_3_LINE_1_REGEX;
import static com.datachecker.hybridocrlib.mlkit.text.TextRecognitionProcessor.PASSPORT_TD_3_LINE_2_REGEX;
import static com.datachecker.hybridocrlib.mlkit.text.TextRecognitionProcessor.TYPE_ID_CARD;
import static com.datachecker.hybridocrlib.model.Constants.ERROR_CODE.MRZ_SCAN_FAILED;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.datachecker.hybridocrlib.mlkit.text.TextRecognitionProcessor;
import com.datachecker.hybridocrlib.model.Constants;
import com.datachecker.hybridocrlib.model.DocType;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


import net.sf.scuba.data.Gender;

import org.jmrtd.lds.icao.MRZInfo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HybridOCRLib implements TextRecognitionProcessor.ResultListener {

    private String scannedTextBuffer = "";
    private DCOCRResultListener resultListener;
    MRZInfo mrzInfo;
    AppCompatActivity activity;




    public void greet(){
        System.out.println("Hello World.!!!");
    }

    public HybridOCRLib() {

    }

    public void scanImage(String dataString, DCOCRResultListener resultListener){
        this.resultListener = resultListener;
        int angle = 0;
        for (int i = 0; i < 2; i++) {
            Matrix matrix = new Matrix();

            if (angle == 90) {
                angle = -90;
            } else{
                angle = 90;
            }
            matrix.postRotate(angle);

            Bitmap bitmap = StringToBitMap(dataString);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inTargetDensity = 1;
            rotatedBitmap.setDensity(Bitmap.DENSITY_NONE);

            int oneThirdHeight = rotatedBitmap.getHeight() / 3;
            int fromHere = (int) (rotatedBitmap.getHeight() * 0.5) + (int) (oneThirdHeight / 2);
//        Bitmap croppedBitmap = Bitmap.createBitmap(rotatedBitmap, 0, rotatedBitmap.getHeight() / 2, rotatedBitmap.getWidth(), rotatedBitmap.getHeight() / 2);
            Bitmap croppedBitmap = Bitmap.createBitmap(rotatedBitmap, 0, fromHere, rotatedBitmap.getWidth(), oneThirdHeight);
            runTextRecognition(croppedBitmap);
        }
    }

    private void runTextRecognition(Bitmap mSelectedImage) {

        //prepare input image using bitmap
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);

        //creating TextRecognizer instance
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        //process the image
        recognizer.process(image)
                .addOnSuccessListener(
                        texts -> {
                            //Task completed successfully
                            processTextRecognitionResult(texts, activity);
                        })
                .addOnFailureListener(
                        e -> {
                            // Task failed with an exception
                            e.printStackTrace();
                        });
    }

    private void processTextRecognitionResult(Text texts, AppCompatActivity activity) {
        scannedTextBuffer = "";
        List<Text.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(activity, "No text found", Toast.LENGTH_SHORT).show();
            return;
        }
        for (Text.TextBlock block : texts.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                for (Text.Element element : line.getElements()) {
                    //draws the bounding box around the element.
                    Log.d("RECOGNISEDTEXT", element.getText());
                    filterScannedText( element);
                }
            }
        }

        if (mrzInfo != null){
            Log.d(TAG, "Calling finishScanning2: " + mrzInfo.getDocumentNumber());
            finishScanning(mrzInfo);
        } else {
            Log.d(TAG, "Failed Calling finishScanning2: MRZINFO is NULL");
        }
    }

    private void finishScanning(final MRZInfo mrzInfo) {
        try {
            if(isMrzValid(mrzInfo)) {
                // Delay returning result 1 sec. in order to make mrz text become visible on graphicOverlay by user
                // You want to call 'resultListener.onSuccess(mrzInfo)' without no delay
                Log.d(TAG, "MRZInfo: " + mrzInfo.getDocumentNumber());
                resultListener.onSuccessMRZScan(mrzInfo);
            } else {
                resultListener.onFailure(MRZ_SCAN_FAILED);
            }

        } catch(Exception exp) {
            Log.d(TAG, "MRZ DATA is not valid");
        }
    }

    private void filterScannedText( Text.Element element) {
        scannedTextBuffer += element.getText();
        DocType docType;
        docType = getDocumentType(scannedTextBuffer);

        Log.d(TAG, "Scanned text Buffer is : " + scannedTextBuffer);

        if(docType == DocType.ID_CARD) {
            Log.d("ID_CARD", "**** ID_CARD Detected");
//            Log.d(TAG, "Scanned text Buffer is : " + scannedTextBuffer);
            Pattern patternIDCardTD1Line1 = Pattern.compile(ID_CARD_TD_1_LINE_1_REGEX);
            Matcher matcherIDCardTD1Line1 = patternIDCardTD1Line1.matcher(scannedTextBuffer);

            Pattern patternIDCardTD1Line2 = Pattern.compile(ID_CARD_TD_1_LINE_2_REGEX);
            Matcher matcherIDCardTD1Line2 = patternIDCardTD1Line2.matcher(scannedTextBuffer);

            if(matcherIDCardTD1Line1.find() && matcherIDCardTD1Line2.find()) {
                String line1 = matcherIDCardTD1Line1.group(0);
                String line2 = matcherIDCardTD1Line2.group(0);
                int indexOfID = line1.indexOf(TYPE_ID_CARD);
                if ( indexOfID >= 0) {
                    line1 = line1.substring(line1.indexOf(TYPE_ID_CARD));
                    String documentNumber = line1.substring(5, 14);
                    documentNumber = documentNumber.replace("O", "0");
                    String dateOfBirthDay = line2.substring(0, 6);
                    String expiryDate = line2.substring(8, 14);

                    Log.d(TAG, "Scanned Text Buffer ID Card ->>>> " + "Doc Number: " + documentNumber + " DateOfBirth: " + dateOfBirthDay + " ExpiryDate: " + expiryDate);

                    mrzInfo = buildTempMrz(documentNumber, dateOfBirthDay, expiryDate);


                }
            }
        } else if (docType == DocType.PASSPORT) {
            Log.d("PASSPORT", "**** PASSPORT Detected");

            Pattern patternPassportTD3Line1 = Pattern.compile(PASSPORT_TD_3_LINE_1_REGEX);
            Matcher matcherPassportTD3Line1 = patternPassportTD3Line1.matcher(scannedTextBuffer);

            Pattern patternPassportTD3Line2 = Pattern.compile(PASSPORT_TD_3_LINE_2_REGEX);
            Matcher matcherPassportTD3Line2 = patternPassportTD3Line2.matcher(scannedTextBuffer);

            if (matcherPassportTD3Line1.find() && matcherPassportTD3Line2.find()) {
                String line2 = matcherPassportTD3Line2.group(0);
                String documentNumber = line2.substring(0, 9);
                documentNumber = documentNumber.replace("O", "0");
                String dateOfBirthDay = line2.substring(13, 19);
                String expiryDate = line2.substring(21, 27);

                Log.d(TAG, "Scanned Text Buffer Passport ->>>> " + "Doc Number: " + documentNumber + " DateOfBirth: " + dateOfBirthDay + " ExpiryDate: " + expiryDate);

                mrzInfo = buildTempMrz(documentNumber, dateOfBirthDay, expiryDate);

            }
        }
    }

    private MRZInfo buildTempMrz(String documentNumber, String dateOfBirth, String expiryDate) {
        MRZInfo mrzInfo = null;
        try {
            mrzInfo = new MRZInfo("P","NNN", "", "", documentNumber, "NNN", dateOfBirth, Gender.UNSPECIFIED, expiryDate, "");
        } catch (Exception e) {
            Log.d(TAG, "MRZInfo error : " + e.getLocalizedMessage());
        }

        return mrzInfo;
    }

    private DocType getDocumentType(String scannedTextBuffer) {
        if (scannedTextBuffer.matches(".*I<[A-Z]{3}.*")){
            return DocType.ID_CARD;
        } else if (scannedTextBuffer.matches(".*P<[A-Z]{3}.*")) {
            return DocType.PASSPORT;
        } else {
            return DocType.OTHER;
        }
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    private boolean isMrzValid(MRZInfo mrzInfo) {
        return mrzInfo.getDocumentNumber() != null && mrzInfo.getDocumentNumber().length() >= 8 &&
                mrzInfo.getDateOfBirth() != null && mrzInfo.getDateOfBirth().length() == 6 &&
                mrzInfo.getDateOfExpiry() != null && mrzInfo.getDateOfExpiry().length() == 6;
    }

    @Override
    public void onSuccess(MRZInfo mrzInfo) {

    }

    @Override
    public void onError(Exception exp) {

    }

    public interface DCOCRResultListener {
        void onSuccessMRZScan(MRZInfo mrzInfo);
        void onFailure(Constants.ERROR_CODE error);
    }
}
