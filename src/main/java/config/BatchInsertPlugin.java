package config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import config.content.*;
import config.exception.GenException;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * 批量插入插件
 *
 * @author wenxuan.wong
 */
public class BatchInsertPlugin extends PluginAdapter {

    /**
     * 在接口创建新方法
     */
    @Override
    public boolean clientGenerated(Interface inter, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addBatchInsertMethod(inter, introspectedTable);
        return super.clientGenerated(inter, topLevelClass, introspectedTable);
    }

    /**
     * SQLMAP 新文件xml生成
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        addBatchInsertSelectiveXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * 添加接口方法
     */
    private void addBatchInsertMethod(Interface inter, IntrospectedTable introspectedTable) {
        Set<FullyQualifiedJavaType> importedTypes = new HashSet<FullyQualifiedJavaType>();
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

        Method ibsmethod = new Method();

        ibsmethod.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType ibsreturnType = FullyQualifiedJavaType.getIntInstance();
        ibsmethod.setReturnType(ibsreturnType);

        ibsmethod.setName(Function.INSERT_BATCH_SELECTIVE);

        FullyQualifiedJavaType paramType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType paramListType;
        if (introspectedTable.getRules().generateBaseRecordClass()) {
            paramListType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        } else {
            if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                paramListType = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
            } else {
                throw new GenException("生成失败");
            }
        }
        paramType.addTypeArgument(paramListType);

        ibsmethod.addParameter(new Parameter(paramType, Text.RECORDS));

        inter.addImportedTypes(importedTypes);
        inter.addMethod(ibsmethod);
    }

    private void addBatchInsertSelectiveXml(Document document, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();

        String incrementField = introspectedTable.getTableConfiguration().getProperties().getProperty("incrementField");
        if (incrementField != null) {
            incrementField = incrementField.toUpperCase();
        }

        //insert方法配置
        XmlElement insertBatchElement = new XmlElement(Ele.INSERT);
        insertBatchElement.addAttribute(new Attribute(Attr.ID, Function.INSERT_BATCH_SELECTIVE));
        insertBatchElement.addAttribute(new Attribute(Attr.PARAMETER_TYPE, JavaType.LIST));

        XmlElement javaPropertyAndDbType = new XmlElement(Ele.TRIM);
        javaPropertyAndDbType.addAttribute(new Attribute(Attr.PREFIX, Text.LEFT_BRACKET));
        javaPropertyAndDbType.addAttribute(new Attribute(Attr.SUFFIX, Text.RIGHT_BRACKET));
        javaPropertyAndDbType.addAttribute(new Attribute(Attr.SUFFIX_OVERRIDES, Text.COMMA));

        XmlElement trim1Element = new XmlElement(Ele.TRIM);
        trim1Element.addAttribute(new Attribute(Attr.PREFIX, Text.LEFT_BRACKET));
        trim1Element.addAttribute(new Attribute(Attr.SUFFIX, Text.RIGHT_BRACKET));
        trim1Element.addAttribute(new Attribute(Attr.SUFFIX_OVERRIDES, Text.COMMA));

        XmlElement foreachElement = new XmlElement(Ele.FOREACH);
        foreachElement.addAttribute(new Attribute(Attr.COLLECTION, Text.LIST));
        foreachElement.addAttribute(new Attribute(Attr.INDEX, Text.INDEX));
        foreachElement.addAttribute(new Attribute(Attr.ITEM, Text.ITEM));

        for (IntrospectedColumn introspectedColumn : columns) {
            String columnName = introspectedColumn.getActualColumnName();
            if (!columnName.equalsIgnoreCase(incrementField)) {
                XmlElement iftest = new XmlElement(Ele.IF);
                iftest.addAttribute(new Attribute(Attr.TEST, "item." + introspectedColumn.getJavaProperty() + "!=null"));
                iftest.addElement(new TextElement(columnName + ","));
                trim1Element.addElement(iftest);
                XmlElement trimiftest = new XmlElement(Ele.IF);
                trimiftest.addAttribute(new Attribute(Attr.TEST, "item." + introspectedColumn.getJavaProperty() + "!=null"));
                trimiftest.addElement(new TextElement("#{item." + introspectedColumn.getJavaProperty() + ",jdbcType=" + introspectedColumn.getJdbcTypeName() + "},"));
                javaPropertyAndDbType.addElement(trimiftest);
            }
        }

        insertBatchElement.addElement(foreachElement);

        foreachElement.addElement(new TextElement("insert into " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        foreachElement.addElement(trim1Element);
        foreachElement.addElement(new TextElement(" values "));
        foreachElement.addElement(javaPropertyAndDbType);
        foreachElement.addElement(new TextElement(Text.SEMICOLON));

        document.getRootElement().addElement(insertBatchElement);
    }

}
