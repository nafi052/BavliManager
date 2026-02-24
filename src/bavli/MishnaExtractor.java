package bavli;

import java.util.*;

/**
 * Extracts Mishna text from Talmud page content for a given tractate and chapter (perek).
 */
public class MishnaExtractor {

    /** Returns list of Mishna text blocks for the given tractate and chapter. */
    public static List<String> getMishnayotOfPerek(String masechet, int perek) {
        List<String> result = new ArrayList<>();
        
        // מציאת כל הדפים שיש בהם משניות מהפרק המבוקש
        List<Integer> relevantDafs = DataIndex.getDafsWithMishna(masechet, perek);
        System.out.println("נמצאו " + relevantDafs.size() + " דפים רלוונטיים במסכת " + masechet + " פרק " + perek);
        
        if (relevantDafs.isEmpty()) {
            return result;
        }
        
        // עבור על כל דף רלוונטי וחלץ את המשניות
        for (int daf : relevantDafs) {
            // נסה למצוא משניות בשני העמודים של כל דף (א' וב')
            for (char amud : new char[]{'א', 'ב'}) {
                String pageContent = FileManager.loadPage(masechet, daf, amud);
                if (pageContent != null && !pageContent.isEmpty()) {
                    List<String> pageResults = extractMishnayotFromText(pageContent, perek);
                    result.addAll(pageResults);
                }
            }
        }
        
        return result;
    }
    
   
    private static List<String> extractMishnayotFromText(String pageContent, int targetPerek) { // מחלץ משניות מטקסט
        List<String> mishnayot = new ArrayList<>();
        StringBuilder currentMishna = null;
        boolean inMishna = false;
        int currentPerek = -1;
        
        // פיצול הטקסט לשורות
        String[] lines = pageContent.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            
            // בדיקה אם השורה מציינת פרק חדש
            if (line.contains("פרק") && !line.contains("גמרא") && !line.contains("משנה")) {
                // ניסיון לחלץ את מספר הפרק
                int perekIndex = line.indexOf("פרק");
                if (perekIndex >= 0 && perekIndex + 4 < line.length()) {
                    String afterPerek = line.substring(perekIndex + 4).trim();
                    String[] parts = afterPerek.split("\\s+");
                    if (parts.length > 0) {
                        currentPerek = DataIndex.hebrewToNumber(parts[0]);
                        System.out.println("נמצא פרק " + currentPerek + " בטקסט");
                    }
                }
            }
            
            // כשמוצאים תחילת משנה
            if (line.startsWith("משנה") || line.startsWith("מתני'")) {
                if (inMishna && currentMishna != null && currentMishna.length() > 0) {
                    // שמירת המשנה הקודמת אם היא מהפרק המבוקש
                    if (targetPerek == -1 || currentPerek == targetPerek || currentPerek == -1) {
                        mishnayot.add(currentMishna.toString());
                    }
                }
                
                // התחלת משנה חדשה
                inMishna = true;
                currentMishna = new StringBuilder(line);
            }
            // כשמוצאים תחילת גמרא, מסיימים את המשנה האחרונה
            else if ((line.startsWith("גמרא") || line.startsWith("דף ") || line.startsWith("גמ'")) && inMishna) {
                if (currentMishna != null && currentMishna.length() > 0) {
                    // שמירת המשנה האחרונה אם היא מהפרק המבוקש
                    if (targetPerek == -1 || currentPerek == targetPerek || currentPerek == -1) {
                        mishnayot.add(currentMishna.toString());
                    }
                }
                inMishna = false;
            }
            // אם אנחנו בתוך משנה, נוסיף את השורה למשנה הנוכחית
            else if (inMishna) {
                if (currentMishna != null) {
                    currentMishna.append("\n").append(line);
                }
            }
        }
        
        // במקרה שהמשנה האחרונה לא הסתיימה בגמרא מפורשת
        if (inMishna && currentMishna != null && currentMishna.length() > 0) {
            if (targetPerek == -1 || currentPerek == targetPerek || currentPerek == -1) {
                mishnayot.add(currentMishna.toString());
            }
        }
        
        return mishnayot;
    }
}
