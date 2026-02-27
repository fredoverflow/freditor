package freditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TabbedEditors {
    private static final String TOOLTIP = "\uD83D\uDDB0 new tab";

    private final Path applicationDirectory;
    private final Flexer flexer;
    private final Indenter indenter;
    private final Function<Freditor, FreditorUI> create;

    public final JTabbedPane tabs;

    private JPanel withLineNumbers(FreditorUI editor) {
        JPanel lineNumbersAndEditor = new JPanel();
        lineNumbersAndEditor.setLayout(new BoxLayout(lineNumbersAndEditor, BoxLayout.X_AXIS));
        lineNumbersAndEditor.add(new LineNumbers(editor));
        lineNumbersAndEditor.add(editor);
        editor.setComponentToRepaint(tabs);
        return lineNumbersAndEditor;
    }

    private static FreditorUI editorInside(Component lineNumbersAndEditor) {
        return (FreditorUI) ((JPanel) lineNumbersAndEditor).getComponent(1);
    }

    public TabbedEditors(String application, Flexer flexer, Indenter indenter, Function<Freditor, FreditorUI> create) {

        applicationDirectory = Paths.get(System.getProperty("user.home")).resolve(application);
        String applicationTxt = application + ".txt";
        this.flexer = flexer;
        this.indenter = indenter;
        this.create = create;

        tabs = new JTabbedPane();
        tabs.setFont(Fronts.sansSerif);

        for (Freditor freditor : loadFreditors(applicationTxt)) {
            String title = freditor.file.getFileName().toString();
            tabs.addTab(title, null, withLineNumbers(create.apply(freditor)), TOOLTIP);
        }

        tabs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON3) {
                    selectOrCreateEditor(nextTitle(getSelectedTitle()));
                }
                getSelectedEditor().requestFocusInWindow();
            }
        });

        int applicationIndex = indexOfTitle(applicationTxt);
        if (applicationIndex != -1) {
            tabs.setSelectedIndex(applicationIndex);
        }
    }

    private List<Freditor> loadFreditors(String applicationTxt) {
        List<Freditor> freditors = new ArrayList<>();
        for (Path file : sortedFiles()) {
            Freditor freditor = new Freditor(flexer, indenter, file);
            try {
                freditor.load();
                if (freditor.length() > 0) {
                    freditors.add(freditor);
                }
            } catch (IOException loadingFailed) {
                // If loading from user.home fails, there is no sensible way to recover
                loadingFailed.printStackTrace();
            }
        }
        if (freditors.isEmpty()) {
            freditors.add(new Freditor(flexer, indenter, applicationDirectory.resolve(applicationTxt)));
        }
        return freditors;
    }

    private List<Path> sortedFiles() {
        try (Stream<Path> applicationFiles = Files.list(applicationDirectory)) {
            return applicationFiles
                    .filter(Files::isRegularFile)
                    .filter(TabbedEditors::fileHasPlausibleSize)
                    .filter(file -> !ignoreFile(file))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException directoryAbsent) {
            return Collections.emptyList();
        }
    }

    private static boolean fileHasPlausibleSize(Path file) {
        try {
            long size = Files.size(file);
            return 0 < size && size < 100_000;
        } catch (IOException sizeUndetectable) {
            return false;
        }
    }

    private static boolean ignoreFile(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.equals("report") ||
                fileName.length() == 31 && LEGACY_BACKUP_FILE.matcher(fileName).matches();
    }

    private static final Pattern LEGACY_BACKUP_FILE = Pattern.compile("[A-Za-z0-9_-]{27}[.]txt");

    private static String nextTitle(String title) {
        int number;
        try {
            number = Integer.parseInt(title);
        } catch (NumberFormatException notAnInteger) {
            number = 0;
        }
        return String.format("%02d", number + 1);
    }

    private int indexOfTitle(String title) {
        int n = tabs.getTabCount();
        for (int i = 0; i < n; ++i) {
            if (tabs.getTitleAt(i).equals(title)) {
                return i;
            }
        }
        return -1;
    }

    private int insertionIndex(String title) {
        for (int i = tabs.getTabCount() - 1; i >= 0; --i) {
            if (tabs.getTitleAt(i).compareTo(title) <= 0) {
                return i + 1;
            }
        }
        return 0;
    }

    public FreditorUI getSelectedEditor() {
        return editorInside(tabs.getSelectedComponent());
    }

    public void selectEditor(FreditorUI editor) {
        int n = tabs.getTabCount();
        for (int i = 0; i < n; ++i) {
            if (editorInside(tabs.getComponentAt(i)) == editor) {
                tabs.setSelectedIndex(i);
                return;
            }
        }
    }

    public void selectOrCreateEditor(String title) {
        int index = indexOfTitle(title);
        if (index == -1) {
            Freditor freditor = new Freditor(flexer, indenter, applicationDirectory.resolve(title));
            try {
                freditor.load();
            } catch (IOException expected) {
                // ...unless the file materialized after starting the application
            }
            index = insertionIndex(title);
            tabs.insertTab(title, null, withLineNumbers(create.apply(freditor)), TOOLTIP, index);
        }
        tabs.setSelectedIndex(index);
    }

    public String getSelectedTitle() {
        return tabs.getTitleAt(tabs.getSelectedIndex());
    }

    public boolean isReportSelected() {
        return getSelectedTitle().equals("report");
    }

    public void selectReport() {
        if (!isReportSelected()) {
            nonReport = getSelectedEditor();
            selectOrCreateEditor("report");
        }
    }

    private FreditorUI nonReport;

    public void selectNonReport() {
        if (isReportSelected()) {
            selectEditor(nonReport);
        }
    }

    public Stream<FreditorUI> stream() {
        return Arrays.stream(tabs.getComponents()).map(TabbedEditors::editorInside);
    }

    public void saveOnExit(Window window) {
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                stream().forEach(FreditorUI::saveWithBackup);
            }
        });
    }
}
