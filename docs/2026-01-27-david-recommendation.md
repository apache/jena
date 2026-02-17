so key things for the jena lucene integration:
1. specification of how to build indexes. this will be an extension to the existing config.ttl jena config
- allow specification of shacl nodeshapes to create an initial set of documents/instances to be indexed - should allow multiple node shapes per index and store the shape that matched as a field (e.g. CarsShape BusesShape) together form the vehicles index and each instsance of car/bus automatically has a field with the shape they matched on to be included in the index
- allow specification of *which* fields to be indexed using SHACL property shapes (this solves the current path through the graph high cost issue)
- allow specification of *how* to index the fields by creating a mini vocab which exposes all lucene options (string, text, numeric etc.), facetable, multi valued etc.)
2. sparql apis
- text:queryWithFacets as you currently have it but we probably need to rename as this one won't return facets.. maybe we just reuse text:query (breaking existing)
- text:facet - need to update the function signature to support: search term, facet fields, facet filters (key value, implement as list of lists in sparql), limit
12m

the shacl parts should look to reuse jenas shacl library and "listen" for changes to triples which impact the index (i.e. covered by the shapes), hopefully there are existing mechanisms for this for other functionality in jena, inference etc.

text:facet ==> input is search term, facet fields, facet filters (key value, implement as list of lists in sparql), limit

text:query ==> fields, input is search term, limit

extend text:query to allow





text:facet ==> input search term, (facet fields), ((field, filter value), (field, filter value), ...), limit


