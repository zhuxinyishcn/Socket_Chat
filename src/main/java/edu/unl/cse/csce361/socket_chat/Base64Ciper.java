package edu.unl.cse.csce361.socket_chat;

import java.util.Base64;

public class Base64Ciper implements Cipher {
    @Override
    public String encipher (String plaintext) {
        return Base64.getEncoder().encodeToString(plaintext.getBytes());
    }

    @Override
    public String decipher (String ciphertext) {
        try {
            Base64.getDecoder().decode(ciphertext);
        } catch (IllegalArgumentException e) {
            return ciphertext;
        }
        final byte[] decodedBytes = Base64.getDecoder().decode(ciphertext);
        return new String(decodedBytes);
    }
}
