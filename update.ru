PREFIX foo: <http://foo#>
PREFIX inst: <http://foo/instances#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

CREATE SILENT GRAPH <http://foo/model04>

INSERT DATA INTO <http://foo/model04>
{ 
inst:env a foo:Environment .
inst:env foo:hasLocation _:loc .
_:loc a foo:Location .
_:loc foo:indoor 'true' .
}
