

:-op(1200,xfx,'::=').
:- dynamic (::=)/2.
:- discontiguous (::=)/2.

terminal(naturalNumber).

%type ::= description.


axiom ::= enumeratedClass( c, 
                    [+deprecated], { annotation }, {i} ) .

axiom ::= disjointClasses( description, description ).
axiom ::= subClassOf( description, description ).

%description ::= c.
%description ::= restriction.
description ::= unionOf( {description} ).
description ::= intersectionOf( {description} ).
description ::= complementOf( description ).
description ::= oneOf( { i } ).


restriction ::= restriction( cdp, hasValue(dataLiteral) ).
restriction ::= restriction( cop, hasValue( i ) ).
restriction ::= restriction( tp, hasValue( i ) ).

smallInt ::= naturalNumber .

comment(note,
   ['These rules broadens the scope of restrictions, domain and range conditions.'] ).
classInRestriction ::= description .

dataRange ::= dataRange( {dataLiteral} ).
/*
comment(rationale,
  ['Using the same dataRange in multiple range constraints
    of restrictions requires too much repetition without 
    allowing ',link(dr),'.']).
axiom ::= dataRange( dr, {annotation}, {dataLiteral} ).
comment(rationale,
  ['A datarange declaraion which does not identify the individuals is not useful,
    except perhaps to document the intended range of a datatype property.
    However, it does make the proofs easier, and the special cases fewer.'] ).
axiom ::= dataRange( dr, +declaration, {annotation} ).
directive ::= dataRange( {dataLiteral} ).
*/