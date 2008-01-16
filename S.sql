SELECT R_4.lex AS V_4_lex, R_4.datatype AS V_4_datatype, R_4.lang AS V_4_lang, R_4.type AS V_4_type, 
  R_5.lex AS V_5_lex, R_5.datatype AS V_5_datatype, R_5.lang AS V_5_lang, R_5.type AS V_5_type, 
  R_6.lex AS V_6_lex, R_6.datatype AS V_6_datatype, R_6.lang AS V_6_lang, R_6.type AS V_6_type
FROM
    ( SELECT DISTINCT                    -- arq:UnionGraph ?o ?q ?z
                                         -- ?q:(Q_2.p=>SB_2.X_1) ?z:(Q_2.o=>SB_2.X_2) ?o:(Q_2.s=>SB_2.X_3)
        Q_2.p AS X_1, Q_2.o AS X_2, Q_2.s AS X_3
      FROM Quads AS Q_2                  -- arq:UnionGraph ?o ?q ?z
    ) AS SB_2                            -- arq:UnionGraph ?o ?q ?z
                                         -- ?q:(Q_2.p=>SB_2.X_1) ?z:(Q_2.o=>SB_2.X_2) ?o:(Q_2.s=>SB_2.X_3)
  LEFT OUTER JOIN
    Nodes AS R_4                         -- Var: ?z
  ON ( SB_2.X_2 = R_4.hash )
  LEFT OUTER JOIN
    Nodes AS R_5                         -- Var: ?o
  ON ( SB_2.X_3 = R_5.hash )
  LEFT OUTER JOIN
    Nodes AS R_6                         -- Var: ?q
  ON ( SB_2.X_1 = R_6.hash )
