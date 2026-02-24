package bavli;

import java.io.*;
import java.util.*;

/**
 * Full-text and fuzzy search over Talmud pages under {@code pages/}.
 * Supports exact and Levenshtein-based approximate matching.
 */
public class SearchEngine {

    /** Searches all pages for the given quote (exact match). */
    public static List<String> searchByQuote(String quote) {
        return searchByQuote(quote, false, 0);
    }
    
    /**
     * חיפוש ציטוט מתקדם עם אפשרות לחיפוש מטושטש
     * 
     * @param quote הציטוט לחיפוש
     * @param fuzzySearch האם להשתמש בחיפוש מטושטש (עם סבילות לשגיאות)
     * @param maxDistance המרחק המקסימלי המותר בחיפוש מטושטש
     * @return רשימת תוצאות החיפוש
     */
    public static List<String> searchByQuote(String quote, boolean fuzzySearch, int maxDistance) {
        List<String> results = new ArrayList<>();
        Map<String, List<String>> foundLocations = new HashMap<>(); // לאחסון מיקומים מדויקים של תוצאות
        
        File baseDir = new File("pages/");
        if (!baseDir.exists()) {
            results.add("לא נמצאה תיקיית הדפים.");
            return results;
        }

        // הכנסת חיפוש במקביל עם Thread Pool
        List<Thread> searchThreads = new ArrayList<>();
        final Object lock = new Object();

        for (File masechetDir : baseDir.listFiles()) {
            if (!masechetDir.isDirectory()) continue;
            
            Thread thread = new Thread(() -> {
                String masechet = masechetDir.getName();
                List<String> masechetResults = searchInMasechet(masechet, quote, fuzzySearch, maxDistance);
                
                synchronized (lock) {
                    if (!masechetResults.isEmpty()) {
                        foundLocations.put(masechet, masechetResults);
                    }
                }
            });
            
            searchThreads.add(thread);
            thread.start();
        }

        // המתנה לסיום כל התהליכונים
        for (Thread thread : searchThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("החיפוש הופרע: " + e.getMessage());
            }
        }

        // מיון התוצאות לפי מסכת
        List<String> sortedMasechtot = new ArrayList<>(foundLocations.keySet());
        Collections.sort(sortedMasechtot);
        
        for (String masechet : sortedMasechtot) {
            results.add("== מסכת " + masechet + " ==");
            results.addAll(foundLocations.get(masechet));
        }

        if (results.isEmpty()) {
            results.add("הציטוט לא נמצא.");
        } else {
            results.add(0, "מספר התוצאות שנמצאו: " + 
                        (results.size() - sortedMasechtot.size())); // מספר התוצאות האמיתי (פחות כותרות)
        }
        
