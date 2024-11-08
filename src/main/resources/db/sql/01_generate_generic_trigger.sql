--liquibase formatted sql

--changeset seku:generate_generic_triggers runOnChange:true endDelimiter:GO

CREATE OR ALTER PROCEDURE PublicApi.GenerateGenericTriggers
	@ViewName VARCHAR(256)
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
		schema_name,
		view_name,
		column_id,
		column_name,
		CASE 
			WHEN column_name = 'CurrencyCode' THEN 'Instrument'
			WHEN column_name = 'ModelCode' THEN 'Portfolio'
			WHEN column_name = 'OmniAccountCode' THEN 'Account'
			WHEN column_name = 'SwiftCode' THEN 'SwiftCode'
			WHEN column_name = 'CitizenshipRegionCode' THEN 'Region'
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
		CONCAT('LEFT JOIN Core.', fk_table, ' ', view_columns.column_name, CHAR(13)+CHAR(10),
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
			CONCAT('DECLARE @ErrorMessage VARCHAR(MAX);', CHAR(13)+CHAR(10),
			'WITH CTE_Validation AS (', CHAR(13)+CHAR(10),
			'SELECT', CHAR(13)+CHAR(10),
				'ErrorMessage = ', CHAR(13)+CHAR(10),
					STRING_AGG(CONVERT(NVARCHAR(MAX),
					IIF(view_columns.column_name = 'Code', CONCAT('IIF(', object_name(tables.object_id) + '.Code IS NULL AND inserted.', view_columns.view_name ,'Id IS NOT NULL, ''Code already exist'', '''')'),
					CONCAT('IIF(ValidationRule.Faulty', view_columns.column_name, ' = 1, ''', view_columns.column_name, ' doesn''''t exist''', ', '''')'))), ' +' + CHAR(13)+CHAR(10))  WITHIN GROUP (ORDER BY view_columns.column_id)
					+  CHAR(13)+CHAR(10),
				'FROM inserted', CHAR(13)+CHAR(10),
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
			'SELECT', CHAR(13)+CHAR(10),
			'	@ErrorMessage = NULLIF(CONVERT(VARCHAR(1000), CTE_Validation.ErrorMessage), '''')', CHAR(13)+CHAR(10),
			'FROM CTE_Validation', CHAR(13)+CHAR(10),
			'IF @ErrorMessage IS NOT NULL', CHAR(13)+CHAR(10),
			'RAISERROR(@ErrorMessage, 16, 1) WITH SETERROR', CHAR(13)+CHAR(10)
		), 3 as sortOrder
		FROM view_columns
		WHERE view_columns.column_name LIKE '_%Code'
	) sql
), sql_stubbs(sql, stub, sortOrder) as (
	SELECT DISTINCT 
	CONCAT('CREATE OR ALTER TRIGGER PublicApi.PublicApi_' + view_columns.view_name + '_Insert_Trigger', CHAR(13)+CHAR(10),
	'ON PublicApi.' , view_columns.view_name , CHAR(13)+CHAR(10),
	'INSTEAD OF INSERT AS', CHAR(13)+CHAR(10),
	'BEGIN', CHAR(13)+CHAR(10)
	) AS header, 'Insert_Trigger', 1 as sortOrder
	FROM view_columns
	UNION
	SELECT sql AS header, 'Insert_Trigger', 2 as sortOrder
	FROM validationCTE
	UNION

	/*
	;WITH CTE_Validation AS (
		SELECT
			Code = COALESCE(InsertExchange.ExchangeCode, ''),
			ValidationErrorMessage = 
				IIF(ValidationRule.EmptyExchange = 1, 'ExchangeCode can''t be empty,', '')
				+
				IIF(ValidationRule.ExistingExchange = 1, 'ExchangeCode already exists, ' + InsertExchange.ExchangeCode+ ', ', '')
				+
				IIF(ValidationRule.FaultyRegion = 1, 'RegionCode doesn''t exist: ' + InsertExchange.RegionCode + ', ', '')
				+
				IIF(ValidationRule.FaultyCalendarType = 1, 'CalendarTypeCode doesn''t exist: ' + InsertExchange.CalendarTypeCode, '')
		FROM @InsertExchange InsertExchange
		LEFT JOIN Core.Exchange
		ON Exchange.Code = InsertExchange.ExchangeCode
		LEFT JOIN Core.Region
		ON Region.Code = InsertExchange.RegionCode
		LEFT JOIN Core.CalendarType
		ON CalendarType.Code = InsertExchange.CalendarTypeCode
		CROSS APPLY(
			SELECT
				IIF(InsertExchange.ExchangeCode IS NULL, 1, 0) AS EmptyExchange,
				IIF(InsertExchange.ExchangeCode = Exchange.Code, 1, 0) AS ExistingExchange,
				IIF(Region.RegionId IS NULL, 1, 0) AS FaultyRegion,
				IIF(CalendarType.CalendarTypeId IS NULL, 1, 0) AS FaultyCalendarType
		)ValidationRule
		WHERE ValidationRule.EmptyExchange = 1
		OR ValidationRule.ExistingExchange = 1
		OR ValidationRule.FaultyRegion = 1
		OR ValidationRule.FaultyCalendarType = 1
	)

	SELECT
		@ErrorString = CONVERT(VARCHAR(1000), CTE_Validation.ValidationErrorMessage)
	FROM CTE_Validation
	*/
	SELECT
		CONCAT('INSERT INTO ', schema_name(tables.schema_id), '.', object_name(tables.object_id), ' (', CHAR(13)+CHAR(10),
			STRING_AGG(CONVERT(NVARCHAR(MAX), CHAR(9) + columns.name), ',' + CHAR(13)+CHAR(10)) WITHIN GROUP (ORDER BY view_columns.column_id), CHAR(13)+CHAR(10),
		')', CHAR(13)+CHAR(10)
		), 'Insert_Trigger', 4 as sortOrder
	FROM sys.columns
	INNER JOIN sys.tables
	on tables.object_id = columns.object_id
	inner join view_columns
	on columns.name = 
		IIF(view_columns.column_name LIKE '_%Code',  
			CASE WHEN view_columns.column_name = 'SwiftCode' THEN 'SwiftCodeId'
			ELSE REPLACE(REPLACE(view_columns.column_name, 'Code', ''), 'Parent', '') + IIF(view_columns.column_name = 'BookValueMethodCode', '', 'Id')
			END
		, view_columns.column_name)
	WHERE schema_name(tables.schema_id) = 'Core'
	AND tables.name = view_columns.view_name
	GROUP BY schema_name(tables.schema_id),
		object_name(tables.object_id)
	UNION
	SELECT
		CONCAT('SELECT',  CHAR(13)+CHAR(10),
			STRING_AGG(CONVERT(NVARCHAR(MAX),
				CONCAT(CHAR(9),
					IIF(view_columns.column_name LIKE '_%Code', 
						CONCAT(view_columns.column_name, '.', fk_table, 'Id'),
						CONCAT('inserted', '.', view_columns.column_name)
					))), ',' + CHAR(13)+CHAR(10)
			) WITHIN GROUP (ORDER BY view_columns.column_id), CHAR(13)+CHAR(10),
		'FROM inserted'
		), 'Insert_Trigger', 5 as sortOrder
	from view_columns
	union all
	select *, 'Insert_Trigger', 6 as sortOrder
	from join_clauses
	UNION ALL
	SELECT CONCAT('SELECT SCOPE_IDENTITY()', CHAR(13)+CHAR(10),
	'END', CHAR(13)+CHAR(10),
	'GO'
	), 'Insert_Trigger', 7 as sortOrder
	UNION
	SELECT DISTINCT CONCAT('CREATE OR ALTER TRIGGER PublicApi.PublicApi_', view_columns.view_name, '_UPDATE_Trigger', CHAR(13)+CHAR(10),
	'ON PublicApi.',  view_columns.view_name, CHAR(13)+CHAR(10),
	'INSTEAD OF UPDATE AS', CHAR(13)+CHAR(10),
	'BEGIN', CHAR(13)+CHAR(10)) AS header, 'UPDATE_Trigger', 1 as sortOrder
	FROM view_columns
	UNION
	SELECT sql AS header, 'UPDATE_Trigger', 2 as sortOrder
	FROM validationCTE
	UNION
	SELECT
		CONCAT('UPDATE ', object_name(tables.object_id), CHAR(13)+CHAR(10),
			CHAR(9), 'SET ', CHAR(13)+CHAR(10),
			STRING_AGG(CONVERT(NVARCHAR(MAX),CONCAT(CHAR(9), CHAR(9), columns.name, ' = ', 
					IIF(view_columns.column_name LIKE '_%Code', 
						CONCAT(view_columns.column_name, '.', fk_table, 'Id'),
						CONCAT('inserted', '.', view_columns.column_name)
					))), ',' + CHAR(13)+CHAR(10)
			) WITHIN GROUP (ORDER BY view_columns.column_id), CHAR(13)+CHAR(10),
		'FROM inserted', CHAR(13)+CHAR(10),
		'INNER JOIN ', schema_name(tables.schema_id), '.', object_name(tables.object_id), CHAR(13)+CHAR(10),
		'ON ', object_name(tables.object_id), '.', object_name(tables.object_id), 'Id', ' = inserted.', object_name(tables.object_id), 'Id'
		), 'UPDATE_Trigger', 3 as sortOrder
	FROM sys.columns
	INNER JOIN sys.tables
	on tables.object_id = columns.object_id
	inner join view_columns
	on columns.name = 
		IIF(view_columns.column_name LIKE '_%Code',  
			CASE WHEN view_columns.column_name = 'SwiftCode' THEN 'SwiftCodeId'
			ELSE 
			IIF(view_columns.column_name = object_name(tables.object_id) + 'ParentCode', 'ParentId' ,
				REPLACE(REPLACE(view_columns.column_name, 'Code', ''), 'Parent', '') + IIF(view_columns.column_name = 'BookValueMethodCode', '', 'Id')
			)
			END
		, view_columns.column_name)
	WHERE schema_name(tables.schema_id) = 'Core'
	AND tables.name = view_columns.view_name
	GROUP BY schema_name(tables.schema_id),
		object_name(tables.object_id)
	union all
	select *, 'UPDATE_Trigger', 4 as sortOrder
	from join_clauses
	UNION ALL
	SELECT CONCAT('END', CHAR(13)+CHAR(10),
	'GO'
	), 'UPDATE_Trigger', 5 as sortOrder
	UNION
	SELECT DISTINCT CONCAT('CREATE OR ALTER TRIGGER PublicApi.PublicApi_', view_columns.view_name, '_DELETE_Trigger', CHAR(13)+CHAR(10),
	'ON PublicApi.',  view_columns.view_name, CHAR(13)+CHAR(10),
	'INSTEAD OF DELETE AS', CHAR(13)+CHAR(10),
	'BEGIN', CHAR(13)+CHAR(10)) AS header, 'DELETE_Trigger', 1 as sortOrder
	FROM view_columns
	UNION
	SELECT 
		CONCAT('DELETE Core.', @ViewName, CHAR(13)+CHAR(10),  
		'FROM Core.', @ViewName, CHAR(13)+CHAR(10),
		'INNER JOIN deleted', CHAR(13)+CHAR(10),
		'ON deleted.', @ViewName, 'Id = ', @ViewName, '.', @ViewName, 'Id', CHAR(13)+CHAR(10),
		'END', CHAR(13)+CHAR(10),
		'GO'), 'DELETE_Trigger', 2 as sortOrder
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
	--EXEC Core.spUtitlPrintMax @PrintableSQL --See the command used (will be truncated in Select/Print, but not when Executing).
	EXEC (@SQL);
	SET @Counter = @Counter + 1
END

--SET @SQL = 'EXEC (''' + REPLACE(REPLACE(@SQL, '''', ''''''), 'GO', '''); EXEC(''') + ''');'--Just add this one line.
--PRINT @SQL --See the command used (will be truncated in Select/Print, but not when Executing).
----EXEC (@SQL);

GO
