/**
 * Copyright (c) 2015-2017, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.codegen.vomodel;

import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.generator.BaseModelGenerator;
import com.jfinal.plugin.activerecord.generator.MetaBuilder;
import com.jfinal.plugin.activerecord.generator.TableMeta;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.jboot.Jboot;
import io.jboot.config.JbootProperties;
import io.jboot.db.datasource.DatasourceConfig;
import io.jboot.utils.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JbootVoModeGenerator extends BaseModelGenerator {


    public static void main(String[] args) {

        Jboot.setBootArg("jboot.datasource.url", "jdbc:mysql://127.0.0.1:3306/jbootdemo");
        Jboot.setBootArg("jboot.datasource.user", "root");

        String modelPackage = "io.jboot.codegen.vomodel.test";
        run(modelPackage);

    }

    public static void run(String voModelPackage) {
        new JbootVoModeGenerator(voModelPackage).doGenerate(null);
    }

    public static void run(String voModelPackage, String excludeTables) {
        new JbootVoModeGenerator(voModelPackage).doGenerate(excludeTables);
    }


    public JbootVoModeGenerator(String basePackage) {
        super(basePackage, PathKit.getWebRootPath() + "/src/main/java/" + basePackage.replace(".", "/"));


        this.packageTemplate = "%n"
                + "package %s;%n%n";

        this.classDefineTemplate = "/**%n"
                + " * Auto generated, do not modify this file.%n"
                + " */%n"
                + "@SuppressWarnings(\"serial\")%n"
                + "public class %s extends JbootVoModel {%n%n"


        ;


        this.importTemplate = "import io.jboot.db.model.JbootVoModel;%n%n";
    }


    public void doGenerate(String excludeTables) {

        System.out.println("start generate...");
        DatasourceConfig datasourceConfig = JbootProperties.get("jboot.datasource", DatasourceConfig.class);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(datasourceConfig.getUrl());
        config.setUsername(datasourceConfig.getUser());
        config.setPassword(datasourceConfig.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setDriverClassName("com.mysql.jdbc.Driver");

        HikariDataSource dataSource = new HikariDataSource(config);

        List<TableMeta> tableMetaList = new MetaBuilder(dataSource).build();

        if (StringUtils.isNotBlank(excludeTables)) {
            List<TableMeta> newTableMetaList = new ArrayList<>();
            Set<String> excludeTableSet = StringUtils.splitToSet(excludeTables.toLowerCase(), ",");
            for (TableMeta tableMeta : tableMetaList) {
                if (excludeTableSet.contains(tableMeta.name.toLowerCase())) {
                    System.out.println("exclude table : " + tableMeta.name);
                    continue;
                }
                newTableMetaList.add(tableMeta);
            }
            tableMetaList = newTableMetaList;
        }

        generate(tableMetaList);

        System.out.println("generate finished !!!");

    }


    @Override
    protected void genClassDefine(TableMeta tableMeta, StringBuilder ret) {
        ret.append(String.format(classDefineTemplate,
                tableMeta.modelName + "Vo"));
    }


    /**
     * base model 覆盖写入
     */
    protected void writeToFile(TableMeta tableMeta) throws IOException {
        File dir = new File(baseModelOutputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String target = baseModelOutputDir + File.separator + tableMeta.modelName + "Vo" + ".java";
        FileWriter fw = new FileWriter(target);
        try {
            fw.write(tableMeta.baseModelContent);
        } finally {
            fw.close();
        }
    }
}
