import { test, expect } from "@playwright/test";
import { readFileSync, mkdirSync, writeFileSync } from "fs";
import { join } from "path";

// ---------------------------------------------------------------------------
// Load test cases from the shared tests.json
// ---------------------------------------------------------------------------

interface GroupEntry {
  group: string;
}
interface TestEntry {
  label: string;
  params: string;
  minResults?: number;
}
type Entry = GroupEntry | TestEntry;

const testsJsonPath = join(__dirname, "..", "..", "test", "tests.json");
const entries: Entry[] = JSON.parse(readFileSync(testsJsonPath, "utf-8"));

// Parse into groups: { name, tests[] }
interface TestGroup {
  name: string;
  tests: TestEntry[];
}

const groups: TestGroup[] = [];
let current: TestGroup | null = null;

for (const entry of entries) {
  if ("group" in entry) {
    current = { name: entry.group, tests: [] };
    groups.push(current);
  } else if (current && "label" in entry) {
    current.tests.push(entry as TestEntry);
  }
}

// ---------------------------------------------------------------------------
// Shared metadata — collected across all tests, written in teardown
// ---------------------------------------------------------------------------

interface TestMetadata {
  group: string;
  label: string;
  params: string;
  resultCount: number | null;
  screenshotFile: string;
}

const metadata: TestMetadata[] = [];
const resultsDir = join(__dirname, "..", "results");
const screenshotsDir = join(resultsDir, "screenshots");

function slugify(text: string): string {
  return text
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-|-$/g, "");
}

// Ensure output directories exist
mkdirSync(screenshotsDir, { recursive: true });

// ---------------------------------------------------------------------------
// Generate tests from groups
// ---------------------------------------------------------------------------

for (const group of groups) {
  test.describe(group.name, () => {
    for (const tc of group.tests) {
      test(tc.label, async ({ page }) => {
        // Build URL with properly encoded filter param
        let url = "index.html";
        const raw = tc.params || "";
        if (raw) {
          const parts = raw.replace(/^\?/, "").split("&");
          const encoded = parts.map((part) => {
            const eq = part.indexOf("=");
            if (eq < 0) return part;
            const key = part.substring(0, eq);
            const val = part.substring(eq + 1);
            return key + "=" + encodeURIComponent(val);
          });
          url += "?" + encoded.join("&");
        }
        await page.goto(url, { waitUntil: "domcontentloaded" });

        // Wait for search results or empty notice (search complete)
        await page.waitForSelector(".result-card, .notice-empty", {
          timeout: 20_000,
        });

        // Brief pause for map tiles and Alpine transitions to settle
        await page.waitForTimeout(1500);

        // Extract result count from description text
        let resultCount: number | null = null;
        const descEl = page.locator(".results-description").first();
        if (await descEl.isVisible()) {
          const text = await descEl.innerText();
          // Matches patterns like "10 results" or "10 of 500 results"
          const m = text.match(/([\d,]+)\s+results?/);
          if (m) {
            resultCount = parseInt(m[1].replace(/,/g, ""), 10);
          }
        }

        // Screenshot
        const slug = slugify(`${group.name}-${tc.label}`);
        const screenshotFile = `${slug}.png`;
        const screenshotPath = join(screenshotsDir, screenshotFile);

        await page.screenshot({ path: screenshotPath, fullPage: true });

        // Collect metadata
        metadata.push({
          group: group.name,
          label: tc.label,
          params: tc.params,
          resultCount,
          screenshotFile,
        });

        // Assert: no connection/query errors
        const errorEl = page.locator(".notice-error");
        if (await errorEl.isVisible()) {
          const errorText = await errorEl.innerText();
          expect(errorText, "Page showed an error").toBeFalsy();
        }

        // Assert: minimum result count when specified
        if (tc.minResults != null) {
          expect(
            resultCount,
            `Expected at least ${tc.minResults} results for "${tc.label}" but got ${resultCount}`
          ).toBeGreaterThanOrEqual(tc.minResults);
        }
      });
    }
  });
}

// ---------------------------------------------------------------------------
// Write metadata after all tests complete
// ---------------------------------------------------------------------------

test.afterAll(() => {
  const metadataPath = join(resultsDir, "metadata.json");
  writeFileSync(metadataPath, JSON.stringify(metadata, null, 2));
});
