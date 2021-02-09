package com.yanan.utils.beans.xml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 当Field类型为List、Map及其实现类时有效，用于一个集合对应多种实体。
 * 此时XmlHelper的扫描方式为主动扫描
 * {@link com.yanan.utils.beans.xml.Mapping}
 * @author yanan
 *
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingGroup{
	/**
	 * 映射集合
	 * @return 映射集合
	 */
	Mapping[] value();
	/**
	 * 支持的注解类型
	 * @return
	 */
	Class<?> support() default Object.class;
}
