package com.yanan.utils.beans.xml;

import org.dom4j.Node;

import java.lang.reflect.Field;

/**
 * xml转化提供这，用于扩展xml转化能力
 * @author Administrator
 */

public interface XmlMappingParser {
	/**
	 * 处理node的具体逻辑
	 * @param node
	 * @param field
	 * @return
	 */
	Object parse(Node node, Field field);
}
