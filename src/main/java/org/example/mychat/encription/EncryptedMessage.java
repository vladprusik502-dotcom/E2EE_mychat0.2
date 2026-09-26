package org.example.mychat.encription;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EncryptedMessage {
    private String encryptedHex;
    private byte[] nonce;
    private String publicKeyHex;

    public EncryptedMessage(String encryptedHex, byte[] nonce, String publicKeyHex) {
        this.encryptedHex = encryptedHex;
        this.nonce = nonce;
        this.publicKeyHex = publicKeyHex;
    }

    public EncryptedMessage() {
    }
}