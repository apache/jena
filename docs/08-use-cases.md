# Use Cases

Each feature is a building block. They can be used independently or combined to build richer search experiences. This page shows what each block does, with examples of where it applies.

---

## Completed Features

### Full-Text Search with Filters — `luc:query`

Search across entity fields using Lucene query syntax. Optionally narrow results with structured JSON filters that apply field-level constraints.

```mermaid
flowchart LR
    User(["User query:<br/>'climate change'"])
    Filter["+ filter:<br/>publisher = CSIRO"]
    Query["luc:query"]
    Results["Matching entities<br/>ranked by relevance"]

    User --> Query
    Filter -.-> Query
    Query --> Results

    style User fill:#f8f9fa,stroke:#999,color:#333
    style Filter fill:#fff3cd,stroke:#c89a06,color:#614a00
    style Query fill:#1a6dd4,stroke:#0d4a94,color:#fff
    style Results fill:#d4edda,stroke:#28a745,color:#1a3d1a
```

```sparql
# Simple search
(?s ?score) luc:query ("climate change") .

# Search narrowed to a publisher
(?s ?score) luc:query ("climate change" '{"publisher": ["CSIRO"]}') .

# Multiple filters (AND across fields, OR within a field)
(?s ?score) luc:query ("climate" '{"publisher": ["CSIRO", "BOM"], "category": ["Environment"]}') .
```

**Where this applies:**
- Search box in a data catalogue
- Keyword search in a document repository
- Filtered API endpoint for a knowledge graph

---

### Facet Counts — `luc:facet`

Get value counts for one or more fields across the result set. Shows how results distribute across categories, publishers, types, etc.

```mermaid
flowchart LR
    Query["luc:facet<br/>'climate change'<br/>fields: category, publisher"]
    Cat["category:<br/>Environment (42)<br/>Policy (28)<br/>Science (15)"]
    Pub["publisher:<br/>CSIRO (31)<br/>BOM (22)<br/>DCCEEW (12)"]

    Query --> Cat
    Query --> Pub

    style Query fill:#1a6dd4,stroke:#0d4a94,color:#fff
    style Cat fill:#d4edda,stroke:#28a745,color:#1a3d1a
    style Pub fill:#d4edda,stroke:#28a745,color:#1a3d1a
```

```sparql
# Counts for category and publisher, top 10 values each
(?field ?value ?count) luc:facet ("climate change" '["category", "publisher"]' 10) .

# With minCount — only values with 5+ results
(?field ?value ?count) luc:facet ("climate change" '["category"]' 10 5) .
```

**Where this applies:**
- Sidebar facet panels in a search UI
- Summary statistics for a dataset collection
- "Browse by" navigation (by theme, by organisation, by type)

---

### Search + Facets Together — Recommended Patterns

Search hits and facet counts are different result shapes. Hits are entities with scores; facets are (field, value, count) aggregations. Care is needed to avoid a cartesian product when combining them.

```mermaid
flowchart TB
    App["Search UI"]
    Q1["Query 1: luc:query<br/>(entity URIs + scores)"]
    Q2["Query 2: luc:facet<br/>(field, value, count)"]
    SE["SearchExecution<br/>(shared internally<br/>when in same query)"]

    App --> Q1
    App --> Q2
    Q1 --> SE
    Q2 --> SE

    style App fill:#f8f9fa,stroke:#999,color:#333
    style Q1 fill:#1a6dd4,stroke:#0d4a94,color:#fff
    style Q2 fill:#1a6dd4,stroke:#0d4a94,color:#fff
    style SE fill:#e8f0fe,stroke:#b8d4f8,color:#333
```

```sparql
# Recommended: separate queries, clean result shapes
# Query 1 — hits
SELECT ?s ?score WHERE {
    (?s ?score) luc:query ("climate change") .
}

# Query 2 — facets
SELECT ?field ?value ?count WHERE {
    (?field ?value ?count) luc:facet ("climate change" '["category", "publisher"]' 10) .
}

# Alternative: single query via UNION (N+M rows, no cartesian product)
SELECT ?s ?score ?field ?value ?count WHERE {
    { (?s ?score) luc:query ("climate change") . }
    UNION
    { (?field ?value ?count) luc:facet ("climate change" '["category", "publisher"]' 10) . }
}
```

