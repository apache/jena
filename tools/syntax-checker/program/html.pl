
%%%%  SYNTAX PART

htmlSyntax(Lvl) :-
   htmlComment('Start Abstract Syntax',Lvl),
   write('<pre>'),
   fail.
htmlSyntax(Lvl) :-
   setof(L+M+H+S,[X,Y]^(setof(N+r(B,OID),sRule(H,B,Lvl,OID,L-N),S),S=[M+X|Y]),Set),
   member(L+_+H+[First|Rest],Set),
   wFirstRule(L,H,First,Lvl),
   member(R,Rest),
   wNextRule(L,R,Lvl),
   fail.
   
htmlSyntax(Lvl) :-
   write('</pre>'),
   htmlComment('End Abstract Syntax',Lvl),
   fail.
htmlSyntax(_).

wFirstRule(L,H,N+r(B,_OID),Lvl) :-
   atom_length(H,Lg),
   linkName(H,Link,Lvl),
   wlist(['<a name="',Link,'" id="',Link,'"></a>',
           H,sp(12-Lg),'<a name="',Lvl,'-',L,N,'" id="',Lvl,'-',L,N,'"></a>','[',L,N,'] ::= ']),
   wRuleBody(B,Lvl).

wNextRule(L,N+r(B,_OID),Lvl) :-
   wlist([sp(12),'<a name="',Lvl,'-',L,N,'" id="',Lvl,'-',L,N,'"></a>','[',L,N,'] | ']),
   wRuleBody(B,Lvl).

wRuleBody(Rule,Lvl) :-
  formatRuleBodyAll(Rule,Lvl,true,20,_),
  !,
  nl.


% formatRuleBody(Body,Lvl,Top,InCol,OutCol) 
formatRuleBody(opt(sub(Opt)),Lvl,_Top) -->
    output('[ '),
    formatRuleBodyAll(Opt,Lvl,fail),
    output(' ]').
formatRuleBody(opt(Opt),Lvl,_Top) -->
    output('[ '),
    formatRuleBodyAll(Opt,Lvl,fail),
    output(' ]').

formatRuleBody(terminal(Terminal),_Lvl,_Top) -->
    {write('<span class="terminal">')},
    output(Terminal),
    {write('</span>')}.

formatRuleBody(sub(Many),Lvl,_Top) -->
    formatRuleBodyAll(Many,Lvl,fail).

formatRuleBody(star(sub(Many)),Lvl,_Top) -->
    output('{ '),
    formatRuleBodyAll(Many,Lvl,fail),
    output(' }').

formatRuleBody(star(Many),Lvl,_Top) -->
    output('{ '),
    formatRuleBodyAll(Many,Lvl,fail),
    output(' }').

formatRuleBody(open(Item),_Lvl,_Top) -->
    { flag(indentation,N,N+2),
      capitalize(Item,CItem) },
    output(CItem),
    output('( ').

formatRuleBody(close(_Item),_Lvl,_Top) -->
    { flag(indentation,N,N-2) },
    output(' )').

  
formatRuleBody(nonterminal(NonTerminal),Lvl,_Top) -->
   {linkName(NonTerminal,Link,Lvl),
   wlist(['<a href="#',Link,'"><span class="nonterminal">'])},
   output(NonTerminal),
   {write('</span></a>')}.  
formatRuleBody(nonterminal(NonTerminal,Sub),Lvl,_Top) -->
   {linkName(NonTerminal,Link,Lvl),
   wlist(['<a href="#',Link,'"><span class="nonterminal">'])},
   output(NonTerminal),
   {write('<sub>')},
   output(Sub),
   {write('</sub>')},
   {write('</span></a>')}.

formatRuleBody(token(T),_Lvl,_Top) -->
   { capitalize(T,TT),
   wlist(['<span class="token">'])},
   output(TT),
   {write('</span>')}.

formatRuleBody(A:builtin,_Lvl,_Top) -->
   {write('<code><strong>') },
   output(A),
   {write(':</strong></code><em>')},
   output(builtin),
   {write('</em>')}.
formatRuleBody(A:B,_Lvl,_Top) -->
   {write('<code><strong>') },
   output(A),
   output(':'),
   output(B),
   {write('</strong></code>')}.

