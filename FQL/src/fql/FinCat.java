package fql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ryan
 *
 * Implementation of finite categories.
 * 
 * @param <Obj> type of objects
 * @param <Arrow> type of arrows
 */
public class FinCat<Obj, Arrow>  {

	List<Obj> objects = new LinkedList<>();
	List<Arr<Obj,Arrow>> arrows = new LinkedList<>();
	Map<Pair<Arr<Obj,Arrow>, Arr<Obj,Arrow>>, Arr<Obj,Arrow>> composition = new HashMap<>();
	Map<Obj, Arr<Obj,Arrow>> identities = new HashMap<>();

	/**
	 * Empty Category
	 */
	public FinCat() { }
	
	/**
	 * Singleton category
	 * @param o the object
	 * @param a the identity arrow
	 */
	public FinCat(Obj o, Arr<Obj,Arrow> a) {
		objects.add(o);
		arrows.add(a);
		composition.put(new Pair<>(a,a), a);
		identities.put(o, a);
	}
	
	/**
	 * Creates a new category, does not copy inputs.
	 */
	public FinCat(List<Obj> objects, List<Arr<Obj,Arrow>> arrows, Map<Pair<Arr<Obj,Arrow>, Arr<Obj,Arrow>>, Arr<Obj,Arrow>> composition,
			Map<Obj, Arr<Obj,Arrow>> identities) {
		noDupes(objects);
		noDupes(arrows);
		this.objects = objects;
		this.arrows = arrows;
		this.composition = composition;
		this.identities = identities;
		if (DEBUG.VALIDATE) {
			validate();
		}
	}

	private <X> void noDupes(List<X> X) {
		Set<X> x = new HashSet<X>(X);
		if (x.size() != X.size()) {
			throw new RuntimeException("duplicates " + X);
		}
	}

	public void validate() {
		if (arrows.size() < objects.size()) {
			throw new RuntimeException("Missing arrows: " + this);
		}
		for (Arr<Obj,Arrow> a : arrows) {
			if (a.src == null) {
				throw new RuntimeException(a + " has no source " + this);
			}
			if (a.dst == null) {
				throw new RuntimeException(a + " has no dst " + this);
			}
		}
		for (Obj o : objects) {
			Arr<Obj,Arrow> i = identities.get(o); 
			if (i == null) {
				throw new RuntimeException(o + " has no arrow " + this);
			}
			for (Arr<Obj,Arrow> a : arrows) {
				if (a.src.equals(o)) {
					if (!a.equals(compose(i, a))) {
						throw new RuntimeException("Identity compose error1 " + i + o + a);
					}
				}
				if (a.dst.equals(o)) {
					if (!a.equals(compose(a, i))) {
						throw new RuntimeException("Identity compose error2 " + i + o + a);
					}
				}
			}
		}
		
		
		for (Arr<Obj,Arrow> a : arrows) {
			for (Arr<Obj,Arrow> b : arrows) {
				if (a.dst.equals(b.src)) {
					Arr<Obj,Arrow> c = compose(a, b);
					if (!arrows.contains(c)) {
						throw new RuntimeException("Not closed under composition " + a + b + c + this);
					}
					if (!a.src.equals(c.src)) {
						throw new RuntimeException("Composition type error1 " + a + b + c + this);
					}
					if (!b.dst.equals(c.dst)) {
						throw new RuntimeException("Composition type error2 " + a + b + c + this);
					}
					for (Arr<Obj,Arrow> cc : arrows) {
						if (cc.src.equals(b.dst)) {
							if (!compose(a, compose(b, cc)).equals(compose(compose(a,b), cc))) {
								throw new RuntimeException("Not associative " + a + b + cc);
							}
						}
					}
				}
			}
		}
		
	}

	public Arr<Obj,Arrow> id(Obj o) {
		return identities.get(o);
	}
	
	public Set<Arr<Obj,Arrow>> hom(Obj A, Obj B) {
		Set<Arr<Obj,Arrow>> ret = new HashSet<>();
		for (Arr<Obj,Arrow> a : arrows) {
			if (a.src.equals(A) && a.dst.equals(B)) {
				ret.add(a);
			}
		}
		return ret;
	}

