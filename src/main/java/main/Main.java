package main;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 执行逆向工程主程序
 * @author wenxuan.wang
 */
public class Main {
    @SuppressWarnings("ALL")
    private static final String FILEPATH = Main.class.getClassLoader().getResource("generatorConfig.xml").getPath();


    public void generator() throws InvalidConfigurationException ,IOException ,XMLParserException,InterruptedException,SQLException{

        List<String> warnings = new ArrayList<String>();
        //指定 逆向工程配置文件
        File configFile = new File(FILEPATH);
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(configFile);
        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config,
                callback, warnings);
        myBatisGenerator.generate(null);
    }
    public static void main(String[] args) throws Exception {
        Main sQLMapReverser = new Main();
        sQLMapReverser.generator();

    }
}
