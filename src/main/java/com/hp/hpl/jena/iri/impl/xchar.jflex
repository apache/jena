
[\-a-zA-Z0-9_\~.] { rule(-10); }

[!$&'()*+,;=] { rule(-20); }

[%]20 { 
  rule(-30); 
  error(PERCENT_20); 
  }
%[A-F0-9][A-F0-9] { 
 rule(-40); 
 error(PERCENT); 
 }
%[A-Fa-f0-9][A-Fa-f0-9] { 
  rule(-50); 
  error(PERCENT); 
  error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);
}

% {
  rule(-60);
  error(ILLEGAL_PERCENT_ENCODING);
}

"  " {
  rule(-70);
  error(DOUBLE_WHITESPACE);
}

^" " {
  rule(-80);
  error(DOUBLE_WHITESPACE);
}

" " {
  rule(-90);
  if (yychar==lastChar)
    error(DOUBLE_WHITESPACE);
  else
    error(WHITESPACE);
}

[\t\n\r] {
  rule(-100);
  error(CONTROL_CHARACTER);
  error(NOT_XML_SCHEMA_WHITESPACE);
}
[\x00-\x1F] {
  rule(-110);
  error(NON_XML_CHARACTER);
  error(CONTROL_CHARACTER);
}

[\x85] {
  rule(-113);
  error(CONTROL_CHARACTER);
}
[\x7F-\x84\x86-\x9F] {
  rule(-115);
  error(DISCOURAGED_XML_CHARACTER);
  error(CONTROL_CHARACTER);
}

[><{}|\^`\\\"] {
  rule(-120);
  error(UNWISE_CHARACTER);
}

[\uD800-\uDBFF][\uDC00-\uDFFF] {
   rule(-130);
   surrogatePair();
}

[\uD800-\uDFFF] {
   rule(-140);
   error(LONE_SURROGATE);   
   difficultChar();
}

[\xA0-\uD7FF\uE000-\uFFFF] {
/*
xxxx,xxxx,xxxx,xxxx xxxx,xxxx,xxxx,xxxx
000u,uuuu,xxxx,xxxx,xxxx,xxxx 110110wwww,xxxx,xx 110111xx,xxxx,xxxx

wwww = uuuuu - 1.
*/

  rule(-150);
  difficultChar();
}


[^a] {
  rule(-160);
  error(ILLEGAL_CHARACTER);
 }