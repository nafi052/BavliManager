package bavli;

import java.io.*;
import java.util.*;

/**
 * Index of tractates (מסכתות) and Mishnayot — maps tractate names to page numbers
 * and builds a per-tractate, per-page Mishna index. Not thread-safe; single-threaded use.
 */
public class DataIndex {
    static final Map <String, List <Integer>> masechtotIndex = new HashMap<>();
    static final Map <String, Map <Integer, PageInfo>> mishnaIndex = new HashMap<>();

    /** Loads tractate index from the {@code pages/} directory into memory. */
    public static void loadIndex() {
        
        File baseDir = new File("pages/"); // object with path of pages file
        if (!baseDir.exists()) {
            System.out.println("תיקיית pages לא קיימת.");
            return;
        }

        for (File masechetDir : baseDir.listFiles()) { // loop on object masechetDir thet includ all files in pages
            if (!masechetDir.isDirectory()) continue;
            String masechetName = masechetDir.getName(); 


            List<Integer> dafs = new ArrayList<>();   // dafs list: thet includ all dafs in each masechetDir
            
            for (File dafDir : masechetDir.listFiles()) { // dafDir loop thet includ all dafs files  
                if (!dafDir.isDirectory()) continue;

                try {
                    int dafNumber = Integer.parseInt(dafDir.getName());
                    dafs.add(dafNumber);                   // add each dafdir file to dafs list
                } catch (NumberFormatException e) {
                    System.out.println("דף לא חוקי: " + dafDir.getName());
                }
            }
            Collections.sort(dafs);                        // binary sort
            masechtotIndex.put(masechetName, dafs);        // put all masechetNames & dafs in masechtotIndex list
        }
    }

    /** @return list of all tractate names in the index */
    public static List<String> getAllMasechtot() {
        return new ArrayList<>(masechtotIndex.keySet());
    }

    /** @return list of page (daf) numbers for the given tractate */
    public static List<Integer> getDafsForMasechet(String masechet) {
        return masechtotIndex.getOrDefault(masechet.toLowerCase(), new ArrayList<>());
    }

    /** @return true if the tractate exists in the index */
    public static boolean masechetExists(String masechet) {
        return masechtotIndex.containsKey(masechet.toLowerCase());
    }

    /** @return true if the given page number exists in the tractate */
    public static boolean dafExists(String masechet, int daf) {
        List<Integer> dafs = masechtotIndex.get(masechet.toLowerCase());
        if (dafs == null) return false;
        return Collections.binarySearch(dafs, daf) >= 0;
    }

