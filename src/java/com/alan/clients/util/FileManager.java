package com.alan.clients.util;

import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
	public static File dir;
	public static File userDir;
	private static final File ALT;
	private static final File LASTALT;

	static {
		FileManager.dir = new File(Minecraft.getMinecraft().mcDataDir, "Alan");
		ALT = getConfigFile("Alts");
		LASTALT = getConfigFile("LastAlt");
	}

	public FileManager() {
		super();
	}


	public static File getConfigFile(final String name) {
		final File file = new File(FileManager.dir, String.format("%s.txt", name));
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException ignored) {
			}
		}
		return file;
	}

	public static void init() {
		if (!FileManager.dir.exists()) {
			FileManager.dir.mkdir();
		}
	}


	public static List<String> read(final String file) {
		final List<String> out = new ArrayList<>();
		try {
			if (!FileManager.dir.exists()) {
				FileManager.dir.mkdir();
			}
			final File f = new File(FileManager.dir, file);
			if (!f.exists()) {
				f.createNewFile();
			}

			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String line;

			while ((line = br.readLine()) != null) {
				out.add(line);
			}

			fis.close();
			return out;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out;
	}
	public static List<String> read(File dir,final String file) {
		final List<String> out = new ArrayList<>();
		try {
			if (!dir.exists()) {
				return null;
			}
			final File f = new File(FileManager.dir, file);
			if (!f.exists()) {
				f.createNewFile();
			}

			final FileInputStream fis = new FileInputStream(f);
			final InputStreamReader isr = new InputStreamReader(fis);
			final BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				out.add(line);
			}
			br.close();
			isr.close();
			isr.close();
			fis.close();
			return out;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out;
	}
	public static void save(final String file, final String content, final boolean append) {
		try {
			final File f = new File(FileManager.dir, file);
			if (!f.exists()) {
				f.createNewFile();
			}
			final FileWriter writer = new FileWriter(f, append);
			writer.write(content);

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void save(File dir, final String file, final String content, final boolean append) {
		try {
			final File f = new File(dir, file);
			if (!f.exists()) {
				f.createNewFile();
			}
			final FileWriter writer = new FileWriter(f, append);
			writer.write(content);

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<String> readUser(final String file) {
		final List<String> out = new ArrayList<>();
		try {
			if (!FileManager.userDir.exists()) {
				FileManager.userDir.mkdir();
			}
			final File f = new File(FileManager.userDir, file);
			if (!f.exists()) {
				f.createNewFile();
			}

			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String line;

			while ((line = br.readLine()) != null) {
				out.add(line);
			}

			fis.close();
			return out;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out;
	}
	public static void saveUser(final String file, final String content, final boolean append) {
		try {
			final File f = new File(FileManager.userDir, file);
			if (!f.exists()) {
				f.createNewFile();
			}
			final FileWriter writer = new FileWriter(f, append);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
