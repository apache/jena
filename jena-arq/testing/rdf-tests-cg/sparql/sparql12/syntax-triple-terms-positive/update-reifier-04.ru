PREFIX : <http://example.com/ns#>

INSERT {
    << :a :b :c ~ _:bnode >> ?P :o2  {| ?Y <<:s1 :p1 ?Z ~ :iri>> |}
} WHERE {
   << :a :b :c ~ _:bnode >> ?P :o1 {| ?Y <<:s1 :p1 ?Z ~ :iri>> |}
}

