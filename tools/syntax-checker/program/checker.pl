

namespace(rdf,'http://www.w3.org/1999/02/22-rdf-syntax-ns#').
namespace(rdfs,'http://www.w3.org/2000/01/rdf-schema#').
namespace(owl,'http://www.w3.org/2002/07/owl#').
namespace(xsd,'http://www.w3.org/2001/XMLSchema#').

grouping([annotationPropID, classID, dataPropID, datatypeID, individualID,
  objectPropID, ontologyID, transitivePropID, notype],userID).

grouping( [annotationPropID, dataPropID, objectPropID, 
           transitivePropID,notype],propertyOnly).

grouping([classID,notype],classOnly).

%grouping([orphan, ontologyPropertyID], ontologyPropertyHack ).
grouping([allDifferent, description, listOfDataLiteral, listOfDescription, 
listOfIndividualID, orphan, unnamedOntology,cyclic, 
cyclicFirst, cyclicRest, restriction, unnamedDataRange, 
unnamedIndividual,notype, badRestriction ],blank).

grouping([restriction],restrictions).
grouping([description],descriptions).
grouping([listOfDataLiteral, listOfDescription, listOfIndividualID],lists).


% allBuiltins(Q,N,T,F) :-

allBuiltins(Q,N,[T],0) :-
  builtinx(Q,N,T).


allBuiltins(Q,N,[T],bad) :-
  badbuiltin(Q,N,T).

allBuiltins(Q,N,[classOnly],notQuite) :-
  classOnly(Q,N).

allBuiltins(Q,N,[propertyOnly],notQuite) :-
  propertyOnly(Q,N).

allBuiltins(Q,N,[Q:N],0) :-
  builtiny(Q,N).

:-dynamic tt/4.
:-dynamic m/2.
:-dynamic m/1.
:-dynamic expg/2.

wTripleTable :-
  wlist(['static final private int SPOA(int s, int p, int o, int a) {',nl,
  ' return (((((s << WW) | p) << WW) | o) << ActionShift ) | a;',nl,
  '}',nl]),
  wlist(['static final int triples[] = new int[]{',nl]),
  tt(S,P,O,A),
  gname([S],SS),
  gname([P],PP),
  gname([O],OO),
  objectAction(P,O,AA),
  subjectAction(P,S,SA),
  dlPart(A,DL),
  pp(A,Pos),
  wlist(['SPOA( ',SS,', ',PP,', ',OO,', ',AA,SA,DL,Pos,' ),',nl]),
  fail.
wTripleTable :-
  wlist(['};',nl,
  'static { Arrays.sort(triples); }',nl]).
cwTripleTable :-
  wlist(['%% Triples',nl]),
  tt(S,P,O,A),
  gname([S],SS),
  gname([P],PP),
  gname([O],OO),
  objectAction(P,O,AA),
  subjectAction(P,S,SA),
  dlPart(A,DL),
  pp(A,Pos),
  wlist([SS,' ',PP,' ',OO,' ',AA,SA,DL,Pos,nl]),
  fail.
cwTripleTable :-
  wlist(['%% End',nl]).
objectAction(P,O,'ObjectAction') :-
   (\+ comparative(P);P==rdfs:subClassOf),
   expg(B,blank),
   member(O,B),
   !.
objectAction(_,_,0).
   
subjectAction(rdfs:subClassOf,S,'|SubjectAction') :-
   expg(B,blank),
   member(S,B),
   !.
subjectAction(_,_,'').
   
dlPart(lite,'') :- !.
dlPart(lite/_,'') :- !.
dlPart(_,'|DL').

pp(_/property(0),'') :- !.
pp(_/property(1),'|FirstOfOne') :- !.
pp(_/property(2),'|SecondOfTwo') :- !.
pp(_/property(3),'|FirstOfTwo') :- !.
pp(_,'').


category(orphan).
category(notype).
category(cyclic).
category(cyclicRest).
category(cyclicFirst).
category(badRestriction).
category(X) :-
   setof(Z,isTTnode(Z),S),
   member(Z,S),
   \+ notOut(Z),
   gname([Z],X).
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
propertyNumber(owl:oneOf, 1):-!.
propertyNumber(owl:someValuesFrom, 3):-!.
propertyNumber(rdf:first, 3):-!.
propertyNumber(P,_) :- throw(propertyNumber(P)).

