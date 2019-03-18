package com.pulsarappdev.scaledronetestproject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageAdapter extends BaseAdapter {

    List<Message> messages = new ArrayList<Message>();
    Context context;
    public boolean safe_mode = false;

    String realmsg = "";

    List<String> swearWords = Arrays.asList("anal","anus","arse","ass","ballsack","balls","bastard","bitch","biatch","bloody","blowjob","blow job","bollock","bollok","boner","boob","bugger","bum","butt","buttplug","clitoris","cock","coon","crap","cunt","damn","dick","dildo","dyke","fag","feck","fellate","fellatio","felching","fuck","f u c k","fudgepacker","fudge packer","flange","Goddamn","God damn","hell","homo","jerk","jizz","knobend","knob end","labia","lmao","lmfao","muff","nigger","nigga","omg","penis","piss","poop","prick","pube","pussy","queer","scrotum","sex","shit","s hit","sh1t","slut","smegma","spunk","tit","tosser","turd","twat","vagina","wank","whore","wtf"); // From http://www.bannedwordlist.com

    public MessageAdapter(Context context) {
        this.context = context;
    }


    public void add(Message message) {
        this.messages.add(message);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    /*public void toggleSafeMode(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            safe_mode = true;
        } else {
            safe_mode = false;
        }
    }*/

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);

        if (message.isBelongsToCurrentUser()) {
            convertView = messageInflater.inflate(R.layout.my_message, null);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);

            if (message.getText().equals("++safe_mode_on")) {
                Toast.makeText(context, "!! Safe filter enabled", Toast.LENGTH_SHORT);
                safe_mode = true;
            } else if (message.getText().equals("++safe_mode_off")) {
                Toast.makeText(context, "!! Safe filter disabled", Toast.LENGTH_SHORT);
                safe_mode = false;
            } else if (safe_mode) {
                for (String str : swearWords) {
                    if (str.trim().contains(message.getText())) {
                        realmsg = context.getString(R.string.filtered);
                    } else {
                        realmsg = message.getText();
                    }

                }
            } else {
                realmsg = message.getText();
            }

            holder.messageBody.setText(realmsg);

        } else {
            convertView = messageInflater.inflate(R.layout.their_message, null);
            holder.avatar = (View) convertView.findViewById(R.id.avatar);
            holder.name = (TextView) convertView.findViewById(R.id.name);

            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);

            holder.name.setText(message.getData().getName());

            if (message.getText().equals("++safe_mode_on")) {
                Toast.makeText(context, "!! Safe filter enabled", Toast.LENGTH_SHORT);
                safe_mode = true;
            } else if (message.getText().equals("++safe_mode_off")) {
                Toast.makeText(context, "!! Safe filter disabled", Toast.LENGTH_SHORT);
                safe_mode = false;
            } else if (safe_mode) {
                for (String str : swearWords) {
                    if (str.trim().contains(message.getText())) {
                        realmsg = context.getString(R.string.filtered);
                    } else {
                        realmsg = message.getText();
                    }
                }
            } else {
                realmsg = message.getText();
            }

            holder.messageBody.setText(realmsg);


            GradientDrawable drawable = (GradientDrawable) holder.avatar.getBackground();
            drawable.setColor(Color.parseColor("#FF283593"));
        }

        return convertView;
    }

}

class MessageViewHolder {
    public View avatar;
    public TextView name;
    public TextView messageBody;
}