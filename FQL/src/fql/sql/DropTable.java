package fql.sql;

import java.util.Map;
import java.util.Set;

/**
 * 
 * @author ryan
 * 
 *         Drop table statements.
 */
public class DropTable extends PSM {

	String name;

	public DropTable(String name) {
		this.name = name;
	}

	@Override
	public String toPSM() {
		return "DROP TABLE " + name + ";";
	}

	@Override
	public void exec(Map<String, Set<Map<String, Object>>> state) {
		if (state.containsKey(name) && state.get(name) == null) {
			throw new RuntimeException("Table does not exist: " + name);
		}
		state.remove(name);
	}

}
