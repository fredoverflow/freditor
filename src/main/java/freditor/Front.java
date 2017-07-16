package freditor;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;

public class Front {
    public static final Front font = new Front("/fonts/" + pickFontSize() + ".png");

    private static String pickFontSize() {
        String title = "Almost there...";
        String prompt = "Please pick a font size:";
        String[] possibilities = {"medium", "large"};
        String defaultChoice = "medium";
        Object choice = showInputDialog(null, prompt, title, QUESTION_MESSAGE, null, possibilities, defaultChoice);
        String size = String.valueOf(choice);
        return size;
    }

    private final int[] argb;
    private final int imageWidth;
    private final int imageHeight;

    public final int width;
    public final int height;

    private final HashMap<Integer, BufferedImage> colored = new HashMap<>();

    public Front(String pathname) {
        int[] argb;
        int imageWidth;
        int imageHeight;
        try {
            InputStream resource = getClass().getResourceAsStream(pathname);
            BufferedImage image = ImageIO.read(resource);
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();
            argb = new int[imageWidth * imageHeight];
            image.getData().getSamples(0, 0, imageWidth, imageHeight, 0, argb);
        } catch (IllegalArgumentException | IOException ex) {
            System.err.println("unable to load " + pathname + ", using fallback font instead");
            imageWidth = 1056;
            imageHeight = 44;
            argb = new int[imageWidth * imageHeight];
            unpackFallbackFont(argb);
        }
        this.argb = argb;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.width = this.imageWidth / 96;
        this.height = this.imageHeight / 2;
        initAlphaChannel();
    }

    private void initAlphaChannel() {
        final int size = argb.length;
        for (int i = 0; i < size; ++i) {
            argb[i] <<= 24;
        }
    }

    private void fillWithColor(int rgb) {
        final int size = argb.length;
        for (int i = 0; i < size; ++i) {
            argb[i] = argb[i] & 0xff000000 | rgb;
        }
    }

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

    private void unpackFallbackFont(int[] argb) {
        for (int i = 0, k = 0; k < 46464; ++i, k += 16) {
            int x = fallbackFont.charAt(i);
            argb[k + 0x0] = (x & 0x0001) == 0 ? 0 : 255;
            argb[k + 0x1] = (x & 0x0002) == 0 ? 0 : 255;
            argb[k + 0x2] = (x & 0x0004) == 0 ? 0 : 255;
            argb[k + 0x3] = (x & 0x0008) == 0 ? 0 : 255;
            argb[k + 0x4] = (x & 0x0010) == 0 ? 0 : 255;
            argb[k + 0x5] = (x & 0x0020) == 0 ? 0 : 255;
            argb[k + 0x6] = (x & 0x0040) == 0 ? 0 : 255;
            argb[k + 0x7] = (x & 0x0080) == 0 ? 0 : 255;
            argb[k + 0x8] = (x & 0x0100) == 0 ? 0 : 255;
            argb[k + 0x9] = (x & 0x0200) == 0 ? 0 : 255;
            argb[k + 0xa] = (x & 0x0400) == 0 ? 0 : 255;
            argb[k + 0xb] = (x & 0x0800) == 0 ? 0 : 255;
            argb[k + 0xc] = (x & 0x1000) == 0 ? 0 : 255;
            argb[k + 0xd] = (x & 0x2000) == 0 ? 0 : 255;
            argb[k + 0xe] = (x & 0x4000) == 0 ? 0 : 255;
            argb[k + 0xf] = (x & 0x8000) == 0 ? 0 : 255;
        }
    }

