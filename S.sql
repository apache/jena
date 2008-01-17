SELECT                                   -- V_1=?s V_2=?p
  R_1.lex AS V_1_lex, R_1.datatype AS V_1_datatype, R_1.lang AS V_1_lang, R_1.type AS V_1_type, 
  R_2.lex AS V_2_lex, R_2.datatype AS V_2_datatype, R_2.lang AS V_2_lang, R_2.type AS V_2_type
FROM
    ( SELECT *
      FROM Nodes AS N_1                  -- Const: "ABC"
      WHERE ( N_1.hash = -4576001914283577307 )
    ) AS N_1
  INNER JOIN
    ( SELECT *
      FROM Triples AS T_1                -- ?s ?p "ABC"
      WHERE ( T_1.o = N_1.id             -- Const condition: "ABC"
         )
    ) AS T_1
  ON  ( 1 = 1 )
  LEFT OUTER JOIN
    Nodes AS R_1                         -- Var: ?s
  ON ( T_1.s = R_1.id )
  LEFT OUTER JOIN
    Nodes AS R_2                         -- Var: ?p
  ON ( T_1.p = R_2.id )
