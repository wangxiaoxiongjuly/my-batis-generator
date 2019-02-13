package generatorConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

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
        recordField.setName("record");
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
        addBatchUpdateExampleMethod(inter,introspectedTable);
        return super.clientGenerated(inter, topLevelClass, introspectedTable);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable){
        addBatchUpdateXml(document, introspectedTable);
        addBatchUpdateExampleXml(document,introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    @Override
    public boolean validate(List<String> warnings){
        return true;
    }

    private void addBatchUpdateMethod(Interface inter, IntrospectedTable introspectedTable) {
        if(introspectedTable.getPrimaryKeyColumns().isEmpty()){
            return;
        }
        Set<FullyQualifiedJavaType> importedTypes = new HashSet<FullyQualifiedJavaType>();
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

        Method ibsmethod = new Method();

        ibsmethod.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType ibsreturnType = FullyQualifiedJavaType.getIntInstance();
        ibsmethod.setReturnType(ibsreturnType);

        ibsmethod.setName("updateBatchByPrimaryKeySelective");

        FullyQualifiedJavaType paramType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType paramListType;
        if (introspectedTable.getRules().generateBaseRecordClass()){
            paramListType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        } else {
            if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                paramListType = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
            } else {
                throw new RuntimeException("批量更新语句生成失败");
            }
        }
        paramType.addTypeArgument(paramListType);

        ibsmethod.addParameter(new Parameter(paramType, "records"));

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

        ibsmethod.setName("updateBatchByExampleSelective");

        FullyQualifiedJavaType paramType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType paramListType;
        if (introspectedTable.getRules().generateExampleClass()){
            paramListType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        } else {
            throw new RuntimeException("批量更新Example语句生成失败");
        }
        paramType.addTypeArgument(paramListType);

        ibsmethod.addParameter(new Parameter(paramType, "list"));

        inter.addImportedTypes(importedTypes);
        inter.addMethod(ibsmethod);
    }

    private void addBatchUpdateXml(Document document, IntrospectedTable introspectedTable){
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        if(introspectedTable.getPrimaryKeyColumns().isEmpty()){
            return;
        }
        String keyColumn = (introspectedTable.getPrimaryKeyColumns().get(0)).getActualColumnName();

        XmlElement insertBatchElement = new XmlElement("update");
        insertBatchElement.addAttribute(new Attribute("id", "updateBatchByPrimaryKeySelective"));
        insertBatchElement.addAttribute(new Attribute("parameterType", "java.util.List"));

        XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection", "list"));
        foreach.addAttribute(new Attribute("item", "item"));
        foreach.addAttribute(new Attribute("index", "index"));

        foreach.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        XmlElement trim1Element = new XmlElement("set");
        String columnName;
        for (IntrospectedColumn introspectedColumn : columns) {
            columnName = introspectedColumn.getActualColumnName();
            if (!columnName.toUpperCase().equalsIgnoreCase(keyColumn)) {
                XmlElement ifxml = new XmlElement("if");
                ifxml.addAttribute(new Attribute("test", "item." + introspectedColumn.getJavaProperty() + "!=null"));
                ifxml.addElement(new TextElement(columnName + "=#{item." + introspectedColumn.getJavaProperty() + ",jdbcType=" + introspectedColumn.getJdbcTypeName() + "},"));
                trim1Element.addElement(ifxml);
            }
        }
        foreach.addElement(trim1Element);

        foreach.addElement(new TextElement("where "));
        int index = 0;
        for (IntrospectedColumn i : introspectedTable.getPrimaryKeyColumns()) {
            foreach.addElement(new TextElement((index > 0 ? " AND " : "") + i.getActualColumnName() + " = #{item." + i.getJavaProperty() + ",jdbcType=" + i.getJdbcTypeName() + "}"));
            index++;
        }
        foreach.addElement(new TextElement(";"));
        insertBatchElement.addElement(foreach);

        document.getRootElement().addElement(insertBatchElement);
    }

    private void addBatchUpdateExampleXml(Document document, IntrospectedTable introspectedTable){
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();

        XmlElement insertBatchElement = new XmlElement("update");
        insertBatchElement.addAttribute(new Attribute("id", "updateBatchByExampleSelective"));
        insertBatchElement.addAttribute(new Attribute("parameterType", "java.util.List"));

        XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection", "list"));
        foreach.addAttribute(new Attribute("item", "item"));
        foreach.addAttribute(new Attribute("index", "index"));

        foreach.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        XmlElement trim1Element = new XmlElement("set");
        String columnName;
        for (IntrospectedColumn introspectedColumn : columns) {
            columnName = introspectedColumn.getActualColumnName();
            XmlElement ifxml = new XmlElement("if");
            ifxml.addAttribute(new Attribute("test", "item.record." + introspectedColumn.getJavaProperty() + "!=null"));
            ifxml.addElement(new TextElement(columnName + "=#{item.record." + introspectedColumn.getJavaProperty() + ",jdbcType=" + introspectedColumn.getJdbcTypeName() + "},"));
            trim1Element.addElement(ifxml);
        }
        foreach.addElement(trim1Element);

        XmlElement ifExampleCauseEmpty = new XmlElement("if");
        ifExampleCauseEmpty.addAttribute(new Attribute("test","_parameter != null"));
        ifExampleCauseEmpty.addElement(getExampleClause());
        foreach.addElement(ifExampleCauseEmpty);

        foreach.addElement(new TextElement(";"));
        insertBatchElement.addElement(foreach);

        document.getRootElement().addElement(insertBatchElement);
    }

    private XmlElement getExampleClause(){
        XmlElement sqlExampleCauseEmpty = new XmlElement("where");

        XmlElement forEle = new XmlElement("foreach");
        forEle.addAttribute(new Attribute("collection","item.oredCriteria"));
        forEle.addAttribute(new Attribute("item","criteria"));
        forEle.addAttribute(new Attribute("separator","or"));

        XmlElement ifEle = new XmlElement("if");
        ifEle.addAttribute(new Attribute("test","criteria.valid"));

        XmlElement trim = new XmlElement("trim");
        trim.addAttribute(new Attribute("prefix","("));
        trim.addAttribute(new Attribute("suffix",")"));
        trim.addAttribute(new Attribute("prefixOverrides","and"));


        XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection","criteria.criteria"));
        foreach.addAttribute(new Attribute("item","criterion"));

        XmlElement choose = new XmlElement("choose");

        XmlElement when1 = new XmlElement("when");
        when1.addAttribute(new Attribute("test","criterion.noValue"));
        when1.addElement(new TextElement("and ${criterion.condition}"));

        XmlElement when2 = new XmlElement("when");
        when2.addAttribute(new Attribute("test","criterion.singleValue"));
        when2.addElement(new TextElement("and ${criterion.condition} #{criterion.value}"));

        XmlElement when3 = new XmlElement("when");
        when3.addAttribute(new Attribute("test","criterion.betweenValue"));
        when3.addElement(new TextElement("and ${criterion.condition} #{criterion.value} and #{criterion.secondValue}"));

        XmlElement when4 = new XmlElement("when");
        when4.addAttribute(new Attribute("test","criterion.listValue"));
        when4.addElement(new TextElement("and ${criterion.condition}"));

        XmlElement when4foreach = new XmlElement("foreach");
        when4foreach.addAttribute(new Attribute("collection","criterion.value"));
        when4foreach.addAttribute(new Attribute("item","listItem"));
        when4foreach.addAttribute(new Attribute("open","("));
        when4foreach.addAttribute(new Attribute("close",")"));
        when4foreach.addAttribute(new Attribute("separator",","));
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