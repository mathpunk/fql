package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.Triple;
import fql.cat.Arr;
import fql.cat.CommaCat;
import fql.cat.FinCat;
import fql.cat.FinFunctor;
import fql.decl.Attribute;
import fql.decl.ConstantInstanceDecl;
import fql.decl.Decl;
import fql.decl.Edge;
import fql.decl.Environment;
import fql.decl.EvalDSPInstanceDecl;
import fql.decl.EvalInstanceDecl;
import fql.decl.Instance;
import fql.decl.Int;
import fql.decl.Mapping;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Program;
import fql.decl.Signature;
import fql.decl.Type;
import fql.decl.Varchar;

public class PSMGen {
	
	
	
	public static List<PSM> guidify(String pre0, Signature sig) {
	//	System.out.println("GUIDifying " + pre0);
		List<PSM> ret = new LinkedList<>();
	
		 Map<String, String> guid_attrs = new HashMap<>();
		 Map<String, String> twocol_attrs = new HashMap<>();
	
		twocol_attrs.put("c0", PSM.VARCHAR);
		twocol_attrs.put("c1", PSM.VARCHAR);
		guid_attrs.put("c0", PSM.VARCHAR);
		guid_attrs.put("c1", PSM.VARCHAR);
		guid_attrs.put("guid", PSM.VARCHAR);
		
		List<String> attrs_foo = new LinkedList<>();
		attrs_foo.add("c0");
		attrs_foo.add("c1");
		
		
		for (Node n : sig.nodes) {
			String pre = pre0 + "_" + n;

			//make new table with GUID
			ret.add(new CreateTable(pre + "_guid", guid_attrs));
			ret.add(new InsertKeygen(pre + "_guid", "guid", pre, attrs_foo));
		
			//make a substitution table
			ret.add(new CreateTable(pre + "_subst", twocol_attrs));
			ret.add(new InsertSQL(pre + "_subst", makeSubst(pre0, n)));
			
			ret.add(new CreateTable(pre + "_subst_inv", twocol_attrs));
			ret.add(new InsertSQL(pre + "_subst_inv", invertSubst(pre0, n)));
			
			//create a new table that applies the substitution	
			ret.add(new CreateTable(pre + "_applied", twocol_attrs));
			ret.add(new InsertSQL(pre + "_applied", makeApplyNode(pre0, n)));
			
			//drop guid table
			ret.add(new DropTable(pre + "_guid"));
			
			//drop original table
			ret.add(new DropTable(pre));
			
			//copy the new table
			ret.add(new CreateTable(pre, twocol_attrs));
			ret.add(new InsertSQL(pre, new CopyFlower(pre + "_applied")));
			
			//drop the new table
			ret.add(new DropTable(pre + "_applied"));		
		}

		for (Edge e : sig.edges) {
			String pre = pre0 + "_" + e.name;
			
			//create a new table that applies the substitution	
			ret.add(new CreateTable(pre + "_applied", twocol_attrs));
			ret.add(new InsertSQL(pre + "_applied", makeApplyEdge(pre0, e)));
			
			//drop original table
			ret.add(new DropTable(pre));
			
			//copy the new table
			ret.add(new CreateTable(pre, twocol_attrs));
			ret.add(new InsertSQL(pre, new CopyFlower(pre + "_applied")));
			
			//drop the new table
			ret.add(new DropTable(pre + "_applied"));		

		}
		
		for (Attribute a : sig.attrs) {
			String pre = pre0 + "_" + a.name;
			
			//create a new table that applies the substitution	
			
			
			ret.add(new CreateTable(pre + "_applied", colattrs(a)));
			ret.add(new InsertSQL(pre + "_applied", makeAttr(pre0, a)));
			
			//drop original table
			ret.add(new DropTable(pre));
			
			//copy the new table
			ret.add(new CreateTable(pre, twocol_attrs));
			ret.add(new InsertSQL(pre, new CopyFlower(pre + "_applied")));
			
			//drop the new table
			ret.add(new DropTable(pre + "_applied"));		

		}
		
		//same for attributes, but one sided
				
		for (Node n : sig.nodes) {
			String pre = pre0 + "_" + n;
			ret.add(new DropTable(pre + "_subst"));
			ret.add(new DropTable(pre + "_subst_inv"));
		}
//		System.out.println("&&&&&&&&&&&&");
//		System.out.println(ret);
		return ret;
	}

