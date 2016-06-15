package com.navibees.sdk.model.tts;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.navibees.sdk.AppManager;
import com.navibees.sdk.R;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.license.NaviBeesFeature;
import com.navibees.sdk.model.metadata.json.Portal;
import com.navibees.sdk.util.CommonUtils;
import com.navibees.sdk.util.Log;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by nabilnoaman on 12/26/15.
 */
final public class TTSManager {

    private String previousSpokenText = null;//We will consider the text will be spoke now is the previousSpokenText , before running it.
    private String latestTextToSpeakAfterWaiting = null;
    private boolean isArrived = false;
    private TextToSpeech tts = null;
    private boolean isTTSReady = false;
    private Context context = null;

    private boolean enableWaiting = false;

    static final String TAG = "TTSManager";


    int simulation_counter = -7;//0 for the same floor , -7 routing in different floor

    public TTSManager(Context context) throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException {
        this.context = context;
        AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature._2D_Maps);
        AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.Positioning);
        AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.Multi_Floor_Navigation);
        AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.TurnByTurn_Navigation);
        AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.Navigation_TTS);
        try {
            tts = new TextToSpeech(context, onInitListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {


            if (status == TextToSpeech.SUCCESS) {

                int result = tts.setLanguage(Locale.US);

                //int result = tts.isLanguageAvailable(new Locale("ar"));
                // tts.setPitch(5); // set pitch level
                // tts.setSpeechRate(2); // set speech speed rate

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {

                    Log.e(TAG , "TTS LANG_MISSING_DATA or LANG_NOT_SUPPORTED");

                    //Launch intent to let user install language
                    Intent installIntent = new Intent();
                    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    context.startActivity(installIntent);

                } else {
                    isTTSReady = true;
                    tts.setOnUtteranceProgressListener(utteranceProgressListener);

                    if(CommonUtils.isArabicLang()) {
                        mapArabicVoices();
                    }

                }

            } else {
                Log.e(TAG, "Initilization Failed");
            }
        }
    };

    private void mapArabicVoices() {
        String packageName = context.getPackageName();

        //Go Straight
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_go_straight), packageName , R.raw.turn_by_turn_message_go_straight);

        //Arrive
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_arrive), packageName , R.raw.turn_by_turn_message_arrive);


        //turn right
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_0), packageName , R.raw.turn_by_turn_message_compltete_turn_right_0);

        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_1), packageName , R.raw.turn_by_turn_message_compltete_turn_right_1);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_2), packageName , R.raw.turn_by_turn_message_compltete_turn_right_2);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_3), packageName , R.raw.turn_by_turn_message_compltete_turn_right_3);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_4), packageName , R.raw.turn_by_turn_message_compltete_turn_right_4);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_5), packageName , R.raw.turn_by_turn_message_compltete_turn_right_5);


        //turn gentle right
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_gentle_right_0), packageName , R.raw.turn_by_turn_message_compltete_turn_right_0);

        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_gentle_right_1), packageName , R.raw.turn_by_turn_message_compltete_turn_gentle_right_1);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_gentle_right_2), packageName , R.raw.turn_by_turn_message_compltete_turn_gentle_right_2);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_gentle_right_3), packageName , R.raw.turn_by_turn_message_compltete_turn_gentle_right_3);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_gentle_right_4), packageName , R.raw.turn_by_turn_message_compltete_turn_gentle_right_4);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_gentle_right_5), packageName , R.raw.turn_by_turn_message_compltete_turn_gentle_right_5);

        //turn sharp right
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_right_0), packageName , R.raw.turn_by_turn_message_compltete_turn_right_0);

        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_right_1), packageName , R.raw.turn_by_turn_message_compltete_turn_sharp_right_1);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_right_2), packageName , R.raw.turn_by_turn_message_compltete_turn_sharp_right_2);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_right_3), packageName , R.raw.turn_by_turn_message_compltete_turn_sharp_right_3);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_right_4), packageName , R.raw.turn_by_turn_message_compltete_turn_sharp_right_4);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_right_5), packageName , R.raw.turn_by_turn_message_compltete_turn_sharp_right_5);


        //turn left
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_left_0), packageName , R.raw.turn_by_turn_message_compltete_turn_left_0);

        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_left_1), packageName , R.raw.turn_by_turn_message_compltete_turn_left_1);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_left_2), packageName , R.raw.turn_by_turn_message_compltete_turn_left_2);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_left_3), packageName , R.raw.turn_by_turn_message_compltete_turn_left_3);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_left_4), packageName , R.raw.turn_by_turn_message_compltete_turn_left_4);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_left_5), packageName , R.raw.turn_by_turn_message_compltete_turn_left_5);


        //turn gentle left
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_gentle_left_0), packageName , R.raw.turn_by_turn_message_compltete_turn_left_0);

        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_gentle_left_1), packageName , R.raw.turn_by_turn_message_compltete_turn_gentle_left_1);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_gentle_left_2), packageName , R.raw.turn_by_turn_message_compltete_turn_gentle_left_2);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_gentle_left_3), packageName , R.raw.turn_by_turn_message_compltete_turn_gentle_left_3);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_gentle_left_4), packageName , R.raw.turn_by_turn_message_compltete_turn_gentle_left_4);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_gentle_left_5), packageName , R.raw.turn_by_turn_message_compltete_turn_gentle_left_5);

        //turn sharp left
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_left_0), packageName , R.raw.turn_by_turn_message_compltete_turn_left_0);

        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_left_1), packageName , R.raw.turn_by_turn_message_compltete_turn_sharp_left_1);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_left_2), packageName , R.raw.turn_by_turn_message_compltete_turn_sharp_left_2);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_left_3), packageName , R.raw.turn_by_turn_message_compltete_turn_sharp_left_3);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_left_4), packageName , R.raw.turn_by_turn_message_compltete_turn_sharp_left_4);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_left_5), packageName , R.raw.turn_by_turn_message_compltete_turn_sharp_left_5);

        //turn distance_to_destination
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_destination_1), packageName , R.raw.turn_by_turn_message_distance_to_destination_1);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_destination_2), packageName , R.raw.turn_by_turn_message_distance_to_destination_2);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_destination_3), packageName , R.raw.turn_by_turn_message_distance_to_destination_3);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_destination_4), packageName , R.raw.turn_by_turn_message_distance_to_destination_4);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_destination_5), packageName , R.raw.turn_by_turn_message_distance_to_destination_5);


        //Portals , Check if text startWith one of next UTTERANCES

        //Stairs
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_stairs_0), packageName , R.raw.turn_by_turn_message_distance_to_stairs_0);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_stairs_1), packageName , R.raw.turn_by_turn_message_distance_to_stairs_1);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_stairs_2), packageName , R.raw.turn_by_turn_message_distance_to_stairs_2);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_stairs_3), packageName , R.raw.turn_by_turn_message_distance_to_stairs_3);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_stairs_4), packageName , R.raw.turn_by_turn_message_distance_to_stairs_4);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_stairs_5), packageName , R.raw.turn_by_turn_message_distance_to_stairs_5);
        //Elevators
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_elevator_0), packageName , R.raw.turn_by_turn_message_distance_to_elevator_0);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_elevator_1), packageName , R.raw.turn_by_turn_message_distance_to_elevator_1);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_elevator_2), packageName , R.raw.turn_by_turn_message_distance_to_elevator_2);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_elevator_3), packageName , R.raw.turn_by_turn_message_distance_to_elevator_3);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_elevator_4), packageName , R.raw.turn_by_turn_message_distance_to_elevator_4);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_elevator_5), packageName , R.raw.turn_by_turn_message_distance_to_elevator_5);
        //Escalator
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_escalator_0), packageName , R.raw.turn_by_turn_message_distance_to_escalator_0);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_escalator_1), packageName , R.raw.turn_by_turn_message_distance_to_escalator_1);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_escalator_2), packageName , R.raw.turn_by_turn_message_distance_to_escalator_2);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_escalator_3), packageName , R.raw.turn_by_turn_message_distance_to_escalator_3);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_escalator_4), packageName , R.raw.turn_by_turn_message_distance_to_escalator_4);
        tts.addSpeech(context.getResources().getString(R.string.turn_by_turn_message_distance_to_escalator_5), packageName , R.raw.turn_by_turn_message_distance_to_escalator_5);


    }


    public void speak(String text){
        if(text == null || isArrived)return;

        //text = getSimulatedStringInDifferentFloor();//getSimulatedStringInTheSameFloor();


        if(CommonUtils.isArabicLang()) {
            text = text.replace("  ", " ");

            if (text.startsWith(context.getResources().getString(R.string.turn_by_turn_message_distance_to_stairs_prefix))) {
                text = text.substring(0, context.getResources().getString(R.string.turn_by_turn_message_distance_to_stairs_prefix).length() + 8);
            } else if (text.startsWith(context.getResources().getString(R.string.turn_by_turn_message_distance_to_elevator_prefix))) {
                text = text.substring(0, context.getResources().getString(R.string.turn_by_turn_message_distance_to_elevator_prefix).length() + 8);
            } else if (text.startsWith(context.getResources().getString(R.string.turn_by_turn_message_distance_to_escalator_prefix))) {
                text = text.substring(0, context.getResources().getString(R.string.turn_by_turn_message_distance_to_escalator_prefix).length() + 8 );
            } else if(text.startsWith(context.getResources().getString(R.string.turn_by_turn_message_go_to_floor)) && (AppManager.getInstance().getTurnByTurnNavigation(context) != null)) {


                if(Portal.PORTAL_TYPE_ARABIC[0].equalsIgnoreCase(AppManager.getInstance().getTurnByTurnNavigation(context).getCurrentUsedPortal().getTypeWRTLang())){
                    //المصعد
                    text = context.getResources().getString(R.string.turn_by_turn_message_distance_to_elevator_0);
                }else if(Portal.PORTAL_TYPE_ARABIC[1].equalsIgnoreCase(AppManager.getInstance().getTurnByTurnNavigation(context).getCurrentUsedPortal().getTypeWRTLang())){
                    //السلم
                    text = context.getResources().getString(R.string.turn_by_turn_message_distance_to_stairs_0);
                }else if(Portal.PORTAL_TYPE_ARABIC[2].equalsIgnoreCase(AppManager.getInstance().getTurnByTurnNavigation(context).getCurrentUsedPortal().getTypeWRTLang())){
                    // السلم المتحرك
                    text = context.getResources().getString(R.string.turn_by_turn_message_distance_to_escalator_0);
                }

            }
        }


        if(previousSpokenText == null){//Start Routing
            previousSpokenText = new String(text);
            speakImmediately(text);
        }else if(text.equalsIgnoreCase(context.getResources().getString(R.string.turn_by_turn_message_arrive))) {//Arrived
                isArrived = true;
                previousSpokenText = null;
                latestTextToSpeakAfterWaiting = null;
                enableWaiting = false;
                speakImmediately(text);
        }else if (!previousSpokenText.equalsIgnoreCase(text)) {//Between Start & Arrival
            //addQueueSpeak(text);
            if(!tts.isSpeaking()) {
                previousSpokenText = new String(text);;
                speakImmediately(previousSpokenText);
            }else {
                latestTextToSpeakAfterWaiting = text;
                enableWaiting = true;
            }
        }
    }


    private void addQueueSpeak(String text) {
        if (isTTSReady) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_ADD, null);
            }
        }else
            Log.e(TAG, "TTS Not Initialized");
    }

    private void speakImmediately(String text) {

        if (isTTSReady) {

            HashMap<String, String> directions = new HashMap<String, String>();
            directions.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
            directions.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, directions);
            }
        } else
            Log.e(TAG, "TTS Not Initialized");
    }

    public void resetTTS(){
        previousSpokenText = null;
        latestTextToSpeakAfterWaiting = null;
        enableWaiting = false;
        isArrived = false;
        simulation_counter = -7;//0 for the same floor , -7 routing in different floor

        if(tts != null) {
            tts.stop();
        }
    }

    public void shutDown() {
        if(tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }


    private UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onDone(String utteranceId) {
            if(enableWaiting && latestTextToSpeakAfterWaiting !=null) {
                enableWaiting = false;
                if (!previousSpokenText.equalsIgnoreCase(latestTextToSpeakAfterWaiting)) {
                    previousSpokenText = new String(latestTextToSpeakAfterWaiting);
                    speakImmediately(latestTextToSpeakAfterWaiting);
                }
            }
        }

        @Override
        public void onError(String utteranceId) {

        }
    };



    private String getSimulatedStringInTheSameFloor() {
        simulation_counter++;

        /*
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

        switch (simulation_counter){
            case 1://Go Straight
            case 2:
            case 3:
                return context.getResources().getString(R.string.turn_by_turn_message_go_straight);
            case 4://Go Right after 2 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_2);
            case 5://Go Right after 1 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_1);
            case 6://Go Straight
            case 7:
            case 8:
            case 9:
                return context.getResources().getString(R.string.turn_by_turn_message_go_straight);
            case 10://Go Right after 2 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_2);
            case 11://Go Right after 1 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_1);
            case 12:
                //Go Right after 0 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_0);
            case 13://Go Straight
            case 14:
            case 15:
                return context.getResources().getString(R.string.turn_by_turn_message_go_straight);
            case 16://Go left after 2 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_left_2);
            case 17://Go left after 1 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_left_1);
            case 18://Go left after 0 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_left_0);
            case 19://Go Straight
                return context.getResources().getString(R.string.turn_by_turn_message_go_straight);
            case 20://distance_to_destination 2
                return context.getResources().getString(R.string.turn_by_turn_message_distance_to_destination_2);
            case 21://distance_to_destination 1
                return context.getResources().getString(R.string.turn_by_turn_message_distance_to_destination_1);
            case 22://Arrived
            case 23:
            case 24:
            case 25:
                return context.getResources().getString(R.string.turn_by_turn_message_arrive);
            case 26://distance_to_destination 3
            case 27:
            case 28:
            case 29:
                return context.getResources().getString(R.string.turn_by_turn_message_distance_to_destination_3);
            default:
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_right_1);
        }
    }

    private String getSimulatedStringInDifferentFloor(){
        simulation_counter++;

        /*
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

        switch (simulation_counter){
            //Floor 0
            case -6://Go Straight
                return context.getResources().getString(R.string.turn_by_turn_message_go_straight);
            case -5://distance_to_stairs 3
                return context.getResources().getString(R.string.turn_by_turn_message_distance_to_stairs_3);
            case -4://distance_to_stairs 2
                return context.getResources().getString(R.string.turn_by_turn_message_distance_to_stairs_2);
            case -3://distance_to_stairs 1
                return context.getResources().getString(R.string.turn_by_turn_message_distance_to_stairs_1);
            case -2://Go to Floor 1
            case -1:
            case 0:
                return context.getResources().getString(R.string.turn_by_turn_message_go_to_floor) + " 1";
            //Floor 1 from Main Stairs to Toilet
            case 1://Go Straight
            case 2:
            case 3:
                return context.getResources().getString(R.string.turn_by_turn_message_go_straight);
            case 4://Go Right after 2 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_2);
            case 5://Go Right after 1 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_1);
            case 6://Go Straight
            case 7:
            case 8:
            case 9:
                return context.getResources().getString(R.string.turn_by_turn_message_go_straight);
            case 10://Go Right after 2 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_2);
            case 11://Go Right after 1 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_1);
            case 12:
                //Go Right after 0 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_right_0);
            case 13://Go Straight
            case 14:
            case 15:
                return context.getResources().getString(R.string.turn_by_turn_message_go_straight);
            case 16://Go left after 2 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_left_2);
            case 17://Go left after 1 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_left_1);
            case 18://Go left after 0 meter
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_left_0);
            case 19://Go Straight
                return context.getResources().getString(R.string.turn_by_turn_message_go_straight);
            case 20://distance_to_destination 2
                return context.getResources().getString(R.string.turn_by_turn_message_distance_to_destination_2);
            case 21://distance_to_destination 1
                return context.getResources().getString(R.string.turn_by_turn_message_distance_to_destination_1);
            case 22://Arrived
            case 23:
            case 24:
            case 25:
                return context.getResources().getString(R.string.turn_by_turn_message_arrive);
            case 26://distance_to_destination 3
            case 27:
            case 28:
            case 29:
                return context.getResources().getString(R.string.turn_by_turn_message_distance_to_destination_3);
            default:
                return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn_sharp_right_1);
        }
    }

}
