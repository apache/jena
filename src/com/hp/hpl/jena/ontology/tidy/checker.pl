
/* This Prolog program has to be loaded with the Prolog
   available at:
   http://www.w3.org/2001/sw/WebOnt/syntaxTF/prolog/
start at
   http://www.w3.org/2001/sw/WebOnt/syntaxTF/prolog/load.pl
to work out all the links :(
*/

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
datatypeID, individualID,  
objectPropID, ontologyID, transitivePropID],userID).

grouping(
[annotationPropID, dataPropID, 
objectPropID, transitivePropID],propertyOnly).

grouping([orphan, ontologyPropertyID], ontologyPropertyHack ).
grouping([allDifferent, description, listOfDataLiteral, listOfDescription, 
listOfIndividualID, orphan,
restriction, unnamedDataRange, unnamedIndividual],blank).

grouping(H) :- grouping(H,_).


% allBuiltins(Q,N,T,F) :-

allBuiltins(Q,N,[T],0) :-
  builtinx(Q,N,T).


allBuiltins(Q,N,[T],bad) :-
  badbuiltin(Q,N,T).

allBuiltins(Q,N,[classID],notQuite) :-
  classOnly(Q,N).

allBuiltins(Q,N,P,notQuite) :-
  grouping(P,propertyOnly),
  propertyOnly(Q,N).

allBuiltins(Q,N,[Q:N],0) :-
  builtiny(Q,N).

gogo :-
   buildChecker,
   gname,
   classFile.

:-dynamic tt/4.
:- dynamic g/1.
:- dynamic x/7.
:- dynamic finished/0.
:- dynamic gn/2.
:-dynamic width/1.
:-dynamic gn2/2.
:-dynamic m/2.
:-dynamic m/1.
:- dynamic h/1.

propertyNumber(owl:intersectionOf, 1):-!.
propertyNumber(owl:complementOf, 1):-!.
propertyNumber(owl:distinctMembers, 1):-!.
propertyNumber(owl:unionOf, 1):-!.
propertyNumber(rdf:type,0) :-!.
propertyNumber(owl:allValuesFrom, 3):-!.
propertyNumber(rdf:rest,2) :- !.
propertyNumber(owl:onProperty,2) :- !.
propertyNumber(owl:cardinality, 3):-!.
propertyNumber(owl:hasValue, 3):-!.
propertyNumber(owl:maxCardinality, 3):-!.
propertyNumber(owl:minCardinality, 3):-!.
propertyNumber(owl:oneOf, 3):-!.
propertyNumber(owl:someValuesFrom, 3):-!.
propertyNumber(rdf:first, 3):-!.
propertyNumber(P,_) :- throw(propertyNumber(P)).


uncan(N,Can) :-
  member([S]+Ps+Os,Can),
  member(P,Ps),
  propertyNumber(P,PN),
  member(O,Os),
  xobj(O,O1),
  tt(S,P,O1,Lvl),
  assert(tt(S-N,P,O1,Lvl/property(PN))),
  fail.
uncan(_,_).
  
  


buildChecker :-
  retractall(tt(_,_,_,_)),
  typedTriple(t(S,P,O),DL,_),
  xobj(O,O1),
  assertOnce(tt(S,P,O1,DL)),
  fail.
buildChecker :-
   tt(S,P,O,lite),
   retract(tt(S,P,O,dl)),
   fail.
buildChecker :-
   retractall(m(_)),
   retractall(m(_,_)),
   dumbCanned(_Mapping-N,Can,D,dl),
   assertOnce(m(D)),
   assert(m(D,D-N)),
   uncan(N,Can),
   fail.
buildChecker :-
   m(D),
   tt(D,P,O,Lvl),
   \+ comparative(P),
   retract(tt(D,P,O,Lvl)),
   fail.
buildChecker :-
   m(D),
   comparative(P),
   retract(tt(D,P,O,Lvl)),
   m(D,N),
   assert(tt(N,P,O,Lvl)),
   fail.
buildChecker :-
   m(D),
   retract(tt(S,P,D,Lvl)),
   m(D,N),
   assert(tt(S,P,N,Lvl)),
   fail.

   
buildChecker :-
  retractall(g(_)),
  grouping(G),
  expandGrouping(G,G1),
  assert(g(G1)),
  fail.
buildChecker :-
  setof(N,isTTnode(N),S),
  member(X,S),
  assert(g([X])),
  fail.

