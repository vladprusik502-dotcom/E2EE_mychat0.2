package org.example.mychat;


import org.example.mychat.encription.EncryptedMessage;
import org.example.mychat.encription.EncryptionMethods;
import org.example.mychat.encription.MessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.utils.KeyPair;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.util.HashMap;

@Component
@Service
public class NetworkClientManager {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PrintWriter out;
    private SSLSocket socket;
    private final EncryptionMethods encryptionMethods = new EncryptionMethods();
    private static final HashMap<String, String> userKeys = new HashMap<>();
    private Window window;
    @Getter
    private final KeyPair user2KeyPair = encryptionMethods.getSodium().cryptoBoxKeypair();

    public NetworkClientManager() throws SodiumException {
    }

    public void connect(String host, int port, String sender, Window window) {
        try {
            this.window = window;
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("kStore.p12")) {
                trustStore.load(is, "55067904Vlk1".toCharArray());
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory factory = sslContext.getSocketFactory();
            socket = (SSLSocket) factory.createSocket(host, port);
            socket.startHandshake();

            out = new PrintWriter(socket.getOutputStream(), true);

            // Надсилаємо перше службове повідомлення для реєстрації користувача на сервері
            MessageDto registerMsg = new MessageDto(sender, "SYSTEM", new EncryptedMessage(null, null, user2KeyPair.getPublicKey().getAsHexString()));
            out.println(objectMapper.writeValueAsString(registerMsg));
            out.flush();

            // Запуск прослуховування сервера
            new Thread(this::listenServer).start();

        } catch (Exception e) {
            System.err.println("[Client] З'єднання втрачено: " + e.getMessage());
            window.addMessageToUI("Server inactive", "SYSTEM");
        }
    }

    // 2. Метод надсилає дані через вже відкритий потік out
    public void sendMessage(String sender, String recipient, String text) throws JsonProcessingException, SodiumException {
        String recipientPubKey = userKeys.get(recipient);

        // Якщо ключа немає — запитуємо у сервера і чекаємо
        if (recipientPubKey == null) {
            requestPublicKey(recipient);
            window.addMessageToUI(recipient + " have not registered yet, request to send message to him was sent, try again ", "SYSTEM");
            return;
        }

        // Шифруємо текст публічним ключем ОТИМУВАЧА та своїм ПРИВАТНИМ ключем
        EncryptedMessage encrypted = encryptionMethods.encryptMessage(text, recipientPubKey, user2KeyPair);

        // У publicKeyHex обов'язково кладемо СВІЙ публічний ключ, щоб отримувач знав, чим розшифрувати
        encrypted.setPublicKeyHex(user2KeyPair.getPublicKey().getAsHexString());

        MessageDto messageDto = new MessageDto(sender, recipient, encrypted);
        out.println(objectMapper.writeValueAsString(messageDto));
        out.flush();
    }

    // 2. Запит ключа іншого користувача
    public void requestPublicKey(String targetUser) throws JsonProcessingException {
        MessageDto request = new MessageDto(window.getUserName(), "GET_KEY:" + targetUser, null);
        out.println(objectMapper.writeValueAsString(request));
        out.flush();
    }

    // 4. Обробка вхідних повідомлень
    private void listenServer() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String json;
            while ((json = reader.readLine()) != null) {
                MessageDto msg = objectMapper.readValue(json, MessageDto.class);

                // Якщо це відповідь з публічним ключем іншого користувача
                if (msg.getEncryptedMessage() != null && msg.getEncryptedMessage().getEncryptedHex() == null) {
                    String fetchedPubKey = msg.getEncryptedMessage().getPublicKeyHex();
                    if (fetchedPubKey != null) {
                        userKeys.put(msg.getSender(), fetchedPubKey);
                        userKeys.get(msg.getSender());
                    }
                    continue;
                }

                // Якщо це зашифроване чат-повідомлення
                if (msg.getEncryptedMessage() != null && msg.getEncryptedMessage().getEncryptedHex() != null) {
                    // Розшифровуємо за допомогою приватного ключа клієнта та публічного ключа відправника
                    String decryptedText = encryptionMethods.decryption(
                            msg.getEncryptedMessage(),
                            user2KeyPair.getSecretKey()
                    );

                    window.addMessageToUI(decryptedText, msg.getSender());
                }
            }
        } catch (Exception e) {
            System.err.println("[Client] Помилка обробки повідомлення: " + e.getMessage());
        }
    }
}
