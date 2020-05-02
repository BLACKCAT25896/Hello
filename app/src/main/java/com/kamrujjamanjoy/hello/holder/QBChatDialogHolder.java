package com.kamrujjamanjoy.hello.holder;

import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QBChatDialogHolder {
    private static QBChatDialogHolder instance;
    private HashMap<String, QBChatDialog> qbChatDialogHashMap;

    public static synchronized QBChatDialogHolder getInstance(){
        QBChatDialogHolder qbChatDialogHolder;
        synchronized (QBChatDialogHolder.class){

            if (instance==null)
                instance = new QBChatDialogHolder();

            qbChatDialogHolder = instance;

        }
        return qbChatDialogHolder;


    }

    public QBChatDialogHolder() {
        this.qbChatDialogHashMap= new HashMap<>();
    }
    public void putDialogs(List<QBChatDialog> dialogs){
        for (QBChatDialog dialog:dialogs)
            putDialog(dialog);

    }

    public void putDialog(QBChatDialog dialog) {
        this.qbChatDialogHashMap.put(dialog.getDialogId(),dialog);
    }

    public QBChatDialog getChatDialogById(String dialogId){
        return qbChatDialogHashMap.get(dialogId);
    }
    public List<QBChatDialog> getChatDialogByIds(List<String> dialogIds){
        List<QBChatDialog> chatDialogs = new ArrayList<>();
        for (String id: dialogIds){
            QBChatDialog qbChatDialog = getChatDialogById(id);
            if (qbChatDialog!=null){
                chatDialogs.add(qbChatDialog);
            }

        }
        return chatDialogs;
    }

    public ArrayList<QBChatDialog> getAllChatDialogs(){
        ArrayList<QBChatDialog> qbChat = new ArrayList<>();
        for (String key : qbChatDialogHashMap.keySet())
            qbChat.add(qbChatDialogHashMap.get(key));

        return qbChat;
    }
    public void removeDialog(String id){
        qbChatDialogHashMap.remove(id);
    }

}
