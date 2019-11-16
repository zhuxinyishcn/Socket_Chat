package edu.unl.cse.csce361.socket_chat;

import java.util.Base64;

public class Base64Ciper implements Cipher {
    @Override
    public String encipher (String plaintext) {
        System.out.println(Base64.getEncoder().encodeToString(plaintext.getBytes()));
        return Base64.getEncoder().encodeToString(plaintext.getBytes());
    }

    @Override
    public String decipher (String ciphertext) {
        final byte[] decodedBytes = Base64.getDecoder().decode(ciphertext);
        return new String(decodedBytes);
    }
}
