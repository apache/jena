# Screenshot Test Report

Generated: 2026-03-14 00:28:14 UTC

## Summary

**34** test cases across **11** groups. All passed.

| Group | Tests | Description |
|-------|------:|-------------|
| [Full-Text Search](#fulltext-search) | 5 | Lucene full-text search across `idx:TextField` fields (title, description). |
| [Single Facet](#single-facet) | 4 | Filter by a single `idx:KeywordField` facet value. |
| [Multi-Value Facet](#multivalue-facet) | 2 | OR filter across multiple values of the same facet field. |
| [Cross-Field Filters](#crossfield-filters) | 3 | AND combination of facet filters across different fields. |
| [FTS + Filters](#fts-filters) | 3 | Full-text search combined with facet filters. |
| [Entity Types](#entity-types) | 3 | Filter by entity type (Site, Borehole, Mining Report). |
| [Sequence Path (Author)](#sequence-path-author) | 2 | Filter by author name indexed via SHACL sequence path. |
| [Spatial: Bbox Filter](#spatial-bbox-filter) | 5 | Bounding box spatial filter using `s_intersects` on LatLon geometry. |
| [Spatial + FTS](#spatial-fts) | 2 | Full-text search combined with spatial bounding box filter. |
| [Spatial + Facets](#spatial-facets) | 3 | Facet filter combined with spatial bounding box filter. |
| [Spatial + FTS + Facets](#spatial-fts-facets) | 2 | All three combined: full-text + facets + spatial bbox. |

---

## Full-Text Search

> Lucene full-text search across `idx:TextField` fields (title, description).

### All entities

- Query: `(no params)`
- Results: **522 results**

![All entities](screenshots/full-text-search-all-entities.png)

### Search: "copper"

- Query: `?q=copper`
- Results: **21 results**

![Search: "copper"](screenshots/full-text-search-search-copper.png)

### Search: "gold mine"

- Query: `?q=gold+mine`
- Results: **156 results**

![Search: "gold mine"](screenshots/full-text-search-search-gold-mine.png)

### Search: "iron ore exploration"

- Query: `?q=iron+ore+exploration`
- Results: **59 results**

![Search: "iron ore exploration"](screenshots/full-text-search-search-iron-ore-exploration.png)

### Search: "diamond drill"

- Query: `?q=diamond+drill`
- Results: **203 results**

![Search: "diamond drill"](screenshots/full-text-search-search-diamond-drill.png)

---

## Single Facet

> Filter by a single `idx:KeywordField` facet value.

### commodity = Gold

- Query: `?filter={"op":"=","args":[{"property":"commodity"},"http://example.org/mining/commodity/Gold"]}`
- Results: **176 results**

![commodity = Gold](screenshots/single-facet-commodity-gold.png)

### state = NSW

- Query: `?filter={"op":"=","args":[{"property":"state"},"http://example.org/mining/state/NSW"]}`
- Results: **89 results**

![state = NSW](screenshots/single-facet-state-nsw.png)

### status = Historical

- Query: `?filter={"op":"=","args":[{"property":"status"},"http://example.org/mining/status/Historical"]}`
- Results: **81 results**

![status = Historical](screenshots/single-facet-status-historical.png)

### operator = BHP

- Query: `?filter={"op":"=","args":[{"property":"operator"},"http://example.org/mining/operator/BHP"]}`
- Results: **24 results**

![operator = BHP](screenshots/single-facet-operator-bhp.png)

---

## Multi-Value Facet

> OR filter across multiple values of the same facet field.

### commodity = Lead OR Copper

- Query: `?filter={"op":"in","args":[{"property":"commodity"},["http://example.org/mining/commodity/Lead","http://example.org/mining/commodity/Copper"]]}`
- Results: **124 results**

![commodity = Lead OR Copper](screenshots/multi-value-facet-commodity-lead-or-copper.png)

### state = WA OR QLD

- Query: `?filter={"op":"in","args":[{"property":"state"},["http://example.org/mining/state/WA","http://example.org/mining/state/QLD"]]}`
- Results: **327 results**

![state = WA OR QLD](screenshots/multi-value-facet-state-wa-or-qld.png)

---

## Cross-Field Filters

> AND combination of facet filters across different fields.

### Gold in WA

- Query: `?filter={"op":"and","args":[{"op":"=","args":[{"property":"commodity"},"http://example.org/mining/commodity/Gold"]},{"op":"=","args":[{"property":"state"},"http://example.org/mining/state/WA"]}]}`
- Results: **75 results**

![Gold in WA](screenshots/cross-field-filters-gold-in-wa.png)

### Copper in QLD + Active

- Query: `?filter={"op":"and","args":[{"op":"=","args":[{"property":"commodity"},"http://example.org/mining/commodity/Copper"]},{"op":"=","args":[{"property":"state"},"http://example.org/mining/state/QLD"]},{"op":"=","args":[{"property":"status"},"http://example.org/mining/status/Active"]}]}`
- Results: **3 results**

![Copper in QLD + Active](screenshots/cross-field-filters-copper-in-qld-active.png)

### Reports by Newmont

- Query: `?filter={"op":"and","args":[{"op":"=","args":[{"property":"entityType"},"http://example.org/mining/MiningReport"]},{"op":"=","args":[{"property":"operator"},"http://example.org/mining/operator/Newmont"]}]}`
- Results: **22 results**

![Reports by Newmont](screenshots/cross-field-filters-reports-by-newmont.png)

---

## FTS + Filters

> Full-text search combined with facet filters.

### "copper" in QLD

- Query: `?q=copper&filter={"op":"=","args":[{"property":"state"},"http://example.org/mining/state/QLD"]}`
- Results: **15 results**

!["copper" in QLD](screenshots/fts-filters-copper-in-qld.png)

### "gold" reports only

- Query: `?q=gold&filter={"op":"=","args":[{"property":"entityType"},"http://example.org/mining/MiningReport"]}`
- Results: **52 results**

!["gold" reports only](screenshots/fts-filters-gold-reports-only.png)

### "exploration" in WA

- Query: `?q=exploration&filter={"op":"=","args":[{"property":"state"},"http://example.org/mining/state/WA"]}`
- Results: **10 results**

!["exploration" in WA](screenshots/fts-filters-exploration-in-wa.png)

---

## Entity Types

> Filter by entity type (Site, Borehole, Mining Report).

### Sites only

- Query: `?filter={"op":"=","args":[{"property":"entityType"},"http://example.org/mining/Site"]}`
- Results: **107 results**

![Sites only](screenshots/entity-types-sites-only.png)

### Boreholes only

- Query: `?filter={"op":"=","args":[{"property":"entityType"},"http://example.org/mining/Borehole"]}`
- Results: **207 results**

![Boreholes only](screenshots/entity-types-boreholes-only.png)

### Reports only

- Query: `?filter={"op":"=","args":[{"property":"entityType"},"http://example.org/mining/MiningReport"]}`
- Results: **208 results**

![Reports only](screenshots/entity-types-reports-only.png)

---

## Sequence Path (Author)

> Filter by author name indexed via SHACL sequence path.

### Author: Dr Sarah Jones

- Query: `?filter={"op":"=","args":[{"property":"authorName"},"Dr Sarah Jones"]}`
- Results: **2 results**

![Author: Dr Sarah Jones](screenshots/sequence-path-author-author-dr-sarah-jones.png)

### Author: James Williams

- Query: `?filter={"op":"=","args":[{"property":"authorName"},"James Williams"]}`
- Results: **2 results**

![Author: James Williams](screenshots/sequence-path-author-author-james-williams.png)

---

## Spatial: Bbox Filter

> Bounding box spatial filter using `s_intersects` on LatLon geometry.

### Australia bbox (all spatial)

- Query: `?filter={"op":"s_intersects","args":[{"property":"location"},{"bbox":[112,-44,154,-10]}]}`
- Results: **309 results**

![Australia bbox (all spatial)](screenshots/spatial-bbox-filter-australia-bbox-all-spatial.png)

### Queensland bbox

- Query: `?filter={"op":"s_intersects","args":[{"property":"location"},{"bbox":[138,-29,154,-10]}]}`
- Results: **77 results**

![Queensland bbox](screenshots/spatial-bbox-filter-queensland-bbox.png)

### Western Australia bbox

- Query: `?filter={"op":"s_intersects","args":[{"property":"location"},{"bbox":[112,-35,129,-14]}]}`
- Results: **120 results**

![Western Australia bbox](screenshots/spatial-bbox-filter-western-australia-bbox.png)

### NSW + SA bbox

- Query: `?filter={"op":"s_intersects","args":[{"property":"location"},{"bbox":[129,-38,154,-28]}]}`
- Results: **66 results**

![NSW + SA bbox](screenshots/spatial-bbox-filter-nsw-sa-bbox.png)

### Excludes PNG (Aus only)

- Query: `?filter={"op":"s_intersects","args":[{"property":"location"},{"bbox":[112,-44,154,-10]}]}`
- Results: **309 results**

![Excludes PNG (Aus only)](screenshots/spatial-bbox-filter-excludes-png-aus-only.png)

---

## Spatial + FTS

> Full-text search combined with spatial bounding box filter.

### "mine" in QLD bbox

- Query: `?q=mine&filter={"op":"s_intersects","args":[{"property":"location"},{"bbox":[138,-29,154,-10]}]}`
- Results: **24 results**

!["mine" in QLD bbox](screenshots/spatial-fts-mine-in-qld-bbox.png)

### "mine" in WA bbox

- Query: `?q=mine&filter={"op":"s_intersects","args":[{"property":"location"},{"bbox":[112,-35,129,-14]}]}`
- Results: **38 results**

!["mine" in WA bbox](screenshots/spatial-fts-mine-in-wa-bbox.png)

---

## Spatial + Facets

> Facet filter combined with spatial bounding box filter.

### Sites in QLD bbox

- Query: `?filter={"op":"and","args":[{"op":"=","args":[{"property":"entityType"},"http://example.org/mining/Site"]},{"op":"s_intersects","args":[{"property":"location"},{"bbox":[138,-29,154,-10]}]}]}`
- Results: **24 results**

![Sites in QLD bbox](screenshots/spatial-facets-sites-in-qld-bbox.png)

### Boreholes in WA bbox

- Query: `?filter={"op":"and","args":[{"op":"=","args":[{"property":"entityType"},"http://example.org/mining/Borehole"]},{"op":"s_intersects","args":[{"property":"location"},{"bbox":[112,-35,129,-14]}]}]}`
- Results: **81 results**

![Boreholes in WA bbox](screenshots/spatial-facets-boreholes-in-wa-bbox.png)

### Gold + Aus bbox

- Query: `?filter={"op":"and","args":[{"op":"=","args":[{"property":"commodity"},"http://example.org/mining/commodity/Gold"]},{"op":"s_intersects","args":[{"property":"location"},{"bbox":[112,-44,154,-10]}]}]}`
- Results: **96 results**

![Gold + Aus bbox](screenshots/spatial-facets-gold-aus-bbox.png)

---

## Spatial + FTS + Facets

> All three combined: full-text + facets + spatial bbox.

### "mine" sites in QLD bbox

- Query: `?q=mine&filter={"op":"and","args":[{"op":"=","args":[{"property":"entityType"},"http://example.org/mining/Site"]},{"op":"s_intersects","args":[{"property":"location"},{"bbox":[138,-29,154,-10]}]}]}`
- Results: **24 results**

!["mine" sites in QLD bbox](screenshots/spatial-fts-facets-mine-sites-in-qld-bbox.png)

### "mine" in WA + state=WA bbox

- Query: `?q=mine&filter={"op":"and","args":[{"op":"=","args":[{"property":"state"},"http://example.org/mining/state/WA"]},{"op":"s_intersects","args":[{"property":"location"},{"bbox":[112,-35,129,-14]}]}]}`
- Results: **38 results**

!["mine" in WA + state=WA bbox](screenshots/spatial-fts-facets-mine-in-wa-state-wa-bbox.png)

---
