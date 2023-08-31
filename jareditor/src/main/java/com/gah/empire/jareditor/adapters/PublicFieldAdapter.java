package com.gah.empire.jareditor.adapters;

import org.objectweb.asm.FieldVisitor;

public class PublicFieldAdapter extends Adapter {

	private String fieldName;
	private int access;

	public PublicFieldAdapter( String fieldName, int access ) {
		super();
		this.access = access;
		this.fieldName = fieldName;
	}

	@Override
	public FieldVisitor visitField( int access, String name, String desc, String signature, Object value ) {
		if ( name.equals(fieldName) ) {
			return cv.visitField(this.access, name, desc, signature, value);
		}
		return cv.visitField(access, name, desc, signature, value);
	}

}