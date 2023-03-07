package freditor;

import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Stream;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;

public class Fronts {
    public static final Front front = selectFrontIcon().front;

    public static final int point = front.height * 2 / 3;
    public static final Font monospaced = new Font(Font.MONOSPACED, Font.PLAIN, point);
    public static final Font sansSerif = new Font(Font.SANS_SERIF, Font.PLAIN, point);

    private static FrontIcon selectFrontIcon() {
        String prompt = "Select font height";
        FrontIcon[] frontIcons = loadFronts().map(FrontIcon::new).toArray(FrontIcon[]::new);
        FrontIcon initialSelection = initialSelectionFromAscending(frontIcons);
        Object selection = showInputDialog(null, prompt, prompt, QUESTION_MESSAGE, null, frontIcons, initialSelection);
        if (selection == null) {
            System.exit(0);
        }
        return (FrontIcon) selection;
    }

    private static Stream<Front> loadFronts() {
        Front _72 = Front.read("/font.png");
        // Replacing anonymous inner class with lambda causes DEADLOCK,
        // see https://stackoverflow.com/questions/45246122
        // noinspection Convert2Lambda
        ForkJoinTask<Front> _66 = ForkJoinPool.commonPool().submit(new Callable<Front>() {
            @Override
            public Front call() {
                // 66 is the most expensive by a long shot
                return _72.thirdScaled(11).halfScaled().halfScaled();
            }
        });
        Front _60 = _72.thirdScaled(5).halfScaled();
        Front _54 = _72.scaled(3).halfScaled().halfScaled();
        Front _48 = _72.thirdScaled(2);
        Front _42 = _72.thirdScaled(7).halfScaled().halfScaled();
        Front _36 = _72.halfScaled();
        Front _30 = _60.halfScaled();
        Front _24 = _48.halfScaled();
        Front _20 = _60.thirdScaled(1);
        Front _16 = _48.thirdScaled(1);
        Front _14 = _42.thirdScaled(1);
        Front _12 = _24.halfScaled();
        return Stream.of(_12, _14, _16, _20, _24, _30, _36, _42, _48, _54, _60, _66.join(), _72);
    }

    private static FrontIcon initialSelectionFromAscending(FrontIcon[] frontIcons) {
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int defaultWidth = screenWidth / 160;

        for (int i = frontIcons.length - 1; i > 0; --i) {
            if (frontIcons[i].front.width <= defaultWidth) {
                return frontIcons[i];
            }
        }
        return frontIcons[0];
    }
}
