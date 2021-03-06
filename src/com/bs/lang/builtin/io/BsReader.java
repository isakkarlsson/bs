package com.bs.lang.builtin.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import com.bs.lang.Bs;
import com.bs.lang.BsConst;
import com.bs.lang.BsObject;
import com.bs.lang.annot.BsRuntimeMessage;
import com.bs.lang.builtin.BsError;
import com.bs.lang.builtin.BsNumber;
import com.bs.lang.builtin.BsString;

public class BsReader extends BsObject {

	public static BsObject create(Reader reader) {
		BsObject obj = new BsObject(BsConst.Reader);
		obj.value(new BufferedReader(reader));
		return obj;
	}

	public BsReader() {
		super(BsConst.IO, "Reader", BsReader.class);
		initRuntimeMethods();
	}

	@BsRuntimeMessage(name = "mark", arity = 1)
	public BsObject mark(BsObject self, BsObject... args) {
		if (!args[0].instanceOf(BsConst.Number)) {
			return BsError.typeError("mark", args[0], BsConst.Number);
		}

		BufferedReader reader = self.value();
		try {
			reader.mark(Bs.asNumber(args[0]).intValue());
		} catch (IOException e) {
			return BsError.IOError(e.getMessage());
		}

		return self;
	}

	@BsRuntimeMessage(name = "read", arity = 0)
	public BsObject read(BsObject self, BsObject... args) {
		BufferedReader reader = self.value();
		try {
			int ret = reader.read();
			return BsNumber.clone(ret);
		} catch (IOException e) {
			return BsError.IOError(e.getMessage());
		}
	}

	@BsRuntimeMessage(name = "readLine", arity = 0)
	public BsObject readLine(BsObject self, BsObject... args) {
		BufferedReader reader = self.value();
		try {
			String ret = reader.readLine();
			return BsString.clone(ret);
		} catch (IOException e) {
			return BsError.IOError(e.getMessage());
		}
	}

	@BsRuntimeMessage(name = "init", arity = 1)
	public BsObject init(BsObject self, BsObject... args) {
		if (!args[0].instanceOf(BsConst.File)) {
			return BsError.typeError("init", args[0], BsConst.File);
		}

		File file = args[0].value();
		try {
			self.value(new BufferedReader(new FileReader(file)));
		} catch (FileNotFoundException e) {
			return BsError.IOError(e.getMessage());
		}

		return self;
	}
}
