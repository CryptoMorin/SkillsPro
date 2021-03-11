import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null, "An unbelievable unexpected unhandled IndexOutOfBoundsException has occurred:\n" +
                "The level is greater than the max value, resulting in an RException." +
                "\n\nThis is a Minecraft plugin.\nPut it in the plugins folder. Don't just click on it.", "RException", JOptionPane.ERROR_MESSAGE);

    }
}
