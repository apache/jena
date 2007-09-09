# Simple example of making a SPARQL query directly

require 'java'

include_class 'com.hp.hpl.jena.query.ARQ'
include_class 'com.hp.hpl.jena.query.QueryExecutionFactory'
include_class 'com.hp.hpl.jena.query.QueryFactory'
include_class 'com.hp.hpl.jena.query.ResultSetFormatter'
include_class 'com.hp.hpl.jena.util.FileManager'
include_class 'com.hp.hpl.jena.rdf.model.ModelFactory'
include_class 'com.hp.hpl.jena.datatypes.xsd.XSDDatatype'
include_class 'java.lang.System'

## Configure
# Example: turn on Roman numerals (datatype URI is
# "http://rome.example.org/Numeral")
ARQ.getContext().setTrue(ARQ.enableRomanNumerals)

romanDT = "http://rome.example.org/Numeral"
XSD = "http://www.w3.org/2001/XMLSchema#"

model = ModelFactory.createDefaultModel
r  = model.createResource()
p1 = model.createProperty("http://example/p1")
p2 = model.createProperty("http://example/p2")

o11 = model.createTypedLiteral("I", romanDT)
o21 = model.createTypedLiteral("II", romanDT)
# NB Not: plain 2 as JRuby finds the char method first
o12 = model.createTypedLiteral(1, XSD+"integer")
o22 = model.createTypedLiteral(2, XSD+"integer")

model.add(r,p1,o11)
model.add(r,p1,o21)
model.add(r,p2,o12)
model.add(r,p2,o22)

qs = <<-"END"
  SELECT ?o
  WHERE
  { ?s ?p ?o . FILTER (?o = 2) 
  }
  END

q = QueryFactory.create(qs)

## # Print query
puts q.to_s
## puts '-------------------'
## 
## # Output model (note use of Java's System.out)
## model.write(System.out,"N3") ;
## puts '-------------------'

begin
  # Make the query and do something with the results
  # ensuring that the query execution is closed explicitly.
  qe = QueryExecutionFactory.create(q, model)
  ResultSetFormatter.out(qe.execSelect())
ensure
  qe.close unless qe.nil?
end