	private static Map<String, String> colattrs(Attribute a) {
		Map<String, String> twocol_attrs = new HashMap<>();
		twocol_attrs.put("c0", PSM.VARCHAR);
		twocol_attrs.put("c1", typeTrans(a.target));
		return twocol_attrs;
	}

	private static SQL invertSubst(String pre0, Node n) {
		String pre = pre0 + "_" + n;
		Map<String, Pair<String, String>> select = new HashMap<>();
		select.put("c0", new Pair<>(pre + "_subst", "c1"));
		select.put("c1", new Pair<>(pre + "_subst", "c0"));
		
		Map<String, String> from = new HashMap<>();
		from.put(pre + "_subst", pre + "_subst");
		
		List<Pair<Pair<String, String>, Pair<String, String>>> where
		 = new LinkedList<>();
		
		Flower f = new Flower(select, from, where);
		
		return f;
	}

	private static SQL makeApplyEdge(String i, Edge e) {

		 String src = e.source.string;
		 String dst = e.target.string;

		SQL f = compose(new String[] { i + "_" + src + "_subst_inv",
				i + "_" + e.name, i + "_" + dst + "_subst"
		});
		
	//	System.out.println("apply edge for " + e + " on " + i + " is " + f);

		return f;
		 	}
	
	private static SQL makeAttr(String i, Attribute a) {

		 String src = a.source.string;

		SQL f = compose(new String[] { i + "_" + src + "_subst_inv",
				i + "_" + a.name});
		
		//System.out.println("apply aatr for " + a + " on " + i + " is " + f);

		return f;
		 	}

	private static SQL makeApplyNode(String i, Node n) {
		List<Pair<Pair<String, String>, Pair<String, String>>> where
		 = new LinkedList<>();
		 
		Map<String, String> from = new HashMap<>();
		from.put(i + "_" + n.string + "_guid", i + "_" + n.string + "_guid");
		
		Map<String, Pair<String, String>> select = new HashMap<>();
		select.put("c0", new Pair<>(i + "_" + n.string + "_guid", "guid"));
		select.put("c1", new Pair<>(i + "_" + n.string + "_guid", "guid"));
		
		return new Flower(select, from, where);
	}

	//project guid as c0, c0 as c1 from i_n_guid
	private static SQL makeSubst(String i, Node n) {
		List<Pair<Pair<String, String>, Pair<String, String>>> where
		 = new LinkedList<>();
		 
		Map<String, String> from = new HashMap<>();
		from.put(i + "_" + n.string + "_guid", i + "_" + n.string + "_guid");
		
		Map<String, Pair<String, String>> select = new HashMap<>();
		select.put("c1", new Pair<>(i + "_" + n.string + "_guid", "guid"));
		select.put("c0", new Pair<>(i + "_" + n.string + "_guid", "c0"));
		
		return new Flower(select, from, where);
	}

	public static List<PSM> compile0(Environment env, Program prog) throws FQLException {
		List<PSM> ret = new LinkedList<>();
		for (Decl d : prog.decls) {
			if (d instanceof ConstantInstanceDecl) {
				ret.addAll(addInstance(env, (ConstantInstanceDecl) d));
			} else if (d instanceof EvalInstanceDecl) {
				ret.addAll(addInstance(env, (EvalInstanceDecl) d));
			} else if (d instanceof EvalDSPInstanceDecl) {
				ret.addAll(addInstance(env, (EvalDSPInstanceDecl) d));
			}
		}
		return ret;
	}
	
