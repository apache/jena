/*
	(c) Copyright 2003 Hewlett-Packard Development Company, LP
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:

	1. Redistributions of source code must retain the above copyright
	   notice, this list of conditions and the following disclaimer.

	2. Redistributions in binary form must reproduce the above copyright
	   notice, this list of conditions and the following disclaimer in the
	   documentation and/or other materials provided with the distribution.

	3. The name of the author may not be used to endorse or promote products
	   derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
%% Given an abs syntax structure
%% Map it to triples.

apply(Lvl,List,Triples,Rules,Builtins) :-
   List = [_|_],
   !,
   apply(Lvl,void,_,_,List,TrRules,[]),
   sublist(isRule,TrRules,Rules),
   sublist(isTriple,TrRules,Triples),
   sublist(isBuiltin,TrRules,Builtins).

apply(Lvl,Funct,Triples,Rules) :-
   expandRHS(Funct,List),
   apply(Lvl,List,Triples,Rules).

isRule(rule(_)).
isTriple(t(_,_,_)).
   

% apply(Lvl,NAV,Up,Ret,Expr,Tr0,Tr1)
apply(_Lvl,node,_,Ret,[nonterminate(_,Ret)],T,T).
apply(Lvl,V,_,_,List,[rule(ID)|Tr0],Tr1) :-
  (V=void;V=reallyvoid),
  maybeStartsWithOpen(List,LL),
  mRule0(void,LL,_Triples,Divided,void,Lvl,ID),
  addAssignment(Assignments,blank,_),
  applyDetail(List,Divided,Assignments,Lvl,Tr0,Tr1).
apply(Lvl,NV,_,Ret,List,[rule(ID)|Tr0],Tr2) :-
  (NV=void;NV=node),
  maybeStartsWithOpen(List,LL),
  mRule0(node,LL,_Triples,Divided,Rslt,Lvl,ID),
  addAssignment(Assignments,blank,_),
  applyDetail(List,Divided,Assignments,Lvl,Tr0,Tr1),
  applyItem(Lvl,Assignments,Rslt,Ret,Tr1,Tr2).
apply(Lvl,arg,Up,_,List,[rule(ID)|Tr0],Tr1) :-
  addAssignment(Assignments,blank,_),
  addAssignment(Assignments,up,Up),
  maybeStartsWithOpen(List,LL),
  mRule0(arg,LL,_Triples,Divided,void,Lvl,ID),
  applyDetail(List,Divided,Assignments,Lvl,Tr0,Tr1).

maybeStartsWithOpen([open(X)|_],[open(X)|_]) :- !.
maybeStartsWithOpen(_,_).


match([],L0,L0,_,_).
match([X|L],[X|LL],TT,Assignments,Lvl) :-
  syntacticExpr(X),
  match(L,LL,TT,Assignments,Lvl).
match([terminal(uriref)|L],[Q:B|LL],TT,A,Lvl) :-
  addAssignment(A,uriref,Q:B),
  match(L,LL,TT,A,Lvl).
match([terminal(uriref)|L],[nonterminal(M)|LL],TT,A,Lvl) :-
  addAssignment(A,uriref,M),
  match(L,LL,TT,A,Lvl).
match([Q:Name|L],[M+S|LL],TT,A,Lvl) :-
  addAssignment(A,Q:Name,M+S),
  match(L,LL,TT,A,Lvl).
match([terminal(uriref)|L],[M+S|LL],TT,A,Lvl) :-
  addAssignment(A,uriref,M+S),
  match(L,LL,TT,A,Lvl).
/*
match([Q:builtin|L],[Q:Name|LL],TT,A,Lvl) :-
  addAssignment(A,builtin,Name),
  match(L,LL,TT,A,Lvl).
*/
match([nonterminal(NT)|L],LL,TTT,A,Lvl) :-
  match([nonterminal(NT,0)|L],LL,TTT,A,Lvl).
match([nonterminal(NT,Sub)|L],[nonterminate(NT,NTX)|TT],TTT,A,Lvl) :-
  addAssignment(A,x([nonterminal(NT,Sub)]),NTX),
  match(L,TT,TTT,A,Lvl).

match([nonterminal(NT,Sub)|L],LL,TTT,A,Lvl) :-
  sRule(NT,L2,Lvl,_,_),
  match(L2,LL,TT,noassignments,Lvl),
  funnyAppend(X,TT,LL),
  addAssignment(A,NT+Sub,X),
  match(L,TT,TTT,A,Lvl).
match([opt(L2)|L],LL,TTT,A,Lvl) :-
  match(L2,LL,TT,A,Lvl),
  match(L,TT,TTT,A,Lvl).
match([opt(_L2)|L],LL,TTT,A,Lvl) :-
 % prematch(L2,none,A),
  match(L,LL,TTT,A,Lvl).