formatRuleBody(X,Y,Z) -->
   { throw(formatRuleBody(X,Y,Z) ) }.

       
formatRuleBodyAll([],_,_) --> [].

formatRuleBodyAll([A1|T],Lvl,Top) -->
   optNL(Top,[A1|T]),
   formatRuleBody(A1,Lvl,fail),
   output(' '),
   formatRuleBodyAll(T,Lvl,Top).
   
optNL(_,[close(_)]) --> !.
optNL(true,_,N,25) :-
   N > 60,
   !,
   nl,
   write('                         ').
optNL(_,_) --> [].
   
   
output(W,N,N1) :-
   atom_length(W,L),
   N1 is N + L,
   write(W).

linkName(NonTerminal,Link,Lvl) :-
  concat_atom([Lvl,'-',NonTerminal],Link).


htmlComment(Comment,Level) :-
   wlist(['<!-- *****************************',nl,
          '*** ',Level,' ****',nl,
          '*** ',Comment,' ****',nl,
          '****************************** -->',nl]).


%%%% MAPPING RULES


map(_) :-
  write('<table border="1" width="100%" rules="groups">
<colgroup span="1"></colgroup><colgroup span="1"></colgroup>
<colgroup span="1" align="right"></colgroup><colgroup span="1"></colgroup><thead><tr>
<th>Input</th><th colspan="2">Triples</th><th>Notes</th></tr></thead>'),nl,
  fail.

map(Lvl) :-
  mRule0(void,_A,_B,Rules,Result,Lvl,_F),
  flag(indentation,_,0),
  (write('<tbody>'),
   mustBe(Result=void),
   member(X,Rules),
   mapEntry(X,void,Lvl);
   write('</tbody>') ),
  fail.

map(_) :-
  write('<thead><tr>
<th>Input</th><th>Triples</th><th>Return</th><th>Notes</th></tr></thead>'),nl,
  fail.

map(Lvl) :-
  mRule0(node,_A,_B,[H|Rules],Result,Lvl,_F),
  (write('<tbody>'),
   mapEntry(H,Result,Lvl),
   member(X,Rules),
   mapEntry(X,void,Lvl);
   write('</tbody>') ),
  fail.  

map(_) :-
  write('<tbody><tr><td colspan="4"><em>The following rules are invoked with
         an argument node which participates in the generated triples.
         It is shown as <code><strong>@</strong></code></td></tr></tbody>'),
  nl,
  fail.
map(Lvl) :-
  mRule0(arg,_A,_B,Rules,Result,Lvl,_F),
  (write('<tbody>'),
   mustBe(Result=void),
   member(X,Rules),
   mapEntry(X,void,Lvl);
   write('</tbody>') ),
  fail.
map(_) :- write('</table>
').

mapEntry(L+R,Result,Lvl) :-
   write('<tr>'),
   mapInput(L,Lvl),
   mapTriples(R,Result,Lvl),
   mapReturn(R,Result,Lvl),
   mapNotes(R),
   write('</tr>
'),
   !.
mapEntry(L+R,Result,_Lvl) :-
  throw(mapEntryFailure(L,R,Result)).

mapReturn(_,void,_).
mapReturn(true,R,Lvl) :-
   write('<td colspan="2" align="right">'),
   output(R,Lvl),
   write('</td>').
mapReturn(_,R,Lvl) :-
   write('<td>'),
   output(R,Lvl),
   write('</td>').

mapInput(L,Lvl) :-
   write('<td><tt>'),
   indent,
   formatRuleBody(L,Lvl,fail,0,_),
   !,
   write('</tt></td>'),nl.

mapTriples(X,void,Lvl) :-
   write('<td colspan="2">'),
   mapTriples(X,Lvl),
   write('</td>').
mapTriples(true,_,_).
mapTriples(X,_,Lvl) :-
   write('<td>'),
   mapTriples(X,Lvl),
   write('</td>').

mapTriples(true,_).
mapTriples(sub(L,_),Lvl) :-

   mapTriples2(L,Lvl).
%   write('</td>').
mapTriples(star(sub(L,_)),Lvl) :-
   write('{'),
   mapTriples2(L,Lvl),
   write('}').
mapTriples(seq(sub(L,_)),Lvl) :-
  % write('<td>'),
   mapTriples2(L,Lvl).
   
mapTriples(opt(sub(L,_)),Lvl) :-
   write('['),
   mapTriples2(L,Lvl),
   write(']').

mapTriples2(L,Lvl) :-
   append(_,[X,_|_],L),
   writeTriple(X,Lvl),
   write('<br />'),nl,
   fail.
mapTriples2(L,Lvl) :-
   append(_,[X],L),
   writeTriple(X,Lvl).
   

writeTriple(opt(X),Lvl) :-
   write('[ '),
   writeTriple(X,Lvl),
   write(' ] (opt)'),
   !.
writeTriple(t(A,B,C),Lvl) :-
   output(A,Lvl),put(32),output(B,Lvl),put(32),output(C,Lvl),write(' .'), !.
writeTriple(x(A,B),Lvl) :-
   wlist(['T( ']),
   output(A,Lvl),
   write(' '),
   formatRuleBodyAll(B,Lvl,fail,0,_),
   write(' )'), !.
writeTriple(x(B),Lvl) :-
   wlist(['T( ']),
   formatRuleBodyAll(B,Lvl,fail,0,_),
   write(' )'), !.
   

   

mapNotes(true) :-
   write('<td></td>').
mapNotes(sub(_,L)):-
   write('<td>'),
   member(X,L),
   wlist(['<a href="#note-',X,'">',X,'</a>']),
   fail;
   write('</td>').
mapNotes(star(X)):-mapNotes(X).
mapNotes(opt(X)):-mapNotes(X).
mapNotes(seq(X)):-mapNotes(X).
  
indent :- 
  flag(indentation,Cnt,Cnt),
  length(L,Cnt),
  checklist(nbsp,L).
nbsp(_) :- write('&nbsp;').


output(up,_) :-
  !,
  write( '<code><strong>@</strong></code>' ).

output(Special,_) :-
   member(Special,[uriref,blank,0,1,naturalNumber,lexicalForm,language]),
   !,
   wlist(['<em>',Special,'</em>']).
output(A:builtin,_) :-
  !,
   wlist(['<code><strong>',A,':</strong></code><em>builtin</em>']).
output(A:B,_) :-
  !,
   wlist(['<code><strong>',A:B,'</strong></code>']).
output(x(L),Lvl) :-
  writeTriple(x(L),Lvl).
output(x(A,B),Lvl) :-
  writeTriple(x(A,B),Lvl).
output(A@X^^D,Lvl) :-
   output((A@X)^^D,Lvl).
output(T^^D,Lvl) :-
   output(T,Lvl),
   write('^^'),
   output(D,Lvl).
output(A@B,Lvl) :-
   output(A,Lvl),write(@),output(B,Lvl).



%%% TRIPLES
%%% 

level(lite,lite).
level(dl,dl).
level(dl2,dl).

triples(LvlX) :-
   level(LvlX,Lvl),
   wlist(['<h3><a name="',Lvl,'-triple-table" id="',Lvl,'-triple-table" >',Lvl,' Triple Table</h3>',nl]),
   write('<table border="1">
<thead><th>Subject</th><th>Predicate</th><th>Object</th></thead>'),
   nl,
   fail.
triples(Lvl) :-
   bagof(T,X^typedTriple(T,Lvl,X),Bag),
   sublist(nonStructuralNorAuxilliary,Bag,SmallBag),
   writeBagSlowly0(SmallBag),
   write('</table>'),
   fail.
triples(dl2) :-
   bagof(T,(X+Z)^(typedTriple(T,dl,X),\+typedTriple(T,lite,Z)),Bag),
   sublist(nonStructuralNorAuxilliary,Bag,SmallBag),
   writeBagSlowly(SmallBag),
   write('</table>'),
   fail.
triples(lite) :-
  triples(dl2),
  fail.
triples(_).

auxilliaryTriples(Lvl) :-
   bagof(T,X^(typedTriple(T,Lvl,X),auxilliary(T)),Bag),
   !,
   wlist(['<h3><a name="',Lvl,'-aux-table" id="',Lvl,'-aux-table" >Auxilliary Triple Table</h3>',nl]),
   write('<table border="1">
<thead><th>Subject</th><th>Predicate</th><th>Object</th></thead>'),
   nl,
   sublist(nonStructural,Bag,SmallBag),
   writeBagSlowly(SmallBag),
   write('</table>').
auxilliaryTriples(_Lvl).


writeBagSlowly0(SmallBag) :-
        setof(E, F^member(t(F, annotationPropID, E), SmallBag), Cs),
        setof(F, E^member(t(F, annotationPropID, E), SmallBag), As),
        verify(SmallBag,As,Cs),
        writeTriplePattern(As+[annotationPropID]+Cs),
        pruneBag(SmallBag,As+[annotationPropID]+Cs,Triples),
        writeBagSlowly(Triples).

verify(Triples,As,Cs) :-
  member(A,As),
  member(C,Cs),
  \+ member(t(A,annotationPropID,C),Triples),
  throw(noAnnotation(A,C)).
verify(_,_,_).


writeBagSlowly(Triples) :-
   setof(GT,getGenTriples(Triples,GT),[Score+TT|_]),
   Score < 0,
   !,
   writeTriplePattern(TT),
   pruneBag(Triples,TT,Triples1),
   writeBagSlowly(Triples1).

writeBagSlowly(Triples) :-
   sort(Triples,TSort),
   member(t(A,B,C),TSort),
   writeTriplePattern([A]+[B]+[C]),
   fail.
writeBagSlowly(_).

nonStructuralNorAuxilliary(X) :-
  nonStructural(X),
  \+ auxilliary(X).


getGenTriples(Triples,Score+TT) :-
   member(t(A,B,C),Triples),
   B \= annotationPropID,
   fullyGeneralize(Triples,[A]+[B]+[C],TT),
   score(TT,Score).


score(A+B+C,N) :-
  length(A,N1),
  length(B,N2),
  length(C,N3),
  N is max(N1,max(N2,N3)) - N1 * N2 * N3.

pruneBag([],_,[]).
pruneBag([t(S,P,O)|T],SS+PP+OO,TT) :-
   member(S,SS),
   member(P,PP),
   member(O,OO),
   !,
   pruneBag(T,SS+PP+OO,TT).
pruneBag([H|T],Pattern,[H|TT]) :-
   !,
   pruneBag(T,Pattern,TT).



writeTriplePattern(A+B+C) :-
  write('<tr>'),
  writePos(A),
  writePos(B),
  writePos(C),
  write('</tr>').
writePos(X) :-
  write('<td>'),
  writePosList(X),
  write('</td>').

writePosList([X]) :-
  writeX(X), !.
writePosList([H|T]) :-
  writeX(H),
  wlist([' <em>or</em><br />',nl]),
  writePosList(T).

writeX(A:B) :-
  wlist(['<code><strong>',A,':',B,'</strong></code>']),
  !.
writeX(A) :-
  wlist(['<em>',A,'</em>']).
nonStructural(t(X,_,_)) :-
  \+ structural(X).
nonStructural(t(X,Y,_)) :-
  structural(X),
  comparative(Y).
nonStructural(t(X,rdf:type,rdfs:'Class')) :-
  structural(X).

auxilliary(t(X,Y,_)) :-
  fail,
  structural(X),
  comparative(Y).

structural(restriction).
structural(description).
structural(unnamedDataRange).
structural(allDifferent).
structural(List) :-
   atom_concat(listOf,_,List).

comparative(rdfs:subClassOf).
comparative(owl:equivalentClass).
comparative(owl:disjointWith).



%%% Blank node rules

cans(Lvl) :-
  canned(Lvl,L),
  \+ okCan(Lvl,L),
/*
  choose(Triples,L),
  \+ (satisfy(Triples,Rule,Lvl)
%   ,wlist([Rule])
),
*/
  throw(badCan(Lvl,L)).

cans(Lvl) :-
  setof(X,(A+B+R)^(typedTriple(t(X,A,B),Lvl,R),structural(X),\+ auxilliary(X)),Set),
  member(X,Set),
  wlist([nl,'%%%% ',X,nl,nl]),
  notCanned(Rule,Triples,X,Lvl),
  wlist([nl,'%%%% ',X,'   ',Rule,nl,nl]),
  member(TT,Triples),
  wlist([TT,nl]),
  fail.
/*
 % wlist(['<h3>Rules for Blank Nodes of Category ',X,'</h3>',nl,
       '<table border="1">',nl]),
  %write(Rules),nl,
  showStructure(X,RuleIDs,Rules,Lvl),
  write('</table>
'),
  fail.
*/
cans(_).

additional(Lvl,X) :-
   typedTriple(t(X,A,B),Lvl,_),
   nonStructural(t(X,A,B)),
   !,
   wlist(['There may be additional triples in which such nodes are subjects
           as listed in the main triple tables.',nl]).
additional(lite,X) :-
   typedTriple(t(X,A,B),dl,_),
   nonStructural(t(X,A,B)),
   !,
   wlist(['In OWL DL only, there may be additional triples in which such nodes are subjects
           as listed in the main triple tables.',nl]).
additional(_Lvl,_X) :-
   wlist(['There are no other triples in the graph in which such nodes are subjects.',
           nl]).
   
blankCategories(Lvl) :-
  setof(X,(A+B+R+Any)^(typedTriple(t(X,A,B),Lvl,R),(atom_concat(listOf,Any,X);\+ atom_concat(Any,'ID',X))),Set),
  member(X,Set),
  wlist(['<li>',X,'</li>',nl]),
  fail;
  true.
blankCategories(lite) :-
  setof(X,(A+B+R+AA+BB+RR+Any)^(typedTriple(t(X,A,B),dl,R),\+typedTriple(t(X,AA,BB),lite,RR),(atom_concat(listOf,Any,X);\+ atom_concat(Any,'ID',X))),Set),
  member(X,Set),
  wlist(['<li>',X,'[DL only]</li>',nl]),
  fail;
  true.
  
  
tableList(dl) :-
  wlist(['<li><a href="#dl-triple-table">Main table</a></li>',nl]),
  fail.
tableList(lite) :-
  wlist(['<li><a href="#lite-triple-table">Main Lite table</a></li>',nl]),
  wlist(['<li><a href="#dl-triple-table">Main DL table</a></li>',nl]),
  fail.
tableList(Lvl) :-
  setof(X,(A+B+R)^(typedTriple(t(X,A,B),Lvl,R),structural(X),\+ auxilliary(X)),Set),
  member(X,Set),
  wlist(['<li><a href="#blank-',Lvl,'-',X,'">',
         'blank nodes of category ',X,'</a></li>',nl]),
  fail.
tableList(lite) :-
  setof(X,(A+B+R+AA+BB+RR)^(typedTriple(t(X,A,B),dl,R),\+typedTriple(t(X,AA,BB),lite,RR),structural(X),\+ auxilliary(X)),Set),
  member(X,Set),
  wlist(['<li>[DL only] <a href="#blank-dl-',X,'">',
         'blank nodes of category ',X,'</a></li>',nl]),
  fail.
tableList(_).


blanks(Lvl) :-
  setof(X,(A+B+R)^(typedTriple(t(X,A,B),Lvl,R),structural(X)),Set),
  blanks2(Lvl,Set,'').
blanks(lite) :-
  setof(X,(A+B+R+AA+BB+RR)^(typedTriple(t(X,A,B),dl,R),\+typedTriple(t(X,AA,BB),lite,RR),structural(X)),Set),
  blanks2(dl,Set,'[DL only]').
/*
 % wlist(['<h3>Rules for Blank Nodes of Category ',X,'</h3>',nl,
       '<table border="1">',nl]),
  %write(Rules),nl,
  showStructure(X,RuleIDs,Rules,Lvl),
  write('</table>
'),
  fail.
*/
blanks(_).

blanks2(Lvl,Set,Marker) :-
  member(X,Set),
  (wlist(['<h3><a name="blank-',Lvl,'-',X,'" id="blank-',Lvl,'-',X,'"> </a>',nl,Marker,
         ' Rules for Blank Nodes of Category ',X,'</h3>',nl]),
  Triples = [ [X]+_+_ | _ ],
  setof(Triples,
  Rule^ 
  (canned(Lvl,Triples),
    (okCan(Lvl,Triples)->true;throw(badCan(Lvl,Triples)));
    notCanned(Rule,Triples,X,Lvl) ),
     AllPatterns ),
  member(Triples,AllPatterns),
  patternHeading(Lvl,X,AllPatterns,Triples),
  ( append(First,[A+[rdf:type]+B|Rest],Triples)->
  append([A+[rdf:type]+B|First],Rest,Triples2);
   Triples = Triples2 ),
  member(TT,Triples2),
  writeTriplePattern(TT);
  wlist(['</table>',nl]) ),
  fail.

patternHeading(Lvl,X,[T],T) :- !,
  wlist([
       '<p>Every node of category ',X,' must fit the following pattern.',nl,
       'For each such node, exactly one instance of each triple in the pattern must be present
        in the graph.',nl]),
  additional(Lvl,X),
  wlist(['</p>',nl, 
       '<table border="1">',nl,
       '<thead><tr><th>Subject</th><th>Predicate</th><th>Object</th></tr></thead>',nl
       ]).
patternHeading(Lvl,X,[T|_],T) :- !,
   wlist([
       '<p>Every node of category ',X,' must fit one of these patterns.',nl,
       'For each such node, exactly one instance of each triple in the pattern must be present
        in the graph.',nl]), 
  additional(Lvl,X),
  wlist(['</p>',nl, 
       '<table border="1">',nl,
       '<thead><tr><th>Subject</th><th>Predicate</th><th>Object</th></tr></thead>',nl
       ]),
  wlist(['<tr><td colspan="3"><strong><em>Either:</em></strong></td></tr>',nl]).
patternHeading(_,_,_,_) :- !,
   wlist(['<tr><td colspan="3"><strong><em>Or:</em></strong></td></tr>',nl]).

okCan(Lvl,L) :-
  \+ (choose(Triples,L),
      \+ satisfy(Triples,_Rule,Lvl)
  ).

%%%% restriction   mapping-52
%%%% restriction   mapping-53
%%%% restriction   mapping-54
%%%% restriction   mapping-58
%%%% restriction   mapping-59
%%%% restriction   mapping-60

canned(lite,
    [
[restriction]+[owl:maxCardinality,owl:minCardinality,owl:cardinality]+[0^^ (xsd:nonNegativeInteger), 1^^ (xsd:nonNegativeInteger)],
[restriction]+[owl:onProperty]+[objectPropID,dataPropID],
[restriction]+[rdf:type]+[owl:'Restriction']
    ]).

%%%% restriction   mapping-50
%%%% restriction   mapping-51

canned(lite,
  [
[restriction]+[owl:allValuesFrom, owl:someValuesFrom]+[datatypeID],
[restriction]+[owl:onProperty]+[dataPropID],
[restriction]+[rdf:type]+[owl:'Restriction']
  ]).


%%%% restriction   mapping-63
%%%% restriction   mapping-62
%%%% restriction   mapping-56
%%%% restriction   mapping-57

canned(lite,
  [
[restriction]+[owl:allValuesFrom, owl:someValuesFrom]+[classID],
[restriction]+[owl:onProperty]+[objectPropID,transitivePropID],
[restriction]+[rdf:type]+[owl:'Restriction']
  ]).

canned(dl,
  [
[restriction]+[owl:hasValue]+[individualID],
[restriction]+[owl:onProperty]+[objectPropID,transitivePropID],
[restriction]+[rdf:type]+[owl:'Restriction']
  ]).


%%%% description   mapping-78
%%%% description   mapping-79
canned(dl,
[
[description]+[owl:intersectionOf,owl:unionOf]+[listOfDescription, rdf:nil],
[description]+[rdf:type]+[owl:'Class']
]).

%%%% restriction   mapping-52
%%%% restriction   mapping-53
%%%% restriction   mapping-54
%%%% restriction   mapping-58
%%%% restriction   mapping-59
%%%% restriction   mapping-60

canned(dl,
    [
[restriction]+[owl:maxCardinality,owl:minCardinality,owl:cardinality]+[nonNegativeInteger,0^^ (xsd:nonNegativeInteger), 1^^ (xsd:nonNegativeInteger)],
[restriction]+[owl:onProperty]+[objectPropID,dataPropID],
[restriction]+[rdf:type]+[owl:'Restriction']
    ]).

%%%% restriction   mapping-50
%%%% restriction   mapping-51

canned(dl,
  [
[restriction]+[owl:allValuesFrom, owl:someValuesFrom]+[ datatypeID, unnamedDataRange],
[restriction]+[owl:onProperty]+[dataPropID],
[restriction]+[rdf:type]+[owl:'Restriction']
  ]).


%%%% restriction   mapping-63
%%%% restriction   mapping-62
%%%% restriction   mapping-56
%%%% restriction   mapping-57

canned(dl,
  [
[restriction]+[owl:allValuesFrom, owl:someValuesFrom]+[classID, description, restriction],
[restriction]+[owl:onProperty]+[objectPropID,transitivePropID],
[restriction]+[rdf:type]+[owl:'Restriction']
  ]).

satisfy([],_,_).
satisfy([A+B+C|T],R,Lvl) :-
  typedTriple(t(A,B,C),Lvl,R),
  satisfy(T,R,Lvl).


dumbCanned(Rule,Triples,X,Lvl) :-
 setof([X]+[A]+BB,
  setof(B,
  ( typedTriple(t(X,A,B),Lvl,Rule),
  \+ nonStructural(t(X,A,B))),BB), Triples),
 Triples = [[X]+_+_|_].

notCanned(Rule,Triples,X,Lvl) :-
  dumbCanned(Rule,Triples,X,Lvl),
  \+ precanned(Lvl,Triples).

precanned(Lvl,Triples) :-
  \+ (choose(T,Triples),
     sort(T,U),
     \+ (
       canned(Lvl,Can),
       choose(TT,Can),
       sort(TT,U) ) ).
  
choose(T,Triples) :-
  maplist(choose1,T,Triples).

choose1(A+B+C,AA+BB+CC) :-
  member(A,AA),
  member(B,BB),
  member(C,CC).

/* Dead code??
showStructure(_X,_RuleIDs,[],_Lvl) :- !.
showStructure(X,RuleIDs,Rules,Lvl) :-
  member(_+t(X,A,B),Rules),
  \+ ( member(R,RuleIDs),\+ member(R+t(X,A,B),Rules) ),
  writeTriplePattern([X]+[A]+[B]),
  mySubList(_+t(X,A,B) \= _,2,Rules,Rules2),
  !,
  showStructure(X,RuleIDs,Rules2,Lvl).
showStructure(X,RuleIDs,Rules,Lvl) :-
  member(_+t(X,A,_),Rules),
  \+ ( member(R,RuleIDs),\+ member(R+t(X,A,_),Rules) ),
  setof(B,R^member(R+t(X,A,B),Rules),BB),
  writeTriplePattern([X]+[A]+BB),
  mySubList(_+t(X,A,_) \= _,2,Rules,Rules2),
  !,
  showStructure(X,RuleIDs,Rules2,Lvl).
showStructure(X,RuleIDs,Rules,Lvl) :-
  member(_+t(X,_,B),Rules),
  \+ ( member(R,RuleIDs),\+ member(R+t(X,_,B),Rules) ),
  setof(A,R^member(R+t(X,A,B),Rules),AA),
  writeTriplePattern([X]+AA+[B]),
  mySubList(_+t(X,_,B) \= _,2,Rules,Rules2),
  !,
  showStructure(X,RuleIDs,Rules2,Lvl).
showStructure(X,RuleIDs,Rules,Lvl) :-
  append(_,[R+_|T],Rules),
  member(R+_,T),
  wlist([nl,'@@@@@@@@@@@@@',Rules,nl,'@@@@@@@@@@@@',nl]),
  !,
  fail,
  throw(showStructure(X,R,Lvl)).
showStructure(X,RuleIDs,Rules,Lvl) :-
  write('<tr><td colspan="3"><em>and exactly one of</em></td></tr>'),
  nl,
  member(_+t(A,B,C),Rules),
  writeTriplePattern([A]+[B]+[C]),
  fail.
showStructure(_,_,_,_).

*/

mySubList(Pred,Arg1,[H1|T1],[H1|T2]) :-
  \+ \+ (arg(Arg1,Pred,H1),Pred),
  !,
  mySubList(Pred,Arg1,T1,T2).
mySubList(Pred,Arg,[_H|T],TT) :-
  mySubList(Pred,Arg,T,TT).
mySubList(_,_,[],[]).



   


  




   