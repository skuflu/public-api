package se.centevo.generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class RepositoryGenerator {
    private final static String repositorySql = """
            select schema_name(v.schema_id) as schema_name,
                object_name(c.object_id) as view_name,
                c.column_id,
                c.name as column_name,
                type_name(user_type_id) as data_type,
                c.max_length,
                c.precision,
                c.scale,
                c.is_nullable
            from sys.columns c
            join sys.views v
            	 on v.object_id = c.object_id
            where schema_name(v.schema_id) = 'PublicApi'
            and c.name <> object_name(c.object_id) + 'Id'
            and c.name <> 'lastModifiedDate'
            and c.name not in ('Version', 'VersionId')
            and object_name(c.object_id) NOT IN ('Transactions', 'TransactionsChildren', 'Report', 'Account', 'AccountOwner', 'AccountOmniAccount', 'AccountOwner', 'AccountPortfolioMapping')
            order by schema_name,
                view_name,
                column_id
                       """;

    private final static String referenceObjectSql = """
                SELECT 	distinct
                    schema_name(tables.schema_id)  as schema_name,
                    tables.name as view_name,
                    columns.column_id,
                    columns.name as column_name,
                    type_name(user_type_id) as data_type,
                    columns.max_length,
                    columns.precision,
                    columns.scale,
                    columns.is_nullable

                FROM sys.views
                INNER JOIN sys.sql_dependencies
                on sql_dependencies.object_id = views.object_id
                INNER JOIN sys.tables
                ON tables.object_id = sql_dependencies.referenced_major_id
                inner join sys.columns
                on columns.object_id = tables.object_id
                LEFT JOIN sys.views AlreadyExistingPublicView
                ON AlreadyExistingPublicView.name = OBJECT_NAME(referenced_major_id)
                where schema_name(views.schema_id) = 'PublicApi'
                AND AlreadyExistingPublicView.object_id is null
                AND columns.name in ('code', 'description')
                order by schema_name,
                    view_name,
                    column_id
                        """;

    // private final static String targetDirectory = "target/generated-sources/se/centevo/endpoint/";
    private final static String targetDirectory = "src/main/java/se/centevo/endpoint/";

    public static void main(String[] args) throws IOException, SQLException {
        new Generator(repositorySql, targetDirectory, "PublicReadWriteRepositories.java", true).generate();
        new Generator(referenceObjectSql, targetDirectory, "PublicReadRepositories.java", false).generate();
    }
}

@AllArgsConstructor
class Generator {
    String sql;
    String targetDirectory;
    String singleFileNameIfYouWantOne;
    boolean editable;

    void generate() throws SQLException, IOException {
        List<Map<String, Object>> viewWithColumns = List.of();

        try (Connection conn = DriverManager
                .getConnection(
                        "jdbc:sqlserver://localhost:1433;databaseName=AnonPeak20240423;encrypt=true;trustServerCertificate=true;",
                        "cairo", "apskit")) {

            try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
                viewWithColumns = resultSetToList(rs);
            }
            // "0" means disabling the timeout, when doing isValid checks
            boolean isValid = conn.isValid(0);
            System.out.println("Do we have a valid db connection? = " + isValid);
        }



        List<String> views = viewWithColumns.stream().map(
                row -> row.get("view_name").toString()).distinct().toList();

        List<Entity> entities = new ArrayList<>();
        for (var view : views) {
            List<Map<String, Object>> columnRows = viewWithColumns.stream()
                    .filter(item -> item.get("view_name").equals(view))
                    .sorted((item1, item2) -> Integer.valueOf(item1.get("column_id").toString())
                            .compareTo(Integer.valueOf(item2.get("column_id").toString())))
                    .toList();

            var columns = columnRows.stream().map(row -> {
                var dataType = row.get("data_type").toString();
                var type = switch (dataType) {
                    case "int", "tinyint" -> "Integer";
                    case "varchar", "nvarchar", "char" -> "String";
                    case "date" -> "LocalDate";
                    case "numeric" -> "BigDecimal";
                    case "bit" -> "boolean";
                    case "datetime" -> "LocalDateTime";
                    default -> throw new RuntimeException("Data type not supported:" + dataType + " for column" + row.get("column_name") + " in view " + view);
                };

                List<String> validationAnnotations = new ArrayList<>();
                var sizeAnnotation = switch (type) {
                    case "String" -> {
                        if (Integer.valueOf(row.get("max_length").toString()) != -1)
                            yield "@Size(min = 1, max = " + Integer.valueOf(row.get("max_length").toString()) + ")";
                        else
                            yield "";
                    }
                    case "BigDecimal" -> "@Digits(integer = "
                            + (Integer.valueOf(row.get("precision").toString())
                                    - Integer.valueOf(row.get("scale").toString()))
                            + ", fraction = " + Integer.valueOf(row.get("scale").toString()) + ")";
                    default -> "";
                };

                var notNullAnnotation = !Boolean.parseBoolean(row.get("is_nullable").toString()) ? "@NotNull" : "";

                if (!sizeAnnotation.isBlank())
                    validationAnnotations.add(sizeAnnotation);
                if (!notNullAnnotation.isBlank())
                    validationAnnotations.add(notNullAnnotation);

                var columnName = row.get("column_name").toString();
                String fieldName = StringUtils.uncapitalize(row.get("column_name").toString());
                if(StringUtils.isAllUpperCase(columnName)) {
                    fieldName = StringUtils.lowerCase(columnName);
                }


                return new Column(type, fieldName, validationAnnotations, List.of());

            }).toList();

            var schema = columnRows.stream().map(row -> row.get("schema_name").toString()).findFirst().orElseThrow();
            var entity = new Entity(view, schema, editable, columns);
            entities.add(entity);
        }

        Path newDirectoryPath = Paths.get(targetDirectory);
        Files.createDirectories(newDirectoryPath);

        if(singleFileNameIfYouWantOne != null) {
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache m = mf.compile("single_repository.mustache");
            StringWriter writer = new StringWriter();
            Map<String, Object> context = new HashMap<>();
            context.put("entities", entities);
            m.execute(writer, context).flush();

            try(FileOutputStream fileOutputStream = new FileOutputStream(targetDirectory + singleFileNameIfYouWantOne)){
                fileOutputStream.write(writer.toString().getBytes());
            }
        } else {
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache m = mf.compile("multi_repository.mustache");
            for(var entity : entities) {
                StringWriter writer = new StringWriter();
                 m.execute(writer, entity).flush();
                 try(FileOutputStream fileOutputStream = new FileOutputStream(targetDirectory + entity.getCapitalizedEntityModelName() + "Repository.java")){
                
                    fileOutputStream.write(writer.toString().getBytes());
                }
            }
        }
    }

    private List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<String, Object>(columns);
            for (int i = 1; i <= columns; ++i) {
                row.put(md.getColumnName(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }
}

@AllArgsConstructor
class Entity {
    private String entityModelName;
    String schema;
    boolean editable;
    List<Column> columns;

    String getCapitalizedEntityModelName() {
        return entityModelName;
    }

    String getJavaPrimaryKeyFieldName() {
        return StringUtils.uncapitalize(entityModelName) + "Id";
    }
}

@Getter
@AllArgsConstructor
class Column {
    String javaDataType;
    String javaFieldName;
    List<String> validationAnnotations;
    List<String> jsonAnnotations;
}

@Builder
@Getter
class Todo {
    private String title;
    private String text;
    private boolean done;
    private Date createdOn;
    private Date completedOn;
}