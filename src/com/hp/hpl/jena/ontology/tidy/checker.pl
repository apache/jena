
isQname(_:_).
isNode(NN) :-
  typedTriple(t(NN,_,_),_,_).

isNode(NN) :-
  typedTriple(t(_,NN,_),_,_).

isNode(NN) :-
  typedTriple(t(_,_,NN),_,_).

allQnames(S) :-
  setof(X,(isNode(X),isQname(X)),S).

allURIrefs(U) :-
  allQnames(S),
  maplist(expandQname,S,U).

expandQname(Q:N,URIref+QN) :-
  namespace(Q,U),
  !,
  atom_concat(U,N,URIref),
  atom_concat(Q,N,QN).
expandQname(X,X).


namespace(rdf,'http://www.w3.org/1999/02/22-rdf-syntax-ns#').
namespace(rdfs,'http://www.w3.org/2000/01/rdf-schema#').
namespace(owl,'http://www.w3.org/2002/07/owl#').
namespace(xsd,'http://www.w3.org/2001/XMLSchema#').

grouping(
[annotationPropID, classID, dataPropID, 
datatypeID, individualID, listOfIndividualID, 
objectPropID, ontologyID, transitivePropID],userID).

grouping(
[annotationPropID, dataPropID, 
objectPropID, transitivePropID],propertyOnly).

grouping([allDifferent, description, listOfDataLiteral, listOfDescription, 
restriction, unnamedDataRange, unnamedIndividual],blank).

grouping(H) :- grouping(H,_).

:-dynamic tt/4.
:- dynamic g/1.
:- dynamic x/7.
:- dynamic finished/0.
:- dynamic gn/2.
:-dynamic width/1.
:-dynamic gn2/2.

buildChecker :-
  retractall(tt(_,_,_,_)),
  typedTriple(t(S,P,O),DL,_),
  xobj(O,O1),
  assertOnce(tt(S,P,O1,DL)),
  fail.
buildChecker :-
  retractall(g(_)),
  grouping(G),
  assert(g(G)),
  fail.
buildChecker :-
  setof(N,isTTnode(N),S),
  member(X,S),
  assert(g([X])),
  fail.
buildChecker :-
  retractall(x(_,_,_,_,_,_,_)),
  retractall(finished),
  repeat,
  (asserta(finished),
  g(A),g(B),g(C),
  switch(A,B,C,AA,BB,CC,DL),
  \+x(A,B,C,AA,BB,CC,DL),
  assert(x(A,B,C,AA,BB,CC,DL)),
  assertOnce(g(AA)),
  assertOnce(g(BB)),
  assertOnce(g(CC)),
  retract(finished),
  fail;
  finished,!).


gname :-
  retractall(gn(_,_)),
  g(G),
  gname(G,GN),
  assert(gn(G,GN)),
  fail.
gname.

bits(N,NB) :-
  member(_,L),
  length(L,NB),
  N < 1<<NB,
  !.

gname(L,N) :- grouping(L,N), !.
gname([(Q:N)],QN) :- atom_concat(Q,N,QN),!.
gname([X],X) :- !.
gname([],'Empty') :- !.
gname(L,N) :-
   maplist(gn1,L,L1),
   concat_atom(L1,'_',N).

gn1(X,XX) :-
  gname([X],XX).


classfile :-
  wlist(['package com.hp.hpl.jena.ontology.tidy;',nl,nl]),
  wlist(['/** automatically generated. */',nl]),
  wlist(['class Grammar {',nl]),
  wGetBuiltinID,
  wActions,
  wCategories,
  wAddTriple,
  wlist(['}',nl]).

wGetBuiltinID :-
  wsfi('Failure',-1),
  wlist(['static int getBuiltinID(String uri) { return Failure; }',nl]).

wActions :-
  wsfi('ActionShift',3).

wCategories :-
  retractall(width(_)),
  retractall(gn2(_,_)),
  setof(G,g(G),L),
  length(L,N),
  bits(N,NB),
  wsfi('CategoryShift',NB),
  assert(width(NB)),
  nth1(Ix,L,G),
  gn(G,Name),
  wsfi(Name,Ix),
  assert(gn2(G,Ix)),
  fail.
wCategories.

wsfi(Name,Val) :-
  wlist(['    static final int ',Name,' = ',Val,';',nl]).


