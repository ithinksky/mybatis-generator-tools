package com.ithinksky;

import com.google.common.base.CaseFormat;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 代码生成器，根据数据表名称生成对应的Model、Mapper、Service、Controller简化开发。
 */
public class CodeGenerator {


    private static final String CONFIG_FILENAME = "config-dwz";

    private static ResourceBundle rb = PropertyResourceBundle.getBundle("generator/config/" + CONFIG_FILENAME);

    //生成代码所在的基础包名称，可根据自己公司的项目修改（注意：这个配置修改之后需要手工修改src目录项目默认的包路径，使其保持一致，不然会找不到类）
    private static final String BASE_PACKAGE = rb.getString("basePackage");
    private static final String AUTHOR = rb.getString("author");
    private static final String DATE = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
    private static final String TABLE_NAME_PREFIX = rb.getString("tableNamePrefix");

    //Mapper插件基础接口的完全限定名
    private static final String MAPPER_INTERFACE_REFERENCE = rb.getString("mapperInterfaceReference");

    private static final String MODEL_PACKAGE = rb.getString("modelPackage");
    private static final String MAPPER_PACKAGE = rb.getString("mapperPackage");
    private static final String SERVICE_PACKAGE = rb.getString("servicePackage");
    private static final String SERVICE_IMPL_PACKAGE = rb.getString("serviceImplPackage");
    private static final String CONTROLLER_PACKAGE = rb.getString("controllerPackage");

    //JDBC配置，请修改为你项目的实际配置
    private static final String JDBC_URL = rb.getString("jdbcUrl");
    private static final String JDBC_USERNAME = rb.getString("jdbcUser");
    private static final String JDBC_PASSWORD = rb.getString("jdbcPassword");
    private static final String JDBC_DIVER_CLASS_NAME = rb.getString("jdbcDriver");

    private static final String BASE_PROJECT_PATH = System.getProperty("user.dir");//项目在硬盘上的基础路径
    private static final String SUB_PROJECT_PATH = rb.getString("subProjectPath");//子项目路径

    private static final String PROJECT_PATH = BASE_PROJECT_PATH + SUB_PROJECT_PATH;
    private static final String TEMPLATE_FILE_PATH = BASE_PROJECT_PATH
            + "/mybatis-generator-tool-tk-mapper" + rb.getString("templateFilePath");

    private static final String JAVA_PATH =  SUB_PROJECT_PATH + "/src/main/java"; //java文件路径
    private static final String RESOURCES_PATH =  SUB_PROJECT_PATH + "/src/main/resources";//资源文件路径

    //生成的Service存放路径
    private static final String PACKAGE_PATH_SERVICE =  packageConvertPath(SERVICE_PACKAGE);

    //生成的Service实现存放路径
    private static final String PACKAGE_PATH_SERVICE_IMPL =  packageConvertPath(SERVICE_IMPL_PACKAGE);

    //生成的Controller存放路径
    private static final String PACKAGE_PATH_CONTROLLER =  packageConvertPath(CONTROLLER_PACKAGE);

    private static final String TABLES = rb.getString("tables");

    private static final String[] TABLES_ARRAY = TABLES.split(",");


    public static void main(String[] args) {
        genCode(
                TABLES_ARRAY
        );
        //genCodeByCustomModelName("输入表名","输入自定义Model名称");
    }

    /**
     * 去除表前缀
     *
     * @param tableName
     * @return
     */
    public static String cleanPrefix(String tableName) {
        String str = StringUtils.substring(tableName, TABLE_NAME_PREFIX.length());
        return str;
    }

    /**
     * 通过数据表名称生成代码，Model 名称通过解析数据表名称获得，下划线转大驼峰的形式。
     * 如输入表名称 "t_user_detail" 将生成 TUserDetail、TUserDetailMapper、TUserDetailService ...
     * @param tableNames 数据表名称...
     */
    public static void genCode(String... tableNames) {
        for (String tableName : tableNames) {
            String modelName = tableNameConvertUpperCamel(cleanPrefix(tableName));
            genCodeByCustomModelName(tableName, modelName);
        }
    }

    /**
     * 通过数据表名称，和自定义的 Model 名称生成代码
     * 如输入表名称 "t_user_detail" 和自定义的 Model 名称 "User" 将生成 User、UserMapper、UserService ...
     * @param tableName 数据表名称
     * @param modelName 自定义的 Model 名称
     */
    public static void genCodeByCustomModelName(String tableName, String modelName) {
        genModelAndMapper(tableName, modelName);
        genService(tableName, modelName);
//        genFsController(tableName, modelName);
//        genMsController(tableName, modelName);
//        genRpcController(tableName, modelName);
    }


