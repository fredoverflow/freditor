package freditor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;

public class Front {
    public static final String scale = pickFontScale();
    public static final Front font = Front.read("/font.png").scaled(scale);

    private static final int COPY_BLUE_INTO_ALL_CHANNELS = 0x01010101;

    private static String pickFontScale() {
        String title = "Almost there...";
        String prompt = "Please pick a font scale:";
        String[] possibilities = {"25%", "33%", "50%", "66%", "75%", "100%"};
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        String defaultChoice = screenHeight < 1000 ? "33%" : screenHeight < 1500 ? "50%" : "75%";
        Object choice = showInputDialog(null, prompt, title, QUESTION_MESSAGE, null, possibilities, defaultChoice);
        return choice != null ? choice.toString() : defaultChoice;
    }

    private final int[] argb;
    private final int imageWidth;
    private final int imageHeight;

    public final int width;
    public final int height;

    private Front(int[] argb, int imageWidth, int imageHeight) {
        if (argb.length != imageWidth * imageHeight) {
            throw new IllegalArgumentException(argb.length + " != " + imageWidth * imageHeight);
        }
        this.argb = argb;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        // The 192 visible Latin-1 characters are split into 2 lines
        this.width = this.imageWidth / 96;
        this.height = this.imageHeight / 2;

        copyBlueIntoAllChannels();
    }

    public static Front read(String pathname) {
        try {
            InputStream resource = Front.class.getResourceAsStream(pathname);
            BufferedImage image = ImageIO.read(resource);
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            int[] argb = new int[imageWidth * imageHeight];
            image.getData().getSamples(0, 0, imageWidth, imageHeight, 0, argb);
            return new Front(argb, imageWidth, imageHeight);
        } catch (IOException ex) {
            // There is no sensible way to recover from required but absent resources
            throw new RuntimeException(ex);
        }
    }

    public Front scaled(String scale) {
        switch (scale) {
            case "25%":
                return halfScaled().halfScaled();

            case "33%":
                return thirdScaled();

            case "50%":
                return halfScaled();

            case "66%":
                return doubleScaled().thirdScaled();

            case "75%":
                return tripleScaled().halfScaled().halfScaled();

            case "100%":
                return this;

            default:
                throw new IllegalArgumentException(scale);
        }
    }

    private Front halfScaled() {
        final int size = argb.length;
        int[] scaled = new int[size / 4];
        int dst = 0;
        for (int src = 0; src < size; src += imageWidth) {
            for (int x = 0; x < imageWidth; x += 2, src += 2) {
                int a = argb[src] & 255;
                int b = argb[src + 1] & 255;

                int c = argb[src + imageWidth] & 255;
                int d = argb[src + imageWidth + 1] & 255;

                scaled[dst++] = ((a + b + c + d + 2) / 4) * COPY_BLUE_INTO_ALL_CHANNELS;
            }
        }
        return new Front(scaled, imageWidth / 2, imageHeight / 2);
    }

    private Front thirdScaled() {
        final int size = argb.length;
        int[] scaled = new int[size / 9];
        final int imageWidth2 = imageWidth * 2;
        int dst = 0;
        for (int src = 0; src < size; src += imageWidth2) {
            for (int x = 0; x < imageWidth; x += 3, src += 3) {
                int a = argb[src] & 255;
                int b = argb[src + 1] & 255;
                int c = argb[src + 2] & 255;

                int d = argb[src + imageWidth] & 255;
                int e = argb[src + imageWidth + 1] & 255;
                int f = argb[src + imageWidth + 2] & 255;

                int g = argb[src + imageWidth2] & 255;
                int h = argb[src + imageWidth2 + 1] & 255;
                int i = argb[src + imageWidth2 + 2] & 255;

                scaled[dst++] = ((a + b + c + d + e + f + g + h + i + 4) / 9) * COPY_BLUE_INTO_ALL_CHANNELS;
            }
        }
        return new Front(scaled, imageWidth / 3, imageHeight / 3);
    }

    private Front doubleScaled() {
        int[] scaled = new int[argb.length * 4];
        final int size = scaled.length;
        final int imageWidth2 = imageWidth * 2;
        int src = 0;
        for (int dst = 0; dst < size; dst += imageWidth2) {
            for (int x = 0; x < imageWidth; ++x, dst += 2) {
                int nearest = argb[src++];

                scaled[dst] = nearest;
                scaled[dst + 1] = nearest;

                scaled[dst + imageWidth2] = nearest;
                scaled[dst + imageWidth2 + 1] = nearest;
            }
        }
        return new Front(scaled, imageWidth2, imageHeight * 2);
    }

    private Front tripleScaled() {
        int[] scaled = new int[argb.length * 9];
        final int size = scaled.length;
        final int imageWidth3 = imageWidth * 3;
        final int imageWidth6 = imageWidth * 6;
        int src = 0;
        for (int dst = 0; dst < size; dst += imageWidth6) {
            for (int x = 0; x < imageWidth; ++x, dst += 3) {
                int nearest = argb[src++];

                scaled[dst] = nearest;
                scaled[dst + 1] = nearest;
                scaled[dst + 2] = nearest;

                scaled[dst + imageWidth3] = nearest;
                scaled[dst + imageWidth3 + 1] = nearest;
                scaled[dst + imageWidth3 + 2] = nearest;

                scaled[dst + imageWidth6] = nearest;
                scaled[dst + imageWidth6 + 1] = nearest;
                scaled[dst + imageWidth6 + 2] = nearest;
            }
        }
        return new Front(scaled, imageWidth3, imageHeight * 3);
    }

    private void copyBlueIntoAllChannels() {
        final int size = argb.length;
        for (int i = 0; i < size; ++i) {
            argb[i] = (argb[i] & 255) * COPY_BLUE_INTO_ALL_CHANNELS;
        }
    }

    private void fillWithColor(int rgb) {
        final int size = argb.length;
        for (int i = 0; i < size; ++i) {
            argb[i] = argb[i] & 0xff000000 | rgb;
        }
    }

    private final HashMap<Integer, BufferedImage> colored = new HashMap<>();

    private synchronized BufferedImage coloredFont(int rgb) {
        rgb &= 0x00ffffff;
        BufferedImage result = colored.get(rgb);
        if (result == null) {
            result = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            fillWithColor(rgb);
            result.setRGB(0, 0, imageWidth, imageHeight, argb, 0, imageWidth);
            colored.put(rgb, result);
        }
        return result;
    }

    public void drawCharacter(Graphics g, int x, int y, char c, int rgb) {
        int sx = ((c & 0x7f) - 32) * width;
        int sy = (c >>> 7) * height;
        g.drawImage(coloredFont(rgb), x, y, x + width, y + height, sx, sy, sx + width, sy + height, null);
    }

    public void drawString(Graphics g, int x, int y, CharSequence s, int rgb) {
        for (int i = 0; i < s.length(); ++i) {
            drawCharacter(g, x, y, s.charAt(i), rgb);
            x += width;
        }
    }
}
