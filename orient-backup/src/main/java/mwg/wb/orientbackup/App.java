package mwg.wb.orientbackup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import mwg.wb.client.graph.ORThreadLocal;

/**
 * Hello world!
 *
 */
public class App {

	private static BufferedReader reader;

	public static void main(String[] args) throws Throwable {
		reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Mode (0 = dump, 1 = diff): ");
		int mode = Integer.parseInt(reader.readLine());
		switch (mode) {
		case 0:
			backupOrient();
			break;
		case 1:
			diffOrient();
			break;
		default:
			System.out.println("Invalid mode.");
			break;
		}
		System.out.println("Done.");
		System.in.read();
	}

	public static void diffOrient() throws Throwable {
		System.out.print("From datacenter: ");
		int from = Integer.parseInt(reader.readLine());
		ORThreadLocal ofrom = new ORThreadLocal();
		var cfrom = ConfigUtils.GetOnlineClientConfig(from);
		ofrom.initReadAPI(cfrom, 0);
		System.out.println("From: " + ofrom.getURL());
		var fromFuncs = Arrays.stream(ofrom.QueryFunction("functions", OFunction[].class, false))
				.collect(Collectors.toMap(f -> f.name, f -> f));
		System.out.println("Loaded " + fromFuncs.size() + " functions");
		System.out.print("To datacenter: ");
		int to = Integer.parseInt(reader.readLine());
		ORThreadLocal oto = new ORThreadLocal();
		var cto = ConfigUtils.GetOnlineClientConfig(to);
		oto.initReadAPI(cto, 0);
		System.out.println("To: " + oto.getURL());
		var toFuncs = Arrays.stream(oto.QueryFunction("functions", OFunction[].class, false))
				.collect(Collectors.toMap(f -> f.name, f -> f));
		System.out.println("Loaded " + toFuncs.size() + " functions");
		System.out.println("Comparing...");
		var diff = fromFuncs.values().stream()
				.filter(f -> toFuncs.get(f.name) == null || !toFuncs.get(f.name).isIdentical(f))
				.collect(Collectors.toList());
		System.out.println(diff.size() + " functions diff");
		File dir = new File("diff");
		if (dir.exists() && dir.isDirectory()) {
			for (var f : dir.listFiles())
				f.delete();
		} else
			dir.mkdir();
		System.out.println("Path: " + dir.getPath());
		for (var func : diff) {
			System.out.println(func.name);
			try {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(dir.getPath() + "\\" + func.name + ".txt"), "UTF-8"));
				if (func.parameters != null) {
					writer.write("Params: " + StringUtils.join(func.parameters, ", "));
					writer.newLine();
				}
				writer.write("Language: " + func.language);
				writer.newLine();
				writer.write("##########################");
				writer.newLine();
				writer.write(func.code);
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void backupOrient() throws Throwable {
		System.out.print("From datacenter: ");
		int dc = Integer.parseInt(reader.readLine());
		var oclient = new ORThreadLocal();
		var config = ConfigUtils.GetOnlineClientConfig(dc);
		oclient.initReadAPI(config, 0);
		System.out.println("From: " + oclient.getURL());
		var funcs = oclient.QueryFunction("functions", OFunction[].class, false);
		File dir = new File("functions");
		if (dir.exists() && dir.isDirectory()) {
			for (var f : dir.listFiles())
				f.delete();
		} else
			dir.mkdir();
		System.out.println("Path: " + dir.getPath());

		for (var func : funcs) {
			System.out.println(func.name);
			try {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(dir.getPath() + "\\" + func.name + ".txt"), "UTF-8"));
				if (func.parameters != null) {
					writer.write("Params: " + StringUtils.join(func.parameters, ", "));
					writer.newLine();
				}
				writer.write("Language: " + func.language);
				writer.newLine();
				writer.write("##########################");
				writer.newLine();
				writer.write(func.code);
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
