package com.yanan.utils;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import com.yanan.utils.reflect.ReflectUtils;

public class ThreadLocalUtils {
	public static Class<?>[] expungeStaleEntryParameterType = { int.class };

	/**
	 * 移除线程池
	 * 
	 * @param threadLocalMap 线程表
	 * @throws IllegalArgumentException  ex
	 * @throws IllegalAccessException    ex
	 * @throws NoSuchFieldException      ex
	 * @throws SecurityException         ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException     ex
	 */
	public static void remove(Object threadLocalMap) throws IllegalArgumentException, IllegalAccessException,
			NoSuchFieldException, SecurityException, InvocationTargetException, NoSuchMethodException {
		// Entry[] tab = threadLocalMap.table;
		Object tab = ReflectUtils.getDeclaredFieldValue("table", threadLocalMap);
		// int len = tab.length
		int len = Array.getLength(tab);
		for (int i = 0; i < len; i++) {
			// Entry e = tab[i];
			Object e = Array.get(tab, i);
			// if(e != null && e.get() != null);
			if (e != null && ReflectUtils.invokeMethod(e, "get") != null) {
				// e.clear();
				ReflectUtils.invokeMethod(e, "clear");
				// threadLocalMap.expungeStaleEntry(i);
				ReflectUtils.invokeDeclaredMethod(threadLocalMap, "expungeStaleEntry", expungeStaleEntryParameterType,
						i);
			}
		}
	}

	/**
	 * 移除当前线程的局部变量
	 */
	public static void removeThreadLocalMap() {
		Thread currentThread = Thread.currentThread();

	}

	public static void removeThreadLocalMap(Thread thread) {
		try {
			System.out.println(thread);
			Object inheritableThreadLocals = ReflectUtils.getDeclaredFieldValue("inheritableThreadLocals", thread);
			System.out.println(inheritableThreadLocals);
			if (inheritableThreadLocals != null) {
				remove(inheritableThreadLocals);
			}
			Object threadLocals = ReflectUtils.getDeclaredFieldValue("threadLocals", thread);
			System.out.println(threadLocals);
			if (threadLocals != null) {
				remove(threadLocals);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
