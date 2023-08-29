package com.gah.jareditor;

import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.objectweb.asm.ClassReader;

import com.gah.jareditor.adapters.Adapter;

public class ClassModifier {

	private JarFile jarFile;
	private JarOutputStream jarOutputStream;
	private JarEntry entry;
	private Adapter adapter;

	public ClassModifier( JarFile jarFile, JarOutputStream jarOutputStream, JarEntry entry, Adapter adapter ) {
		super();
		this.jarFile = jarFile;
		this.jarOutputStream = jarOutputStream;
		this.entry = entry;
		this.adapter = adapter;
	}

	public boolean modify() {
		try {
			// Read the original class file
			InputStream input = jarFile.getInputStream(entry);
			ClassReader classReader = new ClassReader(input);

			// Apply the adaptors
			classReader.accept(adapter, ClassReader.SKIP_FRAMES);

			// Write the modified class to the new JAR
			jarOutputStream.putNextEntry(new JarEntry(entry.getName()));
			jarOutputStream.write(adapter.getWriter().toByteArray());
			jarOutputStream.closeEntry();
			return true;
		} catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
	}
}