package com.gah.empire.utils.jareditor.adapters;

import org.objectweb.asm.Opcodes;

public class PublicClassAdapter extends Adapter {

	public PublicClassAdapter() {
		super();
	}

	@Override
	public void visit( int version, int access, String name, String signature, String superName, String[] interfaces ) {
		// Change the access level to public
		super.visit(version, Opcodes.ACC_PUBLIC, name, signature, superName, interfaces);
	}

	@Override
	public void visitInnerClass( final String name, final String outerName, final String innerName, final int access ) {
		super.visitInnerClass(name, outerName, innerName, Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC);
	}

}