        return results;
    }
    
    private static List<String> searchInMasechet(String masechet, String quote, boolean fuzzySearch, int maxDistance) {
        List<String> results = new ArrayList<>();
        File masechetDir = new File("pages/" + masechet);
        
        for (File dafDir : masechetDir.listFiles()) {
            if (!dafDir.isDirectory()) continue;
            
            String daf = dafDir.getName();
            
            for (File amudFile : dafDir.listFiles()) {
                if (!amudFile.isFile()) continue;
                
                String amud = amudFile.getName().replace(".txt", "");
                
                try (BufferedReader reader = new BufferedReader(new FileReader(amudFile))) {
                    String line;
                    int lineNumber = 0;
                    
                    while ((line = reader.readLine()) != null) {
                        lineNumber++;
                        
                        boolean found = false;
                        
                        if (fuzzySearch) {
                            // חיפוש מטושטש עם סבילות לשגיאות
                            found = fuzzyContains(line, quote, maxDistance);
                        } else {
                            // חיפוש רגיל
                            found = line.contains(quote);
                        }
                        
                        if (found) {
                            String hebrewAmud = amud.equals("alef") ? "א" : "ב"; 
                            String resultLine = "נמצא במסכת " + masechet + " דף " + daf + 
                                        " עמוד " + hebrewAmud + " (שורה " + lineNumber + "): " + 
                                        highlightMatch(line, quote, fuzzySearch);
                            results.add(resultLine);
                            break; // לא צריך לקרוא את כל הקובץ אם כבר נמצא
                        }
                    }
                } catch (IOException e) {
                    System.out.println("שגיאה בקריאת קובץ: " + amudFile.getPath() + " - " + e.getMessage());
                }
            }
        }
        
        return results;
    }
    
    /**
     * בודק האם מחרוזת כוללת תת-מחרוזת אחרת עם סבילות לשגיאות
     * 
     * @param text הטקסט הראשי
     * @param pattern התבנית לחיפוש
     * @param maxDistance מספר השגיאות המקסימלי המותר
     * @return האם נמצאה התאמה
     */
    private static boolean fuzzyContains(String text, String pattern, int maxDistance) {
        if (text == null || pattern == null) return false;
        if (pattern.isEmpty()) return true;
        if (text.isEmpty()) return false;
        
        // בדיקה מהירה תחילה - האם יש התאמה מדויקת
        if (text.contains(pattern)) return true;
        
        // בדיקה עם אלגוריתם לוונשטיין
        int n = text.length();
        int m = pattern.length();
        
        // עבור כל עמדה אפשרית בטקסט
        for (int i = 0; i <= n - m + maxDistance; i++) {
            int end = Math.min(i + m + maxDistance, n);
            String subText = text.substring(i, end);
            
            if (levenshteinDistance(subText, pattern) <= maxDistance) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * חישוב מרחק לוונשטיין בין שתי מחרוזות
     * 
     * @param s1 המחרוזת הראשונה
     * @param s2 המחרוזת השנייה
     * @return מרחק העריכה המינימלי
     */
    public static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        dp[i-1][j-1] + (s1.charAt(i-1) == s2.charAt(j-1) ? 0 : 1), 
                        Math.min(dp[i-1][j] + 1, dp[i][j-1] + 1)
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * מדגיש את ההתאמה בטקסט
     * 
     * @param text הטקסט המלא
     * @param pattern התבנית שיש להדגיש
     * @param fuzzy האם זה חיפוש מטושטש
     * @return טקסט עם הדגשה
     */
    private static String highlightMatch(String text, String pattern, boolean fuzzy) {
        if (!fuzzy && text.contains(pattern)) {
            int startPos = text.indexOf(pattern);
            int endPos = startPos + pattern.length();
            
            // חותכים את הטקסט לפני ואחרי ההתאמה, ומוסיפים הדגשה באמצעות הסוגריים { }
            return text.substring(Math.max(0, startPos - 20), startPos) + 
                   "{" + pattern + "}" + 
                   text.substring(endPos, Math.min(text.length(), endPos + 20));
        }
        
        // במקרה של חיפוש מטושטש או אם לא נמצאה התאמה מדויקת, מחזירים חתיכה מהטקסט
        int maxLength = Math.min(60, text.length());
        return text.substring(0, maxLength) + (text.length() > maxLength ? "..." : "");
    }
    
    /**
     * חיפוש לפי מסכת ופרק מסוימים
     */
    public static List<String> searchInSpecificMasechet(String masechet, int startDaf, int endDaf, String quote) {
        List<String> results = new ArrayList<>();
        File masechetDir = new File("pages/" + masechet);
        
        if (!masechetDir.exists() || !masechetDir.isDirectory()) {
            results.add("מסכת " + masechet + " לא נמצאה.");
            return results;
        }
        
        for (File dafDir : masechetDir.listFiles()) {
            if (!dafDir.isDirectory()) continue;
            
            try {
                int dafNum = Integer.parseInt(dafDir.getName());
                if (dafNum < startDaf || dafNum > endDaf) continue;
                
                for (File amudFile : dafDir.listFiles()) {
                    if (!amudFile.isFile()) continue;
                    String amud = amudFile.getName().replace(".txt", "");
                    
                    try (BufferedReader reader = new BufferedReader(new FileReader(amudFile))) {
                        String line;
                        int lineNumber = 0;
                        
                        while ((line = reader.readLine()) != null) {
                            lineNumber++;
                            
                            if (line.contains(quote)) {
                                String hebrewAmud = amud.equals("alef") ? "א" : "ב"; 
                                results.add("נמצא במסכת " + masechet + " דף " + dafNum + 
                                           " עמוד " + hebrewAmud + " (שורה " + lineNumber + "): " + 
                                           highlightMatch(line, quote, false));
                            }
                        }
                    } catch (IOException e) {
                        results.add("שגיאה בקריאת קובץ: " + amudFile.getPath());
                    }
                }
            } catch (NumberFormatException e) {
                // דילוג על תיקיות שאינן מספרי דפים
            }
        }
        
        if (results.isEmpty()) {
            results.add("הציטוט לא נמצא במסכת " + masechet + " בדפים " + startDaf + "-" + endDaf);
        }
        
        return results;
    }
}
