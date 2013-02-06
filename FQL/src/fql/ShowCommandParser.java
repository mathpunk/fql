package fql;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.KeywordParser;
import fql.parse.Parser;
import fql.parse.Partial;
import fql.parse.StringParser;
import fql.parse.Tokens;

/**
 * 
 * @author ryan
 *
 * Parser for show commands.
 */
public class ShowCommandParser implements Parser<Command> {

	@Override
	public Partial<Command> parse(Tokens s) throws BadSyntax, IllTyped {
		Parser<?> p = new KeywordParser("show");
		Parser<String> q = new StringParser();
		
		Partial<?> x = p.parse(s);
		Partial<String> y = q.parse(x.tokens);
		
		ShowCommand cmd = new ShowCommand(y.value);
		return new Partial<Command>(y.tokens, cmd);
	}

}