wAddTriple :-
  wsfi('DL','1 << (3 * CategoryShift)'),
  wlist(['    static private final int W = CategoryShift;',nl]),
  wlist(['/','** Given some knowledge about the categorization',nl,
         'of a triple, return a refinement of that knowledge,',nl,
         'or {@link #Failure} if no refinement exists.',nl,
         '@param triple Shows the prior categorization of subject,',nl,
          'predicate and object in the triple.',nl,
         '@return Shows the possible legal matching categorizations of subject,',nl,
          'predicate and object in the triple.',nl,
         '*/',nl]),
   wlist(['    static int addTriple(int triple) {',nl]),
   wlist(['      switch(triple) {',nl]),
   x(S,P,O,SS,PP,OO,DL),
   write('case '),spo(S,P,O),wlist([':',nl,'return ']),spo(SS,PP,OO),
   (DL=dl?write('| DL;');write(';')),nl,fail.
wAddTriple :-
   wlist(['      default: return Failure;',nl,'   }',nl,
         '}',nl]).

/*
spo(S,P,O) :-
   gn(S,SN),wlist(['( /* subject */',SN,'<<(2*W))|',nl]),
   gn(P,PN),wlist(['( /* predicate */',PN,'<<W)|',nl]),
   gn(O,ON),wlist([' /* object */',ON, ' ']).
*/

spo(S,P,O) :-
   width(W),
   gn2(S,SN),
   gn2(P,PN),
   gn2(O,ON),
   R is (SN<<(2*W))\/(PN<<W)\/ON,
   write(R).

  
isTTnode(N) :-
  tt(N,_,_,_).
isTTnode(N) :-
  tt(_,N,_,_).
isTTnode(N) :-
  tt(_,_,N,_).

xobj(nonNegativeInteger,dlInteger).
xobj(nonNegativeInteger,liteInteger):- !.
xobj(0^^(xsd:nonNegativeInteger),liteInteger):- !.
xobj(1^^(xsd:nonNegativeInteger),liteInteger):- !.
xobj(literal,dlInteger).
xobj(literal,liteInteger).
xobj(A,A).

dull(_:_).
dull(literal).
dull(dlInteger).
dull(liteInteger).

switch(As,Bs,Cs,AA,BB,CC,DL) :-
   setof(A,[B,C,D]^(member(A,As),member(B,Bs),member(C,Cs),tt(A,B,C,D)),AA),
   setof(B,[A,C,D]^(member(A,As),member(B,Bs),member(C,Cs),tt(A,B,C,D)),BB),
   setof(C,[A,B,D]^(member(A,As),member(B,Bs),member(C,Cs),tt(A,B,C,D)),CC),
   (member(A,AA),member(B,BB),member(C,CC),tt(A,B,C,lite)->DL=lite;DL=dl),
   !.

  /*
switch(As,Bs,Cs,AA,BB,CC,lite) :-
  ignore((As=[A],dull(A),AA=[A])),
  ignore((Bs=[B],dull(B),BB=[B])),
  ignore((Cs=[C],dull(C),CC=[C])),
  tt(A,B,C,_),
  ignore(AA=[]),
  ignore(BB=[]),
  ignore(CC=[]),
  !.

*/
/*
[annotationPropID, classID, dataAnnotationPropID, dataPropID, 
datatypeID, individualID, listOfIndividualID, 
objectPropID, ontologyID, ontologyPropertyID, transitivePropID]
*/


/*
[allDifferent, description, listOfDataLiteral, listOfDescription, 
literal, nonNegativeInteger, restriction, unnamedDataRange, unnamedIndividual]

[owl:'AllDifferent', owl:'AnnotationProperty', owl:'Class', owl:'DataRange', 
owl:'DatatypeProperty', owl:'DeprecatedClass', owl:'DeprecatedProperty', 
owl:'FunctionalProperty', owl:'InverseFunctionalProperty', owl:'ObjectProperty', 
owl:'Ontology', owl:'OntologyProperty', owl:'Restriction', 
owl:'SymmetricProperty', owl:'TransitiveProperty', owl:allValuesFrom, 
owl:cardinality, owl:complementOf, owl:differentFrom, owl:disjointWith, 
owl:distinctMembers, owl:equivalentClass, owl:equivalentProperty, owl:hasValue, 
owl:intersectionOf, owl:inverseOf, owl:maxCardinality, owl:minCardinality, 
owl:onProperty, owl:oneOf, owl:sameIndividualAs, owl:someValuesFrom, 
owl:unionOf, 
rdf:'List', rdf:'Property', rdf:first, rdf:nil, rdf:rest, rdf:type, 
rdfs:'Class', rdfs:'Datatype', rdfs:domain, rdfs:range, rdfs:subClassOf, 
rdfs:subPropertyOf, 
0^^ (xsd:nonNegativeInteger), 1^^ (xsd:nonNegativeInteger)]
*/