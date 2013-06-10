package fql.parse;

import java.util.List;

import fql.Pair;

/**
 * 
 * @author ryan
 *
 * Parser for the data part of instance declarations.
 */
public class TableDeclParser implements
		Parser<Pair<String, List<Pair<String, String>>>> {

	@Override
	public Partial<Pair<String, List<Pair<String, String>>>> parse(Tokens s)
			throws BadSyntax, IllTyped {
		
		String name;
		List<Pair<String, String>> data;

		Parser<?> e = new KeywordParser("->");
		
		Parser<?> sep = new KeywordParser(",");
		Parser<Pair<String, String>> p1 = new TupleParser();
		Parser<List<Pair<String, String>>> u = ParserUtils.manySep(p1, sep);
		
		Parser<?> l = new KeywordParser("{");
		Parser<?> r = new KeywordParser("}");
		Parser<List<Pair<String, String>>> u0 =ParserUtils.outside(l, u, r);
		
		Partial<String> x = new StringParser().parse(s);
		name = x.value;
		s = x.tokens;

		Partial<?> y = e.parse(s);
		s = y.tokens;
		
		Partial<List<Pair<String, String>>> z = u0.parse(s);
		data = z.value;
		s = z.tokens;

		return new Partial<Pair<String, List<Pair<String, String>>>>(s, 
				new Pair<String, List<Pair<String, String>>>(name, data));
	}

}
