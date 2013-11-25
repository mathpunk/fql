package fql.examples;

public class FunctorExample extends Example {

	@Override
	public String getName() {
		return "Functor";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "schema One = {"
			+ "\n	nodes a;"
			+ "\n	attributes attx:a->string,atty:a->string,attz:a->string;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema D = {"
			+ "\n	nodes x,y,z;"
			+ "\n	attributes attx:x->string,atty:y->string,attz:z->string;"
			+ "\n	arrows f:x->z, g:y->z;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nmapping F = {"
			+ "\n	nodes x->a, y->a, z->a;"
			+ "\n	attributes attx->attx, atty->atty, attz->attz;"
			+ "\n	arrows f->a, g->a;"
			+ "\n} : D -> One "
			+ "\n"
			+ "\ninstance I = {"
			+ "\n	nodes x->{1,2,3},y->{4,5,6,7},z->{ev,od};"
			+ "\n	attributes attx->{(1,1),(2,2),(3,3)},atty->{(4,4),(5,5),(6,6),(7,7)},attz->{(ev,even),(od,odd)};"
			+ "\n	arrows f->{(1,od),(2,ev),(3,od)},g->{(4,ev),(5,od),(6,ev),(7,od)};"
			+ "\n} :  D"
			+ "\n"
			+ "\ninstance J = {"
			+ "\n	nodes x->{1,2,3},y->{4,5,6,7},z->{ev,od};"
			+ "\n	attributes attx->{(1,1),(2,2),(3,3)},atty->{(4,4),(5,5),(6,6),(7,7)},attz->{(ev,even),(od,odd)};"
			+ "\n	arrows f->{(1,od),(2,ev),(3,od)},g->{(4,ev),(5,od),(6,ev),(7,od)};"
			+ "\n} :  D"
			+ "\n"
			+ "\ntransform h = {"
			+ "\n	nodes x->{(1,1),(2,2),(3,3)},y->{(4,4),(5,5),(6,6),(7,7)},z->{(ev,ev),(od,od)};"
			+ "\n} : I -> J"
			+ "\n"
			+ "\ninstance piFI = pi F I"
			+ "\ninstance piFJ = pi F J"
			+ "\n"
			+ "\n//transform t0 = pi piFI piFJ h"
			+ "\n"
			+ "\n"
			+ "\nmapping G = {"
			+ "\n	nodes a->x;"
			+ "\n	attributes attx->attx, atty->attx, attz->attx;"
			+ "\n	arrows ;"
			+ "\n} : One -> D "
			+ "\n"
			+ "\ntransform t = {"
			+ "\n	nodes x->{(1,1),(2,2),(3,3)},y->{(4,4),(5,5),(6,6),(7,7)},z->{(ev,ev),(od,od)};"
			+ "\n} : I -> J"
			+ "\n"
			+ "\ninstance deltaGI = delta G I"
			+ "\ninstance deltaGJ = delta G J"
			+ "\n"
			+ "\ntransform t1 = delta deltaGI deltaGJ t"
			+ "\n"
			+ "\nschema OneX = {"
			+ "\n	nodes a;"
			+ "\n	attributes atta:a->string;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema Two = {"
			+ "\n	nodes a, b;"
			+ "\n	attributes atta : a -> string, attb : b -> string;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nmapping H = {"
			+ "\n	 nodes a -> a;"
			+ "\n	 attributes atta -> atta;"
			+ "\n	 arrows;"
			+ "\n} : OneX -> Two"
			+ "\n"
			+ "\ninstance K = {"
			+ "\n	nodes a -> {1,2};"
			+ "\n	attributes atta -> {(1,1),(2,1)};"
			+ "\n	arrows;"
			+ "\n} : OneX"
			+ "\n "
			+ "\ninstance sigmaFI = sigma H K"
			+ "\ninstance sigmaFJ = sigma H L"
;


}