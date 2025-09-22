PREFIX : <http://example.com/ns#>

INSERT {
    :s ?P <<( :a :b :c )>>  {| ?Y <<(:s1 :p1 ?Z)>> |}
} WHERE {
   ?s ?P <<( :a :b :c )>> {| ?Y <<(:s1 :p1 ?Z)>> |}
}

