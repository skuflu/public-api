--liquibase formatted sql

--changeset seku:manager_vw runOnChange:true endDelimiter:GO
DROP VIEW IF EXISTS PublicApi.Manager ;
GO
CREATE OR ALTER VIEW PublicApi.Manager
AS
	SELECT 
		Manager.ManagerId,
		Manager.Code,
		Manager.Description,
		Region.Code AS RegionCode,
		CitizenshipRegion.Code AS CitizenshipRegionCode,
		Manager.OrganizationId,
		Manager.Mobil,
		Manager.LEI,
		Manager.CreateDate,
		Manager.UpdateDate,
		Manager.VersionId
	FROM Core.Manager
	INNER JOIN Core.Region
	ON Region.RegionId = Manager.RegionId
	LEFT JOIN Core.Region CitizenshipRegion
	ON CitizenshipRegion.RegionId = Manager.CitizenshipRegionId
;
GO
EXEC PublicApi.GenerateGenericTriggers 'Manager';
