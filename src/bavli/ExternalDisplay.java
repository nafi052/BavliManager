package bavli;

import javax.swing.*;   // יבוא מחלקות ממשק משתמש
import java.awt.*;      // יבוא מחלקות גרפיות
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File; 
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.io.BufferedReader; 
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException; 

/**
 * Swing GUI for BavliManager: load pages, search, notes, export, cache, and run tests.
 */
public class ExternalDisplay extends JFrame implements ActionListener {

    // GUI Components
    private JButton btnLoadPage, btnShowMishnayot, btnSearchQuote, btnShowFullText, btnSplitFile, btnLoadIndex; // כפתורים ראשיים
    private JButton btnRunTests, btnClearCache, btnCacheInfo; // כפתורים לטסטים ולמטמון
    private JButton btnExportHtml, btnAddNote, btnViewNotes, btnAdvancedSearch; // כפתורים חדשים לפונקציונליות נוספת
    private JTextArea outputArea, noteArea; // אזורי טקסט
    private JTextField txtMasechet, txtDaf, txtAmud, txtPerek, txtQuote, txtFilePath, txtMasechetMishnayot; // שדות קלט
    private JTextField txtExportPath;   // שדה נתיב לייצוא
    private JTabbedPane tabbedPane;     // לשוניות לתצוגת אזורים שונים
    private JCheckBox chkFuzzySearch;   // תיבת סימון לחיפוש מטושטש
    private JSpinner spnMaxDistance;    // בורר למרחק מקסימלי בחיפוש מטושטש
    
    // מצב נוכחי של הדף המוצג
    private String currentMasechet = "";
    private int currentDaf = -1;
    private char currentAmud = ' ';

    public ExternalDisplay() { // בונה את ממשק המשתמש
        setTitle("by Nafi פרויקט תלמוד בבלי");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT); // Set layout to RTL
        
        
        tabbedPane = new JTabbedPane(); // יצירת לשוניות ראשיות
        tabbedPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        JPanel controlPanel = createControlPanel();  // אזור הפקדים והאפשרויות
       
        JPanel pageViewPanel = createPageViewPanel();  // כלים בסיסיים
        tabbedPane.addTab("כלים בסיסיים", pageViewPanel);
        
        JPanel searchPanel = createSearchPanel(); // לשונית לחיפוש מתקדם
        tabbedPane.addTab("חיפוש מתקדם", searchPanel);
        
        JPanel notesPanel = createNotesPanel(); // לשונית להערות אישיות
        tabbedPane.addTab("הערות אישיות", notesPanel);
        
        JPanel advancedToolsPanel = createAdvancedToolsPanel(); // לשונית לכלים מתקדמים
        tabbedPane.addTab("כלים מתקדמים", advancedToolsPanel);

        // הוספת רכיבים לחלון
        add(controlPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        addActionListeners(); // מאזיני פעולות לכפתורים

        setSize(800, 600); // גודל חלון ברירת מחדל
        setLocationRelativeTo(null); // מרכז חלון
    }
    