match([star(L2)|L],LL,TTT,A,Lvl) :-
  prematch(L2,star,A),
  manyMatch(L2,LL,TT,A,Lvl,_,_),
  prematch(L2,endstar,A),
  match(L,TT,TTT,A,Lvl).

manyMatch(_L,LL,LL,_A,_Lvl,T,T).
manyMatch(L,LL,TTT,A,Lvl,Tr0,TrB) :-
   match(L,LL,TT,A,Lvl),
  builtins(A,Tr0,TrA),
   manyMatch(L,TT,TTT,A,Lvl,TrA,TrB).
  
builtins(S,T,T) :-
  nonvar(S),
  S=star(_,_).
builtins(A,Tr0,Tr1) :-
   append(X,V,A),
   \+ \+ V=[],
   !,
   sublist(qname,X,Q),
   maplist(qname,Q,QQ),
   append(QQ,Tr1,Tr0).
qname(_:_=_+_).
qname(Q:Name=Var+Sub,builtin(Var,Sub,Q,Name)).



syntacticExpr(open(_)).
syntacticExpr(close(_)).
syntacticExpr(_:_).
syntacticExpr(token(_)).
syntacticExpr(terminal(_)).

funnyAppend(A,B,C) :-
   append(A,BB,C),
   B == BB,
   !.


prematch([],_,_).
prematch([X|T],Ty,A) :-
   syntactic(X),
   prematch(T,Ty,A).
prematch([nonterminal(N,Sub)|T],Ty,A) :-
   addAssignment(A,N+Sub,Ty),
   prematch(T,Ty,A).
prematch([nonterminal(N)|T],Ty,A) :-
   addAssignment(A,N+0,Ty),
   prematch(T,Ty,A).
prematch([terminal(N)|T],Ty,A) :-
   addAssignment(A,N+0,Ty),
   prematch(T,Ty,A).
prematch([_:builtin|T],Ty,A) :-
   addAssignment(A,builtin,Ty),
   prematch(T,Ty,A).

syntactic(open(_)).
syntactic(close(_)).
syntactic(token(_)).


addAssignment(A,_,_) :-
   A == noassignments,
   !.
addAssignment(Star,A,V) :-
  nonvar(Star),
  Star = star(Main,Sub),
  !,
  (member(XX,Sub),
      (var(XX),!,addAssignment(Main,A,V)
        
        ; XX=(A=VV), !, V=VV ) ).
addAssignment(A,V,Star) :-
   Star == star,
   once(member(V=Val,A)),
   !,
   Val = star(_).
addAssignment(A,V,Endstar) :-
   Endstar == endstar,
   once(member(V=Val,A)),
   !,
   Val = star(L),
   once(append(_,[],L)).
addAssignment(A,K,Val) :-
   once(member(K=Val0,A)),
   assignVal(Val0,Val).

assignVal(V,V) :- (var(V);V\=star(_)), !.
assignVal(star(L),V) :-
   member(Var,L),
   var(Var),
   !,
   V = Var.
assignVal(star(L),L).

   
starAssignments(A,star(_,A)).



% applyDetail(List,Divided,Assignments,Lvl,Tr1,Tr2).
  

applyDetail([],[],_Assignments,_Lvl,Tr,Tr).
applyDetail(List,[D|DD],A,Lvl,Tr0,Tr2) :-
  div(D,L,T,simple),
  match(L,List,TT,A,Lvl),
  builtins(A,Tr0,TrA),
  applyTriples(T,A,Lvl,TrA,Tr1),
  applyDetail(TT,DD,A,Lvl,Tr1,Tr2).
applyDetail(List,[D|DD],A,Lvl,Tr0,Tr2) :-
  div(D,L,T,star),
  starAssignments(A,Astar),
  match(L,List,TT,Astar,Lvl),
  builtins(Astar,Tr0,TrA),
  applyTriples(T,Astar,Lvl,TrA,Tr1),
  applyDetail(TT,[D|DD],A,Lvl,Tr1,Tr2).
applyDetail(List,[D|DD],A,Lvl,Tr0,Tr2) :-
  div(D,_L,_T,star),
  applyDetail(List,DD,A,Lvl,Tr0,Tr2).
applyDetail(List,[D|DD],A,Lvl,Tr0,Tr2) :-
  div(D,L,T,opt),
  match(L,List,TT,A,Lvl),
  builtins(A,Tr0,TrA),
  applyTriples(T,A,Lvl,TrA,Tr1),
  applyDetail(TT,DD,A,Lvl,Tr1,Tr2).
applyDetail(List,[D|DD],A,Lvl,Tr0,Tr2) :-
  div(D,_L,_T,opt),
  applyDetail(List,DD,A,Lvl,Tr0,Tr2).
applyDetail(List,[D|DD],A,Lvl,Tr0,Tr2) :-
  div(D,L,T,seq),
  prematch(L,star,A),
  manyMatch(L,List,TT,A,Lvl,Tr0,TrA),
  prematch(L,endstar,A),
  applyTriples(T,A,Lvl,TrA,Tr1),
  applyDetail(TT,DD,A,Lvl,Tr1,Tr2).
  
  



