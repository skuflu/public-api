--liquibase formatted sql

--changeset seku:generate_generic_triggers runOnChange:true endDelimiter:GO

CREATE OR ALTER PROCEDURE PublicApi.GenerateBatchValidation
	@ViewName VARCHAR(256),
	@ValidationTempTable VARCHAR(256),
	@UniqueColumnName VARCHAR(256),
	@ErrorColumn VARCHAR(256)
AS

SET NOCOUNT ON;

IF @ViewName IS NULL
	RETURN;

	
DECLARE @SQL NVARCHAR(MAX),
	@PrintableSQL NVARCHAR(MAX);

WITH raw_view_columns AS (
	select schema_name(v.schema_id) as schema_name,
		   object_name(c.object_id) as view_name,
		   c.column_id,
		   c.name as column_name,
		   type_name(user_type_id) as data_type,
		   c.max_length,
		   c.precision,
		   c.is_nullable
	from sys.columns c
	join sys.views v 
		 on v.object_id = c.object_id
	where schema_name(v.schema_id) = 'PublicApi'
	AND object_name(c.object_id) = @ViewName
	AND c.name <> @ViewName + 'Id'
	AND c.name <> 'LastModifiedDate'
), view_columns AS (
	SELECT
		CASE 
			WHEN column_name = 'FourEyeApproverCode' THEN 'SystemSecurity'
			ELSE 'Core'
		END AS schema_name,
		view_name,
		column_id,
		column_name,
		CASE 
			WHEN column_name = 'CurrencyCode' THEN 'Instrument'
			WHEN column_name = 'ModelCode' THEN 'Portfolio'
			WHEN column_name = 'OmniAccountCode' THEN 'Account'
			WHEN column_name = 'SwiftCode' THEN 'SwiftCode'
			WHEN column_name = 'CitizenshipRegionCode' THEN 'Region'
			WHEN column_name = 'CashAccountCode' THEN 'Account'
			WHEN column_name = 'ReferenceAccountCode' THEN 'Account'
			WHEN column_name = 'ReferenceInstrumentCode' THEN 'Instrument'
			WHEN column_name = 'FourEyeApproverCode' THEN 'SystemUser'
			ELSE REPLACE(REPLACE(raw_view_columns.column_name, 'Code', ''), 'Parent', '')
		END AS fk_table,
		data_type,
		max_length,
		precision,
		is_nullable
	FROM raw_view_columns
), join_clauses AS (
	SELECT
		STRING_AGG(CONVERT(NVARCHAR(MAX),
		CASE WHEN view_columns.column_name LIKE '_%Code' THEN
		CONCAT('LEFT JOIN ', schema_name ,'.', fk_table, ' ', view_columns.column_name, CHAR(13)+CHAR(10),
			'ON ',  view_columns.column_name, '.Code', ' = inserted.', view_columns.column_name
		)
		END)
		, CHAR(13)+CHAR(10)) WITHIN GROUP (ORDER BY view_columns.column_id) as clause
	FROM view_columns
), validationCTE AS (
	SELECT
		STRING_AGG(CONVERT(NVARCHAR(MAX), sql), CHAR(13)+CHAR(10)) WITHIN GROUP (ORDER BY sortOrder) AS sql
	FROM (
		SELECT
			CONCAT('WITH CTE_Validation AS (', CHAR(13)+CHAR(10),
			'SELECT', CHAR(13)+CHAR(10),
				'inserted.', @UniqueColumnName, ',', CHAR(13)+CHAR(10),
				'ErrorMessage = CONCAT_WS('', '', ', CHAR(13)+CHAR(10),
					STRING_AGG(CONVERT(NVARCHAR(MAX),
					IIF(view_columns.column_name = 'Code', CONCAT('IIF(', object_name(tables.object_id) + '.Code IS NULL AND inserted.', view_columns.view_name ,'Id IS NOT NULL, ''Code already exist'', '''')'),
					CONCAT('IIF(ValidationRule.Faulty', view_columns.column_name, ' = 1, ''', view_columns.column_name, ' doesn''''t exist''', ', NULL)'))), ',' + CHAR(13)+CHAR(10))  WITHIN GROUP (ORDER BY view_columns.column_id)
					, CHAR(13)+CHAR(10),
					')', CHAR(13)+CHAR(10),
				'FROM ', @ValidationTempTable , ' inserted', CHAR(13)+CHAR(10),
				IIF(EXISTS (SELECT TOP 1 1 FROM view_columns WHERE column_name = 'Code'), 
				CONCAT('LEFT JOIN ', schema_name(tables.schema_id), '.', object_name(tables.object_id), CHAR(13)+CHAR(10),
				'ON ', object_name(tables.object_id), '.Code', ' = inserted.Code')
				,'')
				) AS sql, 1 as sortOrder
		FROM sys.columns
		INNER JOIN sys.tables
		on tables.object_id = columns.object_id
		inner join view_columns
		on columns.name = IIF(view_columns.column_name LIKE '_%Code',  fk_table + 'Id', view_columns.column_name)
		WHERE schema_name(tables.schema_id) = 'Core'
		AND tables.name = view_columns.view_name
		AND view_columns.column_name LIKE '%Code'
		GROUP BY schema_name(tables.schema_id),
			object_name(tables.object_id)
		UNION
		select *, 2 as sortOrder
		from join_clauses
		UNION
		SELECT
			CONCAT('CROSS APPLY(', CHAR(13)+CHAR(10),
				'SELECT', CHAR(13)+CHAR(10),
					STRING_AGG(CONVERT(NVARCHAR(MAX), CONCAT('IIF(inserted.', view_columns.column_name, ' IS NOT NULL AND ' + CONCAT(view_columns.column_name, '.', fk_table, 'Id'), ' IS NULL, 1, 0) AS Faulty', view_columns.column_name)), ',' + CHAR(13)+CHAR(10))  WITHIN GROUP (ORDER BY view_columns.column_id), CHAR(13)+CHAR(10),
			') ValidationRule', CHAR(13)+CHAR(10),
			'WHERE 1 = 1', CHAR(13)+CHAR(10),
			STRING_AGG(CONVERT(NVARCHAR(MAX),CONCAT('OR ValidationRule.Faulty', view_columns.column_name, ' = 1')), CHAR(13)+CHAR(10))  WITHIN GROUP (ORDER BY view_columns.column_id), CHAR(13)+CHAR(10),
			')', CHAR(13)+CHAR(10),
			'UPDATE ', @ValidationTempTable , CHAR(13)+CHAR(10),
			'	SET ', @ErrorColumn, ' = NULLIF(CONVERT(VARCHAR(1000), CTE_Validation.ErrorMessage), '''')', CHAR(13)+CHAR(10),
			'FROM CTE_Validation', CHAR(13)+CHAR(10),
			'INNER JOIN ', @ValidationTempTable, CHAR(13)+CHAR(10),
			'ON ', @ValidationTempTable, '.', @UniqueColumnName, ' = CTE_Validation.', @UniqueColumnName, CHAR(13)+CHAR(10)
		), 3 as sortOrder
		FROM view_columns
		WHERE view_columns.column_name LIKE '_%Code'
	) sql
), sql_stubbs(sql, stub, sortOrder) as (
	SELECT DISTINCT 
	CONCAT('CREATE OR ALTER PROCEDURE PublicApi.spValidate' + view_columns.view_name, CHAR(13)+CHAR(10),
	'AS', CHAR(13)+CHAR(10),
	'SET NOCOUNT ON;', CHAR(13)+CHAR(10)
	) AS header, 'spValidate', 1 as sortOrder
	FROM view_columns
	UNION
	SELECT sql AS header, 'spValidate', 2 as sortOrder
	FROM validationCTE
	UNION
	SELECT 'GO', 'spValidate', 3 as sortOrder
)

SELECT 
	ROW_NUMBER() over (order by (select null)) as counter,
	sql = STRING_AGG(sql, CHAR(13)+CHAR(10)) WITHIN GROUP (ORDER BY sortOrder) 
INTO #Sql
FROM sql_stubbs 
GROUP BY stub;

declare @Counter int = 1;

while @Counter <= (SELECT MAX(counter) FROM #sql)
BEGIN
	SET @SQL = 'EXEC (''' + REPLACE(REPLACE((select sql from #sql where counter = @counter), '''', ''''''), 'GO', '''); EXEC(''') + ''');'--Just add this one line.
	SET @PrintableSQL = (select sql from #sql where counter = @counter)
	--EXEC Core.spUtilPrintMax @PrintableSQL --See the command used (will be truncated in Select/Print, but not when Executing).
	EXEC (@SQL);
	SET @Counter = @Counter + 1
END

--SET @SQL = 'EXEC (''' + REPLACE(REPLACE(@SQL, '''', ''''''), 'GO', '''); EXEC(''') + ''');'--Just add this one line.
--PRINT @SQL --See the command used (will be truncated in Select/Print, but not when Executing).
----EXEC (@SQL);

GO