--liquibase formatted sql

--changeset seku:customer_trigger runOnChange:true
CREATE OR ALTER TRIGGER PublicApi.PublicApi_Customer_Insert_Trigger
ON PublicApi.Customer
INSTEAD OF INSERT AS
BEGIN
	DECLARE @CustomerId INT;

	INSERT INTO Core.Customer (
		Code,
		FirstName,
		SurName,
		OrganizationId,
		CustomerTypeId,
		RegionId,
		ManagerId,
		LanguageId,
		TargetPortfolioId,
		TargetAccountId,
		VAT,
		TIN,
		MifidInvestorTypeId,
		MifidKnowledgeAndExperienceId,
		MifidAbilityToBearLossesId,
		LastReportDate,
		MifidLastReportedDrawdown,
		LEI,
		DateOfBirth,
		StartDate,
		EndDate,
		CitizenshipRegionId,
		PEPStatusDate
	)
	SELECT
		inserted.code,
		inserted.FirstName,
		inserted.SurName,
		inserted.OrganizationId,
		(SELECT CustomerTypeId FROM Core.CustomerType WHERE CustomerType.Code = inserted.CustomerTypeCode),
		(SELECT RegionId FROM Core.Region WHERE Region.Code = inserted.RegionCode),
		(SELECT ManagerId FROM Core.Manager WHERE Manager.Code = inserted.ManagerCode),
		(SELECT LanguageId FROM Core.Language WHERE Language.Code = inserted.LanguageCode),
		(SELECT PortfolioId FROM Core.Portfolio WHERE Portfolio.Code = inserted.TargetPortfolioCode),
		(SELECT AccountId FROM Core.Account WHERE Account.Code = inserted.TargetAccountCode),
		inserted.VAT,
		inserted.TIN,
		(SELECT MifidInvestorTypeId FROM Authority.MifidInvestorType WHERE MifidInvestorType.Code = inserted.MifidInvestorTypeCode),
		(SELECT MifidKnowledgeAndExperienceId FROM Authority.MifidKnowledgeAndExperience WHERE MifidKnowledgeAndExperience.Code = inserted.MifidKnowledgeAndExperienceCode),
		(SELECT MifidAbilityToBearLossesId FROM Authority.MifidAbilityToBearLosses WHERE MifidAbilityToBearLosses.Code = inserted.MifidAbilityToBearLossesCode),
		inserted.LastReportDate,
		inserted.MifidLastReportedDrawdown,
		inserted.LEI,
		inserted.DateOfBirth,
		inserted.StartDate,
		inserted.EndDate,
		(SELECT RegionId FROM Core.Region WHERE Region.Code = inserted.CitizenshipRegionCode),
		inserted.PEPStatusDate
	FROM inserted;

	IF @@ROWCOUNT = 1
		SELECT @CustomerId = SCOPE_IDENTITY();

	INSERT INTO Core.Contact (
		ObjectId,
		ContactTypeId,
		Address,
		PostalCode,
		City,
		PhoneNumber,
		Mobile,
		Email,
		DefaultContact
	)
	SELECT
		Customer.CustomerId,
		(SELECT ContactTypeId FROM Core.ContactType WHERE ObjectType = 'CUSTOMER' AND DefaultContactType = 1),
		inserted.Address,
		inserted.PostalCode,
		inserted.City,
		inserted.PhoneNumber,
		inserted.Mobile,
		inserted.Email,
		DefaultContact = 1
	FROM inserted
	INNER JOIN Core.Customer
	ON Customer.Code = inserted.Code
	WHERE inserted.Address IS NOT NULL;

	IF @CustomerId IS NOT NULL
	BEGIN
		SELECT @CustomerId;
	END
END