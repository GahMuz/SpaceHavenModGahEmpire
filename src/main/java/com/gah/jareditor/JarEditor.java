package com.gah.jareditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import com.gah.jareditor.adapters.Adapter;

public class JarEditor {

	private String jarPath;
	private Map<String, Adapter> modifications;

	private List<String> processed = new ArrayList<>();
	private List<String> modified = new ArrayList<>();
	private List<JarEntry> failed = new ArrayList<>();

	public JarEditor( String jarPath, Map<String, Adapter> modifications ) {
		super();
		this.jarPath = jarPath;
		this.modifications = modifications;
	}

	private String getBackupPath() {
		return jarPath + ".backup";
	}

	private String backup() throws IOException {
		String backupPath = getBackupPath();
		File backupFile = new File(backupPath);
		if ( !backupFile.exists() ) {
			Files.copy(Paths.get(jarPath), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		return backupPath;
	}

	private void copy( JarOutputStream jarOutputStream, JarFile jarFile, JarEntry entry ) throws IOException {
		System.out.println("copy " + entry.getName());
		jarOutputStream.putNextEntry(new JarEntry(entry.getName()));
		try ( InputStream input = jarFile.getInputStream(entry) ) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ( ( bytesRead = input.read(buffer) ) != -1 ) {
				jarOutputStream.write(buffer, 0, bytesRead);
			}
		}
		jarOutputStream.closeEntry();
	}

	private void modify( JarOutputStream jarOutputStream, JarFile jarFile, JarEntry entry ) throws IOException {
		System.out.println("modify " + entry.getName());
		ClassModifier modifier = new ClassModifier(jarFile, jarOutputStream, entry, modifications.get(entry.getName()));
		boolean result = modifier.modify();
		if ( result )
			modified.add(entry.getName());
		else
			failed.add(entry);
	}

	private boolean checkIfAlreadyProcessed( JarEntry entry ) {
		if ( processed.contains(entry.getName()) ) {
			System.out.println("multiple " + entry.getName());
			return true;
		} else {
			processed.add(entry.getName());
			return false;
		}
	}

	private void displayFailed() {
		if ( failed.size() > 0 ) {
			System.out.println();
			System.out.println("Failed");
			for ( JarEntry entry : failed )
				System.out.println("- " + entry.getName());
			System.out.println();
		}
	}

	private void displayModification() {
		System.out.println();
		System.out.println("Modification done");
		for ( String mod : modified )
			System.out.println("- " + mod);
		System.out.println();
	}

	public void edit() throws FileNotFoundException, IOException {
		String backupPath = backup();

		JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(jarPath));
		try ( JarFile jarFile = new JarFile(backupPath) ) {
			Enumeration<JarEntry> entries = jarFile.entries();
			while ( entries.hasMoreElements() ) {
				JarEntry entry = entries.nextElement();

				if ( checkIfAlreadyProcessed(entry) ) {
					continue;
				}

				if ( modifications.containsKey(entry.getName()) ) {
					modify(jarOutputStream, jarFile, entry);
				} else {
					copy(jarOutputStream, jarFile, entry);
				}
			}

			displayFailed();
			displayModification();
			jarOutputStream.close();
		}
	}
}