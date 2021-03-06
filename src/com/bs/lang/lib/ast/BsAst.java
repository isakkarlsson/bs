package com.bs.lang.lib.ast;

import java.io.StringReader;

import com.bs.interpreter.BsCompiler;
import com.bs.lang.Bs;
import com.bs.lang.BsAbstractProto;
import com.bs.lang.BsConst;
import com.bs.lang.BsObject;
import com.bs.lang.annot.BsProto;
import com.bs.lang.annot.BsRuntimeMessage;
import com.bs.lang.builtin.BsError;
import com.bs.lang.builtin.BsString;
import com.bs.lang.builtin.java.BsJavaData;
import com.bs.lang.builtin.java.BsJavaInstance;
import com.bs.lang.builtin.java.ReflectionUtils;
import com.bs.parser.tree.Node;

@BsProto(name = "Ast")
public class BsAst extends BsAbstractProto {

	public BsAst() {
		super(BsConst.Proto, "Ast", BsAst.class);
	}

	@BsRuntimeMessage(name = "compile", arity = 1, types = { BsString.class })
	public BsObject compile(BsObject self, BsObject... args) {
		BsCompiler compiler = new BsCompiler();
		Node node = compiler.parse(new StringReader(Bs.asString(args[0])));
		return ReflectionUtils.createBsObject(node);
	}

	@BsRuntimeMessage(name = "execute", arity = 1, types = { BsJavaInstance.class })
	public BsObject execute(BsObject self, BsObject... args) {
		BsJavaData node = args[0].value();
		if (node == null || !(node.instance instanceof Node)) {
			return BsError.typeError("Excpected a 'Node'");
		}

		return Bs.eval((Node) node.instance);
	}
}
