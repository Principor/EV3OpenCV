package com.example.ev3opencv;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class SpeechHandler implements RecognitionListener {

    private static final String MENU_SEARCH = "menu";
    private static final String[] SEARCH_PHRASES = new String[]{"explore","stay","fetch","spin"};

    private EV3Communicator ev3Communicator;

    /* Recognition object */
    private SpeechRecognizer recognizer;

    MainActivity parent;

    public SpeechHandler(EV3Communicator ev3Communicator, MainActivity parent){
        this.ev3Communicator = ev3Communicator;
        this.parent = parent;
        runRecognizerSetup();
    }

    @SuppressLint("StaticFieldLeak")
    private void runRecognizerSetup(){
        new AsyncTask<Void, Void, Exception>(){

            @Override
            protected Exception doInBackground(Void... params) {
                try{
                    Assets assets = new Assets(parent);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                }catch(IOException e){
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    System.out.println(result.getMessage());
                } else {
                    recognizer.startListening(MENU_SEARCH);
                }
            }
        }.execute();
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                // Disable this line if you don't want recognizer to save raw
                // audio files to app's storage
                //.setRawLogDir(assetsDir)
                .getRecognizer();
        recognizer.addListener(this);
        // Create your custom grammar-based search
        File menuGrammar = new File(assetsDir, "mymenu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
    }

    public void onBeginningOfSpeech() {
    }

    public void onEndOfSpeech() {
    }

    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        String text = hypothesis.getHypstr();

        for (String searchPhrase : SEARCH_PHRASES) {
            if(text.equals(searchPhrase)){
                Log.i(MainActivity.TAG, searchPhrase);
                ev3Communicator.sendMessage(searchPhrase.toUpperCase());
                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);
            }
        }
    }

    public void onResult(Hypothesis hypothesis) {
    }

    public void onError(Exception error) {
    }

    public void onTimeout() {
    }

    public void stop(){
        if(recognizer != null){
            recognizer.cancel();
            recognizer.shutdown();
        }
    }
}