theSame([owl:maxCardinality,owl:minCardinality,owl:cardinality]).
theSame([owl:someValuesFrom,owl:allValuesFrom]).

outCat(In,Out) :-
  theSame([Out|S]),
  member(In,S),
  !.
outCat(In,In).
notOut(X) :-
  theSame([_|S]),
  member(X,S),
  !.


uncan(N,Can) :-
  member([S]+Ps+Os,Can),
  member(P,Ps),
  propertyNumber(P,PN),
  member(O,Os),
  xobj(O,O1),
  tt(S,P,O1,Lvl),
  assertOnce(tt(S-N,P,O1,Lvl/property(PN))),
  fail.
uncan(_,_).
  
copyrightHead :-
     write('/* (c) Copyright '),
     wDate,
     wlist([' Hewlett-Packard Company, all rights reserved.',nl,
              '  [See end of file]',nl,
              '  $Id: checker.pl,v 1.20 2003-12-02 06:21:11 jeremy_carroll Exp $',nl,
              '*/',nl]).

wDate :-
   date(date(2003,_,_)),
   write(2003),
   !.
wDate :-
   date(date(D,_,_)),
   wlist([2003,'-',D]).
   
copyrightTail :- wlist([
' /*',nl,
'	(c) Copyright Hewlett-Packard Company ']),
wDate,
wlist([
nl,
'	All rights reserved.',nl,
' ',nl,
'	Redistribution and use in source and binary forms, with or without',nl,
'	modification, are permitted provided that the following conditions',nl,
'	are met:',nl,
' ',nl,
'	1. Redistributions of source code must retain the above copyright',nl,
'	   notice, this list of conditions and the following disclaimer.',nl,
' ',nl,
'	2. Redistributions in binary form must reproduce the above copyright',nl,
'	   notice, this list of conditions and the following disclaimer in the',nl,
'	   documentation and/or other materials provided with the distribution.',nl,
' ',nl,
'	3. The name of the author may not be used to endorse or promote products',nl,
'	   derived from this software without specific prior written permission.',nl,
' ',nl,
'	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR',nl,
'	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES',nl,
'	OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.',nl,
'	IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,',nl,
'	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT',nl,
'	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,',nl,
'	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY',nl,
'	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT',nl,
'	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF',nl,
'	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.',nl,
'*/  ',nl]).
gogo :-
  compMapping,
  buildChecker,
  gogo('Grammar','com.hp.hpl.jena.ontology.tidy.impl',
         '../../src/com/hp/hpl/jena/ontology/tidy/impl/Grammar.java'),
  gogo('Triples','owlcompiler','java/owlcompiler/Triples.java').
gogo(G,P,JF) :-
  % tt/4 is now good.
  % jfile(G,JF),
  tell(JF),
  copyrightHead,
  wlist(['package ', P,';',nl]),
  (G=='Triples'->
  wlist(['import com.hp.hpl.jena.ontology.tidy.impl.*;',nl,
         'import java.util.Arrays;',nl]); true),
  wlist([ 'class ',G,' implements Constants {',nl]),
  wCategories,
  %wActions,
  wGetBuiltinID,
  wGroups1,
  wInitSingletons,
  (G=='Triples'->wTripleTable;true),
  wGroups2,
  wlist(['}',nl]),
  copyrightTail,
  told.
wCategories :-
  flag(catID,_,1),
  category(C),
  flag(catID,N,N+1),
  wsfi(C,N),
  fail.

wCategories :-
  wlist([' static String catNames[] = { "--not used--",',nl]),
  category(C),
  wlist(['      "',C,'",',nl]),
  fail.
wCategories :-
  wlist(['       };',nl]).
 c :-
  tell(compileddata),
  wlist(['%% categories',nl]),
  flag(catID,_,1),
  category(C),
  flag(catID,N,N+1),
  cwsfi(C,N),
  fail.
  
c :-
  cwActions,
  cwGetBuiltinID,
  cwGroups,
  cwInitSingletons,
  cwTripleTable,
  told.
  
