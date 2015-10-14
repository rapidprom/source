package org.rapidprom.util;

public class ObjectUtils {

	public static <T extends Object> String[] toString(T[] t) {
		String[] str = new String[t.length];
		for (int i = 0; i < t.length; i++) {
			str[i] = t[i].toString();
		}
		return str;
	}

}
