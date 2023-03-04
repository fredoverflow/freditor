package freditor;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

public class SwingConfig {
    static {
        System.setProperty("sun.java2d.uiScale.enabled", "false");
    }

    public static final Font SANS_SERIF_PLAIN_16 = new Font(Font.SANS_SERIF, Font.PLAIN, 16);

    public static void metalWithDefaultFont(Font defaultFont) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof FontUIResource) {
                    UIManager.put(key, defaultFont);
                }
            }
        });
    }

    public static void nimbusWithDefaultFont(Font defaultFont) {
        EventQueue.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (info.getName().equals("Nimbus")) {
                        UIManager.setLookAndFeel(info.getClassName());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            UIManager.getLookAndFeelDefaults().put("defaultFont", defaultFont);
        });
    }
}
