package bavli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-process test suite for file system, index, search, Mishna extraction, cache, and Hebrew numbers.
 * Run via {@link #main} (e.g. {@code mvn verify}) or from menu option 8.
 */
public class Tests {

    private static int passedTests = 0;
    private static int failedTests = 0;
    private static final Map<String, List<String>> testFailures = new HashMap<>();

    /** Runs all test groups and prints summary; exits with code 1 if any test failed. */
    public static void runAllTests() {
        
        // איפוס מונה הבדיקות
        passedTests = 0;
        failedTests = 0;
        testFailures.clear();
        
        System.out.println("=== מתחיל הרצת מערך בדיקות מקיף ===");
        
        runFileSystemTests();  // בדיקות של מערכת הקבצים
        
        runIndexTests();  // בדיקות של מערכת האינדקס
        
        runSearchTests();  // בדיקות של מערכת החיפוש
        
        runMishnaExtractionTests(); // בדיקות של מערכת חילוץ המשניות
        
        runCacheTests(); // בדיקות של מערכת המטמון
        
        runHebrewNumberTests(); // בדיקות המרת מספרים עבריים
        
        printTestsSummary(); // הצג סיכום של תוצאות הבדיקות
    }
    
    private static void runFileSystemTests() { // בדיקות של מערכת הקבצים
        startTestGroup("בדיקות מערכת קבצים");
        
        assertTrue("בדיקת קיום תיקיית pages", // בדיקת קיום תיקיית pages
                new File("pages").exists() && new File("pages").isDirectory()); 
        
        File pagesDir = new File("pages"); // בדיקת קיום מסכתות בתיקיית pages
        File[] masechtot = pagesDir.listFiles(File::isDirectory);
        assertTrue("בדיקת קיום מסכתות בתיקיית pages", 
                masechtot != null && masechtot.length > 0);
        
        String pageContent = loadAnyExistingPage(); // בדיקת טעינת דף קיים בפועל במבנה הנתונים
        assertTrue("בדיקת טעינת דף קיים", 
            pageContent != null && !pageContent.isEmpty());
        
        pageContent = FileManager.loadPage("מסכת_לא_קיימת", 999, 'א');  // בדיקת טעינת דף לא קיים
        assertTrue("בדיקת טעינת דף לא קיים", 
                pageContent.isEmpty());
                
        
        try { // בדיקה שאפשר לטעון את הטקסט המלא (פונקציה קיימת אבל רק אם הקובץ קיים)
            File tempFile = File.createTempFile("bavli_test", ".txt");
            tempFile.deleteOnExit();
            Files.write(tempFile.toPath(), "תוכן טסט".getBytes());
            
            String content = FileManager.loadFullText(tempFile.getAbsolutePath());
            assertTrue("בדיקת loadFullText עם קובץ קיים", 
                    content != null && content.contains("תוכן טסט"));
        } catch (IOException e) {
            fail("בדיקת loadFullText - שגיאת IO: " + e.getMessage());
        }
    }
    
    private static void runIndexTests() { // בדיקות של מערכת האינדקס
        startTestGroup("בדיקות מערכת האינדקס");
        
     
        DataIndex.loadIndex(); // טעינת האינדקס
        
        List<String> masechtot = DataIndex.getAllMasechtot(); // בדיקת קיום מסכתות באינדקס
        assertTrue("בדיקת קיום מסכתות באינדקס", 
                masechtot != null && !masechtot.isEmpty());
        
        boolean exists = DataIndex.masechetExists("ברכות"); // בדיקת קיום מסכת ברכות באינדקס
        assertTrue("בדיקת קיום מסכת ברכות", exists);
        
        exists = false;
        if (DataIndex.masechetExists("ברכות")) { // בדיקת קיום דף במסכת קיימת
            exists = DataIndex.dafExists("ברכות", 2);
        }
        assertTrue("בדיקת קיום דף 2 במסכת ברכות", exists);
        
        // בדיקת החזרת דפים במסכת
        List<Integer> dafs = DataIndex.getDafsForMasechet("ברכות");
        assertTrue("בדיקת החזרת דפים במסכת ברכות", 
                dafs != null && !dafs.isEmpty());
        
        // בדיקת בניית אינדקס משניות
        DataIndex.buildMishnaIndex();
    }
    
    /**
     * בדיקות של מערכת החיפוש
     */
    private static void runSearchTests() {
        startTestGroup("בדיקות מערכת החיפוש");
        
        // בדיקת חיפוש ציטוט 
        String testQuote = "משנה";  // מילה שאמורה להימצא בטקסטים רבים
        List<String> results = SearchEngine.searchByQuote(testQuote);
        assertTrue("בדיקת חיפוש ציטוט נפוץ", 
                results != null);
                
        // בדיקת חיפוש ציטוט שלא אמור להימצא (הממשק מחזיר הודעה "הציטוט לא נמצא.")
        String randomQuote = "גחךצקכעיףחל" + System.currentTimeMillis(); // ציטוט רנדומלי שלא אמור להימצא
        results = SearchEngine.searchByQuote(randomQuote);
        assertTrue("בדיקת חיפוש ציטוט שלא אמור להימצא",
                results != null && (results.isEmpty() || results.stream().anyMatch(s -> s.contains("לא נמצא"))));
    }
    
    /**
     * בדיקות של מערכת חילוץ המשניות
     */
    private static void runMishnaExtractionTests() {
        startTestGroup("בדיקות מערכת חילוץ המשניות");
        
        // בדיקת חילוץ משניות ממסכת וממספר פרק
        List<String> mishnayot = MishnaExtractor.getMishnayotOfPerek("ברכות", 1);
        // אין צורך שתהיה תוצאה, רק שלא תיזרק שגיאה
        assertTrue("בדיקת חילוץ משניות מפרק 1 במסכת ברכות", 
                mishnayot != null);
                
        // בדיקת חילוץ משניות ממסכת וממספר פרק שלא קיים
        mishnayot = MishnaExtractor.getMishnayotOfPerek("מסכת_לא_קיימת", 999);
        assertTrue("בדיקת חילוץ משניות ממסכת לא קיימת", 
                mishnayot != null && mishnayot.isEmpty());
    }
    
    /**
     * בדיקות של מערכת המטמון
     */
    private static void runCacheTests() {
        startTestGroup("בדיקות מערכת המטמון");
        
        // ניקוי המטמון לפני הבדיקות
        CacheManager.clear();
        
        // בדיקת הוספת ערך למטמון
        String testKey = "test_key_" + System.currentTimeMillis();
        String testValue = "test_value_" + System.currentTimeMillis();
        
        CacheManager.put(testKey, testValue);
        assertTrue("בדיקת הוספת ערך למטמון", 
                CacheManager.contains(testKey));
                
        // בדיקת שליפת ערך מהמטמון
        String retrievedValue = CacheManager.get(testKey);
        assertEquals("בדיקת שליפת ערך מהמטמון", 
                testValue, retrievedValue);
                
        // בדיקת מחיקת ערך מהמטמון
        CacheManager.remove(testKey);
        assertTrue("בדיקת מחיקת ערך מהמטמון", 
                !CacheManager.contains(testKey));
    }
    
    /**
     * בדיקות של המרת מספרים עבריים
     */
    private static void runHebrewNumberTests() {
        startTestGroup("בדיקות המרת מספרים עבריים");
        
        // בדיקת המרת אותיות עבריות למספרים
        assertEquals("בדיקת המרת א למספר 1", 
                1, DataIndex.hebrewToNumber("א"));
                
        assertEquals("בדיקת המרת י למספר 10", 
                10, DataIndex.hebrewToNumber("י"));
                
        assertEquals("בדיקת המרת צט למספר 99", 
                99, DataIndex.hebrewToNumber("צט"));
                
        assertEquals("בדיקת המרת ק למספר 100", 
                100, DataIndex.hebrewToNumber("ק"));
                
        // בדיקת ערך לא תקין
        assertEquals("בדיקת המרת ערך לא תקין", 
                0, DataIndex.hebrewToNumber("ערךלאתקין"));
    }
    
    /**
     * בדיקות נוספות לדוגמה
     */
    private static void runAdditionalTests() {
        startTestGroup("בדיקות נוספות");
        
        // בדיקת assert לדוגמה עם מספרים
        assertEquals("בדיקת שוויון מספרים", 5, 2 + 3);
        
        // בדיקת assert עם מחרוזות
        assertEquals("בדיקת שוויון מחרוזות", "שלום", "שלום");
        
        // בדיקת assert עם בוליאנים
        assertTrue("בדיקת אמת", true);
        assertFalse("בדיקת שקר", false);
        
        // בדיקת assert עם מערכים
        int[] array1 = {1, 2, 3};
        int[] array2 = {1, 2, 3};
        assertArrayEquals("בדיקת שוויון מערכים", array1, array2);
        
        // בדיקת assert לדוגמה עם null
        Object obj = null;
        assertNull("בדיקת ערך null", obj);
        
        // בדיקת assert לדוגמה עם לא-null
        obj = new Object();
        assertNotNull("בדיקת ערך לא null", obj);
    }

    private static String loadAnyExistingPage() {
        File pagesDir = new File("pages");
        File[] masechtot = pagesDir.listFiles(File::isDirectory);
        if (masechtot == null) {
            return "";
        }

        for (File masechetDir : masechtot) {
            File[] dafDirs = masechetDir.listFiles(File::isDirectory);
            if (dafDirs == null) {
                continue;
            }

            for (File dafDir : dafDirs) {
                int daf;
                try {
                    daf = Integer.parseInt(dafDir.getName());
                } catch (NumberFormatException e) {
                    continue;
                }

                String alef = FileManager.loadPage(masechetDir.getName(), daf, 'א');
                if (alef != null && !alef.isEmpty()) {
                    return alef;
                }

                String bet = FileManager.loadPage(masechetDir.getName(), daf, 'ב');
                if (bet != null && !bet.isEmpty()) {
                    return bet;
                }
            }
        }

        return "";
    }
    
    /* שיטות עזר לטסטים */
    
    private static void startTestGroup(String groupName) {
        System.out.println("\n=== " + groupName + " ===");
    }
    
    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            passedTest(testName);
        } else {
            failTest(testName, "ערך לא נכון. נדרש: true, התקבל: false");
        }
    }
    
    private static void assertFalse(String testName, boolean condition) {
        if (!condition) {
            passedTest(testName);
        } else {
            failTest(testName, "ערך לא נכון. נדרש: false, התקבל: true");
        }
    }
    
    private static void assertEquals(String testName, Object expected, Object actual) {
        if (expected == null && actual == null) {
            passedTest(testName);
        } else if (expected != null && expected.equals(actual)) {
            passedTest(testName);
        } else {
            failTest(testName, "ערכים לא שווים. נדרש: " + expected + ", התקבל: " + actual);
        }
    }
    
    private static void assertEquals(String testName, int expected, int actual) {
        if (expected == actual) {
            passedTest(testName);
        } else {
            failTest(testName, "ערכים לא שווים. נדרש: " + expected + ", התקבל: " + actual);
        }
    }
    
    private static void assertNull(String testName, Object obj) {
        if (obj == null) {
            passedTest(testName);
        } else {
            failTest(testName, "ערך לא null. נדרש: null, התקבל: " + obj);
        }
    }
    
    private static void assertNotNull(String testName, Object obj) {
        if (obj != null) {
            passedTest(testName);
        } else {
            failTest(testName, "ערך null. נדרש: לא null");
        }
    }
    
    private static void assertArrayEquals(String testName, int[] expected, int[] actual) {
        if (Arrays.equals(expected, actual)) {
            passedTest(testName);
        } else {
            failTest(testName, "מערכים לא שווים. נדרש: " + Arrays.toString(expected) + 
                    ", התקבל: " + Arrays.toString(actual));
        }
    }
    
    private static void fail(String testName) {
        failTest(testName, "הבדיקה נכשלה");
    }
    
    private static void fail(String testName, String reason) {
        failTest(testName, reason);
    }
    
    private static void passedTest(String testName) {
        passedTests++;
        System.out.println("✓ עבר: " + testName);
    }
    
    private static void failTest(String testName, String reason) {
        failedTests++;
        System.out.println("✗ נכשל: " + testName + " - " + reason);
        
        // שמירת פרטי הכישלון להצגה בסיכום
        testFailures.computeIfAbsent(testName, k -> new ArrayList<>()).add(reason);
    }
    
    private static void printTestsSummary() {
        System.out.println("\n=== סיכום בדיקות ===");
        System.out.println("סך הכל בדיקות: " + (passedTests + failedTests));
        System.out.println("עברו בהצלחה: " + passedTests);
        System.out.println("נכשלו: " + failedTests);
        
        if (!testFailures.isEmpty()) {
            System.out.println("\n=== פירוט כישלונות ===");
            for (Map.Entry<String, List<String>> entry : testFailures.entrySet()) {
                System.out.println("בדיקה: " + entry.getKey());
                for (String reason : entry.getValue()) {
                    System.out.println("  - " + reason);
                }
            }
        }
        
        if (failedTests == 0) {
            System.out.println("\nכל הבדיקות עברו בהצלחה! 🎉");
        }

        if (failedTests > 0) {
            System.exit(1);
        }
    }

    /**
     * Entry point for running tests from Maven/CLI (e.g. mvn verify).
     * Exits with code 1 if any test failed.
     */
    public static void main(String[] args) {
        runAllTests();
    }
}