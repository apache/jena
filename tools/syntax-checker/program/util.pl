
   
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

  