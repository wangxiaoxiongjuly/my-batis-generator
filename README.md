# myBatisGenerator - myBatis逆向工程
## 文件结构说明
- config (逆向工程插件)
- main (逆向主程序)
- resources （配置文件）


    generator.properties （数据库连接等基本配置） 
    generatorConfig.xml （org.mybatis.generator 的配置文件）
    module-generatorConfig.xml （org.mybatis.generator 配置文件解释模板）
    
    
## 主程序
- 直接调用main方法即可 

## 插件主要解决问题
主要解决了like语句原生插件的bug，注释自动添加，批量添加及修改，插入数据后主键返回，输出后原始文件覆盖等问题