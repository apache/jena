
uriref(o, ontology, lite ).

comment(note,
   ['The only metaproperties are: owl:imports,
     owl:backwardsCompatibleWith, etc. 
     There is no facility for user-defined metaproperties.']).
uriref(m, ontologyProperty, lite ).
uriref(dt, datatype, lite).
uriref(a, annotationProp, lite).
uriref(da, dataAnnotationProp, lite).
uriref(c, class, lite).
uriref(i, individual, lite).
comment(note,
   ['In OWL Lite the only DataRange is rdfs:Literal. 
     User-defined DataRanges are available in OWL DL.']).
%uriref(dr,dataRange, lite).
%uriref(sdp,simpleDataProp, lite).
uriref(cdp,dataProp, lite).

%uriref(sop,simpleObjectProp, lite).
uriref(cop,objectProp, lite).
uriref(tp,transitiveProp, lite).
