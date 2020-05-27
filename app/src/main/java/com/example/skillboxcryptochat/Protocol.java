package com.example.skillboxcryptochat;

import com.google.gson.Gson;

class Protocol {
    /**
     * Идентификатор группового чата
     */
    final static int GROUP_CHAT = 1;

    /**
     * Типы допустимых при обмене данными с сервером сообщений
     */
    final static int USER_STATUS = 1;
    final static int MESSAGE = 2;
    final static int USER_NAME = 3;

    /**
     * Класс уведомления об установке имени пользователя
     */
    static class Username {
        private String name;

        Username(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Класс сообщения
     */
    static class Message {
        private long sender;
        private String encodedText;
        private long receiver;

        Message(String encodedText) {
            this.encodedText = encodedText;
        }

        public long getSender() {
            return sender;
        }

        public void setSender(long sender) {
            this.sender = sender;
        }

        public String getEncodedText() {
            return encodedText;
        }

        public void setEncodedText(String encodedText) {
            this.encodedText = encodedText;
        }

        public long getReceiver() {
            return receiver;
        }

        public void setReceiver(long receiver) {
            this.receiver = receiver;
        }
    }

    /**
     * Класс пользователя
     */
    static class User {
        private String name;
        private Long id;

        User() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    /**
     * Класс обновления статуса пользователя
     */
    static class UserStatus {
        private boolean connected;
        private User user;

        UserStatus() {
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    /**
     * Отдает тип сообщения, полученного от сервера
     *
     * @param jsonMessage Сообщение в формате JSON, полученное от серверв
     * @return тип переданного сообщения
     */
    public static int getMessageType(String jsonMessage) {
        if (jsonMessage.isEmpty()) {
            return -1;
        }

        return Integer.parseInt(jsonMessage.substring(0, 1));
    }

    /**
     * Кодируем имя пользователя для отправки события об установке имени
     *
     * @param name Имя пользователя
     * @return Закодированное сообщение
     */
    public static String packName(Username name) {
        Gson g = new Gson();

        return USER_NAME + g.toJson(name);
    }

    /**
     * Кодируем объект сообщения для отправки на сервер
     *
     * @param message Объект сообщения для отправки
     * @return Закодированное сообщение
     */
    public static String packMessage(Message message) {
        Gson g = new Gson();

        return MESSAGE + g.toJson(message);
    }

    /**
     * Распаковываем сообщение от сервера
     *
     * @param jsonMessage Закодированное сообщение от сервера
     * @return Распакованное сообщение
     */
    public static Message unpackMessage(String jsonMessage) {
        Gson g = new Gson();

        return g.fromJson(jsonMessage.substring(1), Message.class);
    }

    /**
     * Распаковываем обновление статуса пользователя от сервера
     *
     * @param jsonUserStatus Закодированное сообщение от сервера
     * @return Распакованное сообщение об изменении статуса
     */
    public static UserStatus unpackStatus(String jsonUserStatus) {
        Gson g = new Gson();

        return g.fromJson(jsonUserStatus.substring(1), UserStatus.class);
    }
}
