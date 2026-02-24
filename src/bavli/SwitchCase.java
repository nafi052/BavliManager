package bavli;

import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.Date;
import java.io.IOException;

/**
 * Main console menu: handles user choices and delegates to FileManager, SearchEngine,
 * PersonalNotes, HtmlExporter, CacheManager, and ExternalDisplay.
 */
public class SwitchCase {

    /** Runs the interactive console menu loop. */
    public static void displayMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nבחר פעולה:");
            System.out.println("1. טען דף מסוים");
            System.out.println("2. הצג משניות לפי מסכת ופרק");
            System.out.println("3. חפש ציטוט");
            System.out.println("4. הצגת טקסט התלמוד כולו");
            System.out.println("5. יציאה");
            System.out.println("");
            System.out.println("אפשריות מערכת");
            System.out.println("6. פיצול הקובץ הראשי לדפים");
            System.out.println("7. טעינת אינדקסים");
            System.out.println("8. טסטים");
            System.out.println("9. ממשק גרפי");
            System.out.println("\nאפשרויות מתקדמות");
            System.out.println("10. חיפוש מתקדם (עם תמיכה בטעויות הקלדה)");
            System.out.println("11. ניהול הערות אישיות");
            System.out.println("12. ייצוא דף/משניות לקובץ");
            System.out.println("13. ניהול מטמון");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("שם מסכת: ");
                    String masechet = scanner.nextLine();
                    System.out.print("הזן דף: (במספרים) ");
                    try { 
                        int daf = Integer.parseInt(scanner.nextLine());
                        System.out.print("עמוד (1/2 או א/ב): ");
                        char amud = scanner.nextLine().charAt(0);
                        String page = FileManager.loadPage(masechet, daf, amud);
                        System.out.println(page.isEmpty() ? "לא נמצא הדף." : page);
                        
                        // בדיקה אם קיימת הערה אישית לדף זה
                        if (PersonalNotes.hasNote(masechet, daf, amud)) {
                            System.out.println("\n=== הערה אישית לדף זה ===");
                            try {
                                String note = PersonalNotes.loadNote(masechet, daf, amud);
                                System.out.println(note);
                            } catch (IOException e) {
                                System.out.println("שגיאה בטעינת ההערה: " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("שגיאה: " + e.getMessage());
                    }

                break;
                case "2":
                    System.out.print("שם מסכת: ");
                    String m = scanner.nextLine();
                    System.out.print("מספר פרק: ");
                    try { int perek = Integer.parseInt(scanner.nextLine());
                        List<String> mishnayot = MishnaExtractor.getMishnayotOfPerek(m, perek);
                        if (mishnayot.isEmpty()) {
                            System.out.println("לא נמצאו משניות בפרק זה.");
                        } else {
                            for (int i = 0; i < mishnayot.size(); i++) {
                                System.out.println("\n--- משנה " + (i + 1) + " ---");
                                System.out.println(mishnayot.get(i));
                            }
                            
                            // אפשרות לייצוא המשניות
                            System.out.print("\nהאם ברצונך לייצא את המשניות לקובץ? (כ/ל): ");
                            String exportChoice = scanner.nextLine();
                            if (exportChoice.equals("כ")) {
                                System.out.print("הזן נתיב לקובץ הייצוא (ברירת מחדל: C:\\temp\\mishnayot.pdf): ");
                                String exportPath = scanner.nextLine();
                                if (exportPath.trim().isEmpty()) {
                                    exportPath = "C:\\temp\\mishnayot.pdf";
                                }
                                boolean success = HtmlExporter.exportMishnayot(m, perek, exportPath);
                                System.out.println(success ? "הייצוא הושלם בהצלחה" : "שגיאה בייצוא");
                            }
                        }
                            
                    } catch (Exception e) {
                        System.out.println("שגיאה: " + e.getMessage());
                    }
                    
                break;
                case "3":
                    System.out.print("הכנס ציטוט לחיפוש: ");
                    String quote = scanner.nextLine();
                    List<String> results = SearchEngine.searchByQuote(quote);
                    for (String res : results) {
                        System.out.println(res);
                    }
                break;

                case "4":
                    System.out.println("הצגת טקסט התלמוד כולו");
                    System.out.print("הזן נתיב לקובץ bavli.txt (ברירת מחדל: C:\\Users\\nafta\\Desktop\\java\\BavliManager\\bavli.txt): ");
                    String fullTextPath = scanner.nextLine();
                    if (fullTextPath.trim().isEmpty()) {
                        fullTextPath = "C:\\Users\\nafta\\Desktop\\java\\BavliManager\\bavli.txt";
                    }
                    String fullText = FileManager.loadFullText(fullTextPath);
                    System.out.println(fullText); // הדפסת הטקסט המלא של התלמוד
                break;

                case "5":
                    System.out.println("להתראות!");
                    scanner.close();
                    return;

                case "6":
                    System.out.print("הכנס נתיב לקובץ bavli.txt: ");
                    String path = scanner.nextLine();
                    FileManager.splitFile(path); // פיצול הקובץ הראשי לדפים
                break;

                case "7":
                    DataIndex.loadIndex(); // טעינת אינדקס המסכתות
                    DataIndex.buildMishnaIndex(); // בניית אינדקס משניות 
                    System.out.println("אינדקסים נטענו בהצלחה");
                    
                    // הצגת מידע מהאינדקס
                    System.out.println("\nמסכתות קיימות במערכת:");
                    for (String masechtName : DataIndex.getAllMasechtot()) {
                        System.out.println("- " + masechtName);
                    }
                break;
                
                case "8":
                    Tests.runAllTests(); // הרצת כל הבדיקות
                break;                 
                
                case "9":
                    System.out.println("מפעיל את הממשק הגרפי...");
                    // המתנה קצרה לפני הפעלת הממשק הגרפי
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        try {
                            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ExternalDisplay gui = new ExternalDisplay();
                        gui.setAlwaysOnTop(true); // הגדרת החלון להיות תמיד בחזית
                        gui.setVisible(true);
                        gui.toFront();
                        
                        // הגדרת טיימר להסרת דגל ה-AlwaysOnTop לאחר שהחלון כבר בחזית
                        new javax.swing.Timer(1000, evt -> {
                            ((javax.swing.Timer)evt.getSource()).stop();
                            gui.setAlwaysOnTop(false);
                        }).start();
                    });
                    
                    // יצירת השהייה קלה להמתנה עד שהחלון נפתח לפני המשך התוכנית
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                break;
                
                case "10":
                    System.out.print("הכנס ציטוט לחיפוש: ");
                    String advancedQuote = scanner.nextLine();
                    System.out.print("האם להפעיל חיפוש מטושטש עם סבילות לטעויות? (כ/ל): ");
                    String fuzzyChoice = scanner.nextLine();
                    
                    boolean fuzzySearch = fuzzyChoice.equals("כ");
                    int maxDistance = 2; // ברירת מחדל
                    
                    if (fuzzySearch) {
                        System.out.print("הכנס מרחק מקסימלי לטעויות (1-5, ברירת מחדל 2): ");
                        String distanceStr = scanner.nextLine();
                        if (!distanceStr.trim().isEmpty()) {
                            try {
                                maxDistance = Integer.parseInt(distanceStr);
                                if (maxDistance < 1 || maxDistance > 5) {
                                    maxDistance = 2;
                                    System.out.println("ערך לא חוקי, משתמש בערך ברירת מחדל: 2");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("ערך לא חוקי, משתמש בערך ברירת מחדל: 2");
                            }
                        }
                    }
                    
                    System.out.println("מחפש '" + advancedQuote + "'" + 
                                      (fuzzySearch ? " (חיפוש מטושטש עם סבילות " + maxDistance + ")" : "") + "...");
                    List<String> advancedResults = SearchEngine.searchByQuote(advancedQuote, fuzzySearch, maxDistance);
                    
                    for (String res : advancedResults) {
                        System.out.println(res);
                    }
                break;
                
                case "11":
                    boolean exitNotes = false;
                    while (!exitNotes) {
                        System.out.println("\n=== ניהול הערות אישיות ===");
                        System.out.println("1. הוספת/עדכון הערה לדף");
                        System.out.println("2. צפייה בהערה לדף");
                        System.out.println("3. מחיקת הערה מדף");
                        System.out.println("4. רשימת כל ההערות");
                        System.out.println("5. חזרה לתפריט הראשי");
                        
                        String noteChoice = scanner.nextLine();
                        
                        switch (noteChoice) {
                            case "1": // הוספת/עדכון הערה
                                try {
                                    System.out.print("שם מסכת: ");
                                    String noteMasechet = scanner.nextLine();
                                    System.out.print("מספר דף: ");
                                    int noteDaf = Integer.parseInt(scanner.nextLine());
                                    System.out.print("עמוד (א/ב): ");
                                    char noteAmud = scanner.nextLine().charAt(0);
                                    
                                    // בדיקה אם כבר קיימת הערה
                                    String existingNote = "";
                                    if (PersonalNotes.hasNote(noteMasechet, noteDaf, noteAmud)) {
                                        existingNote = PersonalNotes.loadNote(noteMasechet, noteDaf, noteAmud);
                                        System.out.println("קיימת הערה קודמת לדף זה:");
                                        System.out.println(existingNote);
                                    }
                                    
                                    System.out.println("הכנס את ההערה החדשה (סיים בשורה שמכילה רק END):");
                                    StringBuilder noteContent = new StringBuilder();
                                    String line;
                                    while (!(line = scanner.nextLine()).equals("END")) {
                                        noteContent.append(line).append("\n");
                                    }
                                    
                                    PersonalNotes.saveNote(noteMasechet, noteDaf, noteAmud, noteContent.toString());
                                    System.out.println("ההערה נשמרה בהצלחה");
                                } catch (Exception e) {
                                    System.out.println("שגיאה: " + e.getMessage());
                                }
                            break;
                            
                            case "2": // צפייה בהערה
                                try {
                                    System.out.print("שם מסכת: ");
                                    String viewMasechet = scanner.nextLine();
                                    System.out.print("מספר דף: ");
                                    int viewDaf = Integer.parseInt(scanner.nextLine());
                                    System.out.print("עמוד (א/ב): ");
                                    char viewAmud = scanner.nextLine().charAt(0);
                                    
                                    if (PersonalNotes.hasNote(viewMasechet, viewDaf, viewAmud)) {
                                        String note = PersonalNotes.loadNote(viewMasechet, viewDaf, viewAmud);
                                        System.out.println("\n=== הערה למסכת " + viewMasechet + 
                                                         " דף " + viewDaf + " עמוד " + viewAmud + " ===");
                                        System.out.println(note);
                                    } else {
                                        System.out.println("לא נמצאה הערה לדף זה");
                                    }
                                } catch (Exception e) {
                                    System.out.println("שגיאה: " + e.getMessage());
                                }
                            break;
                            
                            case "3": // מחיקת הערה
                                try {
                                    System.out.print("שם מסכת: ");
                                    String deleteMasechet = scanner.nextLine();
                                    System.out.print("מספר דף: ");
                                    int deleteDaf = Integer.parseInt(scanner.nextLine());
                                    System.out.print("עמוד (א/ב): ");
                                    char deleteAmud = scanner.nextLine().charAt(0);
                                    
                                    if (PersonalNotes.hasNote(deleteMasechet, deleteDaf, deleteAmud)) {
                                        System.out.print("האם אתה בטוח שברצונך למחוק את ההערה? (כ/ל): ");
                                        String confirmDelete = scanner.nextLine();
                                        
                                        if (confirmDelete.equals("כ")) {
                                            boolean deleted = PersonalNotes.deleteNote(deleteMasechet, deleteDaf, deleteAmud);
                                            if (deleted) {
                                                System.out.println("ההערה נמחקה בהצלחה");
                                            } else {
                                                System.out.println("שגיאה במחיקת ההערה");
                                            }
                                        }
                                    } else {
                                        System.out.println("לא נמצאה הערה לדף זה");
                                    }
                                } catch (Exception e) {
                                    System.out.println("שגיאה: " + e.getMessage());
                                }
                            break;
                            
                            case "4": // רשימת כל ההערות
                                Map<String, Date> notes = PersonalNotes.getAllNotes();
                                if (notes.isEmpty()) {
                                    System.out.println("לא נמצאו הערות אישיות במערכת");
                                } else {
                                    System.out.println("\n=== הערות אישיות במערכת ===");
                                    for (Map.Entry<String, Date> entry : notes.entrySet()) {
                                        String fileName = entry.getKey();
                                        String[] noteInfo = PersonalNotes.decodeNoteFileName(fileName);
                                        if (noteInfo != null) {
                                            System.out.println("מסכת " + noteInfo[0] + " דף " + noteInfo[1] + 
                                                             " עמוד " + noteInfo[2] + " - עודכן ב: " + entry.getValue());
                                        }
                                    }
                                }
                            break;
                            
                            case "5": // חזרה לתפריט ראשי
                                exitNotes = true;
                            break;
                            
                            default:
                                System.out.println("אפשרות לא חוקית");
                        }
                    }
                break;
                
                case "12":
                    System.out.println("\n=== ייצוא דף/משניות לקובץ ===");
                    System.out.println("1. ייצוא דף");
                    System.out.println("2. ייצוא משניות של פרק");
                    System.out.println("3. חזרה לתפריט הראשי");
                    
                    String exportChoice = scanner.nextLine();
                    
                    switch (exportChoice) {
                        case "1": // ייצוא דף
                            try {
                                System.out.print("שם מסכת: ");
                                String exportMasechet = scanner.nextLine();
                                System.out.print("מספר דף: ");
                                int exportDaf = Integer.parseInt(scanner.nextLine());
                                System.out.print("עמוד (א/ב): ");
                                char exportAmud = scanner.nextLine().charAt(0);
                                System.out.print("נתיב לשמירת הקובץ: ");
                                String exportPath = scanner.nextLine();
                                
                                if (exportPath.trim().isEmpty()) {
                                    exportPath = "C:\\temp\\" + exportMasechet + "_" + exportDaf + "_" + exportAmud + ".pdf";
                                }
                                
                                boolean success = HtmlExporter.exportPage(exportMasechet, exportDaf, exportAmud, exportPath);
                                System.out.println(success ? "הייצוא הושלם בהצלחה" : "שגיאה בייצוא");
                            } catch (Exception e) {
                                System.out.println("שגיאה: " + e.getMessage());
                            }
                        break;
                        
                        case "2": // ייצוא משניות
                            try {
                                System.out.print("שם מסכת: ");
                                String exportMishnaMasechet = scanner.nextLine();
                                System.out.print("מספר פרק: ");
                                int exportMishnaPerek = Integer.parseInt(scanner.nextLine());
                                System.out.print("נתיב לשמירת הקובץ: ");
                                String exportMishnaPath = scanner.nextLine();
                                
                                if (exportMishnaPath.trim().isEmpty()) {
                                    exportMishnaPath = "C:\\temp\\" + exportMishnaMasechet + "_perek_" + exportMishnaPerek + ".pdf";
                                }
                                
                                boolean success = HtmlExporter.exportMishnayot(exportMishnaMasechet, exportMishnaPerek, exportMishnaPath);
                                System.out.println(success ? "הייצוא הושלם בהצלחה" : "שגיאה בייצוא");
                            } catch (Exception e) {
                                System.out.println("שגיאה: " + e.getMessage());
                            }
                        break;
                        
                        case "3": // חזרה לתפריט ראשי
                        break;
                        
                        default:
                            System.out.println("אפשרות לא חוקית");
                    }
                break;
                
                case "13":
                    System.out.println("\n=== ניהול מטמון ===");
                    System.out.println("1. ניקוי מטמון");
                    System.out.println("2. הצגת מידע על המטמון");
                    System.out.println("3. הצגת פריטים פופולריים במטמון");
                    System.out.println("4. חזרה לתפריט הראשי");
                    
                    String cacheChoice = scanner.nextLine();
                    
                    switch (cacheChoice) {
                        case "1": // ניקוי מטמון
                            CacheManager.clear();
                            System.out.println("המטמון נוקה בהצלחה");
                        break;
                        
                        case "2": // הצגת מידע על המטמון
                            System.out.println("גודל נוכחי של המטמון: " + CacheManager.getSize() + " פריטים");
                            System.out.println("גודל מקסימלי של המטמון: " + CacheManager.getMaxSize() + " פריטים");
                            System.out.println("המטמון מנוהל כ-LinkedHashMap עם מדיניות LRU (העמוד שנגש אליו הכי פחות לאחרונה יורד)");
                        break;
                        
                        case "3": // פריטים פופולריים
                            List<String> popularItems = CacheManager.getPopularItems(5);
                            if (popularItems.isEmpty()) {
                                System.out.println("אין פריטים במטמון כרגע");
                            } else {
                                System.out.println("הפריטים הפופולריים ביותר במטמון:");
                                for (int i = 0; i < popularItems.size(); i++) {
                                    System.out.println((i + 1) + ". " + popularItems.get(i));
                                }
                            }
                        break;
                        
                        case "4": // חזרה לתפריט ראשי
                        break;
                        
                        default:
                            System.out.println("אפשרות לא חוקית");
                    }
                break;
                
                default:    
                    System.out.println("בחירה לא חוקית.");
            }
                System.out.print("\nלחץ Enter להמשך...");
                scanner.nextLine(); // Wait for user to press Enter
        }
    }
}
