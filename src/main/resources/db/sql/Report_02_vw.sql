--liquibase formatted sql

--changeset seku:report_vw runOnChange:true
DROP VIEW IF EXISTS PublicApi.Report;
GO
CREATE OR ALTER VIEW PublicApi.Report
AS
	SELECT 
		BatchExecutionResultsCustomer.BatchExecutionResultId AS ReportId,
		BatchExecutionResultsCustomer.StepDescription AS Description,
		BatchExecutionResultsCustomer.FileName,
		BatchExecutionResultsCustomer.FilePath,
		COALESCE(BatchExecutionResultsCustomer.UpdateDate, BatchExecutionResultsCustomer.CreateDate) AS LastModifiedDate,
		BatchExecutionResultsCustomer.VersionId AS Version
	FROM core.BatchExecutionResultsCustomer