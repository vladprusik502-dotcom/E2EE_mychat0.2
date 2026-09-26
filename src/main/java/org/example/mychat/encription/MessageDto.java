package org.example.mychat.encription;


import io.netty.channel.ChannelHandler;
import lombok.Getter;
import lombok.Setter;

@ChannelHandler.Sharable
@Setter
@Getter
public class MessageDto {
    private String sender;
    private String recipient;
    private EncryptedMessage encryptedMessage;

    public MessageDto(String sender, String recipient, EncryptedMessage encryptedMessage) {
        this.sender = sender;
        this.recipient = recipient;
        this.encryptedMessage = encryptedMessage;
    }

    public MessageDto() {
    }
}