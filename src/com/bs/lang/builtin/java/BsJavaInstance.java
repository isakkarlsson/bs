package com.bs.lang.builtin.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.MethodUtils;

import com.bs.lang.Bs;
import com.bs.lang.BsAbstractProto;
import com.bs.lang.BsConst;
import com.bs.lang.BsObject;
import com.bs.lang.annot.BsRuntimeMessage;
import com.bs.lang.builtin.BsError;
import com.bs.lang.builtin.BsList;
import com.bs.lang.builtin.BsString;
import com.bs.lang.builtin.BsSymbol;

public class BsJavaInstance extends BsAbstractProto {

	public BsJavaInstance() {
		super(null, "JavaInstance", BsJavaInstance.class);
	}

	@BsRuntimeMessage(name = "=>?", arity = 0)
	public BsObject methods(BsObject self, BsObject... args) {
		List<BsObject> methods = new ArrayList<BsObject>();
		BsJavaData data = self.value();
		for (Method m : data.cls.getMethods()) {
			methods.add(BsSymbol.get(m.getName()));
		}

		return BsList.create(methods);
	}

	@BsRuntimeMessage(name = "=>", arity = 1, types = { BsSymbol.class })
	public BsObject call(BsObject self, BsObject... args) {
		String name = Bs.asString(args[0]);
		BsJavaData data = self.value();
		Object[] arguments = ReflectionUtils.createJavaObjects(args, 1);
		return call(self, name, data, arguments);
	}

	@BsRuntimeMessage(name = Bs.METHOD_MISSING, arity = -1)
	public BsObject methodMissing(BsObject self, BsObject... args) {
		String name = Bs.asString(args[0]);

		BsJavaData data = self.value();
		Object[] arguments = ReflectionUtils.createJavaObjects(args, 1);

		return call(self, name, data, arguments);
	}

	/**
	 * @param self
	 * @param name
	 * @param data
	 * @param arguments
	 * @return
	 */
	protected BsObject call(BsObject self, String name, BsJavaData data,
			Object[] arguments) {
		try {
			Object ret = MethodUtils.invokeMethod(data.instance, name,
					arguments);

			/*
			 * Enable method chaining
			 */
			if (ret == null) {
				return self;
			}
			return ReflectionUtils.createBsObject(ret);
		} catch (Exception e) {
			return BsError.javaError(e.getMessage());
		}
	}

	@BsRuntimeMessage(name = "->", arity = 1)
	public BsObject getSlot(BsObject self, BsObject... args) {

		try {
			BsJavaData data = self.value();
			String fieldName = Bs.asString(args[0]);

			Field field = data.cls.getField(fieldName);
			BsObject ret = ReflectionUtils.createBsObject(field
					.get(data.instance));

			return ret;
		} catch (Exception e) {
			return BsError.javaError("field '%s' does not exist or is hidden",
					Bs.asString(args[0]));
		}
	}

	@BsRuntimeMessage(name = "<-", arity = 2)
	public BsObject setSlot(BsObject self, BsObject... args) {
		if (!args[0].instanceOf(BsConst.Symbol)) {
			return BsError.typeError("setSlot", args[0], BsConst.Symbol);
		}
		String name = Bs.asString(args[0]);
		BsJavaData data = self.value();
		try {
			Field field = data.cls.getField(name);
			field.set(data.instance, ReflectionUtils.getValue(args[1].value()));

		} catch (Exception e) {
			return BsError.javaError("field '%s' does not exist or is hidden",
					Bs.asString(args[0]));
		}

		return self;
	}

	@BsRuntimeMessage(name = "toString", arity = 0)
	public BsObject toString(BsObject self, BsObject... args) {
		BsJavaData data = self.value();
		return BsString.clone(data.instance.toString());
	}

}
