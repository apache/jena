Final-for-spec-publication versions of the grammar.

sparql_10-final.jj       - SPARQL 1.0 "sparql_10.jj" ("main.jj" after cpp)
sparql_11-final.jj	     - SPARQL 1.1 "sparql_11.jj" ("main.jj" after cpp)
sparql-main-11.jj        - SPARQL 1.1 "main.jj" (com.hp)


sparql_11-dev-final.jj   - End SPARQL 1.1 development.  (org.apache.jena.graph "main.jj" at SPARQLParser11)
sparql_11-dev-final.txt  - jjdoc
tokens_11.txt            - Tokens file.

-- Coming soon.
sparql_12-final.jj       - SPARQL 1.2 "sparql_11.jj" ("main.jj" after cpp)
sparql-main-12.jj        - SPARQL 1.2 "main.jj"

-- Notes

SPARQL 1.1:
* Class "E_Conditional" became "E_If" 
  - java legacy files in the codebase updated
  - jj files not updated.
