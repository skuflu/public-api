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
		BatchExecutionResultsCustomer.CreateDate,
		BatchExecutionResultsCustomer.UpdateDate,
		BatchExecutionResultsCustomer.VersionId
	FROM core.BatchExecutionResultsCustomer