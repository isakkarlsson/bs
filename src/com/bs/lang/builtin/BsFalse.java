package com.bs.lang.builtin;

import com.bs.lang.BsAbstractProto;
import com.bs.lang.BsConst;

public class BsFalse extends BsAbstractProto {

	public BsFalse() {
		super(BsConst.Bool, "False", BsFalse.class);
	}
}
