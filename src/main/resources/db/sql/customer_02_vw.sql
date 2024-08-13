--liquibase formatted sql

--changeset seku:customer_vw runOnChange:true
DROP VIEW IF EXISTS PublicApi.Customer ;
GO
CREATE OR ALTER VIEW PublicApi.Customer
AS
	SELECT 
		Customer.CustomerId,
		Customer.Code,
		Customer.Description,
		Customer.FirstName,
		Customer.SurName,
		Customer.OrganizationId,
		CustomerType.Code AS CustomerTypeCode,
		Region.Code AS RegionCode,
		Manager.Code AS ManagerCode,
		Language.Code AS LanguageCode,
		TargetPortfolio.Code AS TargetPortfolioCode,
		TargetAccount.Code AS TargetAccountCode,
		Customer.VAT,
		Customer.TIN,
		MifidInvestorType.Code AS MifidInvestorTypeCode,
		MifidKnowledgeAndExperience.Code AS MifidKnowledgeAndExperienceCode,
		MifidAbilityToBearLosses.Code AS MifidAbilityToBearLossesCode,
		Customer.LastReportDate,
		Customer.MifidLastReportedDrawdown,
		Customer.Lei,
		Customer.DateOfBirth,
		Customer.StartDate,
		Customer.EndDate,
		CitizenshipRegion.Code AS CitizenshipRegionCode,
		Customer.PEPStatusDate,
		Contact.Address,
		Contact.PostalCode,
		Contact.City,
		Contact.PhoneNumber,
		Contact.Mobile,
		Contact.Email,
		COALESCE(Customer.UpdateDate, Customer.CreateDate) AS LastModifiedDate,
		Customer.VersionId AS Version
	FROM Core.Customer
	INNER JOIN Core.CustomerType
	ON CustomerType.CustomerTypeId = Customer.CustomerTypeId
	INNER JOIN Core.Region
	ON Region.RegionId = Customer.RegionId
	INNER JOIN Core.Language
	ON Language.LanguageId = Customer.LanguageId
	LEFT JOIN Core.Manager
	ON Manager.ManagerId = Customer.ManagerId
	LEFT JOIN Core.Region CitizenshipRegion
	ON CitizenshipRegion.RegionId = Customer.CitizenshipRegionId
	LEFT JOIN Core.Account TargetAccount
	ON TargetAccount.AccountId = Customer.TargetAccountId
	LEFT JOIN Core.Portfolio TargetPortfolio
	ON TargetPortfolio.PortfolioId = Customer.TargetPortfolioId
	LEFT JOIN Authority.MifidInvestorType
	ON MifidInvestorType.MifidInvestorTypeId = Customer.MifidInvestorTypeId
	LEFT JOIN Authority.MifidKnowledgeAndExperience
	ON MifidKnowledgeAndExperience.MifidKnowledgeAndExperienceId = Customer.MifidKnowledgeAndExperienceId
	LEFT JOIN Authority.MifidAbilityToBearLosses
	ON MifidAbilityToBearLosses.MifidAbilityToBearLossesId = Customer.MifidAbilityToBearLossesId	
	OUTER APPLY (
		SELECT TOP 1
			Address,
			PostalCode,
			City,
			PhoneNumber,
			Mobile,
			Email
		FROM Core.Contact
		INNER JOIN Core.ContactType
		ON ContactType.ContactTypeId = Contact.ContactTypeId
		WHERE Contact.DefaultContact = 1
		AND ObjectId = Customer.CustomerId
	) Contact;