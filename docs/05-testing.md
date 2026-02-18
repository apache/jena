# Testing

## Running Tests

```bash
# Full jena-text suite (362 tests)
mvn test -pl jena-text

# Only SHACL / entity-per-document tests (35 tests)
mvn test -pl jena-text -Dtest="TestShaclIndexMapping,TestShaclDocumentBuilding,TestShaclTextDocProducer,TestShaclAssembler,TestShaclEntityPerDocument"

# Only faceting tests (classic mode)
mvn test -pl jena-text -Dtest="TestNativeFacetCounts,TestTextFacetPF,TestSearchExecution"
```

All tests run via JUnit 4 and are aggregated in `TS_Text.java` (Surefire only picks up `**/TS_*.java`).

---

## Test Suite Overview

### Classic Faceting Tests

| Class | Tests | What it covers |
|-------|-------|---------------|
| `TestNativeFacetCounts` | 10 | Java API: open facets, filtered facets, maxValues, minCount, getAllChildren, empty/nonexistent fields |
| `TestTextFacetPF` | 8 | SPARQL `text:facet` PF: basic counts, multiple fields, filters, maxValues, minCount, property args |
| `TestSearchExecution` | 6 | Shared execution: key generation, normalisation, reuse across PFs |

### SHACL Entity-Per-Document Tests

| Class | Tests | What it covers |
|-------|-------|---------------|
| `TestShaclIndexMapping` | 8 | Data model: predicate lookup, class lookup, irrelevant predicates, facet field names, defaults |
| `TestShaclDocumentBuilding` | 11 | Lucene doc building: TEXT/KEYWORD/INT/LONG/DOUBLE field types, multi-valued, discriminator, null fields, int-from-string |
| `TestShaclTextDocProducer` | 5 | Change listener: add type creates doc, add property rebuilds, delete type removes, irrelevant predicate ignored, multiple entities |
| `TestShaclAssembler` | 4 | Config parsing: valid shapes parsed, EntityDefinition derived, both shapes+entityMap errors, neither errors |
| `TestShaclEntityPerDocument` | 7 | End-to-end: text search, SPARQL query, facet counts, filtered facets, add after load, entity-per-doc model verification |

### Existing Tests (unchanged, verifying no regressions)

327 pre-existing tests covering text search, multilingual support, graph indexing, deletion, analyzers, property lists, etc. All pass.

---

## Test Patterns

### Programmatic setup (no assembler)

Most tests create the index programmatically:

```java
// Define fields
FieldDef titleField = new FieldDef("title", FieldType.TEXT, null,
    true, true, false, false, false, true,
    Collections.singleton(TITLE_PRED));

// Build profile and mapping
IndexProfile profile = new IndexProfile(shapeNode, targetClasses, "uri", "docType", fields);
ShaclIndexMapping mapping = new ShaclIndexMapping(Collections.singletonList(profile));
EntityDefinition defn = ShaclIndexAssembler.deriveEntityDefinition(mapping);

// Build config and index
TextIndexConfig config = new TextIndexConfig(defn);
config.setShaclMapping(mapping);
config.setFacetFields(mapping.getFacetFieldNames());

TextIndexLucene textIndex = new TextIndexLucene(new ByteBuffersDirectory(), config);

// Wire dataset with SHACL producer
ShaclTextDocProducer producer = new ShaclTextDocProducer(baseDs.asDatasetGraph(), textIndex, mapping);
Dataset dataset = TextDatasetFactory.create(baseDs, textIndex, true, producer);
```

### Assembler-based setup

`TestShaclAssembler` builds config in-memory using the Jena Model API:

```java
Resource bookShape = model.createResource(EX + "BookShape")
    .addProperty(model.createProperty(SH, "targetClass"), model.createResource(EX + "Book"))
    .addProperty(model.createProperty(SH, "property"),
        model.createResource()
            .addProperty(model.createProperty(IDX, "fieldName"), "label")
            .addProperty(model.createProperty(IDX, "fieldType"), IndexVocab.TextField)
            .addProperty(model.createProperty(IDX, "defaultSearch"), model.createTypedLiteral(true))
            .addProperty(model.createProperty(SH, "path"), RDFS.label));

RDFNode shapesList = model.createList(new RDFNode[]{ bookShape });

Resource indexSpec = model.createResource(EX + "index")
    .addProperty(RDF.type, TextVocab.textIndexLucene)
    .addProperty(TextVocab.pDirectory, model.createLiteral("mem"))
    .addProperty(TextVocab.pShapes, shapesList);

TextIndexLucene index = (TextIndexLucene) Assembler.general().open(indexSpec);
```

---

## What's Tested vs Not Tested

### Covered

- All SPARQL argument forms for `text:query` and `text:facet`
- JSON filter parsing and semantics (OR within field, AND across fields)
- All five field types (TEXT, KEYWORD, INT, LONG, DOUBLE)
- Multi-valued fields
- Entity lifecycle: create, update (add field), delete (remove type)
- Assembler config parsing (valid and error cases)
- Shared execution between PFs
- Facet count accuracy with filters
- minCount and maxValues options
- Backward compatibility (all 327 existing tests pass)

### Not yet covered (candidates for future tests)

- Named graph support in SHACL mode
- Multiple shapes with overlapping predicates
- Large-scale performance (10k+ entities)
- Concurrent write transactions
- TTL-file-based assembler integration test (currently programmatic only)
- `sh:alternativePath` in assembler config
- Edge cases: empty string values, very long field values, special characters in filters

---

## Adding New Tests

1. Create your test class in `jena-text/src/test/java/org/apache/jena/query/text/`
2. Add it to `TS_Text.java` suite class (Surefire won't find it otherwise)
3. Run: `mvn test -pl jena-text`

For assembler tests, put them in the `assembler` subpackage and import into `TS_Text.java`.
