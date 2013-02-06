package fql.parse;

import java.util.List;

import fql.Pair;

public class EqParser implements Parser<Pair<List<String>, List<String>>> {

	@Override
	public Partial<Pair<List<String>, List<String>>> parse(Tokens s)
			throws BadSyntax, IllTyped {
		Parser<List<String>> p = new PathParser();
		Parser<?> e = new KeywordParser("=");
		
		Partial<List<String>> x = p.parse(s);
		
		Partial<?> y = e.parse(x.tokens);
		
		Partial<List<String>> z = p.parse(y.tokens);
		
		return new Partial<Pair<List<String>, List<String>>>(z.tokens, new Pair<List<String>, List<String>>(x.value,z.value));
	}

}