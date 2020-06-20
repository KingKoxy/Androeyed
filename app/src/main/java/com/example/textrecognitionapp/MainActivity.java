package com.example.textrecognitionapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    ImageButton cameraBtn;
    ImageButton galleryBtn;
    ImageButton libraryBtn;
    ImageButton saveBtn;
    ImageButton playBtn;
    ImageButton stopBtn;
    ImageButton replayBtn;
    EditText textBox;
    Speaker speaker;

    Uri currentPhotoPath;

    //Codes:
    final int CAMERA_CODE = 0, LIBRARY_CODE = 1, GALLERY_CODE = 2, CHECK_TTS_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraBtn = findViewById(R.id.btnCamera);
        galleryBtn = findViewById(R.id.btnGallery);
        libraryBtn = findViewById(R.id.btnLibrary);
        saveBtn = findViewById(R.id.btnSave);
        textBox = findViewById(R.id.textBox);
        playBtn = findViewById(R.id.btnPlay);
        stopBtn = findViewById(R.id.btnStop);
        replayBtn = findViewById(R.id.btnReplay);


        //Setzt die OnClick Methoden
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraIntent();
            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_CODE);
            }
        });
        libraryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listIntent = new Intent(MainActivity.this, LibraryActivity.class);
                startActivityForResult(listIntent, LIBRARY_CODE);
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveText();
            }
        });

        checkTTS();
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readOutLoud();
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speaker.stop();
            }
        });
        replayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speaker.stop();
                readOutLoud();
            }
        });
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Falls Resultat aus Galerie
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && null != data) {
            //Erfassen des Fotopfades
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            cursor.close();

            //Lesen des Textes auf Foto
            readTextFromImage(BitmapFactory.decodeFile(picturePath, options));
        } else if (requestCode == CAMERA_CODE) {
            //Falls Resultat aus Kamera
            //Rotieren und Scalieren des Fotos, um lesbar zu machen
            Bitmap bitmap = null;
            try {
                bitmap = getRotatedImage(currentPhotoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Erfassen des Textes auf Foto
            readTextFromImage(bitmap);

        } else if (requestCode == LIBRARY_CODE && resultCode == RESULT_OK && null != data) {
            //Falls Resultat aus LibraryActivity
            Uri selectedImage = data.getData();
            //Setzen des EditTextes zu gespeichertem Text
            textBox.setText(readFromFile(this, selectedImage.toString()));

        } else if (requestCode == CHECK_TTS_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                speaker = new Speaker(this);
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }
    }

    Bitmap getRotatedImage(Uri imagePath) throws IOException {
        Context context = this;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(imagePath);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(imagePath);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, imagePath);
        return img;
    }

    private void startCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            //Erstellen einer Datei für das zukünftige Foto
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                currentPhotoPath = photoURI;
                startActivityForResult(cameraIntent, CAMERA_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        //Zwischenspeichern eines Fotos der Kamera für höhere Auflösung
        String imageFileName = "temp";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    void saveText() {
        String title;
        if (!textBox.getText().toString().matches("")) {
            try {
                String fileData = textBox.getText().toString();
                title = (System.nanoTime()) + ".txt";
                writeToFile(fileData, this, title);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast toast = Toast.makeText(this, "Kein Text vorhanden!", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    void readOutLoud() {
        String text = getReadableText(((EditText) this.findViewById(R.id.textBox)).getText().toString());
        speaker.speak(text);
    }

    String getReadableText(String input) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) != '\n') {
                output.append(input.charAt(i));
            } else {
                output.append(' ');
            }
        }
        return output.toString();
    }

    private void checkTTS() {
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_TTS_CODE);
    }

    private String readFromFile(Context context, String title) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(title);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString).append("\n");
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private void writeToFile(String data, Context context, String name) throws IOException {
        File path = context.getFilesDir();
        File file = new File(path, name);
        FileOutputStream stream = new FileOutputStream(file);
        try {
            stream.write(data.getBytes());
            Toast toast = Toast.makeText(context, "Datei wurde erfolgreich gespeichert!", Toast.LENGTH_LONG);
            toast.show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stream.close();
        }

    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void readTextFromImage(Bitmap bm) {
        //Erstellen eines Textregognizers
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        Frame.Builder imageFrameBuilder = new Frame.Builder();
        imageFrameBuilder.setBitmap(bm);
        Frame imageFrame = imageFrameBuilder.build();
        if (textRecognizer.isOperational()) {
            //Erkennen des Textes auf Foto
            final SparseArray<TextBlock> items = textRecognizer.detect(imageFrame);
            //Falls Text erkennbar
            if (items.size() != 0) {
                //Setzen des Textes in EditText
                textBox.post(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < items.size(); ++i) {
                            TextBlock item = items.valueAt(i);
                            stringBuilder.append(item.getValue());
                            stringBuilder.append("\n");
                            textBox.setText(stringBuilder.toString());
                        }
                    }
                });
            } else {
                Toast toast = Toast.makeText(this, "Kein Text erkennbar", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}
