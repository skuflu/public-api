--liquibase formatted sql

--changeset seku:exchange_vw runOnChange:true endDelimiter:GO
DROP VIEW IF EXISTS PublicApi.Exchange ;
GO
CREATE OR ALTER VIEW PublicApi.Exchange
AS
	SELECT 
		Exchange.ExchangeId,
		ExchangeParent.Code AS ExchangeParentCode,
		Exchange.Code,
		Exchange.Description,
		Region.Code AS RegionCode,
		CalendarType.Code AS CalendarTypeCode,
		SwiftCode.Code AS SwiftCode,
		Exchange.Mic,
		Exchange.CreateDate,
		Exchange.UpdateDate,
		Exchange.VersionId
	FROM Core.Exchange
	LEFT JOIN Core.Exchange ExchangeParent
	ON ExchangeParent.ExchangeId = Exchange.ExchangeId
	INNER JOIN Core.Region
	ON Region.RegionId = Exchange.RegionId
	INNER JOIN Core.CalendarType
	ON CalendarType.CalendarTypeId = Exchange.CalendarTypeId
	LEFT JOIN Core.SwiftCode
	ON SwiftCode.Code = Exchange.SwiftCodeId
;
GO
EXEC PublicApi.GenerateGenericTriggers 'Exchange';
