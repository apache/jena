
:- dynamic mRule0/7.


:- dynamic typing/3.
:- dynamic typedTriple/3.
:- dynamic argType/3.

compMapping :-
   compSyntax,
   retractall(mRule0(_,_,_,_,_,_,_)),
   retractall(typing(_,_,_)),
   retractall(argType(_,_,_)),
   retractall(typedTriple(_,_,_)),
   mapping(NAV,In,AS,Triples,Node,ID),
   compMapping(NAV,In,AS,Triples,Node,ID),
   fail.

compMapping :-
% sanity checks
   alignedVarsCheck,
   fail.

compMapping :-
  mRule0(A,B,C,D,E,lite,F),
  assertz(mRule0(A,B,C,D,E,dl,F)),
  fail.

compMapping :-
  retractall(typing(_,_,_)),
  mRule0(node,Seq,_,_,NodeC,Lvl,ID),
  returnType(Seq,NodeC,Lvl,ID),
  fail.

compMapping :-
  mRule0(NAV,Seq,Triples,_,_,Lvl,ID),
  NAV \= arg,
  extractTriple(Triples,Triple),
  tripleType(Seq,Lvl,Triple,TypedTriple),
  assertOnce(typedTriple(TypedTriple,Lvl,ID)),
 % prove(Seq,TypedTriple,Lvl),
  fail.

compMapping :-
  mRule0(_,_,Triples,_,_,Lvl,ID),
  extract(Triples,x(Seq)),
  removeSubScripts(Seq,L),
  mRule0(NAV,Seq2,_,_,_,Lvl,ID2),
  call( (removeSubScripts(Seq2,LL),
         matchLevel(L,[],Lvl,LL,[]),
         !,
         NAV=arg,
         throw(argUnexpected(ID,ID2)) ) ),
  fail.

compMapping :-
  mRule0(_,Seq0,Triples,_,_,Lvl,_),
  extract(Triples,x(X,Seq)),
  findType(Seq0,X,Type,Lvl),
  removeSubScripts(Seq,L),
  mRule0(NAV,Seq2,_,_,_,Lvl,ID2),
  call( (removeSubScripts(Seq2,LL),
         matchLevel(L,[],Lvl,LL,[]),
         !,
         mustBe(NAV=arg),
         assertOnce(argType(ID2,Lvl,Type)) ) ),
  fail.  

compMapping :-
  mRule0(arg,_,Triples,_,_,Lvl,ID),
  extractTriple(Triples,Triple),
  argType(ID,Lvl,Type),
  tripleType(up(Type),Lvl,Triple,TypedTriple),
  assertOnce(typedTriple(TypedTriple,Lvl,ID)),
  fail.
  

compMapping :-
  retractall(typedTriple(t(_,_,owl:'Thing'),_,_)).
%compMapping.


sets(Lvl,Out) :-
 setof(X+Y+Z,
  setof(A,setof(B,setof(C,ID^typedTriple(t(A,B,C),Lvl,ID),Z),Y),X),
   All),
  maplist(weight,All,Wall),
  sort(Wall,Out).


generalize(Triples,S+P+O,[S1|S]+P+O) :-
     head(S,S2),
     head(P,P1),
     head(O,O1),
     member(t(S1,P1,O1),Triples),
     S1 @< S2,
     allIn([S1]+P+O,Triples).
generalize(Triples,S+P+O,S+[P1|P]+O) :-
     head(S,S1),
     head(P,P2),
     head(O,O1),
     member(t(S1,P1,O1),Triples),
     P1 @< P2,
     allIn(S+[P1]+O,Triples).
generalize(Triples,S+P+O,S+P+[O1|O]) :-
     head(S,S1),
     head(P,P1),
     head(O,O2),
     member(t(S1,P1,O1),Triples),
     O1 @< O2,
     allIn(S+P+[O1],Triples).

fullyGeneralize(_Triples,V,V).
fullyGeneralize(Triples,V,V2) :-
   generalize(Triples,V,V1),
   fullyGeneralize(Triples,V1,V2).
head([H|_],H).
allIn(SL+PL+OL,Triples) :-
  member(S,SL),
  member(P,PL),
  member(O,OL),
  \+ member(t(S,P,O),Triples),
  !,
  fail.
