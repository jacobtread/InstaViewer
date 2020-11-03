package me.jacobtread.instaviewer;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.annotation.RequiresApi;

import java.util.*;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListener extends NotificationListenerService {

    private final LinkedList<InstaMessage> messages = new LinkedList<>();
    private final List<InstaMessage> previousMessages = new LinkedList<>();
    private ReqReceiver reqReceiver;
    private final IconListParcelable icons = new IconListParcelable(null);
    public static int counter = 0;

    private void findAll() {
        counter = 0;
        List<InstaMessage> instaMessages = new LinkedList<>();
        for (StatusBarNotification activeNotification : getActiveNotifications()) {
            if (notInstagram(activeNotification)) {
                continue;
            }
            instaMessages.addAll(parseMessages(activeNotification));
        }
        handle(instaMessages);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationPosted(sbn, rankingMap);
        findAll();
        sendBroadcast(createResponse());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationRemoved(sbn, rankingMap);
        List<InstaMessage> contained = parseMessages(sbn);
        for (InstaMessage message : messages) {
            if(contained.contains(message)) {
                message.deleted = true;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        reqReceiver = new ReqReceiver();
        IntentFilter intentFilter = new IntentFilter("me.jacobtread.instaviewer.REQUEST");
        registerReceiver(reqReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(reqReceiver);
    }

    public void handle(List<InstaMessage> instaMessages) {
        if (instaMessages.isEmpty()) {
            previousMessages.clear();
            previousMessages.addAll(instaMessages);
            return;
        }
        if (instaMessages.equals(previousMessages)) {
            return;
        }
        List<InstaMessage> oldRemoved = new LinkedList<>();
        for (InstaMessage message : messages) {
            if (message.deleted) {
                oldRemoved.add(message);
            }
        }
        messages.clear();
        if (!previousMessages.isEmpty()) {
            int i1 = 0;
            int i2 = 0;
            while (i1 < previousMessages.size()) {
                InstaMessage previousMessage = previousMessages.get(i1);
                if (i1 >= instaMessages.size()) {
                    previousMessage.deleted = true;
                    messages.add(previousMessage);
                    break;
                } else {
                    InstaMessage currentMessage = instaMessages.get(i2);
                    if (!currentMessage.equals(previousMessage)) {
                        if (i2 + 1 < instaMessages.size()) {
                            i2++;
                        } else {
                            previousMessage.deleted = true;
                            messages.add(previousMessage);
                            break;
                        }
                    } else {
                        i1++;
                        i2++;
                    }
                }
            }
        }
        messages.addAll(oldRemoved);
        messages.addAll(instaMessages);
        Collections.sort(messages, new Comparator<InstaMessage>() {
            @Override
            public int compare(InstaMessage o1, InstaMessage o2) {
                return Integer.compare(o1.id, o2.id);
            }
        });
        previousMessages.clear();
        previousMessages.addAll(instaMessages);
    }

    private List<InstaMessage> parseMessages(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if (!notification.getChannelId().equals("ig_direct")) {
            return Collections.emptyList();
        }
        Bundle extras = notification.extras;
        Icon icon = (Icon) extras.get("android.largeIcon");
        CharSequence[] rawLines = extras.getCharSequenceArray("android.textLines");
        if (rawLines == null) {
            String text = extras.getString("android.text");
            if (text == null) {
                return Collections.emptyList();
            } else {
                rawLines = new CharSequence[]{text};
            }
        }
        List<InstaMessage> instaMessages = new LinkedList<>();
        for (CharSequence rawLine : rawLines) {
            String line = String.valueOf(rawLine);
            if (line.length() < 3) {
                continue;
            }
            line = line.substring(1);
            int splitIndex = line.indexOf("):");
            String account = line.substring(0, splitIndex);
            String childText;
            if (line.length() > splitIndex + 3) {
                childText = line.substring(splitIndex + 3);
            } else {
                childText = "";
            }
            splitIndex = childText.indexOf(":");
            if (splitIndex > 0) {
                String username = childText.substring(0, splitIndex);
                if(!icons.contains(username)) {
                    icons.add(username, icon);
                }
                String text;
                if (childText.length() > splitIndex + 2) {
                    text = childText.substring(splitIndex + 2);
                } else {
                    text = "";
                }
                instaMessages.add(new InstaMessage(counter, account, username, text, false));
                counter++;
            }
        }
        return instaMessages;
    }


    private boolean notInstagram(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        return packageName == null || !packageName.equals("com.instagram.android");
    }

    private Intent createResponse() {
        Intent res = new Intent("me.jacobtread.instaviewer.RESULT");
        res.putExtra("data", new Wrapper<>(messages));
        res.putExtra("icons", icons);
        return res;
    }

    class ReqReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra("clear")) {
                counter = 0;
                messages.clear();
                previousMessages.clear();
                icons.clear();
            }
            findAll();
            sendBroadcast(createResponse());
        }
    }

}
