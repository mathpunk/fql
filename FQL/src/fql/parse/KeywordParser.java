package fql.parse;


public class KeywordParser implements Parser<Unit> {

	String word;

	
	public KeywordParser(String keyword) {
		word = keyword;
	}

	public Partial<Unit> parse(Tokens s) throws BadSyntax {
		if (s.head().equals(word)) {
			return new Partial<Unit>(s.pop(), new Unit());
		}
		throw new BadSyntax("Keyword " + word + " expected at " + s.head() + " " + s.peek(1) + " " + s.peek(2));
	}

}