allIn(_,_).

generalizedTriples(Lvl,S,P,O) :-
  setof(t(A,B,C),ID^typedTriple(t(A,B,C),Lvl,ID),Triples),
  member(t(A,B,C),Triples),
  fullyGeneralize(Triples,[A]+[B]+[C],S+P+O).


  

weight(A+B+C,N+A+B+C) :-
  length(A,AN),
  length(B,BN),
  length(C,CN),
  N is AN*BN*CN - max(max(AN,BN),CN).

tripleType(Seq,Lvl,t(A,B,C),t(AA,BB,CC)) :-
  findType(Seq,A,AA,Lvl),
  findType(Seq,B,BB,Lvl),
  findType(Seq,C,CC,Lvl) 
    *->
    true;
  throw(tripleType(Seq,Lvl,t(A,B,C))).
  

extractTriple(Triples,Triple) :-
  extract(Triples,Triple),
  Triple = t(_,_,_).
extractVoid(Triples,Triple) :-
  extract(Triples,Triple),
  Triple = x(_).
extractArg(Triples,Triple) :-
  extract(Triples,Triple),
  Triple = x(_,_).

extract(Triples,Triple) :-
   member(T,Triples),
   extract(T,Triple).
extract(sub(L,_),Y) :-
 !,
 extract(L,Y).
extract(opt(X),Y) :-
  !,
  extract(X,Y).
extract(star(X),Y) :-
  !,
  extract(X,Y).
extract(seq(X),Y) :-
  !,
  extract(X,Y).
extract([],_) :- !, fail.
extract([_|_],_) :- !, fail.
extract(A,A).

% getTriple(t(S,P,O),t(S,P,O)).

findType(up(Type),up,Type,_).
findType([open(ID),terminal(uriref),close(ID)],uriref,ID,_).
findType([open(ID),A:B,close(ID)],A:B,ID,_) :- !.
findType(_,A:B,A:B,_) :-
  builtin(A,B).
findType(_Seq,lexicalForm@language^^_DT,literal,_).
findType(_Seq,lexicalForm@language,literal,_).
findType(_Seq,lexicalForm^^_DT,literal,_).
findType(_Seq,lexicalForm,literal,_).
findType(_,x([nonterminal(SomeID)]),SomeID,_) :-
   atomic(SomeID),
   atom_concat(_,'ID',SomeID),
   !.
findType(_,x(L),T, Lvl) :-
   removeSubScripts(L,LL),
   mustMatch(LL, Lvl),
   matchType(LL,Lvl,T).

findType(_Seq,0 ^^ (xsd:nonNegativeInteger),0 ^^ (xsd:nonNegativeInteger),_ ).
findType(_Seq,1 ^^ (xsd:nonNegativeInteger),1 ^^ (xsd:nonNegativeInteger),_ ).
findType(_Seq,naturalNumber ^^ (xsd:nonNegativeInteger),nonNegativeInteger,_ ).
findType([open(header)|_],blank,unnamedOntology,_).
findType([open(individual)|_],blank,unnamedIndividual,_).
findType([open(restriction)|_],blank,restriction,_).
findType([open(intersectionOf)|_],blank,description,_).
findType([open(unionOf)|_],blank,description,_).
findType([open(complementOf)|_],blank,description,_).
findType([open(allDifferentIndividuals)|_],blank,allDifferent,_).
findType([open(oneOf),star([nonterminal(individualID)])|_],blank,description,_).
findType([open(dataRange),star([nonterminal(dataLiteral)])|_],blank,unnamedDataRange,_).
findType(_,rdf:nil,rdf:nil,_).
findType([open(seq),nonterminal(X,_)|_],blank,XList,_) :-
    capitalize(X,XX),
    atom_concat(listOf,XX,XList).


mustMatch(L,Lvl) :-
  matchType(L,Lvl,_),
  !.
mustMatch(L,Lvl) :-
  throw(unmatched(L,Lvl)).

%matchType([nonterminal(description)],_,description).
%matchType([nonterminal(restriction)],_,restriction).
matchType(L,Lvl,T) :-
  mRule0(NAV,Seq,_Triples,_,_,Lvl,ID),
  removeSubScripts(Seq,Seq0),
  matchLevel(L,[],Lvl,Seq0,[]),
  mustBe(NAV=node),
  typing(Lvl,ID,T).

