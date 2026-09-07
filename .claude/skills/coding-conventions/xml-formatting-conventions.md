---
name: xml-formatting-conventions
description: House style for writing and reviewing XML code in this repository.
---

## XML Formatting Conventions

- **2 spaces per indent level.** No tabs.
- One attribute per line only when a tag has many attributes and would otherwise run long; short tags keep attributes inline.
- Self-closing tags (`<element .../>`) for elements with no children/content.
- Closing tags aligned with the indentation level of their opening tag.

(Note: keep XML indentation at 2 spaces even though Java in the same repo uses 4 — they're intentionally different.)