    /** Builds the Mishna index (tractate → page → chapter/Mishna info) from {@code pages/}. */
    public static void buildMishnaIndex() {
         
        File baseDir = new File("pages/");
        if (!baseDir.exists()) return;

        System.out.println("מתחיל לבנות אינדקס משניות...");

        for (File masechetDir : baseDir.listFiles()) {
            if (!masechetDir.isDirectory()) continue;
            
            String masechet = masechetDir.getName();
            Map<Integer, PageInfo> dafMap = new HashMap<>(); //
            Map<Integer, Integer> dafToPerekMap = new HashMap<>(); // אינדקס פרק לפי דף - לשימוש לזיהוי פרקים בהמשך

            System.out.println("מעבד מסכת: " + masechet);
            
            // שלב ראשון: מציאת מידע על פרקים ומשניות
            for (File dafDir : masechetDir.listFiles()) {
                if (!dafDir.isDirectory()) continue;
                
                try {
                    int dafNumber = Integer.parseInt(dafDir.getName());
                    boolean hasMishna = false;
                    int perek = -1;

                    // בדיקה של קבצי העמודים
                    File[] amudFiles = dafDir.listFiles();
                    if (amudFiles != null) {
                        for (File amudFile : amudFiles) {
                            if (!amudFile.isFile()) continue;
                            
                            try (BufferedReader reader = new BufferedReader(new FileReader(amudFile))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    // חיפוש משנה
                                    if (line.startsWith("משנה   ")) {
                                        hasMishna = true;
                                    }
                                    
                                    
                                    if (line.contains("פרק")) { // חיפוש פרק - בדיקת כמה פורמטים אפשריים
                                        // בדיקת שורה שמתחילה ב"מסכת" ומכילה "פרק"
                                        if (line.startsWith("מסכת") && line.contains("פרק")) { // בדיקת שורה שמתחילה ב"מסכת" ומכילה "פרק"
                                            int perekIndex = line.indexOf("פרק");
                                            if (perekIndex >= 0) {
                                                String remainingText = line.substring(perekIndex + 3).trim();
                                                String[] words = remainingText.split("\\s+");
                                                if (words.length > 0) {
                                                    int extractedPerek = hebrewToNumber(words[0].trim());
                                                    if (extractedPerek > 0) {
                                                        perek = extractedPerek;
                                                        System.out.println("נמצא פרק " + perek + " בדף " + dafNumber + " במסכת " + masechet);
                                                    }
                                                }
                                            }
                                        } 
                                        // בדיקת פורמט "פרק X"
                                        else {
                                            int perekIndex = line.indexOf("פרק");
                                            if (perekIndex >= 0 && perekIndex + 4 < line.length()) {
                                                String afterPerek = line.substring(perekIndex + 4).trim();
                                                String perekNum = afterPerek.split("\\s+")[0];
                                                int extractedPerek = hebrewToNumber(perekNum);
                                                if (extractedPerek > 0) {
                                                    perek = extractedPerek;
                                                    System.out.println("נמצא פרק " + perek + " בדף " + dafNumber + " במסכת " + masechet);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                System.out.println("שגיאה בקריאת קובץ: " + amudFile.getPath() + " - " + e.getMessage());
                            }
                        }
                    }

                    // שמירת מידע על פרק ומשנה לדף הנוכחי
                    if (hasMishna || perek > 0) {
                        dafMap.put(dafNumber, new PageInfo(hasMishna, perek));
                        
                        // אם מצאנו פרק, נשמור אותו במפת דף->פרק
                        if (perek > 0) {
                            dafToPerekMap.put(dafNumber, perek);
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("דף לא חוקי: " + dafDir.getName());
                }
            }
            
            // שלב שני: השלמת מידע פרק לדפים עם משניות ללא מידע פרק
            // מסדרים את הדפים לפי מספר עולה
            List<Integer> sortedDafs = new ArrayList<>(dafMap.keySet());
            Collections.sort(sortedDafs);
            
            int currentPerek = -1;
            for (int daf : sortedDafs) {
                PageInfo info = dafMap.get(daf);
                
                // אם יש מידע על פרק בדף הנוכחי, נעדכן את הפרק הנוכחי
                if (info.perek > 0) {
                    currentPerek = info.perek;
                } 
                // אם אין מידע על פרק אבל יש משנה, וכבר יודעים את הפרק הנוכחי, נשייך את המשנה לפרק הנוכחי
                else if (info.hasMishna && currentPerek > 0) {
                    info.perek = currentPerek;
                    System.out.println("שיוך משנה בדף " + daf + " לפרק " + currentPerek + " במסכת " + masechet);
                }
            }
            
            if (!dafMap.isEmpty()) {
                mishnaIndex.put(masechet, dafMap);
                System.out.println("נוספו " + dafMap.size() + " דפים עם מידע על משניות/פרקים למסכת " + masechet);
            }
        }
        
        System.out.println("בניית אינדקס משניות הסתיימה");
    }

    /** @return list of page numbers that contain Mishna for the given tractate and chapter (perek) */
    public static List<Integer> getDafsWithMishna(String masechet, int perek) {
        List<Integer> result = new ArrayList<>();
        Map<Integer, PageInfo> dafMap = mishnaIndex.get(masechet);
        
        System.out.println("מחפש דפים עם משניות במסכת " + masechet + " פרק " + perek);
        System.out.println("האם קיים מידע למסכת זו במפה: " + (dafMap != null));
        
        if (dafMap == null || dafMap.isEmpty()) { 
            // אם אין מידע במפה, ננסה לחפש בכל הדפים של המסכת
            List<Integer> allDafs = getDafsForMasechet(masechet);
            System.out.println("לא נמצא מידע על פרקים ומשניות, מחזיר את כל הדפים של המסכת: " + allDafs.size() + " דפים");
            return allDafs.isEmpty() ? result : allDafs.subList(0, Math.min(10, allDafs.size())); // מחזיר עד 10 דפים ראשונים
        }

        System.out.println("מספר דפים במפה: " + dafMap.size());
        // בדיקה כמה דפים יש עם משניות בכלל
        int countWithMishna = 0;
        for (PageInfo info : dafMap.values()) {
            if (info.hasMishna) countWithMishna++;
        }
        System.out.println("מספר דפים עם משניות: " + countWithMishna);
        
        // חיפוש דפים בפרק המבוקש
        for (Map.Entry<Integer, PageInfo> entry : dafMap.entrySet()) {
            PageInfo info = entry.getValue();
            // מחזירים דף אם יש בו משנה והוא מהפרק המבוקש או אם מספר הפרק לא ידוע (-1)
            if (info.hasMishna && (info.perek == perek || info.perek == -1)) {
                result.add(entry.getKey());
                System.out.println("נמצא דף " + entry.getKey() + " עם משנה" + 
                                   (info.perek == -1 ? " (פרק לא ידוע)" : " בפרק " + info.perek));
            }
        }
        
        // אם לא נמצאו דפים בפרק המבוקש, ננסה להחזיר את כל הדפים עם משניות
        if (result.isEmpty()) {
            System.out.println("לא נמצאו דפים בפרק המבוקש, מחפש כל דף עם משניות");
            for (Map.Entry<Integer, PageInfo> entry : dafMap.entrySet()) {
                if (entry.getValue().hasMishna) {
                    result.add(entry.getKey());
                }
            }
        }
        
        System.out.println("מחזיר " + result.size() + " דפים");
        return result;
    }
    private static class PageInfo { // מידע על דף האם קיים בו משנה ופרק
        boolean hasMishna;
        int perek;

        PageInfo(boolean hasMishna, int perek) {
            this.hasMishna = hasMishna;
            this.perek = perek;
        }
    }

    /** Converts Hebrew numeral letters to a decimal number. */
    public static int hebrewToNumber(String hebrew) {
        switch (hebrew.trim()) {
            case "א": return 1;         case "ב": return 2;         case "ג": return 3;     case "ד": return 4;
            case "ה": return 5;         case "ו": return 6;         case "ז": return 7;     case "ח": return 8;
            case "ט": return 9;         case "י": return 10;        case "יא": return 11;       case "יב": return 12;
            case "יג": return 13;       case "יד": return 14;       case "טו": return 15;       case "טז": return 16;
            case "יז": return 17;       case "יח": return 18;       case "יט": return 19;       case "כ": return 20;
            case "כא": return 21;       case "כב": return 22;       case "כג": return 23;       case "כד": return 24;
            case "כה": return 25;       case "כו": return 26;       case "כז": return 27;       case "כח": return 28;
            case "כט": return 29;       case "ל": return 30;        case "לא": return 31;       case "לב": return 32;
            case "לג": return 33;       case "לד": return 34;       case "לה": return 35;       case "לו": return 36;
            case "לז": return 37;       case "לח": return 38;       case "לט": return 39;       case "מ": return 40;
            case "מא": return 41;       case "מב": return 42;       case "מג": return 43;       case "מד": return 44;
            case "מה": return 45;       case "מו": return 46;       case "מז": return 47;       case "מח": return 48;
            case "מט": return 49;       case "נ": return 50;        case "נא": return 51;       case "נב": return 52;
            case "נג": return 53;       case "נד": return 54;       case "נה": return 55;       case "נו": return 56;
            case "נז": return 57;       case "נח": return 58;       case "נט": return 59;       case "ס": return 60;
            case "סא": return 61;       case "סב": return 62;       case "סג": return 63;       case "סד": return 64;
            case "סה": return 65;       case "סו": return 66;       case "סז": return 67;       case "סח": return 68;
            case "סט": return 69;       case "ע": return 70;        case "עא": return 71;       case "עב": return 72;
            case "עג": return 73;       case "עד": return 74;       case "עה": return 75;       case "עו": return 76;
            case "עז": return 77;       case "עח": return 78;       case "עט": return 79;       case "פ": return 80;
            case "פא": return 81;       case "פב": return 82;       case "פג": return 83;       case "פד": return 84;
            case "פה": return 85;       case "פו": return 86;       case "פז": return 87;       case "פח": return 88;
            case "פט": return 89;       case "צ": return 90;        case "צא": return 91;       case "צב": return 92;
            case "צג": return 93;       case "צד": return 94;       case "צה": return 95;       case "צו": return 96;
            case "צז": return 97;       case "צח": return 98;       case "צט": return 99;       case "ק": return 100;
            case "קא": return 101;      case "קב": return 102;      case "קג": return 103;      case "קד": return 104;
            case "קה": return 105;      case "קו": return 106;      case "קז": return 107;      case "קח": return 108;
            case "קט": return 109;      case "קי": return 110;      case "קיא": return 111;     case "קיב": return 112;
            case "קיג": return 113;     case "קיד": return 114;     case "קטו": return 115;     case "קטז": return 116;
            case "קיז": return 117;     case "קיח": return 118;     case "קיט": return 119;     case "קכ": return 120;
            case "קכא": return 121;     case "קכב": return 122;     case "קכג": return 123;     case "קכד": return 124;
            case "קכה": return 125;     case "קכו": return 126;     case "קכז": return 127;     case "קכח": return 128;
            case "קכט": return 129;     case "קל": return 130;      case "קלא": return 131;     case "קלב": return 132;
            case "קלג": return 133;     case "קלד": return 134;     case "קלה": return 135;     case "קלו": return 136;
            case "קלז": return 137;     case "קלח": return 138;     case "קלט": return 139;     case "קמ": return 140;
            case "קמא": return 141;     case "קמב": return 142;     case "קמג": return 143;     case "קמד": return 144;
            case "קמה": return 145;     case "קמו": return 146;     case "קמז": return 147;     case "קמח": return 148;
            case "קמט": return 149;     case "קנ": return 150;      case "קנא": return 151;     case "קנב": return 152;
            case "קנג": return 153;     case "קנד": return 154;     case "קנה": return 155;     case "קנו": return 156;
            case "קנז": return 157;     case "קנח": return 158;     case "קנט": return 159;     case "קס": return 160;
            case "קסא": return 161;     case "קסב": return 162;     case "קסג": return 163;     case "קסד": return 164;
            case "קסה": return 165;     case "קסו": return 166;     case "קסז": return 167;     case "קסח": return 168;
            case "קסט": return 169;     case "קצ": return 170;      case "קצא": return 171;     case "קצב": return 172;
            case "קצג": return 173;     case "קצד": return 174;     case "קצה": return 175;     case "קצו": return 176;
            default: return 0;
        }
    }
}