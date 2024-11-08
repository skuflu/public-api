--liquibase formatted sql

--changeset seku:broker_vw runOnChange:true endDelimiter:GO
DROP VIEW IF EXISTS PublicApi.Broker ;
GO
CREATE OR ALTER VIEW PublicApi.Broker
AS
	SELECT 
		Broker.BrokerId,
		BrokerParent.Code AS BrokerParentCode,
		Broker.Code,
		Broker.Description,
		FinancialInstitution.Code AS FinancialInstitutionCode,
		Broker.LEI,
		Broker.CanUpdatePortfolioOrder,
		
		Broker.VersionId,
		Broker.CreateDate,
		Broker.UpdateDate
	FROM Core.Broker
	LEFT JOIN Core.Broker BrokerParent
	ON BrokerParent.BrokerId = Broker.BrokerId
	LEFT JOIN Core.FinancialInstitution
	ON FinancialInstitution.FinancialInstitutionId = Broker.FinancialInstitutionId
;
GO
EXEC PublicApi.GenerateGenericTriggers 'Broker';