    private JPanel createControlPanel() { // אזור הפקדים והאפשרויות
        JPanel controlPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // שינוי לגריד גדול יותר
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        // אזור 1: טעינת דף
        controlPanel.add(new JLabel("טען דף:"));
        JPanel loadPagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loadPagePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        txtMasechet = createTextField(10);
        txtDaf = createTextField(3);
        txtAmud = createTextField(1);
        btnLoadPage = createButton("טען");
        loadPagePanel.add(new JLabel("מסכת:"));
        loadPagePanel.add(txtMasechet);
        loadPagePanel.add(new JLabel("דף:"));
        loadPagePanel.add(txtDaf);
        loadPagePanel.add(new JLabel("עמוד (א/ב):"));
        loadPagePanel.add(txtAmud);
        loadPagePanel.add(btnLoadPage);
        controlPanel.add(loadPagePanel);

        // אזור 2: הצגת משניות
        controlPanel.add(new JLabel("הצג משניות:"));
        JPanel showMishnayotPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        showMishnayotPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        txtMasechetMishnayot = createTextField(10); // Create the new field
        txtPerek = createTextField(3);
        btnShowMishnayot = createButton("הצג");
        showMishnayotPanel.add(new JLabel("מסכת:"));
        showMishnayotPanel.add(txtMasechetMishnayot); // Use the new field
        showMishnayotPanel.add(new JLabel("פרק:"));
        showMishnayotPanel.add(txtPerek);
        showMishnayotPanel.add(btnShowMishnayot);
        controlPanel.add(showMishnayotPanel);

        // אזור 3: חיפוש בסיסי
        controlPanel.add(new JLabel("חפש ציטוט:"));
        JPanel searchQuotePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchQuotePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        txtQuote = createTextField(20);
        btnSearchQuote = createButton("חפש");
        btnAdvancedSearch = createButton("חיפוש מתקדם");
        searchQuotePanel.add(txtQuote);
        searchQuotePanel.add(btnSearchQuote);
        searchQuotePanel.add(btnAdvancedSearch);
        controlPanel.add(searchQuotePanel);

        // אזור 4: הצגת טקסט מלא
        controlPanel.add(new JLabel("הצגת טקסט התלמוד כולו:"));
        JPanel showFullTextPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        showFullTextPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        btnShowFullText = createButton("הצג");
        showFullTextPanel.add(btnShowFullText);
        controlPanel.add(showFullTextPanel);

        return controlPanel;
    }
    
    private JPanel createPageViewPanel() { // אזור תצוגת דף
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        
        outputArea = new JTextArea(20, 50); // אזור תצוגת טקסט
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        outputArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // כפתורי פעולה לדף
        actionPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT); 
        
        btnAddNote = createButton("הוסף/ערוך הערה"); // כפתור להוספת/עריכת הערה
        btnExportHtml = createButton("ייצוא לקובץ"); // כפתור לייצוא לדף
        
        actionPanel.add(btnAddNote); 
        actionPanel.add(btnExportHtml);
        
        panel.add(actionPanel, BorderLayout.SOUTH); 
        
