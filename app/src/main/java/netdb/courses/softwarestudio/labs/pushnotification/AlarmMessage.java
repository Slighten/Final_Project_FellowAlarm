package netdb.courses.softwarestudio.labs.pushnotification;


import java.text.SimpleDateFormat;
import java.util.Date;

// import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.content.Context;
import android.os.Vibrator;
import java.util.Calendar;
import java.util.Locale;
// import java.util.Random;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
// import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
/**
 * Created by Slighten on 2015/1/19.
 */
public class AlarmMessage extends ActionBarActivity {

    private Context context;
    private static MediaPlayer mediaPlayer = null;
    private static Vibrator vibrator;
    private static AudioManager audio = null;
    protected static AlarmManager alarm = null;
    private TextView msg = null;
    // private Calendar calendar = Calendar.getInstance();
    static public SharedPreferences prefs;
    static public SharedPreferences.Editor editor;
    private int counter;
    private static final String FILENAME = "dom";
    private static final int ONESEC = 1000;
    private static final int ONEMIN = 60000;
    private int snoozeIntevalinMills = 60000;
    private boolean flag = false;
    private static long[] pattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pattern = new long[]{0, ((int) (Math.random() * 30) + 1) * 100, ((int) (Math.random() * 5) + 1) * 100, ((int) (Math.random() * 30) + 1) * 100};
        setContentView(R.layout.activity_main);
        msg = (TextView) findViewById(R.id.delete);
        prefs = getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        counter = prefs.getInt("counter", 0);

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audio.adjustVolume(AudioManager.ADJUST_RAISE, 0);
        if (counter >= 2)
            mediaPlayer = MediaPlayer.create(AlarmMessage.this, R.raw.extreme_clock_alarm);
        else
            mediaPlayer = MediaPlayer.create(AlarmMessage.this, R.raw.alarmsound);
        mediaPlayer.setVolume(1.0f, 1.0f);
        mediaPlayer.setLooping(true);
        /*try {
            mediaPlayer.prepare();
        } catch (Exception e){
            mediaPlayer.release();
        }*/
        mediaPlayer.start();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        vibrator.vibrate(pattern, 0);
        Dialog dialog = new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher3).setTitle("Times up!")
                .setMessage(new SimpleDateFormat("'Now it is' K : mm : ss a '!!'", Locale.ENGLISH)
                        .format(new Date(System.currentTimeMillis())))
                .setPositiveButton("Snooze", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelAlarm();
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method
                /*if ( keyCode == KeyEvent.KEYCODE_POWER || keyCode == KeyEvent.KEYCODE_SLEEP || keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
                    cancelAlarm();
                }*/
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    // cancelAlarm();
                }
                return true;
            }
        });
    }

    /*public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        Log.d("Focus debug", "Focus changed !");

        if(!hasFocus) {
            Log.d("Focus debug", "Lost focus !");

            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }*/

    /*
        @Override
        public void onAttachedToWindow() {
            this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
            super.onAttachedToWindow();
        }
    */
    private void cancelAlarm() {
        flag = true;
        editor = prefs.edit();
        if (counter < 3) {
            snoozeIntevalinMills = prefs.getInt("snoozeInteval", 0);
            snoozeIntevalinMills = (snoozeIntevalinMills < 5000) ? 60000 : snoozeIntevalinMills;

            alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(AlarmMessage.this, AlarmReceiver.class);
            intent.setAction("slighten.setalarm");
            PendingIntent sender = PendingIntent.getBroadcast(AlarmMessage.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + snoozeIntevalinMills, sender);
        }
        // msg.setText("Alarm time: " + calendar.get + ":" + calendar.MINUTE+1);
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            // mediaPlayer.release();
        }
        vibrator.cancel();
        int min = snoozeIntevalinMills / ONEMIN;
        int sec = (snoozeIntevalinMills % ONEMIN) / ONESEC;
        if (counter < 2) {
            Toast.makeText(AlarmMessage.this, "Snooze after " + ((min > 0) ? ((min == 1) ? min + " minite" : min + " minutes") : sec + " seconds"), Toast.LENGTH_SHORT).show();
            editor.putInt("counter", ++counter);
            editor.apply();
        } else if (counter == 2) {
            Toast.makeText(AlarmMessage.this, "Snooze again " + ((min > 0) ? ((min == 1) ? min + " minite" : min + " minutes") : sec + " seconds") + ".\nNext time I'll call someone else.", Toast.LENGTH_LONG).show();
            msg.setText("DELETE ALARM");
            editor.putInt("counter", ++counter);
            editor.apply();
        } else {
            Toast.makeText(AlarmMessage.this, "I'm calling !!!", Toast.LENGTH_SHORT).show();
            String telStr = MainActivity.PhoneNumber;
            Uri uri = Uri.parse("tel:" + telStr);
            Intent it = new Intent();
            it.setAction(Intent.ACTION_CALL);
            it.setData(uri);
            startActivity(it);
            // startActivityForResult(it, 5);

            msg.setText("DELETE ALARM");
            editor.putInt("call", 1);
            editor.apply();

        }
        // StaticWakeLock.lockOff(context);
        //if (counter < 3)
            AlarmMessage.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StaticWakeLock.lockOff(context);
        /*if (!flag)
            cancelAlarm();*/
        // cancelAlarm();
    }

    @Override
    protected void onStop() {
        super.onStop();
        StaticWakeLock.lockOff(context);
        /*if (!flag)
        cancelAlarm();*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        StaticWakeLock.lockOff(context);
        /*if (!flag)
        cancelAlarm();*/
        // cancelAlarm();
    }

   /* @Override
    protected void onResume() {
        super.onResume();
        /*alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AlarmMessage.this, AlarmReceiver.class);
        intent.setAction("slighten.setalarm");
        PendingIntent sender = PendingIntent.getBroadcast(AlarmMessage.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + snoozeIntevalinMills, sender);
        Toast.makeText(AlarmMessage.this, "To delete snooze, click \"DELETE ALARM\" button on bottom of the main screen.", Toast.LENGTH_LONG).show();
        AlarmMessage.this.finish();*/
    //}




    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

            // Toast.makeText(this, PhoneNumber, Toast.LENGTH_LONG).show();
            // Toast.makeText(this, "???", Toast.LENGTH_SHORT).show();
    }*/
}