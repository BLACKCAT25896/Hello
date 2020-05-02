package com.kamrujjamanjoy.hello.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.kamrujjamanjoy.hello.R;
import com.kamrujjamanjoy.hello.holder.QBUnreadMessageHolder;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatDialogAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<QBChatDialog> qbChatDialogs;

    public ChatDialogAdapter(Context context, ArrayList<QBChatDialog> qbChatDialogs) {
        this.context = context;
        this.qbChatDialogs = qbChatDialogs;
    }

    @Override
    public int getCount() {
        return qbChatDialogs.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatDialogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_chat_dialog, null);

            TextView chatTitle, chatMessage;
            final ImageView chatImage, unreadMessage;

            chatTitle = view.findViewById(R.id.listChatDialogTitle);
            chatMessage = view.findViewById(R.id.listChatDialogMessage);
            chatImage = view.findViewById(R.id.imageChatDialog);
            unreadMessage = view.findViewById(R.id.image_unread_message);

            chatMessage.setText(qbChatDialogs.get(position).getLastMessage());
            chatTitle.setText(qbChatDialogs.get(position).getName());


            ColorGenerator colorGenerator = ColorGenerator.MATERIAL;
            int randomColor = colorGenerator.getRandomColor();

            if (qbChatDialogs.get(position).getPhoto().equals("null")) {
                TextDrawable.IBuilder builder = TextDrawable.builder().beginConfig()
                        .withBorder(4)
                        .endConfig()
                        .round();
                TextDrawable drawable = builder.build(chatTitle.getText().toString().substring(0, 1).toUpperCase(), randomColor);

                chatImage.setImageDrawable(drawable);
            } else {
                //download bitmap
                QBContent.getFile(Integer.parseInt(qbChatDialogs.get(position).getPhoto())).performAsync(new QBEntityCallback<QBFile>() {
                    @Override
                    public void onSuccess(QBFile qbFile, Bundle bundle) {
                        String fileUrl = qbFile.getPublicUrl();
                        Picasso.get()
                                .load(fileUrl)
                                .resize(50, 50)
                                .centerCrop()
                                .into(chatImage);

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR", e.getMessage() );

                    }
                });
            }

            //setMessage Unread count

            TextDrawable.IBuilder unreadBuilder = TextDrawable.builder().beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round();
            int unreadCount = QBUnreadMessageHolder.getInstance().getBundle().getInt(qbChatDialogs.get(position).getDialogId());

            if (unreadCount > 0) {
                TextDrawable unreadDrawable = unreadBuilder.build("" + unreadCount, Color.RED);
                unreadMessage.setImageDrawable(unreadDrawable);

            }


        }
        return view;
    }
}
