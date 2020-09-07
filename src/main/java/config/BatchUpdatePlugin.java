package config;

import config.content.*;
import config.exception.GenterException;
import config.utils.PropertiesUtil;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 批量更新插件
 *
 * @author wenxuan.wang
 */
public class BatchUpdatePlugin extends PluginAdapter {

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addPageInfoToExampleClass(topLevelClass, introspectedTable);
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    private void addPageInfoToExampleClass(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        CommentGenerator commentGenerator = context.getCommentGenerator();

        Field recordField = new Field();
        recordField.setVisibility(JavaVisibility.PROTECTED);
        recordField.setType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        recordField.setName(Text.RECORD);
        commentGenerator.addFieldComment(recordField, introspectedTable);
        topLevelClass.addField(recordField);

        // add setter method
        Method recordSetter = new Method();
        recordSetter.setVisibility(JavaVisibility.PUBLIC);
        recordSetter.setName("setRecord");
        recordSetter.addParameter(new Parameter(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "record"));
        recordSetter.addBodyLine("this.record = record;");
        commentGenerator.addGeneralMethodComment(recordSetter, introspectedTable);
        topLevelClass.addMethod(recordSetter);

        // add getter method
        Method recordGetter = new Method();
        recordGetter.setVisibility(JavaVisibility.PUBLIC);
        recordGetter.setReturnType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        recordGetter.setName("getRecord");
        recordGetter.addBodyLine("return record;");
        commentGenerator.addGeneralMethodComment(recordGetter, introspectedTable);
        topLevelClass.addMethod(recordGetter);
    }

    @Override
    public boolean clientGenerated(Interface inter, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addBatchUpdateMethod(inter, introspectedTable);
        addBatchUpdateExampleMethod(inter, introspectedTable);
        return super.clientGenerated(inter, topLevelClass, introspectedTable);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        String dataType = PropertiesUtil.getProperty("dataBaseType", "mysql");
        addBatchUpdateXml(document, introspectedTable, dataType);
        addBatchUpdateExampleXml(document, introspectedTable, dataType);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    private void addBatchUpdateMethod(Interface inter, IntrospectedTable introspectedTable) {
        if (introspectedTable.getPrimaryKeyColumns().isEmpty()) {
            return;
        }
        Set<FullyQualifiedJavaType> importedTypes = new HashSet<FullyQualifiedJavaType>();
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

        Method ibsmethod = new Method();

        ibsmethod.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType ibsreturnType = FullyQualifiedJavaType.getIntInstance();
        ibsmethod.setReturnType(ibsreturnType);

        ibsmethod.setName(Function.UPDATE_BATCH_BY_PRIMARY_KEY_SELECTIVE);

        FullyQualifiedJavaType paramType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType paramListType;
        if (introspectedTable.getRules().generateBaseRecordClass()) {
            paramListType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        } else {
            if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                paramListType = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
            } else {
                throw new GenterException("批量更新语句生成失败");
            }
        }
        paramType.addTypeArgument(paramListType);

        ibsmethod.addParameter(new Parameter(paramType, Text.RECORDS));

        inter.addImportedTypes(importedTypes);
        inter.addMethod(ibsmethod);
    }

    private void addBatchUpdateExampleMethod(Interface inter, IntrospectedTable introspectedTable) {
        Set<FullyQualifiedJavaType> importedTypes = new HashSet<FullyQualifiedJavaType>();
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

        Method ibsmethod = new Method();

        ibsmethod.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType ibsreturnType = FullyQualifiedJavaType.getIntInstance();
        ibsmethod.setReturnType(ibsreturnType);

        ibsmethod.setName(Function.UPDATE_BATCH_BY_EXAMPLE_SELECTIVE);

        FullyQualifiedJavaType paramType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType paramListType;
        if (introspectedTable.getRules().generateExampleClass()) {
            paramListType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        } else {
            throw new GenterException("批量更新Example语句生成失败");
        }
        paramType.addTypeArgument(paramListType);

        ibsmethod.addParameter(new Parameter(paramType, Text.LIST));

        inter.addImportedTypes(importedTypes);
        inter.addMethod(ibsmethod);
    }

