PREFIX : <http://example.com/ns#>

DELETE {
    ?s :r ?o~ :iri {| :added 'Property :r' |}
} WHERE {
   ?s :p ?o~ :iri {| :q1+ 'ABC' |}
}