        return panel;
    }
    
    private JPanel createSearchPanel() { // אזור חיפוש מתקדם
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel searchOptionsPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // אזור הגדרות חיפוש

        searchOptionsPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        // אפשרות לחיפוש מטושטש
        searchOptionsPanel.add(new JLabel("חיפוש מטושטש:"));
        chkFuzzySearch = new JCheckBox("אפשר טעויות הקלדה");
        chkFuzzySearch.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        searchOptionsPanel.add(chkFuzzySearch);
        
        // הגדרת מרחק מקסימלי לחיפוש מטושטש
        searchOptionsPanel.add(new JLabel("מרחק מקסימלי לטעויות:"));
        spnMaxDistance = new JSpinner(new SpinnerNumberModel(2, 1, 5, 1));
        searchOptionsPanel.add(spnMaxDistance);
        
        // חיפוש במסכת ספציפית
        searchOptionsPanel.add(new JLabel("חיפוש במסכת ספציפית:"));
        JPanel specificMasechetPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        specificMasechetPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JComboBox<String> cmbMasechtot = new JComboBox<>();
        cmbMasechtot.addItem("כל המסכתות");
        specificMasechetPanel.add(cmbMasechtot);
        searchOptionsPanel.add(specificMasechetPanel);
        
        // הוספת אזור האפשרויות לחלון החיפוש
        panel.add(searchOptionsPanel, BorderLayout.NORTH);
        
        // אזור תוצאות חיפוש
        JTextArea searchResultsArea = new JTextArea(15, 50);
        searchResultsArea.setEditable(false);
        searchResultsArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        panel.add(new JScrollPane(searchResultsArea), BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createNotesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // אזור רשימת ההערות
        JPanel notesListPanel = new JPanel(new BorderLayout());
        JLabel lblNotesTitle = new JLabel("הערות אישיות");
        lblNotesTitle.setFont(new Font("Arial", Font.BOLD, 16));
        notesListPanel.add(lblNotesTitle, BorderLayout.NORTH);
        
        DefaultListModel<String> notesListModel = new DefaultListModel<>();
        JList<String> notesList = new JList<>(notesListModel);
        notesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notesList.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        notesListPanel.add(new JScrollPane(notesList), BorderLayout.CENTER);
        
        // כפתורי פעולה להערות
        JPanel notesButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnViewNotes = createButton("צפה בכל ההערות");
        JButton btnDeleteNote = createButton("מחק הערה");
        notesButtonPanel.add(btnViewNotes);
        notesButtonPanel.add(btnDeleteNote);
        notesListPanel.add(notesButtonPanel, BorderLayout.SOUTH);
        
        panel.add(notesListPanel, BorderLayout.WEST);
        
        // אזור עריכת הערות
        JPanel noteEditPanel = new JPanel(new BorderLayout());
        JLabel lblNoteEditTitle = new JLabel("עריכת הערה");
        lblNoteEditTitle.setFont(new Font("Arial", Font.BOLD, 16));
        noteEditPanel.add(lblNoteEditTitle, BorderLayout.NORTH);
        
        noteArea = new JTextArea(20, 40);
        noteArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        noteArea.setFont(new Font("Arial", Font.PLAIN, 14));
        noteEditPanel.add(new JScrollPane(noteArea), BorderLayout.CENTER);
        
        JPanel noteEditButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnSaveNote = createButton("שמור הערה");
        noteEditButtonPanel.add(btnSaveNote);
        noteEditPanel.add(noteEditButtonPanel, BorderLayout.SOUTH);
        
        panel.add(noteEditPanel, BorderLayout.CENTER);
        
        // האזנה לשינויים ברשימה
        notesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && notesList.getSelectedValue() != null) {
                String selectedNote = notesList.getSelectedValue();
                // טעינת ההערה הנבחרת
                String[] noteInfo = PersonalNotes.decodeNoteFileName(selectedNote);
                if (noteInfo != null) {
                    try {
                        String masechet = noteInfo[0];
                        int daf = Integer.parseInt(noteInfo[1]);
                        char amud = noteInfo[2].charAt(0);
                        
                        String noteContent = PersonalNotes.loadNote(masechet, daf, amud);
                        noteArea.setText(noteContent);
                    } catch (Exception ex) {
                        noteArea.setText("שגיאה בטעינת ההערה: " + ex.getMessage());
                    }
                }
            }
        });
        
        // הגדרת פעולה לכפתור מחיקה
        btnDeleteNote.addActionListener(e -> {
            String selectedNote = notesList.getSelectedValue();
            if (selectedNote != null) {
                String[] noteInfo = PersonalNotes.decodeNoteFileName(selectedNote);
                if (noteInfo != null) {
                    try {
                        String masechet = noteInfo[0];
                        int daf = Integer.parseInt(noteInfo[1]);
                        char amud = noteInfo[2].charAt(0);
                        
                        boolean deleted = PersonalNotes.deleteNote(masechet, daf, amud);
                        if (deleted) {
                            notesListModel.removeElement(selectedNote);
                            noteArea.setText("");
                            JOptionPane.showMessageDialog(this, "ההערה נמחקה בהצלחה", "מחיקת הערה", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "שגיאה במחיקת ההערה", "שגיאה", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "שגיאה: " + ex.getMessage(), "שגיאה", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        // הגדרת פעולה לכפתור שמירה
        btnSaveNote.addActionListener(e -> {
            String selectedNote = notesList.getSelectedValue();
            if (selectedNote != null) {
                String[] noteInfo = PersonalNotes.decodeNoteFileName(selectedNote);
                if (noteInfo != null) {
                    try {
                        String masechet = noteInfo[0];
                        int daf = Integer.parseInt(noteInfo[1]);
                        char amud = noteInfo[2].charAt(0);
                        
                        PersonalNotes.saveNote(masechet, daf, amud, noteArea.getText());
                        JOptionPane.showMessageDialog(this, "ההערה נשמרה בהצלחה", "שמירת הערה", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "שגיאה בשמירת ההערה: " + ex.getMessage(), "שגיאה", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else if (currentMasechet != null && !currentMasechet.isEmpty() && currentDaf > 0) {
                // שמירת הערה לדף הנוכחי
                try {
                    PersonalNotes.saveNote(currentMasechet, currentDaf, currentAmud, noteArea.getText());
                    JOptionPane.showMessageDialog(this, "ההערה נשמרה בהצלחה", "שמירת הערה", JOptionPane.INFORMATION_MESSAGE);
                    refreshNotesList(notesListModel);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "שגיאה בשמירת ההערה: " + ex.getMessage(), "שגיאה", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "יש לבחור הערה מהרשימה או לטעון דף כדי לשמור הערה", "שגיאה", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // טעינה ראשונית של הערות
        refreshNotesList(notesListModel);
        
        // הגדרת פעולה לכפתור הצגת כל ההערות
        btnViewNotes.addActionListener(e -> refreshNotesList(notesListModel));
        
        return panel;
    }
    
    private void refreshNotesList(DefaultListModel<String> model) {
        model.clear();
        Map<String, Date> notes = PersonalNotes.getAllNotes();
        for (String fileName : notes.keySet()) {
            model.addElement(fileName);
        }
    }
    
    private JPanel createAdvancedToolsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        // אזור 1: הטענת אינדקס
        JPanel indexPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        indexPanel.setBorder(BorderFactory.createTitledBorder("ניהול אינדקס"));
        indexPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        btnLoadIndex = createButton("טען אינדקס");
        indexPanel.add(btnLoadIndex);
        panel.add(indexPanel);
        
        // אזור 2: ייצוא קבצים
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exportPanel.setBorder(BorderFactory.createTitledBorder("ייצוא קבצים"));
        exportPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        JLabel lblExportPath = new JLabel("נתיב לשמירה:");
        txtExportPath = createTextField(25);
        txtExportPath.setText(System.getProperty("user.home") + "\\Desktop\\exported_page.html");
        
        exportPanel.add(lblExportPath);
        exportPanel.add(txtExportPath);
        panel.add(exportPanel);
        
        // אזור 3: מטמון
        JPanel cachePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cachePanel.setBorder(BorderFactory.createTitledBorder("ניהול מטמון"));
        cachePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        btnClearCache = createButton("נקה מטמון");
        cachePanel.add(btnClearCache);
        btnCacheInfo = createButton("הצג מידע על מטמון");
        cachePanel.add(btnCacheInfo);
        panel.add(cachePanel);
        
        // אזור 4: פיצול קובץ ראשי
        JPanel splitFilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        splitFilePanel.setBorder(BorderFactory.createTitledBorder("פיצול קובץ ראשי"));
        splitFilePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        JLabel lblFilePath = new JLabel("נתיב לקובץ:");
        txtFilePath = createTextField(25);
        txtFilePath.setText("C:\\Users\\nafta\\Desktop\\java\\BavliManager (3)\\BavliManager\\bavli.txt");
        btnSplitFile = createButton("פצל קובץ");
        
        splitFilePanel.add(lblFilePath);
        splitFilePanel.add(txtFilePath);
        splitFilePanel.add(btnSplitFile);
        panel.add(splitFilePanel);
        
        // אזור 5: ריצת בדיקות
        JPanel testsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        testsPanel.setBorder(BorderFactory.createTitledBorder("בדיקות מערכת"));
        testsPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        btnRunTests = createButton("הרץ מערך בדיקות");
        testsPanel.add(btnRunTests);
        panel.add(testsPanel);
        
        return panel;
    }

    // Helper method to create and configure JTextField
    private JTextField createTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        return textField;
    }

    // Helper method to create and configure JButton
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        return button;
    }
    
    private void addActionListeners() {
        // כפתורי ניווט וחיפוש בסיסי
        btnLoadPage.addActionListener(this);
        btnShowMishnayot.addActionListener(this);
        btnSearchQuote.addActionListener(this);
        btnShowFullText.addActionListener(this);
        
        // כפתורים מתקדמים
        btnSplitFile.addActionListener(this);
        btnLoadIndex.addActionListener(this);
        btnRunTests.addActionListener(this);
        btnClearCache.addActionListener(this);
        btnCacheInfo.addActionListener(this);
        
        // כפתורים חדשים
        btnExportHtml.addActionListener(this);
        btnAddNote.addActionListener(this);
        btnViewNotes.addActionListener(this);
        btnAdvancedSearch.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        try {
            if (source == btnLoadPage) {
                loadPageAction();
            } else if (source == btnShowMishnayot) {
                showMishnayotAction();
            } else if (source == btnSearchQuote) {
                searchQuoteAction(false);
            } else if (source == btnAdvancedSearch) {
                // מעבר ללשונית חיפוש מתקדם
                tabbedPane.setSelectedIndex(1);
                searchQuoteAction(true);
            } else if (source == btnShowFullText) {
                showFullTextAction();
            } else if (source == btnSplitFile) {
                splitFileAction();
            } else if (source == btnLoadIndex) {
                loadIndexAction();
            } else if (source == btnRunTests) {
                runTestsAction();
            } else if (source == btnClearCache) {
                clearCacheAction();
            } else if (source == btnCacheInfo) {
                showCacheInfoAction();
            } else if (source == btnExportHtml) {
                exportHtmlAction();
            } else if (source == btnAddNote) {
                addNoteAction();
            }
        } catch (NumberFormatException nfe) {
            showError("שגיאה: יש להזין מספרים תקינים עבור דף ופרק.");
        } catch (Exception ex) {
            showError("שגיאה כללית: " + ex.getMessage());
            ex.printStackTrace(); // For debugging
        }
    }

    private void loadPageAction() {
        String masechet = txtMasechet.getText().trim();
        String dafStr = txtDaf.getText().trim();
        String amudStr = txtAmud.getText().trim();
        if (masechet.isEmpty() || dafStr.isEmpty() || amudStr.isEmpty() || amudStr.length() != 1) {
            showError("שגיאה: יש למלא מסכת, דף ועמוד (א/ב).");
            return;
        }
        int daf = Integer.parseInt(dafStr);
        char amud = amudStr.charAt(0);
        if (amud != 'א' && amud != 'ב') {
            showError("שגיאה: עמוד חייב להיות 'א' או 'ב'.");
            return;
        }
        
        // שמירת הדף הנוכחי
        currentMasechet = masechet;
        currentDaf = daf;
        currentAmud = amud;
        
        // טעינת הדף
        String page = FileManager.loadPage(masechet, daf, amud);
        
        // מעבר ללשונית תצוגת דף
        tabbedPane.setSelectedIndex(0);
        outputArea.setText(page.isEmpty() ? "לא נמצא הדף." : page);
        
        // בדיקה האם קיימת הערה לדף זה
        try {
            if (PersonalNotes.hasNote(masechet, daf, amud)) {
                String note = PersonalNotes.loadNote(masechet, daf, amud);
                noteArea.setText(note);
                tabbedPane.setSelectedIndex(2); // מעבר ללשונית הערות
                JOptionPane.showMessageDialog(this, "נמצאה הערה לדף זה. עברת ללשונית הערות.", "יש הערה לדף", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            System.err.println("שגיאה בבדיקת הערות: " + ex.getMessage());
        }
    }

    private void showMishnayotAction() {
        String masechet = txtMasechetMishnayot.getText().trim();
        String perekStr = txtPerek.getText().trim();
        if (masechet.isEmpty() || perekStr.isEmpty()) {
            showError("שגיאה: יש למלא מסכת ופרק.");
            return;
        }
        int perek = Integer.parseInt(perekStr);
        List<String> mishnayot = MishnaExtractor.getMishnayotOfPerek(masechet, perek);
        if (mishnayot.isEmpty()) {
            outputArea.setText("לא נמצאו משניות בפרק זה.");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("מסכת ").append(masechet).append(" פרק ").append(perek).append("\n\n");
            for (int i = 0; i < mishnayot.size(); i++) {
                sb.append("=== משנה ").append(i + 1).append(" ===\n");
                sb.append(mishnayot.get(i)).append("\n\n");
            }
            outputArea.setText(sb.toString());
            
            // מעבר ללשונית תצוגת דף
            tabbedPane.setSelectedIndex(0);
        }
    }

    private void searchQuoteAction(boolean advanced) {
        String quote = txtQuote.getText().trim();
        if (quote.isEmpty()) {
            showError("שגיאה: יש להכניס ציטוט לחיפוש.");
            return;
        }
        
        // אם זה חיפוש מתקדם נשתמש באפשרויות החיפוש המורחבות
        if (advanced && chkFuzzySearch != null) {
            boolean fuzzySearch = chkFuzzySearch.isSelected();
            int maxDistance = (Integer) spnMaxDistance.getValue();
            
            outputArea.setText("מחפש '" + quote + "'" + (fuzzySearch ? " (חיפוש מטושטש עם סבילות " + maxDistance + ")" : "") + "...\n");
            
            // הרץ את החיפוש בנפרד כדי לא לתקוע את הממשק
            new SwingWorker<List<String>, Void>() {
                @Override
                protected List<String> doInBackground() throws Exception {
                    return SearchEngine.searchByQuote(quote, fuzzySearch, maxDistance);
                }

                @Override
                protected void done() {
                    try {
                        List<String> results = get();
                        if (results.isEmpty()) {
                            outputArea.setText("לא נמצאו תוצאות.");
                        } else {
                            outputArea.setText(String.join("\n", results));
                        }
                    } catch (Exception ex) {
                        showError("שגיאה בחיפוש: " + ex.getMessage());
                    }
                }
            }.execute();
        } else {
            // חיפוש רגיל
            List<String> results = SearchEngine.searchByQuote(quote);
            if (results.isEmpty()) {
                outputArea.setText("לא נמצאו תוצאות.");
            } else {
                outputArea.setText(String.join("\n", results));
            }
            
            // מעבר ללשונית תצוגת דף
            tabbedPane.setSelectedIndex(0);
        }
    }

    private void showFullTextAction() {
        outputArea.setText("טוען את כל הטקסט..."); // Initial message on EDT

        new SwingWorker<Void, String>() {
            private boolean contentPublished = false; // Track if any content was actually published

            @Override
            protected Void doInBackground() throws Exception {
                // Path from existing code context
                String filePath = "C:\\Users\\nafta\\Desktop\\java\\BavliManager\\bavli.txt";
                try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
                    String line;
                    StringBuilder chunkBuffer = new StringBuilder();
                    int linesInChunk = 0;
                    final int BATCH_SIZE = 100; // Number of lines per chunk

                    while ((line = reader.readLine()) != null) {
                        chunkBuffer.append(line).append("\n");
                        linesInChunk++;
                        if (linesInChunk >= BATCH_SIZE) {
                            publish(chunkBuffer.toString());
                            chunkBuffer.setLength(0); // Reset buffer
                            linesInChunk = 0;
                        }
                    }
                    if (chunkBuffer.length() > 0) { // Publish any remaining lines
                        publish(chunkBuffer.toString());
                    }
                }
                // If doInBackground completes without publishing (e.g., empty file),
                // 'contentPublished' remains false. 'process' won't be called.
                // 'done()' will handle this state.
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                // This runs on EDT. SwingWorker ensures 'chunks' is not empty.
                if (!contentPublished) {
                    // First time process is called, replace the "Loading..." text.
                    outputArea.setText(chunks.get(0));
                    for (int i = 1; i < chunks.size(); i++) {
                        outputArea.append(chunks.get(i));
                    }
                    contentPublished = true;
                } else {
                    // Subsequent calls, just append.
                    for (String textChunk : chunks) {
                        outputArea.append(textChunk);
                    }
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // Call get() to propagate exceptions from doInBackground.
                    
                    if (!contentPublished) {
                        // No content was published. If get() didn't throw an exception, file was empty.
                        outputArea.setText("הקובץ ריק או שלא נטען תוכן.\\n");
                    } else {
                        // Content was published. Add a completion message.
                        outputArea.append("\\nטעינת הטקסט המלא הושלמה.\\n");
                    }
                    outputArea.setCaretPosition(0); // Scroll to top
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupt status
                    outputArea.setText("טעינת הטקסט הופרעה.\\n");
                    showError("טעינת הטקסט הופרעה: " + e.getMessage());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    outputArea.setText("שגיאה בטעינת הטקסט המלא.\\n");
                    showError("שגיאה בטעינת הטקסט המלא: " + (cause != null ? cause.getMessage() : e.getMessage()));
                    if (cause != null) cause.printStackTrace(); // For debugging
                } catch (Exception e) { 
                    outputArea.setText("שגיאה לא צפויה בטעינת הטקסט המלא.\\n");
                    showError("שגיאה לא צפויה: " + e.getMessage());
                    e.printStackTrace(); // For debugging
                }
            }
        }.execute();
    }

    private void splitFileAction() {
        String path = txtFilePath.getText().trim();
        if (path.isEmpty()) {
            showError("שגיאה: יש להכניס נתיב לקובץ.");
            return;
        }
        
        outputArea.setText("מפצל את הקובץ...");
        
        // הרץ את הפיצול בנפרד כדי לא לתקוע את הממשק
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                FileManager.splitFile(path);
                return null;
            }

            @Override
            protected void done() {
                outputArea.setText("פיצול הקובץ הושלם (אם הקובץ היה קיים וקריא).");
            }
        }.execute();
    }

    private void loadIndexAction() {
        outputArea.setText("טוען אינדקס...");
        
        // הרץ את טעינת האינדקס בנפרד כדי לא לתקוע את הממשק
        new SwingWorker<StringBuilder, Void>() {
            @Override
            protected StringBuilder doInBackground() throws Exception {
                DataIndex.loadIndex();
                DataIndex.buildMishnaIndex();
                
                StringBuilder indexInfo = new StringBuilder();
                indexInfo.append("אינדקס נטען.").append("\n");
                indexInfo.append("המסכתות הקיימות הן: ").append(DataIndex.getAllMasechtot()).append("\n");
                
                // דוגמה: קבל דפים למסכת ספציפית
                String sampleMasechet = "ברכות";
                indexInfo.append("דפים במסכת ").append(sampleMasechet).append(": ")
                       .append(DataIndex.getDafsForMasechet(sampleMasechet)).append("\n");
                indexInfo.append("בדוק אם מסכת ").append(sampleMasechet).append(" קיימת: ")
                       .append(DataIndex.masechetExists(sampleMasechet));
                
                return indexInfo;
            }

            @Override
            protected void done() {
                try {
                    outputArea.setText(get().toString());
                    // מעבר ללשונית תצוגת דף
                    tabbedPane.setSelectedIndex(0);
                } catch (Exception ex) {
                    showError("שגיאה בטעינת האינדקס: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void runTestsAction() {
        outputArea.setText("מריץ מערך בדיקות מקיף...\n");
        
        // הרץ את הבדיקות בנפרד כדי לא לתקוע את הממשק
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                PrintOutputCapturer capturer = new PrintOutputCapturer();
                System.setOut(capturer);
                Tests.runAllTests();
                return capturer.getOutput();
            }

            @Override
            protected void done() {
                try {
                    System.setOut(System.out);
                    outputArea.setText(get());
                    // מעבר ללשונית תצוגת דף
                    tabbedPane.setSelectedIndex(0);
                } catch (Exception ex) {
                    showError("שגיאה בהרצת הבדיקות: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void clearCacheAction() {
        CacheManager.clear();
        outputArea.setText("המטמון נוקה בהצלחה!");
    }

    private void showCacheInfoAction() {
        StringBuilder info = new StringBuilder();
        info.append("מידע על מטמון:").append("\n");
        info.append("========================").append("\n");
        info.append("גודל נוכחי של המטמון: ").append(CacheManager.getSize()).append(" פריטים").append("\n");
        info.append("הגודל המקסימלי של המטמון: ").append(CacheManager.getMaxSize()).append(" פריטים").append("\n");
        info.append("המטמון מנוהל כ-LinkedHashMap עם מדיניות LRU").append("\n");
        
        info.append("\nפריטים פופולריים במטמון:").append("\n");
        List<String> popularItems = CacheManager.getPopularItems(5);
        for (String item : popularItems) {
            info.append("- ").append(item).append("\n");
        }
        
        info.append("\nפונקציות זמינות לשימוש:").append("\n");
        info.append("- get(key): שליפת ערך לפי מפתח").append("\n");
        info.append("- put(key, value): הוספת ערך למטמון").append("\n");
        info.append("- contains(key): בדיקה אם מפתח קיים במטמון").append("\n");
        info.append("- remove(key): הסרת ערך מהמטמון").append("\n");
        info.append("- clear(): ניקוי כל המטמון").append("\n");
        
        outputArea.setText(info.toString());
    }

    private void exportHtmlAction() {
        if (currentMasechet.isEmpty() || currentDaf <= 0) {
            showError("שגיאה: יש לטעון דף תחילה.");
            return;
        }
        
        // יצירת שם קובץ ברירת מחדל
        String defaultFileName = currentMasechet + "_" + currentDaf + "_" + currentAmud + ".html";
        
        // הצגת דיאלוג לבחירת קובץ
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("בחר מיקום לשמירת הקובץ");
        fileChooser.setSelectedFile(new File(defaultFileName));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("HTML Files", "html", "htm"));
        
        // הגדרת כיוון לעברית
        fileChooser.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        for (java.awt.Component c : fileChooser.getComponents()) {
            c.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        // הצגת הדיאלוג
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            // קבלת הנתיב שנבחר
            String exportPath = fileChooser.getSelectedFile().getAbsolutePath();
            
            // הוספת סיומת .html אם אינה קיימת
            if (!exportPath.toLowerCase().endsWith(".html") && !exportPath.toLowerCase().endsWith(".htm")) {
                exportPath += ".html";
            }
            
            // עדכון שדה הנתיב בממשק
            txtExportPath.setText(exportPath);
            
            // ייצוא הדף
            boolean success = HtmlExporter.exportPage(currentMasechet, currentDaf, currentAmud, exportPath);
            
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "הדף יוצא בהצלחה לנתיב: " + exportPath, 
                    "ייצוא הושלם", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showError("שגיאה בייצוא הדף.");
            }
        }
    }

    private void addNoteAction() {
        if (currentMasechet.isEmpty() || currentDaf <= 0) {
            showError("שגיאה: יש לטעון דף תחילה.");
            return;
        }
        
        // מעבר ללשונית הערות אישיות
        tabbedPane.setSelectedIndex(2);
        
        try {
            // בדיקה אם כבר קיימת הערה לדף זה
            String existingNote = PersonalNotes.loadNote(currentMasechet, currentDaf, currentAmud);
            noteArea.setText(existingNote);
            
            String title = "הערות למסכת " + currentMasechet + " דף " + currentDaf + " עמוד " + currentAmud;
            JOptionPane.showMessageDialog(this, 
                "עברת למצב עריכת הערות. \nערוך את ההערה ולחץ על 'שמור הערה' כדי לשמור.", 
                title, JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            showError("שגיאה בטעינת הערה קיימת: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "שגיאה", JOptionPane.ERROR_MESSAGE);
    }

    // מחלקת עזר פנימית ללכידת הפלט
    private static class PrintOutputCapturer extends java.io.PrintStream {
        private StringBuilder output = new StringBuilder();

        public PrintOutputCapturer() {
            super(new java.io.OutputStream() {
                @Override
                public void write(int b) {}
            });
        }

        @Override
        public void println(String x) {
            output.append(x).append("\n");
        }

        @Override
        public void println(Object x) {
            output.append(x).append("\n");
        }

        @Override
        public void print(String x) {
            output.append(x);
        }

        @Override
        public void print(Object x) {
            output.append(x);
        }

        public String getOutput() {
            return output.toString();
        }
    }
}