div(sub(L)+true,L,[],simple).
div(sub(L)+sub(T,_),L,T,simple).
div(star(sub(L))+star(sub(T,_)),L,T,star).
div(star(sub(L))+seq(sub(T,_)),L,T,seq).
div(opt(sub(L))+opt(sub(T,_)),L,T,opt).


% applyTriples(Triples,Assignments,Lvl,Tr0,Tr1)

applyTriples([],_,_,T,T).
applyTriples([H|R],A,Lvl,T0,T2) :-
  applyTriple(H,A,Lvl,T0,T1),
  applyTriples(R,A,Lvl,T1,T2).
applyTriple(t(S,P,O),A,Lvl,[t(SS,PP,OO)|T0],T3) :-
  applyItem(Lvl,A,S,SS,T0,T1),
  applyItem(Lvl,A,P,PP,T1,T2),
  applyItem(Lvl,A,O,OO,T2,T3).
applyTriple(opt(_T),_A,_Lvl,T,T).
applyTriple(opt(T),A,Lvl,T0,T1) :-
  applyTriple(T,A,Lvl,T0,T1).
applyTriple(x(X),A,Lvl,T0,T1) :-
  applyItem(Lvl,A,x(X),_,T0,T1).
applyTriple(x(X),A,Lvl,T0,T1) :-
   beltAndBracesSubscript(X,XX),
   addAssignment(A,x(XX),reallyvoid),
   expandNT(A,X,XX),
   apply(Lvl,reallyvoid,_,_,XX,T0,T1).

applyTriple(x(U,X),A,Lvl,T0,T2) :-
   applyItem(Lvl,A,U,UU,T0,T1),
   expandNT(A,X,XX),
   apply(Lvl,arg,UU,_,XX,T1,T2).

  

fixed(_:_).
fixed(_@_).
fixed(_^^_).
%fixed(uriref).
fixed(lexicalForm).
applyItem(_Lvl,_A,X,X,T,T) :-
    fixed(X).
applyItem(_Lvl,A,blank,B,T,T) :-
   addAssignment(A,blank,B).
applyItem(_Lvl,A,Any,B,T,T) :-
   Any \= _:_,
   addAssignment(A,Any,B),nonvar(B).
applyItem(Lvl,A,x(X),R,T0,T1) :-
   beltAndBracesSubscript(X,XX),
   addAssignment(A,x(XX),V),
   (var(V)->resolveX(Lvl,A,X,R,T0,T1),
   V=R;V=R).

beltAndBracesSubscript([nonterminal(X)],[nonterminal(X,0)]) :- !.
beltAndBracesSubscript(A,A).

resolveX(_,_,[nonterminal(dataLiteral)],literal,T,T) :-
   !.
/*
resolveX(_,_,[nonterminal(NT)],R,T,T) :-
   blankNode(NT,_,R).
resolveX(_,_,[nonterminal(NT,Sub)],R,T,T) :-
   blankNode(NT,Sub,R).
*/

resolveX(_,_,[open(seq),star(_), close(seq)],rdf:nil,T,T).
resolveX(Lvl,_,[open(seq),star([nonterminal(Item)]), close(seq)],R,T,T) :-
   blankNode(Lvl,seq(Item),_,R).
resolveX(Lvl,_,[open(seq),star([nonterminal(Item,Sub)]), close(seq)],R,T,T) :-
   blankNode(Lvl,seq(Item),Sub,R).
resolveX(Lvl,A,X,R,T0,T1) :-
   expandNT(A,X,XX),
   apply(Lvl,node,_,R,XX,T0,T1).



blankNode(Lvl,A,S,B) :-
   var(S),
   flag(bn,S,S+1),
   blankNode2(Lvl,A,S,B).
blankNode(Lvl,A,S,B) :-
   nonvar(S),
   blankNode2(Lvl,A,S,B).

blankNode2(_,individual,Sub,unnamedIndividual+Sub).
blankNode2(dl,description,Sub,description+Sub).
blankNode2(_,restriction,Sub,restriction+Sub).
blankNode2(dl,dataRange,Sub,unnamedDataRange+Sub).

blankNode2(_,seq(individualID),Sub,listOfIndividualID+Sub).
blankNode2(dl,seq(dataLiteral),Sub,listOfDataLiteral+Sub).
blankNode2(_,seq(description),Sub,listOfDescription+Sub).
   

expandNT(_A,[],[]).
expandNT(A,[nonterminal(N)|T],L) :-
   !,
   expandNT(A,[nonterminal(N,0)|T],L).
expandNT(A,[nonterminal(N,Sub)|T],L) :-
   !,
   addAssignment(A,N+Sub,X),
   expandNT(A,T,TT),
   append(X,TT,L).
expandNT(A,[X|T],[X|TT]) :-
   expandNT(A,T,TT).


  