**Where this applies:**
- Any search page that shows results and facet counts together
- Matches the pattern used by Elasticsearch and Solr — one request for hits, one for facets
- UNION alternative when a single SPARQL request is required

---

### Automatic Index Maintenance — Change Listener

When triples are added or removed from the RDF dataset, the Lucene index updates automatically. No manual reindexing or sync jobs required.

```mermaid
flowchart LR
    App["Application<br/>adds/removes triples"]
    TDB["TDB2 Dataset"]
    Listener["ShaclTextDocProducer<br/>(change listener)"]
    Lucene["Lucene Index<br/>(entity doc rebuilt)"]

    App --> TDB
    TDB -- "triple change event" --> Listener
    Listener -- "rebuild entity doc" --> Lucene

    style App fill:#f8f9fa,stroke:#999,color:#333
    style TDB fill:#f8f9fa,stroke:#999,color:#333
    style Listener fill:#1a6dd4,stroke:#0d4a94,color:#fff
    style Lucene fill:#d4edda,stroke:#28a745,color:#1a3d1a
```

**Where this applies:**
- Live data feeds — index stays current without batch jobs
- Editorial workflows — changes are searchable immediately
- Replaces custom ETL pipelines that sync data to a separate search engine

---

### Entity-Per-Document Indexing — SHACL Shapes

Each entity (e.g. a Book, a Dataset, a Person) becomes a single Lucene document with all its fields. SHACL shapes define what gets indexed and how.

```mermaid
flowchart LR
    subgraph RDF["RDF Triples"]
        t1["ex:book1 rdf:type ex:Book"]
        t2["ex:book1 rdfs:label 'Machine Learning'"]
        t3["ex:book1 ex:category 'Technology'"]
        t4["ex:book1 ex:year 2024"]
    end

    subgraph Doc["Lucene Document"]
        d1["uri: ex:book1"]
        d2["docType: Book"]
        d3["title: 'Machine Learning' (TEXT)"]
        d4["category: 'Technology' (KEYWORD)"]
        d5["year: 2024 (INT)"]
    end

    RDF -- "SHACL shape<br/>defines mapping" --> Doc

    style RDF fill:#f8f9fa,stroke:#999,color:#333
    style Doc fill:#d4edda,stroke:#28a745,color:#1a3d1a
```

Field types control how each value is indexed:

| Type | Searchable | Facetable | Range queries | Example |
|------|-----------|-----------|---------------|---------|
| TEXT | Full-text | No | No | Title, description, abstract |
| KEYWORD | Exact match | Yes | No | Category, publisher, status |
| INT / LONG | No | No | Yes | Year, count, size |
| DOUBLE | No | No | Yes | Score, latitude, price |

**Where this applies:**
- Any RDF dataset where entities have typed properties
- Replaces the need for a separate search schema — SHACL shapes serve as both data model and index definition

---

## Proposed Features

### Inverse and Sequence Paths

Extends `sh:path` support · No breaking changes

Currently only direct properties (`sh:path <uri>`) and alternatives (`sh:alternativePath`) can be indexed. This limits what data reaches the index without denormalising the RDF. Path extensions let the index follow relationships.

```mermaid
flowchart LR
    subgraph direct["Current: direct path only"]
        B1["ex:book1"] -- "rdfs:label" --> L1["'Machine Learning'"]
    end

    subgraph inverse["Inverse path"]
        P1["ex:smith"] -- "ex:wrote" --> B2["ex:book1"]
        B2 -. "sh:inversePath ex:wrote<br/>indexes author on book" .-> P1
    end

    subgraph sequence["Sequence path"]
        B3["ex:book1"] -- "ex:author" --> P2["ex:smith"]
        P2 -- "ex:name" --> N1["'Jane Smith'"]
        B3 -. "ex:author / ex:name<br/>indexes name on book" .-> N1
    end

    style direct fill:none,stroke:#999
    style inverse fill:none,stroke:#b8d4f8
    style sequence fill:none,stroke:#b8d4f8
```

