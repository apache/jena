

/* 
Rule format:
 one of:
AbstractSyntaxExpr -> Triples.
AbstractSyntaxExpr -> Triples; MainNode .
SubjNode+AbstractSyntax -> Triples.


Internal syntax
   id-subscript
   note(link)/thingy
*/

:-op(150,xfy,@).
:-op(150,xfy,^^).

comment(1, heading(1,'URI References')).
comment(2, 'The following rules are ones that use a node provided from higher
 in the abstract syntax tree. This node is shown as <code><strong>@</strong></code>.
It is always the subject of the triples.').

o(O) -> 
        t(O,rdf:type,owl:ontology);      O.
dt(DT) -> 
         t(DT,rdf:type,rdfs:datatype);  DT.

dt(rdf:xMLLiteral) -> true;              rdf:xMLLiteral.

dt(xsd:Builtin) -> true;                 xsd:Builtin.

%dt(Any) ->
%         t(Any, rdf:type, rdfs:datatype ); Any.


c(owl:thing) -> true;                    owl:thing.
c(owl:nothing) -> true;                    owl:nothing.

c(C) -> 
         t(C,rdf:type,owl:class); C.
 %        +[t(C,rdf:type,rdfs:class)];     C.

i(I) -> note(thing)/(+[t(I,rdf:type,owl:thing)]);                            I.

m(owl:Builtin) -> true;  owl:Builtin.
m(M) -> t(M,rdf:type,owl:ontologyProperty);    M.
%m(rdfs:Builtin) -> true;                 rdfs:Builtin .

dr(rdfs:literal) -> true;                rdfs:literal.
/*
dr(DR) -> 
         note(dataRange)/t(DR,rdf:type,owl:dataRange);   DR.
*/
cdp(DP) -> 
          t(DP,rdf:type,owl:datatypeProperty); DP.
%          +[t(DP,rdf:type,rdf:property)]; DP.

a(OP) -> 
          t(OP,rdf:type,owl:annotationProperty); OP.
da(OP) -> 
          +[t(OP,rdf:type,owl:annotationProperty)]; OP.

%,        +[t(OP,rdf:type,rdf:property)];      OP.

a(rdfs:Builtin) -> true; rdfs:Builtin .

cop(OP) -> 
          note(prop)/(+[t(OP,rdf:type,owl:objectProperty)]); OP.
%          +[t(OP,rdf:type,rdf:property)]; OP.

tp(OP) -> 
          note(prop)/(+[t(OP,rdf:type,owl:objectProperty)]); OP.
%,         +[t(OP,rdf:type,rdf:property)]; OP.
comment(1, heading(1,'Literals')).
% dataLiteral(lexicalForm,language,dt)->true; lexicalForm@language^^x(dt).
dataLiteral(lexicalForm,dt)->true; lexicalForm^^x(dt).
dataLiteral(lexicalForm,language)->true; lexicalForm@language.
dataLiteral(lexicalForm)->true; lexicalForm.
         

comment(1, heading(1,'Ontologies')).

ontology( { directive } ) -> 
               {note(directive)/x(directive)}.


comment(2, heading(1,'Header and Annotations')).
header(o,{metaPropValue},{annotation}) -> 
          t(x(o),rdf:type,owl:ontology),
          {x(x(o),metaPropValue)},
          {x(x(o),annotation)}.
header({metaPropValue},{annotation}) -> 
          t(blank,rdf:type,owl:ontology),
          {x(blank,metaPropValue)},
          {x(blank,annotation)}; blank.

D+metaPropValue( m, o ) -> 
          t(D,x(m),x(o) ).

D + annotation( a, dataLiteral ) -> 
          t(D,x(a),x(dataLiteral)).
D + annotation( da, dataLiteral ) -> 
          t(D,x(da),x(dataLiteral)).
D + annotation( a, individual ) ->
          t(D,x(a),x(individual) ).


D + annotation( a, a ) ->
          t(D,x(a),x(a) ).

D + annotation( a, m ) ->
          t(D,x(a),x(m) ).
D + annotation( a, o ) ->
          t(D,x(a),x(o) ).
D + annotation( a, c ) ->
          t(D,x(a),x(c) ).
D + annotation( a, cdp ) ->
          t(D,x(a),x(cdp) ).
D + annotation( a, tp ) ->
          t(D,x(a),x(tp) ).
D + annotation( a, cop ) ->
          t(D,x(a),x(cop) ).
D + annotation( a, i ) ->
          t(D,x(a),x(i) ).
D + annotation( a, dt ) ->
          t(D,x(a),x(dt) ).
D + annotation( a, dr ) ->
          t(D,x(a),x(dr) ).


comment(1, heading(1,individuals)).
comment(1, heading(2,'Named Individuals')).
/*
comment(1, 'The interaction between these two individual rules make it obligatory 
to declare at least one type for a named individual.
The default is <code>owl:thing</code>').
*/

individual( i, {annotation}, {type( description )}, 
            {value}, {individualComparison} ) ->
       {x(x(i),annotation)},
       {t(x(i),rdf:type,x(description))},
       {x(x(i),value)},
       {x(x(i),individualComparison)};            x(i).

comment(1, heading(2,'Unnamed Individuals')).

 
individual( {annotation}, {type( description )}, 
            {value} ) ->
       note(thing)/(+[t(blank,rdf:type,owl:thing)]),
       {x(blank,annotation)},
       {t(blank,rdf:type,x(description))},
       {x(blank,value)};            blank.   

annotationProperty( a, { annotation } ) ->
    t(x(a),rdf:type,owl:annotationProperty),
    +[t(x(a),rdf:type,rdf:property)],
    {x(x(a),annotation)}.
ontologyProperty( m, { annotation } ) ->
    t(x(m),rdf:type,owl:ontologyProperty),
    +[t(x(m),rdf:type,rdf:property)],
    {x(x(m),annotation)}.



comment(2, heading(1,'Property Values')).

D+value(cop,individual) ->
       t(D,x(cop),x(individual)).
D+value(tp,individual) ->
       t(D,x(tp),x(individual)).
D+value(cdp,dataLiteral) ->
       t(D,x(cdp),x(dataLiteral)).


comment(2, heading(1,'Comparison of Individuals')).

D+sameIndividual(i)->
   t(D,owl:sameAs,x(i)).
D+differentIndividuals(i)->
   t(D,owl:differentFrom,x(i)).

comment(1, heading(1,'Classes')).


class(c,  [+deprecated], +partial, {annotation},
                    {super(description)}
   ) ->
   t(x(c),rdf:type,owl:class),
   +[t(x(c),rdf:type,rdfs:class)],
   [t(x(c),rdf:type,owl:deprecatedClass)],
   {x(x(c),annotation)},
   {t(x(c),rdfs:subClassOf,x(description))};     x(c).

class(c, [+deprecated], +complete, {annotation},
                     {super(description)}
   ) ->
   t(x(c),rdf:type,owl:class),
   +[t(x(c),rdf:type,rdfs:class)],
   [t(x(c),rdf:type,owl:deprecatedClass)],
   {x(x(c),annotation)},
   t(x(c),owl:intersectionOf,x(seq({description})));     x(c).

class(c, [+deprecated], +complete, {annotation},
                    super(description)
   ) ->
   t(x(c),rdf:type,owl:class),
   +[t(x(c),rdf:type,rdfs:class)],
   [t(x(c),rdf:type,owl:deprecatedClass)],
   {x(x(c),annotation)},
   t(x(c),owl:equivalentClass,x(description));     x(c).

class(c,  [+deprecated], +complete, {annotation}, 
                    super(unionOf({description}))
   ) ->
   t(x(c),rdf:type,owl:class),
   +[t(x(c),rdf:type,rdfs:class)],
   [t(x(c),rdf:type,owl:deprecatedClass)],
   {x(x(c),annotation)},
   t(x(c),owl:unionOf,x(seq({description})));     x(c).


class(c, [+deprecated], +complete, {annotation},
                      super(complementOf(description))
    
   ) ->
   t(x(c),rdf:type,owl:class),
   +[t(x(c),rdf:type,rdfs:class)],
   [t(x(c),rdf:type,owl:deprecatedClass)],
   {x(x(c),annotation)},
   t(x(c),owl:complementOf,x(description));     x(c).
      
comment(1, heading(2,'Enumerated Classes')).
enumeratedClass(c,[+deprecated], { annotation }, 
                     {i} )->
   t(x(c),rdf:type,owl:class),
   +[t(x(c),rdf:type,rdfs:class)],
   [t(x(c),rdf:type,owl:deprecatedClass)],
   {x(x(c),annotation)},
   t(x(c),owl:oneOf,x(seq({i})));     x(c).



comment(1, heading(1,'Restrctions')).

comment(1, heading(2,'Restrictions on Datatype Properties')).


restriction( cdp, allValuesFrom( dataRange ) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(cdp)),
   t(blank,owl:allValuesFrom,x(dataRange)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.

restriction( cdp, someValuesFrom( dataRange ) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(cdp)),
   t(blank,owl:someValuesFrom,x(dataRange)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.    

restriction( cdp, cardinality(smallInt) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(cdp)),
   t(blank,owl:cardinality,x(smallInt)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.    
restriction( cdp, minCardinality(smallInt) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(cdp)),
   t(blank,owl:minCardinality,x(smallInt)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.     
restriction( cdp, maxCardinality(smallInt) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(cdp)),
   t(blank,owl:maxCardinality,x(smallInt)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.     
restriction( cdp, hasValue(dataLiteral) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(cdp)),
   t(blank,owl:hasValue,x(dataLiteral)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.  


comment(1, heading(2,'Restrictions on Object Properties')).

restriction( cop, allValuesFrom( classInRestriction ) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(cop)),
   t(blank,owl:allValuesFrom,x(classInRestriction)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.

restriction( cop, someValuesFrom( classInRestriction ) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(cop)),
   t(blank,owl:someValuesFrom,x(classInRestriction)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.    

restriction( cop, cardinality(smallInt) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(cop)),
   t(blank,owl:cardinality,x(smallInt)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.    
restriction( cop, minCardinality(smallInt) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(cop)),
   t(blank,owl:minCardinality,x(smallInt)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.     
restriction( cop, maxCardinality(smallInt) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(cop)),
   t(blank,owl:maxCardinality,x(smallInt)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.      
restriction( cop, hasValue(i) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(cop)),
   t(blank,owl:hasValue,x(i)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.  

comment(1, heading(2,'Restrictions on Transitive Properties')).


restriction( tp, allValuesFrom( classInRestriction ) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(tp)),
   t(blank,owl:allValuesFrom,x(classInRestriction)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.

restriction( tp, someValuesFrom( classInRestriction ) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(tp)),
   t(blank,owl:someValuesFrom,x(classInRestriction)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.     
restriction( tp, hasValue(i) ) ->
   t(blank,rdf:type,owl:restriction),
   +[t(blank,rdf:type,owl:class)],
   t(blank,owl:onProperty,x(tp)),
   t(blank,owl:hasValue,x(i)),
   +[t(blank,rdf:type,rdfs:class)];  
                                        blank.  

comment(1, heading(2,'Numbers in Cardinalities')).
0 -> true; 0^^xsd:nonNegativeInteger.
1 -> true; 1^^xsd:nonNegativeInteger.
naturalNumber -> true; naturalNumber^^xsd:nonNegativeInteger.


comment(1, heading(1,'Property Axioms' )).
  
datatypeProperty( cdp-1, [+deprecated], { annotation },
                    { super(cdp-2) },
                    { domain(classInRestriction) },
                    { range(dataRange) }, [+functional] ) ->
   t(x(cdp-1),rdf:type,owl:datatypeProperty),
  +[t(x(cdp-1),rdf:type,rdf:property)],
   [t(x(cdp-1),rdf:type,owl:deprecatedProperty)],
   { x(x(cdp-1),annotation) },
   { t(x(cdp-1),rdfs:subPropertyOf,x(cdp-2)) },
   { t(x(cdp-1), rdfs:domain, x(classInRestriction)) },
   { t(x(cdp-1), rdfs:range, x(dataRange) ) },
   [ t(x(cdp-1), rdf:type, owl:functionalProperty) ].



individualProperty( cop-1, [+deprecated], { annotation },
                    { super(cop-2) },
                    { super(tp) },
                    { domain(classInRestriction-1) },
                    { range(classInRestriction-2) }, 
                    { inverse(cop-3) },
                    [+functional], 
                    [+inverseFunctional], 
                    [+symmetric]) ->
   note(prop)/(+[t(x(cop-1),rdf:type,owl:objectProperty)]),
  +[t(x(cop-1),rdf:type,rdf:property)],
   [t(x(cop-1),rdf:type,owl:deprecatedProperty)],
   { x(x(cop-1),annotation) },
   { t(x(cop-1),rdfs:subPropertyOf,x(cop-2)) },
   { t(x(cop-1),rdfs:subPropertyOf,x(tp)) },
   { t(x(cop-1), rdfs:domain, x(classInRestriction-1)) },
   { t(x(cop-1), rdfs:range, x(classInRestriction-2)) },
   { t(x(cop-1), owl:inverseOf, x(cop-3)) },
   [t(x(cop-1),rdf:type,owl:functionalProperty)],
   [t(x(cop-1),rdf:type,owl:inverseFunctionalProperty)],
   [t(x(cop-1),rdf:type,owl:symmetricProperty)].

individualProperty( tp-1,[+deprecated], { annotation },
                    { super(tp-2) },
                    { domain(classInRestriction-1) },
                    { range(classInRestriction-2) }, 
                    { inverse(tp-3) },
                    [+transitive], 
                    [+symmetric]) ->
   note(prop)/( +[t(x(tp-1),rdf:type,owl:objectProperty)]),
  +[t(x(tp-1),rdf:type,rdf:property)],
   [t(x(tp-1),rdf:type,owl:deprecatedProperty)],
   { x(x(tp-1),annotation) },
   { t(x(tp-1),rdfs:subPropertyOf,x(tp-2)) },
   { t(x(tp-1), rdfs:domain, x(classInRestriction-1)) },
   { t(x(tp-1), rdfs:range, x(classInRestriction-2)) },
   { t(x(tp-1), owl:inverseOf, x(tp-3)) },
   [t(x(tp-1),rdf:type,owl:transitiveProperty)],
   [t(x(tp-1),rdf:type,owl:symmetricProperty)].

allDifferentIndividuals( {i} ) ->
   t(blank,rdf:type,owl:allDifferent),
   t(blank,owl:distinctMembers,x(seq({i}))); blank .

comment(1, heading(1,'Equivalent Properties')).

equivalentProperties(tp-1,tp-2,{tp-3}) ->
   t(x(tp-1),owl:equivalentProperty,x(tp-2)),
   {t(x(tp-1),owl:equivalentProperty,x(tp-3))}.

equivalentProperties(cop-1,cop-2,{cop-3}) ->
   t(x(cop-1),owl:equivalentProperty,x(cop-2)),
   {t(x(cop-1),owl:equivalentProperty,x(cop-3))}.



equivalentProperties(cdp-1,cdp-2,{cdp-3}) ->
   t(x(cdp-1),owl:equivalentProperty,x(cdp-2)),
   {t(x(cdp-1),owl:equivalentProperty,x(cdp-3))}.

%datatypeDeclaration(dt) -> x(dt).
comment(1, heading(1,'Descriptions')).



intersectionOf({description}) ->
   t(blank,rdf:type,owl:class),
   t(blank,owl:intersectionOf,x(seq({description}))),
   +[t(blank,rdf:type,rdfs:class)];     blank.

unionOf({description}) ->
   t(blank,rdf:type,owl:class),
   t(blank,owl:unionOf,x(seq({description}))),
   +[t(blank,rdf:type,rdfs:class)];     blank.

complementOf(description) ->
   t(blank,rdf:type,owl:class),
   t(blank,owl:complementOf,x(description)),
   +[t(blank,rdf:type,rdfs:class)];     blank.

oneOf({i}) ->
   t(blank,rdf:type,owl:class),
   t(blank,owl:oneOf,x(seq({i}))),
   +[t(blank,rdf:type,rdfs:class)];     blank.

comment(1, heading(1,'DataRanges')).

dataRange( {dataLiteral} ) ->
   t(blank,rdf:type,owl:dataRange),
   t(blank,owl:oneOf,x(seq({dataLiteral}))),
   +[t(blank,rdf:type,rdfs:class)];     blank.
 
/*
dataRange( dr, {annotation}, {dataLiteral} ) ->
   t(x(dr),rdf:type,owl:dataRange),
   {x(x(dr),annotation)},
   t(x(dr),owl:oneOf,x(seq({dataLiteral})));     x(dr).
dataRange( dr, +declaration, {annotation} ) ->
   t(x(dr),rdf:type,owl:dataRange),
   {x(x(dr),annotation)};     x(dr).
*/ 

seq({[]}) -> true; rdf:nil .
seq(description-1,{description-2}) -> 
    +[t(blank,rdf:type,rdf:list)],
    t(blank,rdf:first,x(description-1)),
    t(blank,rdf:rest,x(seq({description-2}))); blank.

seq(dataLiteral-1,{dataLiteral-2}) -> 
    +[t(blank,rdf:type,rdf:list)],
    t(blank,rdf:type,rdf:list),
    t(blank,rdf:first,x(dataLiteral-1)),
    t(blank,rdf:rest,x(seq({dataLiteral-2}))); blank.

seq(i-1,{i-2}) -> 
    +[t(blank,rdf:type,rdf:list)],
    t(blank,rdf:type,rdf:list),
    t(blank,rdf:first,x(i-1)),
    t(blank,rdf:rest,x(seq({i-2}))); blank.

equivalentClasses( classInRestriction-1, classInRestriction-2 ) ->
    t( x(classInRestriction-1), owl:equivalentClass, x(classInRestriction-2) ).
disjointClasses(description-1, description-2 ) ->
    t( x(description-1), owl:disjointWith, x(description-2) ).
subClassOf(description-1, description-2 ) ->
    t( x(description-1), rdfs:subClassOf, x(description-2) ).

datatypeDeclaration( dt, [+deprecated], {annotation} ) -> 
   t(x(dt),rdf:type,rdfs:datatype),
   [t(x(dt),rdf:type,owl:deprecatedClass)],
   {x(x(dt),annotation)}.

/*
seq(type-1,{type-2}) -> 
    t(blank,rdf:type,rdf:list),
    t(blank,rdf:first,x(type-1)),
    t(blank,rdf:rest,x(seq({type-2}))); blank.
*/

