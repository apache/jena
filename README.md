README
===========

Fork of Apache Jena from tag jena-2.12.1 (version 050c298ada38749a1ff166a77851b963991e4785)

This fork in used to implement scoring access, multi-lingual indexing and multi-index management in **jena-text** (branch multilingual-indexing)

Read Jena text documentation first : http://jena.apache.org/documentation/query/text-query.html


Combining Lucene indexes with datasets
--------------

In the following cases, **dir** is a lucene Directory abstract type.
It could be a disk index with 
```
File folder = new File("./myIndex");
Directory dir = FSDirectory.open(folder);
```
or in memory with 
```
Directory dir = new RAMDirectory();
```

**entDef** is the definition of predicates to index.
``` 
EntityDefinition entDef = new EntityDefinition("uri", "text", RDFS.label);
```

Case 1: Standard index
```
TextIndex index = TextDatasetFactory.createLuceneIndex(dir, entDef);
Dataset ds = TextDatasetFactory.create(dataset, index);
```


Case 2: Localized index (ex: french)
```
TextIndex index = TextDatasetFactory.createLuceneIndexLocalized(dir, entDef, "fr");
Dataset ds = TextDatasetFactory.create(dataset, index);
```


Case 3: Multilingual indexes(ex: french, english)
```
TextIndex index = TextDatasetFactory.createLuceneIndexMultiLingual(dir, entDef, new String[]{"fr", "en"});
Dataset ds = TextDatasetFactory.create(dataset, index);
```



SPARQL full-text clauses
------------------------
The following examples mix free text search (on lucene linguistic related indexes) within SPARQL queries . 

The query pattern of full text clauses is
```
(?uri [?score]) text:query (property 'query' ['lang:language'])
```
        
example 1: retrieve resources (and scores) with term 'school' in rdfs label (unlocalized)
```
PREFIX text: <http://jena.apache.org/text#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT ?uri ?label ?score
WHERE {
    (?uri ?score) text:query (rdfs:label 'school') .
    ?uri rdfs:label ?label
}
```

example 2: retrieve resources (without score) with term 'book' in rdfs label on english lucene index.
```
PREFIX text: <http://jena.apache.org/text#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT ?uri ?label
WHERE {
    ?uri text:query (rdfs:label 'book' 'lang:en') .
    ?uri rdfs:label ?label
}
```

example 3: retrieve resources (and scores) with term 'book' in label on english index or with term 'livre' in label on french index.
```
PREFIX text: <http://jena.apache.org/text#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT ?uri ?label ?score
WHERE {
    { 
        (?uri ?score) text:query (rdfs:label 'book' 'lang:en') 
    }
    UNION
    { 
        (?uri ?score) text:query (rdfs:label 'livre' 'lang:fr') 
    }
    ?uri rdfs:label ?label
}
```

