package org.example.mychat;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;

@org.springframework.stereotype.Component
public class Window extends JFrame {
    private JPanel chatPanel;
    private JTextField inputField1;
    private JTextField inputField2;
    private JButton sendButton;
    @Autowired
    private NetworkClientManager networkClientManager;
    @Getter
    private String userName;

    public Window() throws InterruptedException, SodiumException {
        // Налаштування основного вікна (JFrame)
        setTitle("Secret chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null); // Центрування вікна на екрані
        setLayout(new BorderLayout());

        // Панель для відображення повідомлень
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(240, 240, 240));

        // Додаємо прокрутку (JScrollPane), щоб можна було гортати повідомлення
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // Нижня панель для введення тексту та кнопки відправки
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Поле імені з лейблом
        JLabel nameLabel = new JLabel("Name:");
        inputPanel.add(nameLabel);

        inputField1 = new JTextField();
        inputPanel.add(inputField1);

        // Відступ між полями
        inputPanel.add(Box.createVerticalStrut(5));

        // 2. Поле тексту з лейблом
        JLabel textLabel = new JLabel("Text:");
        inputPanel.add(textLabel);

        inputField2 = new JTextField();
        inputPanel.add(inputField2);

        // Відступ перед кнопкою
        inputPanel.add(Box.createVerticalStrut(10));

        // 3. Кнопка відправки
        sendButton = new JButton("Send");
        inputPanel.add(sendButton);

        add(inputPanel, BorderLayout.SOUTH);

        // Обробник події для кнопки відправки
        ActionListener sendAction = new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(inputField1.getText().trim(), inputField2.getText().trim());
            }
        };


        sendButton.addActionListener(sendAction);
        inputField1.addActionListener(sendAction);
        inputField2.addActionListener(sendAction); // Відправка по натисканню Enter
    }

    // Метод запуску підключення після введення імені
    public void startSession(String userName) throws InterruptedException, SodiumException {
        this.userName = userName;
        this.networkClientManager.connect("localhost", 8443, userName, this);
    }

    // 1. Метод МАТЕРІАЛІЗАЦІЇ плашки в GUI (без мережевих викликів!)
    public void addMessageToUI(String text, String sender) {
        SwingUtilities.invokeLater(() -> {
            JPanel messageBubble = createMessageBubble(sender + ": " + text);
            messageBubble.setAlignmentX(Component.LEFT_ALIGNMENT);

            chatPanel.add(messageBubble);
            chatPanel.add(Box.createVerticalStrut(8));

            chatPanel.revalidate();
            chatPanel.repaint();

            // Безопасний пошук JScrollPane
            if (chatPanel.getParent() instanceof JViewport viewport) {
                if (viewport.getParent() instanceof JScrollPane scrollPane) {
                    JScrollBar vertical = scrollPane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                }
            }
        });
    }

    public void sendMessage(String textField1, String textField2) throws InterruptedException {
        if (!textField1.isEmpty() && !textField2.isEmpty()) {

            // 1. Відмальовуємо наше власне повідомлення у чаті
            addMessageToUI(textField2, "Me");

            // 2. Викликаємо мережевий менеджер у фоновому потоці
            CompletableFuture.runAsync(() -> {
                try {
                    networkClientManager.sendMessage(userName, textField1, textField2);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                } catch (SodiumException e) {
                    throw new RuntimeException(e);
                }
            });

            // 3. Очищаємо поле введення тексту
            inputField2.setText("");
        } else {
            inputField1.setText("Enter your text");
            inputField2.setText("Enter your text");
        }
    }

    private JPanel createMessageBubble(String text) {
        // Контейнер для повідомлення
        JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        container.setOpaque(false);
        container.setAlignmentX(Component.LEFT_ALIGNMENT); // Вирівнювання по лівому краю

        // "Бабл" (хмарка) повідомлення
        JLabel label = new JLabel("<html><body style='width: 200px; word-wrap: break-word;'>" + text + "</body></html>");
        label.setOpaque(true);
        label.setBackground(new Color(220, 248, 198)); // Світло-зелений колір
        label.setForeground(Color.BLACK);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 220, 160), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        container.add(label);
        return container;
    }
}
