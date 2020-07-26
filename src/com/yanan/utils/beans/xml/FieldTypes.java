package com.yanan.utils.beans.xml;

/**
 * <p>扫描类Field时使用的方式</p>
 * <p>DEFAULTED:使用ClassHelper.getFields(){@link com.yanan.utils.reflect.cache.ClassHelper}</p>
 * <p>DECLARED:使用ClassHelper.getDeclaredFields()</p>
 * <p>ALL:使用ClassHelper.getAllFields()</p>
 * @author yanan
 */
public enum FieldTypes {
	DEFAULTED,DECLARED,ALL
}
