package config;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

/**
 * @author wenxuan.wong
 */
public class InsertPrimaryPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * useGeneratedKeys="true" keyProperty="id" keyColumn="id"
     */
    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (!introspectedTable.getPrimaryKeyColumns().isEmpty()) {
            element.addAttribute(new Attribute("useGeneratedKeys", "true"));
            element.addAttribute(new Attribute("keyProperty", introspectedTable.getPrimaryKeyColumns().get(0).getJavaProperty()));
            element.addAttribute(new Attribute("keyColumn", introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName()));
        }
        return super.sqlMapInsertElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (!introspectedTable.getPrimaryKeyColumns().isEmpty()) {
            element.addAttribute(new Attribute("useGeneratedKeys", "true"));
            element.addAttribute(new Attribute("keyProperty", introspectedTable.getPrimaryKeyColumns().get(0).getJavaProperty()));
            element.addAttribute(new Attribute("keyColumn", introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName()));
        }
        return super.sqlMapInsertSelectiveElementGenerated(element, introspectedTable);
    }
}
