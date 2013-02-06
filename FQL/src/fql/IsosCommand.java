package fql;

/**
 * 
 * @author ryan
 *
 * Command for enumerating isomorphisms.
 */
public class IsosCommand extends Command {

	String lhs, rhs;
	
	public IsosCommand(String text, String lhs, String rhs) {
		super(text);
		this.lhs = lhs;
		this.rhs = rhs;
	}

}