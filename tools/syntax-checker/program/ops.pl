
:- multifile syntax/3.
:- discontiguous syntax/3.
:- discontiguous mapping/6.
:- discontiguous comment/4.
:- discontiguous uriref/4.
:- multifile comment/4.
:- dynamic syntax/3.
:- dynamic saved/2.
:- dynamic lastSymbol/1.

:-dynamic lastSymbol/1.
:-dynamic makeLink/2.
:-dynamic linked/1.

:-multifile terminal/1.



:-op(150,xfy,@).
:-op(150,xfy,^^).
:-op(1200,xfx,'::=').
:- dynamic (::=)/2.
:- discontiguous (::=)/2.