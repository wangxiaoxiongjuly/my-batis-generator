package config;

import config.content.Attr;
import config.content.Ele;
import config.content.Function;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

/**
 * @author wenxuan.wang
 */
public class OraclePagingPlugin extends PluginAdapter {
	
	private void updateSchema(TopLevelClass topLevelClass){
		FullyQualifiedJavaType fullyQualifiedJavaType = topLevelClass.getType();
		String basePackage = fullyQualifiedJavaType.getPackageName();
		basePackage = basePackage.replace(".zrliqpower", "");
		
		try {
			java.lang.reflect.Field packageField = fullyQualifiedJavaType.getClass().getDeclaredField("packageName");
			packageField.setAccessible(true);
			packageField.set(fullyQualifiedJavaType, basePackage);
			
			String shortName = fullyQualifiedJavaType.getShortName();
			
			java.lang.reflect.Field baseQualifiedNameField = fullyQualifiedJavaType.getClass().getDeclaredField("baseQualifiedName");
			baseQualifiedNameField.setAccessible(true);
			baseQualifiedNameField.set(fullyQualifiedJavaType, basePackage+"."+shortName);
			
		} catch (Exception e) {
		}
	}
	
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		updateSchema(topLevelClass);
		return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
	}
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    	
    	// 临时去掉DS的schema
    	TextElement tableNameText = (TextElement)element.getElements().get(3);
    	String tableName = tableNameText.getContent();
    	tableName = tableName.replace("ZRLIQPOWER.", "");
    	
    	try {
    		java.lang.reflect.Field field = TextElement.class.getDeclaredField("content");
    		field.setAccessible(true);
    		field.set(tableNameText, tableName);
		} catch (Exception e) {
		}
    	
    	XmlElement pageStart = new XmlElement(Ele.INCLUDE);
		pageStart.addAttribute(new Attribute(Attr.REFID, Function.ORACLE_DIALECT_PREFIX));
		element.getElements().add(0, pageStart);

		XmlElement isNotNullElement = new XmlElement(Ele.INCLUDE);
		isNotNullElement.addAttribute(new Attribute(Attr.REFID, Function.ORACLE_DIALECT_SUFFIX));
		element.getElements().add(isNotNullElement);
    	
    	return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }
    
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		XmlElement parentElement = document.getRootElement();

		// 产生分页语句前半部分
		XmlElement paginationPrefixElement = new XmlElement(Ele.SQL);
		paginationPrefixElement.addAttribute(new Attribute(Attr.ID, Function.ORACLE_DIALECT_PREFIX));
		XmlElement pageStart = new XmlElement(Ele.IF);
		pageStart.addAttribute(new Attribute(Attr.TEST, "limitOffset >= 0"));
		pageStart.addElement(new TextElement("select * from ( select row_.*, rownum rownum_ from ( "));
		paginationPrefixElement.addElement(pageStart);
		parentElement.addElement(paginationPrefixElement);

		// 产生分页语句后半部分
		XmlElement paginationSuffixElement = new XmlElement(Ele.SQL);
		paginationSuffixElement.addAttribute(new Attribute(Attr.ID, Function.ORACLE_DIALECT_SUFFIX));
		XmlElement pageEnd = new XmlElement(Ele.IF);
		pageEnd.addAttribute(new Attribute(Attr.TEST, "limitOffset >= 0"));
		pageEnd.addElement(new TextElement("<![CDATA[ ) row_ ) where rownum_ > #{begin} and rownum_ <= #{end} ]]>"));
		paginationSuffixElement.addElement(pageEnd);
		parentElement.addElement(paginationSuffixElement);

		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // add field, getter, setter for page info
        addPageInfoToExampleClass(topLevelClass, introspectedTable);
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    private void addPageInfoToExampleClass(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    	updateSchema(topLevelClass);
    	CommentGenerator commentGenerator = context.getCommentGenerator();

		Field limitOffsetField = new Field();
		limitOffsetField.setVisibility(JavaVisibility.PROTECTED);
		limitOffsetField.setType(FullyQualifiedJavaType.getIntInstance());
		limitOffsetField.setName("limitOffset");
		limitOffsetField.setInitializationString("-1");
		commentGenerator.addFieldComment(limitOffsetField, introspectedTable);
		topLevelClass.addField(limitOffsetField);

		Field limitSizeField = new Field();
		limitSizeField.setVisibility(JavaVisibility.PROTECTED);
		limitSizeField.setType(FullyQualifiedJavaType.getIntInstance());
		limitSizeField.setName("limitSize");
		limitSizeField.setInitializationString("1");
		commentGenerator.addFieldComment(limitSizeField, introspectedTable);
		topLevelClass.addField(limitSizeField);

		// add setter method
		Method limitOffsetSetter = new Method();
		limitOffsetSetter.setVisibility(JavaVisibility.PUBLIC);
		limitOffsetSetter.setName("setLimitOffset");
		limitOffsetSetter.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), "limitOffset"));
		limitOffsetSetter.addBodyLine("this.limitOffset = limitOffset;");
		commentGenerator.addGeneralMethodComment(limitOffsetSetter, introspectedTable);
		topLevelClass.addMethod(limitOffsetSetter);

		Method limitSizeSetter = new Method();
		limitSizeSetter.setVisibility(JavaVisibility.PUBLIC);
		limitSizeSetter.setName("setLimitSize");
		limitSizeSetter.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), "limitSize"));
		limitSizeSetter.addBodyLine("this.limitSize = limitSize;");
		commentGenerator.addGeneralMethodComment(limitSizeSetter, introspectedTable);
		topLevelClass.addMethod(limitSizeSetter);

		// add getter method
		Method limitOffsetGetter = new Method();
		limitOffsetGetter.setVisibility(JavaVisibility.PUBLIC);
		limitOffsetGetter.setReturnType(FullyQualifiedJavaType.getIntInstance());
		limitOffsetGetter.setName("getLimitOffset");
		limitOffsetGetter.addBodyLine("return limitOffset;");
		commentGenerator.addGeneralMethodComment(limitOffsetGetter, introspectedTable);
		topLevelClass.addMethod(limitOffsetGetter);

		Method limitSizeGetter = new Method();
		limitSizeGetter.setVisibility(JavaVisibility.PUBLIC);
		limitSizeGetter.setReturnType(FullyQualifiedJavaType.getIntInstance());
		limitSizeGetter.setName("getLimitSize");
		limitSizeGetter.addBodyLine("return limitSize;");
		commentGenerator.addGeneralMethodComment(limitSizeGetter, introspectedTable);
		topLevelClass.addMethod(limitSizeGetter);

		Method beginGetter = new Method();
		beginGetter.setVisibility(JavaVisibility.PUBLIC);
		beginGetter.setReturnType(FullyQualifiedJavaType.getIntInstance());
		beginGetter.setName("getBegin");
		beginGetter.addBodyLine("return limitOffset;");
		commentGenerator.addGeneralMethodComment(beginGetter, introspectedTable);
		topLevelClass.addMethod(beginGetter);
		
		
		Method endGetter = new Method();
		endGetter.setVisibility(JavaVisibility.PUBLIC);
		endGetter.setReturnType(FullyQualifiedJavaType.getIntInstance());
		endGetter.setName("getEnd");
		endGetter.addBodyLine("return limitOffset+limitSize;");
		commentGenerator.addGeneralMethodComment(endGetter, introspectedTable);
		topLevelClass.addMethod(endGetter);
    }

    /**
     * This plugin is always valid - no properties are required
     */
    @Override
    public boolean validate(List<String> warnings){
        return true;
    }
}