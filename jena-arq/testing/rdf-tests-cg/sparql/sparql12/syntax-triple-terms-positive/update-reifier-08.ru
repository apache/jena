PREFIX : <http://example.com/ns#>

DELETE DATA  {
    :s :p :o1 ~ :iri  {| :added 'Test' |}
}
;
INSERT DATA  {
    :s :p :o2 ~ :iri  {| :added 'Test' |}
}


