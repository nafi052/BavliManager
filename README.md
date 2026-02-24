# BavliManager

[![Build and Test](https://github.com/nafi052/BavliManager/actions/workflows/build.yml/badge.svg)](https://github.com/nafi052/BavliManager/actions/workflows/build.yml)

Created by **Nafi Shvinger** — Java application for managing and studying Talmud Bavli (Babylonian Talmud).

**Tech stack:** Java 11, Maven, Swing (GUI), single-package structure.

## Quick Start

**Requirements:** JDK 11 (or 17), Maven 3.6+

```bash
# Build and run tests
mvn verify

# Run the application (console menu)
mvn compile exec:java -Dexec.mainClass="bavli.Main"

# Or from your IDE: run bavli.Main
```

The repo includes minimal sample data under `pages/ברכות/` so tests pass. For full content, use option **6** in the menu to split a main `bavli.txt` file into tractate/page structure.

## Overview
BavliManager is a Java-based project designed to manage and interact with the Talmud Bavli (Babylonian Talmud).
It provides comprehensive tools for indexing, searching, displaying, and studying content from the Talmud, as well as managing personal notes and exporting content.

## Features
- **Index Management**: Load and build indexes for tractates (מסכתות) and Mishnayot (משניות).
- **Search Functionality**: Retrieve pages containing Mishnayot for specific chapters and tractates.
- **User Interface**: Display a menu for user interaction.
- **Personal Notes**: Create and manage personal study notes tied to specific Talmud pages.
- **HTML Export**: Export Talmud content and notes to HTML format for viewing in browsers.
- **Caching**: Optimize performance by caching frequently accessed data.
- **Search Engine**: Advanced search capabilities across Talmud content.
- **External Display**: Interface for displaying content on external viewers.

## Project Structure
- `src/`: Java source (package `bavli`).
- `target/`: Maven build output (after `mvn compile`).
- `pages/`: Tractate/page folders (sample included; use menu option 6 to split `bavli.txt` for full data).
- `lib/`: Optional JARs (e.g. itext for PDF).
- `user_notes/`: Storage for personal study notes.

## Key Classes
- `Main`: The main entry point of the application.
- `DataIndex`: Handles indexing and retrieval of Talmudic data.
- `SwitchCase`: Manages user menu interactions.
- `SearchEngine`: Implements advanced search capabilities.
- `PersonalNotes`: Manages user's study notes.
- `MishnaExtractor`: Extracts Mishna content from Talmud pages.
- `HtmlExporter`: Exports content to HTML format.
- `FileManager`: Handles file system operations.
- `ExternalDisplay`: Manages external viewing interfaces.
- `CacheManager`: Optimizes performance through data caching.
- `Tests`: Contains testing functionality.

## How to Run
- **Maven:** `mvn compile exec:java -Dexec.mainClass="bavli.Main"` then follow the menu.
- **IDE:** Open as Maven project, run `bavli.Main`.
- **Tests:** `mvn verify` runs the full test suite (exit code 1 if any test fails).

## Example Usage
```java
DataIndex.loadIndex(); // Load tractate index
DataIndex.buildMishnaIndex(); // Build Mishna index
System.out.println(DataIndex.getDafsWithMishna("ברכות", 2)); // Get pages with Mishnayot in chapter 2 of Berakhot
SwitchCase.displayMenu(); // Display user menu
```

## Dependencies
- **JDK 11** (or 17)
- **Maven 3.6+**
- Optional: `itext-2.1.7.jar` in `lib/` for PDF functionality (see itext documentation for download).

## License
This project is licensed under the MIT License.

## סקירה כללית
פרויקט BavliManager הוא פרויקט מבוסס Java שנועד לנהל ולתקשר עם התלמוד הבבלי.
הוא מספק כלים מקיפים לניהול אינדקסים, חיפוש, הצגה ולימוד של תוכן מהתלמוד, כמו גם ניהול הערות אישיות וייצוא תוכן.

## תכונות
- **ניהול אינדקסים**: טעינה ובניית אינדקסים למסכתות ומשניות.
- **פונקציונליות חיפוש**: שליפת דפים המכילים משניות עבור פרקים ומסכתות מסוימות.
- **ממשק משתמש**: הצגת תפריט לאינטראקציה עם המשתמש.
- **הערות אישיות**: יצירה וניהול של הערות לימוד אישיות הקשורות לדפי תלמוד מסוימים.
- **ייצוא HTML**: ייצוא תוכן תלמודי והערות לפורמט HTML לצפייה בדפדפנים.
- **מטמון (Caching)**: אופטימיזציה של ביצועים באמצעות שמירת נתונים נגישים בזיכרון מטמון.
- **מנוע חיפוש**: יכולות חיפוש מתקדמות בתוכן התלמוד.
- **תצוגה חיצונית**: ממשק להצגת תוכן על צגים חיצוניים.

## מבנה הפרויקט
- `src/`: קוד מקור Java (חבילה `bavli`).
- `target/`: פלט בניית Maven (אחרי `mvn compile`).
- `pages/`: תיקיות מסכת/דף (כולל דוגמה; אפשרות 6 בתפריט מפצלת את `bavli.txt` לנתונים מלאים).
- `lib/`: JAR אופציונליים (למשל itext ל-PDF).
- `user_notes/`: אחסון הערות לימוד אישיות.

## מחלקות עיקריות
- `Main`: נקודת הכניסה הראשית של האפליקציה.
- `DataIndex`: מטפל בניהול אינדקסים ושליפת נתונים תלמודיים.
- `SwitchCase`: מנהל אינטראקציות עם תפריט המשתמש.
- `SearchEngine`: מיישם יכולות חיפוש מתקדמות.
- `PersonalNotes`: מנהל את הערות הלימוד של המשתמש.
- `MishnaExtractor`: מחלץ תוכן משנה מדפי תלמוד.
- `HtmlExporter`: מייצא תוכן לפורמט HTML.
- `FileManager`: מטפל בפעולות מערכת קבצים.
- `ExternalDisplay`: מנהל ממשקי צפייה חיצוניים.
- `CacheManager`: אופטימיזציה של ביצועים באמצעות מטמון נתונים.
- `Tests`: מכיל פונקציונליות בדיקה.

## כיצד להפעיל
- **Maven:** `mvn compile exec:java -Dexec.mainClass="bavli.Main"` ואז לעקוב אחר התפריט.
- **IDE:** לפתוח כפרויקט Maven ולהריץ את `bavli.Main`.
- **בדיקות:** `mvn verify` מריץ את כל הבדיקות (קוד יציאה 1 אם נכשלת בדיקה).

## דוגמת שימוש
```java
DataIndex.loadIndex(); // טעינת אינדקס מסכתות
DataIndex.buildMishnaIndex(); // בניית אינדקס משניות
System.out.println(DataIndex.getDafsWithMishna("ברכות", 2)); // שליפת דפים עם משניות בפרק 2 של ברכות
SwitchCase.displayMenu(); // הצגת תפריט משתמש
```

## תלויות
- **JDK 11** (או 17)
- **Maven 3.6+**
- אופציונלי: `itext-2.1.7.jar` בתיקיית `lib/` לפונקציונליות PDF.

## רישיון
הפרויקט הזה מורשה תחת רישיון MIT.
