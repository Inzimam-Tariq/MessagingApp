package com.invogen.messagingapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class MessagingTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_test);

            setTitle("dev2qa.com - Android Chat App Example");

            // Get RecyclerView object.
            final RecyclerView msgRecyclerView = findViewById(R.id.chat_recycler_view);

            // Set RecyclerView layout manager.
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            msgRecyclerView.setLayoutManager(linearLayoutManager);

            // Create the initial data list.
            final List<ChatAppMsgDTO> msgDtoList = new ArrayList<>();
            ChatAppMsgDTO msgDto = new ChatAppMsgDTO(ChatAppMsgDTO.MSG_TYPE_RECEIVED, "hello");
            msgDtoList.add(msgDto);

            // Create the data adapter with above data list.
            final ChatAppMsgAdapter chatAppMsgAdapter = new ChatAppMsgAdapter(msgDtoList);

            // Set data adapter to RecyclerView.
            msgRecyclerView.setAdapter(chatAppMsgAdapter);

            final EditText msgInputText = findViewById(R.id.chat_input_msg);

            Button msgSendButton = findViewById(R.id.chat_send_msg);

            msgSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String msgContent = msgInputText.getText().toString();
                    if(!TextUtils.isEmpty(msgContent))
                    {
                        // Add a new sent message to the list.
                        ChatAppMsgDTO msgDto = new ChatAppMsgDTO(ChatAppMsgDTO.MSG_TYPE_SENT, msgContent);
                        msgDtoList.add(msgDto);

                        int newMsgPosition = msgDtoList.size() - 1;

                        // Notify recycler view insert one new data.
                        chatAppMsgAdapter.notifyItemInserted(newMsgPosition);

                        // Scroll RecyclerView to the last message.
                        msgRecyclerView.scrollToPosition(newMsgPosition);

                        // Empty the input edit text box.
                        msgInputText.setText("");
                    }
                }
            });
        }
    }