returnType(Seq,NodeC,Lvl,ID) :-
  findType(Seq,NodeC,_,Lvl),
  !,
  findType(Seq,NodeC,Type,Lvl),
  assertz(typing(Lvl,ID,Type)).
returnType(Seq,NodeC,_,ID) :-
  write(typing(Seq,NodeC,ID)),nl.

alignedVarsCheck :-
   mRule0(NAV,_,_,Aligned,Node,_,_ID),
%   write(ID),put(32),
   checkAlignment(NAV,Aligned,Node),
   fail.
alignedVarsCheck.
   
compMapping(NAV,In,AS,Triples,Node,ID) :-
   ignore(Node=uriref),
   ignore(In=up),
%   write(AS),nl,write(ID),nl,nl,
   expandRHS(AS,ASList),   % defined in syntax
   removeSubScripts(ASList,NoSubs),
   matchLevel(NoSubs,Lvl),
   starsOnce(NoSubs,NoStars),
   matchLevel(NoStars,Lvl),
   expandTriples(Triples,TriplesList),
   align(ASList,TriplesList,AlignedList),
   fixup(Node,NodeC),
   assertz(mRule0(NAV,ASList,TriplesList,AlignedList,NodeC,Lvl,ID)),
   !.
compMapping(_NAV,_In,_AS,_Triples,_Node,ID) :-
   throw(mappingRuleSyntax(ID)).

removeSubScripts(L,LL) :-
  maplist(removeSubScript,L,LL).
removeSubScript(V,V) :- var(V),!.
removeSubScript(t(A,B,C),t(A1,B1,C1)) :-
  !,
  removeSubScript(A,A1),
  removeSubScript(B,B1),
  removeSubScript(C,C1).
removeSubScript(A+_,A) :- !.
removeSubScript(nonterminal(A,_),nonterminal(A)) :-
  !.
removeSubScript(nonterminal(uriref),terminal(uriref)) :-
  !.
removeSubScript(star(X),star(XX)):-
  !,
  removeSubScripts(X,XX).

removeSubScript(opt(X),opt(XX)):-
  !,
  removeSubScripts(X,XX).
removeSubScript(X,X).

starsOnce([],[]).
starsOnce([star(A)|T],TTT) :-
  !,
  starsOnce(T,TT),
  append(A,TT,TTT).

starsOnce([opt(A)|T],TTT) :-
  !,
  starsOnce(T,TT),
  append(A,TT,TTT).
starsOnce([H|T],[H|TT]) :-
  starsOnce(T,TT).

matchLevel([open(dataRangeID), terminal(uriref), close(dataRangeID)],dl) 
  :- !.
matchLevel(Match,lite) :-
  matchLevel([nonterminal(_)],[],lite,Match,[]),
  !.
matchLevel(Match,dl) :-
  matchLevel([nonterminal(_)],[],dl,Match,[]),
  !.
matchLevel([open(seq),nonterminal(dataLiteral)|_],dl) :-
   
   !.
matchLevel([open(seq)|T],lite) :-
   seqList(T),
   !.
matchLevel(Match,Lvl) :-
  throw(unmatchedMapping(Match,Lvl)).
seqList([close(seq)]).
seqList([star([])|_]).
seqList([nonterminal(description)|_]).
seqList([nonterminal(dataLiteral)|_]).
seqList([nonterminal(individualID)|_]).
seqList([nonterminal(type)|_]).

matchLevel(Any,Any,_)-->[].
matchLevel([terminal(uriref)|T],TT,Lvl) -->
  [_:_],
  matchLevel(T,TT,Lvl).
matchLevel([H|T],TT,Lvl) -->
  [H],
  matchLevel(T,TT,Lvl).
matchLevel([opt(L)|T],TT,Lvl) -->
  matchLevel(L,[],Lvl),
  matchLevel(T,TT,Lvl).
matchLevel([opt(_)|T],TT,Lvl) -->
  matchLevel(T,TT,Lvl).
