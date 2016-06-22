package com.javarush.test.level30.lesson15.big01.client;

import com.javarush.test.level30.lesson15.big01.Connection;
import com.javarush.test.level30.lesson15.big01.ConsoleHelper;
import com.javarush.test.level30.lesson15.big01.Message;
import com.javarush.test.level30.lesson15.big01.MessageType;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by vadym on 27.05.2016.
 */
public class Client
{
    protected Connection connection;
    private volatile boolean clientConnected = false;
    /** PSVM Client **/
    public static void main(String[] args) {

        Client client = new Client();
        client.run();
    }


    /** Methods **/
    /** run **/
    public void run() {

        // Создавать новый сокетный поток с помощью метода getSocketThread
        SocketThread socketThread = getSocketThread();
        // Помечать созданный поток как daemon, это нужно для того, чтобы при выходе
        // из программы вспомогательный поток прервался автоматически.
        socketThread.setDaemon(true);
        socketThread.start();

        // Заставить текущий поток ожидать, пока он не получит нотификацию из другого потока
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Ошибка");
            return;
        }

        //После того, как поток дождался нотификации, проверь значение clientConnected
        if (clientConnected)
        {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        }
        else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }

            //Считывай сообщения с консоли пока клиент подключен. Если будет введена команда 'exit', то выйди из цикла
        while (clientConnected) {
            String message = ConsoleHelper.readString();
            if (message.equals("exit"))
            {
            break;
            }
            if (shouldSentTextFromConsole())
            {
                sendTextMessage(message);
            }
        }


    }
    protected String getServerAddress()
    {
        ConsoleHelper.writeMessage("Введите адресс сервера...");
        return  ConsoleHelper.readString();
    }
    protected int getServerPort()
    {
        ConsoleHelper.writeMessage("Введите номер порта...");
        return ConsoleHelper.readInt();
    }
    protected String getUserName()
    {
        ConsoleHelper.writeMessage("Введите имя пользователя...");
        return ConsoleHelper.readString();
    }
    protected boolean shouldSentTextFromConsole()
    {
        return true;
    }
    protected SocketThread getSocketThread()
    {
        return new SocketThread();
    }
    protected void sendTextMessage(String text)
    {
        Message message = new Message(MessageType.TEXT, text);
        try
        {
            connection.send(message);
        }
        catch (IOException e)
        {
            ConsoleHelper.writeMessage("Сообщение не отправлено...");
            clientConnected = false;
        }
    }

    public class SocketThread  extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                String address = getServerAddress();
                int port = getServerPort();
                Socket socket = new Socket(address, port);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            }
            catch (IOException e)
            {
                notifyConnectionStatusChanged(false);
            }
            catch (ClassNotFoundException e)
            {
                notifyConnectionStatusChanged(false);
            }
        }
        protected void clientHandshake() throws IOException, ClassNotFoundException
        {
            boolean isBreak = true;
            while (isBreak)
            {
                Message message = connection.receive();
                switch (message.getType())
                {
                    case NAME_REQUEST:
                        connection.send(new Message(MessageType.USER_NAME, getUserName()));
                        break;
                    case NAME_ACCEPTED:
                        notifyConnectionStatusChanged(true);
                        isBreak = false;
                        break;
                    default:
                        throw new IOException("Unexpected MessageType");
                }
            }
        }
        /*protected void clientHandshake() throws IOException,ClassNotFoundException{
            while (true){
                Message message=connection.receive();
                if (MessageType.NAME_REQUEST.equals(message.getType())){
                    String name=getUserName();
                    connection.send(new Message(MessageType.USER_NAME,name));
                } else {
                    if (MessageType.NAME_ACCEPTED.equals(message.getType())){
                        notifyConnectionStatusChanged(true);
                        break;
                    } else {
                        throw new IOException("Unexpected MessageType");
                    }
                }
            }
        }*/


        protected void processIncomingMessage(String message)
        {
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName)
        {
            ConsoleHelper.writeMessage(userName + " присоединился к чату");
        }
        protected void informAboutDeletingNewUser(String userName)
        {
            ConsoleHelper.writeMessage(userName + " покинул чат");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected)
        {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this)
            {
                Client.this.notify();
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException
        {
            while (true)
            {
                Message message = connection.receive();
                switch (message.getType())
                {
                    case TEXT:
                        processIncomingMessage(message.getData());
                        break;
                    case USER_ADDED:
                        informAboutAddingNewUser(message.getData());
                        break;
                    case USER_REMOVED:
                        informAboutDeletingNewUser(message.getData());
                        break;
                    default:
                        throw new IOException("Unexpected MessageType");
                }
            }
        }
    }

}
