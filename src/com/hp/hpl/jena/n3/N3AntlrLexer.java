// $ANTLR : "c:/home/afs/Projects/Jena/src/com/hp/hpl/jena/n3/n3.g" -> "N3AntlrLexer.java"$

package com.hp.hpl.jena.n3 ;
import java.io.* ;
import antlr.TokenStreamRecognitionException ;

import java.io.InputStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.ANTLRException;
import java.io.Reader;
import java.util.Hashtable;
import antlr.CharScanner;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.MismatchedCharException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.SemanticException;

public class N3AntlrLexer extends antlr.CharScanner implements N3AntlrParserTokenTypes, TokenStream
 {
public N3AntlrLexer(InputStream in) {
	this(new ByteBuffer(in));
}
public N3AntlrLexer(Reader in) {
	this(new CharBuffer(in));
}
public N3AntlrLexer(InputBuffer ib) {
	this(new LexerSharedInputState(ib));
}
public N3AntlrLexer(LexerSharedInputState state) {
	super(state);
	caseSensitiveLiterals = true;
	setCaseSensitive(true);
	literals = new Hashtable();
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '<':
				{
					mURI_OR_IMPLIES(true);
					theRetToken=_returnToken;
					break;
				}
				case '?':
				{
					mUVAR(true);
					theRetToken=_returnToken;
					break;
				}
				case '"':  case '\'':
				{
					mSTRING(true);
					theRetToken=_returnToken;
					break;
				}
				case '.':
				{
					mSEP(true);
					theRetToken=_returnToken;
					break;
				}
				case '(':
				{
					mLPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case ')':
				{
					mRPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case '[':
				{
					mLBRACK(true);
					theRetToken=_returnToken;
					break;
				}
				case ']':
				{
					mRBRACK(true);
					theRetToken=_returnToken;
					break;
				}
				case '{':
				{
					mLCURLY(true);
					theRetToken=_returnToken;
					break;
				}
				case '}':
				{
					mRCURLY(true);
					theRetToken=_returnToken;
					break;
				}
				case ';':
				{
					mSEMI(true);
					theRetToken=_returnToken;
					break;
				}
				case ',':
				{
					mCOMMA(true);
					theRetToken=_returnToken;
					break;
				}
				case '!':
				{
					mPATH(true);
					theRetToken=_returnToken;
					break;
				}
				case '>':
				{
					mARROW_P_L(true);
					theRetToken=_returnToken;
					break;
				}
				case '#':
				{
					mSL_COMMENT(true);
					theRetToken=_returnToken;
					break;
				}
				case '\t':  case '\n':  case '\u000c':  case '\r':
				case ' ':
				{
					mWS(true);
					theRetToken=_returnToken;
					break;
				}
				default:
					if ((LA(1)=='@') && (LA(2)=='p')) {
						mAT_PREFIX(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='^') && (LA(2)=='^')) {
						mDATA_T(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (LA(2)=='>')) {
						mARROW_R(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (LA(2)=='>')) {
						mARROW_P_R(true);
						theRetToken=_returnToken;
					}
					else if ((_tokenSet_0.member(LA(1))) && (true)) {
						mQNAME_OR_KEYWORD_OR_NAME_OP(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='@') && (true)) {
						mAT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='^') && (true)) {
						mRPATH(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (true)) {
						mEQUAL(true);
						theRetToken=_returnToken;
					}
				else {
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_ttype = testLiteralsTable(_ttype);
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				throw new TokenStreamRecognitionException(e);
			}
		}
		catch (CharStreamException cse) {
			if ( cse instanceof CharStreamIOException ) {
				throw new TokenStreamIOException(((CharStreamIOException)cse).io);
			}
			else {
				throw new TokenStreamException(cse.getMessage());
			}
		}
	}
}

	public final void mQNAME_OR_KEYWORD_OR_NAME_OP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QNAME_OR_KEYWORD_OR_NAME_OP;
		int _saveIndex;
		
		boolean synPredMatched361 = false;
		if (((_tokenSet_1.member(LA(1))) && (_tokenSet_0.member(LA(2))) && (_tokenSet_0.member(LA(3))))) {
			int _m361 = mark();
			synPredMatched361 = true;
			inputState.guessing++;
			try {
				{
				mNSNAME(false);
				mCOLON(false);
				mLNAME(false);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched361 = false;
			}
			rewind(_m361);
			inputState.guessing--;
		}
		if ( synPredMatched361 ) {
			mNSNAME(false);
			mCOLON(false);
			mLNAME(false);
			if ( inputState.guessing==0 ) {
				_ttype = QNAME ;
			}
		}
		else {
			boolean synPredMatched371 = false;
			if (((LA(1)=='h') && (LA(2)=='a') && (LA(3)=='s'))) {
				int _m371 = mark();
				synPredMatched371 = true;
				inputState.guessing++;
				try {
					{
					match("has");
					mNON_ANC(false);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched371 = false;
				}
				rewind(_m371);
				inputState.guessing--;
			}
			if ( synPredMatched371 ) {
				match("has");
				if ( inputState.guessing==0 ) {
					_ttype = KW_HAS ;
				}
			}
			else {
				boolean synPredMatched375 = false;
				if (((LA(1)=='t') && (LA(2)=='h') && (LA(3)=='i'))) {
					int _m375 = mark();
					synPredMatched375 = true;
					inputState.guessing++;
					try {
						{
						match("this");
						mNON_ANC(false);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched375 = false;
					}
					rewind(_m375);
					inputState.guessing--;
				}
				if ( synPredMatched375 ) {
					match("this");
					if ( inputState.guessing==0 ) {
						_ttype = KW_THIS ;
					}
				}
				else {
					boolean synPredMatched363 = false;
					if (((LA(1)==':') && (_tokenSet_2.member(LA(2))))) {
						int _m363 = mark();
						synPredMatched363 = true;
						inputState.guessing++;
						try {
							{
							mCOLON(false);
							mLNAME(false);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched363 = false;
						}
						rewind(_m363);
						inputState.guessing--;
					}
					if ( synPredMatched363 ) {
						mCOLON(false);
						mLNAME(false);
						if ( inputState.guessing==0 ) {
							_ttype = QNAME ;
						}
					}
					else {
						boolean synPredMatched365 = false;
						if (((_tokenSet_1.member(LA(1))) && (_tokenSet_0.member(LA(2))) && (true))) {
							int _m365 = mark();
							synPredMatched365 = true;
							inputState.guessing++;
							try {
								{
								mNSNAME(false);
								mCOLON(false);
								}
							}
							catch (RecognitionException pe) {
								synPredMatched365 = false;
							}
							rewind(_m365);
							inputState.guessing--;
						}
						if ( synPredMatched365 ) {
							mNSNAME(false);
							mCOLON(false);
							if ( inputState.guessing==0 ) {
								_ttype = QNAME ;
							}
						}
						else {
							boolean synPredMatched369 = false;
							if (((LA(1)==':') && (LA(2)=='-'))) {
								int _m369 = mark();
								synPredMatched369 = true;
								inputState.guessing++;
								try {
									{
									mCOLON(false);
									match('-');
									}
								}
								catch (RecognitionException pe) {
									synPredMatched369 = false;
								}
								rewind(_m369);
								inputState.guessing--;
							}
							if ( synPredMatched369 ) {
								match(":-");
								if ( inputState.guessing==0 ) {
									_ttype = NAME_OP ;
								}
							}
							else {
								boolean synPredMatched373 = false;
								if (((LA(1)=='o') && (LA(2)=='f') && (true))) {
									int _m373 = mark();
									synPredMatched373 = true;
									inputState.guessing++;
									try {
										{
										match("of");
										mNON_ANC(false);
										}
									}
									catch (RecognitionException pe) {
										synPredMatched373 = false;
									}
									rewind(_m373);
									inputState.guessing--;
								}
								if ( synPredMatched373 ) {
									match("of");
									if ( inputState.guessing==0 ) {
										_ttype = KW_OF ;
									}
								}
								else {
									boolean synPredMatched379 = false;
									if (((LA(1)=='i') && (LA(2)=='s') && (true))) {
										int _m379 = mark();
										synPredMatched379 = true;
										inputState.guessing++;
										try {
											{
											match("is");
											mNON_ANC(false);
											}
										}
										catch (RecognitionException pe) {
											synPredMatched379 = false;
										}
										rewind(_m379);
										inputState.guessing--;
									}
									if ( synPredMatched379 ) {
										match("is");
										if ( inputState.guessing==0 ) {
											_ttype = KW_IS ;
										}
									}
									else {
										boolean synPredMatched367 = false;
										if (((LA(1)==':') && (true))) {
											int _m367 = mark();
											synPredMatched367 = true;
											inputState.guessing++;
											try {
												{
												mCOLON(false);
												}
											}
											catch (RecognitionException pe) {
												synPredMatched367 = false;
											}
											rewind(_m367);
											inputState.guessing--;
										}
										if ( synPredMatched367 ) {
											mCOLON(false);
											if ( inputState.guessing==0 ) {
												_ttype = QNAME ;
											}
										}
										else {
											boolean synPredMatched377 = false;
											if (((LA(1)=='a') && (true))) {
												int _m377 = mark();
												synPredMatched377 = true;
												inputState.guessing++;
												try {
													{
													match("a");
													mNON_ANC(false);
													}
												}
												catch (RecognitionException pe) {
													synPredMatched377 = false;
												}
												rewind(_m377);
												inputState.guessing--;
											}
											if ( synPredMatched377 ) {
												match("a");
												if ( inputState.guessing==0 ) {
													_ttype = KW_A ;
												}
											}
											else {
												throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
											}
											}}}}}}}}}
											if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
												_token = makeToken(_ttype);
												_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
											}
											_returnToken = _token;
										}
										
	protected final void mNSNAME(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NSNAME;
		int _saveIndex;
		
		{
		int _cnt444=0;
		_loop444:
		do {
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':  case 'A':  case 'B':
			case 'C':  case 'D':  case 'E':  case 'F':
			case 'G':  case 'H':  case 'I':  case 'J':
			case 'K':  case 'L':  case 'M':  case 'N':
			case 'O':  case 'P':  case 'Q':  case 'R':
			case 'S':  case 'T':  case 'U':  case 'V':
			case 'W':  case 'X':  case 'Y':  case 'Z':
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':  case 'g':  case 'h':
			case 'i':  case 'j':  case 'k':  case 'l':
			case 'm':  case 'n':  case 'o':  case 'p':
			case 'q':  case 'r':  case 's':  case 't':
			case 'u':  case 'v':  case 'w':  case 'x':
			case 'y':  case 'z':
			{
				mALPHANUMERIC(false);
				break;
			}
			case '_':
			{
				match('_');
				break;
			}
			case '-':
			{
				match('-');
				break;
			}
			default:
			{
				if ( _cnt444>=1 ) { break _loop444; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			}
			_cnt444++;
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mCOLON(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COLON;
		int _saveIndex;
		
		match(':');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mLNAME(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LNAME;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':  case 'A':  case 'B':
		case 'C':  case 'D':  case 'E':  case 'F':
		case 'G':  case 'H':  case 'I':  case 'J':
		case 'K':  case 'L':  case 'M':  case 'N':
		case 'O':  case 'P':  case 'Q':  case 'R':
		case 'S':  case 'T':  case 'U':  case 'V':
		case 'W':  case 'X':  case 'Y':  case 'Z':
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			mALPHANUMERIC(false);
			break;
		}
		case '_':
		{
			match('_');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		_loop448:
		do {
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':  case 'A':  case 'B':
			case 'C':  case 'D':  case 'E':  case 'F':
			case 'G':  case 'H':  case 'I':  case 'J':
			case 'K':  case 'L':  case 'M':  case 'N':
			case 'O':  case 'P':  case 'Q':  case 'R':
			case 'S':  case 'T':  case 'U':  case 'V':
			case 'W':  case 'X':  case 'Y':  case 'Z':
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':  case 'g':  case 'h':
			case 'i':  case 'j':  case 'k':  case 'l':
			case 'm':  case 'n':  case 'o':  case 'p':
			case 'q':  case 'r':  case 's':  case 't':
			case 'u':  case 'v':  case 'w':  case 'x':
			case 'y':  case 'z':
			{
				mALPHANUMERIC(false);
				break;
			}
			case '_':
			{
				match('_');
				break;
			}
			case '-':
			{
				match('-');
				break;
			}
			default:
			{
				break _loop448;
			}
			}
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mNON_ANC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NON_ANC;
		int _saveIndex;
		
		{
		match(_tokenSet_3);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mURI_OR_IMPLIES(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = URI_OR_IMPLIES;
		int _saveIndex;
		
		boolean synPredMatched384 = false;
		if (((LA(1)=='<') && (LA(2)=='=') && (LA(3)=='>'))) {
			int _m384 = mark();
			synPredMatched384 = true;
			inputState.guessing++;
			try {
				{
				mARROW_MEANS(false);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched384 = false;
			}
			rewind(_m384);
			inputState.guessing--;
		}
		if ( synPredMatched384 ) {
			mARROW_MEANS(false);
			if ( inputState.guessing==0 ) {
				_ttype = ARROW_MEANS ;
			}
		}
		else {
			boolean synPredMatched382 = false;
			if (((LA(1)=='<') && (LA(2)=='=') && (true))) {
				int _m382 = mark();
				synPredMatched382 = true;
				inputState.guessing++;
				try {
					{
					mARROW_L(false);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched382 = false;
				}
				rewind(_m382);
				inputState.guessing--;
			}
			if ( synPredMatched382 ) {
				mARROW_L(false);
				if ( inputState.guessing==0 ) {
					_ttype = ARROW_L ;
				}
			}
			else if ((LA(1)=='<') && (_tokenSet_4.member(LA(2))) && (true)) {
				mURIREF(false);
				if ( inputState.guessing==0 ) {
					_ttype = URIREF ;
				}
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
				_token = makeToken(_ttype);
				_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
			}
			_returnToken = _token;
		}
		
	protected final void mARROW_L(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ARROW_L;
		int _saveIndex;
		
		match("<=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mARROW_MEANS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ARROW_MEANS;
		int _saveIndex;
		
		match("<=>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mURIREF(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = URIREF;
		int _saveIndex;
		
		_saveIndex=text.length();
		match('<');
		text.setLength(_saveIndex);
		{
		_loop388:
		do {
			// nongreedy exit test
			if ((LA(1)=='>') && (true)) break _loop388;
			if ((_tokenSet_4.member(LA(1))) && (_tokenSet_4.member(LA(2)))) {
				{
				match(_tokenSet_4);
				}
			}
			else {
				break _loop388;
			}
			
		} while (true);
		}
		_saveIndex=text.length();
		match('>');
		text.setLength(_saveIndex);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mURICHAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = URICHAR;
		int _saveIndex;
		
		switch ( LA(1)) {
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':  case 'A':  case 'B':
		case 'C':  case 'D':  case 'E':  case 'F':
		case 'G':  case 'H':  case 'I':  case 'J':
		case 'K':  case 'L':  case 'M':  case 'N':
		case 'O':  case 'P':  case 'Q':  case 'R':
		case 'S':  case 'T':  case 'U':  case 'V':
		case 'W':  case 'X':  case 'Y':  case 'Z':
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			mALPHANUMERIC(false);
			break;
		}
		case '-':
		{
			match('-');
			break;
		}
		case '_':
		{
			match('_');
			break;
		}
		case '.':
		{
			match('.');
			break;
		}
		case '!':
		{
			match('!');
			break;
		}
		case '~':
		{
			match('~');
			break;
		}
		case '*':
		{
			match('*');
			break;
		}
		case '\'':
		{
			match("'");
			break;
		}
		case '(':
		{
			match('(');
			break;
		}
		case ')':
		{
			match(')');
			break;
		}
		case ';':
		{
			match(';');
			break;
		}
		case '/':
		{
			match('/');
			break;
		}
		case '?':
		{
			match('?');
			break;
		}
		case ':':
		{
			match(':');
			break;
		}
		case '@':
		{
			match('@');
			break;
		}
		case '&':
		{
			match('&');
			break;
		}
		case '=':
		{
			match('=');
			break;
		}
		case '+':
		{
			match('+');
			break;
		}
		case '$':
		{
			match('$');
			break;
		}
		case ',':
		{
			match(',');
			break;
		}
		case '{':
		{
			match('{');
			break;
		}
		case '}':
		{
			match('}');
			break;
		}
		case '|':
		{
			match('|');
			break;
		}
		case '\\':
		{
			match('\\');
			break;
		}
		case '^':
		{
			match('^');
			break;
		}
		case '[':
		{
			match('[');
			break;
		}
		case ']':
		{
			match(']');
			break;
		}
		case '`':
		{
			match('`');
			break;
		}
		case '%':
		{
			match('%');
			break;
		}
		case '#':
		{
			match('#');
			break;
		}
		case '"':
		{
			match('"');
			break;
		}
		case ' ':
		{
			match(' ');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mALPHANUMERIC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ALPHANUMERIC;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':  case 'a':  case 'b':
		case 'c':  case 'd':  case 'e':  case 'f':
		case 'g':  case 'h':  case 'i':  case 'j':
		case 'k':  case 'l':  case 'm':  case 'n':
		case 'o':  case 'p':  case 'q':  case 'r':
		case 's':  case 't':  case 'u':  case 'v':
		case 'w':  case 'x':  case 'y':  case 'z':
		{
			mALPHA(false);
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			matchRange('0','9');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mUVAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = UVAR;
		int _saveIndex;
		
		mQUESTION(false);
		{
		int _cnt392=0;
		_loop392:
		do {
			if ((_tokenSet_5.member(LA(1)))) {
				mALPHANUMERIC(false);
			}
			else {
				if ( _cnt392>=1 ) { break _loop392; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt392++;
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mQUESTION(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUESTION;
		int _saveIndex;
		
		match('?');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mAT_PREFIX(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = AT_PREFIX;
		int _saveIndex;
		
		mAT(false);
		match("prefix");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mAT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = AT;
		int _saveIndex;
		
		match('@');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTRING(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '\'':
		{
			mSTRING1(false);
			break;
		}
		case '"':
		{
			mSTRING2(false);
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mSTRING1(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING1;
		int _saveIndex;
		
		boolean synPredMatched451 = false;
		if (((LA(1)=='\'') && (LA(2)=='\'') && (LA(3)=='\''))) {
			int _m451 = mark();
			synPredMatched451 = true;
			inputState.guessing++;
			try {
				{
				mQUOTE3S(false);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched451 = false;
			}
			rewind(_m451);
			inputState.guessing--;
		}
		if ( synPredMatched451 ) {
			_saveIndex=text.length();
			mQUOTE3S(false);
			text.setLength(_saveIndex);
			{
			_loop456:
			do {
				// nongreedy exit test
				if ((LA(1)=='\'') && (LA(2)=='\'') && (LA(3)=='\'')) break _loop456;
				boolean synPredMatched454 = false;
				if (((LA(1)=='\n'||LA(1)=='\r') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')))) {
					int _m454 = mark();
					synPredMatched454 = true;
					inputState.guessing++;
					try {
						{
						mNL(false);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched454 = false;
					}
					rewind(_m454);
					inputState.guessing--;
				}
				if ( synPredMatched454 ) {
					mNL(false);
				}
				else if ((_tokenSet_6.member(LA(1))) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe'))) {
					{
					match(_tokenSet_6);
					}
				}
				else if ((LA(1)=='\\')) {
					mESCAPE(false);
				}
				else {
					break _loop456;
				}
				
			} while (true);
			}
			_saveIndex=text.length();
			mQUOTE3S(false);
			text.setLength(_saveIndex);
		}
		else if ((LA(1)=='\'') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true)) {
			_saveIndex=text.length();
			match('\'');
			text.setLength(_saveIndex);
			{
			_loop458:
			do {
				// nongreedy exit test
				if ((LA(1)=='\'') && (true)) break _loop458;
				if ((_tokenSet_6.member(LA(1))) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe'))) {
					matchNot('\\');
				}
				else if ((LA(1)=='\\')) {
					mESCAPE(false);
				}
				else {
					break _loop458;
				}
				
			} while (true);
			}
			_saveIndex=text.length();
			match('\'');
			text.setLength(_saveIndex);
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mSTRING2(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING2;
		int _saveIndex;
		
		boolean synPredMatched461 = false;
		if (((LA(1)=='"') && (LA(2)=='"') && (LA(3)=='"'))) {
			int _m461 = mark();
			synPredMatched461 = true;
			inputState.guessing++;
			try {
				{
				mQUOTE3D(false);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched461 = false;
			}
			rewind(_m461);
			inputState.guessing--;
		}
		if ( synPredMatched461 ) {
			_saveIndex=text.length();
			mQUOTE3D(false);
			text.setLength(_saveIndex);
			{
			_loop466:
			do {
				// nongreedy exit test
				if ((LA(1)=='"') && (LA(2)=='"') && (LA(3)=='"')) break _loop466;
				boolean synPredMatched464 = false;
				if (((LA(1)=='\n'||LA(1)=='\r') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')))) {
					int _m464 = mark();
					synPredMatched464 = true;
					inputState.guessing++;
					try {
						{
						mNL(false);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched464 = false;
					}
					rewind(_m464);
					inputState.guessing--;
				}
				if ( synPredMatched464 ) {
					mNL(false);
				}
				else if ((_tokenSet_6.member(LA(1))) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe'))) {
					{
					match(_tokenSet_6);
					}
				}
				else if ((LA(1)=='\\')) {
					mESCAPE(false);
				}
				else {
					break _loop466;
				}
				
			} while (true);
			}
			_saveIndex=text.length();
			mQUOTE3D(false);
			text.setLength(_saveIndex);
		}
		else if ((LA(1)=='"') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true)) {
			_saveIndex=text.length();
			match('"');
			text.setLength(_saveIndex);
			{
			_loop468:
			do {
				// nongreedy exit test
				if ((LA(1)=='"') && (true)) break _loop468;
				if ((_tokenSet_6.member(LA(1))) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe'))) {
					matchNot('\\');
				}
				else if ((LA(1)=='\\')) {
					mESCAPE(false);
				}
				else {
					break _loop468;
				}
				
			} while (true);
			}
			_saveIndex=text.length();
			match('"');
			text.setLength(_saveIndex);
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSEP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SEP;
		int _saveIndex;
		
		match('.');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LPAREN;
		int _saveIndex;
		
		match('(');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RPAREN;
		int _saveIndex;
		
		match(')');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLBRACK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LBRACK;
		int _saveIndex;
		
		match('[');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRBRACK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RBRACK;
		int _saveIndex;
		
		match(']');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLCURLY(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LCURLY;
		int _saveIndex;
		
		match('{');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRCURLY(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RCURLY;
		int _saveIndex;
		
		match('}');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSEMI(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SEMI;
		int _saveIndex;
		
		match(';');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOMMA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COMMA;
		int _saveIndex;
		
		match(',');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPATH(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PATH;
		int _saveIndex;
		
		match('!');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRPATH(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RPATH;
		int _saveIndex;
		
		match('^');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDATA_T(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DATA_T;
		int _saveIndex;
		
		match("^^");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mNAME_IT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NAME_IT;
		int _saveIndex;
		
		match(":-");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mARROW_R(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ARROW_R;
		int _saveIndex;
		
		match("=>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mARROW_P_L(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ARROW_P_L;
		int _saveIndex;
		
		match(">-");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mARROW_P_R(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ARROW_P_R;
		int _saveIndex;
		
		match("->");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mEQUAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EQUAL;
		int _saveIndex;
		
		match("=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSL_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SL_COMMENT;
		int _saveIndex;
		
		match("#");
		{
		_loop421:
		do {
			if ((_tokenSet_4.member(LA(1)))) {
				{
				match(_tokenSet_4);
				}
			}
			else {
				break _loop421;
			}
			
		} while (true);
		}
		{
		if ((LA(1)=='\n'||LA(1)=='\r')) {
			mNL(false);
		}
		else {
		}
		
		}
		if ( inputState.guessing==0 ) {
			_ttype = Token.SKIP;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mNL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NL;
		int _saveIndex;
		
		boolean synPredMatched428 = false;
		if (((LA(1)=='\r') && (LA(2)=='\n') && (true))) {
			int _m428 = mark();
			synPredMatched428 = true;
			inputState.guessing++;
			try {
				{
				mNL1(false);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched428 = false;
			}
			rewind(_m428);
			inputState.guessing--;
		}
		if ( synPredMatched428 ) {
			mNL1(false);
		}
		else {
			boolean synPredMatched430 = false;
			if (((LA(1)=='\n'))) {
				int _m430 = mark();
				synPredMatched430 = true;
				inputState.guessing++;
				try {
					{
					mNL2(false);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched430 = false;
				}
				rewind(_m430);
				inputState.guessing--;
			}
			if ( synPredMatched430 ) {
				mNL2(false);
			}
			else {
				boolean synPredMatched432 = false;
				if (((LA(1)=='\r') && (true) && (true))) {
					int _m432 = mark();
					synPredMatched432 = true;
					inputState.guessing++;
					try {
						{
						mNL3(false);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched432 = false;
					}
					rewind(_m432);
					inputState.guessing--;
				}
				if ( synPredMatched432 ) {
					mNL3(false);
				}
				else {
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}}
				if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
					_token = makeToken(_ttype);
					_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
				}
				_returnToken = _token;
			}
			
	protected final void mNL1(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NL1;
		int _saveIndex;
		
		match("\r\n");
		if ( inputState.guessing==0 ) {
			newline();
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mNL2(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NL2;
		int _saveIndex;
		
		match("\n");
		if ( inputState.guessing==0 ) {
			newline();
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mNL3(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NL3;
		int _saveIndex;
		
		match("\r");
		if ( inputState.guessing==0 ) {
			newline();
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = WS;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case ' ':
		{
			match(' ');
			break;
		}
		case '\t':
		{
			match('\t');
			break;
		}
		case '\u000c':
		{
			match('\f');
			break;
		}
		case '\n':  case '\r':
		{
			mNL(false);
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			_ttype = Token.SKIP;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mALPHA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ALPHA;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':
		{
			{
			matchRange('A','Z');
			}
			break;
		}
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			{
			matchRange('a','z');
			}
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mQUOTE3S(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUOTE3S;
		int _saveIndex;
		
		match("'''");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mESCAPE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ESCAPE;
		int _saveIndex;
		char  ch = '\0';
		
		_saveIndex=text.length();
		match('\\');
		text.setLength(_saveIndex);
		{
		boolean synPredMatched474 = false;
		if (((_tokenSet_7.member(LA(1))) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true))) {
			int _m474 = mark();
			synPredMatched474 = true;
			inputState.guessing++;
			try {
				{
				mESC_CHAR(false);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched474 = false;
			}
			rewind(_m474);
			inputState.guessing--;
		}
		if ( synPredMatched474 ) {
			mESC_CHAR(false);
		}
		else if (((LA(1) >= '\u0000' && LA(1) <= '\ufffe')) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true)) {
			ch = LA(1);
			matchNot(EOF_CHAR);
			if ( inputState.guessing==0 ) {
				text.setLength(_begin); text.append("\\"+ch) ;
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mQUOTE3D(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUOTE3D;
		int _saveIndex;
		
		match('"');
		match('"');
		match('"');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mESC_CHAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ESC_CHAR;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'n':
		{
			match('n');
			if ( inputState.guessing==0 ) {
				text.setLength(_begin); text.append("\n") ;
			}
			break;
		}
		case 'r':
		{
			match('r');
			if ( inputState.guessing==0 ) {
				text.setLength(_begin); text.append("\r") ;
			}
			break;
		}
		case 'b':
		{
			match('b');
			if ( inputState.guessing==0 ) {
				text.setLength(_begin); text.append("\b") ;
			}
			break;
		}
		case 't':
		{
			match('t');
			if ( inputState.guessing==0 ) {
				text.setLength(_begin); text.append("\t") ;
			}
			break;
		}
		case 'f':
		{
			match('f');
			if ( inputState.guessing==0 ) {
				text.setLength(_begin); text.append("\f") ;
			}
			break;
		}
		case 'v':
		{
			match('v');
			if ( inputState.guessing==0 ) {
				text.setLength(_begin); text.append("\f") ;
			}
			break;
		}
		case 'a':
		{
			match('a');
			if ( inputState.guessing==0 ) {
				text.setLength(_begin); text.append("\007") ;
			}
			break;
		}
		case '"':
		{
			match('"');
			if ( inputState.guessing==0 ) {
				text.setLength(_begin); text.append("\"") ;
			}
			break;
		}
		case '\\':
		{
			match('\\');
			if ( inputState.guessing==0 ) {
				text.setLength(_begin); text.append("\\") ;
			}
			break;
		}
		case '\'':
		{
			match('\'');
			if ( inputState.guessing==0 ) {
				text.setLength(_begin); text.append("'") ;
			}
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = new long[1025];
		data[0]=576214461698801664L;
		data[1]=576460745995190270L;
		for (int i = 2; i<=1024; i++) { data[i]=0L; }
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = new long[1025];
		data[0]=287984085547089920L;
		data[1]=576460745995190270L;
		for (int i = 2; i<=1024; i++) { data[i]=0L; }
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = new long[1025];
		data[0]=287948901175001088L;
		data[1]=576460745995190270L;
		for (int i = 2; i<=1024; i++) { data[i]=0L; }
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = new long[2048];
		data[0]=-576179277326712833L;
		data[1]=-576460743847706623L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		for (int i = 1024; i<=2047; i++) { data[i]=0L; }
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = new long[2048];
		data[0]=-9217L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		for (int i = 1024; i<=2047; i++) { data[i]=0L; }
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = new long[1025];
		data[0]=287948901175001088L;
		data[1]=576460743847706622L;
		for (int i = 2; i<=1024; i++) { data[i]=0L; }
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = new long[2048];
		data[0]=-1L;
		data[1]=-268435457L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		for (int i = 1024; i<=2047; i++) { data[i]=0L; }
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = new long[1025];
		data[0]=566935683072L;
		data[1]=23714567704018944L;
		for (int i = 2; i<=1024; i++) { data[i]=0L; }
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	
	}
