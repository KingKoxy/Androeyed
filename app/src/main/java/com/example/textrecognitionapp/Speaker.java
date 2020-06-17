package com.example.textrecognitionapp;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

public class Speaker implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    private boolean ready = false;

    Speaker(Context context){
        tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            tts.setLanguage(Locale.GERMAN);
            ready = true;
        }else{
            ready = false;
        }
    }

    public void speak(String text){
        if(ready) {
            HashMap<String, String> hash = new HashMap<>();
            hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                    String.valueOf(AudioManager.STREAM_NOTIFICATION));
            tts.speak(text, TextToSpeech.QUEUE_ADD, hash);
        }
    }

    public void pause(int duration){
        tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }

    public void stop() {
        tts.stop();
    }
}