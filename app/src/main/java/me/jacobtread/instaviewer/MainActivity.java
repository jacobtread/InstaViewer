package me.jacobtread.instaviewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    private ResReceiver resultReceiver;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultReceiver = new ResReceiver();
        IntentFilter filter = new IntentFilter("me.jacobtread.instaviewer.RESULT");
        registerReceiver(resultReceiver, filter);
        findViewById(R.id.getButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("me.jacobtread.instaviewer.REQUEST");
                sendBroadcast(intent);
            }
        });
        findViewById(R.id.clearButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("me.jacobtread.instaviewer.REQUEST");
                intent.putExtra("clear",true);
                sendBroadcast(intent);
            }
        });
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        Intent intent = new Intent("me.jacobtread.instaviewer.REQUEST");
        intent.putExtra("clear", true);
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(resultReceiver);
    }

    static class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

        private List<InstaMessage> messages = new ArrayList<>();
        private IconListParcelable icons;

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout, parent, false);
            return new MessageViewHolder(linearLayout);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            InstaMessage message = messages.get(position);
            holder.username.setText(message.username);
            holder.message.setText(message.message);
            if (icons.contains(message.username)) {
                holder.iconView.setImageIcon(icons.get(message.username));
            }
            if (message.deleted) {
                holder.layout.setBackgroundColor(Color.RED);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        public void setMessages(List<InstaMessage> messages) {
            this.messages = messages;
            notifyDataSetChanged();
        }

        public void setIcons(IconListParcelable icons) {
            this.icons = icons;
        }

        public static class MessageViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout layout;
            public ImageView iconView;
            public TextView username;
            public TextView message;

            public MessageViewHolder(LinearLayout layout) {
                super(layout);
                this.layout = layout;
                iconView = layout.findViewById(R.id.iconView);
                username = layout.findViewById(R.id.username);
                message = layout.findViewById(R.id.message);
            }
        }
    }

    class ResReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Wrapper<LinkedList<InstaMessage>> wrapper = (Wrapper<LinkedList<InstaMessage>>) intent.getSerializableExtra("data");
            final IconListParcelable icons = intent.getParcelableExtra("icons");
            final LinkedList<InstaMessage> messages = wrapper.get();
            Collections.sort(messages, new Comparator<InstaMessage>() {
                @Override
                public int compare(InstaMessage o1, InstaMessage o2) {
                    return Integer.compare(o1.id, o2.id);
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(messages.size());
                    adapter.setIcons(icons);
                    adapter.setMessages(messages);
                }
            });
        }
    }
}