    public static void genModelAndMapper(String tableName, String modelName) {
        Context context = new Context(ModelType.FLAT);
        context.setId("Mysql");
        context.setTargetRuntime("MyBatis3");
        context.addProperty(PropertyRegistry.CONTEXT_BEGINNING_DELIMITER, "`");
        context.addProperty(PropertyRegistry.CONTEXT_ENDING_DELIMITER, "`");

        CommentGeneratorConfiguration commentGeneratorConfiguration = new CommentGeneratorConfiguration();
        commentGeneratorConfiguration.setConfigurationType(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_ALL_COMMENTS);
        context.setCommentGeneratorConfiguration(commentGeneratorConfiguration);

        JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();
        jdbcConnectionConfiguration.setConnectionURL(JDBC_URL);
        jdbcConnectionConfiguration.setUserId(JDBC_USERNAME);
        jdbcConnectionConfiguration.setPassword(JDBC_PASSWORD);
        jdbcConnectionConfiguration.setDriverClass(JDBC_DIVER_CLASS_NAME);
        context.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);

        PluginConfiguration pluginConfiguration = new PluginConfiguration();
        pluginConfiguration.setConfigurationType("tk.mybatis.mapper.generator.MapperPlugin");
        pluginConfiguration.addProperty("mappers", MAPPER_INTERFACE_REFERENCE);
        pluginConfiguration.addProperty("swagger", "false");
//        pluginConfiguration.addProperty("lombok", "Getter,Setter,ToString,Accessors,EqualsAndHashCode");
//        pluginConfiguration.addProperty("lombokEqualsAndHashCodeCallSuper", "false");
        context.addPluginConfiguration(pluginConfiguration);


        // 在Mapper类上 添加 @Mapper注解
        PluginConfiguration mapperAnnotationPluginConfiguration = new PluginConfiguration();
        mapperAnnotationPluginConfiguration.setConfigurationType("org.mybatis.generator.plugins.MapperAnnotationPlugin");
        context.addPluginConfiguration(mapperAnnotationPluginConfiguration);

        //   <!-- 状态枚举生成插件 -->
        PluginConfiguration enumTypeStatusPlugin = new PluginConfiguration();
        enumTypeStatusPlugin.setConfigurationType("com.itfsw.mybatis.generator.plugins.EnumTypeStatusPlugin");
        enumTypeStatusPlugin.addProperty("autoScan", "true");
        enumTypeStatusPlugin.addProperty("enumColumns", "type, status");
        context.addPluginConfiguration(enumTypeStatusPlugin);

        //<!-- 数据Model链式构建插件 -->
        PluginConfiguration modelBuilderPlugin = new PluginConfiguration();
        modelBuilderPlugin.setConfigurationType("com.itfsw.mybatis.generator.plugins.ModelBuilderPlugin");
        context.addPluginConfiguration(modelBuilderPlugin);

        // Example 增强插件(example,andIf,orderBy)
        PluginConfiguration exampleEnhancedPlugin = new PluginConfiguration();
        exampleEnhancedPlugin.setConfigurationType("com.itfsw.mybatis.generator.plugins.ExampleEnhancedPlugin");
        context.addPluginConfiguration(exampleEnhancedPlugin);

//        // <!-- 数据Model属性对应Column获取插件 -->
//        PluginConfiguration modelColumnPlugin = new PluginConfiguration();
//        modelColumnPlugin.setConfigurationType("com.itfsw.mybatis.generator.plugins.ModelColumnPlugin");
//        context.addPluginConfiguration(modelColumnPlugin);

