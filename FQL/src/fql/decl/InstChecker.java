package fql.decl;

import fql.FQLException;
import fql.Pair;
import fql.decl.InstExp.Const;
import fql.decl.InstExp.Delta;
import fql.decl.InstExp.Eval;
import fql.decl.InstExp.Exp;
import fql.decl.InstExp.External;
import fql.decl.InstExp.FullEval;
import fql.decl.InstExp.FullSigma;
import fql.decl.InstExp.InstExpVisitor;
import fql.decl.InstExp.One;
import fql.decl.InstExp.Pi;
import fql.decl.InstExp.Plus;
import fql.decl.InstExp.Relationalize;
import fql.decl.InstExp.Sigma;
import fql.decl.InstExp.Times;
import fql.decl.InstExp.Two;
import fql.decl.InstExp.Zero;

public class InstChecker implements InstExpVisitor<SigExp, FQLProgram> {

	@Override
	public SigExp visit(FQLProgram env, Zero e) {
		return e.sig.typeOf(env);
	}

	@Override
	public SigExp visit(FQLProgram env, One e) {
		return e.sig.typeOf(env);
	}

	@Override
	public SigExp visit(FQLProgram env, Two e) {
		return e.sig.typeOf(env);
	}
	
	private SigExp visit2(FQLProgram env, String a, String b) {
		if (!env.insts.containsKey(a)) {
			throw new RuntimeException("Missing instance: " + a);
		}
		if (!env.insts.containsKey(b)) {
			throw new RuntimeException("Missing instance: " + a);
		}
		SigExp lt = env.insts.get(a).accept(env, this);
		SigExp rt = env.insts.get(b).accept(env, this);
		if (!lt.equals(rt)) {
			throw new RuntimeException("Not of same type: " + lt
					+ " and " + rt);
		}
		return lt;	
	}

	@Override
	public SigExp visit(FQLProgram env, Plus e) {
		return visit2(env, e.a, e.b);
	}

	@Override
	public SigExp visit(FQLProgram env, Times e) {
		return visit2(env, e.a, e.b);
	}

	@Override
	public SigExp visit(FQLProgram env, Exp e) {
		return visit2(env, e.a, e.b);
	}

	/*
	 * @Override public SigExp visit( Quad<Map<String, SigExp>, Map<String,
	 * MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env, Var e) { if
	 * (seen.contains(e.v)) { throw new RuntimeException("Cyclic definition: " +
	 * e.v); } seen.add(e.v); InstExp i = env.third.get(e.v); if (i == null) {
	 * throw new RuntimeException("Unknown instance " + e); } return
	 * i.accept(env, this); }
	 */

	@Override
	public SigExp visit(FQLProgram env, Const e) {
		SigExp k = e.sig.typeOf(env);
		try {
			new Instance(k.toSig(env), e.data);
			return k;
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}

	@Override
	public SigExp visit(FQLProgram env, Delta e) {
		InstExp xxx = env.insts.get(e.I);
		if (xxx == null) {
			throw new RuntimeException("Missing " + e.I);
		}
		SigExp it = xxx.accept(env, this);
		Pair<SigExp, SigExp> ft = e.F.type(env);
		if (!ft.second.equals(it)) {
			throw new RuntimeException("In " + e + " expected instance to be "
					+ ft.second + " but is " + it);
		}
		return ft.first;
	}

	@Override
	// TODO check disc op fib
	public SigExp visit(FQLProgram env, Sigma e) {
		InstExp xxx = env.insts.get(e.I);
		if (xxx == null) {
			throw new RuntimeException("Missing " + e.I);
		}
		SigExp it = xxx.accept(env, this);
		Pair<SigExp, SigExp> ft = e.F.type(env);
		if (!ft.first.equals(it)) {
			throw new RuntimeException("In " + e + " expected instance to be "
					+ ft.first + " but is " + it);
		}
		return ft.second;
	}

	@Override
	// TODO check bijection
	public SigExp visit(FQLProgram env, Pi e) {
		InstExp xxx = env.insts.get(e.I);
		if (xxx == null) {
			throw new RuntimeException("Missing " + e.I);
		}
		SigExp it = xxx.accept(env, this);
		Pair<SigExp, SigExp> ft = e.F.type(env);
		if (!ft.first.equals(it)) {
			throw new RuntimeException("In " + e + " expected instance to be "
					+ ft.first + " but is " + it);
		}
		return ft.second;
	}

	@Override
	public SigExp visit(FQLProgram env, FullSigma e) {
		InstExp xxx = env.insts.get(e.I);
		if (xxx == null) {
			throw new RuntimeException("Missing " + e.I);
		}
		SigExp it = xxx.accept(env, this);
		Pair<SigExp, SigExp> ft = e.F.type(env);
		if (!ft.first.equals(it)) {
			throw new RuntimeException("In " + e + " expected instance to be "
					+ ft.first + " but is " + it);
		}
		return ft.second;
	}

	@Override
	public SigExp visit(FQLProgram env, Relationalize e) {
		InstExp xxx = env.insts.get(e.I);
		if (xxx == null) {
			throw new RuntimeException("Missing " + e.I);
		}
		return xxx.accept(env, this);
	}

	@Override
	public SigExp visit(FQLProgram env, External e) {
		return e.sig.typeOf(env);
	}

	@Override
	public SigExp visit(FQLProgram env, Eval e) {
		Pair<SigExp, SigExp> k = e.q.type(env);
		if (null == env.insts.get(e.e)) {
			throw new RuntimeException("Unknown: " + e.e);
		}
		SigExp v = env.insts.get(e.e).accept(env, this);
		if (!(k.first.equals(v))) {
			throw new RuntimeException("On " + e + ", expected input to be "
					+ k.first + " but computed " + v);
		}
		return k.second;
	}

	@Override
	public SigExp visit(FQLProgram env, FullEval e) {
		Pair<SigExp, SigExp> k = e.q.type(env);
		if (null == env.insts.get(e.e)) {
			throw new RuntimeException("Unknown: " + e.e);
		}
		SigExp v = env.insts.get(e.e).accept(env, this);
		if (!(k.first.equals(v))) {
			throw new RuntimeException("On " + e + ", expected input to be "
					+ k.first + " but computed " + v);
		}
		return k.second;
	}

}
