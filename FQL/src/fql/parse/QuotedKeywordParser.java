package fql.parse;

import fql.Unit;

/**
 * 
 * @author ryan
 *
 * Parser for quoted keywords, for JSON.
 */
public class QuotedKeywordParser implements RyanParser<Unit> {

	String p;
	
	public QuotedKeywordParser(String p) {
		this.p = p;
	}
	
	@Override
	public Partial<Unit> parse(Tokens s) throws BadSyntax, IllTyped {
		return ParserUtils.outside(new KeywordParser("\""), new KeywordParser(p), new KeywordParser("\"")).parse(s);
	}

}