matchLevel([nonterminal(N)|T],TT,Lvl) -->
  {sRule(N,L,Lvl,_,_)},
  matchLevel(L,[],Lvl),
  matchLevel(T,TT,Lvl).
%  {wlist([N,' => ',L,nl])}.
matchLevel([star([nonterminal(dataLiteral)])|_],_,_) -->
   [star([nonterminal(individualID)])],
   !,
   {fail}.
matchLevel([star([nonterminal(individualID)])|_],_,_) -->
   [star([nonterminal(dataLiteral)])],
   !,
   {fail}.
matchLevel([star(X)|T],TT,Lvl) -->
  [star(Y)],
  {matchLevel(X,[],Lvl,Y,[])},
  !,
  matchLevel([star(X)|T],TT,Lvl).
matchLevel([star(X)|T],TT,Lvl) -->
  matchLevel(X,[],Lvl),
  matchLevel([star(X)|T],TT,Lvl).
matchLevel([star(_)|T],TT,Lvl) -->
  matchLevel(T,TT,Lvl).
matchLevel(L,TT,Lvl,[star(X)|R],RR) :-
  matchLevel(L,T,Lvl,X,[]),
  matchLevel(T,TT,Lvl,[star(X)|R],RR).
matchLevel(L,TT,Lvl) -->
  [star(_)],
  matchLevel(L,TT,Lvl).
matchLevel(L,TT,Lvl) -->
  [opt(X)],
  { matchLevel(L,T,Lvl,X,[]) },
  matchLevel(T,TT,Lvl).
matchLevel(L,TT,Lvl) -->
  [opt(_)],
  matchLevel(L,TT,Lvl).

% align(ASList,TripleList,AlignedList)

%align(ASList,TripleList,AlignedList) :-
%   wlist(['************',nl,ASList,nl,'++++',nl,TripleList,nl]),
%   fail.
align(ASList,TripleList,AlignedList) :-
   combine(ASList,ASList2),
%   wlist(['+++',nl,ASList2,nl]),
   align2(ASList2,TripleList,AlignedList).

align2([],[],[]).
align2([star(X)|T],[star(Y)|TT],[star(X)+star(Y)|TTT]) :-
   !,
   align2(T,TT,TTT).
align2([star(X)|T],[seq(Y)|TT],[star(X)+seq(Y)|TTT]) :-
   !,
   align2(T,TT,TTT).
align2([opt(X)|T],[opt(Y)|TT],[opt(X)+opt(Y)|TTT]) :-
   !,
   align2(T,TT,TTT).
align2([sub(X)|T],[sub(Y,Z)|TT],[sub(X)+sub(Y,Z)|TTT]) :-
   !,
   align2(T,TT,TTT).
align2([sub(X)|T],TT,[sub(X)+true|TTT]) :-
   !,
   align2(T,TT,TTT).
align2(T,[sub(Y,Z)|TT],[true+sub(Y,Z)|TTT]) :-
   !,
   align2(T,TT,TTT).


combine([],[]).
combine(ASL,[sub(H)|T]) :-
   nonOptionalPrefix(H,ASL,TT),
   H \= [],
   combine(TT,T).
combine([star(X)|TT],[star(sub(X))|T]) :-
   combine(TT,T).
combine([opt(X)|TT],[opt(sub(X))|T]) :-
   combine(TT,T).

   

% expandTriples(Triples,TriplesList)


rhs2list(true,[]) :- !.
rhs2list((A,B),[A|T]) :-
  !,
  rhs2list(B,T).
rhs2list(A,[A]).

expandTriples(RHS,TriplesList) :-
  rhs2list(RHS,RHSList),
  expandTList(RHSList,TriplesList).

expandTList([],[]) :- !.
expandTList([{Rstar}|RT],[star(sub(XL,Notes))|TT]) :-
  !,
  rhs2list(Rstar,RL),
  simpleRHS(RL,XL,Notes),
  expandTList(RT,TT).
expandTList([t(A,B,x(seq({RStar})))|RT],[seq(sub(XL,Notes))|TT]) :-
  !,
  simpleRHS([t(A,B,x(seq({RStar})))],XL,Notes),
  expandTList(RT,TT).
