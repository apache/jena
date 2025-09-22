PREFIX : <http://example.com/ns#>

INSERT {
    ?S ?P <<( :a :b :c )>>  {| ?Y ?Z |}
} WHERE {
   :s ?P <<( :a :b :c )>> {| ?Y ?Z |}
}

