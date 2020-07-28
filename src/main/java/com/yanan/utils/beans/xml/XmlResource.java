package com.yanan.utils.beans.xml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将当前节点作为xml原型
 * @author Administrator
 *
 */
@Target({ElementType.FIELD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface XmlResource {
	/**
	 * 默认值
	 * @return xml资源
	 */
	String value() default "";
}