	public static String compile(Environment env, Program prog) throws FQLException {
		return preamble + prettyPrint(compile0(env, prog));
	}

	private static String prettyPrint(List<PSM> l) {
		String ret = "";
		for (PSM p : l) {
			ret += p.toPSM() + "\n\n";
		}
		return ret;
	}

	private static List<PSM> addInstance(Environment env, EvalInstanceDecl d) {
		List<PSM> ret = new LinkedList<>();
		
		return ret;
	}

	private static List<PSM> addInstance(Environment env, EvalDSPInstanceDecl d) throws FQLException {
		List<PSM> ret = new LinkedList<>();
		
		String inst = d.inst;
		Mapping f = env.getMapping(d.mapping);
		String name = d.name;
		

		
		if (d.kind.equals("delta")) {
			ret.addAll(makeTables(name, f.source));
			ret.addAll(delta(f, inst, name));
			ret.addAll(guidify(name, f.source));
		//	System.out.println("adding " + delta(f, inst, name));
		} else if (d.kind.equals("sigma")) {
			ret.addAll(makeTables(name, f.target));
			ret.addAll(sigma(f, name, inst));
			ret.addAll(guidify(name, f.target));
		} else if (d.kind.equals("pi")) {
			ret.addAll(makeTables(name, f.target));
			ret.addAll(pi(f, inst, name));
		// not needed	ret.addAll(guidify(name, f.target));
		} else {
			throw new RuntimeException(d.kind);
		}
		
		return ret;
	}

	private static List<PSM> addInstance(Environment env, ConstantInstanceDecl d) throws FQLException {
		List<PSM> ret = new LinkedList<>();

		Signature sig = env.signatures.get(d.type);
		Instance inst = new Instance(d.name, sig, d.data);
		
		ret.addAll(makeTables(d.name, sig));

		for (Node n : sig.nodes) {
			ret.add(populateTable(d.name, n.string, inst.data.get(n.string)));
		}
		for (Edge e : sig.edges) {
			ret.add(populateTable(d.name, e.name, inst.data.get(e.name)));
		}
		for (Attribute a : sig.attrs) {
			ret.add(populateTable(d.name, a.name, inst.data.get(a.name)));
		}
		
		ret.addAll(guidify(d.name, sig));
		
		return ret;
	}

	private static PSM populateTable(String iname,
			String tname, Set<Pair<String, String>> data) {
		
		List<String> attrs = new LinkedList<>();
		attrs.add("c0");
		attrs.add("c1");
		Set<Map<String, Object>> values = new HashSet<>();
		
		for (Pair<String, String> row : data) {
			Map<String, Object> m = new HashMap<>();
			m.put("c0", row.first);
			m.put("c1", row.second);
			values.add(m);
		}
		
		return new InsertValues(iname + "_" + tname, attrs, values);
	}

