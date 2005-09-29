  s/test-(.)-(..)/test-$1-$2.rq/g;
  s/query-dump/query-dump.rq/g ;
  s!file:[^>]/*!! ;
  s!mf:querySyntax.*$!! ;
