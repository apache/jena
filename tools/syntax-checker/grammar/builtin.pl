
builtinx(owl,'Thing', classID).
builtinx(owl,'Nothing',classID).

builtinx(rdfs,'Literal',datatypeID).
builtinx(rdf,'XMLLiteral',datatypeID).
builtinx(xsd,string,datatypeID).
builtinx(xsd,boolean,datatypeID).
builtinx(xsd,decimal,datatypeID).
builtinx(xsd,float,datatypeID).
builtinx(xsd,double,datatypeID).
builtinx(xsd,dateTime,datatypeID).
builtinx(xsd,time,datatypeID).
builtinx(xsd,date,datatypeID).
builtinx(xsd,gYearMonth,datatypeID).
builtinx(xsd,gYear,datatypeID).
builtinx(xsd,gMonthDay,datatypeID).
builtinx(xsd,gDay,datatypeID).
builtinx(xsd,gMonth,datatypeID).
builtinx(xsd,hexBinary,datatypeID).
builtinx(xsd,base64Binary,datatypeID).
builtinx(xsd,anyURI,datatypeID).
builtinx(xsd,normalizedString,datatypeID).
builtinx(xsd,token,datatypeID).
builtinx(xsd,language,datatypeID).
builtinx(xsd,'NMTOKEN',datatypeID).
builtinx(xsd,'Name',datatypeID).
builtinx(xsd,'NCName',datatypeID).
builtinx(xsd,integer,datatypeID).
builtinx(xsd,nonPositiveInteger,datatypeID).
builtinx(xsd,negativeInteger,datatypeID).
builtinx(xsd,long,datatypeID).
builtinx(xsd,int,datatypeID).
builtinx(xsd,short,datatypeID).
builtinx(xsd,byte,datatypeID).
builtinx(xsd,nonNegativeInteger,datatypeID).
builtinx(xsd,unsignedLong,datatypeID).
builtinx(xsd,unsignedInt,datatypeID).
builtinx(xsd,unsignedShort,datatypeID).
builtinx(xsd,unsignedByte,datatypeID).
builtinx(xsd,positiveInteger,datatypeID).

builtinx(rdfs, comment, dataAnnotationPropID ).
builtinx(rdfs, label, dataAnnotationPropID ).
builtinx(rdfs, isDefinedBy, annotationPropID  ).
builtinx(rdfs, seeAlso, annotationPropID  ).
builtinx(owl, versionInfo, annotationPropID  ).

builtinx(owl,imports,ontologyPropertyID).
builtinx(owl,priorVersion,ontologyPropertyID).
builtinx(owl,backwardCompatibleWith,ontologyPropertyID).
builtinx(owl,incompatibleWith,ontologyPropertyID).

badbuiltin(xsd,duration,datatypeID).
badbuiltin(xsd,'QName',datatypeID).
badbuiltin(xsd,'ENTITY',datatypeID).
badbuiltin(xsd,'ID',datatypeID).
badbuiltin(xsd,'IDREF',datatypeID).
badbuiltin(xsd,'ENTITIES',datatypeID).
badbuiltin(xsd,'IDREFS',datatypeID).
badbuiltin(xsd,'NOTATION',datatypeID).
badbuiltin(xsd,'NMTOKENS',datatypeID).

classOnly(rdf,'Bag').
classOnly(rdf,'Seq').
classOnly(rdf,'Alt').
classOnly(rdf,'Statement').

propertyOnly(rdf,subject).
propertyOnly(rdf,predicate).
propertyOnly(rdf,object).

builtin(A,B) :-
  builtinx(A,B,_).

builtin(A,B) :-
  builtiny(A,B).


builtiny(rdf,type).
builtiny(owl,'Ontology').
builtiny(owl,'ObjectProperty').
builtiny(rdfs,'Datatype').
builtiny(owl,'DataRange').
builtiny(owl,'DatatypeProperty').
builtiny(owl, equivalentClass ).
builtiny(owl, sameIndividualAs ).
builtiny(rdfs, subClassOf ).
builtiny(rdfs, subPropertyOf ).
builtiny(rdfs, domain ).
builtiny(rdfs, range ).
builtiny(owl, equivalentProperty ).
builtiny(owl, 'Class' ).
builtiny(rdfs, 'Class' ).
builtiny(owl, intersectionOf ).
builtiny(owl, unionOf ).
builtiny(owl, complementOf ).
builtiny(owl, 'Restriction' ).
builtiny(owl, onProperty ).
builtiny(owl, allValuesFrom ).
builtiny(owl, someValuesFrom ).
builtiny(owl, cardinality ).
builtiny(owl, minCardinality ).
builtiny(owl, maxCardinality ).
builtiny(owl, hasValue ).
builtiny(rdf, 'Property' ).
builtiny(rdf, nil).
builtiny(rdf,first).
builtiny(rdf,rest).
builtiny(rdf,'List').
builtiny(owl,'OntologyProperty').
builtiny(owl,'AllDifferent').
builtiny(owl,distinctMembers).
builtiny(owl,'AnnotationProperty').
builtiny(owl,'FunctionalProperty').
builtiny(owl,'InverseFunctionalProperty').
builtiny(owl,'SymmetricProperty').
builtiny(owl,'TransitiveProperty').
builtiny(owl,'DeprecatedProperty').
builtiny(owl,'DeprecatedClass').
builtiny(owl,inverseOf).
builtiny(owl,oneOf).
builtiny(owl,differentFrom).
builtiny(owl,disjointWith).
builtiny(owl,'AllDifferent').
builtiny(owl,distinctMembers).

disallowed(rdf,type).
disallowed(rdf,'Property').
disallowed(rdf,nil).
disallowed(rdf,'List').
disallowed(rdf,'XMLLiteral').
disallowed(rdf,first).
disallowed(rdf,rest).
disallowed(rdfs,domain).
disallowed(rdfs,range).
disallowed(rdfs,'Resource').
disallowed(rdfs,'Datatype').
disallowed(rdfs,'Class').
disallowed(rdfs,subClassOf).
disallowed(rdfs,subPropertyOf).
disallowed(rdfs,member).
disallowed(rdfs,'Container').
disallowed(rdfs,'ContainerMembershipProperty').
disallowed(owl,'AllDifferent').
disallowed(owl,allValuesFrom).
disallowed(owl,'AnnotationProperty').
disallowed(owl,cardinality).
disallowed(owl,'Class').
disallowed(owl,complementOf).
disallowed(owl,'DataRange').
disallowed(owl,'DatatypeProperty').
disallowed(owl,'DeprecatedClass').
disallowed(owl,'DeprecatedProperty').
disallowed(owl,differentFrom).
disallowed(owl,disjointWith).
disallowed(owl,distinctMembers).
disallowed(owl,equivalentClass).
disallowed(owl,equivalentProperty).
disallowed(owl,'FunctionalProperty').
disallowed(owl,hasValue).
disallowed(owl,intersectionOf).
disallowed(owl,'InverseFunctionalProperty').
disallowed(owl,inverseOf).
disallowed(owl,maxCardinality).
disallowed(owl,minCardinality).
disallowed(owl,'ObjectProperty').
disallowed(owl,oneOf).
disallowed(owl,onProperty).
disallowed(owl,'Ontology').
disallowed(owl,'Restriction').
disallowed(owl,sameAs).
disallowed(owl,sameIndividualAs).
disallowed(owl,someValuesFrom).
disallowed(owl,'SymmetricProperty').
disallowed(owl,'TransitiveProperty').
disallowed(owl,'OntologyProperty').
disallowed(owl,unionOf).

