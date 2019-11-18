package edu.unl.cse.csce361.socket_chat;

public class XORCiper implements Cipher {
    private static String keyString = "CSCE361 Chat Program";

    public static void setKeyString (String keyString) {
        XORCiper.keyString = keyString;
    }

    @Override
    public String encipher (String plaintext) {
        final String emptyString = "";
        final StringBuilder sb = new StringBuilder(emptyString);
        final int key = keyString.hashCode();
        for (int i = 0; i < plaintext.length(); i++) {
            sb.append(" ");
            sb.append(plaintext.charAt(i) ^ key);
        }
        return sb.toString();
    }

    @Override
    public String decipher (String ciphertext) {
        try {
            Integer.parseInt(ciphertext);
        } catch (NumberFormatException e) {
            return ciphertext;
        }
        final String emptyString = "";
        final StringBuilder sb = new StringBuilder(emptyString);
        final String[] token = ciphertext.split(" ", -1);
        final int key = keyString.hashCode();
        for (String s : token) {
            if (!s.isEmpty()) {
                sb.append((char) (Integer.parseInt(s) ^ key));
            }
        }

        return sb.toString();
    }
}
