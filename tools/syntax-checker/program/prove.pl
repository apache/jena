:-dynamic proof/6.



prove(Lvl,N) :-
  retractall(proof(Lvl,_,_,_,_)),
  prove(Lvl,N,Exp,TriplesExp,TriplesOther,Rules,Bs),
  recordProof(Lvl,Exp,TriplesExp,TriplesOther,Rules,Bs),
  fail.
prove(Lvl,_) :-
  typedTriple(T,Lvl,_),
  \+ (proof(Lvl,T,_,_,_,_);proof(Lvl,Ts,_,_,_,_),member(T,Ts)),
  wlist([T,nl]),
  fail.
prove(_,_).

recordProof(Lvl,Exp,[T],[],Rules,[]) :-
  nontvar(T),
 % (B\=[]->wlist([B,nl,T,nl,nl]);true),
  removeSubScript(T,T1),
  \+ proof(Lvl,T1,_,_,_,explicit),
  assert(proof(Lvl,T1,Exp,Rules,[],explicit)).


recordProof(Lvl,Exp,_,[T],Rules,Builtins) :-
  nontvar(T),
  removeSubScript(T,T1),
  sort(Builtins,BuiltinsX),
  \+ proof(Lvl,T1,_,_,BuiltinsX,general),
  assert(proof(Lvl,T1,Exp,Rules,BuiltinsX,general)).

recordProof(Lvl,Exp,Explicit,[One],Rules,Builtins) :-
  tvar(One,V),
  explicit(V,Explicit,Type),
  removeSubScript(One,One1),
  removeSubScript(Type,Type1),
  sort(Builtins,BuiltinsX),
  \+ proof(Lvl,[Type1,One1],_,_,BuiltinsX,blank),
  assert(proof(Lvl,[Type1,One1],Exp,Rules,BuiltinsX,blank)).

recordProof(Lvl,Exp,Explicit,[One,Two],Rules,Builtins) :-
  tvar(One,V),
  tvar(Two,V1),
  V==V1,
  explicit(V,Explicit,Type),
  removeSubScript(One,One1),
  removeSubScript(Two,Two1),
  removeSubScript(Type,Type1),
  sort(Builtins,BuiltinsX),
  \+ proof(Lvl,[Type1,One1,Two1],_,_,BuiltinsX,blank),
  assert(proof(Lvl,[Type1,One1,Two1],Exp,Rules,BuiltinsX,blank)).
  

nontvar(t(V,_,_)) :- nonvar(V).
tvar(t(V,_,_),V) :- var(V).
explicit(V,Exp,Type) :-
  member(Type,Exp),
  tvar(Type,V1),
  V==V1,
  !,
  type(Type).
type(t(description,rdf:type,owl:'Class')).
type(t(restriction,rdf:type,owl:'Restriction')).
type(t(unnamedDataRange,rdf:type,owl:'DataRange')).
type(t(allDifferent,rdf:type,owl:'AllDifferent')).

prove(Lvl,Exp,TriplesExp,TriplesOther,Rules,B) :-
  prove(Lvl,3,Exp,TriplesExp,TriplesOther,Rules,B).
prove(Lvl,Depth,Exp,TriplesExp,TriplesOther,Rules,Builtins) :-
   flag(bn,_,100),
   length(Choice,Depth),
   expand(Lvl,[nonterminal(directive)],Choice+OK,Exp,[]),
   numberZ(Exp),
   apply(Lvl,Exp,Triples0,Rules,Builtins),
   (Triples0=[],Triples=[];
     setof(T,member(T,Triples0),Triples)),
   sublist(explicitType,Triples,TriplesExp),
   sublist(notExplicitType,Triples,TriplesOther).

explicitType(t(_,rdf:type,A)) :-
  nonvar(A),
   \+ member(A,[rdf:'Property',owl:'DeprecatedProperty', owl:'FunctionalProperty',
           owl:'DeprecatedClass',rdfs:'Class']).
notExplicitType(t(A,B,C)) :-
 
   \+ explicitType(t(A,B,C)).
isBuiltin(builtin(_,_,_,_)).




expand(_Lvl,[],_) --> [].
expand(Lvl,[Syn|T],M+OK) -->
  {syntactic(Syn,OK)},
  [Syn],
  expand(Lvl,T,M+OK).

expand(Lvl,[nonterminal(Blank)|T],M+OK) -->
  {blankNode(Lvl,Blank,_,BX)},
  [nonterminate(Blank,BX)],
  expand(Lvl,T,M+OK).
expand(Lvl,[star(_)|T],V) -->
  expand(Lvl,T,V).
expand(Lvl,[star(S)|T],M+OK) -->
  {append(_,[V],M),var(V),V=done},
  expand(Lvl,S,M+OK),
  expand(Lvl,T,M+OK).

expand(Lvl,[opt(_)|T],V) -->
  expand(Lvl,T,V).
expand(Lvl,[opt(S)|T],M+OK) -->
  {once((member(V,M),var(V))),V=done},
  expand(Lvl,S,M+OK),
  expand(Lvl,T,M+OK).
expand(Lvl,[nonterminal(N)|T],V) -->
  {sRule(N,RHS,Lvl,_,_)},
  expand(Lvl,RHS,V),
  expand(Lvl,T,V).
expand(Lvl,[terminal(X)|T],V) -->
  {X \= uriref},
  [terminal(X)],
  expand(Lvl,T,V).
expand(Lvl,[open(ID),terminal(uriref)|T],V) -->
   [open(ID),ID+_],
   expand(Lvl,T,V).

numberZ(L) :-
   bagof(X+Vs,setof(V,member(X+V,L),Vs),Bag),
   !,
   checklist(numberX,Bag).
numberZ(_).
numberX(_+[0]):-!.
numberX(_+NN) :-
  count(1,NN).
count(_,[]).
count(N,[N|T]) :-
   N1 is N + 1,
   count(N1,T).

syntactic(open(D),OK) :-
   member(D,[restriction,oneOf,complementOf,unionOf,intersectionOf]),
   %(D==description;D==restriction),
   !,
   var(OK),
   OK=done.
syntactic(X,_) :-
  syntactic(X).

blank(restriction,restriction).
blank(description,description).
blank(dataRange,unnamedDataRange).
blank(individual,unnamedIndividual).

