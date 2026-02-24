package bavli;

import java.io.*;
import java.util.List;
import java.awt.print.*;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Exports Talmud pages and Mishnayot to HTML files (and optional print).
 */
public class HtmlExporter {

    /** Exports a single page to an HTML file at {@code outputPath}. */
    public static boolean exportPage(String masechet, int daf, char amud, String outputPath) {
        String content = FileManager.loadPage(masechet, daf, amud);
        if (content.isEmpty()) {
            return false;
        }
        
        return exportToHtml(masechet, daf, amud, outputPath);
    }
    
    /**
     * מייצא רשימת משניות לקובץ HTML
     * 
     * @param masechet שם המסכת
     * @param perek מספר הפרק
     * @param outputPath נתיב קובץ הפלט
     * @return האם הייצוא הצליח
     */
    public static boolean exportMishnayot(String masechet, int perek, String outputPath) {
        List<String> mishnayot = MishnaExtractor.getMishnayotOfPerek(masechet, perek);
        if (mishnayot.isEmpty()) {
            return false;
        }
        
        // בדיקה שהנתיב מסתיים ב-.html
        if (!outputPath.toLowerCase().endsWith(".html")) {
            outputPath += ".html";
        }
        
        try {
            // יצירת תיקיית הורה אם לא קיימת
            File parentDir = new File(outputPath).getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            System.out.println("מייצא משניות לקובץ HTML: " + outputPath);
            
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html dir=\"rtl\">\n");
            html.append("<head>\n");
            html.append("  <meta charset=\"UTF-8\">\n");
            html.append("  <title>מסכת ").append(masechet).append(" פרק ").append(perek).append("</title>\n");
            html.append("  <style>\n");
            html.append("    body { font-family: 'David', 'Times New Roman', serif; direction: rtl; padding: 20px; }\n");
            html.append("    h1, h2 { text-align: center; }\n");
            html.append("    .mishna { margin-bottom: 20px; border-bottom: 1px solid #ccc; padding-bottom: 10px; }\n");
            html.append("    .mishna-title { font-weight: bold; margin-bottom: 10px; }\n");
            html.append("    .mishna-content { line-height: 1.6; white-space: pre-wrap; }\n");
            html.append("    .footer { margin-top: 30px; font-size: 0.8em; text-align: center; }\n");
            html.append("  </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("  <h1>מסכת ").append(masechet).append("</h1>\n");
            html.append("  <h2>פרק ").append(perek).append("</h2>\n");
            
            // הוספת כל המשניות
            for (int i = 0; i < mishnayot.size(); i++) {
                html.append("  <div class=\"mishna\">\n");
                html.append("    <div class=\"mishna-title\">משנה ").append(i + 1).append("</div>\n");
                html.append("    <div class=\"mishna-content\">\n");
                html.append("      ").append(mishnayot.get(i).replace("&", "&amp;")
                                              .replace("<", "&lt;")
                                              .replace(">", "&gt;")
                                              .replace("\n", "<br>\n      "));
                html.append("\n    </div>\n");
                html.append("  </div>\n");
            }
            
            html.append("  <div class=\"footer\">\n");
            html.append("    יוצא באמצעות מערכת BavliManager - ").append(new java.util.Date()).append("\n");
            html.append("  </div>\n");
            html.append("</body>\n");
            html.append("</html>");
            
            // כתיבה לקובץ
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputPath), "UTF-8"))) {
                writer.write(html.toString());
            }
            
            System.out.println("ייצוא המשניות הסתיים בהצלחה");
            return true;
            
        } catch (Exception e) {
            System.err.println("שגיאה בייצוא המשניות לקובץ HTML: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * יצירת הדפסה של דף או משניות
     * 
     * @param text הטקסט להדפסה
     * @return האם הפקודה נשלחה למדפסת
     */
    public static boolean printText(String text) {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            
            // הגדרת המסמך להדפסה
            job.setPrintable(new Printable() {
                @Override
                public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
                    if (page > 0) {
                        return NO_SUCH_PAGE;
                    }
                    
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.translate(pf.getImageableX(), pf.getImageableY());
                    
                    // הגדרת פונט לעברית
                    g2d.setFont(new Font("David", Font.PLAIN, 12));
                    
                    // פיצול הטקסט לשורות
                    String[] lines = text.split("\n");
                    int y = 50;
                    
                    for (String line : lines) {
                        g2d.drawString(line, 50, y);
                        y += 15;
                    }
                    
                    return PAGE_EXISTS;
                }
            });
            
            // הצגת דיאלוג הדפסה והדפסה
            if (job.printDialog()) {
                job.print();
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("שגיאה בהדפסה: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * ייצוא מסמך HTML מורחב עם מידע נוסף
     * 
     * @param masechet שם המסכת
     * @param daf מספר הדף
     * @param amud עמוד (א' או ב')
     * @param includeNotes האם לכלול הערות אישיות
     * @param outputPath נתיב קובץ הפלט
     * @return האם הייצוא הצליח
     */
    public static boolean exportEnhancedPage(String masechet, int daf, char amud, boolean includeNotes, String outputPath) {
        String content = FileManager.loadPage(masechet, daf, amud);
        if (content.isEmpty()) {
            return false;
        }
        
        // בדיקה שהנתיב מסתיים ב-.html
        if (!outputPath.toLowerCase().endsWith(".html")) {
            outputPath += ".html";
        }
        
        try {
            // יצירת תיקיית הורה אם לא קיימת
            File parentDir = new File(outputPath).getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            System.out.println("מייצא מסמך מורחב לקובץ HTML: " + outputPath);
            
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html dir=\"rtl\">\n");
            html.append("<head>\n");
            html.append("  <meta charset=\"UTF-8\">\n");
            html.append("  <title>").append(masechet).append(" דף ").append(daf).append(" עמוד ").append(amud).append("</title>\n");
            html.append("  <style>\n");
            html.append("    body { font-family: 'David', 'Times New Roman', serif; direction: rtl; padding: 20px; }\n");
            html.append("    h1, h2 { text-align: center; }\n");
            html.append("    .page-info { margin-bottom: 20px; font-weight: bold; }\n");
            html.append("    .content-section { margin-top: 20px; border-top: 1px solid #ccc; padding-top: 10px; }\n");
            html.append("    .page-content { line-height: 1.6; white-space: pre-wrap; }\n");
            html.append("    .notes-section { margin-top: 30px; border-top: 1px solid #ccc; padding-top: 10px; }\n");
            html.append("    .footer { margin-top: 30px; font-size: 0.8em; text-align: center; }\n");
            html.append("  </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("  <h1>מסכת ").append(masechet).append(" דף ").append(daf).append(" עמוד ").append(amud).append("</h1>\n");
            
            // מידע על הדף
            html.append("  <div class=\"page-info\">\n");
            html.append("    תאריך ייצוא: ").append(new java.util.Date()).append("<br>\n");
            html.append("  </div>\n");
            
            // תוכן הדף
            html.append("  <div class=\"content-section\">\n");
            html.append("    <h2>תוכן הדף</h2>\n");
            html.append("    <div class=\"page-content\">\n");
            html.append("      ").append(content.replace("&", "&amp;")
                                             .replace("<", "&lt;")
                                             .replace(">", "&gt;")
                                             .replace("\n", "<br>\n      "));
            html.append("\n    </div>\n");
            html.append("  </div>\n");
            
            // הוספת הערות אישיות אם יש
            if (includeNotes) {
                try {
                    if (PersonalNotes.hasNote(masechet, daf, amud)) {
                        String note = PersonalNotes.loadNote(masechet, daf, amud);
                        html.append("  <div class=\"notes-section\">\n");
                        html.append("    <h2>הערות אישיות</h2>\n");
                        html.append("    <div class=\"page-content\">\n");
                        html.append("      ").append(note.replace("&", "&amp;")
                                                      .replace("<", "&lt;")
                                                      .replace(">", "&gt;")
                                                      .replace("\n", "<br>\n      "));
                        html.append("\n    </div>\n");
                        html.append("  </div>\n");
                    }
                } catch (IOException e) {
                    System.err.println("שגיאה בטעינת הערות: " + e.getMessage());
                }
            }
            
            html.append("  <div class=\"footer\">\n");
            html.append("    יוצא באמצעות מערכת BavliManager - ").append(new java.util.Date()).append("\n");
            html.append("  </div>\n");
            html.append("</body>\n");
            html.append("</html>");
            
            // כתיבה לקובץ
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputPath), "UTF-8"))) {
                writer.write(html.toString());
            }
            
            System.out.println("ייצוא המסמך המורחב הסתיים בהצלחה");
            return true;
            
        } catch (Exception e) {
            System.err.println("שגיאה בייצוא לקובץ HTML: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * ייצוא קובץ HTML עם תצוגה מותאמת אישית
     * 
     * @param masechet שם המסכת
     * @param daf מספר הדף
     * @param amud עמוד (א' או ב')
     * @param outputPath נתיב קובץ הפלט
     * @return האם הייצוא הצליח
     */
    public static boolean exportToHtml(String masechet, int daf, char amud, String outputPath) {
        String content = FileManager.loadPage(masechet, daf, amud);
        if (content.isEmpty()) {
            return false;
        }
        
        // בדיקה שהנתיב מסתיים ב-.html
        if (!outputPath.toLowerCase().endsWith(".html")) {
            outputPath += ".html";
        }
        
        try {
            // יצירת תיקיית הורה אם לא קיימת
            File parentDir = new File(outputPath).getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            System.out.println("מייצא לקובץ HTML: " + outputPath);
            
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html dir=\"rtl\">\n");
            html.append("<head>\n");
            html.append("  <meta charset=\"UTF-8\">\n");
            html.append("  <title>").append(masechet).append(" דף ").append(daf).append(" עמוד ").append(amud).append("</title>\n");
            html.append("  <style>\n");
            html.append("    body { font-family: 'David', 'Times New Roman', serif; direction: rtl; padding: 20px; }\n");
            html.append("    h1 { text-align: center; }\n");
            html.append("    .page-content { line-height: 1.6; white-space: pre-wrap; }\n");
            html.append("    .footer { margin-top: 30px; font-size: 0.8em; text-align: center; }\n");
            html.append("  </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("  <h1>מסכת ").append(masechet).append(" דף ").append(daf).append(" עמוד ").append(amud).append("</h1>\n");
            html.append("  <div class=\"page-content\">\n");
            
            // הוספת התוכן עם המרת תווים מיוחדים
            html.append("    ").append(content.replace("&", "&amp;")
                                           .replace("<", "&lt;")
                                           .replace(">", "&gt;")
                                           .replace("\n", "<br>\n    "));
            
            html.append("\n  </div>\n");
            html.append("  <div class=\"footer\">\n");
            html.append("    יוצא באמצעות מערכת BavliManager - ").append(new java.util.Date()).append("\n");
            html.append("  </div>\n");
            html.append("</body>\n");
            html.append("</html>");
            
            // כתיבה לקובץ
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputPath), "UTF-8"))) {
                writer.write(html.toString());
            }
            
            System.out.println("ייצוא ה-HTML הסתיים בהצלחה");
            return true;
            
        } catch (Exception e) {
            System.err.println("שגיאה בייצוא לקובץ HTML: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}