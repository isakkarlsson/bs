import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;

import com.bs.interpreter.stack.BsStack;
import com.bs.interpreter.stack.Stack;
import com.bs.lang.Bs;
import com.bs.lang.BsConst;
import com.bs.lang.BsObject;
import com.bs.lang.builtin.BsModule;
import com.bs.lang.builtin.BsString;
import com.bs.lang.lib.Loadable;
import com.bs.parser.StatementsParser;
import com.bs.parser.source.BsScanner;
import com.bs.parser.source.BsTokenizer;
import com.bs.parser.source.Scanner;
import com.bs.parser.source.Tokenizer;
import com.bs.parser.token.DefaultTokenFactory;
import com.bs.parser.tree.DefaultNodeFactory;
import com.bs.parser.tree.Node;
import com.bs.util.MessageHandler;
import com.bs.util.PrintStreamMessageListener;

public class bs {

	private static final String VERSION = "version";

	private static final String LOADABLE = "loadable";

	private static final String EVAL = "eval";

	private static final String HELP = "help";

	private static final String LOAD_PATH = "loadPath";

	private static final String VERSION_ID = "0.5";

	private static Option help = new Option("h", HELP, false,
			"Print this message");

	private static Option version = new Option("v", VERSION, false,
			"Show the version");

	// @formatter:off

	@SuppressWarnings("static-access")
	private static Option loadPath = OptionBuilder.withArgName(LOAD_PATH)
			.isRequired(false).hasArg()
			.withDescription("Append <loadPath> to the load path")
			.withLongOpt(LOAD_PATH).create('p');

	@SuppressWarnings("static-access")
	private static Option eval = OptionBuilder.withArgName(EVAL)
			.isRequired(false).hasArg()
			.withDescription("Evaluate code and exit").withLongOpt(EVAL)
			.create('e');

	@SuppressWarnings("static-access")
	private static Option loadable = OptionBuilder
			.withArgName(LOADABLE)
			.isRequired(false)
			.hasArg()
			.withDescription(
					"Add all instances of 'Loadable' as loadable modules")
			.withLongOpt(LOADABLE).create('l');

	// @formatter:on

	public static void main(String[] args) throws FileNotFoundException {
		Options options = new Options();
		options.addOption(help);
		options.addOption(version);
		options.addOption(loadPath);
		options.addOption(loadable);
		options.addOption(eval);

//		 args = new String[] { "Reflection.bs" };

		try {
			Bs.init();

			CommandLineParser argParser = new PosixParser();
			CommandLine line = argParser.parse(options, args);

			if (line.hasOption(HELP)) {
				help(options);
			}

			if (line.hasOption(VERSION)) {
				version();
			}

			if (line.hasOption(LOAD_PATH)) {
				loadPath(line.getOptionValue(LOAD_PATH));
			}

			if (line.hasOption(EVAL)) {
				eval(line.getOptionValue(EVAL));
			}

			if (line.hasOption(LOADABLE)) {
				loadable(line.getOptionValue(LOADABLE));
			}

			List<?> rest = line.getArgList();

			if (rest.size() > 0) {
				eval(rest);
			} else {
				repl();
			}

		} catch (ParseException e) {
			help(options);
		}

	}

	private static void version() {
		System.out.println("bs (bullshit lang) " + VERSION_ID);
		System.out.println("Copyright (C) 2012+ Isak Karlsson");
		System.out.println("License BSD");
		System.exit(1);
	}

	private static void loadable(String filename) {
		File file = new File(filename);

		JarInputStream is;
		try {
			ClassLoader loader = URLClassLoader.newInstance(new URL[] { file
					.toURI().toURL() });
			is = new JarInputStream(new FileInputStream(file));
			JarEntry entry;
			while ((entry = is.getNextJarEntry()) != null) {
				if (entry.getName().endsWith(".class")
						&& !entry.getName().contains("/")) {
					Class<?> cls = Class.forName(
							FilenameUtils.removeExtension(entry.getName()),
							false, loader);
					for (Class<?> i : cls.getInterfaces()) {
						if (i.equals(Loadable.class)) {
							Loadable l = (Loadable) cls.newInstance();
							Bs.addModule(l);
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	/**
	 * 
	 */
	protected static void repl() {
		java.util.Scanner scanner = new java.util.Scanner(System.in);
		BsObject module = BsModule.create("<stdin>");
		Stack stack = BsStack.getDefault();
		stack.push(module);
		while (true) {
			String code = read(scanner, ">> ");
			BsObject obj = Bs.evalRepl(code, stack);
			if (obj == null) {
				continue;
			}
			if (obj.isError()) {
				Bs.breakError(obj, false);
			} else {
				System.out.println(obj);
			}
		}
	}

	/**
	 * @param rest
	 * @throws FileNotFoundException
	 */
	protected static void eval(List<?> rest) throws FileNotFoundException {
		MessageHandler handler = new MessageHandler();
		handler.add(new PrintStreamMessageListener(System.out));

		String file = (String) rest.get(0);
		BsObject module = BsModule.create(file);
		Stack stack = BsStack.getDefault();
		stack.push(module);

		Scanner sc = new BsScanner(new FileReader(new File(file)));
		Tokenizer tz = new BsTokenizer(sc, new DefaultTokenFactory(), handler,
				'#');
		StatementsParser parser = new StatementsParser(tz,
				new DefaultNodeFactory(), handler);

		Node code = parser.parse();

		if (handler.errors() == 0) {
			BsObject value = Bs.eval(code, BsStack.getDefault());
			if (value == null) {
				System.out.println("Wtf!?");
			}
			if (value.isError()) {
				Bs.breakError(value);
			}
		}
	}

	/**
	 * @param code
	 */
	protected static void eval(String code) {
		BsObject obj = Bs.eval(code);
		if (obj.isError()) {
			Bs.breakError(obj);
		}

		System.exit(1);
	}

	/**
	 * @param path
	 */
	protected static void loadPath(String path) {
		List<BsObject> loadPath = BsConst.Module.getSlot(BsModule.LOAD_PATH)
				.value();

		loadPath.add(BsString.clone(path));
	}

	/**
	 * @param options
	 */
	protected static void help(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("bs", options);
		System.exit(0);
	}

	private static String read(java.util.Scanner scanner, String promt) {
		System.out.print(promt);
		return scanner.nextLine();
	}
}
