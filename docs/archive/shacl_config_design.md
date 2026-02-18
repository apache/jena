# SHACL-driven Lucene indexing configuration (core SHACL)

## NodeShapes

- **Purpose:** define which RDF nodes become Lucene documents (“focus nodes”).
- **Targets ⇒ focus nodes:** `sh:targetClass`, `sh:targetSubjectsOf`, `sh:targetObjectsOf`, `sh:targetNode`, etc.

### Auto discriminator (`docType` / FocusNodeDiscriminator)

- Default derived from the NodeShape’s target(s).
- If **exactly one** `sh:targetClass` → `docType = that class` (qname or IRI).
- If **multiple target classes / mixed target kinds** → emit **multi-valued** `docType` (or `docTypePrimary + docTypeMulti`), so global search can facet/filter cleanly.
- If not class-based (subjectsOf/objectsOf/node) → use a stable label like  
  `subjectsOf:<p>`, `objectsOf:<p>`, `shape:<shapeIRI>` unless explicitly overridden.

## PropertyShapes

- **Purpose:** define how to extract values from a focus node.
- **Core:** `sh:path` (or more complex SHACL paths) → yields literals/strings (or things you string-ify).
- You can use **multiple PropertyShapes** to feed one logical search field (your “all_text” union idea):  
  `all_text = values from rdfs:label + skos:prefLabel + schema:description + …`
- Not every doc needs every path; missing values are fine.

## Do you need a “Field” abstraction?

If you want “multiple paths per field” and analyzers/boosts per field: **yes**. SHACL PropertyShapes alone don’t naturally express “these 6 paths all populate the same Lucene field with analyzer X”.

### Minimal clean model

- **NodeShape** (doc profile)
  - links to **Field** definitions
- **Field** (Lucene field config)
  - has `fieldName`, analyzer/boost, etc.
  - links to one or more **PropertyShapes** (each provides a `sh:path`)
- **PropertyShape**
  - just the path + optional constraints (lang filter, maxCount, etc.)

**Structure**
- 1 NodeShape → many Fields  
- 1 Field → many PropertyShapes  
  (PropertyShapes can be reused across fields if you want, but most people won’t need that.)

**This gives you**
- global search: query `all_text`
- type filters: query `docType`
- IDs: `identifier_ngram` fed by multiple identifier paths

If you want it even simpler: skip reusable PropertyShapes and let Field directly carry multiple `sh:path` values. But if you want to stay “SHACL-ish”, Field → PropertyShapes is the cleanest.

---

# Example Turtle configuration

```turtle
@prefix sh:   <http://www.w3.org/ns/shacl#> .
@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix dct:  <http://purl.org/dc/terms/> .
@prefix sdo:  <https://schema.org/> .
@prefix ex:   <https://example.com/> .

# Indexing vocabulary (project-agnostic)
@prefix idx:  <urn:jena:lucene:index#> .

#####################################################################
# Analyzer + field-type identifiers (you map these to Lucene)
#####################################################################

idx:TextField    a idx:FieldType .     # analyzed full text
idx:KeywordField a idx:FieldType .     # exact keyword (no analysis)

idx:Standard     a idx:Analyzer .      # StandardAnalyzer (example)
idx:Keyword      a idx:Analyzer .      # KeywordAnalyzer
idx:EdgeNGram    a idx:Analyzer .      # EdgeNGram analyzer for prefix matching

#####################################################################
# NodeShape: focus nodes are instances of either class (union)
#####################################################################

ex:EntityIndexShape
  a sh:NodeShape, idx:IndexProfile ;

  sh:targetClass ex:Car ;
  sh:targetClass ex:Person ;

  # Document identity + discriminator fields
  idx:docIdField "iri" ;
  idx:discriminatorField "docType" ;

  # Fields attached to this profile
  idx:field ex:AllTextField ;
  idx:field ex:LabelField ;
  idx:field ex:IdentifierExactField ;
  idx:field ex:IdentifierPrefixField .

#####################################################################
# Field: all_text (global search field)
# Uses core SHACL path union via sh:alternativePath.
#####################################################################

ex:AllTextField
  a idx:Field ;
  idx:fieldName "all_text" ;
  idx:fieldType idx:TextField ;
  idx:analyzer idx:Standard ;

  # One extraction path that is a union of multiple paths
  idx:path [
    sh:alternativePath (
      rdfs:label
      skos:prefLabel
      skos:altLabel
      sdo:name
      dct:description
      sdo:description
      rdfs:comment
    )
  ] .

#####################################################################
# Field: label (more targeted field, can be boosted at query time)
#####################################################################

ex:LabelField
  a idx:Field ;
  idx:fieldName "label" ;
  idx:fieldType idx:TextField ;
  idx:analyzer idx:Standard ;

  idx:path [
    sh:alternativePath (
      rdfs:label
      skos:prefLabel
      sdo:name
    )
  ] .

#####################################################################
# ID fields: exact + prefix/autocomplete
# Same source paths, different analyzers.
#####################################################################

ex:IdentifierExactField
  a idx:Field ;
  idx:fieldName "identifier_exact" ;
  idx:fieldType idx:KeywordField ;
  idx:analyzer idx:Keyword ;

  idx:path [
    sh:alternativePath (
      dct:identifier
      sdo:identifier
      ex:internalId
    )
  ] .

ex:IdentifierPrefixField
  a idx:Field ;
  idx:fieldName "identifier_ngram" ;
  idx:fieldType idx:TextField ;
  idx:analyzer idx:EdgeNGram ;

  idx:path [
    sh:alternativePath (
      dct:identifier
      sdo:identifier
      ex:internalId
    )
  ] .
```
Notes (implementation expectations)
sh:targetClass entries are unioned to select focus nodes.

idx:path values are evaluated as SHACL paths; values from each path are added to the field (no “must exist” requirement).

docType is auto-populated from the focus node’s rdf:type that matches the NodeShape’s target(s), emitting multiple values if applicable.

Global search queries should primarily hit all_text, with optional boosting for label, identifier_exact, and identifier_ngram.