        // <!-- 查询结果选择性返回插件 -->
        PluginConfiguration selectSelectivePlugin = new PluginConfiguration();
        selectSelectivePlugin.setConfigurationType("com.itfsw.mybatis.generator.plugins.SelectSelectivePlugin");
        context.addPluginConfiguration(selectSelectivePlugin);


        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetProject(BASE_PROJECT_PATH + JAVA_PATH);
        javaModelGeneratorConfiguration.setTargetPackage(MODEL_PACKAGE);
        context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
        sqlMapGeneratorConfiguration.setTargetProject(BASE_PROJECT_PATH + RESOURCES_PATH);
        sqlMapGeneratorConfiguration.setTargetPackage("mapper");
        context.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);

        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = new JavaClientGeneratorConfiguration();
        javaClientGeneratorConfiguration.setTargetProject(BASE_PROJECT_PATH + JAVA_PATH);
        javaClientGeneratorConfiguration.setTargetPackage(MAPPER_PACKAGE);
        javaClientGeneratorConfiguration.setConfigurationType("XMLMAPPER");
        context.setJavaClientGeneratorConfiguration(javaClientGeneratorConfiguration);

        TableConfiguration tableConfiguration = new TableConfiguration(context);
        tableConfiguration.setTableName(tableName);

        tableConfiguration.setSelectByExampleStatementEnabled(true);
        tableConfiguration.setDeleteByExampleStatementEnabled(true);
        tableConfiguration.setUpdateByExampleStatementEnabled(true);
        tableConfiguration.setCountByExampleStatementEnabled(true);

        tableConfiguration.setDeleteByPrimaryKeyStatementEnabled(true);
        tableConfiguration.setSelectByPrimaryKeyStatementEnabled(true);
        tableConfiguration.setUpdateByPrimaryKeyStatementEnabled(true);

        if (StringUtils.isNotEmpty(modelName)){
            tableConfiguration.setDomainObjectName(modelName);
            tableConfiguration.setMapperName(modelName + "Mapper");
        } else {
            tableConfiguration.setDomainObjectName(cleanPrefix(tableName));
            tableConfiguration.setMapperName(cleanPrefix(tableName) + "Mapper");
        }


        tableConfiguration.setGeneratedKey(new GeneratedKey("id", "Mysql", true, null));
        context.addTableConfiguration(tableConfiguration);

        List<String> warnings;
        MyBatisGenerator generator;
        try {
            Configuration config = new Configuration();
            config.addContext(context);
            config.validate();

            boolean overwrite = true;
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            warnings = new ArrayList<>();
            generator = new MyBatisGenerator(config, callback, warnings);
            generator.generate(null);
        } catch (Exception e) {
            throw new RuntimeException("生成Model和Mapper失败", e);
        }

        if (generator.getGeneratedJavaFiles().isEmpty() || generator.getGeneratedXmlFiles().isEmpty()) {
            throw new RuntimeException("生成Model和Mapper失败：" + warnings);
        }
        if (StringUtils.isEmpty(modelName))
            modelName = tableNameConvertUpperCamel(tableName);
        System.out.println(modelName + ".java 生成成功");
        System.out.println(modelName + "Mapper.java 生成成功");
        System.out.println(modelName + "Mapper.xml 生成成功");
    }

    public static void genService(String tableName, String modelName) {
        try {
            freemarker.template.Configuration cfg = getConfiguration();

            Map<String, Object> data = new HashMap<>();
            data.put("date", DATE);
            data.put("author", AUTHOR);
            String modelNameUpperCamel = StringUtils.isEmpty(modelName)
                    ? tableNameConvertUpperCamel(tableName) : modelName;
            data.put("modelNameUpperCamel", modelNameUpperCamel);
            data.put("modelNameLowerCamel", CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, modelNameUpperCamel));
            data.put("basePackage", BASE_PACKAGE);

            File file = new File(BASE_PROJECT_PATH + JAVA_PATH + PACKAGE_PATH_SERVICE + "I" + modelNameUpperCamel + "Service.java");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            cfg.getTemplate("service.ftl").process(data,
                    new FileWriter(file));
            System.out.println(modelNameUpperCamel + "Service.java 生成成功");

            File file1 = new File(BASE_PROJECT_PATH + JAVA_PATH + PACKAGE_PATH_SERVICE_IMPL + modelNameUpperCamel + "ServiceImpl.java");
            if (!file1.getParentFile().exists()) {
                file1.getParentFile().mkdirs();
            }
            cfg.getTemplate("service-impl.ftl").process(data,
                    new FileWriter(file1));
            System.out.println(modelNameUpperCamel + "ServiceImpl.java 生成成功");
        } catch (Exception e) {
            throw new RuntimeException("生成Service失败", e);
        }
    }

    public static void genMsController(String tableName, String modelName) {
        try {
            freemarker.template.Configuration cfg = getConfiguration();

            Map<String, Object> data = new HashMap<>();
            data.put("date", DATE);
            data.put("author", AUTHOR);
            String modelNameUpperCamel = StringUtils.isEmpty(modelName) ?
                    tableNameConvertUpperCamel(tableName) : modelName;
            data.put("baseRequestMapping", "manager/v1" + modelNameConvertMappingPath(modelNameUpperCamel));
            data.put("modelNameUpperCamel", modelNameUpperCamel);
            data.put("modelNameLowerCamel", CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, modelNameUpperCamel));
            data.put("basePackage", BASE_PACKAGE);

            File file = new File(BASE_PROJECT_PATH + JAVA_PATH + PACKAGE_PATH_CONTROLLER +
                    "manager/Manager" + modelNameUpperCamel + "Controller.java");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            //cfg.getTemplate("controller-restful.ftl").process(data, new FileWriter(file));
            cfg.getTemplate("ms-controller.ftl").process(data, new FileWriter(file));

            System.out.println("Manager" + modelNameUpperCamel + "Controller.java 生成成功");
        } catch (Exception e) {
            throw new RuntimeException("生成Ms-Controller失败", e);
        }

    }

    public static void genFsController(String tableName, String modelName) {
        try {
            freemarker.template.Configuration cfg = getConfiguration();

            Map<String, Object> data = new HashMap<>();
            data.put("date", DATE);
            data.put("author", AUTHOR);
            String modelNameUpperCamel = StringUtils.isEmpty(modelName) ?
                    tableNameConvertUpperCamel(tableName) : modelName;
            data.put("baseRequestMapping", "frontend/v1" +modelNameConvertMappingPath(modelNameUpperCamel));
            data.put("modelNameUpperCamel", modelNameUpperCamel);
            data.put("modelNameLowerCamel", CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, modelNameUpperCamel));
            data.put("basePackage", BASE_PACKAGE);

            File file = new File(BASE_PROJECT_PATH + JAVA_PATH + PACKAGE_PATH_CONTROLLER +
                    "frontend/Frontend" + modelNameUpperCamel + "Controller.java");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            //cfg.getTemplate("controller-restful.ftl").process(data, new FileWriter(file));
            cfg.getTemplate("fs-controller.ftl").process(data, new FileWriter(file));

            System.out.println("Frontend" +modelNameUpperCamel + "Controller.java 生成成功");
        } catch (Exception e) {
            throw new RuntimeException("生成fs-ontroller失败", e);
        }

    }

    public static void genRpcController(String tableName, String modelName) {
        try {
            freemarker.template.Configuration cfg = getConfiguration();

            Map<String, Object> data = new HashMap<>();
            data.put("date", DATE);
            data.put("author", AUTHOR);
            String modelNameUpperCamel = StringUtils.isEmpty(modelName) ?
                    tableNameConvertUpperCamel(tableName) : modelName;
            data.put("baseRequestMapping", "rpc/v1" + modelNameConvertMappingPath(modelNameUpperCamel));
            data.put("modelNameUpperCamel", modelNameUpperCamel);
            data.put("modelNameLowerCamel", CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, modelNameUpperCamel));
            data.put("basePackage", BASE_PACKAGE);

            File file = new File(BASE_PROJECT_PATH + JAVA_PATH + PACKAGE_PATH_CONTROLLER +
                    "rpc/Rpc" + modelNameUpperCamel + "Controller.java");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            //cfg.getTemplate("controller-restful.ftl").process(data, new FileWriter(file));
            cfg.getTemplate("rpc-controller.ftl").process(data, new FileWriter(file));

            System.out.println("Rpc" + modelNameUpperCamel + "Controller.java 生成成功");
        } catch (Exception e) {
            throw new RuntimeException("生成Rpc=Controller失败", e);
        }

    }

    private static freemarker.template.Configuration getConfiguration() throws IOException {
        freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_23);
        cfg.setDirectoryForTemplateLoading(new File(TEMPLATE_FILE_PATH));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        return cfg;
    }

    private static String tableNameConvertLowerCamel(String tableName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, tableName.toLowerCase());
    }

    private static String tableNameConvertUpperCamel(String tableName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName.toLowerCase());

    }

    private static String tableNameConvertMappingPath(String tableName) {
        tableName = tableName.toLowerCase();//兼容使用大写的表名
        return "/" + (tableName.contains("_") ? tableName.replaceAll("_", "/") : tableName);
    }

    private static String modelNameConvertMappingPath(String modelName) {
        String tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, modelName);
        return tableNameConvertMappingPath(tableName);
    }

    private static String packageConvertPath(String packageName) {
        return String.format("/%s/", packageName.contains(".") ? packageName.replaceAll("\\.", "/") : packageName);
    }

}