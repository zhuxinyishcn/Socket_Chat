package edu.unl.cse.csce361.socket_chat;

public class CipherFactory {
    public static Cipher createCipher (String name, String[] keys) {
        Cipher cipher = null;
        if ("XOR".equalsIgnoreCase(name)) {
            cipher = new XORCiper();
            XORCiper.setKeyString(keys[0]);
        } else if ("Base64".equalsIgnoreCase(name)) {
            cipher = new Base64Ciper();
        }
        return cipher;
    }

    public static Cipher createCipher () {
        return new NullCipher();
    }
}
