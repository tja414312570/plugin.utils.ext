package com.yanan.utils.beans.xml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 为List、Map及其子类实现类提供多标签映射,是MappingGroup的数组的实现类。
 * {@link com.yanan.utils.beans.xml.MappingGroup}
 * @author yanan
 *
 */
@Target({ElementType.FIELD})
@Repeatable(MappingGroup.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {
	/**
	 * 节点名称
	 * @return 节点名称
	 */
	String node();
	/**
	 * 对应实现类
	 * @return 目标实现类
	 */
	Class<?> target() default Object.class;
}
