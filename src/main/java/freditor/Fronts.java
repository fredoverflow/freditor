package freditor;

import java.awt.*;
import java.util.Arrays;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;

public class Fronts {
    public static final Front front = chooseFrontIcon().front;

    public static final int point = front.height * 2 / 3;
    public static final Font monospaced = new Font(Font.MONOSPACED, Font.PLAIN, point);
    public static final Font sansSerif = new Font(Font.SANS_SERIF, Font.PLAIN, point);

    private static FrontIcon chooseFrontIcon() {
        Front[] fronts = new Front[13];
        fronts[12] = Front.read("/font.png");
        // Replacing anonymous inner class with lambda causes DEADLOCK,
        // see https://stackoverflow.com/questions/45246122
        // noinspection Convert2Lambda
        Thread eleven = new Thread(new Runnable() {
            @Override
            public void run() {
                // 11 is the most expensive by a long shot
                fronts[11] = fronts[12].thirdScaled(11).halfScaled().halfScaled();
            }
        });
        eleven.start();
        fronts[10] = fronts[12].thirdScaled(5).halfScaled();
        fronts[9] = fronts[12].scaled(3).halfScaled().halfScaled();
        fronts[8] = fronts[12].thirdScaled(2);
        fronts[7] = fronts[12].thirdScaled(7).halfScaled().halfScaled();
        fronts[6] = fronts[12].halfScaled();
        fronts[5] = fronts[10].halfScaled();
        fronts[4] = fronts[8].halfScaled();
        fronts[3] = fronts[6].halfScaled();
        fronts[2] = fronts[4].halfScaled();
        final int EMPTY_SLOTS = 2;
        try {
            eleven.join();
        } catch (InterruptedException ex) {
            fronts[11] = fronts[12];
            ex.printStackTrace();
        }

        String title = "Almost there...";
        String prompt = "Please choose font height:";
        Object[] frontIcons = Arrays.stream(fronts).skip(EMPTY_SLOTS).map(FrontIcon::new).toArray();
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        Object defaultChoice = frontIcons[Math.min(screenHeight / 216, fronts.length - 1) - EMPTY_SLOTS];
        Object choice = showInputDialog(null, prompt, title, QUESTION_MESSAGE, null, frontIcons, defaultChoice);
        return (FrontIcon) (choice != null ? choice : defaultChoice);
    }
}
