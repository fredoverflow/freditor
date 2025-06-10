package freditor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class Front {
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

    private void copyBlueIntoAllChannels() {
        for (int i = 0; i < argb.length; ++i) {
            argb[i] = (argb[i] & 255) * 0x01010101;
        }
    }

    static Front read(String pathname) {
        try {
            InputStream resource = Front.class.getResourceAsStream(pathname);
            BufferedImage image = ImageIO.read(resource);
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            int[] argb = new int[imageWidth * imageHeight];
            image.getData().getSamples(0, 0, imageWidth, imageHeight, 0, argb);
            return new Front(argb, imageWidth, imageHeight);
        } catch (IOException requiredResourceAbsent) {
            // There is no sensible way to recover from required but absent resources
            throw new RuntimeException(requiredResourceAbsent);
        }
    }

    Front halfScaled() {
        final int size = argb.length;
        int[] scaled = new int[size / 4];
        int dst = 0;
        for (int src = 0; src < size; src += imageWidth) {
            for (int x = 0; x < imageWidth; x += 2, src += 2) {
                int a = argb[src] & 255;
                int b = argb[src + 1] & 255;

                int c = argb[src + imageWidth] & 255;
                int d = argb[src + imageWidth + 1] & 255;

                scaled[dst++] = ((a + b + c + d + 2) / 4) * 0x01010101;
            }
        }
        return new Front(scaled, imageWidth / 2, imageHeight / 2);
    }

    Front thirdScaled(int virtualScale) {
        final int size = argb.length * virtualScale * virtualScale;
        int[] scaled = new int[size / 9];
        final int width = this.imageWidth * virtualScale;
        final int height = this.imageHeight * virtualScale;
        int dst = 0;

        final long reciprocal = 0x100000000L / virtualScale + 1;
        final long WIDTH = width * reciprocal;
        final long HEIGHT = height * reciprocal;

        for (long Y = 0; Y < HEIGHT; Y += 3 * reciprocal) {
            int y0 = (int) (Y >>> 32) * this.imageWidth;
            int y1 = (int) ((Y + reciprocal) >>> 32) * this.imageWidth;
            int y2 = (int) ((Y + 2 * reciprocal) >>> 32) * this.imageWidth;

            for (long X = 0; X < WIDTH; X += 3 * reciprocal) {
                int x0 = (int) (X >>> 32);
                int x1 = (int) ((X + reciprocal) >>> 32);
                int x2 = (int) ((X + 2 * reciprocal) >>> 32);

                int a = argb[y0 + x0] & 255;
                int b = argb[y0 + x1] & 255;
                int c = argb[y0 + x2] & 255;

                int d = argb[y1 + x0] & 255;
                int e = argb[y1 + x1] & 255;
                int f = argb[y1 + x2] & 255;

                int g = argb[y2 + x0] & 255;
                int h = argb[y2 + x1] & 255;
                int i = argb[y2 + x2] & 255;

                scaled[dst++] = ((a + b + c + d + e + f + g + h + i + 4) / 9) * 0x01010101;
            }
        }
        return new Front(scaled, width / 3, height / 3);
    }

    Front scaled(int scale) {
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

    private void fillWithColor(int rgb) {
        for (int i = 0; i < argb.length; ++i) {
            argb[i] = argb[i] & 0xff000000 | rgb;
        }
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

    public void drawMultilineString(Graphics g, int x0, int y, CharSequence s, int rgb) {
        int x = x0;
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            if (ch == '\n') {
                x = x0;
                y += height;
            } else {
                drawCharacter(g, x, y, ch, rgb);
                x += width;
            }
        }
    }

    public void drawIntRight(Graphics g, int x, int y, int number, int rgb) {
        if (number >= 0) {
            do {
                x -= width;
                int digit = number % 10;
                number = number / 10;
                drawCharacter(g, x, y, (char) ('0' + digit), rgb);
            } while (number != 0);
        } else {
            do {
                x -= width;
                int digit = number % 10;
                number = number / 10;
                drawCharacter(g, x, y, (char) ('0' - digit), rgb);
            } while (number != 0);
            x -= width;
            drawCharacter(g, x, y, '-', rgb);
        }
    }

    public void drawHexRight(Graphics g, int x, int y, int number, int rgb) {
        do {
            x -= width;
            int digit = number & 15;
            number = number >>> 4;
            drawCharacter(g, x, y, "0123456789abcdef".charAt(digit), rgb);
        } while (number != 0);
    }
}
