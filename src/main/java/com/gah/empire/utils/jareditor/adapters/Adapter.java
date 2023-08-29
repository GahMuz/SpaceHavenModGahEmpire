package com.gah.empire.utils.jareditor.adapters;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.gah.empire.utils.jareditor.Configuration;

public class Adapter extends ClassVisitor {

	public Adapter() {
		super(Configuration.ASM_VERSION, new ClassWriter(ClassWriter.COMPUTE_FRAMES));
	}

	public ClassWriter getWriter() {
		return (ClassWriter) this.cv;
	}
}