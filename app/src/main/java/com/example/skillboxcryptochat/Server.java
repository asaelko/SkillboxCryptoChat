package com.example.skillboxcryptochat;

import android.util.Log;
import android.util.Pair;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

class Server {
    /**
     * Клиент для установки соединения и обмена данными по WebSocket
     */
    private WebSocketClient client;

    /**
     * Консьюмеры для общения с UI-потоком
     */
    private Consumer<Pair<String,String>> onMessageReceived;
    private Consumer<Pair<Boolean,String>> onUserStatusChanged;

    /**
     * Словарь текущих подключенных пользователей
     */
    private Map<Long,String> users = new ConcurrentHashMap<>();

    /**
     * Флаг состояния подключения к серверу
     */
    private Boolean isConnected = false;

    Server(
            Consumer<Pair<String,String>> onMessageReceived,
            Consumer<Pair<Boolean,String>> onUserStatusChanged
    ) {
        this.onMessageReceived = onMessageReceived;
        this.onUserStatusChanged = onUserStatusChanged;
    }

    void connect() {
        URI serverAddress = null;
        try {
            serverAddress = new URI("ws://35.214.3.133:8881");
        } catch (URISyntaxException e) {
            Log.i("SERVER", "URI Error: " + e.getMessage());
            return;
        }

        // создаем объект клиента для подключения к серверу
        client = new WebSocketClient(serverAddress) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i("SERVER", "Connection to server is open");

                isConnected = true;

                // при установке соединения отправляем серверу свое имя
                String packedName = Protocol.packName(new Protocol.Username(MainActivity.AUTHOR_NAME));
                Log.i("SERVER", packedName);

                client.send(packedName);
            }

            @Override
            public void onMessage(String message) {
                Log.i("SERVER", "Received new message: " + message);

                // при получении сообщения от сервера определяем его тип
                int type = Protocol.getMessageType(message);

                // в зависимости от типа обрабатываем сообщение
                switch (type) {
                    case Protocol.USER_STATUS:
                        addNewUser(message);
                        break;
                    case Protocol.MESSAGE:
                        displayIncomingMessage(message);
                        break;
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("SERVER", "Connection to server was closed");

                isConnected = false;
            }

            @Override
            public void onError(Exception ex) {
                Log.i("SERVER", "Error occurred: " + ex.getMessage());
            }
        };

        client.connect();
    }

    /**
     * Обрабатывает событие об изменении статуса пользователя
     *
     * @param userUpdateMessage Закодированное сообщение об изменении статуса пользователя
     */
    private void addNewUser(String userUpdateMessage) {
        Protocol.UserStatus status = Protocol.unpackStatus(userUpdateMessage);

        Protocol.User user = status.getUser();

        if (status.isConnected()) {
            users.put(user.getId(), user.getName());
        } else {
            users.remove(user.getId());
        }

        onUserStatusChanged.accept(
            new Pair<>(status.isConnected(), user.getName())
        );
    }

    /**
     * Обрабатывает событие о новом сообщении
     *
     * @param newEncodedMessage Закодированное сообщение о новом сообщении в чате
     */
    private void displayIncomingMessage(String newEncodedMessage) {
        Protocol.Message message = Protocol.unpackMessage(newEncodedMessage);

        String name = users.get(message.getSender());

        if (name == null) {
            name = "Безымянный";
        }

        onMessageReceived.accept(
            new Pair<>(name, message.getEncodedText())
        );
    }

    /**
     * Отправяет уведомление с новым сообщением на сервер
     *
     * @param message Текст сообщения
     */
    public void sendMessage(String message) {
        Protocol.Message greetingMessage = new Protocol.Message(message);
        greetingMessage.setReceiver(Protocol.GROUP_CHAT);

        client.send(Protocol.packMessage(greetingMessage));
    }

    /**
     * Отдает текущее количество подключенных пользователей
     *
     * @return Текущее количество пользователей в чате
     */
    public int getUsersCount() {
        return users.size();
    }

    /**
     * Отдает текущий статус подключения к серверу
     *
     * @return Текущий статус подключения
     */
    public Boolean isConnected() {
        return isConnected;
    }
}
