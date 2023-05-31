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

        tabs = new JTabbedPane();
        tabs.setFont(Fronts.sansSerif);

        for (Freditor freditor : loadFreditors(application, flexer, indenter)) {
            String title = freditor.file.getFileName().toString();
            tabs.addTab(title, null, withLineNumbers(create.apply(freditor)), TOOLTIP);
        }

        tabs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON3) {
                    int selectedIndex = tabs.getSelectedIndex();
                    String selectedTitle = tabs.getTitleAt(selectedIndex);

                    String nextTitle = nextTitle(selectedTitle);
                    int indexOfNextTitle = indexOfTitle(nextTitle);
                    if (indexOfNextTitle != -1) {
                        tabs.setSelectedIndex(indexOfNextTitle);
                    } else {
                        Freditor freditor = new Freditor(flexer, indenter, applicationDirectory.resolve(nextTitle));
                        try {
                            freditor.load();
                        } catch (IOException expected) {
                            // unless the file materialized after starting the application
                        }
                        int nextIndex = insertionIndex(nextTitle);
                        tabs.insertTab(nextTitle, null, withLineNumbers(create.apply(freditor)), TOOLTIP, nextIndex);
                        tabs.setSelectedIndex(nextIndex);
                    }
                }
                getSelectedEditor().requestFocusInWindow();
            }
        });

        int applicationIndex = indexOfTitle(application + ".txt");
        if (applicationIndex != -1) {
            tabs.setSelectedIndex(applicationIndex);
        }
    }

    private List<Freditor> loadFreditors(String application, Flexer flexer, Indenter indenter) {
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
            freditors.add(new Freditor(flexer, indenter, applicationDirectory.resolve(application + ".txt")));
        }
        return freditors;
    }

    private List<Path> sortedFiles() {
        try (Stream<Path> applicationFiles = Files.list(applicationDirectory)) {
            return applicationFiles
                    .filter(Files::isRegularFile)
                    .filter(TabbedEditors::fileHasPlausibleSize)
                    .filter(file -> !isLegacyBackupFile(file))
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

    private static boolean isLegacyBackupFile(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.length() == 31 && LEGACY_BACKUP_FILE.matcher(fileName).matches();
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
