
uriref(o, ontology, lite ).

/*
comment(note,
   ['The only metaproperties are: owl:imports,
     owl:backwardsCompatibleWith, etc. 
     There is no facility for user-defined metaproperties.']).
     */
uriref(b, bad, lite).
uriref(m, ontologyProperty, lite ).
uriref(dt, datatype, lite).
uriref(a, annotationProp, lite).
uriref(da, dataAnnotationProp, lite).
uriref(c, class, lite).
uriref(i, individual, lite).
comment(note,
   ['The only DataRange is rdfs:Literal. 
     ']).
uriref(dr,dataRange, lite).
%uriref(sdp,simpleDataProp, lite).
uriref(cdp,dataProp, lite).

%uriref(sop,simpleObjectProp, lite).
uriref(cop,objectProp, lite).
uriref(tp,transitiveProp, lite).
