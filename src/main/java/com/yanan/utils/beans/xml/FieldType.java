package com.yanan.utils.beans.xml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>扫描类Field时的方式</p>
 * <p>不引用此注解时，默认使用FieldTypes.DECLARED {@link com.yanan.utils.beans.xml.FieldTypes}</p>
 * <p>引用此注解时，默认使用FieldTypes.DEFAULTED</p>
 * @author Administrator
 */
@Target({ElementType.FIELD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldType {
	/**
	 * 默认使用FieldTypes.DEFAULTED
	 * @return 属性类型
	 */
	FieldTypes value() default FieldTypes.DEFAULTED;
}
