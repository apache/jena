
/*

OWL Lite and OWL DL

Abstract Syntax, Mapping Rules, Concrete Syntax

Author: Carroll
Based on: AS&amp;S editor's draft 24 January 2003 Patel-Schneider
With significant modification.

*/

:-op(1200,xfx,'::=').
:- dynamic (::=)/2.
:- discontiguous (::=)/2.

terminal(uriref).
terminal(lexicalForm).
terminal(language).
terminal(0).
terminal(1).


/* Notation:
   ::= rewrite arrow
   {}   zero or more
   [] zero or one
   + terminal symbol
   (o,m,dt, ...)   IDs
*/
ontology ::=
   ontology( { directive } ).

comment(rationale,
   ['When dealing with imports, the resulting ontology may many ',link(header),' blocks, with many
     different ',link(o),' URI references. Hence, the change from the published AS&amp;S.' ]).

directive ::=
   header .

directive ::=
   axiom .

directive ::=
   fact .

header ::=
   header( [o], {metaPropValue}, {annotation} ).


metaPropValue ::= 
   metaPropValue( m, o ).
annotation ::=
   annotation( a, dataLiteral ).
annotation ::=
   annotation( da, dataLiteral ).
comment(note,
   ['Wherever in an abstract syntax document the following rule is used,
     the ',next(individual),' is also treated as a top-level ',link(fact),'.']).

annotation ::=
   annotation( a, individual ).
annotation ::=
   annotation( a, a ).


annotation ::=
   annotation( a, da ).
annotation ::=
   annotation( a, m ).

annotation ::=
   annotation( a, o ).

annotation ::=
   annotation( a, dt ).

annotation ::=
   annotation( a, c ).

annotation ::=
   annotation( a, i ).


annotation ::=
   annotation( a, cdp ).

annotation ::=
   annotation( a, cop ).

annotation ::=
   annotation( a, tp ).


fact ::= 
   individual .

fact ::= allDifferentIndividuals( {i} ).


individual ::=
   individual( i,  {annotation}, { type( description ) }, 
   { value }, {individualComparison} ).

individual ::=
   individual(   {annotation}, { type( description ) }, 
   { value } ).


value ::=
    value( cop, individual ).
value ::=
    value( tp, individual ).
value ::=
    value( cdp, dataLiteral ).

%type ::= c.
%type ::= restriction.

description ::= c.
description ::= restriction.

comment(note,
   [ 'This maps directly onto RDF literals and has the same constraints and definitions.',
     'See [RDF Concepts]. Note, specifically that there the lexicalForm is in Unicode Normal 
      Form C, and that the language identifier is an RFC 3066 language tag normalized to lower
      case.'
   ]).


dataLiteral ::=
      dataLiteral(lexicalForm,[language],[dt]).

individualComparison ::=
   sameIndividual(i).
individualComparison ::=
   differentIndividuals(i).

axiom ::=
   class(c, [+deprecated], modality, {annotation},
                     {super(description)}
   ).
modality ::= +partial.
modality ::= +complete.

axiom ::= equivalentClasses( classInRestriction,  classInRestriction  ).

comment(rationale, ['This differs from the published AS&amp;S in that each ',link(restriction),' may only
                      contain one condition, not an arbitrary number. The expressive power is unchanged,
                      since every use may be repeated arbitrarily. This makes it easier to express the
                      mapping to RDF in which each condition becomes a separate blank node.']). 

restriction ::= restriction( cdp, allValuesFrom( dataRange ) ).
restriction ::= restriction( cdp, someValuesFrom( dataRange ) ).
restriction ::= restriction( cdp, cardinality(smallInt) ).
restriction ::= restriction( cdp, minCardinality(smallInt) ).
restriction ::= restriction( cdp, maxCardinality(smallInt) ).

restriction ::= restriction( cop, allValuesFrom( classInRestriction ) ).
restriction ::= restriction( cop, someValuesFrom( classInRestriction  ) ).
restriction ::= restriction( cop, cardinality(smallInt) ).
restriction ::= restriction( cop, minCardinality(smallInt) ).
restriction ::= restriction( cop, maxCardinality(smallInt) ).

restriction ::= restriction( tp, allValuesFrom( classInRestriction  ) ).
restriction ::= restriction( tp, someValuesFrom( classInRestriction  ) ).

axiom ::= restriction .
comment(rationale, 
  ['The extra level here is used for an extension in OWL DL.']).
classInRestriction ::= c .


smallInt ::= 0.
smallInt ::= 1.

dataRange ::= dt.
%dataRange ::= dr.
axiom ::=
  annotationProperty( a, { annotation } ).
axiom ::=
  annotationProperty( da, { annotation } ).

comment(rationale,
  [ 'For ease of writing the document, I have made the property axioms
     identical in OWL Lite and OWL DL, but kept the same semantics by
     having the additional rules for dataRange and classInRestriction 
     in OWL DL' ]).
axiom ::=
  datatypeProperty( cdp, 
                    [+deprecated],{ annotation },
                    { super(cdp) },
                    { domain(classInRestriction) },
                    { range(dataRange) }, [+functional] ).

axiom ::=
  individualProperty( cop, 
                    [+deprecated],{ annotation },
                    { super(cop) },
                    { super(tp) },
                    { domain(classInRestriction) },
                    { range(classInRestriction) }, 
                    { inverse(cop) },
                    [+functional], 
                    [+inverseFunctional], 
                    [+symmetric] ).
axiom ::=
  individualProperty( tp, 
                    [+deprecated],{ annotation },
                    { super(tp) },
                    { domain(classInRestriction) },
                    { range(classInRestriction) }, 
                    { inverse(tp) }, 
                    [+transitive], 
                    [+symmetric] ).

axiom ::=
   equivalentProperties(tp,tp,{tp}).
axiom ::=
   equivalentProperties(cop,cop,{cop}).
axiom ::=
   equivalentProperties(cdp,cdp,{cdp}).

axiom ::=
   datatypeDeclaration( dt,{ annotation } ).

axiom ::= description.