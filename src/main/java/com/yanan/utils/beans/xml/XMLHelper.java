package com.yanan.utils.beans.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.frame.plugin.PlugsFactory;
import com.yanan.utils.reflect.AppClassLoader;
import com.yanan.utils.reflect.cache.ClassHelper;
import com.yanan.utils.reflect.cache.ClassInfoCache;
import com.yanan.utils.reflect.cache.FieldHelper;
import com.yanan.utils.resource.AbstractResourceEntry;
import com.yanan.utils.resource.ResourceManager;

/**
 * xml解析工具，反向加载，通过java映射寻址xml，主要通过注解方式驱动，所有对象通过PlugsHandler代理
 * <p>Encode注解，用于提供读取xml的字符集 Attribute 可用于Field，标示该字段是一个节点的属性，用于基础类型 tip:基础类型</p>
 * <p>java八中基础类型与String类型以及对应的数组类型 Element 用于集合或其他pojo类型，如List，Map，或自定义类型，可以用于类声明</p>
 * <p>Value 用于Field，标示该字段是节点的文本值 AsXml 用于Field,标示该字段为该节点作为xml Ignore 不处理该标签 Type</p>
 * <p>用于指定聚合类的实现类 20180919</p>
 * <p>支持Encode注解，Attribute注解，Element注解，Value注解，AsXml注解，Ignore注解,Type注解</p>
 * <p>支持List，简单POJO以及基本数据类型</p>
 * <p>20181010 新增MappingGroup的支持，重构代码结构</p>
 * <p>20181011 FieldTypes{@link com.yanan.utils.beans.xml.FieldType}新增All类型，支持类所有Field的获取</p>
 * 
 * @author yanan
 *
 */
public class XMLHelper {
	private AbstractResourceEntry xmlResource;
	private InputStream inputStream;
	private Class<?> mapping;
	// 字符集
	private String charset = "UTF-8";
	// 命名映射
	private Map<String, String> nameMapping = new HashMap<String, String>();
	// 日志
	Logger log = LoggerFactory.getLogger(XMLHelper.class);
	// 用于存储结果集
	private List<Object> beanObjectList = new ArrayList<Object>();
	// remove mapping
	private List<String> removeNodes = new ArrayList<String>();
	// 节点名
	private String nodeName;
	// 扫描层次
	private int scanLevel = -1;
	// 集合映射
	private Map<String, Class<?>> mapMapping = new HashMap<String, Class<?>>();
	// 映射类的ClassHelper
	private ClassHelper classHelper;
	/**
	 * @param files xml文件
	 * @param wrappClass 需要转化的Class
	 */
	public XMLHelper(File files, Class<?> wrappClass) {
		this.setFile(files);
		this.setMapping(wrappClass);
	}

	public XMLHelper() {
	}

	public XMLHelper(InputStream inputStream, Class<?> wrappClass) {
		this.setInputStream(inputStream);
		this.setMapping(wrappClass);
	}
	public XMLHelper(AbstractResourceEntry resource, Class<?> wrappClass) {
		this.setInputStream(resource.getInputStream());
		this.xmlResource = resource;
		this.setMapping(wrappClass);
	}
	public String getCharset() {
		return charset;
	}

	/**
	 * 设置读取xml的编码
	 * 
	 * @param charset 字符编码
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getNodeName() {
		return nodeName;
	}

	/**
	 * 添加命名映射，已弃用
	 * 
	 * @param name 名称
	 * @param field 属性
	 */
	@Deprecated
	public void addNameMaping(String name, String field) {
		this.nameMapping.put(name, field);

	}
	/**
	 * 添加名称映射
	 * @param name 节点名
	 * @param cls 类
	 */
	public void addMapMapping(String name, Class<?> cls) {
		this.mapMapping.put(name, cls);
	}

