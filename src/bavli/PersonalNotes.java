package bavli;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Personal study notes per Talmud page. Notes are stored under {@code user_notes/}
 * with filenames derived from tractate, daf, and amud (side).
 */
public class PersonalNotes {
    private static final String NOTES_DIR = "user_notes/";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    static {
        // יצירת תיקיית ההערות בתחילת טעינת המחלקה אם לא קיימת
        try {
            Files.createDirectories(Paths.get(NOTES_DIR));
        } catch (IOException e) {
            System.err.println("שגיאה ביצירת תיקיית הערות: " + e.getMessage());
        }
    }
    
    /** Saves a note for the given page; overwrites existing. */
    public static void saveNote(String masechet, int daf, char amud, String note) throws IOException {
        String fileName = getNotesFileName(masechet, daf, amud);
        File noteFile = new File(fileName);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(noteFile))) {
            // הוספת חותמת זמן להערה
            writer.write("/* נערך ב: " + DATE_FORMAT.format(new Date()) + " */\n");
            writer.write(note);
        }
    }
    
    /**
     * טעינת הערה אישית לדף מסוים
     * 
     * @param masechet שם המסכת
     * @param daf מספר הדף
     * @param amud עמוד (א' או ב')
     * @return תוכן ההערה או מחרוזת ריקה אם אין הערה
     * @throws IOException במקרה של שגיאת קובץ
     */
    public static String loadNote(String masechet, int daf, char amud) throws IOException {
        String fileName = getNotesFileName(masechet, daf, amud);
        File noteFile = new File(fileName);
        
        if (!noteFile.exists()) {
            return "";
        }
        
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(noteFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
    
    /**
     * בדיקה אם קיימת הערה לדף מסוים
     * 
     * @param masechet שם המסכת
     * @param daf מספר הדף
     * @param amud עמוד (א' או ב')
     * @return האם קיימת הערה
     */
    public static boolean hasNote(String masechet, int daf, char amud) {
        String fileName = getNotesFileName(masechet, daf, amud);
        File noteFile = new File(fileName);
        return noteFile.exists() && noteFile.length() > 0;
    }
    
    /**
     * מחיקת הערה לדף מסוים
     * 
     * @param masechet שם המסכת
     * @param daf מספר הדף
     * @param amud עמוד (א' או ב')
     * @return האם המחיקה הצליחה
     */
    public static boolean deleteNote(String masechet, int daf, char amud) {
        String fileName = getNotesFileName(masechet, daf, amud);
        File noteFile = new File(fileName);
        return noteFile.delete();
    }
    
    /**
     * קבלת רשימה של כל ההערות הקיימות
     * 
     * @return מפה של נתיבי הערות וזמני יצירה/עדכון
     */
    public static Map<String, Date> getAllNotes() {
        Map<String, Date> notes = new HashMap<>();
        File notesDir = new File(NOTES_DIR);
        
        if (notesDir.exists() && notesDir.isDirectory()) {
            for (File file : notesDir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    notes.put(file.getName(), new Date(file.lastModified()));
                }
            }
        }
        
        return notes;
    }
    
    public static Map<String, Date> getNotesForMasechet(String masechet) { // קבלת רשימה של הערות למסכת מסוימת
        Map<String, Date> notes = new HashMap<>();
        File notesDir = new File(NOTES_DIR);
        
        if (notesDir.exists() && notesDir.isDirectory()) {
            String prefix = masechet.toLowerCase() + "_";
            for (File file : notesDir.listFiles()) {
                if (file.isFile() && file.getName().startsWith(prefix) && file.getName().endsWith(".txt")) {
                    notes.put(file.getName(), new Date(file.lastModified()));
                }
            }
        }
        
        return notes;
    }
    
    /**
     * פענוח שם קובץ להערות לקבלת מידע על הדף
     * 
     * @param fileName שם הקובץ (ללא נתיב)
     * @return מערך עם [מסכת, דף, עמוד] או null אם הפורמט לא תקין
     */
    public static String[] decodeNoteFileName(String fileName) {
        if (fileName == null || !fileName.endsWith(".txt")) {
            return null;
        }
        
        // הסרת סיומת .txt
        fileName = fileName.substring(0, fileName.length() - 4);
        
        String[] parts = fileName.split("_");
        if (parts.length < 3) {
            return null;
        }
        
        // חילוץ עמוד (תו אחרון)
        String amudStr = parts[parts.length - 1];
        
        // חילוץ מספר דף
        String dafStr = parts[parts.length - 2];
        
        // חילוץ שם מסכת (יכול להכיל מספר מילים עם קו תחתון)
        StringBuilder masechetBuilder = new StringBuilder();
        for (int i = 0; i < parts.length - 2; i++) {
            if (i > 0) {
                masechetBuilder.append("_");
            }
            masechetBuilder.append(parts[i]);
        }
        
        return new String[] { masechetBuilder.toString(), dafStr, amudStr };
    }
    
    private static String getNotesFileName(String masechet, int daf, char amud) { // בניית שם קובץ להערות לפי פרטי הדף
        String safeMasechet = masechet.trim().toLowerCase().replaceAll(" ", "_");
        String amudStr = Character.toString(amud);
        return NOTES_DIR + safeMasechet + "_" + daf + "_" + amudStr + ".txt";
    }
}