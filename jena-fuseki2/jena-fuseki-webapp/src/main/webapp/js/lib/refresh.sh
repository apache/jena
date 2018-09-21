mkdir -p addon/fold
mkdir -p mode/javascript
mkdir -p mode/xml
mkdir -p mode/turtle
mkdir -p mode/sparql

cp ~/dev/js/codemirror-4.3/lib/codemirror.js .
cp ~/dev/js/codemirror-4.3/lib/codemirror.css ../../css
cp ~/dev/js/codemirror-4.3/addon/fold/brace-fold.js ./addon/fold
cp ~/dev/js/codemirror-4.3/addon/fold/comment-fold.js ./addon/fold
cp ~/dev/js/codemirror-4.3/addon/fold/foldcode.js ./addon/fold
cp ~/dev/js/codemirror-4.3/addon/fold/foldgutter.js ./addon/fold
cp ~/dev/js/codemirror-4.3/addon/fold/xml-fold.js ./addon/fold
cp ~/dev/js/codemirror-4.3/addon/fold/foldgutter.css ../../css

cp ~/dev/js/codemirror-4.3/mode/javascript/javascript.js ./mode/javascript
cp ~/dev/js/codemirror-4.3/mode/sparql/sparql.js ./mode/sparql
cp ~/dev/js/codemirror-4.3/mode/xml/xml.js ./mode/xml
cp ~/dev/js/codemirror-4.3/mode/turtle/turtle.js ./mode/turtle


