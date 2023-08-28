package com.gah.empire.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class ModifyJarAccess {
	public static void main( String[] args ) throws Exception {
		String outputPath = "C:/Program Files (x86)/Steam/steamapps/common/SpaceHaven/spacehaven.jar";
		String jarPath = backup(outputPath);

		// @formatter:off
		List<String> toPublic = Arrays.asList(
				"fi/bugbyte/spacehaven/gui/MenuSystemItems$SectorSelected.class", 
				"fi/bugbyte/spacehaven/gui/MenuSystemItems$SectorSelected$SectorInfo.class",
				"fi/bugbyte/spacehaven/gui/StarMapScreen.class",
				"fi/bugbyte/spacehaven/gui/StarMapScreen$ScrollTarget.class"
		);
		// @formatter:on

		// Create a new JAR file for the modified classes
		JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputPath));

		List<String> names = new ArrayList<>();
		List<String> modified = new ArrayList<>();
		List<JarEntry> failed = new ArrayList<>();

		// Open the original JAR for reading
		try ( JarFile jarFile = new JarFile(jarPath) ) {
			Enumeration<JarEntry> entries = jarFile.entries();
			while ( entries.hasMoreElements() ) {
				JarEntry entry = entries.nextElement();

				if ( names.contains(entry.getName()) ) {
					System.out.println("multiple " + entry.getName());
					continue;
				} else {
					names.add(entry.getName());
				}

				if ( toPublic.contains(entry.getName()) ) {
					System.out.println("treating " + entry.getName());
					boolean result = treat(jarFile, jarOutputStream, entry);
					if ( result )
						modified.add(entry.getName());
					else
						failed.add(entry);
				} else {
					System.out.println("skiping " + entry.getName());

					// Copy non-class files as they are
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
			}

			if ( failed.size() > 0 ) {
				System.out.println();
				System.out.println("Failed");
				for ( JarEntry entry : failed )
					System.out.println("- " + entry.getName());
				System.out.println();
			}

			System.out.println();
			System.out.println("Modification done");
			for ( String mod : modified )
				System.out.println("- " + mod);
			System.out.println();

			jarOutputStream.close();
		}
	}

	private static boolean treat( JarFile jarFile, JarOutputStream jarOutputStream, JarEntry entry ) {
		try {
			// Read the original class file
			InputStream input = jarFile.getInputStream(entry);
			ClassReader classReader = new ClassReader(input);

			// Create a ClassWriter for the modified class
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM6, classWriter) {
				@Override
				public void visit( int version, int access, String name, String signature, String superName, String[] interfaces ) {
					// Change the access level to public
					super.visit(version, Opcodes.ACC_PUBLIC, name, signature, superName, interfaces);
				}

				@Override
				public void visitInnerClass( final String name, final String outerName, final String innerName, final int access ) {
					super.visitInnerClass(name, outerName, innerName, Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC);
				}
			};

			// Apply the modifications
			classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);

			// Write the modified class to the new JAR
			jarOutputStream.putNextEntry(new JarEntry(entry.getName()));
			jarOutputStream.write(classWriter.toByteArray());
			jarOutputStream.closeEntry();
			return true;
		} catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
	}

	private static String backup( String jarPath ) throws IOException {
		String backup = jarPath + ".backup";
		File backupFile = new File(backup);
		if ( !backupFile.exists() ) {
			Files.copy(Paths.get(jarPath), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		return backup;
	}
}