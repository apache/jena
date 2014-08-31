// Encoding in Thrift iof RDF terms and other items 
// for Graph, Datasets, Result Set and Patches

// Versioning considerations?

namespace java org.apache.jena.riot.thrift.wire

// ==== RDF Term Definitions 

struct RDF_IRI {
1: required string iri
}

# A prefix name (abbrev for an IRI)
struct RDF_PrefixName {
1: required string prefix ;
2: required string localName ;
}

struct RDF_BNode {
  // Maybe support (or even insist) on a global unique identifier e.g. UUID
  // long mostSig
  // long leastSig
1: required string label
}

// Common abbreviated for datatypes and other URIs?
// union with additional values. 

struct RDF_Literal {
1: required string  lex ;
2: optional string  langtag ;
3: optional string  datatype ;          // Either 3 or 4 but UNION is heavy.
4: optional RDF_PrefixName dtPrefix ;   // datatype as prefix name
}

struct RDF_Decimal {
1: required i64  value ;
2: required i32  scale ;
}

struct RDF_VAR {
1: required string name ;
}

struct RDF_ANY { }

struct RDF_UNDEF { }

struct RDF_REPEAT { }

union RDF_Term {
1: RDF_IRI          iri
2: RDF_BNode        bnode
3: RDF_Literal      literal     # Full form lexical form/datattype/langtag
4: RDF_PrefixName   prefixName 
5: RDF_VAR          variable
6: RDF_ANY          any
7: RDF_UNDEF        undefined
8: RDF_REPEAT       repeat
# Value forms of literals.
10: i64             valInteger
11: double          valDouble
12: RDF_Decimal     valDecimal
}

// === Stream RDF items 

struct RDF_Triple {
1: required RDF_Term S
2: required RDF_Term P
3: required RDF_Term O
}

struct RDF_Quad {
1: required RDF_Term S
2: required RDF_Term P
3: required RDF_Term O
4: optional RDF_Term G
}

# Prefix declaration
struct RDF_PrefixDecl {
1: required string prefix ;
2: required string uri ;
}

union RDF_StreamRow {
# No base - no relative URI resolution.
1: RDF_PrefixDecl   prefixDecl
2: RDF_Triple       triple
3: RDF_Quad         quad
}

// ==== SPARQL Result Sets

struct RDF_VarTuple {
1: list<RDF_VAR> vars
}

struct RDF_DataTuple {
1: list<RDF_Term> row
}

// // ==== RDF Patch
// 
// # Includes 
// # Prefix declaration
// 
// enum RDF_Patch {
//      ADD, 
//      ADD_NO_OP,         // ADD recorded that had no effect
//      DELETE, 
//      DELETE_NO_OP       // DELETE recorded that had no effect
// }

// Local Variables:
// tab-width: 2
// indent-tabs-mode: nil
// comment-default-style: "//"
// End:
