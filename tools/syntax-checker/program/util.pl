
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
wlist([nl|T]) :- !, nl, wlist(T).
wlist([sp(N)|T]) :-
   !,
   N1 is N,
   spaces(N1),
   wlist(T).
wlist([X|T]) :- write(X), wlist(T).
wlist([]).

spaces(N) :- N < 1, !.
spaces(N) :- put(" "),N1 is N - 1, spaces(N1).
wuser(L) :-
   telling(T),
   tell(user),
   wlist(L),
   tell(T).




capitalize(F,Upcased) :-
   sub_atom(F,1,_,0,Rest),
   sub_atom(F,0,1,_,First),
   upcase_atom(First,Up),
   atom_concat(Up,Rest,Upcased).

mustBe(Goal) :- \+ \+ Goal, !.
mustBe(Goal) :- throw(assertionFailure(Goal)).

assertOnce(G) :-
    G, !.
assertOnce(G) :- assertz(G).

make_directory_for_file(FileName) :-
  concat_atom(AAF,'/',FileName),
  append(L,[_],AAF),
  (L=[];mkdir(L)),
  !.

mkdir(A) :-
  atomic(A),
  concat_atom(AA,'/',A),
  mkdir(AA).

mkdir([H|T]) :-
  mkdir(H,T).
mkdir(H,T) :-
  (exists_directory(H);make_directory(H)),
  !,
  (T=[];
    T=[A|B],
    concat_atom([H,'/',A],HH),
    mkdir(HH,B)).

  