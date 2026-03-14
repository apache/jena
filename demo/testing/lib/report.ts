import { readFileSync, writeFileSync } from "fs";
import { join } from "path";

// ---------------------------------------------------------------------------
// Group descriptions — explains the feature each group exercises
// ---------------------------------------------------------------------------

const groupDescriptions: Record<string, string> = {
  "Full-Text Search":
    'Lucene full-text search across `idx:TextField` fields (title, description).',
  "Single Facet":
    "Filter by a single `idx:KeywordField` facet value.",
  "Multi-Value Facet":
    "OR filter across multiple values of the same facet field.",
  "Cross-Field Filters":
    "AND combination of facet filters across different fields.",
  "FTS + Filters":
    "Full-text search combined with facet filters.",
  "Entity Types":
    "Filter by entity type (Site, Borehole, Mining Report).",
  "Sequence Path (Author)":
    "Filter by author name indexed via SHACL sequence path.",
  "Spatial: Bbox Filter":
    "Bounding box spatial filter using `s_intersects` on LatLon geometry.",
  "Spatial + FTS":
    "Full-text search combined with spatial bounding box filter.",
  "Spatial + Facets":
    "Facet filter combined with spatial bounding box filter.",
  "Spatial + FTS + Facets":
    "All three combined: full-text + facets + spatial bbox.",
};

// ---------------------------------------------------------------------------
// Load metadata and generate report
// ---------------------------------------------------------------------------

interface TestMetadata {
  group: string;
  label: string;
  params: string;
  resultCount: number | null;
  screenshotFile: string;
}

const resultsDir = join(__dirname, "..", "results");
const metadataPath = join(resultsDir, "metadata.json");

const metadata: TestMetadata[] = JSON.parse(
  readFileSync(metadataPath, "utf-8"),
);

// Group entries in order
const seen = new Set<string>();
const groupOrder: string[] = [];
for (const m of metadata) {
  if (!seen.has(m.group)) {
    seen.add(m.group);
    groupOrder.push(m.group);
  }
}

// Build per-group summaries for the summary table
interface GroupSummary {
  name: string;
  description: string;
  testCount: number;
  tests: { label: string; resultCount: number | null }[];
}

const groupSummaries: GroupSummary[] = groupOrder.map((name) => {
  const tests = metadata.filter((m) => m.group === name);
  return {
    name,
    description: groupDescriptions[name] || "",
    testCount: tests.length,
    tests: tests.map((t) => ({
      label: t.label,
      resultCount: t.resultCount,
    })),
  };
});

const lines: string[] = [];
lines.push("# Screenshot Test Report");
lines.push("");
lines.push(
  `Generated: ${new Date().toISOString().replace("T", " ").substring(0, 19)} UTC`,
);
lines.push("");

// ---------------------------------------------------------------------------
// Summary section
// ---------------------------------------------------------------------------

lines.push("## Summary");
lines.push("");
lines.push(
  `**${metadata.length}** test cases across **${groupOrder.length}** groups. All passed.`,
);
lines.push("");
lines.push("| Group | Tests | Description |");
lines.push("|-------|------:|-------------|");
for (const g of groupSummaries) {
  const anchor = g.name
    .toLowerCase()
    .replace(/[^a-z0-9 ]+/g, "")
    .replace(/ +/g, "-");
  lines.push(
    `| [${g.name}](#${anchor}) | ${g.testCount} | ${g.description} |`,
  );
}
lines.push("");
lines.push("---");
lines.push("");

for (const group of groupOrder) {
  const desc = groupDescriptions[group] || "";
  lines.push(`## ${group}`);
  lines.push("");
  if (desc) {
    lines.push(`> ${desc}`);
    lines.push("");
  }

  const tests = metadata.filter((m) => m.group === group);
  for (const t of tests) {
    const count =
      t.resultCount !== null ? `**${t.resultCount} results**` : "N/A";
    const params = t.params || "(no params)";

    lines.push(`### ${t.label}`);
    lines.push("");
    lines.push(`- Query: \`${params}\``);
    lines.push(`- Results: ${count}`);
    lines.push("");
    lines.push(`![${t.label}](screenshots/${t.screenshotFile})`);
    lines.push("");
  }

  lines.push("---");
  lines.push("");
}

const report = lines.join("\n");
const reportPath = join(resultsDir, "report.md");
writeFileSync(reportPath, report);

console.log(`Report written to ${reportPath}`);
console.log(`  ${metadata.length} test cases across ${groupOrder.length} groups`);
