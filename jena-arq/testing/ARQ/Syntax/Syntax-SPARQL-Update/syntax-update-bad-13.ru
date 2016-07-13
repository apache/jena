DELETE { } 
WHERE {  
    {SELECT * #?sub ?obj
    WHERE {
        ?sub ?pred ?obj .
    } GROUP BY ?sub }
}
