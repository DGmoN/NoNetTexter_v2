package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

public class IO {

	private PrintWriter Writer;
	private BufferedReader Reader;

	public IO(InputStream a, OutputStream b) {
		Writer = new PrintWriter(b);
		Reader = new BufferedReader(new InputStreamReader(a));
	}

}
