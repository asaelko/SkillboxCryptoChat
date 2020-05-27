package com.example.skillboxcryptochat;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {
    public final static String AUTHOR_NAME = "Глеб";

    Button sendButton;
    EditText userInput;
    RecyclerView chatWindow;
    TextView usersOnlineCountLabel;
    MessageController controller;

    Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = findViewById(R.id.sendButton);
        userInput = findViewById(R.id.userInput);
        chatWindow = findViewById(R.id.chatWindow);
        usersOnlineCountLabel = findViewById(R.id.usersOnlineCountLabel);

        controller = new MessageController();

        controller.setIncomingLayout(R.layout.incoming_message);
        controller.setOutgoingLayout(R.layout.outgoing_message);
        controller.setSystemLayout(R.layout.system_message);

        controller.setMessageTextId(R.id.messageText);
        controller.setUserNameId(R.id.authorName);
        controller.setMessageTimeId(R.id.messageDate);

        controller.appendTo(chatWindow, this);

        controller.addMessage(new MessageController.Message(
                "Приветствую тебя в чате! Правила: не материться, не флудить, помогать тестировать другим участникам",
                "Skillbox",
                false,
                true
        ));

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        userInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendMessage();
                }

                return false;
            }
        });

        server = new Server(
                getMessageConsumer(),
                getUserStatusConsumer()
        );
        server.connect();
    }

    /**
     * Создает консумер для обмена данными о новых сообщениях в чате
     *
     * @return Консумер новых сообщений в чате
     */
    protected Consumer<Pair<String, String>> getMessageConsumer() {
        return new Consumer<Pair<String, String>>() {
            @Override
            public void accept(final Pair<String, String> p) {
                runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            controller.addMessage(new MessageController.Message(
                                    p.second,
                                    p.first,
                                    false
                            ));
                        }
                    }
                );
            }
        };
    }

    /**
     * Создает консумер для обмена данными об изменении статусов пользователей
     *
     * @return Консумер изменений статусов пользователей
     */
    protected Consumer<Pair<Boolean,String>> getUserStatusConsumer() {
        return new Consumer<Pair<Boolean, String>>() {
            @Override
            public void accept(final Pair<Boolean, String> p) {
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                usersOnlineCountLabel.setText(
                                        String.format("Пользователей онлайн: %d", server.getUsersCount())
                                );
                                showStatusUpdateNotification(p);
                            }
                        }
                );
            }
        };
    }

    /**
     * Отправляет новое сообщение в чат и отображает его на экране
     */
    protected void sendMessage() {
        String messageText = userInput.getText().toString().trim();

        if (messageText.isEmpty()) {
            showToast("Нельзя отправить пустое сообщение");
            return;
        }

        if (server == null || !server.isConnected()) {
            showToast("Нет соединения с сервером");
            return;
        }

        server.sendMessage(messageText);

        controller.addMessage(new MessageController.Message(
                messageText,
                MainActivity.AUTHOR_NAME,
                true
        ));

        userInput.setText("");
    }

    /**
     * Отображает сообщение об изменении статуса пользователя
     * @param pair Пара <Изменение статуса, Имя пользователя>
     */
    private void showStatusUpdateNotification(Pair<Boolean,String> pair) {
        String toastMessage;
        if (pair.first) {
            toastMessage = "К чату подключился " + pair.second;
        } else {
            toastMessage = "Из чата вышел " + pair.second;
        }

        showToast(toastMessage);
    }

    /**
     * Показывает уведомление на экране
     * @param text Текст уведомления
     */
    private void showToast(String text) {
        Context context = getApplicationContext();

        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