    private static final String fallbackFont = "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\30\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u3300\0\0\0\6\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u8000\0\60\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u7000\14\0\u3300\0\3\0\6\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\uc000\1\140\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\ud800\14\u8000\u3301\u0198\uc703\u01f0\uc006\300\0\0\0\u3000\370\u3e03\u01f0\uff30\uc7e1\uf87f\u07c0\0\u1800\u1800\u1f00\uc0fc\u3f87\ue1f0\uff0f\u0ff9\u061f\u07e3\u0df8\u8066\u1900\u1f0c\uc0fe\u3f87\uf1f0\u833f\u6c19\u0660\u981b\uf0ff\141\u607c\3\0\u0180\0\60\u0f80\u0600\u0300\u1860\u0780\0\0\0\0\0\3\0\0\0\u8000\u0303\u980e\15\u8000\u3301\uc198\ucd8f\u0318\u6006\u0180\0\0\0\u3000\u818c\u6303\u0318\u0338\uc030\u8c60\u0c61\0\u0c00\u3000\u3180\u6186\u618c\u6318\u0318\u8018\u0631\u0183\u0c60\u8066\u1981\u318c\u6186\u618c\u0318\u8303\u6c19\u0660\u181b\u30c0\140\u3060\6\0\u0180\0\60\300\u0600\u0300\u1860\u0600\0\0\0\0\0\3\0\0\0\uc000\u0300\u1818\7\u8000\u3301\u6198\u6d9b\u0318\u3006\u0300\0\0\0\u1800\uc306\uc183\u060c\u033c\uc018\u0660\u1833\0\u0600\u6000\u60c0\u3303\uc198\u660c\u0330\uc018\u0660\u0183\u0c60\u8063\u19c3\u60cc\u3306\uc198\u060c\u8303\u6c19\u8c60\u0c31\u30c0\300\u1860\14\0\u0180\0\60\300\u0600\u0300\u1860\u0600\0\0\0\0\0\3\0\0\0\u6000\u0300\60\0\u8000\1\u3198\u6733\u0318\u3000\u8300\u6061\0\0\u1800\u6306\uc183\u0600\u0336\30\u0660\u1833\u600c\u0300\uc000\u60c0\u33e3\uc198\u660c\u0330\uc018\u0660\u0183\u8c60\u8061\u19e7\u60cc\u3306\uc198\14\u8303\u6c19\u8c60\u0c31\u30c0\300\140\0\0\u0180\0\60\300\u0600\0\u1800\u0600\0\0\0\0\0\3\0\0\0\u6000\u0300\60\0\u8000\1\u37fe\u3003\u01b0\u1800\u0600\u6033\0\0\u0c00\u0386\uc183\u0600\u0333\30\u0630\u1833\u600c\u0180\u81ff\u6001\u3333\uc198\u600c\u0330\uc018\u0600\u0183\ucc60\u8060\u39bd\u60cc\u3306\uc198\14\u8303\u6c19\ud860\u0660\u3060\u0180\140\0\ue000\u3f87\u81f0\u7c3f\u07f8\ufe7f\u03c0\u1878\u8606\uf87f\u1f03\uc0fe\uf99f\ue3f8\u831f\u6c19\u0660\u9833\u60ff\u0300\60\0\u8000\1\u3198\u3003\340\u1800\u0600\u601e\0\0\u0c00\u03c6\uc003\u8300\u7f31\u03f8\u8c30\u1831\u600c\300\0\u3003\u331b\u6198\u600c\u0330\uc018\u0600\u0183\u6c60\u8060\u7999\u60cc\u3306\uc198\30\u8303\u6631\ud860\u0660\u3030\u0180\140\0\0\u618c\uc318\uc630\u80c0\u8661\u0301\u1860\u8603\u18d9\u3186\u6186\u0d98\u060c\u8303\u6c19\u0660\u1833\u60c0\u0300\60\0\u8000\1\u6198\u1803\360\u1800\u0600\u600c\0\0\u0600\u0366\u6003\uc1e0\uc030\u0618\uf818\u1830\0\140\0\u1806\u331b\u3f98\u600c\u7f30\uc3f8\ufe00\u0183\u3c60\u8060\ud981\u60cc\u3186\u6198\u01f0\u8303\u6631\u7060\u03c0\u3018\u0300\140\0\0\uc198\u660c\u8330\uc0c1\u0660\u0303\u9860\u8601\u1999\u60cc\u3306\u0798\14\u8303\u6c19\u8c60\u1831\u3860\u0300\340\0\u8000\1\uc198\u180f\u0d98\u1800\uc600\ufeff\7\u01ff\u0600\u0336\u3003\u6300\u8030\u0c19\u8c18\u1861\0\140\0\u0c06\uf31b\u619f\u600c\u0330\uc018\u067c\u0183\u3c60\u8060\u9981\u60cd\u30fe\u3f98\u0300\u8303\u6631\u7066\u03c0\u300c\u0300\140\0\0\uc198\u600c\u8330\uc0c1\u0660\u0303\ud860\u8600\u1999\u60cc\u3306\u0398\14\u8303\u6631\ud866\u1830\u6030\u0300\60\0\u8000\1\u0198\u0c1b\u0f0c\u1800\u0600\u600c\0\0\u0300\u031e\u1803\u6600\u8030\u0c19\u060c\u1fc3\0\300\0\u0c03\u331b\uc198\u600c\u0330\uc018\u0660\u0183\u6c60\u8060\u1981\u60cf\u3006\u0798\u0600\u8303\u6631\ud866\u0180\u3006\u0600\140\0\ue000\uc19f\u600c\uff30\uc0c1\u0660\u0303\u7860\u8600\u1999\u60cc\u3306\u0198\u03f8\u8303\u6631\u7066\u1830\u6018\u0300\60\0\0\0\u07fe\u0c33\u060c\u1800\u0600\u601e\0\0\u0300\u030e\u0c03\u6600\u8030\u0c19\u060c\u1803\0\u0180\u81ff\1\u3333\uc198\u600c\u0330\uc018\u0660\u8183\ucc61\u8060\u1981\u60ce\u3006\u0d98\u0600\u8303\u6361\ud86f\u0180\u3003\u0600\140\0\u3000\uc198\u600c\u0330\uc0c0\u0660\u0303\u7860\u8600\u1999\u60cc\u3306\u0198\u0600\u8303\u6631\u7066\u1830\u600c\u0300\60\0\0\0\u0198\ue633\u060c\u3000\u0300\u6033\u0300\0\u0180\u0306\u0603\ue600\u833f\u0c19\u0606\u1803\u600c\u0300\uc000\0\u33e3\uc198\u660c\u0330\uc018\u0660\u8183\u8c61\u8061\u1981\u60cc\u3006\u1998\u0600\u8303\ue361\u8c79\u8181\u3001\u0c00\140\0\u3000\uc198\u600c\u0330\uc0c0\u0660\u0303\ud860\u8600\u1999\u60cc\u3306\u0198\u0600\u8303\u6361\ud866\u1830\u6006\u0300\60\0\u8000\1\u3198\ub633\u060d\u3000\u8300\u6061\u0300\uc000\u0180\u0306\u0303\u060c\u8330\u0c19\u0606\u1803\u600c\u0600\u6000\u0c00\u3003\uc198\u660c\u0330\uc018\u0660\u8183\u0c61\u8063\u1981\u60cc\u3006\u3198\u060c\u8303\ue1c1\u8c70\u8181\u3001\u0c00\140\0\u3000\uc198\u660c\u0330\uc0c0\u0660\u0303\u9860\u8601\u1999\u60cc\u3306\u0198\u0600\u8303\u6361\u8c66\u1831\u6003\u0300\60\0\u8000\1\u6198\ub31b\u0f19\u6000\u0180\0\u0300\uc000\300\u018c\u0183\u0318\uc630\u0630\u8c06\u0c01\u600c\u0c00\u3000\u0c00\u3006\u6198\u6318\u0318\u8018\u0631\u0183\u0c33\u8066\u1981\u318c\u6006\u618f\u0318\uc603\u61c0\u0660\u8183\u3001\u1800\140\0\u3000\u6198\uc318\u8630\u80c1\u0661\u0303\u1860\u8603\u1999\u318c\u6186\u0198\u060c\u8603\u61c1\u0666\u9863\uc001\u0300\30\0\u8000\1\uc198\ue30f\u0df0\uc000\300\0\u0300\uc000\300\ue0f8\uff9f\u01f0\u7c30\u03e0\uf806\u07e0\u6000\u1800\u1800\u0c00\u33fc\u3f98\ue1f0\uff0f\31\u061f\u07e3\u0c1e\ubfe6\u1981\u1f0c\uc006\uc187\u01f0\u7c03\u21c0\u0640\u8183\uf0ff\u1801\174\0\ue000\u3f9f\u81f0\ufc3f\300\u067f\u0fc3\u1860\u9f86\u1999\u1f0c\uc0fe\u019f\u03f8\ufc3e\uc1c1\u063f\u9fc3\u80ff\u0303\16\0\0\0\0\3\0\0\0\0\u0180\0\0\0\0\0\0\0\0\0\u3000\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\14\0\0\0\0\0\0\0\0\0\0\0\0\0\0\140\0\143\0\0\0\6\30\0\0\0\0\u1800\0\0\0\0\0\0\0\3\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\30\0\0\0\0\0\0\0\0\u7fc0\0\0\0\0\0\140\0\143\0\0\0\6\30\0\0\0\0\u1800\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\60\0\143\0\0\0\6\30\0\0\0\0\u0c00\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u8000\37\0\76\0\0\0\6\30\0\0\0\0\u07e0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u8c00\1\0\0\0\0\0\0\0\14\0\0\0\0\0\0\0\30\u1c06\uc670\u3818\0\u1800\u0600\u181c\303\uc060\u3181\uc000\u0619\u0180\u9c07\u0631\0\300\ue030\u18c0\140\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u8c00\1\77\0\0\u3f80\170\u1e00\360\6\0\0\u0300\77\0\0\0\60\u3603\uc6d8\u6c18\0\u3000\u0300\u1836\u0183\u6030\u3183\u6000\u0c1b\u80c0\ub60d\u0631\0\u0180\ub018\u18c1\60\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u8c1f\1\140\0\0\0\314\u3300\u0198\3\0\0\u8380\141\300\u7806\0\u8060\u6301\uc398\u6c18\0\u6000\u0180\u1863\u0303\u3018\u3186\u6000\u180e\uc060\ue618\u0630\0\u0300\u180c\u18c3\30\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u8000\1\0\u8180\u80c1\61\0\140\0\0\0\314\u3300\u0180\0\u0ff8\0\u83c0\141\340\ucc07\u0600\0\0\0\u3800\u1ff0\37\0\0\0\0\0\376\0\0\0\0\uf800\u9830\301\u8000\u1981\u0f80\30\u1c06\uc670\u3818\0\u1800\u0600\u181c\u0183\uc0c0\u3181\ue06c\u060c\u0180\u9c07\u0631\0\300\ue030\u18c0\u1860\u3180\u8000\1\u01f0\u8180\u80c1\1\0\177\0\0\0\314\u1800\340\0\u0ccc\0\u8300\141\u80f0\uc107\u0600\uc0f8\u3e07\u81f0\u7c0f\u8198\ufe31\u9ff3\ufcff\u0fc7\uf07e\u1f83\u3186\u3e18\u81f0\u7c0f\u03e0\u8c00\u9831\u0cc1\ub066\u1981\u18c0\60\u3603\uc6d8\u6c18\0\u3000\u0300\u1836\u0303\u6060\u3183\ub018\u0c0d\u80c0\ub60d\u0631\0\u0180\ub018\u18c1\u1830\u3180\u8000\u0c01\u3318\uc330\u80c0\1\u87e0\141\0\uf000\3\170\u0c00\u0180\0\u0ccc\0\u8300\141\u20c0\u7186\u0608\u618c\u630c\uc318\uc618\uc18c\u0660\u8030\u0c01\u0300\uc018\u0600\u3306\u6318\uc318\uc618\u0630\u0600\u9837\u0cc1\u3066\u18c3\u30c0\u8060\u6301\uc398\u6c18\0\u6000\u0180\u1863\u0603\u3030\u3186\u3036\u1807\uc060\ue618\u0630\6\u0300\u180c\u18c3\u1818\u3180\0\u0c00\u6318\uc318\300\17\u8810\141\0\u0800\4\u8000\u0601\u0198\0\u0ccc\0\u8300\141\u30c0\uc0c6\14\u3306\uc198\u660c\u8330\uc18d\u0660\u8030\u0c01\u0300\uc018\u0600\u3306\uc198\u660c\u8330\u0c19\u0600\u9833\u0cc1\u3066\u18c3\u30c0\0\0\0\u3800\0\0\0\0\0\0\0\140\0\0\0\0\6\0\0\0\u1800\0\0\u3f00\uc018\u660f\u80c0\31\u93c8\u6061\6\uf400\11\u8000\u3f01\360\u8300\u0ccd\0\u8780\u6661\u18c0\ucc66\6\u3306\uc198\u660c\u8330\uc18d\u0600\u8030\u0c01\u0300\uc018\u0600\u7306\uc198\u660c\u8330\ucc19\u8660\u9833\u0cc1\u3066\uf866\u30c3\ue0fc\u3f07\uc1f8\u7e0f\u0778\uf81f\u07c0\uf03e\u0781\ue03c\u0f01\uf0f8\u3e07\u81f0\u7c0f\u03e0\uf806\u9836\u0cc1\u3066\uf983\u60c3\u8000\u6d81\u6018\u6618\u80c0\61\u1668\u307f\u3fe3\u3400\13\u8000\1\0\u8300\u0ccd\0\0\ucc3f\u0cc0\u7836\u0603\u3306\uc198\u660c\u8330\uc18d\u0600\u8030\u0c01\u0300\uc018\u0600\uf306\uc198\u660c\u8330\u8c19\uc631\u9833\u0cc1\u3066\u1866\u18c6\u0180\u600c\u0300\uc018\u8cc0\u8c31\u0c61\u1863\u0603\u8030\u0c01\u318c\u630c\uc318\uc618\u0630\u8c00\u9833\u0cc1\u3066\u1983\u60c6\u8000\uccc1\u6018\u3c18\u8000\61\u1068\u9800\u3001\u3400\13\u8000\1\0\u8300\u0ccd\6\0\u9800\u0601\u8018\u0601\u3306\uc198\u660c\u8330\udffd\u0600\u8030\u0c01\u0300\uc018\u0600\ub33f\uc199\u660c\u8330\u0c19\u661b\u9833\u0cc1\u3066\u183c\u1fcc\u0300\uc018\u0600\u8030\uccc1\u0660\u9833\u0cc1\u0606\u8030\u0c01\u3306\uc198\u660c\u8330\u0c19\u8600\u9833\u0cc1\u3066\u1983\u60cc\u8000\u0cc1\u60fc\u1818\u8000\61\u9068\ucc7f\u3000\uf400\11\uf800\37\0\u8300\u0cf9\6\u8000\u307f\u3303\uc00c\u030c\u3306\uc198\u660c\u8330\uc18d\ufe00\u87f0\ufc3f\u0301\uc018\u0600\u3306\uc19b\u660c\u8330\u0c19\u360e\u9833\u0cc1\u3066\u183c\u30cc\u0300\uc018\u0600\u8030\uccc1\u0600\u9833\u0cc1\u0606\u8030\u0c01\u3306\uc198\u660c\u8330\uec19\uc67f\u9833\u0cc1\u3066\u1983\u60cc\u8000\u0cc1\u6018\uff18\u80c0\61\u1068\u6600\u3000\uf400\10\u8000\1\0\u8300\u0cc1\6\0\u6000\u3986\u60f6\u018e\uf3fe\uff9f\ue7fc\uff3f\uc18d\u0600\u8030\u0c01\u0300\uc018\u0600\u3306\uc19e\u660c\u8330\u0c19\u1e0e\u9833\u0cc1\u3066\u1818\u60cc\ue3fc\uff1f\uc7f8\ufe3f\ucff9\ufe00\u9ff3\ufcff\u0607\u8030\u0c01\u3306\uc198\u660c\u8330\u0c19\u6600\u9833\u0cc1\u3066\u1983\u60cc\u8000\u0cc1\u6018\u1818\300\63\u1668\u6600\u3000\ub400\11\u8000\1\0\u8300\u0cc1\0\0\u6000\u3cc6\u319b\317\u3306\uc198\u660c\u8330\uc18d\u0600\u8030\u0c01\u0300\uc018\u0600\u3306\uc19c\u660c\u8330\u0c19\u0e1b\u9833\u0cc1\u3066\u1818\u60cc\u3306\uc198\u660c\u8330\uc0cd\u0600\u8030\u0c01\u0600\u8030\u0c01\u3306\uc198\u660c\u8330\u0c19\u3600\u9833\u0cc1\u3066\u1983\u60cc\u8000\u0cc1\uc018\u180f\300\36\u13c8\ucc00\0\u3400\13\u8000\1\0\u8300\u0cc1\0\0\u3000\u3663\u9999\u60cd\u3306\uc198\u660c\u8330\uc18d\u0660\u8030\u0c01\u0300\uc018\u0600\u3306\uc198\u660c\u8330\u8c19\u0631\u9833\u0cc1\u3066\u1818\u60cc\u3306\uc198\u660c\u8330\uc0cd\u0600\u8030\u0c01\u0600\u8030\u0c01\u3306\uc198\u660c\u8330\u0c19\u1e06\u9833\u0cc1\u3066\u1983\u60cc\u8000\uccc1\u6018\uff18\300\60\u0810\u9800\1\u0800\4\u8000\1\0\uc300\u0cc1\0\0\u9800\u3321\uc8c0\u60cc\u3306\uc198\u660c\u8330\uc18d\u0660\u8030\u0c01\u0300\uc018\u0600\u3306\uc198\u660c\u8330\ucc19\u0760\u9833\u0cc1\u3066\u1818\u60cc\u3306\uc198\u660c\u8330\uc0cd\u0660\u8030\u0c01\u0600\u8030\u0c01\u3306\uc198\u660c\u8330\u0c19\u0e06\u9833\u0cc1\u3066\u1983\u60cc\u8000\u6d81\u3618\u1830\300\60\u07e0\u3000\3\uf000\3\0\0\0\ue300\u0cc1\0\0\ucc00\u3f00\uc060\u318f\u3306\uc198\u660c\u8330\u818d\u0631\u8030\u0c01\u0300\uc018\u0600\u3186\u6318\uc318\uc618\u0630\u8c00\u0c61\u1863\u18c3\u1818\u31c6\u3306\uc198\u660c\u8330\u8ccd\u0c31\u1863\u18c3\u0606\u8030\u0c01\u318c\u6318\uc318\uc618\u0630\u8e06\u1861\u18c3\u30c6\u1986\u6186\u8000\u3f01\u0618\u1800\u80c0\61\0\u6000\6\0\0\uf800\37\0\ubf00\u0cc1\0\0\u6600\u3000\60\u1f0c\u3306\uc198\u660c\u8330\u1f8d\ufe1f\u9ff3\ufcff\u0fc7\uf07e\u1f83\u30fe\u3e18\u81f0\u7c0f\u03e0\uf800\u07c0\uf03e\u0f81\uf818\u1ec3\ue3fc\uff1f\uc7f8\ufe3f\u07f9\uf81f\u0fc1\uf07e\u1f83\ue0fc\u3f07\u30f8\u3e18\u81f0\u7c0f\u03e0\ufb00\u1fc0\uf0fe\u3f87\uf9fc\u7f03\0\u0c00\u07fc\0\0\37\0\0\0\0\0\0\0\0\u0300\0\u6000\0\0\u3000\u01f8\14\0\0\0\0\0\14\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u1800\0\0\0\0\0\0\14\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u1980\u6000\0\u0c00\0\0\0\0\0\0\0\0\0\0\0\0\u0300\0\u6000\0\0\0\0\0\0\0\0\0\0\14\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u1800\0\0\0\0\0\0\14\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u1980\u6000\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u0300\0\u6000\0\0\0\0\0\0\0\0\0\0\14\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u1800\0\0\0\0\0\0\14\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u18c0\u3000\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u0300\0\u3000\0\0\0\0\0\0\0\0\0\0\6\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u1800\0\0\0\0\0\0\6\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u187e\u1f80\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0";
}
