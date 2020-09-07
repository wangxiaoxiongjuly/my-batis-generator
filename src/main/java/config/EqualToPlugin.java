package config;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EqualToPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        super.modelExampleClassGenerated(topLevelClass, introspectedTable);
        InnerClass criteria = null;
        Iterator i$ = topLevelClass.getInnerClasses().iterator();

        while (i$.hasNext()) {
            InnerClass innerClass = (InnerClass) i$.next();
            if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
                criteria = innerClass;
                break;
            }
        }

        if (criteria == null) {
            return true;
        } else {
            for (Method method : criteria.getMethods()){
                if(method.getName().endsWith("EqualTo")){
                    List<String> newLine = new ArrayList<String>(method.getBodyLines());
                    method.getBodyLines().clear();
                    method.getBodyLines().add("if(value == null) {\n" +
                            "\t\t\t\treturn (Criteria) this;\n" +
                            "\t\t\t}");
                    for (String line : newLine){
                        method.getBodyLines().add(line);
                    }

                }
            }
            return true;
        }
    }
}