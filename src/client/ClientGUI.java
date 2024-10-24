package client;

import server.ServerWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ClientGUI extends JFrame {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    private static final String HISTORY_FILE = "chat_history.txt";

    private final JTextArea log = new JTextArea();

    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));
    private final JTextField tfIPAddress = new JTextField("127.0.0.1");
    private final JTextField tfPort = new JTextField("12345");
    private final JTextField tfLogin = new JTextField("user");
    private final JPasswordField tfPassword = new JPasswordField("password");
    private final JButton btnLogin = new JButton("Login");

    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JTextField tfMessage = new JTextField();
    private final JButton btnSend = new JButton("Send");

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientGUI(ServerWindow serverWindow) {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        setTitle("Chat client");

        tfMessage.setEnabled(false);
        btnSend.setEnabled(false);

        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnLogin);
        add(panelTop, BorderLayout.NORTH);

        panelBottom.add(tfMessage, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);
        add(panelBottom, BorderLayout.SOUTH);

        log.setEditable(false);
        JScrollPane scrolling = new JScrollPane(log);
        add(scrolling, BorderLayout.CENTER);

        loadChatHistory();

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        tfMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setVisible(true);
    }

    private void connect() {
        try {
            socket = new Socket(tfIPAddress.getText(), Integer.parseInt(tfPort.getText()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            tfMessage.setEnabled(true);
            btnSend.setEnabled(true);

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        logMessage(message);
                    }
                } catch (IOException e) {
                    log.append("Connection closed.\n");
                }
            }).start();
        } catch (IOException e) {
            log.append("Unable to connect to server.\n");
        }
    }

    private void sendMessage() {
        String message = tfMessage.getText().trim(); // Убираем пробелы в начале и конце
        String username = tfLogin.getText().trim(); // Получаем имя пользователя из поля
        if (message.isEmpty()) {
            return; // Если сообщение пустое, ничего не отправляем
        }

        String formattedMessage = username + ": " + message; // Форматируем сообщение с именем пользователя
        out.println(formattedMessage);
        logMessage(formattedMessage);
        tfMessage.setText("");
    }

    private void logMessage(String message) {
        log.append(message + "\n");
        try (FileWriter writer = new FileWriter(HISTORY_FILE, true)) {
            writer.write(message + "\n");
        } catch (IOException e) {
            log.append("Error saving chat history.\n");
        }
    }

    private void loadChatHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.append(line + "\n");
            }
        } catch (IOException e) {
            log.append("Error loading chat history.\n");
        }
    }
}
