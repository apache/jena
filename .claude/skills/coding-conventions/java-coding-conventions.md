---
name: java-coding-conventions
description: House style for writing and reviewing Java and XML code in this repository. Use this skill whenever you are writing new Java code, editing existing Java files, generating code samples in Java, reviewing or refactoring Java code, or touching XML files (pom.xml, etc.) in this repo. Enforces brace style, indentation, and Javadoc conventions specific to this codebase — apply automatically, don't wait for the user to ask about "style" or "conventions" explicitly.
---

# Java Coding Conventions

House style for this repository. Apply these rules by default to every Java and XML file you write or edit here, without being asked each time.

## Java rules

### Indentation
- **4 spaces per indent level.** No tabs, anywhere, ever.
- Continuation lines (wrapped method args, chained calls, long expressions) indent by an extra 8 spaces (two levels) from the start of the statement, to visually distinguish them from a new block.

### Braces — K&R "Egyptian brackets"
- The opening brace `{` stays on the **same line** as the declaration/statement, preceded by a single space.
- The closing brace `}` starts a new line, aligned with the start of the opening statement.
- `else`, `catch`, `finally` go on the **same line** as the preceding closing brace.
- Braces are **not required** for single-line, single statement ones.
- Braces are **required** for multi-line code blocks.
 
```java
public class OrderService {

    public int computeTotal(List<Item> items) {
        int total = 0;
        for (Item item : items) {
            if (item.isTaxable()) {
                total += item.getPriceWithTax();
            } else {
                total += item.getPrice();
            }
        }
        return total;
    }

    public void process() {
        try {
            doWork();
        } catch (IOException e) {
            log.error("Failed to process", e);
        } finally {
            cleanup();
        }
    }
}
```

### Javadoc / comments
- **No `@author` tags**, in any file, ever — not on new files, not when editing old ones. If you encounter an existing `@author` tag while editing a file for another reason, leave it unless the user asks you to clean it up (don't do drive-by removals that bloat an unrelated diff).
- Javadoc is otherwise written normally: `/** ... */` blocks above public classes/methods, `@param`, `@return`, `@throws` as appropriate.

### Other conventions (standard "common Java style" — flag deviations if you see them)
- One top-level public class per file, filename matches the class name.
- `UpperCamelCase` for classes/interfaces/enums, `lowerCamelCase` for methods/fields/variables, `UPPER_SNAKE_CASE` for constants.
- Opening brace of a class/method body counts as level 0; the first statement inside is indented one level (4 spaces) from the class/method declaration.
- Import order: standard groups (java.*, javax.*, third-party, then project packages), alphabetized within each group, with a blank line between groups.
- Imports: Use wildcard imports for packages with 5 or more class imports.
- Imports: Use wildcard static imports for 5 or more imports.
- Line length: wrap around 80–100 columns rather than let lines run on indefinitely