wInitSingletons :-
  flag(catID,N,N),
  wlist([
  'static final int MAX_SINGLETON_SET = ',N,' + 1;',nl,
  'static {',nl,
  'for (int i=0; i<',N,'; i++) {',nl,
   'if ( i != CategorySet.find(new int[]{i},true) )',nl,
   '      System.err.println("initialization problem");',nl,
   '}',nl,
   '};',nl]).
  
cwInitSingletons.


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
   dumbCanned(_Mapping-N0,Can,D,dl),
   assertOnce(m(D)),
   %
   %((D=description;D=restriction),comparative(_:C);
   %    C=object),
   drName(D,C,N0,N,Can),
   assert(m(D,D-N*C)),
   uncan(N*C,Can),
   fail.
buildChecker :-
   m(D),
   tt(D,P,O,Lvl),
   \+ nonStructural(t(D,P,O)),
   retract(tt(D,P,O,Lvl)),
   fail.
buildChecker :-
   m(D),
   comparative(Q:P),
   retract(tt(D,Q:P,O,Lvl)),
   m(D,D-N*P),
   assert(tt(D-N*P,Q:P,O,Lvl)),
   fail.
buildChecker :-
   m(D),
   comparative(Q:P),
   P\=subClassOf,
   retract(tt(S,Q:P,D,Lvl)),
   m(D,D-N*P),
   assert(tt(S,Q:P,D-N*P,Lvl)),
   fail.
buildChecker :-
   m(D),
   funnyType(t(D,PP,T)),
   retract(tt(D,PP,T,Lvl)),
   m(D,D-N*P),
   assert(tt(D-N*P,PP,T,Lvl)),
   fail.
buildChecker :-
   m(D),
   retract(tt(S,P,D,Lvl)),
   m(D,D-N*object),
   assert(tt(S,P,D-N*object,Lvl)),
   fail.

   
buildChecker :-
  retractall(expg(_,_)),
  grouping(G,N),
  expandGrouping(G,G1),
  assert(expg(G1,N)),
  fail.
  
buildChecker :-
  setof(D-N*disjointWith,isTTnode(D-N*disjointWith),S),
  assert(expg(S,disjointWith)).
  
  
drName(D,object,N,N,_) :-
  D \= description,
  D \= restriction.
drName(description,C,N0,5,Can) :-
   comparative(_:C); C=object.
drName(restriction,C,N0,N,Can) :-
   (comparative(_:C); C=object),
   member([restriction]+[owl:onProperty]+[PPP],Can),
   getN(PPP,N).

getN(dataPropID,6).
getN(objectPropID,7).
getN(transitivePropID,8).
  

wGroups1 :-
  expg(S,N),
  wlist(['static final int ',N,'X[] = new int[]{',nl]),
  (member(M,S),
     \+ notOut(M),
     gname([M],MM),
     wlist([MM,',',nl]),
     fail;
   wlist(['};',nl])),
  fail.
wGroups1.
wGroups2 :-
  expg(_,N),
  wlist([
    'static final int ',N,' = CategorySet.find( ',N,'X,false);',nl]),
  fail.
wGroups2.

cwGroups :-
  wlist(['%% Groups',nl]),
  expg(S,N),
  wlist(['',N,' = ']),
  (member(M,S),
     gname([M],MM),
     wlist([MM,' ']),
     fail;
   wlist([nl])),
  fail.
cwGroups.

countx(G,_) :-
  flag(count,_,0),
  G,
  flag(count,N,N+1),
  fail.
countx(_,N) :-
  flag(count,N,N).

expandGrouping(In,Out) :-
  setof(X,expandIn(X,In),Out).

expandIn(X,In) :-
  member(X1,In),
  expand2(X1,X).

expand2(X,XX) :-
   m(X,XX).
expand2(X,X) :- \+ m(X,_).



bits(N,NB) :-
  member(_,L),
  length(L,NB),
  N < 1<<NB,
  !.

gname([(Q:N)],QN) :- 
    outCat(Q:N,Q1:N1),
    atom_concat(Q1,N1,QN),!.
gname([D-_],D) :- countx(m(D,_),1),!.
gname([D-N*S],QN) :- concat_atom([D,N,S],QN),!.
gname([D-N],QN) :- concat_atom([D,N],QN),!.
gname([X],X) :- !.

