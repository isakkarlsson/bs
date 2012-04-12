package com.bs.lang;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BsObject {

	public static BsObject value(BsObject proto, Object value) {
		BsObject obj = new BsObject(proto);
		obj.value(value);
		return obj;
	}

	private static long ID = 0;

	private long id;
	private BsObject prototype;

	private String name;
	private Object value;

	private Map<String, BsMessage> messages = new HashMap<String, BsMessage>();
	private Map<String, BsObject> instVars = new HashMap<String, BsObject>();

	private Class<?> klass;

	public BsObject(BsObject prototype, String name) {
		this(prototype, name, BsObject.class);
	}

	public BsObject(BsObject prototype) {
		this(prototype, null);
	}

	protected BsObject(BsObject prototype, String name, Class<?> me) {
		this.name = name;
		this.prototype = prototype;
		this.klass = me;
		id = ID++;
	}

	public long id() {
		return id;
	}

	public String name() {
		return name;
	}

	public void value(Object value) {
		this.value = value;
	}

	public Object safeValue() {
		return value;
	}

	@SuppressWarnings("unchecked")
	public <T> T value() {
		return (T) safeValue();
	}

	public BsMessage message(String name) {
		BsMessage msg = messages.get(name);
		if (msg != null) {
			return msg;
		}
		if ((msg = javaMethod(name)) != null) {
			return msg;
		}

		if (prototype != null) {
			return prototype.message(name);
		} else {
			return null;
		}
	}

	private BsMessage javaMethod(String name) {
		Method[] methods = klass.getMethods();
		for (Method m : methods) {
			BsRuntimeMessage brm = m.getAnnotation(BsRuntimeMessage.class);
			if (brm != null && brm.name().equals(name)) {
				return new BsMessage(brm.name(), brm.arity(), new BsJavaProxy(
						this, m));
			}
		}
		return null;
	}

	public BsObject invoke(String message, BsObject... args) {
		BsMessage msg = message(message);
		if (msg != null) {
			return msg.invoke(this, args);
		} else {
			throw BsError.NO_SUCH_METHOD;
		}
	}

	public BsObject var(String key) {
		return instVars.get(key);
	}

	public void var(String key, BsObject value) {
		instVars.put(key, value);
	}

	public void var(BsObject value) {
		if (value.name() != null)
			instVars.put(value.name(), value);
	}

	@Override
	public String toString() {
		BsObject str = invoke("toString");
		if (str.value() != null) {
			return (String) str.value();
		}
		return name + "@" + id;
	}
}
