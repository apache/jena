

% minimal declaration for this file.
% main declarations are in ops.
:- dynamic loadingFrom/1.
:- op(1200,xfx,'::=').
:- dynamic ruleCount/2.

% for AS rule
term_expansion((A::=B),syntax(A,B,File-N)) :-
  loadingFrom(File),
  flag(rule,N,N+1).


% for mapping rule
term_expansion( (A->B;C), mapping(node,void,A,B,C,File-N) ) :-
  loadingFrom(File),
  flag(rule,N,N+1).
term_expansion( (BN+A->B), mapping(arg,BN,A,B,void,File-N) ) :-
  loadingFrom(File),
  checkVar(BN),
  flag(rule,N,N+1).
term_expansion( (A->B), mapping(void,void,A,B,void,File-N) ) :-
  loadingFrom(File),
  flag(rule,N,N+1).
term_expansion( (A->B;C), mapping(node,void,A,B,C,File-N) ) :-
  loadingFrom(File),
  flag(rule,N,N+1).

checkVar(BN) :-
  var(BN).
checkVar(BN) :-
  throw(mustBeVar(BN)).

% for uriref

term_expansion( uriref(U,V,L), uriref(U,V,L,File-N) ) :-
  loadingFrom(File),
  flag(rule,N,N+1).

term_expansion( comment(Lvl,Text), comment(File,N,Lvl,Text) ) :-
  loadingFrom(File),
  flag(rule,N,N).



load(_Dir,[]).
load(Dir,[H|T]) :-
   concat_atom([Dir,('/'),H],File),
   retractall(loadingFrom(_)),
   assert(loadingFrom(H)),
   flag(rule,_,1),
   consult(File),
   flag(rule,N,N),
   assertIfNotOne(N,ruleCount(H,N)),
   load(Dir,T).

assertIfNotOne(1,_) :- !.
assertIfNotOne(_,G) :- assert(G).

load :-
  retractall(ruleCount(_,_)),
  load(program,[ops,apply,util,syntax,map,html,out,prove,checker]),
  load(grammar,[lite,dl,mapping,builtin,uri]).
  