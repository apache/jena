PREFIX : <http://example.com/ns#>

INSERT {
    << :a :b :c ~ :iri >> ?P :o2  {| ?Y ?Z |}
} WHERE {
   << :a :b :c ~ :iri >> ?P :o1 {| ?Y ?Z |}
}

