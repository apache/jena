SELECT 
  R$1.lex AS s$lex, R$1.datatype AS s$datatype, R$1.lang AS s$lang, R$1.type AS s$type
  , R$2.lex AS p$lex, R$2.datatype AS p$datatype, R$2.lang AS p$lang, R$2.type AS p$type
  , R$3.lex AS o$lex, R$3.datatype AS o$datatype, R$3.lang AS o$lang, R$3.type AS o$type
FROM
    Triples AS T$1                      -- ?s ?p ?o
  INNER JOIN
    Nodes AS R$1                        -- Var: ?s
  ON ( T$1.s = R$1.id )
  INNER JOIN
    Nodes AS R$2                        -- Var: ?p
  ON ( T$1.p = R$2.id )
  INNER JOIN
    Nodes AS R$3                        -- Var: ?o
  ON ( T$1.o = R$3.id )
