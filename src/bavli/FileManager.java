package bavli;

import java.io.*;

/**
 * File system operations: loading full text, splitting a main file into tractate/page structure,
 * and loading individual Talmud pages (with cache key usage).
 */
public class FileManager {

    /** Reads the entire file at {@code path} and returns its content as a string. */
    public static String loadFullText(String path) {
        StringBuilder content = new StringBuilder(); // יצירת מחרוזת ריקה לאחסון תוכן הטקסט
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) { // יצירת אובייקט קריאה לקובץ
            String line; // משתנה לאחסון שורות
            while ((line = reader.readLine()) != null) { // כל עוד יש שורות לקרוא
                content.append(line).append("\n"); // הוספת השורה למחרוזת התוכן
            }
        } catch (IOException e) { // טיפול בשגיאות קריאה
            System.out.println("שגיאה בקריאת קובץ: " + e.getMessage()); // הדפסת הודעת שגיאה
            return ""; // החזרת מחרוזת ריקה במקרה של שגיאה
        }
        return content.toString(); // החזרת התוכן המלא של הקובץ כמחרוזת
    }

    /** Splits the main Talmud text file into {@code pages/}&lt;tractate&gt;/&lt;daf&gt;/alef.txt and bet.txt. */
    public static void splitFile (String filePath) {
        BufferedReader reader = null; // קריאה לקובץ
        String currentMasechet = null; // מסכת נוכחית
        int currentDaf = -1; // דף נוכחי
        char currentAmud = ' '; // עמוד נוכחי
        StringBuilder pageContent = new StringBuilder(); // תוכן הדף הנוכחי

        try {
            reader = new BufferedReader(new FileReader(filePath)); // קריאה לקובץ
            String line; // שורת קלט

            while ((line = reader.readLine()) != null) { // קריאה שורה שורה
                 // דילוג על שורות ריקות מלכתחילה
                 if (line.trim().isEmpty()) {
                    continue; // דילוג על שורות ריקות
                }

                if (line.startsWith("מסכת ")) { // אם השורה מתחילה ב: מסכת
                    if (currentMasechet != null && pageContent.length() > 0) {  // אם יש תוכן לדף 
                        savePageToFile(currentMasechet, currentDaf, currentAmud, pageContent.toString()); // שמירת תיקייה חדשה בשם המסכת
                        pageContent.setLength(0); // איפוס משתנה תוכן הדף 
                    }

                    if (!line.contains("פרק")) { // בדיקה שהמילה "פרק" מופיעה בשורה
                        System.out.println("שגיאה: שורת מסכת לא תקינה, חסרה המילה 'פרק': " + line);
                        continue; // דלג על השורה הזו
                    }

                    String[] parts = line.split(" "); // פיצול השורה לפי רווחים
                    if (parts.length > 1) { // אם יש יותר ממילה אחת
                        StringBuilder masechetNameBuilder = new StringBuilder();
                    
                        // אוסף את שם המסכת (עד 2 מילים)
                        int i = 1; // מתחיל מהמילה אחרי מסכת
                        int wordCount = 0;

                        while (i < parts.length && wordCount < 2 && !parts[i].equals("פרק")) {
                            if (wordCount > 0) {
                                masechetNameBuilder.append(" ");
                            }
                            masechetNameBuilder.append(parts[i]);
                            i++;
                            wordCount++;
                        }

                        currentMasechet = masechetNameBuilder.toString().trim().replaceAll("[^א-תa-zA-Z0-9 ]", "").toLowerCase();
                    }
                    
                    // הוסף את שורת המסכת לתוכן הדף
                    pageContent.append(line).append("\n");
                }

                // אם זו שורת דף
                else if (line.startsWith("דף")) {
                    String[] parts = line.split(" ");
                    int newDaf = -1;
                    char newAmud = ' ';
                    try {
                        newDaf = DataIndex.hebrewToNumber(parts[1].trim());
                        newAmud = parts[2].trim().charAt(0);
                    } catch (Exception e) {
                        // parsing error, keep defaults
                    }
                    // only save & reset if switching to a different daf/amud
                    if ((newDaf != currentDaf || newAmud != currentAmud) && currentMasechet != null && pageContent.length() > 0) {
                        savePageToFile(currentMasechet, currentDaf, currentAmud, pageContent.toString());
                        pageContent.setLength(0);
                    }

                
                    currentDaf = newDaf; // עדכון הדף הנוכחי
                    currentAmud = (newAmud == 'א' || newAmud == 'ב') ? newAmud : 'א';
                    System.out.println(">>> דף: " + currentDaf + ", עמוד: " + currentAmud);
                    
                    // הוסף את שורת הדף לתוכן הדף
                    pageContent.append(line).append("\n");
                } else {
                    pageContent.append(line).append("\n");
                }
            }

            // שמירה אחרונה
            if (currentMasechet != null && pageContent.length() > 0) {
                savePageToFile(currentMasechet, currentDaf, currentAmud, pageContent.toString());
            }

            System.out.println("פיצול הסתיים");

        } catch (IOException e) {
            System.out.println("שגיאה בקריאה: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("שגיאה כללית: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                System.out.println("שגיאה בסגירה.");
            }
        }
    }

    private static void savePageToFile(String masechet, int daf, char amud, String content) throws IOException { // שמירת העמודים לקבצים
        
        if (daf < 2) { // אם הדף הוא 1 (א') או מספר שלילי, לא שומרים אותו בכלל
            return;
        }
        
        String amudName = (amud == 'א') ? "alef" : "bet"; 
        String dirPath = "pages/" + masechet + "/" + daf;
        File dir = new File(dirPath); // יצירת אובייקט תיקייה
        if (!dir.exists()) dir.mkdirs(); // יצירת התיקייה אם היא לא קיימת

        File file = new File(dirPath + "/" + amudName + ".txt"); // יצירת אובייקט קובץ
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) { // קריאה לכתיבה לקובץ
            writer.write(content); // כתיבת התוכן לקובץ
        }

        System.out.println("נשמר: " + file.getPath());
    }

    /** Loads a single page from {@code pages/}&lt;masechet&gt;/&lt;daf&gt;/alef.txt or bet.txt; uses cache. */
    public static String loadPage(String masechet, int daf, char amud) {
        // המרה של עמודים ממספר לאות (1->'א', 2->'ב')
        if (amud == '1') amud = 'א';
        else if (amud == '2') amud = 'ב';
        String amudName = (amud == 'א') ? "alef" : "bet"; // המרה של עמודים לאותיות
        String safeMasechet = masechet.replaceAll("[^א-תa-zA-Z0-9 ]", "").toLowerCase(); // שמירה על רווחים כדי לתמוך בשמות מסכת המורכבים משתי מילים
        String key = safeMasechet + "_" + daf + "_" + amudName; // יצירת מפתח ייחודי לדף
        String path = "pages/" + safeMasechet + "/" + daf + "/" + amudName + ".txt"; // יצירת נתיב לקובץ
        System.out.println(" טוען נתיב " + path); // הדפסה לצורך איתור בעיות

        if (CacheManager.contains(key)) { // בדיקה אם הדף כבר קיים במטמון
            return CacheManager.get(key); // החזרת התוכן מהמטמון
        }

        File file = new File(path); // יצירת אובייקט קובץ
        if (!file.exists()) { // בדיקה אם הקובץ לא קיים
            return "";
        }

        StringBuilder content = new StringBuilder(); // יצירת מחרוזת ריקה לאחסון תוכן הקובץ
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) { // קריאה לקובץ
            String line; // משתנה לאחסון שורות
            while ((line = reader.readLine()) != null) { // כל עוד יש שורות לקרוא
                content.append(line).append("\n"); // הוספת השורה למחרוזת התוכן
            }
        } catch (IOException e) {
            return "";
        }
        String result = content.toString(); // המרת התוכן למחרוזת 
        CacheManager.put(key, result); // שמירת התוכן במטמון
        return result; // החזרת התוכן המלא של הקובץ כמחרוזת
    }
}