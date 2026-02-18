# Testing

## Running Tests

```bash
# Full jena-text suite (366 tests)
mvn test -pl jena-text

# Only SHACL / faceting tests
mvn test -pl jena-text -Dtest="TestShaclIndexMapping,TestShaclDocumentBuilding,TestShaclTextDocProducer,TestShaclAssembler,TestShaclEntityPerDocument,TestNativeFacetCounts,TestTextFacetPF,TestTextQueryPFFilters,TestSearchExecution"
```

All tests run via JUnit 4 and are aggregated in `TS_Text.java` (Surefire only picks up `**/TS_*.java`).

---

## Test Suite Overview

### SHACL Faceting Tests

| Class | Tests | What it covers |
|-------|-------|---------------|
| `TestNativeFacetCounts` | 10 | Java API: open facets, filtered facets, maxValues, minCount, getAllChildren, empty/nonexistent fields |
| `TestTextFacetPF` | 7 | SPARQL `luc:facet` PF: basic counts, multiple fields, filters, maxValues, minCount, maxValues=0 |
| `TestTextQueryPFFilters` | 6 | SPARQL `luc:query` with JSON filters: single filter, multi-field, no matches, JSON parsing |
| `TestSearchExecution` | 6 | Shared execution: key generation, normalisation, reuse across PFs |

### SHACL Entity-Per-Document Tests

| Class | Tests | What it covers |
|-------|-------|---------------|
| `TestShaclIndexMapping` | 8 | Data model: predicate lookup, class lookup, irrelevant predicates, facet field names, defaults |
| `TestShaclDocumentBuilding` | 11 | Lucene doc building: TEXT/KEYWORD/INT/LONG/DOUBLE field types, multi-valued, discriminator, null fields, int-from-string |
| `TestShaclTextDocProducer` | 5 | Change listener: add type creates doc, add property rebuilds, delete type removes, irrelevant predicate ignored, multiple entities |
| `TestShaclAssembler` | 3 | Config parsing: valid shapes parsed, EntityDefinition derived, both shapes+entityMap errors |
| `TestShaclEntityPerDocument` | 7 | End-to-end: text search, SPARQL `luc:query`, facet counts, filtered facets, add after load, entity-per-doc model verification |

### Existing Tests (unchanged, verifying no regressions)

327 pre-existing tests covering text search, multilingual support, graph indexing, deletion, analyzers, property lists, etc. All pass unchanged.

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

- All SPARQL argument forms for `luc:query` and `luc:facet`
- JSON filter parsing and semantics (OR within field, AND across fields)
- All five field types (TEXT, KEYWORD, INT, LONG, DOUBLE)
- Multi-valued fields
- Entity lifecycle: create, update (add field), delete (remove type)
- Assembler config parsing (valid and error cases)
- Shared execution between PFs
- Facet count accuracy with filters
- minCount and maxValues options
- Backward compatibility (all 327 existing tests pass unchanged)

### Not yet covered (candidates for future tests)

- Named graph support in SHACL mode
- Multiple shapes with overlapping predicates
- Large-scale performance (10k+ entities)
- Concurrent write transactions
- TTL-file-based assembler integration test (currently programmatic only)
- `sh:alternativePath` in assembler config
- Edge cases: empty string values, very long field values, special characters in filters

---

## Fuseki Integration Testing

The unit tests above cover the Java API and SPARQL property functions programmatically. For end-to-end testing with a running Fuseki server (HTTP endpoint, data loading, curl queries), see the [Deploying with Fuseki](01-user-guide.md#deploying-with-fuseki) section of the User Guide.

```bash
# Build Fuseki
mvn clean install -pl jena-fuseki2/jena-fuseki-server -am -DskipTests

# Start with a config file
java -jar jena-fuseki2/jena-fuseki-server/target/jena-fuseki-server-*.jar \
    --config config.ttl
```

---

## Adding New Tests

1. Create your test class in `jena-text/src/test/java/org/apache/jena/query/text/`
2. Add it to `TS_Text.java` suite class (Surefire won't find it otherwise)
3. Run: `mvn test -pl jena-text`

For assembler tests, put them in the `assembler` subpackage and import into `TS_Text.java`.
