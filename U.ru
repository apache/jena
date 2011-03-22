PREFIX : <http://example/>

INSERT DATA 
{ GRAPH <http://www.social.com/> { :s :p :o } }

DELETE WHERE {  GRAPH <http://www.social.com/> { :s :p :o } }

