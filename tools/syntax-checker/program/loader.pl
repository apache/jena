
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
  load(program,[ops,apply,util,syntax,map,
  %html,out,prove,
  checker]),
  load(grammar,[lite,dl,mapping,builtin,uri]).
  