expandTList(  [[RStar]|RT],[opt(sub(XL,Notes))|TT] ) :-
  !,
  rhs2list(RStar,RL),
  simpleRHS(RL,XL,Notes),
  expandTList(RT,TT).
expandTList( R, [sub(XL,Notes)|TT] ) :-
  nonOptionalPrefix(RP,R,RT),
  simpleRHS(RP,XL,Notes),
  expandTList(RT,TT).
  
nonOptionalPrefix([],[],[]) :- !.
nonOptionalPrefix([],[[X]|T],[[X]|T]):-!.
nonOptionalPrefix([],[{X}|T],[{X}|T]):-!.
nonOptionalPrefix([],[star(X)|T],[star(X)|T]):-!.
nonOptionalPrefix([],[opt(X)|T],[opt(X)|T]):-!.
nonOptionalPrefix([],[t(A,B,x(seq({S})))|T],[t(A,B,x(seq({S})))|T]):-!.
nonOptionalPrefix([H|T],[H|TT],R) :-
  nonOptionalPrefix(T,TT,R).

simpleRHS( A, B, Notes ) :-
  maplist(lineRHS,A,B),
  findall(N,member(note(N)/_,A),Notes).


lineRHS( +[Opt],  opt(OptOut1)  ) :-
   !,
   lineRHS( Opt,  OptOut1  ).
lineRHS(note(_)/X,XX) :-
   !,
   lineRHS(X,XX).
lineRHS(t(Subj,Pred,Obj),t(S,P,O)) :-
   !,
   fixup(Subj,S),
   fixup(Pred,P),
   fixup(Obj,O).
lineRHS(x(Part),XO) :-
   !,
   fixup(x(Part),XO).
lineRHS(x(Arg,Part),XO) :-
   !,
   fixup(x(Arg,Part),XO).
lineRHS(Bad,_) :-
   throw(badMapRule(Bad)).

   
subscriptIn(A-N,A,[N]) :- !.
subscriptIn(Any,Any,[]).

subscript(subscript(A,N),A,[N]) :- !.
subscript(Any,Any,[]).
fixup(up,up) :- !.
fixup(uriref,uriref) :- !.
fixup(blank,blank) :-!.
fixup(x(U),x(Out)) :-
   expandRHS(U,Out).
/*
   subscriptIn(U,U1,Subscript),
   uriref(U1,URI,_,_),
   !,
   atom_concat(URI,'ID',URIID),
   subscript(Out,URIID,Subscript).
*/
fixup(A^^X:D,Out) :-
   !,
   fixup(A^^ (X:D),Out).
fixup(A@L^^D,A@L^^DT) :-
   !,
   fixup(D,DT).
fixup(A^^D,A^^DT) :-
   !,
   fixup(D,DT).
fixup(A@L,A@L) :-
   !.
/*
fixup({A},star(AA)) :-
   !,
   outputFixup(A,AA).
*/
fixup(x(seq(A)),x(AA)) :-
   !,
   expandRHS(seq(A),AA).
/*
fixup(x(NT0),x(NT2)) :-
   !,
   subscriptIn(NT0,NT,Subscript),
   name(NT,_), % checks NT is atomic
   subscript(NT2,NT,Subscript).
*/
fixup(x(In,NT0),x(InOut,NT2)) :-
   !,
   fixup(In,InOut),
   expandRHS(NT0,NT2).
/*
   subscriptIn(NT0,NT,Subscript),
   name(NT,_), % checks NT is atomic
   fixup(In,InOut),
   subscript(NT2,NT,Subscript).
/ *
fixup(U1,U2) :-
   U1 =.. [U,A],
   uriref(U,URI,_,_),
   !,
   outputFixup(A,AO),
   atom_concat(URI,'ID',URIID),
   U2 =.. [URIID,A0].
*/
fixup(U,Out) :-
   subscriptIn(U,U1,Subscript),
   uriref(U1,URI,_,_),
   !,
   atom_concat(URI,'ID',URIID),
   subscript(Out,URIID,Subscript).
fixup(+X,X) :-
    token(X),
    !.
fixup(Namespace:builtin,Namespace:builtin):-!.
fixup(Namespace:Frag,Namespace:FixedFrag) :-
     !,
     caseAdjustBuiltin(Namespace,Frag,FixedFrag).