    private void addBatchUpdateXml(Document document, IntrospectedTable introspectedTable, String dataBase) {
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        if (introspectedTable.getPrimaryKeyColumns().isEmpty()) {
            return;
        }
        String keyColumn = (introspectedTable.getPrimaryKeyColumns().get(0)).getActualColumnName();

        XmlElement updBatchElement = new XmlElement(Ele.UPDATE);
        updBatchElement.addAttribute(new Attribute(Attr.ID, Function.UPDATE_BATCH_BY_PRIMARY_KEY_SELECTIVE));
        updBatchElement.addAttribute(new Attribute(Attr.PARAMETER_TYPE, JavaType.LIST));

        XmlElement foreach = new XmlElement(Ele.FOREACH);
        foreach.addAttribute(new Attribute(Attr.COLLECTION, Text.LIST));
        foreach.addAttribute(new Attribute(Attr.ITEM, Text.ITEM));
        foreach.addAttribute(new Attribute(Attr.INDEX, Text.INDEX));

        if (DataBaseType.ORACLE.equals(dataBase)) {
            foreach.addAttribute(new Attribute(Attr.OPEN, Text.BEGIN));
            foreach.addAttribute(new Attribute(Attr.CLOSE, Text._END));
            foreach.addAttribute(new Attribute(Attr.SEPARATOR, Text.SEMICOLON));
        }

        foreach.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        XmlElement trim1Element = new XmlElement(Ele.SET);
        String columnName;
        for (IntrospectedColumn introspectedColumn : columns) {
            columnName = introspectedColumn.getActualColumnName();
            if (!columnName.toUpperCase().equalsIgnoreCase(keyColumn)) {
                XmlElement ifxml = new XmlElement(Ele.IF);
                ifxml.addAttribute(new Attribute(Attr.TEST, "item." + introspectedColumn.getJavaProperty() + "!=null"));
                ifxml.addElement(new TextElement(columnName + "=#{item." + introspectedColumn.getJavaProperty() + "," + Text.JDBC_TYPE + "=" + introspectedColumn.getJdbcTypeName() + "},"));
                trim1Element.addElement(ifxml);
            }
        }
        foreach.addElement(trim1Element);

        foreach.addElement(new TextElement("where "));
        int index = 0;
        for (IntrospectedColumn i : introspectedTable.getPrimaryKeyColumns()) {
            foreach.addElement(new TextElement((index > 0 ? " AND " : "") + i.getActualColumnName() + " = #{item." + i.getJavaProperty() + "," + Text.JDBC_TYPE + "=" + i.getJdbcTypeName() + "}"));
            index++;
        }
        if (DataBaseType.MYSQL.equals(dataBase)) {
            foreach.addElement(new TextElement(Text.SEMICOLON));
        }
        updBatchElement.addElement(foreach);

        document.getRootElement().addElement(updBatchElement);
    }

    private void addBatchUpdateExampleXml(Document document, IntrospectedTable introspectedTable, String dataBase) {
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();

        XmlElement updBatchElement = new XmlElement(Ele.UPDATE);
        updBatchElement.addAttribute(new Attribute(Attr.ID, Function.UPDATE_BATCH_BY_EXAMPLE_SELECTIVE));
        updBatchElement.addAttribute(new Attribute(Attr.PARAMETER_TYPE, JavaType.LIST));

        XmlElement foreach = new XmlElement(Ele.FOREACH);
        foreach.addAttribute(new Attribute(Attr.COLLECTION, Text.LIST));
        foreach.addAttribute(new Attribute(Attr.ITEM, Text.ITEM));
        foreach.addAttribute(new Attribute(Attr.INDEX, Text.INDEX));

        if (DataBaseType.ORACLE.equals(dataBase)) {
            foreach.addAttribute(new Attribute(Attr.OPEN, Text.BEGIN));
            foreach.addAttribute(new Attribute(Attr.CLOSE, Text._END));
            foreach.addAttribute(new Attribute(Attr.SEPARATOR, Text.SEMICOLON));
        }

        foreach.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        XmlElement trim1Element = new XmlElement(Ele.SET);
        String columnName;
        for (IntrospectedColumn introspectedColumn : columns) {
            columnName = introspectedColumn.getActualColumnName();
            XmlElement ifxml = new XmlElement(Ele.IF);
            ifxml.addAttribute(new Attribute(Attr.TEST, "item.record." + introspectedColumn.getJavaProperty() + "!=null"));
            ifxml.addElement(new TextElement(columnName + "=#{item.record." + introspectedColumn.getJavaProperty() + "," + Text.JDBC_TYPE + "=" + introspectedColumn.getJdbcTypeName() + "},"));
            trim1Element.addElement(ifxml);
        }
        foreach.addElement(trim1Element);

        XmlElement ifExampleCauseEmpty = new XmlElement(Ele.IF);
        ifExampleCauseEmpty.addAttribute(new Attribute(Attr.TEST, "_parameter != null"));
        ifExampleCauseEmpty.addElement(getExampleClause());
        foreach.addElement(ifExampleCauseEmpty);

        if (DataBaseType.MYSQL.equals(dataBase)) {
            foreach.addElement(new TextElement(Text.SEMICOLON));
        }
        updBatchElement.addElement(foreach);

        document.getRootElement().addElement(updBatchElement);
    }

