--liquibase formatted sql

--changeset seku:issuer_vw runOnChange:true endDelimiter:GO
DROP VIEW IF EXISTS PublicApi.Issuer ;
GO
CREATE OR ALTER VIEW PublicApi.Issuer
AS
	SELECT 
		Issuer.IssuerId,
		Issuer.Code,
		Issuer.Description,
		Region.Code AS RegionCode,
		FinancialInstitution.Code AS FinancialInstitutionCode,
		Issuer.OrganizationNumber,
		Issuer.LEI,
		Issuer.CreateDate,
		Issuer.UpdateDate,
		Issuer.VersionId
	FROM Core.Issuer
	INNER JOIN Core.Region
	ON Region.RegionId = Issuer.RegionId
	LEFT JOIN Core.FinancialInstitution
	ON FinancialInstitution.FinancialInstitutionId = Issuer.FinancialInstitutionId
;
GO
EXEC PublicApi.GenerateGenericTriggers 'Issuer';
