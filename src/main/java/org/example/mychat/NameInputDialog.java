package org.example.mychat;


import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class NameInputDialog extends JDialog {
    private final JTextField nameField;
    @Getter
    private String username = null;

    public NameInputDialog(Frame parent) {
        super(parent, "Log in MY_CHAT", true); // true робить вікно модальним

        setLayout(new BorderLayout(10, 10));
        setSize(320, 150);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Панель введення
        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));

        JLabel label = new JLabel("Enter your name:");
        nameField = new JTextField();

        inputPanel.add(label);
        inputPanel.add(nameField);

        // Кнопка підтвердження
        JButton joinButton = new JButton("Log in");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(joinButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Обробник натискання кнопки або Enter у полі
        joinButton.addActionListener(e -> submitName());
        nameField.addActionListener(e -> submitName());

        // Заповнюємо ім'я за замовчуванням ім'ям користувача ОС
        nameField.setText(System.getProperty("user.name"));
    }

    private void submitName() {
        String input = nameField.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name field can not be empty", "Error:", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.username = input;
        dispose(); // Закриваємо діалог
    }
}