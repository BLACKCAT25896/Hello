package com.kamrujjamanjoy.hello.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.kamrujjamanjoy.hello.R;
import com.quickblox.chat.model.QBChatDialog;

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

        if (view==null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_chat_dialog,null);

            TextView chatTitle, chatMessage;
            ImageView chatImage;

            chatTitle = view.findViewById(R.id.listChatDialogTitle);
            chatMessage = view.findViewById(R.id.listChatDialogMessage);
            chatImage = view.findViewById(R.id.imageChatDialog);
            chatMessage.setText(qbChatDialogs.get(position).getLastMessage());
            chatTitle.setText(qbChatDialogs.get(position).getName());


            ColorGenerator colorGenerator = ColorGenerator.MATERIAL;
            int randomColor = colorGenerator.getRandomColor();
            TextDrawable.IBuilder builder = TextDrawable.builder().beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round();
            TextDrawable drawable = builder.build(chatTitle.getText().toString().substring(0,1).toUpperCase(),randomColor);
            chatImage.setImageDrawable(drawable);










        }
        return view;
    }
}
