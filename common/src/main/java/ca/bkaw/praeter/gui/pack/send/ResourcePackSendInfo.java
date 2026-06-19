package ca.bkaw.praeter.gui.pack.send;

/**
 * Information used for sending a resource pack.
 *
 * @param url The URL of the resource pack.
 * @param hash The SHA-1 hash of the resource pack. Must be a 40-character hexadecimal string.
 */
public record ResourcePackSendInfo(String url, String hash) {
    public ResourcePackSendInfo {
        if (hash.length() != 40) {
            throw new IllegalArgumentException("Hash must be a 40-character hexadecimal string");
        }
        if (!hash.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("Hash must be a 40-character hexadecimal string");
        }
    }
}
