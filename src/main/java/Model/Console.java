package Model;


public class Console {

    public void log(final String message) {
        System.out.println("[INFO] " + message);
    }

    public void err(final String message) {
        System.out.println("[ERROR] " + message);
    }
}
