README
===========

Fork of Apache Jena from tag jena-2.12.1 (version 050c298ada38749a1ff166a77851b963991e4785)

This fork in used to implement scoring access, multi-lingual indexing and multi-index management in **jena-text** (branch multilingual-indexing)



Index creation
--------------

```
Dataset ds = TextDatasetFactory.create(dataset, index);
```


Case 2: On localized index for all the dataset (ex: french)<br/>
```
TextIndex index = TextDatasetFactory.createLuceneIndexLocalized(dir, entDef, "fr");
Dataset ds = TextDatasetFactory.create(dataset, index);
```


Case 3: On multi-lingual index for all the dataset (ex: french, english)<br/>
```
TextIndex index = TextDatasetFactory.createLuceneIndexMultiLingual(dir, entDef, new String[]{"fr", "en"});
Dataset ds = TextDatasetFactory.create(dataset, index);
```


Note: dir parameter permits the usage of multiple index directories

SPARQL full-text clauses
------------------------

The query pattern is
```
(?uri ?score) text:query (property "string" ['graph name'] ['indexed language'])
```
        
example 1: retrieve resources (and scores) with term 'school' in label (unlocalized)
```
(?uri ?score) text:query (rdfs:label 'school')
```

example 2: retrieve resources (and scores) with term 'book' in label on index 'library' related to http://uri/library named graph and unlocalized.
```
(?uri ?score) text:query (rdfs:label 'book' 'library')
```

example 3: retrieve resources (and scores) with term 'book' in label on english index or with term 'livre' in label on french index.
```
{ 
    (?uri ?score) text:query (rdfs:label 'book' 'library' 'en') 
}
UNION
{ 
    (?uri ?score) text:query (rdfs:label 'livre' 'library' 'fr') 
}
```

