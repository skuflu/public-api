package se.centevo.jdbc;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SQLServerDataTableConverter<T> {
    private final JdbcClient jdbcClient;
    private final String sqlServerTableTypeName;
    private final List<T> rows;
    
    public SQLServerDataTable convert() {
        SQLServerDataTable insertTransactionsParameter;
            try {
                insertTransactionsParameter = createInputDataTable(sqlServerTableTypeName);
            } catch (SQLServerException e) {
                throw new RuntimeException(e);
            }
            
        for(var row : rows) {
            var listOfFields = new ArrayList<Object>();
            BeanWrapper propertyAccessor = PropertyAccessorFactory.forBeanPropertyAccess(row);
            for(var entry : insertTransactionsParameter.getColumnMetadata().entrySet().stream().filter(e -> !e.getValue().getColumnName().equalsIgnoreCase("itemIndex")).sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey())).toList()) {
                listOfFields.add(propertyAccessor.getPropertyValue(StringUtils.uncapitalize(entry.getValue().getColumnName())));
            }
            
            try {
                listOfFields.add(0, Integer.valueOf(rows.indexOf(row)));
                insertTransactionsParameter.addRow(listOfFields.toArray());
            } catch (SQLServerException e) {
                throw new RuntimeException(e);
            }
        }

        return insertTransactionsParameter;
    }

   private SQLServerDataTable createInputDataTable(String tvpName) throws SQLServerException {
        SQLServerDataTable inputDataTable = new SQLServerDataTable();
        inputDataTable.setTvpName("PublicApi.InsertTransactionsParameter");
    
        var columnDefinitions = jdbcClient.sql("""
                SELECT
                    schemas.name AS schema_name,
                    table_types.name AS table_type_name,
                    columns.name AS column_name,
                    columns.column_id,
                    types.name AS type_name,
                    columns.max_length,
                    columns.precision,
                    columns.scale,
                    columns.collation_name,
                    columns.is_nullable
                FROM sys.columns
                INNER JOIN sys.table_types
                ON columns.object_id = table_types.type_table_object_id
                INNER JOIN sys.types
                ON types.user_type_id = columns.user_type_id
                INNER JOIN sys.schemas
                ON schemas.schema_id = table_types.schema_id
                WHERE schemas.name = ?
                AND table_types.name = ?
                ORDER BY
                    table_types.name,
                    columns.column_id
                """).params(Arrays.asList(tvpName.split("\\."))).query().listOfRows();

               for(var columnDefinition : columnDefinitions) {
                    inputDataTable.addColumnMetadata(columnDefinition.get("column_name").toString(), convertType(columnDefinition.get("type_name").toString()));
               }


        return inputDataTable;
   }

   private int convertType(String type) {
        return switch (type) {
            case "int" -> Types.INTEGER;
            case "varchar" -> Types.VARCHAR;
            case "date" -> Types.DATE;
            case "numeric" -> Types.NUMERIC;
            case "bit" -> Types.BIT;
            case "datetime" -> Types.TIMESTAMP;
            default -> throw new RuntimeException("Data type not supported:" + type);
        };
   }
}