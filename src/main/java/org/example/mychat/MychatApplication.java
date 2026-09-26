package org.example.mychat;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
public class MychatApplication {

    public static void main(String[] args) {
        // 1. Вимикаємо headless режим для розгортання GUI
        SpringApplicationBuilder builder = new SpringApplicationBuilder(MychatApplication.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);

        // 2. Запускаємо графічну оболонку у потоці EventQueue
        EventQueue.invokeLater(() -> {
            // Спочатку відкриваємо вікно введення імені
            NameInputDialog nameDialog = new NameInputDialog(null);
            nameDialog.setVisible(true);

            String username = nameDialog.getUsername();

            // Якщо ім'я введено — підключаємося та відкриваємо головне вікно
            if (username != null && !username.trim().isEmpty()) {
                try {
                    Window chat = context.getBean(Window.class);
                    chat.startSession(username); // Підключаємося до сервера Netty
                    chat.setVisible(true);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Помилка підключення до сервера: " + e.getMessage(),
                            "Помилка з'єднання",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
            } else {
                // Якщо закрили вікно без імені
                System.exit(0);
            }
        });
    }
}


