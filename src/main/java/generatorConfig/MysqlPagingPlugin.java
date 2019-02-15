package generatorConfig;

import generatorConfig.content.Attr;
import generatorConfig.content.Ele;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;


public class MysqlPagingPlugin extends PluginAdapter {

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        appendPageInfoAfterSelectByExampleXml(element, introspectedTable);
        return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        appendPageInfoAfterSelectByExampleXml(element, introspectedTable);
        return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    private void appendPageInfoAfterSelectByExampleXml(XmlElement element, IntrospectedTable introspectedTable) {

        XmlElement pageEl = new XmlElement(Ele.IF);
        pageEl.addAttribute(new Attribute(Attr.TEST, "limitOffset >= 0"));
        pageEl.addElement(new TextElement(" limit ${limitOffset} , ${limitSize}"));
        element.addElement(pageEl);
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // add field, getter, setter for page info
        addPageInfoToExampleClass(topLevelClass, introspectedTable);
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    private void addPageInfoToExampleClass(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
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
    }

    /**
     * This plugin is always valid - no properties are required
     */
    @Override
    public boolean validate(List<String> warnings){
        return true;
    }
}