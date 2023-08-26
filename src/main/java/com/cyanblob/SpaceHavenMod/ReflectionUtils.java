package com.cyanblob.SpaceHavenMod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.aspectj.lang.JoinPoint;

public class ReflectionUtils {

	public static < T > T getThis( JoinPoint jp ) {
		return (T) jp.getThis();
	}

	public static < T, F > F getDeclaredField( T _this, String fieldname )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field privateUriField = _this.getClass().getDeclaredField(fieldname);
		privateUriField.setAccessible(true);
		return (F) privateUriField.get(_this);
	}

	public static < T, F > F getField( T _this, String fieldname )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field privateUriField = _this.getClass().getField(fieldname);
		privateUriField.setAccessible(true);
		return (F) privateUriField.get(_this);
	}

	public static < T, V > V setDeclaredField( T _this, String fieldname, V value )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field privateUriField = _this.getClass().getDeclaredField(fieldname);
		privateUriField.setAccessible(true);
		privateUriField.set(_this, value);
		return value;
	}

	/*
	public static < T > Method getDeclaredMethod( T _this, String methodname, Class<?>... parameterTypes ) throws NoSuchMethodException {
		Method privateUriMethod = _this.getClass().getDeclaredMethod(methodname, parameterTypes);
		privateUriMethod.setAccessible(true);
		return privateUriMethod;
	}
	*/

	public static < T, R > R getDeclaredMethod( T _this, String methodname, List<Class<?>> parameterTypes, List<?> parameters )
			throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method privateUriMethod = _this.getClass().getDeclaredMethod(methodname, parameterTypes.toArray(new Class<?>[0]));
		privateUriMethod.setAccessible(true);
		return (R) privateUriMethod.invoke(_this, parameters.toArray(new Object[0]));
	}

	public static < T, R > R getSuperMethod( T _this, String methodname, List<Class<?>> parameterTypes, List<?> parameters )
			throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method privateUriMethod = _this.getClass().getSuperclass().getDeclaredMethod(methodname, parameterTypes.toArray(new Class<?>[0]));
		privateUriMethod.setAccessible(true);
		return (R) privateUriMethod.invoke(_this, parameters.toArray(new Object[0]));
	}

	public static < T > T getPrivateClass( Class<T> clazz, List<Class<?>> parameterTypes, List<?> parameters ) throws InstantiationException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes.toArray(new Class<?>[0]));
		constructor.setAccessible(true);
		return constructor.newInstance(parameters.toArray(new Object[0]));
	}

	public static < T > Class<?> getParentClass( T _this, int depth )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = _this.getClass();
		for ( int i = 0; i < depth; i++ )
			clazz = clazz.getSuperclass();
		return clazz;
	}

	public static < T, F > F getDeclaredField( T _this, int depth, String fieldname )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = getParentClass(_this, depth);
		Field privateUriField = clazz.getDeclaredField(fieldname);
		privateUriField.setAccessible(true);
		return (F) privateUriField.get(_this);
	}
}