/*
fixup(Functor,XX) :-
  Functor =.. [A,R|T],
  A \= (-),
  !,
  capitalize(A,F),
  flatten([
    ['<code>',F,'</code>( '],
    FF,
    [')']
    ],XX).
*/  
fixup(X,X2) :- 
     subscriptIn(X,X1,Sub),
     subscript(X2,X1,Sub).

checkAlignment(NAV,[First|Rest],Node) :-
   initDeclared(NAV,Decl0),
   checkAlignedRow(Decl0,First,Declared),
   checkAlignedRows(Declared,Rest),
   checkAlignedNode(NAV,Declared,Node),
   !.
checkAlignment(_,L,V) :-
   throw(checkAlignmentFailed(L,V)).

initDeclared(arg,[up]) :- !.
initDeclared(node,[blank]) :- !.
initDeclared(void,[]).

checkAlignedRow(T,AS+Triples,Declared) :-
   getList(AS,ASL),
   extractDeclared(ASL,Declared,T),
   getList(Triples,TList),
   checkDeclared(Declared,TList).

getList(sub(L),L).
getList(sub(L,_),L).
getList(opt(X),L) :- getList(X,L).
getList(seq(X),L) :- getList(X,L).
getList(star(X),L) :- getList(X,L).
getList(true,[]).

checkAlignedRows(_Declared,[]).
checkAlignedRows(Decl,[H|T]) :-
  checkAlignedRow(Decl,H,_),
  checkAlignedRows(Decl,T).

checkAlignedNode(void,_,_).
checkAlignedNode(arg,_,_).
checkAlignedNode(node,Decl,N) :-
   checkDeclared(Decl,N).

extractDeclared([nonterminal(X)|T]) -->
   !,
   [X],
   extractDeclared(T).
extractDeclared([nonterminal(X,N)|T]) -->
   !,
   [subscript(X,N)],
   extractDeclared(T).
extractDeclared([terminal(X)|T]) -->
   !,
   [X],
   extractDeclared(T).
extractDeclared([X:Y|T]) -->
   !,
   [X:Y],
   extractDeclared(T).
extractDeclared([_|T]) -->
   extractDeclared(T).
extractDeclared([]) --> [].


checkDeclared(_,true) :- !.
checkDeclared(Decl,sub(L,_)) :-
   !,
   checkDeclared(Decl,L).
checkDeclared(_,[]) :- !.
checkDeclared(Decl,[H|T]) :-
  !,
  checkDeclared(Decl,H),
  checkDeclared(Decl,T).
checkDeclared(Decl,t(S,P,O)) :-
  !,
  checkDeclared(Decl,[S,P,O]).
checkDeclared(Decl,x(x(A),B)) :-
  !,
  checkDeclared(Decl,[A,B]).
checkDeclared(Decl,x(blank,B)) :-
  !,
  checkDeclared(Decl,[blank,B]).
checkDeclared(Decl,x(A)) :-
  !,
  checkDeclared(Decl,A).
checkDeclared(Decl,star(L)) :-
  !,
  checkDeclared(Decl,L).
checkDeclared(Decl,nonterminal(X)) :-
  !,
  checkDeclared(Decl,X).
checkDeclared(Decl,nonterminal(X,S)) :-
  !,
  checkDeclared(Decl,subscript(X,S)).

checkDeclared(Decl,A) :-
  member(A,Decl),
 % write(A),put(32),
  !.
checkDeclared(Decl,A^^B) :-
  !,
  checkDeclared(Decl,[A,B]).
checkDeclared(Decl,A@B) :-
  !,
  checkDeclared(Decl,[A,B]).
checkDeclared(_,A:B) :-
  builtin(A,B),
  !.
checkDeclared(Decl,opt(A)) :-
  !,
  checkDeclared(Decl,A).
checkDeclared(Decl,seq({super(X)})) :-
  !,
  checkDeclared(Decl,X).
/*
checkDeclared(Decl,seq({X})) :-
  !,
  checkDeclared(Decl,X).
*/
checkDeclared(_,open(_)) :- !.
checkDeclared(_,close(_)) :- !.
checkDeclared(D,A) :-
%  fail,
  throw(undeclared(A,D)).



    