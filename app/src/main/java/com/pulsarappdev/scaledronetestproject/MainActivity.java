package com.pulsarappdev.scaledronetestproject;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Member;
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;

import org.w3c.dom.Text;

import java.io.Console;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements RoomListener {

    // replace this with a real channelID from Scaledrone dashboard
    private String channelID = "jvT5ARJV2paVl8cl";
    private String devChannelID = "P0TQyXPArIhKKHjA";
    private String roomName = "observable-room";
    private EditText editText;
    private Scaledrone scaledrone;
    private MessageAdapter messageAdapter;
    private ListView messagesView;
    private TextView datawarning;
    private CheckBox safe_filter;
    private Vibrator v;
    private Toolbar toolbar;
    private ImageButton developerModeButton;
    private ArrayList<String> developerMenuOptions = new ArrayList<>();
    private Boolean doVibration;
    private TextView dev_ops_warning;
    private Boolean doNotifications = true;
    private Boolean lightTheme = true;

    private MenuItem clear_errors_button;

    private int ERROR_NOTIFICATIONS_SENT = 0;
    private int NEWMSG_NOTIFICATIONS_SENT = 0;

    static ArrayList<Integer> error_notification_ids=new ArrayList<Integer>();
    static ArrayList<Integer> message_notification_ids=new ArrayList<Integer>();

    private static final String NEWMSG_NOTIFICATION_CHANNEL_ID = "NEWMSG_notification_channel";
    private static final String ERROR_NOTIFICATION_CHANNEL_ID = "ERROR_notification_channel";

    MemberData data = new MemberData(getRandomName(), getRandomColor());

    // Start without a delay
    // Each element then alternates between vibrate, sleep, vibrate, sleep...\
    long[] errorVibrationPattern = {0, 50, 100, 50};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        datawarning = findViewById(R.id.data_warning);
        developerModeButton = findViewById(R.id.developerButton);
        dev_ops_warning = findViewById(R.id.dev_ops_warning);

        dev_ops_warning.setVisibility(View.GONE);

        developerModeButton.setVisibility(View.GONE);

        messageAdapter = new MessageAdapter(this);
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

        CharSequence error = getString(R.string.failed_to_send_toast);
        final Toast errorToast = Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT);

        // DEVELOPER MENU OPTIONS 00DEVMENU TODO
        developerMenuOptions.add("Switch to dev chat mode");
        developerMenuOptions.add("Notification Test");
        developerMenuOptions.add("Stop all vibrations");
        developerMenuOptions.add("Reset vibrations");
        developerMenuOptions.add("Stop all notifications");
        developerMenuOptions.add("Reset notifications");
        developerMenuOptions.add("Disable developer mode (removes settings)");
        developerMenuOptions.add("Disable developer mode and keep settings");
        developerMenuOptions.add("Email suggestions to devs");




        String stringappname = getApplicationContext().getString(R.string.action_bar_name_string);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        try {
            toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
            setSupportActionBar(toolbar);
        } catch (java.lang.NullPointerException e) {
            sendErrorNotification("NullPointerException","NullPointerException error while trying to set ActionBar. The ActionBar has not been set.\n\nYou can dismiss this notification.");
            e.printStackTrace();
        }

        scaledrone = new Scaledrone(channelID, data);
        scaledrone.connect(new Listener() {
            @Override
            public void onOpen() {
                System.out.println("Scaledrone connection open");
                scaledrone.subscribe(roomName, MainActivity.this);
                scaledrone.publish(roomName, getString(R.string.starting_published_message));
                scaledrone.publish(roomName, getString(R.string.starting_version_message));
                editText.getText().clear();
                System.out.println("Ok, did welcome message not toast");
            }

            @Override
            public void onOpenFailure(Exception ex) {
                System.err.println(ex);
                errorToast.show();
            }

            @Override
            public void onFailure(Exception ex) {
                errorToast.show();
                System.err.println(ex);
            }

            @Override
            public void onClosed(String reason) {
                errorToast.show();
                System.err.println(reason);
            }
        });

        Context context = getApplicationContext();
        CharSequence intro = context.getString(R.string.starting_message);
        int duration = Toast.LENGTH_LONG;
        Toast introToast = Toast.makeText(context, intro, duration);

        CharSequence versionInfo = context.getString(R.string.starting_version_info);
        Toast versionToast = Toast.makeText(context, versionInfo, duration);

        introToast.show();
        versionToast.show();

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) {
            // warn data
            datawarning.setText("(running on mobile data!)");
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        clear_errors_button = menu.findItem(R.id.clear_errors);

        MenuItem developerModeButton_menutemp = menu.findItem(R.id.toggle_developer_mode);
        developerModeButton_menutemp.setTitle("⚠ Developer Mode (OFF)");

        if (NEWMSG_NOTIFICATIONS_SENT==0&&ERROR_NOTIFICATIONS_SENT==0) {
            clear_errors_button.setEnabled(false);
            clear_errors_button.getIcon().setAlpha(130);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;

        } else if (id == R.id.restart_app) {

            sendErrorNotification("Restarting app...", "The WorldChat app is now restarting. \n\n You can dismiss this notification after the restart.");

            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(i);

            sendErrorNotification("Finished restart", "The WorldChat app has now restarted. \n\n You can dismiss this notification.");

        } else if (id == R.id.toggle_theme) {
            if (lightTheme) {
                getApplication().setTheme(R.style.DarkTheme);
                lightTheme = false;
            } else if(!lightTheme) {
                getApplication().setTheme(R.style.LightTheme);
                lightTheme = true;
            }

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.clear_errors) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            NEWMSG_NOTIFICATIONS_SENT = 0; // This and the next 3 lines will reset the Clear Errors button
            ERROR_NOTIFICATIONS_SENT = 0;
            clear_errors_button.setEnabled(false);
            clear_errors_button.getIcon().setAlpha(130);

        } else if (id == R.id.show_disclaimer) {
            showDialog(findViewById(id), getApplicationContext().getString(R.string.license_dialog_title), getApplicationContext().getString(R.string.license_dialog_message));
            showDialog(findViewById(id), getApplicationContext().getString(R.string.disclaimer_dialog_title), getApplicationContext().getString(R.string.disclaimer_dialog_message));
        } else if (id == R.id.toggle_developer_mode) {
            if (developerModeButton.getVisibility() == View.GONE || developerModeButton.getVisibility() == View.INVISIBLE) {
                developerModeButton.setVisibility(View.VISIBLE);
                item.setTitle("✓ Developer mode (E)");
            } else {
                developerModeButton.setVisibility(View.GONE);
                item.setTitle("⚠ Developer Mode (OFF)");
            }
        }

        return super.onOptionsItemSelected(item);
    }


    public void developerItemsDialog(View view) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setIcon(R.drawable.ic_user);
        builderSingle.setTitle("developer option menu");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
        for (int i = 0; i < developerMenuOptions.size(); i++) {
            arrayAdapter.add(developerMenuOptions.get(i));
        }

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

