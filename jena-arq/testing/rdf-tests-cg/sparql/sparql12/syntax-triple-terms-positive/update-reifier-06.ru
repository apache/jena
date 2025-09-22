PREFIX : <http://example.com/ns#>

INSERT {
    ?s :r ?o~ :iri {| :added 'Property :r' |}
} WHERE {
   ?s :p/:q ?o .
}
