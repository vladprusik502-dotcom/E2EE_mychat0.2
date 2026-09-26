package org.example.mychat.encription;


import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.interfaces.Box;
import com.goterl.lazysodium.utils.Key;
import com.goterl.lazysodium.utils.KeyPair;
import lombok.Getter;

@Getter
public class EncryptionMethods {

    // Ініціалізація libsodium
    private LazySodiumJava sodium = new LazySodiumJava(new SodiumJava());

    public EncryptedMessage encryptMessage(String originalMassage, String pubKeyHexUser1, KeyPair keyPairUser2) throws SodiumException {
        KeyPair user1KeyAndUser2Key = new KeyPair(
                Key.fromHexString(pubKeyHexUser1),
                keyPairUser2.getSecretKey()
        );
        // Генерація унікального одноразового числа (Nonce) завдовжки 24 байти
        byte[] nonce = sodium.nonce(Box.NONCEBYTES);
        String encryptedHex = getSodium().cryptoBoxEasy(
                originalMassage,
                nonce,
                user1KeyAndUser2Key
        );
        return new EncryptedMessage(
                encryptedHex,
                nonce,
                keyPairUser2.getPublicKey().getAsHexString()
        );
    }

    public String decryption(EncryptedMessage encryptedMessage, Key secKeyUser1) throws SodiumException {
        KeyPair user1AndUser2KeyPair = new KeyPair(
                Key.fromHexString(encryptedMessage.getPublicKeyHex()),
                secKeyUser1);
        String decryptedMessage = sodium.cryptoBoxOpenEasy(
                encryptedMessage.getEncryptedHex(),
                encryptedMessage.getNonce(),
                user1AndUser2KeyPair
        );

        return decryptedMessage;
    }

    public LazySodiumJava getSodium() {
        return sodium;
    }

}