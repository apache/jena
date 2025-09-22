PREFIX : <http://example.com/ns#>

INSERT {
    ?S ?P << :a :b ?O~ :iri >> {| ?Y ?Z |}
} WHERE {
   ?S ?P << :a :b ?O~ :iri >> {| ?Y ?Z |}
}