//        developerMenuOptions.add("Switch to dev chat mode");
//        developerMenuOptions.add("Notification Test");
//        developerMenuOptions.add("Stop all vibrations");
//        developerMenuOptions.add("Reset vibrations");
//        developerMenuOptions.add("Stop all notifications");
//        developerMenuOptions.add("Reset notifications");
//        developerMenuOptions.add("Disable developer mode (removes settings)");
//        developerMenuOptions.add("Disable developer mode and keep settings");
//        developerMenuOptions.add("Email suggestions to devs");

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                switch (which) {
                    case (0):
                        dev_ops_warning.setVisibility(View.VISIBLE);
                        dev_ops_warning.setText("!! DEVELOPER CHANNEL");

                        scaledrone = new Scaledrone(devChannelID, data);
                        scaledrone.connect(new Listener() {
                            @Override
                            public void onOpen() {
                                System.out.println("Scaledrone connection open");
                                scaledrone.subscribe(roomName, MainActivity.this);
                                scaledrone.publish(roomName, "New user in DEVELOPER ROOM");
                                scaledrone.publish(roomName, getString(R.string.starting_version_message));
                                editText.getText().clear();
                                System.out.println("Ok, did welcome message not toast");
                            }

                            Toast errorToast = Toast.makeText(getApplicationContext(), "Error while switching channels!", Toast.LENGTH_LONG);

                            @Override
                            public void onOpenFailure(Exception ex) {
                                System.err.println(ex);
                                errorToast.show();
                                dev_ops_warning.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(Exception ex) {
                                errorToast.show();
                                System.err.println(ex);
                                dev_ops_warning.setVisibility(View.GONE);
                            }

                            @Override
                            public void onClosed(String reason) {
                                errorToast.show();
                                System.err.println(reason);
                                dev_ops_warning.setVisibility(View.GONE);
                            }
                        });
                        break;
                    case (1):
                        testNotification(findViewById(R.id.developerButton));
                        break;
                    case (2):
                        dev_ops_warning.setVisibility(View.VISIBLE);
                        dev_ops_warning.setText(dev_ops_warning.getText().toString() + ", Vibration Disabled");
                        errorVibrationPattern = new long[]{0,0,0,0};
                        break;
                    case (3):
                        dev_ops_warning.setVisibility(View.VISIBLE);
                        dev_ops_warning.setText(dev_ops_warning.getText().toString().replace(", Vibration Disabled",""));
                        if (dev_ops_warning.getText().toString().equals("")) {
                            dev_ops_warning.setVisibility(View.GONE);
                        }
                        errorVibrationPattern = new long[]{0, 50, 100, 50};
                        break;
                    case (4):
                        doNotifications = false;
                        dev_ops_warning.setVisibility(View.VISIBLE);
                        dev_ops_warning.setText(dev_ops_warning.getText().toString() + ", Notifications disabled");
                        break;
                    case (5):
                        doNotifications = true;
                        dev_ops_warning.setVisibility(View.VISIBLE);
                        dev_ops_warning.setText(dev_ops_warning.getText().toString().replace(", Notifications disabled",""));
                        if (dev_ops_warning.getText().toString().equals("")) {
                            dev_ops_warning.setVisibility(View.GONE);
                        }
                        break;
                    case (6):
                        doNotifications = true;
                        errorVibrationPattern = new long[]{0, 50, 100, 50};
                        scaledrone = new Scaledrone(channelID, data);
                        scaledrone.connect(new Listener() {
                            @Override
                            public void onOpen() {
                                System.out.println("Scaledrone connection open");
                                scaledrone.subscribe(roomName, MainActivity.this);
                                scaledrone.publish(roomName, getString(R.string.back_in_global_msg));
                                scaledrone.publish(roomName, getString(R.string.starting_version_message));
                                editText.getText().clear();
                                System.out.println("Ok, did welcome message not toast");
                            }

                            Toast errorToast = Toast.makeText(getApplicationContext(), "Error while switching channels!", Toast.LENGTH_LONG);

                            @Override
                            public void onOpenFailure(Exception ex) {
                                System.err.println(ex);
                                errorToast.show();
                                dev_ops_warning.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(Exception ex) {
                                errorToast.show();
                                System.err.println(ex);
                                dev_ops_warning.setVisibility(View.GONE);
                            }

                            @Override
                            public void onClosed(String reason) {
                                errorToast.show();
                                System.err.println(reason);
                                dev_ops_warning.setVisibility(View.GONE);
                            }
                        });
                        dev_ops_warning.setText("");
                        dev_ops_warning.setVisibility(View.GONE);
                        developerModeButton.setVisibility(View.GONE);
                    case (7):
                        dev_ops_warning.setText("");
                        dev_ops_warning.setVisibility(View.GONE);
                        developerModeButton.setVisibility(View.GONE);

                }
            }
        });
        builderSingle.show();
    }


    public void testNotification(View view) {
        sendNewMsgNotification("Test","Test");
        sendErrorNotification("Error Test","Error Test");
    }

    // -----------------------------------
    // Notifications are below, use id
    // "00NOTIFY" to jump to this marker
    // -----------------------------------

    public void sendNewMsgNotification(String title, String message) { // This works
        if (doNotifications) {
            NEWMSG_NOTIFICATIONS_SENT++;

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NEWMSG_NOTIFICATION_CHANNEL_ID, "WorldChat New Message Notification", NotificationManager.IMPORTANCE_LOW);

                // Configure the notification channel.
                notificationChannel.setDescription("This is a low-level WorldChat notification channel. It will show your new messages.");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 100, 100, 100});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.setBigContentTitle(title);
            bigTextStyle.bigText(message);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NEWMSG_NOTIFICATION_CHANNEL_ID)
                    .setVibrate(new long[]{0, 100, 100, 100, 100, 100})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setSmallIcon(R.drawable.text_message_notification_baseline)
                    .setContentText(message)
                    .setStyle(bigTextStyle);

            notificationManager.notify("newmsg_notification", NEWMSG_NOTIFICATIONS_SENT, builder.build());

            try {
                clear_errors_button.setEnabled(true);
                clear_errors_button.getIcon().setAlpha(255);
            } catch (Exception e) {
                System.out.println("This isn't the first notification.");
            }
        }
    }

    public void sendErrorNotification(String title, String message) { // This works
        if (doNotifications) {
            ERROR_NOTIFICATIONS_SENT++;

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(ERROR_NOTIFICATION_CHANNEL_ID, "WorldChat Error Notification", NotificationManager.IMPORTANCE_HIGH);

                // Configure the notification channel.
                notificationChannel.setDescription("This is a high-level WorldChat notification channel. It will show errors.");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 100, 100, 100});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.setBigContentTitle(title);
            bigTextStyle.bigText(message);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ERROR_NOTIFICATION_CHANNEL_ID)
                    .setVibrate(new long[]{0, 100, 100, 100, 100, 100})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setSmallIcon(R.drawable.error_notification_baseline)
                    .setContentText(message)
                    .setStyle(bigTextStyle);

            notificationManager.notify("error_notification", ERROR_NOTIFICATIONS_SENT, builder.build());
            error_notification_ids.add(ERROR_NOTIFICATIONS_SENT - 1);

            try {
                clear_errors_button.setEnabled(true);
                clear_errors_button.getIcon().setAlpha(255);
            } catch (Exception e) {
                System.out.println("This isn't the first notification.");
            }
        }
    }


    public void showDialog(View view, String title, String message) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        // add a button
        builder.setPositiveButton("OK", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public int timesSent = 0;

    public void toggleSafeMode(View view) {
        Context context = getApplicationContext();
        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            scaledrone.publish(roomName, "++safe_mode_on");
        } else {
            scaledrone.publish(roomName, "++safe_mode_off");
        }
    }

    public void sendMessage(View view) {

        // Vibrate for 250 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(50);
        }

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        Context context = getApplicationContext();

        String message = editText.getText().toString();
        if (message.length() == 0) {
            v.vibrate(errorVibrationPattern, -1);
            System.out.println("No message entered");
            CharSequence text = context.getString(R.string.no_message_entered_toast);
            int duration = Toast.LENGTH_SHORT;
            Toast noMessageToast = Toast.makeText(context, text, duration);
            noMessageToast.show();
            editText.setText("");
            editText.setTextColor(Color.parseColor("black"));
            editText.setEnabled(true);
        } else if (message.equals("bug report")) {
            System.out.println("Got bug report request");
            int duration = Toast.LENGTH_LONG;
            CharSequence versioninfo = context.getString(R.string.starting_version_info);
            Toast versionToast = Toast.makeText(context, versioninfo, duration);
            versionToast.show();
            showDialog(view, context.getString(R.string.bug_report_dialog_title), context.getString(R.string.bug_report_dialog_text));
            editText.getText().clear();
        } else if (message.equals(getApplicationContext().getString(R.string.no_internet_default_textbox))) {
            System.out.print("User pressed send button while app was locked, unlocking interface.");
            editText.setText("");
            editText.setTextColor(Color.parseColor("black"));
            editText.setEnabled(true);
        } else if (message.toLowerCase().contains("http".toLowerCase()) || message.toLowerCase().contains("www".toLowerCase())) {
            v.vibrate(errorVibrationPattern, -1);
            Toast.makeText(context, context.getString(R.string.url_detected_toast), Toast.LENGTH_SHORT).show();
            editText.getText().clear();
        } else if (message.length() > 0) {
            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                System.out.println("Published " + message);
                scaledrone.publish(roomName, message);
                editText.getText().clear();
                timesSent++;
            } else {
                System.out.println("Didn't detect internet connection on send. Locking down app.");
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.internet_not_connected_toast), Toast.LENGTH_SHORT).show(); // You may think this is redundant but what if the internet is switched off between messages?
                editText.setText(getApplicationContext().getString(R.string.no_internet_default_textbox));
                editText.setTextColor(Color.parseColor("red"));
                editText.setEnabled(false);
                timesSent++;
                v.vibrate(errorVibrationPattern, -1);
            }
        }
    }

    public void messageOnTapCopy(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        TextView messageView = (TextView) view;
        ClipData clip = ClipData.newPlainText("Copied message text (BTW: Hello developer!)", messageView.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), "Copied to clipboard: " + messageView.getText().toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onOpen(Room room) {
        System.out.println("Connected to room");
    }

    @Override
    public void onOpenFailure(Room room, Exception ex) {
        System.err.println(ex);
    }

    @Override
    public void onMessage(Room room, final JsonNode json, final Member member) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final MemberData data = mapper.treeToValue(member.getClientData(), MemberData.class);
            boolean belongsToCurrentUser = member.getId().equals(scaledrone.getClientID());
            final Message message = new Message(json.asText(), data, belongsToCurrentUser);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageAdapter.add(message);
                    messagesView.setSelection(messagesView.getCount() - 1);
                }
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private String getRandomName() {
        String[] adjs = {"autumn", "hidden", "bitter", "misty", "silent", "empty", "dry", "dark", "summer", "icy", "delicate", "quiet", "white", "cool", "spring", "winter", "patient", "twilight", "dawn", "crimson", "wispy", "weathered", "blue", "billowing", "broken", "cold", "damp", "falling", "frosty", "green", "long", "late", "lingering", "bold", "little", "morning", "muddy", "old", "red", "rough", "still", "small", "sparkling", "throbbing", "shy", "wandering", "withered", "wild", "black", "young", "holy", "solitary", "fragrant", "aged", "snowy", "proud", "floral", "restless", "divine", "polished", "ancient", "purple", "lively", "nameless"};
        String[] nouns = {"waterfall", "river", "breeze", "moon", "rain", "wind", "sea", "morning", "snow", "lake", "sunset", "pine", "shadow", "leaf", "dawn", "glitter", "forest", "hill", "cloud", "meadow", "sun", "glade", "bird", "brook", "butterfly", "bush", "dew", "dust", "field", "fire", "flower", "firefly", "feather", "grass", "haze", "mountain", "night", "pond", "darkness", "snowflake", "silence", "sound", "sky", "shape", "surf", "thunder", "violet", "water", "wildflower", "wave", "water", "resonance", "sun", "wood", "dream", "cherry", "tree", "fog", "frost", "voice", "paper", "frog", "smoke", "star"};
        return (
                adjs[(int) Math.floor(Math.random() * adjs.length)] +
                        "_" +
                        nouns[(int) Math.floor(Math.random() * nouns.length)]
        );
    }

    private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }
}

class MemberData {
    private String name;
    private String color;

    public MemberData(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public MemberData() {
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "MemberData{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}