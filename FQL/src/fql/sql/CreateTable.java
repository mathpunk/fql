package fql.sql;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateTable extends PSM {

	String name;
	Map<String, String> attrs;
	
	public CreateTable(String name, Map<String, String> attrs) {
		this.name = name;
		this.attrs = attrs;
	}

	@Override
	public String toPSM() {
		String s = "";
		List<String> keys = new LinkedList<>(attrs.keySet());
		
		for (int i = 0; i < keys.size(); i++) {
			if (i > 0) {
				s += ", ";
			}
			s += keys.get(i) + " " + attrs.get(keys.get(i));
		}
		
		return "CREATE TABLE " + name + "(" + s + ");";
	}

	@Override
	public void exec(Map<String, Set<Map<String, Object>>> state) {
		if (state.get(name) != null) {
			throw new RuntimeException("table already exists: " + name + " in " + state);
		}
		state.put(name, new HashSet<Map<String, Object>>());
	}


}
