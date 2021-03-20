package freditor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;

public class Front {
    private static final Front[] fronts;
    private static final int EMPTY_SLOTS;

    static {
        fronts = new Front[13];
        EMPTY_SLOTS = 2; // index * 6 == font size
        fronts[12] = Front.read("/font.png");
        fronts[11] = fronts[12].scaled(11).halfScaled().halfScaled().thirdScaled();
        fronts[10] = fronts[12].scaled(5).halfScaled().thirdScaled();
        fronts[9] = fronts[12].scaled(3).halfScaled().halfScaled();
        fronts[8] = fronts[12].scaled(2).thirdScaled();
        fronts[7] = fronts[12].scaled(7).halfScaled().halfScaled().thirdScaled();
        fronts[6] = fronts[12].halfScaled();
        fronts[5] = fronts[10].halfScaled();
        fronts[4] = fronts[8].halfScaled();
        fronts[3] = fronts[6].halfScaled();
        fronts[2] = fronts[4].halfScaled();
    }

    public static final Front front = pickFrontIcon().front;

    public static final int point = front.height * 2 / 3;
    public static final Font monospaced = new Font(Font.MONOSPACED, Font.PLAIN, point);
    public static final Font sansSerif = new Font(Font.SANS_SERIF, Font.PLAIN, point);

    private static FrontIcon pickFrontIcon() {
        String title = "Almost there...";
        String prompt = "Please pick font height:";
        Object[] possibilities = Arrays.stream(fronts).skip(EMPTY_SLOTS).map(FrontIcon::new).toArray();
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        Object defaultChoice = possibilities[Math.min(screenHeight / 216, fronts.length - 1) - EMPTY_SLOTS];
        Object choice = showInputDialog(null, prompt, title, QUESTION_MESSAGE, null, possibilities, defaultChoice);
        return (FrontIcon) (choice != null ? choice : defaultChoice);
    }

    private static final int COPY_BLUE_INTO_ALL_CHANNELS = 0x01010101;

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

    private Front scaled(int scale) {
        int[] scaled = new int[argb.length * scale * scale];
        final int width = imageWidth * scale;
        final int stride = width * (scale - 1);
        int src = 0;
        for (int dst = 0; dst < scaled.length; dst += stride) {
            for (int x = 0; x < imageWidth; ++x) {
                fillSquare(scaled, width, dst, scale, argb[src++]);
                dst += scale;
            }
        }
        return new Front(scaled, width, imageHeight * scale);
    }

    private void fillSquare(int[] array, int width, int offset, int size, int value) {
        for (int y = 0; y < size; ++y) {
            for (int x = 0; x < size; ++x) {
                array[offset + x] = value;
            }
            offset += width;
        }
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