**Where this applies:**
- Index an author's name directly on a book (sequence path: `ex:author / ex:name`) — avoids needing a denormalised `ex:authorName` property
- Index incoming references (inverse path: `sh:inversePath ex:memberOf`) — find an organisation by searching its members' names
- Index labels of linked entities — a dataset's theme label, a product's manufacturer name

---

### Spatial Filtering — Bounding Box

Extends filter argument · No breaking changes

Restrict search results and facet counts to entities within a geographic bounding box.

```mermaid
flowchart LR
    Search["luc:query<br/>'climate change'"]
    BBox["+ bbox filter:<br/>SW: -44°, 112°<br/>NE: -10°, 154°<br/>(Australia)"]
    Results["Only results<br/>within the<br/>bounding box"]
    Facets["Facet counts<br/>reflect spatial<br/>filter"]

    Search --> Results
    BBox -.-> Search
    Search --> Facets

    style Search fill:#1a6dd4,stroke:#0d4a94,color:#fff
    style BBox fill:#fff3cd,stroke:#c89a06,color:#614a00
    style Results fill:#d4edda,stroke:#28a745,color:#1a3d1a
    style Facets fill:#d4edda,stroke:#28a745,color:#1a3d1a
```

**Where this applies:**
- Map-based data discovery (draw a region, see what's there)
- Geospatial catalogues (environmental data, infrastructure, land use)
- Location-scoped search (find datasets near a city or within a jurisdiction)

---

### DrillSideways — Smarter Facet Counting

Opt-in on `luc:facet` · No breaking changes · Deferrable

Standard faceted search UX. When a user filters by `category = Environment`, the category facet still shows counts for *all* categories — so the user can see what else is available and switch. Other facets (publisher, year) narrow as expected.

```mermaid
flowchart LR
    subgraph current["Current: narrow counting"]
        F1c["category:<br/>Environment (42)"]
        F1p["publisher:<br/>CSIRO (20)<br/>BOM (12)<br/>DCCEEW (10)"]
    end

    subgraph drill["With DrillSideways"]
        F2c["category:<br/>Environment (42) ✓<br/>Policy (28)<br/>Science (15)"]
        F2p["publisher:<br/>CSIRO (20)<br/>BOM (12)<br/>DCCEEW (10)"]
    end

    style current fill:#f8f9fa,stroke:#999,color:#333
    style drill fill:#e8f0fe,stroke:#b8d4f8,color:#333
```

**Where this applies:**
- E-commerce product filtering (Amazon, eBay pattern)
- Library catalogue browsing
- Any UI where users iteratively refine search by selecting facets

---

### Hierarchical Facets — Taxonomy Drill-Down

Extends `luc:facet` · No breaking changes · Deferrable

Navigate taxonomy trees as facets. Values become paths rather than flat strings.

```mermaid
flowchart TB
    Root["category"]
    Sci["Science (85)"]
    Earth["Earth Science (42)"]
    Clim["Climate (28)"]
    Phys["Physics (31)"]
    Soc["Social Science (35)"]

    Root --> Sci
    Root --> Soc
    Sci --> Earth
    Sci --> Phys
    Earth --> Clim

    style Root fill:#1a6dd4,stroke:#0d4a94,color:#fff
    style Sci fill:#e8f0fe,stroke:#b8d4f8,color:#333
    style Earth fill:#e8f0fe,stroke:#b8d4f8,color:#333
    style Clim fill:#e8f0fe,stroke:#b8d4f8,color:#333
    style Phys fill:#e8f0fe,stroke:#b8d4f8,color:#333
    style Soc fill:#e8f0fe,stroke:#b8d4f8,color:#333
```

**Where this applies:**
- Subject classification systems (ANZSRC, DDC, LCSH)
- Product category trees (electronics > phones > smartphones)
- Organisational hierarchies (department > division > team)

---

### Range Facets — Numeric Buckets

New PF `luc:facetRange` · No breaking changes · Deferrable

Group numeric or date values into ranges and return counts per range.

```mermaid
flowchart LR
    Query["luc:facetRange<br/>field: year<br/>ranges: 2020, 2022, 2024, 2026"]
    R1["2020–2022 (35)"]
    R2["2022–2024 (28)"]
    R3["2024–2026 (18)"]

    Query --> R1
    Query --> R2
    Query --> R3

    style Query fill:#1a6dd4,stroke:#0d4a94,color:#fff
    style R1 fill:#e8f0fe,stroke:#b8d4f8,color:#333
    style R2 fill:#e8f0fe,stroke:#b8d4f8,color:#333
    style R3 fill:#e8f0fe,stroke:#b8d4f8,color:#333
```

**Where this applies:**
- Year/date filtering (publications, events, records)
- Price bands (products, grants, budgets)
- Size or quantity ranges (file size, population, area)

---

### Result Grouping

New PF `luc:group` · No breaking changes · Deferrable

Group search results by a field value instead of returning a flat list.

```mermaid
flowchart LR
    Query["luc:group<br/>'climate change'<br/>group by: publisher"]

    subgraph Groups["Grouped results"]
        G1["CSIRO (31 results)<br/>· Coral Reef Report<br/>· Emissions Study<br/>· ..."]
        G2["BOM (22 results)<br/>· Climate Data 2025<br/>· Rainfall Trends<br/>· ..."]
        G3["DCCEEW (12 results)<br/>· National Assessment<br/>· Policy Framework<br/>· ..."]
    end

    Query --> Groups

    style Query fill:#1a6dd4,stroke:#0d4a94,color:#fff
    style Groups fill:#e8f0fe,stroke:#b8d4f8,color:#333
```

**Where this applies:**
- "Results by publisher" or "results by category" view
- Email-style threading (group by conversation)
- Grouped search results (like Google's site-grouped results)

---

### Suggest / Autocomplete

New PF `luc:suggest` · No breaking changes · Deferrable

Type-ahead completions as the user types, backed by Lucene's suggester infrastructure.

```mermaid
flowchart LR
    Input["User types:<br/>'clim'"]
    Suggest["luc:suggest"]
    S1["climate change"]
    S2["climate modelling"]
    S3["climatology"]

    Input --> Suggest
    Suggest --> S1
    Suggest --> S2
    Suggest --> S3

    style Input fill:#f8f9fa,stroke:#999,color:#333
    style Suggest fill:#1a6dd4,stroke:#0d4a94,color:#fff
    style S1 fill:#e8f0fe,stroke:#b8d4f8,color:#333
    style S2 fill:#e8f0fe,stroke:#b8d4f8,color:#333
    style S3 fill:#e8f0fe,stroke:#b8d4f8,color:#333
```

**Where this applies:**
- Search box type-ahead in any portal
- Entity name completion in data entry forms
- Quick navigation to known items

---

## How They Combine

These building blocks compose naturally. A single application can use any combination:

| Application | Search | Facets | Filters | Paths | Spatial | DrillSideways | Ranges | Hierarchical | Grouping | Suggest |
|-------------|--------|--------|---------|-------|---------|---------------|--------|--------------|----------|---------|
| Data catalogue | x | x | x | x | x | x | x | | | x |
| Geospatial portal | x | x | x | | x | | | | | |
| Research repository | x | x | x | x | | x | x | x | | x |
| Museum catalogue | x | x | x | x | | | x | | x | x |
| Corporate knowledge graph | x | x | x | x | | x | | | x | x |
| Simple search page | x | | | | | | | | | |

Each "x" is a feature block added to the SPARQL query. No custom backend code — the application constructs SPARQL and sends it to Fuseki.
