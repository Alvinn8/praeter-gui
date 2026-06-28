package ca.bkaw.praeter.gui.pack.send;

/**
 * Information used for sending a resource pack.
 */
public final class ResourcePackSendInfo {
    private final String url;
    private final String hash;

    public ResourcePackSendInfo(String url, String hash) {
        if (hash.length() != 40) {
            throw new IllegalArgumentException("Hash must be a 40-character hexadecimal string");
        }
        if (!hash.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("Hash must be a 40-character hexadecimal string");
        }
        this.url = url;
        this.hash = hash;
    }

    public String url() {
        return this.url;
    }

    public String hash() {
        return this.hash;
    }
}
