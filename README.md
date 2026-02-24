# BavliManager

[![Build and Test](https://github.com/nafi052/BavliManager/actions/workflows/build.yml/badge.svg)](https://github.com/nafi052/BavliManager/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**BavliManager** is a Java application for indexing, searching, and studying Talmud Bavli content, including personal notes and export tools.

**תחזית קצרה:** זהו כלי Java לניהול תוכן תלמודי, חיפוש, הערות אישיות וייצוא.

## Quick Start (EN)

### Requirements
- JDK 17
- Maven 3.8+

### Build, test, verify
```bash
mvn clean test verify
```

### Run the app
```bash
mvn compile exec:java -Dexec.mainClass="bavli.Main"
```

### Data notes
- The repository includes minimal sample data under `pages/ברכות/` for CI/testing.
- For full content preparation, use menu option **6** to split `bavli.txt` into tractate/page structure.

## התחלה מהירה (עברית)

### דרישות
- JDK 17
- Maven 3.8+

### בנייה והרצת בדיקות
```bash
mvn clean test verify
```

### הרצת האפליקציה
```bash
mvn compile exec:java -Dexec.mainClass="bavli.Main"
```

### הערות על נתונים
- הריפו כולל דוגמת נתונים מינימלית תחת `pages/ברכות/` לצורכי CI ובדיקות.
- להכנת נתונים מלאים, ניתן להשתמש באפשרות **6** בתפריט כדי לפצל את `bavli.txt` למבנה מסכת/דף.

## Features / תכונות
- Index loading and mishna indexing / טעינה ובנייה של אינדקסים
- Chapter/tractate search / חיפוש לפי פרק ומסכת
- Personal notes management / ניהול הערות אישיות
- HTML export / ייצוא ל-HTML
- Caching and performance helpers / מטמון ואופטימיזציית ביצועים

## Project Structure
- `src/` - Java source (package `bavli`)
- `src/test/java/` - JUnit tests (incremental migration)
- `pages/` - Tractate/page folders
- `lib/` - Optional external jars
- `user_notes/` - Local personal notes

## Testing Strategy
- `mvn test` runs JUnit tests.
- `mvn verify` also runs the existing internal test runner (`bavli.Tests`) via Maven `exec` during `verify`.
- This dual setup supports gradual migration toward standard JUnit coverage.

## Contributing & Security
- Contribution guide: see [CONTRIBUTING.md](CONTRIBUTING.md)
- Security policy: see [SECURITY.md](SECURITY.md)
- Change log: see [CHANGELOG.md](CHANGELOG.md)

## License
MIT — see [LICENSE](LICENSE).