	private static List<PSM> makeTables(String name,
			Signature sig) {
		List<PSM> ret = new LinkedList<>();
		
		for (Node n : sig.nodes) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR);
			attrs.put("c1", PSM.VARCHAR);
			ret.add(new CreateTable(name + "_" + n.string, attrs));
		}
		for (Edge e : sig.edges) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR);
			attrs.put("c1", PSM.VARCHAR);
			ret.add(new CreateTable(name + "_" + e.name, attrs));
		}
		for (Attribute a : sig.attrs) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR);
			attrs.put("c1", typeTrans(a.target));
			ret.add(new CreateTable(name + "_" + a.name, attrs));
		}
		
		return ret;
	}
	
	private static String typeTrans(Type t) {
		if (t instanceof Int) {
			return PSM.INTEGER;
		} else if (t instanceof Varchar) {
			return PSM.VARCHAR;
		}
		throw new RuntimeException();
	}

	static String preamble = "DROP DATABASE FQL; CREATE DATABASE FQL; USE FQL; SET @guid := 0;\n\n";

	public static List<PSM> delta(Mapping m, String src, String dst) {
		Map<String, SQL> ret = new HashMap<>();
		for (Entry<Node, Node> n : m.nm.entrySet()) {
			ret.put(dst + "_" + n.getKey().string, new CopyFlower(src + "_" + n.getValue().string));
		}
		for (Entry<Edge, Path> e : m.em.entrySet()) {
			ret.put(dst + "_" + e.getKey().name, compose(src, e.getValue()));
		}
		for (Entry<Attribute, Attribute> a : m.am.entrySet()) {
			ret.put(dst + "_" + a.getKey().name, new CopyFlower(src + "_" + a.getValue().name));
		}
		List<PSM> ret0 = new LinkedList<>();
		for (String k : ret.keySet()) {
			SQL v = ret.get(k);
			ret0.add(new InsertSQL(k, v));
		}
		return ret0;
	}
	
	private static Flower compose(String[] p) {
		Map<String, Pair<String, String>> select = new HashMap<>();
		Map<String, String> from = new HashMap<>();
		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();

//		from.put("t0", pre + "_" + p.source.string);

		from.put("t0", p[0]);

		for (int i = 1; i < p.length; i++) {
			from.put("t" + i,  p[i]);
			where.add(new Pair<>(new Pair<>("t" + (i - 1), "c1"), new Pair<>("t" + i, "c0")));
		}
		
		select.put("c0", new Pair<>("t0", "c0"));
		select.put("c1", new Pair<>("t" + (p.length - 1), "c1" ));
		
		return new Flower(select, from, where);
	}

	private static Flower compose(String pre, Path p) {
		Map<String, Pair<String, String>> select = new HashMap<>();
		Map<String, String> from = new HashMap<>();
		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();

		from.put("t0", pre + "_" + p.source.string);
		
		int i = 1;
		for (Edge e : p.path) {
			from.put("t" + i, pre + "_" + e.name);
			where.add(new Pair<>(new Pair<>("t" + (i - 1), "c1"), new Pair<>("t" + i, "c0")));
			i++;
		}
		
		select.put("c0", new Pair<>("t0", "c0"));
		select.put("c1", new Pair<>("t" + (i-1), "c1" ));
		
		return new Flower(select, from, where);
	}
	

	
	public static List<PSM> sigma(Mapping F, String pre, String inst) throws FQLException {
		Signature C = F.source;
		Signature D = F.target;
		List<PSM> ret = new LinkedList<>();
		
		if (!FinFunctor.isDiscreteOpFib(F.toFunctor2().first)) {
			throw new FQLException("Not a discrete op-fibration: " + F.name);
		}
		
		for (Node d : D.nodes) {
			List<Flower> tn = new LinkedList<>();
			for (Node c : C.nodes) {
				if (F.nm.get(c).equals(d)) {
					tn.add(new CopyFlower(inst + "_" + c.string));
				}
			}
			
			SQL y = foldUnion(tn);
			ret.add(new InsertSQL(pre + "_" + d.string, y));
		}

		for (Edge e : D.edges) {
			Node d = e.source;
			//Node d0 = e.target;
			List<Flower> tn = new LinkedList<>();
			for (Node c : C.nodes) {
				if (F.nm.get(c).equals(d)) {
					Path pc = findEquiv(c, F, e);					
					Flower q = compose(inst, pc);
					tn.add(q);
				}
			}
			
			SQL y = foldUnion(tn);
			ret.add(new InsertSQL(pre + "_" + e.name, y));
		}
		
		for (Attribute a : D.attrs) {
			Node d = a.source;
			//Node d0 = e.target;
			List<Flower> tn = new LinkedList<>();
			for (Node c : C.nodes) {
				if (F.nm.get(c).equals(d)) {
					Attribute pc = findEquiv(c, F, a);					
					Flower q = new CopyFlower(inst + "_" + pc.name);
					tn.add(q);
				}
			}
			
			SQL y = foldUnion(tn);
			ret.add(new InsertSQL(pre + "_" + a.name, y));
		}
		
		return ret;
	}
	

	private static SQL foldUnion(List<Flower> tn) {
		if (tn.size() == 0) {
			throw new RuntimeException();
		}
		if (tn.size() == 1) {
			return tn.get(0);
		}
		return new Union(tn);
	}


	private static Attribute findEquiv(Node c, Mapping f, Attribute a) throws FQLException {
		Signature C = f.source;
		for (Attribute peqc : f.source.attrs) {
			if (!peqc.source.equals(c)) {
				continue;
			}
			if ( f.am.get(peqc).equals(a) ) {
				return peqc;
			}
		}
		throw new FQLException("Could not find attribute mapping to " + a
				+ " under " + f);
	}
	
	private static Path findEquiv(Node c, Mapping f, Edge e)
			throws FQLException {
		Signature C = f.source;
		Signature D = f.target;
		FinCat<Node, Path> C0 = C.toCategory2().first;
		for (Arr<Node, Path> peqc : C0.arrows) {
			Path path = peqc.arr;
			//Path path = new Path(f.source, p);
			if (!path.source.equals(c)) {
				continue;
			}
			Path path_f = f.appy(path);
			Fn<Path, Arr<Node, Path>> F = D.toCategory2().second;
			if (F.of(path_f).equals(F.of(new Path(D, e)))) {
				return path;
			}
		}
		throw new FQLException("Could not find path mapping to " + e
				+ " under " + f);
	}
	
