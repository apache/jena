/** Standalone configuration for qonsole on index page */

define( [], function() {
  return {
    prefixes: {
      "rdf":      "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
      "rdfs":     "http://www.w3.org/2000/01/rdf-schema#",
      "owl":      "http://www.w3.org/2002/07/owl#",
      "xsd":      "http://www.w3.org/2001/XMLSchema#"
    },
    queries: [
      { "name": "Selection of triples",
        "query": "select ?subject ?predicate ?object\nwhere {\n" +
                 "  ?subject ?predicate ?object\n}\n" +
                 "limit 25"
      },
      { "name": "Selection of classes",
        "query": "select distinct ?class ?label ?description\nwhere {\n" +
                 "  ?class a owl:Class.\n" +
                 "  optional { ?class rdfs:label ?label}\n" +
                 "  optional { ?class rdfs:comment ?description}\n}\n" +
                 "limit 25"
      }
    ]
  };
} );