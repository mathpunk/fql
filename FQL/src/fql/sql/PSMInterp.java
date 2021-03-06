package fql.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author ryan
 * 
 *         Interpreter for PSM
 */
public class PSMInterp {

	public int guid = 0; 
	
	public Map<String, Integer> sigmas = new HashMap<>();
	public Map<String, Integer> sigmas2 = new HashMap<>();

	// wraps with binary tables
//	public static String interp0(List<PSM> prog) {
	//	Map<String, Set<Map<Object, Object>>> s = interp(prog);

//		return s.toString();
//	}

	public  Map<String, Set<Map<Object, Object>>> interpX(List<PSM> prog,
			Map<String, Set<Map<Object, Object>>> state) {
		// System.out.println(prog);
		for (PSM cmd : prog) {
			// System.out.println("Executing ");
			// System.out.println(cmd);
			// System.out.println(state);

			cmd.exec(this, state);
			
			// System.out.println("RESULT " + state);
			// System.out.println("DONE EXECUTING");

		}
		// System.out.println("RESULT " + state);
		return state;
	}

	public Map<String, Set<Map<Object, Object>>> interp(List<PSM> prog) {
		guid = 0;
		return interpX(prog, new HashMap<String, Set<Map<Object, Object>>>());
	}

}
