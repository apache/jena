// $ANTLR : "n3.g" -> "N3AntlrLexer.java"$

package com.hp.hpl.jena.n3 ;
import antlr.TokenStreamRecognitionException ;

import java.io.InputStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import java.io.Reader;
import java.util.Hashtable;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.TokenStream;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;

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
					mSEP_OR_PATH(true);
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
					if ((LA(1)=='<') && (_tokenSet_0.member(LA(2)))) {
						mURI_OR_IMPLIES(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='@') && (_tokenSet_1.member(LA(2)))) {
						mAT_WORD(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='^') && (LA(2)=='^')) {
						mDATATYPE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (LA(2)=='>')) {
						mARROW_R(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='-')) {
						mARROW_PATH_L(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (LA(2)=='>')) {
						mARROW_PATH_R(true);
						theRetToken=_returnToken;
					}
					else if ((_tokenSet_2.member(LA(1))) && (true)) {
						mTHING(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (true)) {
						mLANGLE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (true)) {
						mRANGLE(true);
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

	public final void mTHING(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = THING;
		int _saveIndex;
		
		boolean synPredMatched251 = false;
		if (((_tokenSet_3.member(LA(1))) && (_tokenSet_4.member(LA(2))) && (_tokenSet_4.member(LA(3))))) {
			int _m251 = mark();
			synPredMatched251 = true;
			inputState.guessing++;
			try {
				{
				mNSNAME(false);
				mCOLON(false);
				mLNAME(false);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched251 = false;
			}
			rewind(_m251);
			inputState.guessing--;
		}
		if ( synPredMatched251 ) {
			mNSNAME(false);
			mCOLON(false);
			mLNAME(false);
			if ( inputState.guessing==0 ) {
				_ttype = QNAME ;
			}
		}
		else {
			boolean synPredMatched263 = false;
			if (((LA(1)=='h') && (LA(2)=='a') && (LA(3)=='s'))) {
				int _m263 = mark();
				synPredMatched263 = true;
				inputState.guessing++;
				try {
					{
					match("has");
					mNON_ANC(false);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched263 = false;
				}
				rewind(_m263);
				inputState.guessing--;
			}
			if ( synPredMatched263 ) {
				match("has");
				if ( inputState.guessing==0 ) {
					_ttype = KW_HAS ;
				}
			}
			else {
				boolean synPredMatched267 = false;
				if (((LA(1)=='t') && (LA(2)=='h') && (LA(3)=='i'))) {
					int _m267 = mark();
					synPredMatched267 = true;
					inputState.guessing++;
					try {
						{
						match("this");
						mNON_ANC(false);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched267 = false;
					}
					rewind(_m267);
					inputState.guessing--;
				}
				if ( synPredMatched267 ) {
					match("this");
					if ( inputState.guessing==0 ) {
						_ttype = KW_THIS ;
					}
				}
				else {
					boolean synPredMatched253 = false;
					if (((LA(1)==':') && (_tokenSet_3.member(LA(2))))) {
						int _m253 = mark();
						synPredMatched253 = true;
						inputState.guessing++;
						try {
							{
							mCOLON(false);
							mLNAME(false);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched253 = false;
						}
						rewind(_m253);
						inputState.guessing--;
					}
					if ( synPredMatched253 ) {
						mCOLON(false);
						mLNAME(false);
						if ( inputState.guessing==0 ) {
							_ttype = QNAME ;
						}
					}
					else {
						boolean synPredMatched255 = false;
						if (((_tokenSet_3.member(LA(1))) && (_tokenSet_4.member(LA(2))) && (true))) {
							int _m255 = mark();
							synPredMatched255 = true;
							inputState.guessing++;
							try {
								{
								mNSNAME(false);
								mCOLON(false);
								}
							}
							catch (RecognitionException pe) {
								synPredMatched255 = false;
							}
							rewind(_m255);
							inputState.guessing--;
						}
						if ( synPredMatched255 ) {
							mNSNAME(false);
							mCOLON(false);
							if ( inputState.guessing==0 ) {
								_ttype = QNAME ;
							}
						}
						else {
							boolean synPredMatched259 = false;
							if (((LA(1)==':') && (LA(2)=='-'))) {
								int _m259 = mark();
								synPredMatched259 = true;
								inputState.guessing++;
								try {
									{
									mCOLON(false);
									match('-');
									}
								}
								catch (RecognitionException pe) {
									synPredMatched259 = false;
								}
								rewind(_m259);
								inputState.guessing--;
							}
							if ( synPredMatched259 ) {
								match(":-");
								if ( inputState.guessing==0 ) {
									_ttype = NAME_OP ;
								}
							}
							else {
								boolean synPredMatched265 = false;
								if (((LA(1)=='o') && (LA(2)=='f') && (true))) {
									int _m265 = mark();
									synPredMatched265 = true;
									inputState.guessing++;
									try {
										{
										match("of");
										mNON_ANC(false);
										}
									}
									catch (RecognitionException pe) {
										synPredMatched265 = false;
									}
									rewind(_m265);
									inputState.guessing--;
								}
								if ( synPredMatched265 ) {
									match("of");
									if ( inputState.guessing==0 ) {
										_ttype = KW_OF ;
									}
								}
								else {
									boolean synPredMatched271 = false;
									if (((LA(1)=='i') && (LA(2)=='s') && (true))) {
										int _m271 = mark();
										synPredMatched271 = true;
										inputState.guessing++;
										try {
											{
											match("is");
											mNON_ANC(false);
											}
										}
										catch (RecognitionException pe) {
											synPredMatched271 = false;
										}
										rewind(_m271);
										inputState.guessing--;
									}
									if ( synPredMatched271 ) {
										match("is");
										if ( inputState.guessing==0 ) {
											_ttype = KW_IS ;
										}
									}
									else {
										boolean synPredMatched257 = false;
										if (((LA(1)==':') && (true))) {
											int _m257 = mark();
											synPredMatched257 = true;
											inputState.guessing++;
											try {
												{
												mCOLON(false);
												}
											}
											catch (RecognitionException pe) {
												synPredMatched257 = false;
											}
											rewind(_m257);
											inputState.guessing--;
										}
										if ( synPredMatched257 ) {
											mCOLON(false);
											if ( inputState.guessing==0 ) {
												_ttype = QNAME ;
											}
										}
										else {
											boolean synPredMatched261 = false;
											if (((_tokenSet_5.member(LA(1))) && (true) && (true))) {
												int _m261 = mark();
												synPredMatched261 = true;
												inputState.guessing++;
												try {
													{
													mNUMBER(false);
													}
												}
												catch (RecognitionException pe) {
													synPredMatched261 = false;
												}
												rewind(_m261);
												inputState.guessing--;
											}
											if ( synPredMatched261 ) {
												mNUMBER(false);
												if ( inputState.guessing==0 ) {
													_ttype = NUMBER ;
												}
											}
											else {
												boolean synPredMatched269 = false;
												if (((LA(1)=='a') && (true))) {
													int _m269 = mark();
													synPredMatched269 = true;
													inputState.guessing++;
													try {
														{
														match("a");
														mNON_ANC(false);
														}
													}
													catch (RecognitionException pe) {
														synPredMatched269 = false;
													}
													rewind(_m269);
													inputState.guessing--;
												}
												if ( synPredMatched269 ) {
													match("a");
													if ( inputState.guessing==0 ) {
														_ttype = KW_A ;
													}
												}
												else {
													throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
												}
												}}}}}}}}}}
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
		_loop299:
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
				break _loop299;
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
		_loop303:
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
				break _loop303;
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
	
	protected final void mNUMBER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NUMBER;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '+':
		{
			match('+');
			break;
		}
		case '-':
		{
			match('-');
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		int _cnt307=0;
		_loop307:
		do {
			if (((LA(1) >= '0' && LA(1) <= '9'))) {
				matchRange('0','9');
			}
			else {
				if ( _cnt307>=1 ) { break _loop307; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt307++;
		} while (true);
		}
		{
		boolean synPredMatched311 = false;
		if (((LA(1)=='.'))) {
			int _m311 = mark();
			synPredMatched311 = true;
			inputState.guessing++;
			try {
				{
				mDOT(false);
				{
				matchRange('0','9');
				}
				}
			}
			catch (RecognitionException pe) {
				synPredMatched311 = false;
			}
			rewind(_m311);
			inputState.guessing--;
		}
		if ( synPredMatched311 ) {
			mDOT(false);
			{
			int _cnt313=0;
			_loop313:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt313>=1 ) { break _loop313; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt313++;
			} while (true);
			}
		}
		else {
		}
		
		}
		{
		if ((LA(1)=='e')) {
			match('e');
			{
			switch ( LA(1)) {
			case '+':
			{
				match('+');
				break;
			}
			case '-':
			{
				match('-');
				break;
			}
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':
			{
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			{
			int _cnt317=0;
			_loop317:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt317>=1 ) { break _loop317; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt317++;
			} while (true);
			}
		}
		else {
		}
		
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
		match(_tokenSet_6);
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
		
		boolean synPredMatched276 = false;
		if (((LA(1)=='<') && (LA(2)=='=') && (LA(3)=='>'))) {
			int _m276 = mark();
			synPredMatched276 = true;
			inputState.guessing++;
			try {
				{
				mARROW_MEANS(false);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched276 = false;
			}
			rewind(_m276);
			inputState.guessing--;
		}
		if ( synPredMatched276 ) {
			mARROW_MEANS(false);
			if ( inputState.guessing==0 ) {
				_ttype = ARROW_MEANS ;
			}
		}
		else {
			boolean synPredMatched274 = false;
			if (((LA(1)=='<') && (LA(2)=='=') && (true))) {
				int _m274 = mark();
				synPredMatched274 = true;
				inputState.guessing++;
				try {
					{
					mARROW_L(false);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched274 = false;
				}
				rewind(_m274);
				inputState.guessing--;
			}
			if ( synPredMatched274 ) {
				mARROW_L(false);
				if ( inputState.guessing==0 ) {
					_ttype = ARROW_L ;
				}
			}
			else if ((LA(1)=='<') && (_tokenSet_0.member(LA(2))) && (true)) {
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
		mLANGLE(false);
		text.setLength(_saveIndex);
		{
		_loop280:
		do {
			// nongreedy exit test
			if ((LA(1)=='>') && (true)) break _loop280;
			if ((_tokenSet_0.member(LA(1))) && (_tokenSet_0.member(LA(2)))) {
				{
				match(_tokenSet_0);
				}
			}
			else {
				break _loop280;
			}
			
		} while (true);
		}
		_saveIndex=text.length();
		mRANGLE(false);
		text.setLength(_saveIndex);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLANGLE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LANGLE;
		int _saveIndex;
		
		match('<');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRANGLE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RANGLE;
		int _saveIndex;
		
		match('>');
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
			mNUMERIC(false);
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
		int _cnt284=0;
		_loop284:
		do {
			if ((_tokenSet_7.member(LA(1)))) {
				mALPHANUMERIC(false);
			}
			else {
				if ( _cnt284>=1 ) { break _loop284; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt284++;
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
	
	public final void mAT_WORD(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = AT_WORD;
		int _saveIndex;
		Token a=null;
		
		boolean synPredMatched287 = false;
		if (((LA(1)=='@') && (LA(2)=='p') && (LA(3)=='r'))) {
			int _m287 = mark();
			synPredMatched287 = true;
			inputState.guessing++;
			try {
				{
				mAT(false);
				match("prefix");
				}
			}
			catch (RecognitionException pe) {
				synPredMatched287 = false;
			}
			rewind(_m287);
			inputState.guessing--;
		}
		if ( synPredMatched287 ) {
			mAT(false);
			match("prefix");
			if ( inputState.guessing==0 ) {
				_ttype = AT_PREFIX ;
			}
		}
		else {
			boolean synPredMatched290 = false;
			if (((LA(1)=='@') && (_tokenSet_1.member(LA(2))) && (true))) {
				int _m290 = mark();
				synPredMatched290 = true;
				inputState.guessing++;
				try {
					{
					mAT(false);
					{
					mALPHA(false);
					}
					}
				}
				catch (RecognitionException pe) {
					synPredMatched290 = false;
				}
				rewind(_m290);
				inputState.guessing--;
			}
			if ( synPredMatched290 ) {
				mAT(false);
				{
				int _cnt_a=0;
				a:
				do {
					if ((_tokenSet_1.member(LA(1)))) {
						mALPHA(false);
					}
					else {
						if ( _cnt_a>=1 ) { break a; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt_a++;
				} while (true);
				}
				{
				if ((LA(1)=='-')) {
					match("-");
					{
					_loop295:
					do {
						if ((_tokenSet_1.member(LA(1)))) {
							mALPHA(false);
						}
						else {
							break _loop295;
						}
						
					} while (true);
					}
				}
				else {
				}
				
				}
				if ( inputState.guessing==0 ) {
					_ttype = AT_LANG ;
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
	
	protected final void mDOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOT;
		int _saveIndex;
		
		match('.');
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
		
		boolean synPredMatched378 = false;
		if (((LA(1)=='\'') && (LA(2)=='\'') && (LA(3)=='\''))) {
			int _m378 = mark();
			synPredMatched378 = true;
			inputState.guessing++;
			try {
				{
				mQUOTE3S(false);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched378 = false;
			}
			rewind(_m378);
			inputState.guessing--;
		}
		if ( synPredMatched378 ) {
			_saveIndex=text.length();
			mQUOTE3S(false);
			text.setLength(_saveIndex);
			{
			_loop383:
			do {
				// nongreedy exit test
				if ((LA(1)=='\'') && (LA(2)=='\'') && (LA(3)=='\'')) break _loop383;
				boolean synPredMatched381 = false;
				if (((LA(1)=='\n'||LA(1)=='\r') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')))) {
					int _m381 = mark();
					synPredMatched381 = true;
					inputState.guessing++;
					try {
						{
						mNL(false);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched381 = false;
					}
					rewind(_m381);
					inputState.guessing--;
				}
				if ( synPredMatched381 ) {
					mNL(false);
				}
				else if ((_tokenSet_8.member(LA(1))) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe'))) {
					{
					match(_tokenSet_8);
					}
				}
				else if ((LA(1)=='\\')) {
					mESCAPE(false);
				}
				else {
					break _loop383;
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
			_loop385:
			do {
				// nongreedy exit test
				if ((LA(1)=='\'') && (true)) break _loop385;
				if ((_tokenSet_8.member(LA(1))) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe'))) {
					matchNot('\\');
				}
				else if ((LA(1)=='\\')) {
					mESCAPE(false);
				}
				else {
					break _loop385;
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
		
		boolean synPredMatched388 = false;
		if (((LA(1)=='"') && (LA(2)=='"') && (LA(3)=='"'))) {
			int _m388 = mark();
			synPredMatched388 = true;
			inputState.guessing++;
			try {
				{
				mQUOTE3D(false);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched388 = false;
			}
			rewind(_m388);
			inputState.guessing--;
		}
		if ( synPredMatched388 ) {
			_saveIndex=text.length();
			mQUOTE3D(false);
			text.setLength(_saveIndex);
			{
			_loop393:
			do {
				// nongreedy exit test
				if ((LA(1)=='"') && (LA(2)=='"') && (LA(3)=='"')) break _loop393;
				boolean synPredMatched391 = false;
				if (((LA(1)=='\n'||LA(1)=='\r') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')))) {
					int _m391 = mark();
					synPredMatched391 = true;
					inputState.guessing++;
					try {
						{
						mNL(false);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched391 = false;
					}
					rewind(_m391);
					inputState.guessing--;
				}
				if ( synPredMatched391 ) {
					mNL(false);
				}
				else if ((_tokenSet_8.member(LA(1))) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe'))) {
					{
					match(_tokenSet_8);
					}
				}
				else if ((LA(1)=='\\')) {
					mESCAPE(false);
				}
				else {
					break _loop393;
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
			_loop395:
			do {
				// nongreedy exit test
				if ((LA(1)=='"') && (true)) break _loop395;
				if ((_tokenSet_8.member(LA(1))) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe'))) {
					matchNot('\\');
				}
				else if ((LA(1)=='\\')) {
					mESCAPE(false);
				}
				else {
					break _loop395;
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
	
	public final void mSEP_OR_PATH(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SEP_OR_PATH;
		int _saveIndex;
		
		boolean synPredMatched323 = false;
		if (((LA(1)=='.') && (true) && (true))) {
			int _m323 = mark();
			synPredMatched323 = true;
			inputState.guessing++;
			try {
				{
				mDOT(false);
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
				case '_':
				{
					match('_');
					break;
				}
				case ':':
				{
					mCOLON(false);
					break;
				}
				case '<':
				{
					mLANGLE(false);
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				}
			}
			catch (RecognitionException pe) {
				synPredMatched323 = false;
			}
			rewind(_m323);
			inputState.guessing--;
		}
		if ( synPredMatched323 ) {
			mDOT(false);
			if ( inputState.guessing==0 ) {
				_ttype = PATH ;
			}
		}
		else if ((LA(1)=='.') && (true) && (true)) {
			mDOT(false);
			if ( inputState.guessing==0 ) {
				_ttype = SEP ;
			}
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
	
	public final void mDATATYPE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DATATYPE;
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
	
	public final void mARROW_PATH_L(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ARROW_PATH_L;
		int _saveIndex;
		
		match(">-");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mARROW_PATH_R(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ARROW_PATH_R;
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
		_loop351:
		do {
			if ((_tokenSet_0.member(LA(1)))) {
				{
				match(_tokenSet_0);
				}
			}
			else {
				break _loop351;
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
		
		boolean synPredMatched358 = false;
		if (((LA(1)=='\r') && (LA(2)=='\n') && (true))) {
			int _m358 = mark();
			synPredMatched358 = true;
			inputState.guessing++;
			try {
				{
				mNL1(false);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched358 = false;
			}
			rewind(_m358);
			inputState.guessing--;
		}
		if ( synPredMatched358 ) {
			mNL1(false);
		}
		else {
			boolean synPredMatched360 = false;
			if (((LA(1)=='\n'))) {
				int _m360 = mark();
				synPredMatched360 = true;
				inputState.guessing++;
				try {
					{
					mNL2(false);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched360 = false;
				}
				rewind(_m360);
				inputState.guessing--;
			}
			if ( synPredMatched360 ) {
				mNL2(false);
			}
			else {
				boolean synPredMatched362 = false;
				if (((LA(1)=='\r') && (true) && (true))) {
					int _m362 = mark();
					synPredMatched362 = true;
					inputState.guessing++;
					try {
						{
						mNL3(false);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched362 = false;
					}
					rewind(_m362);
					inputState.guessing--;
				}
				if ( synPredMatched362 ) {
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
	
	protected final void mNWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NWS;
		int _saveIndex;
		
		{
		match(_tokenSet_9);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mNUMERIC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NUMERIC;
		int _saveIndex;
		
		{
		matchRange('0','9');
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
		boolean synPredMatched401 = false;
		if (((_tokenSet_10.member(LA(1))) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true))) {
			int _m401 = mark();
			synPredMatched401 = true;
			inputState.guessing++;
			try {
				{
				mESC_CHAR(false);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched401 = false;
			}
			rewind(_m401);
			inputState.guessing--;
		}
		if ( synPredMatched401 ) {
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
		long[] data = new long[2048];
		data[0]=-9217L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = new long[1025];
		data[1]=576460743847706622L;
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = new long[1025];
		data[0]=576223257791823872L;
		data[1]=576460745995190270L;
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = new long[1025];
		data[0]=287948901175001088L;
		data[1]=576460745995190270L;
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = new long[1025];
		data[0]=576214461698801664L;
		data[1]=576460745995190270L;
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = new long[1025];
		data[0]=287992881640112128L;
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = new long[2048];
		data[0]=-576179277326712833L;
		data[1]=-576460743847706623L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = new long[1025];
		data[0]=287948901175001088L;
		data[1]=576460743847706622L;
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = new long[2048];
		data[0]=-1L;
		data[1]=-268435457L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = new long[2048];
		data[0]=-4294981121L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = new long[1025];
		data[0]=566935683072L;
		data[1]=23714567704018944L;
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	
	}