    private XmlElement getExampleClause() {
        XmlElement sqlExampleCauseEmpty = new XmlElement(Ele.WHERE);

        XmlElement forEle = new XmlElement(Ele.FOREACH);
        forEle.addAttribute(new Attribute(Attr.COLLECTION, "item.oredCriteria"));
        forEle.addAttribute(new Attribute(Attr.ITEM, "criteria"));
        forEle.addAttribute(new Attribute(Attr.SEPARATOR, Text.OR));

        XmlElement ifEle = new XmlElement(Ele.IF);
        ifEle.addAttribute(new Attribute(Attr.TEST, "criteria.valid"));

        XmlElement trim = new XmlElement(Ele.TRIM);
        trim.addAttribute(new Attribute(Attr.PREFIX, Text.LEFT_BRACKET));
        trim.addAttribute(new Attribute(Attr.SUFFIX, Text.RIGHT_BRACKET));
        trim.addAttribute(new Attribute(Attr.SUFFIX_OVERRIDES, Text.AND));


        XmlElement foreach = new XmlElement(Ele.FOREACH);
        foreach.addAttribute(new Attribute(Attr.COLLECTION, "criteria.criteria"));
        foreach.addAttribute(new Attribute(Attr.ITEM, "criterion"));

        XmlElement choose = new XmlElement(Ele.CHOOSE);

        XmlElement when1 = new XmlElement(Ele.WHEN);
        when1.addAttribute(new Attribute(Attr.TEST, "criterion.noValue"));
        when1.addElement(new TextElement("and ${criterion.condition}"));

        XmlElement when2 = new XmlElement(Ele.WHEN);
        when2.addAttribute(new Attribute(Attr.TEST, "criterion.singleValue"));
        when2.addElement(new TextElement("and ${criterion.condition} #{criterion.value}"));

        XmlElement when3 = new XmlElement(Ele.WHEN);
        when3.addAttribute(new Attribute(Attr.TEST, "criterion.betweenValue"));
        when3.addElement(new TextElement("and ${criterion.condition} #{criterion.value} and #{criterion.secondValue}"));

        XmlElement when4 = new XmlElement(Ele.WHEN);
        when4.addAttribute(new Attribute(Attr.TEST, "criterion.listValue"));
        when4.addElement(new TextElement("and ${criterion.condition}"));

        XmlElement when4foreach = new XmlElement(Ele.FOREACH);
        when4foreach.addAttribute(new Attribute(Attr.COLLECTION, "criterion.value"));
        when4foreach.addAttribute(new Attribute(Attr.ITEM, "listItem"));
        when4foreach.addAttribute(new Attribute(Attr.OPEN, Text.LIST));
        when4foreach.addAttribute(new Attribute(Attr.CLOSE, Text.RIGHT_BRACKET));
        when4foreach.addAttribute(new Attribute(Attr.SEPARATOR, Text.COMMA));
        when4foreach.addElement(new TextElement("#{listItem}"));

        when4.addElement(when4foreach);
        choose.addElement(when1);
        choose.addElement(when2);
        choose.addElement(when3);
        choose.addElement(when4);
        foreach.addElement(choose);
        trim.addElement(foreach);
        ifEle.addElement(trim);
        forEle.addElement(ifEle);
        sqlExampleCauseEmpty.addElement(forEle);
        return sqlExampleCauseEmpty;
    }
}