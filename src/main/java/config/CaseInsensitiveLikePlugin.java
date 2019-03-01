package config;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.ibatis2.Ibatis2FormattingUtilities;

import java.util.Iterator;
import java.util.List;

/**
 * 原生like插件Bug处理
 * @author wenxuan.wong
 */
public class CaseInsensitiveLikePlugin extends PluginAdapter {
    public CaseInsensitiveLikePlugin() {
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        InnerClass criteria = null;
        Iterator var4 = topLevelClass.getInnerClasses().iterator();

        for (;var4.hasNext();) {
            InnerClass innerClass = (InnerClass)var4.next();
            if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
                criteria = innerClass;
                break;
            }
        }

        if (criteria == null) {
            return true;
        } else {
            var4 = introspectedTable.getNonBLOBColumns().iterator();

            while(var4.hasNext()) {
                IntrospectedColumn introspectedColumn = (IntrospectedColumn)var4.next();
                if (introspectedColumn.isJdbcCharacterColumn() && introspectedColumn.isStringColumn()) {
                    Method method = new Method();
                    method.setVisibility(JavaVisibility.PUBLIC);
                    method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), "value"));
                    StringBuilder sb = new StringBuilder();
                    sb.append(introspectedColumn.getJavaProperty());
                    sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
                    sb.insert(0, "and");
                    sb.append("LikeInsensitive");
                    method.setName(sb.toString());
                    method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
                    sb.setLength(0);
                    sb.append("addCriterion(\"upper(");
                    sb.append(Ibatis2FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
                    //原生like直接使用的是存在问题的，根据具体情况调整
                    sb.append(") like\",\"%\"+ value.toUpperCase() +\"%\", \"");
                    sb.append(introspectedColumn.getJavaProperty());
                    sb.append("\");");
                    method.addBodyLine(sb.toString());
                    method.addBodyLine("return (Criteria) this;");
                    criteria.addMethod(method);
                }
            }

            return true;
        }
    }
}

