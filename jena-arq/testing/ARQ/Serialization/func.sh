

function createManifest
{
    local LABEL="$1"
## Header
    cat > manifest.ttl <<EOF
@prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs:	<http://www.w3.org/2000/01/rdf-schema#> .
@prefix mf:     <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> .
@prefix mfx:    <http://jena.hpl.hp.com/2005/05/test-manifest-extra#> .
@prefix qt:     <http://www.w3.org/2001/sw/DataAccess/tests/test-query#> .

<>  rdf:type mf:Manifest ;
    rdfs:comment "Serialization tests $LABEL" ;
    mf:entries
    ( 
EOF
    # Queries good syntax
    for f in *.rq *.arq
      do
      cat >> manifest.ttl <<EOF
      [  mf:name    "$f" ;
         rdf:type   mfx:TestSerialization ;
         mf:action  <$f> ; 
      ]
EOF
    done

## Trailer
    cat >> manifest.ttl <<EOF
    ) .
EOF
}
