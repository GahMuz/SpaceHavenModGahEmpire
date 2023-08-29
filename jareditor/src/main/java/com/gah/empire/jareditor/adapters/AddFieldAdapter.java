package com.gah.empire.jareditor.adapters;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;

public class AddFieldAdapter extends Adapter {

	private String fieldName;
	private Type type;
	private int access;
	private boolean isFieldPresent;

	public AddFieldAdapter( String fieldName, Type type, int access ) {
		super();
		this.access = access;
		this.fieldName = fieldName;
		this.type = type;
	}

	@Override
	public FieldVisitor visitField( int access, String name, String desc, String signature, Object value ) {
		if ( name.equals(fieldName) ) {
			isFieldPresent = true;
		}
		return cv.visitField(access, name, desc, signature, value);
	}

	@Override
	public void visitEnd() {
		if ( !isFieldPresent ) {
			FieldVisitor fv = cv.visitField(access, fieldName, type.toString(), null, null);
			if ( fv != null ) {
				fv.visitEnd();
			}
		}
		cv.visitEnd();
	}

}