	/**
	 * 设置根节点路径
	 * 
	 * @param nodeName 节点路径
	 */
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	/**
	 * 设置映射类
	 * 
	 * @param mappingClass 映射类
	 */
	public void setMapping(Class<?> mappingClass) {
		this.mapping = mappingClass;
		classHelper = ClassInfoCache.getClassHelper(this.mapping);
		Resource resource = classHelper.getAnnotation(Resource.class);
		if (resource != null) {
			AbstractResourceEntry file = ResourceManager.getResource(resource.value());
			this.inputStream = file.getInputStream();
		}
		com.yanan.utils.beans.xml.Element element = classHelper
				.getAnnotation(com.yanan.utils.beans.xml.Element.class);
		if (element != null)
			this.nodeName = element.name();
		Encode encode = classHelper.getAnnotation(Encode.class);
		if (encode != null)
			this.charset = encode.value();
	}

	/**
	 * 设置文件
	 * 
	 * @param file 文件
	 */
	public void setFile(File file) {
		if (file == null || !file.exists())
			throw new RuntimeException("file \"" + file + "\"is not exists");
		if (!file.canRead())
			throw new RuntimeException("file \"" + file + "\" can not be read");
		try {
			this.xmlResource = new AbstractResourceEntry(file);
			System.out.println(file.isAbsolute()+"  "+this.xmlResource);
			this.inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(),e);
		}
	}

	/**
	 * 读取xml文件，并将xml文件转化为目标聚合对像输出
	 * 
	 * @param <T> 实例类型
	 * @return 实例
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> read() {
		if (this.inputStream == null)
			throw new RuntimeException(
					"the xml inputStream is null,check to see if you forgot to add the input stream！");
		if (this.mapping == null)
			throw new RuntimeException("could not find mapping class");
		SAXReader reader = new SAXReader();
		Document document;
		try {
			document = reader.read(inputStream, charset);
			if (this.nodeName == null)
				throw new RuntimeException("node name is null");
			List<?> pNode = document.selectNodes(nodeName);
			if (pNode == null)
				throw new RuntimeException("root node is null at node name" + nodeName);
			if (pNode.size() == 0)
				throw new RuntimeException("XML Path has not any content at : " + nodeName);
			// 遍历节点
			rootElement(pNode);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return (List<T>) this.beanObjectList;
	}

	/**
	 * 根节点的处理
	 * 
	 * @param nodeElement 节点元素
	 */
	public void rootElement(List<?> nodeElement) {
		Iterator<?> eIterator = nodeElement.iterator();
		while (eIterator.hasNext()) {
			Object obj = PlugsFactory.getPluginsInstanceNew(this.mapping);
			AppClassLoader loader = new AppClassLoader(obj);
			Node node = (Node) eIterator.next();
			Field[] fileds = this.getFields(classHelper,null);
			for (Field field : fileds) {
				// 交给Field处理
				try {
					Object object = this.processField(node, field, classHelper, 0);
					if (object != null)
						loader.set(field, object);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					log.error(e.getMessage(),e);
				}
			}
			this.beanObjectList.add(obj);
		}
	}
	/**
	 * 获取类的Field
	 * @param classHelper class helper
	 * @return 属性结合
	 */
	private Field[] getFields(ClassHelper classHelper,FieldHelper helper) {
		FieldType types = null;
		if(helper!=null)
			types =  helper.getAnnotation(FieldType.class);
		if(types==null)
			types = classHelper.getAnnotation(FieldType.class);
		if(types==null)
			types = this.classHelper.getAnnotation(FieldType.class);
		if(types == null||types.value() == FieldTypes.DECLARED)
			return classHelper.getDeclaredFields();
		if(types.value() == FieldTypes.DEFAULTED)
			return classHelper.getFields();
		if(types.value() ==  FieldTypes.ALL)
			return classHelper.getAllFields();
		return classHelper.getDeclaredFields();
	}

	/**
	 * Field的处理
	 * 
	 * @param node 当前节点
	 * @param field java映射的Field
	 * @param classHelper 需要一个ClassHelper来获取反射信息
	 * @param level 当前扫描层次
	 * @return 对象 
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	public Object processField(Node node, Field field, ClassHelper classHelper, int level)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		if (this.scanLevel > -1 && level++ > scanLevel)
			return null;
		Class<?> fieldType = field.getType();
		Object object = null;
		FieldHelper helper = classHelper.getFieldHelper(field);
		if (helper.getAnnotation(Ignore.class) != null)
			return null;
		if (helper.getAnnotation(XmlResource.class) != null)
			return fieldType.equals(String.class) ? this.xmlResource == null ?null:this.xmlResource.getPath() : this.xmlResource;
		if (helper.getAnnotation(NodeName.class) != null)
			return node.getName();
		if (helper.getAnnotation(NodePath.class) != null)
			return node.getPath();
		if (helper.getAnnotation(NodeUniquePath.class) != null)
			return node.getUniquePath();
		// get the node info from Element annotation
		com.yanan.utils.beans.xml.Element element = helper
				.getAnnotation(com.yanan.utils.beans.xml.Element.class);
		String nodeName = this.getNodeName(field, element);
		if (AppClassLoader.isBaseType(fieldType)) {
			// if the field is base java data array or String array , need another method to proccess
			if (fieldType.isArray()) {
				// get the array's origin type
				Class<?> arrayType = fieldType.getComponentType();
				// get the node from document
				List<?> nodes = node.selectNodes(nodeName);
				// call nodes array method to process the field
				object = getNodesValues(nodes, arrayType);
			} else {
				com.yanan.utils.beans.xml.Attribute attribute = helper
						.getAnnotation(com.yanan.utils.beans.xml.Attribute.class);
				if (attribute != null) {
					if (!attribute.name().trim().equals(""))
						nodeName = attribute.name();
					if(node!=null)
						object = ((Element) node).attributeValue(nodeName);
					// if (object == null)
					// throw new RuntimeException(
					// "node attribute is null;\r\nat node " + node.getPath() +
					// "\r\nat nodeName " + nodeName);
				} else if (element != null) {
					Element signleNode = (org.dom4j.Element) node.selectSingleNode(nodeName);
					if (signleNode != null)
						object = signleNode.getTextTrim();
				} else {
					Value value = helper.getAnnotation(Value.class);
					if (value != null) {
						object = node.getText();
					} else {
						AsXml as = helper.getAnnotation(AsXml.class);
						if (as != null) {
							object = node.asXML();
						} else {
							object = ((Element) node).attributeValue(nodeName);
							if (object == null) {
								Element signleNode = (org.dom4j.Element) node.selectSingleNode(nodeName);
								if (signleNode != null)
									object = signleNode.getTextTrim();
							}
						}
					}
				}
			}
		} else {
			// rename node name
			if (AppClassLoader.implementsOf(fieldType, List.class)) {
				//process List node
				//if the node is multiple mapping node ,use reverse scan document node 
				MappingGroup groups = helper.getAnnotation(MappingGroup.class);
				if (groups == null)
					object = this.buildListNode(helper, field, node, level, nodeName);
				else
					object = this.buildGroupListNode(helper, field, node, level, groups);
			} else if (AppClassLoader.implementsOf(fieldType, Map.class)) {
				//process Map node
				MappingGroup groups = helper.getAnnotation(MappingGroup.class);
				if (groups == null)
					object = this.buildMapNode(helper, field, node, level, nodeName);
				else
					object = this.buildGroupMapNode(helper, field, node, level, groups);
			} else if (fieldType.isArray()) {
				//process pojo array
				object = this.buildPojoArrayNode(helper, field, node, level, element, fieldType, nodeName);
			} else {
				//process simple pojo
				object = this.buildPojoNode(helper, field, node, level, fieldType, nodeName);
			}
		}
		return object;
	}

	/**
	 * 构建带有MappingGroup的节点
	 * 
	 * @param helper field helper
	 * @param field field
	 * @param node node
	 * @param level level
	 * @param mappGroup mapping
	 * @return 对象
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object buildGroupListNode(FieldHelper helper, Field field, Node node, int level, MappingGroup mappGroup)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		// get type annotation
		com.yanan.utils.beans.xml.Type typeAnno = helper.getAnnotation(com.yanan.utils.beans.xml.Type.class);
		Class<?> listClass;
		// get the set object
		listClass = typeAnno != null ? typeAnno.value() : ArrayList.class;
		List realList = (List) PlugsFactory.getPluginsInstanceNew(listClass);
		// get all tag
		if(node==null)
			return realList;
		Iterator<?> elementIterator = ((Element) node).elementIterator();
		while (elementIterator.hasNext()) {
			Node childNode = (Node) elementIterator.next();
			String nodeName = childNode.getName();
			if (nodeName == null)
				continue;
			for (Mapping mapping : mappGroup.value()) {
				if (mapping.node().equals(nodeName)) {
					Class<?> realClass = mapping.target();
					Object realObject = PlugsFactory.getPluginsInstanceNew(realClass);
					AppClassLoader loader = new AppClassLoader(realObject);
					ClassHelper fieldClassHelper = ClassInfoCache.getClassHelper(realClass);
					Field[] fields = this.getFields(fieldClassHelper, helper);
					Object tempObject = null;
					for (Field f : fields) {
						tempObject = processField((Node) childNode, f, fieldClassHelper, level);
						if (f != null) {
							loader.set(f, tempObject);
						}
					}
					realList.add(realObject);
				}
			}
		}
		return realList;
	}

	/**
	 * 构建具有Group的Map集合
	 * 
	 * @param helper helper
	 * @param field field
	 * @param node node
	 * @param level level
	 * @param mappGroup mappGroup
	 * @return 对象
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object buildGroupMapNode(FieldHelper helper, Field field, Node node, int level, MappingGroup mappGroup)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		com.yanan.utils.beans.xml.Type typeAnno = helper.getAnnotation(com.yanan.utils.beans.xml.Type.class);
		Class<?> mapClass = typeAnno != null ? typeAnno.value() : HashMap.class;
		Map realMap = (Map) PlugsFactory.getPluginsInstanceNew(mapClass);
		Iterator<?> elementIterator = ((Element) node).elementIterator();
		while (elementIterator.hasNext()) {
			Node childNode = (Node) elementIterator.next();
			String nodeName = childNode.getName();
			if (nodeName == null)
				continue;
			for (Mapping mapping : mappGroup.value()) {
				if (mapping.node().equals(nodeName)) {
					Class<?> realClass = mapping.target();
					Object realObject = PlugsFactory.getPluginsInstanceNew(realClass);
					AppClassLoader loader = new AppClassLoader(realObject);
					ClassHelper fieldClassHelper = ClassInfoCache.getClassHelper(realClass);
					Field[] fields = this.getFields(fieldClassHelper, helper);
					Object tempObject = null;
					for (Field f : fields) {
						tempObject = processField((Node) childNode, f, fieldClassHelper, level);
						if (f != null)
							loader.set(f, tempObject);
					}
					Field key = this.getMapKey(helper, realClass);
					realMap.put(loader.get(key), realObject);
				}
			}
		}
		return realMap;
	}

	/**
	 * Map中获取Key值
	 * 
	 * @param helper field helper
	 * @param realClass class
	 * @return 属性
	 */
	private Field getMapKey(FieldHelper helper, Class<?> realClass) {
		Field key = null;
		ClassHelper classHelper = ClassInfoCache.getClassHelper(realClass);
		// 如果Field有Key属性，从Field中获取,否则从实体类中的查找，找不到则取第一Field的值
		if (helper.getAnnotation(Key.class) != null) {
			key = classHelper.getDeclaredField(helper.getAnnotation(Key.class).value());
		} else if (classHelper.getAnnotation(Key.class) != null) {
			key = classHelper.getDeclaredField(classHelper.getAnnotation(Key.class).value());
		} else {
			key = classHelper.getDeclaredFields()[0];
		}
		return key;
	}
	/**
	 * 获取节点名称
	 * @param field 属性
	 * @param element 元素
	 * @return 节点名
	 */
	private String getNodeName(Field field, com.yanan.utils.beans.xml.Element element) {
		String nodeName = (element != null && !element.name().trim().equals("")) ? element.name() : field.getName();
		return nodeName;
	}

	/**
	 * 构建pojo对象
	 * 
	 * @param helper field helper
	 * @param field field
	 * @param node node
	 * @param level level
	 * @param fieldType field type
	 * @param nodeName node name
	 * @return object
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	private Object buildPojoNode(FieldHelper helper, Field field, Node node, int level, Class<?> fieldType,
			String nodeName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Object object = PlugsFactory.getPluginsInstanceNew(fieldType);
		AppClassLoader loader = new AppClassLoader(object);
		ClassHelper fieldClassHelper = ClassInfoCache.getClassHelper(fieldType);
		Field[] fields = this.getFields(fieldClassHelper, helper);
		Object tempObject = null;
		Node childNode = node.selectSingleNode(nodeName);
		for (Field f : fields) {
			tempObject = processField(childNode, f, fieldClassHelper, level);
			if (f != null) {
				loader.set(f, tempObject);
			}
		}
		return object;
	}

	/**
	 * 构建pojo数据
	 * 
	 * @param helper field helper
	 * @param field field
	 * @param node node
	 * @param level level 
	 * @param element element
	 * @param fieldType field type
	 * @param nodeName node name
	 * @return object
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	private Object buildPojoArrayNode(FieldHelper helper, Field field, Node node, int level,
			com.yanan.utils.beans.xml.Element element, Class<?> fieldType, String nodeName)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		// 获取数组的类型
		Class<?> arrayType = fieldType.getComponentType();
		// 获取节点的数据
		List<?> nodes = node.selectNodes(nodeName);
		// 获取数组数据
		Object tempArray = Array.newInstance(arrayType, nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			Object realObject = PlugsFactory.getPluginsInstanceNew(arrayType);
			AppClassLoader loader = new AppClassLoader(realObject);
			ClassHelper fieldClassHelper = ClassInfoCache.getClassHelper(arrayType);
			Field[] fields = this.getFields(fieldClassHelper, helper);
			Object tempObject = null;
			for (Field f : fields) {
				tempObject = processField((Node) nodes.get(i), f, fieldClassHelper, level);
				if (tempObject != null) {
					loader.set(f, tempObject);
				}
			}
			Array.set(tempArray, i, realObject);
		}
		return tempArray;
	}

	/**
	 * 构建Map节点数据
	 * 
	 * @param helper filed helper
	 * @param field field
	 * @param node node
	 * @param level level
	 * @param nodeName node name
	 * @return object
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object buildMapNode(FieldHelper helper, Field field, Node node, int level, String nodeName)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		com.yanan.utils.beans.xml.Type typeAnno = helper.getAnnotation(com.yanan.utils.beans.xml.Type.class);
		// MAP的处理
		Map realMap = null;
		Class<?> mapClass;
		mapClass = typeAnno != null ? typeAnno.value() : HashMap.class;
		realMap = (Map) PlugsFactory.getPluginsInstanceNew(mapClass);
		// 获取泛型类型
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			// 得到泛型里的class类型对象
			Class<?> realClass = (Class<?>) pt.getActualTypeArguments()[1];
			List<?> nodes = node.selectNodes(nodeName);
			for (Object childs : nodes) {
				Object realObject = PlugsFactory.getPluginsInstanceNew(realClass);
				AppClassLoader loader = new AppClassLoader(realObject);
				ClassHelper fieldClassHelper = ClassInfoCache.getClassHelper(realClass);
				Field[] fields = this.getFields(fieldClassHelper, helper);
				Object tempObject = null;
				for (Field f : fields) {
					tempObject = processField((Node) childs, f, fieldClassHelper, level);
					if (f != null)
						loader.set(f, tempObject);
				}
				Field key = this.getMapKey(helper, realClass);
				realMap.put(loader.get(key), realObject);
			}
		}
		return realMap;
	}

	/**
	 * 构造List节点数据
	 * 
	 * @param helper field helper
	 * @param field field
	 * @param node node
	 * @param level level
	 * @param nodeName node name
	 * @return object
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object buildListNode(FieldHelper helper, Field field, Node node, int level, String nodeName)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		// 获取Type注解
		com.yanan.utils.beans.xml.Type typeAnno = helper.getAnnotation(com.yanan.utils.beans.xml.Type.class);
		Class<?> listClass;
		// 获取集合的实例
		listClass = typeAnno != null ? typeAnno.value() : ArrayList.class;
		List realList = (List) PlugsFactory.getPluginsInstanceNew(listClass);
		// 获取泛型类型
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			// 得到泛型里的class类型对象
			Class<?> realClass = (Class<?>) pt.getActualTypeArguments()[0];
			if(node!=null){
				List<?> nodes = node.selectNodes(nodeName);
				for (Object childs : nodes) {
					Object realObject = PlugsFactory.getPluginsInstanceNew(realClass);
					AppClassLoader loader = new AppClassLoader(realObject);
					ClassHelper fieldClassHelper = ClassInfoCache.getClassHelper(realClass);
					Field[] fields = this.getFields(fieldClassHelper, helper);
					Object tempObject = null;
					for (Field f : fields) {
						tempObject = processField((Node) childs, f, fieldClassHelper, level);
						if (f != null) {
							loader.set(f, tempObject);
						}
					}
					realList.add(realObject);
				}
			}
		}
		return realList;
	}

	public void addField(Object object, Field field) {
		AppClassLoader loader = new AppClassLoader(object);
		try {
			loader.set(field, object);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			log.error(e.getMessage(),e);
		}
	}

	/**
	 * 获取节点的数据
	 * 
	 * @param nodes 节点集合
	 * @param targetType 目标类型
	 * @return 实例
	 */
	protected Object getNodesValues(List<?> nodes, Class<?> targetType) {
		Object tempArrayList = Array.newInstance(targetType, nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			Element element = (org.dom4j.Element) nodes.get(i);
			Array.set(tempArrayList, i, AppClassLoader.castType(element.getText(), targetType));
		}
		return tempArrayList;
	}

	@SuppressWarnings("deprecation")
	public static Object castType(Object orgin, Class<?> targetType) {
		// 整形
		if (targetType.equals(int.class))
			return Integer.parseInt(("" + orgin).equals("") ? "0" : "" + orgin);
		if (targetType.equals(short.class))
			return Short.parseShort((String) orgin);
		if (targetType.equals(long.class))
			return Long.parseLong((String) orgin);
		if (targetType.equals(byte.class))
			return Byte.parseByte((String) orgin);
		// 浮点
		if (targetType.equals(float.class))
			return Float.parseFloat("" + orgin);
		if (targetType.equals(double.class))
			return Double.parseDouble((String) orgin);
		// 日期
		if (targetType.equals(Date.class))
			return new Date(orgin + "");
		// 布尔型
		if (targetType.equals(boolean.class))
			return Boolean.parseBoolean((String) orgin);
		// char
		if (targetType.equals(char.class))
			return (char) orgin;
		// 没有匹配到返回源数据
		return orgin;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public Map<String, String> getNameMapping() {
		return nameMapping;
	}

	public void setNameMapping(Map<String, String> nameMapping) {
		this.nameMapping = nameMapping;
	}

	public List<Object> getBeanObjectList() {
		return beanObjectList;
	}

	public void setBeanObjectList(List<Object> beanObjectList) {
		this.beanObjectList = beanObjectList;
	}

	public List<String> getRemoveNodes() {
		return removeNodes;
	}

	public void setRemoveNodes(List<String> removeNodes) {
		this.removeNodes = removeNodes;
	}

	public int getScanLevel() {
		return scanLevel;
	}

	public void setScanLevel(int scanLevel) {
		this.scanLevel = scanLevel;
	}

	public Map<String, Class<?>> getMapMapping() {
		return mapMapping;
	}

	public void setMapMapping(Map<String, Class<?>> mapMapping) {
		this.mapMapping = mapMapping;
	}

	public Class<?> getMapping() {
		return mapping;
	}
}
