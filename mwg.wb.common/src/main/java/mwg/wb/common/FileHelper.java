package mwg.wb.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import mwg.wb.common.Logs;
import mwg.wb.common.TypeConvertHelper;

public class FileHelper {
	public static void WriteAllText(String path, String content) {
		try {
			Files.write(Paths.get(path), content.getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}

	}

	public static boolean Exists(String path) {

		File tempFile = new File(path);
		return tempFile.exists();

	}

	public static List<String> ReadAllLines(String path) {
		try {
			List<String> allLines = Files.readAllLines(Paths.get(path));
			for (String line : allLines) {
				// System.out.println(line);
			}
			return allLines;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String ReadAllText(String path) throws IOException {

		String content = "";

		try {
			content = new String(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;

	}

	public static void CreateDirectory(String tempdir) {
		Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxr--r--");
		FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(permissions);
		Path path = Paths.get(tempdir);
		try {
			Files.createDirectory(path, fileAttributes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String GetDirectoryName(String localPackFile) {
		// TODO Auto-generated method stub
		return FilenameUtils.getFullPath(localPackFile);
	}

	public static void Delete(String tempPath) {
//		File aa = new File(tempPath);
//		aa.delete();
		Path fileToDeletePath = Paths.get(tempPath);
		try {
			Files.delete(fileToDeletePath);
		} catch (IOException e) {
			System.out.println(tempPath);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void Copy(String tempPath, String localPackFile, boolean b) {
		// Files.copy(tempPath, localPackFile,true);
		File tempPath1 = new File(tempPath);
		File localPackFile1 = new File(localPackFile);
		try {
			FileUtils.copyFile(tempPath1, localPackFile1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean DirectoryExists(String directoryName) {

		return true;
	}

	public static boolean DirectoryExistsAndCreate(String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists()) {
			directory.mkdirs();

		}
		return true;
	}

	 																																																					// +
																																																						// dir.getName());
																																																						// return
																																																						// dir.delete();
																																																						// }

	public static List<String> DirectoryGetFiles(String path) {
		List<String> results = new ArrayList<String>();
		File[] files = new java.io.File(path).listFiles();
		for (File file : files) {
			if (file.isFile()) {

				results.add(file.getName());
			}
		}
		return results;
	}

	public static String GetFileNameWithoutExtension(String fileName) {
		String fileNameAll = FilenameUtils.getName(fileName);
		return FilenameUtils.removeExtension(fileNameAll);

	}

	public static boolean DeleteDirectory(File dir) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (int i = 0; i < children.length; i++) {
				boolean success = DeleteDirectory(children[i]);
				if (!success) {
					return false;
				}
			}
		}
		// either file or an empty directory
		System.out.println("removing file or directory : " + dir.getName());
		return dir.delete();
	}
	public static double  getFileSizeMegaBytes(String filename) {
		File file = new File(filename);
		if (!file.exists() || !file.isFile()) return 0;
 
		return getFileSizeMegaBytes(file) ;
	}

	private static double  getFileSizeMegaBytes(File file) {
		return (double) file.length() / (1024 * 1024) ;
	}
	public static void AppendAllText(String path, String content) {
//		 File file = new File(path);
//		 if(!file.exists()) {
//			 try {
//				file.createNewFile();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			 
//		 }
		
		// FileWriter fw = new FileWriter(file.getAbsoluteFile());
		try {
			Files.write(Paths.get(path), (content + "\r\n").getBytes(), StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
			// exception handling left as an exercise for the reader
		}
	}
}