/*
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
buildChecker :-
  retractall(x(_,_,_,_,_,_,_)),
  retractall(finished),
  countx(tt(_,_,_,_),N),
  wuser(['tt has ',N,' entries.',nl]),
  repeat,
  (asserta(finished),
  g(X),
  \+ h(X),
  show_stats,
  choose(X,A,B,C),
  switch(A,B,C,AA,BB,CC,DL),
  \+x(A,B,C,AA,BB,CC,DL),
  assert(x(A,B,C,AA,BB,CC,DL)),
  assertOnce(g(AA)),
  assertOnce(g(BB)),
  assertOnce(g(CC)),
  retract(finished),
  fail;
  finished,!).

*/

buildChecker :-
   tell('tmpSubCategorizationInput'),
   wlist([orphan,nl]),
   g([A]),
   writeq(A),nl,
   fail.
buildChecker :-
   write('%%'),nl,
   tt(S,P,O,_),
   writeq(S),write(' '),
   writeq(P),write(' '),
   writeq(O),nl,
   fail.
buildChecker :-
   write('%%'),nl,
   g([A,B|T]),
   (member(X,[A,B|T]),
   writeq(X),put(" "),
   fail;nl),fail.
buildChecker :-
  write('%%'),nl,told,
  shell('./c/precompute < tmpSubCategorizationInput > tmpSubCategorizationOutput.pl'),
  [tmpSubCategorizationOutput].

choose(X,X,B,C) :- h(X,B),h(X,C).
choose(X,A,X,C) :- h(A),h(X,C).
choose(X,A,B,X) :- h(A),h(B).
choose(X,_,_,_) :- assertz(h(X)),fail.

h(A,A).
h(_,A) :- h(A).


countx(G,_) :-
  flag(count,_,0),
  G,
  flag(count,N,N+1),
  fail.
countx(_,N) :-
  flag(count,N,N).

show_stats :-
  telling(X),
  tell(user),
  stats1,
  tell(X).

stats1 :-
  countx(g(_),GN),
  countx(h(_),HN),
  statistics(runtime,[_,RT]),
  statistics(inferences,II),
  flag(inferences,OLD,II),
  I is II - OLD,
  writef('%5r %5r %8r %8r\n',[GN,HN,RT,I]).
  

expandGrouping(In,Out) :-
  setof(X,expandIn(X,In),Out).

expandIn(X,In) :-
  member(X1,In),
  expand2(X1,X).

expand2(X,XX) :-
   m(X,XX).
expand2(X,X) :- \+ m(X,_).

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
gname([D-N],QN) :- atom_concat(D,N,QN),!.
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
  wsfi('NotQuiteBuiltin',1),
  wsfi('BadXSD',2),
  wsfi('Failure',-1),
  wlist(['static int getBuiltinID(String uri) {',nl]),
  wlist(['  if ( uri.startsWith("http://www.w3.org/") ) {',nl]),
  wlist(['      uri = uri.substring(18);',nl]),
  wlist(['      if (false) {}',nl]),
  getBuiltins(owl),
  getBuiltins(rdf),
  getBuiltins(xsd),
  getBuiltins(rdfs),
  wlist(['     }',nl,'     return Failure; ',nl,'}',nl]).

getBuiltins(Q) :-
  namespace(Q,URI),
  atom_concat('http://www.w3.org/',R,URI),
  atom_length(R,N),
  wlist(['   else if ( uri.startsWith("',R,'") ) {',nl,
         '       uri = uri.substring(',N,');',nl,
         '       if (false) {',nl]),
  allBuiltins(Q,Nm,P,Special),
  wlist(['       } else if ( uri.equals("',Nm,'") ) {',nl]),
  gn(P,Gp),
  specialBuiltin(Special,SpCode),
  wlist(['          return ',Gp,SpCode,';',nl]),
  fail.
getBuiltins(_) :-
  wlist(['     }',nl,'   }',nl]).

specialBuiltin(bad,'| BadXSD').
specialBuiltin(0,'').
specialBuiltin(notQuite,'| NotQuiteBuiltin').

  

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
   extraInfo(DL,DLX),
   wlist([DLX,';',nl]),fail.
wAddTriple :-
   wlist(['      default: return Failure;',nl,'   }',nl,
         '}',nl]).

extraInfo(dl,'| DL').
extraInfo(lite,'| DL').
extraInfo(dl/position(0),'| DL').
extraInfo(lite/position(0),'').
extraInfo(dl/position(1),'| DL | FirstOfOne ').
extraInfo(lite/position(1),'| FirstOfOne').
extraInfo(dl/position(2),'| DL | SecondOfTwo ').
extraInfo(lite/position(2),'| SecondOfTwo').
extraInfo(dl/position(3),'| DL | FirstOfTwo ').
extraInfo(lite/position(3),'| FirstOfTwo').
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