
dl_only(lite,_Text).
dl_only(_,Text) :-
  write(Text).
  
lemma(Lemma,Sort) :-
  concat_atom(Parts,' ',Lemma),
  maplist(capitalize,Parts,PartsX),
  concat_atom(PartsX,' ',LemmaX),
  concat_atom([Sort|Parts],'-',IDx),
  downcase_atom(IDx,ID),
  capitalize(Sort,SortX),
  wlist(['<dl><dt><a name="',ID,'" id="',ID,'"><strong>',nl,
         LemmaX,' ',SortX,'</strong></a></dt>',nl]).
lemma(Lemma) :-
  lemma(Lemma,lemma).
theorem(Theorem) :-
  lemma(Theorem,theorem).
linkLemma(Lemma) :-
  downcase_atom(Lemma,Lemmax),
  concat_atom(Parts,' ',Lemmax),
  concat_atom([lemma|Parts],'-',ID),
  wlist(['<a href="#',ID,'">',Lemmax,' lemma</a>',nl]).

cat(File,Lvl) :-
   concat_atom(['doc-frags/',File,'.html'],FF),
   see(FF),
   repeat,
     get0(X),
     
     (X= -1;
      (X=37,peek_byte(37)->
         get_byte(37),read_term(T,[variable_names(Vs)]),
         ignore(member('Lvl'=Lvl,Vs)),
         once(T),
         fail
      ;
     put(X),fail) ),
   !,
   seen.

cat2(File,Lvl) :-
%   wlist(['<p>Source: <strong>',File,'</strong></p>']),
   cat(File,Lvl),
   wlist(['<span class="todo">add machine generated table</span>',nl]).

% out(+ListOfFileBits,+ListOfCommands)
out(File,Commands) :-
   append(File,['.html'],Filex),
   File=[Lvl|_],
   concat_atom(['out/'|Filex],FF),
   make_directory_for_file(FF),
   wuser(['Creating ',FF,nl]),
   tell(FF),told,  % force close
   tell(FF),
   member(C,Commands),
   (C=cat(X)->cat(X,Lvl);
    C=cat2(X)->cat2(X,Lvl);
    C),
   fail;
   told.


out(Lvl) :-
   out([Lvl,'/',syntax],
   [
     cat(header),
     nav('../'),
     title(2,[Lvl,' Abstract Syntax']),
     cat('syntax-intro'),
     htmlSyntax(Lvl),
     cat(trailer) ] ),
   out([Lvl,'/',mapping],
   [
     cat(header),
     nav('../'),
     title(2,[Lvl,' Mapping Rules']),
     cat('mapping-intro'),
     map(Lvl),
     cat(trailer) ] ),

   out([Lvl,'/',triples],
   [
     cat(header),
     nav('../'),
     title(2,[Lvl,' as RDF Graphs']),
     startDefinition(Lvl),
     cat(definition1),
     urirefCategories(Lvl),
     cat(definition2),
     blankCategories(Lvl),
     cat(definition3),
     tableList(Lvl),
     cat(definition4),
     triples(Lvl),
     auxilliaryTriples(Lvl),
     blanks(Lvl),
     cat(trailer) ] )

/*,

   out([Lvl,'/',proof1],
   [
     cat(header),
     nav('../'),
     title(2,[Lvl,' Sufficiency Proof']),
  %   defineAcceptable(Lvl),
     cat('proof-intro'),
     title(3,['Initial Lemmas']),
     cat2('proof-red-10'),
   % explicitTypeTable(Lvl),
     title(3,['Reduction Lemmas']),
     cat2('proof-red-20'),
     cat2('proof-red-30'),
     cat2('proof-red-40'),
     cat2('proof-red-50'),
     cat2('proof-red-60'),
     cat2('proof-red-70'),
     cat2('proof-red-80'),
     cat2('proof-red-90'),
     cat2('proof-red-100'),
     (Lvl=dl->cat2('proof-red-110',dl);true),
     (Lvl=dl->cat2('proof-red-120',dl);true),
     title(3,['Structural Lemmas']),
     cat('proof-struct'),
     title(3,['Main Result']),
     cat('proof-last'),
     cat(trailer) ] ) */ .

out :-
  out(lite),
  out(dl),
  out([intro],[
    cat(header),
     nav(''),
    cat(intro),
    cat(trailer)]),
  out([diffs],[
    cat(header),
     nav(''),
    cat(diffs),
    cat(trailer)]).

title(N,L) :-
  concat_atom(L,Title),
  name(Title,TName),
  maplist(space2dash,TName,AName),
  name(Anchor,AName),
  wlist(['<h',N,'> <a id="',Anchor,'" name="',Anchor,'">',nl,
         Title,'</h',N,'>']).


space2dash(32,45) :- !.
space2dash(X,X).

startDefinition(Lvl) :-
  wlist(['<p><a name="dfn-owl-',Lvl,'-rdf-graph" id="dfn-owl-',Lvl,'-rdf-graph">',nl
         ,'An OWL Full document  <span class="todo">[Add definition and link]</span> 
          is an OWL ',Lvl,' Ontology if and only if the
          <a href="http://www.w3.org/TR/owl-semantics/rdfs.html#RDF_graph_imports_closure">imports closure</a> of the
         RDF graph meets the following       conditions:</p>']).

urirefCategories(Lvl) :-
   uriref(_,C,_,_),
   C \= ontologyProperty,
   C \= dataRange,
   call(((Lvl=dl;C\=dataRange),!)),
   wlist(['<li>',C,'ID</li>',nl]),
   fail;
   true.


nav(Px) :-
  title(2,['Navigation']),
  wlist(['<table><tr>',nl]),
  navTo(Px,'intro.html','Introduction'),
  navTo(Px,'diffs.html','Differences'),
  navTo(Px,'src.zip','Source'),
  wlist(['</tr>',nl,'<tr>',nl]),
  navTo(Px,'lite/syntax.html','Lite Syntax'),
  navTo(Px,'lite/mapping.html','Lite Mapping'),
  navTo(Px,'lite/triples.html','Lite Triples'),
  navTo(Px,'lite/proof1.html','Lite Proof'),
  wlist(['</tr>',nl,'<tr>',nl]),
  navTo(Px,'dl/syntax.html','DL Syntax'),
  navTo(Px,'dl/mapping.html','DL Mapping'),
  navTo(Px,'dl/triples.html','DL Triples'),
  navTo(Px,'dl/proof1.html','DL Proof'),
  wlist(['</tr>',nl,'</table>',nl]).
navTo(Px,File,Title) :-
  wlist(['<td><a href="',Px,File,'">',Title,'</a></td>',nl]).
  