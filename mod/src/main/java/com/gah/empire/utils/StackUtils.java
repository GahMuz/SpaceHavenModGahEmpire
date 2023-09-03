package com.gah.empire.utils;

public class StackUtils {

	public static void throwing() {
		try {
			throw new Exception();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
}
