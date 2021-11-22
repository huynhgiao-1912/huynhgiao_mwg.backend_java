package com.mwg.tool.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileHelper {
	public static List<String> readAllLines(Path target) throws IOException{
		List<String> lines = Files.readAllLines(target);

		return lines;
	}
}
