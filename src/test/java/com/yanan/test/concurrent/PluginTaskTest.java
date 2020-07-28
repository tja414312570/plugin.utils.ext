package com.yanan.test.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.yanan.frame.plugin.annotations.Service;
import com.yanan.frame.plugin.builder.resolver.ParameterResolver;
import com.yanan.test.junit.extension.PluginTestContextLoader;

@ExtendWith(PluginTestContextLoader.class)
public class PluginTaskTest {
	@Service(attribute="array")
	private ParameterResolver<?> resolver;
	@Test
	public void test() {
		System.out.println(resolver);
		System.out.println("a"+this);
		assertEquals("a", "A");
	}
	@Test
	public void test2() {
		System.out.println(resolver);
		System.out.println("b"+this);
		assertEquals("A", "A");
	}
}
