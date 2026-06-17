# Semantic SPARQL

Semantic SPARQL is a fork of [Apache Jena](https://jena.apache.org/), the Java
framework for building Semantic Web and SPARQL applications.

This fork keeps the Apache Jena codebase and adds search-oriented capabilities
centered on:

- full-text search via `jena-text`
- vector similarity search via `jena-vector`
- hybrid search across text and vector indexes

Upstream Apache Jena source and project documentation remain available at:

- https://github.com/apache/jena
- https://jena.apache.org/

## Vector And Hybrid Search Guide

This guide shows how to run the `jena-vector` module through the
Fuseki server UI, using a local OpenAI-compatible Ollama embeddings endpoint.

`jena-vector` is separate from `jena-text`. It provides semantic vector search
over RDF literals by embedding configured text predicates synchronously during
data updates, storing vectors in a Lucene vector index, and exposing search via
the SPARQL property function `vector:query`.

## Current MVP Scope

- One configured text predicate per vector index.
- One vector dimension per vector index.
- Synchronous embedding calls during data load/update and query execution.
- OpenAI-compatible `/v1/embeddings` endpoints.
- API keys are read only from environment variables.
- Local unauthenticated endpoints, such as Ollama, work without `vector:apiKeyEnv`.
- Indexed result subjects are expected to be URI resources.
- The Fuseki UI does not have vector-specific screens yet; use the query/update/upload UI.

## Build Fuseki With Vector Support

The shaded Fuseki server jar must include `jena-vector`. From the repository root:

```bash
mvn -pl :jena-fuseki-server -am -Pdev,ui-skip-tests -DskipTests -Dmaven.javadoc.skip=true package
```

The runnable jar is:

```bash
jena-fuseki2/jena-fuseki-server/target/jena-fuseki-server-6.2.0-SNAPSHOT.jar
```

## Local Embeddings Using Ollama

Install and start Ollama, then pull an embedding model:

```bash
ollama pull nomic-embed-text
```

Ollama normally exposes an OpenAI-compatible endpoint at:

```text
http://localhost:11434/v1/embeddings
```

Quick smoke test:

```bash
curl http://localhost:11434/v1/embeddings \
  -H 'Content-Type: application/json' \
  -d '{"model":"nomic-embed-text","input":["hello world"],"encoding_format":"float"}'
```

`nomic-embed-text` returns 768-dimensional vectors, so the Fuseki config below uses `vector:dimension 768`.

## Fuseki Config: In-Memory Dataset

Create `vector-fuseki.ttl`:

```turtle
PREFIX :       <#>
PREFIX fuseki: <http://jena.apache.org/fuseki#>
PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ja:     <http://jena.hpl.hp.com/2005/11/Assembler#>
PREFIX vector: <http://jena.apache.org/vector#>

[] rdf:type fuseki:Server ;
   fuseki:services ( :service ) .

:service rdf:type fuseki:Service ;
    fuseki:name "vector" ;
    fuseki:endpoint [ fuseki:operation fuseki:query ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:update ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:gsp-rw ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:query ; fuseki:name "sparql" ] ;
    fuseki:endpoint [ fuseki:operation fuseki:update ; fuseki:name "update" ] ;
    fuseki:endpoint [ fuseki:operation fuseki:gsp-rw ; fuseki:name "data" ] ;
    fuseki:dataset :vectorDataset .

:vectorDataset rdf:type vector:VectorDataset ;
    vector:dataset :baseDataset ;
    vector:index :vectorIndex .

:baseDataset rdf:type ja:MemoryDataset .

:vectorIndex rdf:type vector:VectorIndexLucene ;
    vector:directory "mem" ;
    vector:dimension 768 ;
    vector:similarity vector:cosine ;
    vector:textPredicate rdfs:label ;
    vector:embeddingProvider :ollamaEmbeddings .

:ollamaEmbeddings rdf:type vector:OpenAICompatibleEmbeddings ;
    vector:endpoint "http://localhost:11434/v1" ;
    vector:model "nomic-embed-text" ;
    vector:batchSize 8 .
```

Run Fuseki:

```bash
java -jar jena-fuseki2/jena-fuseki-server/target/jena-fuseki-server-6.2.0-SNAPSHOT.jar --conf vector-fuseki.ttl
```

Open the UI:

```text
http://localhost:3030/
```

Dataset name:

```text
/vector
```

## Fuseki Config: Persistent TDB2 Dataset And Disk Vector Index

Use this when you want RDF data and Lucene vectors to survive restarts.

```turtle
PREFIX :       <#>
PREFIX fuseki: <http://jena.apache.org/fuseki#>
PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>
PREFIX tdb2:   <http://jena.apache.org/2016/tdb#>
PREFIX vector: <http://jena.apache.org/vector#>

[] rdf:type fuseki:Server ;
   fuseki:services ( :service ) .

:service rdf:type fuseki:Service ;
    fuseki:name "vector" ;
    fuseki:endpoint [ fuseki:operation fuseki:query ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:update ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:gsp-rw ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:query ; fuseki:name "sparql" ] ;
    fuseki:endpoint [ fuseki:operation fuseki:update ; fuseki:name "update" ] ;
    fuseki:endpoint [ fuseki:operation fuseki:gsp-rw ; fuseki:name "data" ] ;
    fuseki:dataset :vectorDataset .

:vectorDataset rdf:type vector:VectorDataset ;
    vector:dataset :baseDataset ;
    vector:index :vectorIndex .

:baseDataset rdf:type tdb2:DatasetTDB ;
    tdb2:location "databases/vector-tdb2" .

:vectorIndex rdf:type vector:VectorIndexLucene ;
    vector:directory "databases/vector-lucene" ;
    vector:dimension 768 ;
    vector:similarity vector:cosine ;
    vector:textPredicate rdfs:label ;
    vector:embeddingProvider :ollamaEmbeddings .

:ollamaEmbeddings rdf:type vector:OpenAICompatibleEmbeddings ;
    vector:endpoint "http://localhost:11434/v1" ;
    vector:model "nomic-embed-text" ;
    vector:batchSize 8 .
```

## Index Data

Indexing happens synchronously when matching triples are added to the dataset.

With the configs above, only triples using `rdfs:label` are embedded and indexed:

```turtle
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ex:   <http://example.org/resource/>

ex:item1 rdfs:label "Apache Jena is a Java framework for RDF and SPARQL." .
ex:item2 rdfs:label "Ollama runs local language and embedding models." .
ex:item3 rdfs:label "A bicycle is a human-powered vehicle with two wheels." .
```

You can load this data in the Fuseki UI:

- Go to `http://localhost:3030/`.
- Open dataset `vector`.
- Use the upload page to upload Turtle data, or use the update page.

You can also use SPARQL Update from the UI or command line:

```sparql
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ex:   <http://example.org/resource/>

INSERT DATA {
  ex:item1 rdfs:label "Apache Jena is a Java framework for RDF and SPARQL." .
  ex:item2 rdfs:label "Ollama runs local language and embedding models." .
  ex:item3 rdfs:label "A bicycle is a human-powered vehicle with two wheels." .
}
```

Command line update:

```bash
curl http://localhost:3030/vector/update \
  -H 'Content-Type: application/sparql-update' \
  --data-binary @- <<'EOF'
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ex:   <http://example.org/resource/>

INSERT DATA {
  ex:item1 rdfs:label "Apache Jena is a Java framework for RDF and SPARQL." .
  ex:item2 rdfs:label "Ollama runs local language and embedding models." .
  ex:item3 rdfs:label "A bicycle is a human-powered vehicle with two wheels." .
}
EOF
```

Each inserted `rdfs:label` causes a synchronous call to Ollama. Large loads will be as fast as your embedding endpoint.

## Query From The Fuseki UI

Open the query page for dataset `vector`, then run:

```sparql
PREFIX vector: <http://jena.apache.org/vector#>

SELECT ?s ?score {
  (?s ?score) vector:query ("local embeddings server" 5) .
}
ORDER BY DESC(?score)
```

The query string is embedded synchronously using the same configured embedding provider. Results are nearest-neighbor matches from the Lucene vector index.

You can join vector results back to RDF data:

```sparql
PREFIX vector: <http://jena.apache.org/vector#>
PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?s ?label ?score {
  (?s ?score) vector:query ("semantic web RDF query engine" 10) .
  ?s rdfs:label ?label .
}
ORDER BY DESC(?score)
```

Command line query:

```bash
curl 'http://localhost:3030/vector/sparql' \
  -H 'Accept: application/sparql-results+json' \
  --data-urlencode 'query=
PREFIX vector: <http://jena.apache.org/vector#>
PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?s ?label ?score {
  (?s ?score) vector:query ("semantic web RDF query engine" 10) .
  ?s rdfs:label ?label .
}
ORDER BY DESC(?score)
'
```

## Delete And Reindex

Deleting a matching `rdfs:label` triple removes the corresponding vector document:

```sparql
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ex:   <http://example.org/resource/>

DELETE DATA {
  ex:item3 rdfs:label "A bicycle is a human-powered vehicle with two wheels." .
}
```

Updating a label can be done as a delete plus insert:

```sparql
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ex:   <http://example.org/resource/>

DELETE {
  ex:item2 rdfs:label ?oldLabel .
}
INSERT {
  ex:item2 rdfs:label "Ollama provides local OpenAI-compatible embedding models." .
}
WHERE {
  ex:item2 rdfs:label ?oldLabel .
}
```

The inserted replacement label is embedded immediately.

## Using OpenAI Or OpenRouter Instead Of Ollama

Set `vector:endpoint` and `vector:model` for your provider, and use `vector:apiKeyEnv` to name the environment variable that contains the key.

Example:

```turtle
:openaiEmbeddings rdf:type vector:OpenAICompatibleEmbeddings ;
    vector:endpoint "https://api.openai.com/v1" ;
    vector:model "text-embedding-3-small" ;
    vector:apiKeyEnv "OPENAI_API_KEY" ;
    vector:batchSize 8 .
```

Start Fuseki with the key in the environment:

```bash
OPENAI_API_KEY='...' java -jar jena-fuseki2/jena-fuseki-server/target/jena-fuseki-server-6.2.0-SNAPSHOT.jar --conf vector-fuseki.ttl
```

Do not put API keys directly in the assembler file.

## Create Vector And Hybrid Datasets At Runtime

Fuseki's admin API can create new datasets while the server is already running.

There are two creation modes:

- `dbName` + `dbType` form posts create only built-in plain `mem`, `tdb`, or `tdb2` datasets.
- Full assembler config posts can create wrapped datasets such as `vector:VectorDataset`, `text:TextDataset`, or a hybrid text+vector stack.

To create vector or hybrid datasets at runtime, post a Turtle assembler description to `/$/datasets`.

### Enable Runtime Config Uploads

Config-body dataset creation is disabled by default. Start Fuseki with:

```bash
java -Dfuseki:allowAddByConfigFile=true \
  -jar jena-fuseki2/jena-fuseki-server/target/jena-fuseki-server-6.2.0-SNAPSHOT.jar
```

If you also need an embeddings API key, include it in the environment when starting Fuseki:

```bash
OPENAI_API_KEY='...' \
java -Dfuseki:allowAddByConfigFile=true \
  -jar jena-fuseki2/jena-fuseki-server/target/jena-fuseki-server-6.2.0-SNAPSHOT.jar
```

The posted service is registered immediately in the running server and its assembler file is also saved in the Fuseki configuration area for restart persistence.

### Create A Vector Dataset At Runtime

This creates a persistent TDB2 dataset with an on-disk Lucene vector index named `/vector-runtime`.

```bash
curl -X POST 'http://localhost:3030/$/datasets' \
  -H 'Content-Type: text/turtle' \
  --data-binary @- <<'EOF'
PREFIX :       <#>
PREFIX fuseki: <http://jena.apache.org/fuseki#>
PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>
PREFIX tdb2:   <http://jena.apache.org/2016/tdb#>
PREFIX vector: <http://jena.apache.org/vector#>

:service rdf:type fuseki:Service ;
    fuseki:name "vector-runtime" ;
    fuseki:endpoint [ fuseki:operation fuseki:query ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:update ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:gsp-rw ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:query ; fuseki:name "sparql" ] ;
    fuseki:endpoint [ fuseki:operation fuseki:update ; fuseki:name "update" ] ;
    fuseki:endpoint [ fuseki:operation fuseki:gsp-rw ; fuseki:name "data" ] ;
    fuseki:dataset :vectorDataset .

:vectorDataset rdf:type vector:VectorDataset ;
    vector:dataset :baseDataset ;
    vector:index :vectorIndex .

:baseDataset rdf:type tdb2:DatasetTDB ;
    tdb2:location "databases/vector-runtime-tdb2" .

:vectorIndex rdf:type vector:VectorIndexLucene ;
    vector:directory "databases/vector-runtime-lucene" ;
    vector:dimension 768 ;
    vector:similarity vector:cosine ;
    vector:textPredicate rdfs:label ;
    vector:embeddingProvider :ollamaEmbeddings .

:ollamaEmbeddings rdf:type vector:OpenAICompatibleEmbeddings ;
    vector:endpoint "http://localhost:11434/v1" ;
    vector:model "nomic-embed-text" ;
    vector:batchSize 8 .
EOF
```

The dataset is then available at:

```text
/vector-runtime
```

### Create A Hybrid Text+Vector Dataset At Runtime

This creates a persistent hybrid dataset named `/hybrid-runtime` by wrapping a `text:TextDataset` with a `vector:VectorDataset`.

```bash
curl -X POST 'http://localhost:3030/$/datasets' \
  -H 'Content-Type: text/turtle' \
  --data-binary @- <<'EOF'
PREFIX :       <#>
PREFIX fuseki: <http://jena.apache.org/fuseki#>
PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>
PREFIX tdb2:   <http://jena.apache.org/2016/tdb#>
PREFIX text:   <http://jena.apache.org/text#>
PREFIX vector: <http://jena.apache.org/vector#>

:service rdf:type fuseki:Service ;
    fuseki:name "hybrid-runtime" ;
    fuseki:endpoint [ fuseki:operation fuseki:query ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:update ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:gsp-rw ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:query ; fuseki:name "sparql" ] ;
    fuseki:endpoint [ fuseki:operation fuseki:update ; fuseki:name "update" ] ;
    fuseki:endpoint [ fuseki:operation fuseki:gsp-rw ; fuseki:name "data" ] ;
    fuseki:dataset :hybridDataset .

:hybridDataset rdf:type vector:VectorDataset ;
    vector:dataset :textDataset ;
    vector:index :vectorIndex .

:textDataset rdf:type text:TextDataset ;
    text:dataset :baseDataset ;
    text:index :textIndex .

:baseDataset rdf:type tdb2:DatasetTDB ;
    tdb2:location "databases/hybrid-runtime-tdb2" .

:textIndex rdf:type text:TextIndexLucene ;
    text:directory "databases/hybrid-runtime-text-lucene" ;
    text:entityMap :textEntityMap .

:textEntityMap rdf:type text:EntityMap ;
    text:entityField "uri" ;
    text:defaultField "contents" ;
    text:map (
      [ text:field "contents" ; text:predicate rdfs:label ]
    ) .

:vectorIndex rdf:type vector:VectorIndexLucene ;
    vector:directory "databases/hybrid-runtime-vector-lucene" ;
    vector:dimension 768 ;
    vector:similarity vector:cosine ;
    vector:textPredicate rdfs:label ;
    vector:embeddingProvider :ollamaEmbeddings .

:ollamaEmbeddings rdf:type vector:OpenAICompatibleEmbeddings ;
    vector:endpoint "http://localhost:11434/v1" ;
    vector:model "nomic-embed-text" ;
    vector:batchSize 8 .
EOF
```

The dataset is then available at:

```text
/hybrid-runtime
```

### Manage Runtime-Created Datasets

List datasets:

```bash
curl 'http://localhost:3030/$/datasets'
```

Delete a runtime-created dataset:

```bash
curl -X DELETE 'http://localhost:3030/$/datasets/hybrid-runtime'
```

For custom runtime-created hybrid datasets, deleting the dataset removes it from the running server and deletes its saved assembler config. Keep your TDB2 and Lucene index directory layout predictable so cleanup is easy if you ever need to remove files manually.

## Hybrid Full-Text And Vector Search

`jena-hybrid-search` combines `jena-text` and `jena-vector` results with Reciprocal Rank Fusion (RRF). It does not create another index. It queries both existing indexes, assigns rank positions in each result list, and computes:

```text
hybridScore = textWeight / (rrfK + textRank) + vectorWeight / (rrfK + vectorRank)
```

The default `rrfK` is `60`.

### Hybrid Fuseki Config

Configure a text dataset wrapped by a vector dataset. The Fuseki service should point to the outer dataset.

This in-memory config matches `hybrid-fuseki.ttl`. The text index uses `uri` as the entity field and stores indexed literal content in the `contents` field.

```turtle
PREFIX :       <#>
PREFIX fuseki: <http://jena.apache.org/fuseki#>
PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ja:     <http://jena.hpl.hp.com/2005/11/Assembler#>
PREFIX text:   <http://jena.apache.org/text#>
PREFIX vector: <http://jena.apache.org/vector#>

[] rdf:type fuseki:Server ;
   fuseki:services ( :service ) .

:service rdf:type fuseki:Service ;
    fuseki:name "hybrid" ;
    fuseki:endpoint [ fuseki:operation fuseki:query ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:update ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:gsp-rw ; ] ;
    fuseki:endpoint [ fuseki:operation fuseki:query ; fuseki:name "sparql" ] ;
    fuseki:endpoint [ fuseki:operation fuseki:update ; fuseki:name "update" ] ;
    fuseki:endpoint [ fuseki:operation fuseki:gsp-rw ; fuseki:name "data" ] ;
    fuseki:dataset :hybridDataset .

:hybridDataset rdf:type vector:VectorDataset ;
    vector:dataset :textDataset ;
    vector:index :vectorIndex .

:textDataset rdf:type text:TextDataset ;
    text:dataset :baseDataset ;
    text:index :textIndex .

:baseDataset rdf:type ja:MemoryDataset .

:textIndex rdf:type text:TextIndexLucene ;
    text:directory "mem" ;
    text:entityMap :textEntityMap .

:textEntityMap rdf:type text:EntityMap ;
    text:entityField "uri" ;
    text:defaultField "contents" ;
    text:map (
      [ text:field "contents" ; text:predicate rdfs:label ]
    ) .

:vectorIndex rdf:type vector:VectorIndexLucene ;
    vector:directory "mem" ;
    vector:dimension 768 ;
    vector:similarity vector:cosine ;
    vector:textPredicate rdfs:label ;
    vector:embeddingProvider :ollamaEmbeddings .

:ollamaEmbeddings rdf:type vector:OpenAICompatibleEmbeddings ;
    vector:endpoint "http://localhost:11434/v1" ;
    vector:model "nomic-embed-text" ;
    vector:batchSize 8 .
```

Run it with:

```bash
java -jar jena-fuseki2/jena-fuseki-server/target/jena-fuseki-server-6.2.0-SNAPSHOT.jar --conf hybrid-fuseki.ttl
```

### Persistent Hybrid Config

For persistent RDF storage plus on-disk text and vector indexes, use `hybrid-fuseki-tdb2.ttl`.

- TDB2 dataset: `databases/hybrid-tdb2`
- text Lucene index: `databases/hybrid-text-lucene`
- vector Lucene index: `databases/hybrid-vector-lucene`

Run it with:

```bash
java -jar jena-fuseki2/jena-fuseki-server/target/jena-fuseki-server-6.2.0-SNAPSHOT.jar --conf hybrid-fuseki-tdb2.ttl
```

### Hybrid Query

Use `hybrid:query` from the Fuseki UI query page:

```sparql
PREFIX hybrid: <http://jena.apache.org/hybrid#>
PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?s ?label ?score ?textRank ?vectorRank {
  (?s ?score ?textRank ?vectorRank) hybrid:query (rdfs:label "semantic web RDF engine" 10) .
  ?s rdfs:label ?label .
}
ORDER BY DESC(?score)
```

Detailed form with raw scores:

```sparql
PREFIX hybrid: <http://jena.apache.org/hybrid#>
PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?s ?label ?score ?textRank ?vectorRank ?textScore ?vectorScore {
  (?s ?score ?textRank ?vectorRank ?textScore ?vectorScore)
    hybrid:query (rdfs:label "semantic web RDF engine" 10) .
  ?s rdfs:label ?label .
}
ORDER BY DESC(?score)
```

Advanced form:

```sparql
PREFIX hybrid: <http://jena.apache.org/hybrid#>
PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?s ?score ?textRank ?vectorRank {
  (?s ?score ?textRank ?vectorRank)
    hybrid:query (rdfs:label "semantic web RDF engine" 10 100 60 1.0 1.0) .
}
ORDER BY DESC(?score)
```

Arguments are:

- `rdfs:label`: indexed property.
- `"semantic web RDF engine"`: query text.
- `10`: final result limit.
- `100`: candidate limit from each index.
- `60`: RRF `k`.
- `1.0`: full-text weight.
- `1.0`: vector weight.

## Troubleshooting

- If Fuseki starts but `vector:query` is unknown, rebuild `jena-fuseki-server` and verify `jena-vector` is included in the shaded jar.
- If Fuseki starts but `hybrid:query` is unknown, rebuild `jena-fuseki-server` and verify `jena-hybrid-search` is included in the shaded jar.
- If indexing fails with a dimension error, check the embedding model dimension and `vector:dimension`.
- If inserts are slow, the embedding endpoint is the bottleneck; indexing is synchronous by design.
- If queries return no results, confirm you inserted triples with the configured `vector:textPredicate`.
- If using Ollama, confirm `curl http://localhost:11434/v1/embeddings` works before starting Fuseki.
