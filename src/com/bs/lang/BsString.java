package com.bs.lang;

public class BsString extends BsObject {

	public static BsObject clone(String str) {
		return BsObject.value(Bs.String, str);
	}

	public BsString() {
		super(Bs.Proto, "String", BsString.class);
	}

	@BsRuntimeMessage(name = "length", arity = 0)
	public BsObject length(BsObject self, BsObject... args) {
		return BsObject.value(Bs.Number, self.value().toString().length());
	}

	@BsRuntimeMessage(name = "toString", arity = 0)
	public BsObject toString(BsObject self, BsObject... args) {
		return self;
	}

	@BsRuntimeMessage(name = "clone", arity = 1)
	public BsObject clone(BsObject self, BsObject... args) {
		return BsObject.value(self, args[0].toString());
	}
}
