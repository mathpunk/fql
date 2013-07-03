package fql.parse;

import fql.decl.Decl;

/**
 * 
 * @author ryan
 *
 * Parser for various kinds of instances.
 */
public class InstanceDeclParser implements RyanParser<Decl> {

	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		RyanParser<Decl> p;
		try {
			p = new ConstantInstanceDeclParser();
			return p.parse(s);
		} catch (Exception e) {
			
		}
		try {
			p = new EvalInstanceDeclParser();
			return p.parse(s);
		} catch (Exception e) {
			
		}
		
		try {
			p = new DSPInstanceDeclParser();
			return p.parse(s);
		} catch (Exception e) {
			
		}
		try {
			p = new ConstantInstanceDeclParser();
			return p.parse(s);
		} catch (Exception e) {
			
		}
		try {
			p = new ExternalDeclParser();
			return p.parse(s);
		} catch (Exception e) {
			
		}

		throw new BadSyntax(s, "Cannot parse instance decl");
	}

}
