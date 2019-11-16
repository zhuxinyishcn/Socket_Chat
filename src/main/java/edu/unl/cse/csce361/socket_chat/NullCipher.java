package edu.unl.cse.csce361.socket_chat;

public class NullCipher implements Cipher {
    @Override
    public String encipher (String plaintext) {
        return plaintext;
    }

    @Override
    public String decipher (String ciphertext) {
        return ciphertext;
    }
}