	public Arr<Obj,Arrow> compose(Arr<Obj,Arrow> a, Arr<Obj,Arrow> b) {
		return composition.get(new Pair<>(a, b));
	}

	public boolean isId(Arr<Obj,Arrow> a) {
		return identities.containsValue(a);
	}
	
	/**
	 * Converts a category to a signature.  
	 * @param n the "name" of the signature
	 * @return a signature and isomorphism
	 * @throws FQLException
	 */
	public Triple<Signature, Pair<Map<Obj, String>, Map<String, Obj>>, Pair<Map<Arr<Obj, Arrow>, String>, Map<String, Arr<Obj, Arrow>>>> 
	toSig(String n) throws FQLException {
		
		int i = 0;
		Map<String, Obj> objM = new HashMap<>();
		Map<Obj, String> objM2 = new HashMap<>();
		for (Obj o : objects) {
			objM2.put(o, "obj" + i);
			objM.put("obj" + i, o);
			i++;
		}
		
		int j = 0;
		Map<String, Arr<Obj,Arrow>> arrM = new HashMap<>();
		Map<Arr<Obj,Arrow>, String> arrM2 = new HashMap<>();
		for (Arr<Obj,Arrow> a : arrows) {
			arrM.put("arrow" + j, a);
			arrM2.put(a, "arrow" + j);	
			j++;
		}
		
		//System.out.println(objM);
		//System.out.println(arrM);
		
		List<Triple<String, String, String>> arrows = new LinkedList<>();
		for (Arr<Obj,Arrow> a : this.arrows) {
	//		System.out.println("arrow a is " + a);
			if (isId(a)) {
				continue;
			}
			arrows.add(new Triple<>(arrM2.get(a), objM2.get(a.src), objM2.get(a.dst)));
		}
		
		for (Obj o : isolated()) {
//			System.out.println("isolated " + o);
			arrows.add(new Triple<String, String, String>(objM2.get(o), null, null));
		}
		
	//	System.out.println("arrows are " + arrows);
		
		Signature ret2 = new Signature(n, arrows, new LinkedList<Pair<List<String>, List<String>>>());
		
//		System.out.println("$$$$$$$$$$$$$$$$$$$$$");
//		System.out.println(this);
//		System.out.println(ret2);
//		System.out.println(objM);
//		System.out.println(objM2);
//		System.out.println(arrM);
//		System.out.println(arrM2);
//		System.out.println("$$$$$$$$$$$$$$$$$$$$$");
		Triple<Signature, Pair<Map<Obj, String>, Map<String, Obj>>, Pair<Map<Arr<Obj,Arrow>, String>, Map<String, Arr<Obj,Arrow>>>> retret 
		= new Triple<>(ret2, new Pair<>(objM2, objM), new Pair<>(arrM2, arrM));
		return retret;
	}

	private Set<Obj> isolated() {
		Set<Obj> ret = new HashSet<>(objects);
		
		for (Arr<Obj,Arrow> a : arrows) {
			if (isId(a)) {
				continue;
			}
			ret.remove(a.src);
			ret.remove(a.dst);
		}
		
		return ret;
	}
	


	@Override
	public String toString() {
		return "FinCat [objects=" + objects + "\n\narrows=" + arrows + "\n\ncomposition=" + composition
				+ "\n\nidentities=" + identities + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arrows == null) ? 0 : arrows.hashCode());
		result = prime * result
				+ ((composition == null) ? 0 : composition.hashCode());
		result = prime * result
				+ ((identities == null) ? 0 : identities.hashCode());
		result = prime * result + ((objects == null) ? 0 : objects.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		FinCat other = (FinCat) obj;
		if (arrows == null) {
			if (other.arrows != null)
				return false;
		} else if (!arrows.equals(other.arrows))
			return false;
		if (composition == null) {
			if (other.composition != null)
				return false;
		} else if (!composition.equals(other.composition))
			return false;
		if (identities == null) {
			if (other.identities != null)
				return false;
		} else if (!identities.equals(other.identities))
			return false;
		if (objects == null) {
			if (other.objects != null)
				return false;
		} else if (!objects.equals(other.objects))
			return false;
		return true;
	}

	
	
	}