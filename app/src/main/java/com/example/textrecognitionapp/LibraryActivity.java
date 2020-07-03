package com.example.textrecognitionapp;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LibraryActivity extends AppCompatActivity {

    FrameLayout frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        // Erfassen der Daten, die im ListView angezeigt werden sollen
        final List<String> listData = new ArrayList<String>();
        for (File f : this.getApplicationContext().getFilesDir().listFiles()) {
            listData.add(readFirstLine(f));
        }
        if (this.getFilesDir().list().length == 0) {
            Toast toast = Toast.makeText(this, R.string.nodata, Toast.LENGTH_LONG);
            toast.show();
            finish();
        }

        ListView listView = findViewById(R.id.listView);

        // Erstellen und zuweisen des ArrayAdapters unter verwendung des ListViewAdapters
        final ListViewAdapter listDataAdapter = new ListViewAdapter(this, listData.toArray(new String[listData.size()]));
        listView.setAdapter(listDataAdapter);

        frame = findViewById(R.id.linearLayout);
        frame.getForeground().setAlpha(0);
    }

    //Methode zum erfassen der ersten Zeile eines gespeicherten Textes
    String readFirstLine(File file) {
        String ret = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String text = br.readLine();
            ret += text;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }
}