gn1(X,XX) :-
  gname([X],XX).



wGetBuiltinID :-
/*
  wsfi('BadXSD','1<<W'),
  wsfi('BadOWL','2<<W'),
  wsfi('BadRDF','3<<W'),
  wsfi('DisallowedVocab','4<<W'),
  wsfi('Failure',-1),
*/
  wlist(['static int getBuiltinID(String uri) {',nl]),
  wlist(['  if ( uri.startsWith("http://www.w3.org/") ) {',nl]),
  wlist(['      uri = uri.substring(18);',nl]),
  wlist(['      if (false) {}',nl]),
  getBuiltins(owl),
  getBuiltins(rdf),
  getBuiltins(xsd),
  getBuiltins(rdfs),
  wlist(['     }',nl,'     return Failure; ',nl,'}',nl]).
  
cwGetBuiltinID.

getBuiltins(Q) :-
  namespace(Q,URI),
  atom_concat('http://www.w3.org/',R,URI),
  atom_length(R,N),
  wlist(['   else if ( uri.startsWith("',R,'") ) {',nl,
         '       uri = uri.substring(',N,');',nl,
         '       if (false) {',nl]),
  allBuiltins(Q,Nm,P,Special),
  wlist(['       } else if ( uri.equals("',Nm,'") ) {',nl]),
 %(P=[ontologyPropertyID]->Gp=ontologyPropertyHack;
  gname(P,Gp),
 % specialBuiltin(Special,SpCode),
  (Special=bad -> wlist(['   return BadXSD;',nl]);
  wlist(['          return ',Gp,SpCode,';',nl])),
  fail.
getBuiltins(Q) :-
  disallowed(Q,N),
  \+ builtin(Q,N),
  wlist(['       } else if ( uri.equals("',N,'") ) {',nl]),
  wlist(['           return DisallowedVocab;',nl]),
  fail.

getBuiltins(owl) :-
  wlist(['       } else { return BadOWL; ',nl]),
  fail.

getBuiltins(RDF_S) :-
  member(RDF_S,[rdf,rdfs]),
  wlist(['       } else { return BadRDF; ',nl]),
  fail.
getBuiltins(_) :-
  wlist(['     }',nl,'   }',nl]).

/*
specialBuiltin(bad,'| BadXSD').
specialBuiltin(0,'').
specialBuiltin(notQuite,'').
*/

jfile(Nm,Nmx) :-
  concat_atom(['../../src/com/hp/hpl/jena/ontology/tidy/',Nm,'.java'],Nmx).
  

wActions :-
  wsfi('FirstOfOne',8),
  wsfi('FirstOfTwo',16),
  wsfi('SecondOfTwo',24),
  wsfi('ObjectAction',2),
  wsfi('SubjectAction',4),
  wsfi('DL',1),
  wsfi('ActionShift',5),
  wsfi('CategoryShift',9),
  wlist(['    static private final int W = CategoryShift;',nl]).
  
cwActions.
  
wsfi(Name,Val) :-
  wlist(['    static final int ',Name,' = ',Val,';',nl]).
cwsfi(Name,Val) :-
  wlist([Name,' ',Val,nl]).


xsub([],_).
xsub([H|T],[H|TT]) :-
  !,
  xsub(T,TT).
xsub(L,[_|TT]) :-
  xsub(L,TT).
/*
spo(S,P,O) :-
   gn(S,SN),wlist(['( /* subject */',SN,'<<(2*W))|',nl]),
   gn(P,PN),wlist(['( /* predicate */',PN,'<<W)|',nl]),
   gn(O,ON),wlist([' /* object */',ON, ' ']).
*/

spo(S,P,O,R) :-
   width(W),
   gn2(S,SN),
   gn2(P,PN),
   gn2(O,ON),
   R is (SN<<(2*W))\/(PN<<W)\/ON,
   !.
spo(S,P,O,_) :-
  throw(spo(S,P,O)).



  

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
xobj(literal,userTypedLiteral).
xobj(A,A).

dull(_:_).
dull(literal).
dull(dlInteger).
dull(liteInteger).
dull(userTypedLiteral).