//	int 
	
	public static List<PSM> pi(Mapping F0, String src, String dst) throws FQLException {
		temp = 0;
		Signature D0 = F0.target;
		Signature C0 = F0.source;
		FinCat<Node, Path> D = D0.toCategory2().first;
		FinCat<Node, Path> C = C0.toCategory2().first;
		FinFunctor<Node, Path, Node, Path> F = F0.toFunctor2().first;
		List<PSM> ret = new LinkedList<>();

		Map<String, Triple[]> colmap = new HashMap<>();
		for (Node d0 : D.objects) {
			CommaCat<Node, Path, Node, Path, Node, Path> B = doComma(D, C, F, d0, D0);

			Map<Triple<Node, Node, Arr<Node, Path>>, String> xxx1 = new HashMap<>();
			Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, String> xxx2 = new HashMap<>();
			List<PSM> xxx3 = deltaX(src, xxx1, xxx2, B.projB);
			ret.addAll(xxx3);
			
			Pair<Flower, Triple[]> xxx = lim(B, xxx1, xxx2);
			Flower r = xxx.first;
			Triple[] cols = xxx.second;
			
			colmap.put(d0.string, cols);
			
			Map<String, String> attrs1 = new HashMap<>();
			int iii = 0;
			for (Triple s : cols) {
				attrs1.put("c" + iii++, PSM.VARCHAR);
			}
			Map<String, String> attrs2 = new HashMap<>(attrs1);
			attrs2.put("guid", PSM.VARCHAR);
			
			List<String> attcs = new LinkedList<String>();
			for (int i = 0; i < cols.length; i++) {
				attcs.add("c" + i);
			}
			
			ret.add(new CreateTable(dst + "_" + d0.string + "_limnoguid", attrs1));
			ret.add(new InsertSQL(dst + "_" + d0.string + "_limnoguid", r));
			
			ret.add(new CreateTable(dst + "_" + d0.string + "_limit", attrs2));
			ret.add(new InsertKeygen(dst + "_" + d0.string + "_limit", "guid", dst + "_" + d0.string + "_limnoguid", attcs));
			
			//craeted by createTables
		//	ret.add(new CreateTable(dst + "_" + d0.string, twocol_attrs));
			ret.add(new InsertSQL(dst + "_" + d0.string, new SquishFlower(dst + "_" + d0.string + "_limit")));
		
			//drop all tables that are the values in xxx2 but not xxx1
			//drop limnoguid
		}

		for (Edge s : F0.target.edges) {
			Node dA = s.source;
//			CommaCat<Node, Path, Node, Path, Node, Path> BA = doComma(D, C, F, dA, D0);
	//		Pair<RA, String[]> q1 = lim(BA, deltaObj(BA.projB), deltaArr(BA.projB));

			Node dB = s.target;
		//	CommaCat<Node, Path, Node, Path, Node, Path> BB = doComma(D, C, F, dB, D0);
		//	Pair<RA, String[]> q2 = lim(BB,deltaObj(BB.projB), deltaArr(BB.projB));
	//		
	//		RA rau = chop1(q2.second.length, q2.first);
	////		RA rav = chop1(q1.second.length, q1.first);
		//	RA raw = new Product(rau, rav);

//			System.out.println("Testing rau ");
//			System.out.println("Query is " + rau);
//			printNicely(eval(rau, test0()));
//			System.out.println("end test");
//
//			System.out.println("Testing rav ");
//			System.out.println("Query is " + rav);
//			printNicely(eval(rav, test0()));
//			System.out.println("end test");
//
//			System.out.println("Testing raw ");
//			System.out.println("Query is " + raw);
//			printNicely(eval(raw, test0()));
//			System.out.println("end test");
//
			
	//		RA rax = subset(q2.second, q1.second, raw);
			
//			System.out.println("Testing rax ");
//			System.out.println("Query is " + rax);
//			printNicely(eval(rax, test0()));
//			System.out.println("end test");

			String q2 = dB.string;
			String q1 = dA.string;
			
			Triple[] q2cols = colmap.get(q2);
			Triple[] q1cols = colmap.get(q1);
		
			//List<Pair<Pair<String, String>, Pair<String, String>>> where = subset(dst, q1cols, q2cols, q1, q2);
			
			List<Pair<Pair<String, String>, Pair<String, String>>> where = subset(dst, q2cols, q1cols, q2, q1);
			Map<String, String> from = new HashMap<>();
			from.put(dst + "_" + q1 + "_limit", dst + "_" + q1 + "_limit");
			from.put(dst + "_" + q2 + "_limit", dst + "_" + q2 + "_limit");
			
			Map<String, Pair<String, String>> select = new HashMap<>();
			select.put("c0", new Pair<>(dst + "_" + q1 + "_limit", "guid") );
			select.put("c1", new Pair<>(dst + "_" + q2 + "_limit", "guid") );
			
			Flower f = new Flower(select, from, where);
			
			System.out.println("flower is " + f);
			
			ret.add(new InsertSQL(dst + "_" + s.name, f));

			//ret.put(s.name, ray);
		}

		return ret;
	}
	
	private static List<Pair<Pair<String, String>, Pair<String, String>>>
	subset(String pre, Triple[] q2cols, Triple[] q1cols, String q1name, String q2name) {
	//	System.out.println("trying subset " + print(q1cols) + " in " + print(q2cols));
		List<Pair<Pair<String, String>, Pair<String, String>>> ret = new LinkedList<>();
		
		a: for (int i = 0; i < q2cols.length; i++) {
		for (int j = 0; j < q1cols.length; j++) {
			Triple q1c = q2cols[i];
			Triple q2c = q1cols[j];
			if (q1c.second.equals(q2c.second)) {
				ret.add(new Pair<>(new Pair<>(pre + "_" + q1name + "_limit", "c" + i),
						           new Pair<>(pre + "_" + q2name + "_limit", "c" + j)));
				continue a;
			}
		}
		String xxx = "";
		for (Triple yyy : q1cols) {
			xxx += ", " + yyy;
		}
		throw new RuntimeException("No col " + q2cols[i] + " in " +xxx + " pre " + pre);
		
	}
	
		return ret;
		
//		a: for (int i = 0; i < q1cols.length; i++) {
//			for (int j = 0; j < q2cols.length; j++) {
//				if (q1cols[i].equals(q2cols[j])) {
//					int col1 = i+1;
//					int col2 = j+
//				//	int col2 = j+2+q1cols.length;
//					ret.add(new Pair<>(new Pair<>(),new Pair<>()));
//					q1q2 = new Select(q1q2, i+1, j+2+q1cols.length);
//					continue a;
//				}
//			}
//			throw new RuntimeException("No col " + q1cols[i] + " in " + q2cols);
//		}
//		return ret;
	}

	private static CommaCat<Node, Path, Node, Path, Node, Path> doComma(
			FinCat<Node, Path> d2,
			FinCat<Node, Path> c,
			FinFunctor<Node, Path, Node, Path> f,
			Node d0, Signature S) throws FQLException {
//		List<String> x = new LinkedList<String>();
//		x.add(d0);
//		List<List<String>> y = new LinkedList<List<String>>();
//		y.add(x);

		FinFunctor<Node, Path, Node, Path> d = FinFunctor
				.singleton(d2, d0, new Arr<>(d2.identities.get(d0).arr,d0,d0));
		CommaCat<Node, Path, Node, Path, Node, Path>
		B = new CommaCat<>(d.srcCat, c, d2, d, f);
		return B;
	}

	public static Flower squish(String s) {
		return new SquishFlower(s);
	}
	
	public static <Arrow> Pair<Flower, Triple[]> 
	lim(	CommaCat<Node, Path, Node, Path, Node, Path> b,
			Map<Triple<Node, Node, Arr<Node, Path>>, String> map,
			Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, String> map2) throws FQLException {
		
		
		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
		Map<String, String> from = new HashMap<>();
		Map<String, Pair<String, String>> select = new HashMap<>();
		
		int m = b.objects.size();
	//String[] cnames = new String[m];
		
		//String[] cnames2 = new String[b.arrows.size() - m];
		int temp = 0;
		Triple[] cnames = new Triple[m];
		
		
		for (Triple<Node, Node, Arr<Node, Path>> n : b.objects) {
			from.put("t" + temp, map.get(n));
			//cnames[temp] = n.second.string;
			cnames[temp] = n;
			
			select.put("c" + temp, new Pair<>("t" + temp, "c0"));
			temp++;
		}
		
//		Set<String> cnames_set = new HashSet<>();
//		System.out.println("***");
//		for (String s : cnames) {
//			System.out.println(s);
//			cnames_set.add(s);
//		}
//		System.out.println("***");
//		
//		if (cnames_set.size() != cnames.length) {
//			throw new RuntimeException();
//		}
		
//		temp = 0;
		for (Arr<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>> e : b.arrows) {
			if (b.isId(e)) {
				continue;
			}
			from.put("t" + temp, map2.get(e.arr));
			where.add(new Pair<>(new Pair<>("t" + temp, "c0"), new Pair<>("t" + cnamelkp(cnames, e.src), "c0")));
			where.add(new Pair<>(new Pair<>("t" + temp, "c1"), new Pair<>("t" + cnamelkp(cnames, e.dst), "c1")));
			temp++;
		}

		
		Flower f = new Flower(select, from, where) ;
	//	System.out.println("flower is " + f);
		
		return new Pair<>(f, cnames);
		
		
/*		
		return null;
		// System.out.println("Taking limit for " + B);
		Flower x0 = null;
		int m = b.objects.size();
		String[] cnames = new String[m];
		int temp = 0;

//		if (m == 0) {
//			x0 = new SingletonRA();
//		} else {
			for (Triple<Node, Node, Arr<Node, Path>> n : b.objects) {
				if (x0 == null) {
					x0 = map.get(n);
				} else {
					x0 = new Product(x0, map.get(n));
				}
				cnames[temp] = n.second.string;
				temp++;
			}
			x0 = new Project(x0, makeCols(temp));
		//}

			//product all objects
			
//		 System.out.println("Testing initial part ");
//		 System.out.println("Query is " + x0);
//		 printNicely(eval(x0, test0()));
//		 System.out.println("end test");

		int[] cols = new int[m];
		for (int i = 0; i < m; i++) {
			cols[i] = i;
		}

		for (Arr<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>> e : b.arrows) {
//			 System.out.println("Doing arrow " + e);
//			 System.out.println("map is " + map);
//			 System.out.println("one " + map.get(e.arr));
//			 System.out.println("two " + map.get(e.arr.first));
//			 System.out.println("three " + map.get(e.arr.second));

			x0 = new Product(x0, map2.get(e.arr));
//			 System.out.println("Query is " + x0);
//			 printNicely(eval(x0, test0()));
//			 System.out.println("end test");
			x0 = new Select(x0, m, cnamelkp(cnames, e.src.second.string));
//			 System.out.println("Query is " + x0);
//			 printNicely(eval(x0, test0()));
//			 System.out.println("end test");
			x0 = new Select(x0, m + 1, cnamelkp(cnames, e.dst.second.string));
//			 System.out.println("Query is " + x0);
//			 printNicely(eval(x0, test0()));
//			 System.out.println("end test");
			x0 = new Project(x0, cols);
//			 System.out.println("Query is " + x0);
//			 printNicely(eval(x0, test0()));
//			 System.out.println("end test");
		}

		RA ret = new Keygen(x0);
//		 System.out.println("Result of test ");
//		 printNicely(eval(ret, test0()));
//		 System.out.println("Query was " + ret);
//		 System.out.println("end test");
		return new Pair<RA, String[]>(ret, cnames); */
	} 
	
	private static <Obj> int cnamelkp(Obj[] cnames, Obj s) throws FQLException {
		for (int i = 0; i < cnames.length; i++) {
			if (s.equals(cnames[i])) {
				return i;
			}
		}
		throw new FQLException("Cannot lookup position of " + s + " in "
				+ cnames.toString());
	}

	static int temp = 0;
	private static List<PSM> deltaX(String pre,
			Map<Triple<Node, Node, Arr<Node, Path>>, String> ob,
			Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, String> ar,
			FinFunctor<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>, Node, Path> projB
			) {
		 Map<String, String> twocol_attrs = new HashMap<>();
			
			twocol_attrs.put("c0", PSM.VARCHAR);
			twocol_attrs.put("c1", PSM.VARCHAR);
		List<PSM> ret = new LinkedList<>();
				
	//	Map<Triple<Node, Node, Arr<Node, Path>>, String> ret = new HashMap<>();
		for (Entry<Triple<Node, Node, Arr<Node, Path>>, Node> p : projB.objMapping
				.entrySet()) {
			ob.put(p.getKey(), pre + "_" + p.getKey().second.string);
		}
		for (Entry<Arr<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>>, Arr<Node, Path>> p : projB.arrowMapping.entrySet()) {
			Path x = p.getKey().arr.second.arr;
			ret.add(new CreateTable("temp" + temp, twocol_attrs));
			ret.add(new InsertSQL("temp" + temp, compose(pre,x)));
			ar.put(p.getKey().arr, "temp" + temp++); 
		}
		System.out.println("DeltaX ret " + ret);
		return ret;
	}
	
//	private static Map<Triple<Node, Node, Arr<Node, Path>>, String> deltaObj(
//			FinFunctor<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>, Node, Path> projB) {
//		Map<Triple<Node, Node, Arr<Node, Path>>, String> ret = new HashMap<>();
//		for (Entry<Triple<Node, Node, Arr<Node, Path>>, Node> p : projB.objMapping
//				.entrySet()) {
//			ret.put(p.getKey(), new Relvar(p.getKey().second.string));
//		}
//		return ret;
//	}
//
//	/** these bastardized versions of delta only works in support of pi
//	 */
//	private static Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, RA> deltaArr(
//			FinFunctor<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>, Node, Path> projB) {
//		Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, RA> ret = new HashMap<>();
//		for (Entry<Arr<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>>, Arr<Node, Path>> p : projB.arrowMapping.entrySet()) {
//			Path x = p.getKey().arr.second.arr;
//			ret.put(p.getKey().arr, compose(x)); 
//		}
//
//		return ret;
//	}

}