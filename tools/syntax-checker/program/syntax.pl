
/*********

Program for sorting out the syntax rules.

********/


:- dynamic sRule/5.
% sRule(Head,Body,Lvl,OrigNumber,NewNumber)

:- dynamic symbol2Letter/2.
% symbol2Letter(Symbol,Letter)
:- dynamic token/1.

compSyntax :-
   retractall(sRule(_,_,_,_,_)),
   retractall(symbol2Letter(_,_)),
   retractall(token(_)),
   compSyntax(lite),
   copyLite2DL,
   compSyntax(dl),
   checkLeftCorner.

compSyntax(Level) :-
   uriref(_,Symbol,Level,OrigNumber),
   flag(letter,_,65),
   atom_concat(Symbol,'ID',SymID),
   RHS =.. [SymID,uriref],
   compRule(SymID,RHS,Level,OrigNumber),
   fail.
compSyntax(Level) :-
   syntax(Head,RHS,Level-N),
   compRule(Head,RHS,Level,Level-N),
   fail.
compSyntax(Level) :-
   sRule(S,RHSList,Level,_,_),
   sanity(S,RHSList,Level),
   fail.
compSyntax(_).

copyLite2DL :-
   sRule(H,B,lite,ON,NN),
   assertz(sRule(H,B,dl,ON,NN)),
   fail.
copyLite2DL.

compRule(S,RHS,Level,OrigNumber) :-
   getNewNumber(S,Level,NewNumber),
   expandRHS(RHS,RHSList),
   assertz(sRule(S,RHSList,Level,OrigNumber,NewNumber)).

getNewNumber(Sym,Level,Let-N) :-
   getLetter(Sym,Let),
   findall(a,sRule(_,_,Level,_,Let-_),Bag),
   length(Bag,N0),
   N is N0 + 1.
getLetter(Sym,Let) :-
   symbol2Letter(Sym,Let),
   !.
getLetter(Sym,Let) :-
   flag(letter,N,N+1),
   name(Let,[N]),
   assertz(symbol2Letter(Sym,Let)).

% expandRHS(RHS,RHSAsList).

expandRHS(RHS,Exp) :-
  expandRHS(RHS,Exp,[]),
  !.
expandRHS(RHS,_) :-
  throw(compileFailure(RHS)).


expandRHS([Opt])-->
    !,  
    {expandRHS(Opt,OptX)},
    [opt(OptX)].

expandRHS({[]})-->
    !,  
    [].
expandRHS({Opt})-->
    !,  
    {expandRHS(Opt,OptX)},
    [star(OptX)].

expandRHS(ID) -->
   {uriref(ID,Desc,_,_),
   atom_concat(Desc,'ID',SymID)},
   !,
   [nonterminal(SymID)].
expandRHS(Terminal) -->
   {terminal(Terminal)},
   !,
   [terminal(Terminal)].
expandRHS(+Token) -->
   {assertOnce(token(Token))},
   [token(Token)].
expandRHS(Q:Name) -->
   !,
   {caseAdjustBuiltin(Q,Name,NName) },
   [Q:NName].

expandRHS(Term-Sub) -->
   !,
   {checkName(Term,B) },
   [nonterminal(B,Sub)].
expandRHS(Expression) -->
   { Expression =.. [A,A1|T],
     checkName(A,B) },
   !,
   [open(B)],
   expandRHSAll([A1|T]),
   [close(B)].
expandRHS(NT) -->
   [nonterminal(NT)].

expandRHSAll([]) --> [].
expandRHSAll([H|T]) -->
   expandRHS(H),
   expandRHSAll(T).
   
  
checkName(L,U) :-
   uriref(L,Sym,_,_),
   atom_concat(Sym,'ID',U).

checkName(A,A) :-
   name(A,L),
   length(L,N),
   L = [H|_],
   N >= 4,
   H >= "a",
   H =< "z".
checkName(seq,seq).
checkName(A,_) :-
  throw(badName(A)).


sanity(Symbol,RHS,Level) :-
   member(star(RHSX),RHS),
   sanity(Symbol,RHSX,Level),
   fail.
sanity(Symbol,RHS,Level) :-
   member(opt(RHSX),RHS),
   sanity(Symbol,RHSX,Level),
   fail.
sanity(Symbol,RHS,Level) :-
   member(nonterminal(NT),RHS),
   \+ sRule(NT,_,Level,_,_),
   throw(undefined(NT,Symbol,Level)).
sanity(ontology,_,_) :- !.
sanity(S,_,Lvl) :-
  sRule(_,RHS,Lvl,_,_),
  occurs(S,RHS),
  !.
sanity(S,_,Lvl) :-
  throw(unused(S,Lvl)).

occurs(S,RHS) :-
  member(nonterminal(S),RHS).
occurs(S,RHS) :-
  member(star(RHSX),RHS),
  occurs(S,RHSX).
occurs(S,RHS) :-
  member(opt(RHSX),RHS),
  occurs(S,RHSX).


leftcorner(Look,[nonterminal(S)|_]):-
    member(S,Look),
    throw(leftcorner(S)).
leftcorner(S,[star(L)|_]) :-
  leftcorner(S,L).
leftcorner(S,[star(_)|T]) :-
  leftcorner(S,T).
leftcorner(S,[opt(L)|_]) :-
  leftcorner(S,L).
leftcorner(S,[opt(_)|T]):-
  leftcorner(S,T).
leftcorner(S,[nonterminal(Sym)|_]) :-
  sRule(Sym,RHS,dl,_,_),
  leftcorner([Sym|S],RHS).

checkLeftCorner :-
  leftcorner([],[nonterminal(_)]).
checkLeftCorner.


% needs to check that F is in Namespace, possibly upcasing first letter.
caseAdjustBuiltin(_,builtin,builtin) :- !.
caseAdjustBuiltin(Namespace,F,F) :-
   builtin(Namespace,F), !.
caseAdjustBuiltin(Namespace,F,Upcased) :-
   capitalize(F,Upcased),
   builtin(Namespace,Upcased), !.
caseAdjustBuiltin(Namespace,F,_) :-
   throw(builtin(Namespace,F)).
