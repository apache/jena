var barx = '||';
function bar(x,y)       { return barx+combine(x,y)+barx } 
function value(n)       { return n.value ; }
function combine(x,y)   { return x+y }
function identity(x)    { return x }

// The JavaScript types (numbers split).
function rtnBoolean()   { return true; }
function rtnString()    { return "foo"; }
function rtnInteger()   { return 57 }
function rtnDouble()    { return 5.7 }
function rtnUndef()     { return undefined }
function rtnNull()      { return null; }
//function rtnSymbol()    { return Symbol('foo') }
