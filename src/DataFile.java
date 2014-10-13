import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

public class DataFile {

	private static File MySide;
	private static File OtherSide;

	private static BufferedReader Read;
	private static PrintWriter Write;
	private static InputStream AlienStream;

	public static void INIT(InputStream a) {
		MySide = new File("MySide");
		OtherSide = new File("OtherSide");
		AlienStream = a;
		try {
			MySide.createNewFile();
			OtherSide.createNewFile();
		} catch (IOException e) {

		}

	}

	public static void Write(String a) {
		try {
			Write = new PrintWriter(MySide);
			Write.write(a);
			Write.close();
		} catch (FileNotFoundException e) {
		}
	}

	public static void WriteAlien() {

	}

	public static String read() {
		String temp = null;
		try {
			Read = new BufferedReader(new InputStreamReader(AlienStream));
			temp = Read.readLine();
			Read.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return temp;
	}
}
