--liquibase formatted sql

--changeset seku:account_vw runOnChange:true endDelimiter:GO
DROP VIEW IF EXISTS PublicApi.Portfolio ;
GO
CREATE OR ALTER VIEW PublicApi.Portfolio
AS
	SELECT 
		Portfolio.PortfolioId,
		PortfolioParent.Code AS PortfolioParentCode,
		Portfolio.Code,
		Portfolio.Description,
		Currency.Code AS CurrencyCode,
		PortfolioType.Code AS PortfolioTypeCode,
		Manager.Code AS ManagerCode,
		

		Model.Code AS ModelCode,
		Portfolio.StartDate,
		Portfolio.EndDate,
		BookValueMethod.Code AS BookValueMethodCode,
		Portfolio.ScenarioId AS ScenarioCode,
		Portfolio.PerformanceStart,
		Portfolio.FullInvestedStart,
		Portfolio.Blocked,
		Portfolio.BlockedText,
		BenchmarkPortfolio.Code AS BenchmarkPortfolioCode,
		BenchmarkInstrument.Code AS BenchmarkInstrumentCode,
		Portfolio.Performance,
		Portfolio.ForceTransactions,
		Portfolio.LimitToCash,
		SavingsplanPortfolio.Code AS SavingsplanPortfolioCode,
		Portfolio.ExtendedDescription,
		InsuredCustomer.Code AS InsuredCustomerCode,
		InvestmentProfilePortfolio.Code AS InvestmentProfilePortfolioCode,
		TargetAccount.Code AS TargetAccountCode,
		MifidReturnProfile.Code AS MifidReturnProfileCode,
		MifidTimeHorizon.Code AS MifidTimeHorizonCode,
		MifidRiskTolerance.Code AS MifidRiskToleranceCode,
		MifidDistributionStrategy.Code AS MifidDistributionStrategyCode,
		DiscountTemplate.Code AS DiscountTemplateCode,
		Portfolio.CreateDate,
		Portfolio.UpdateDate,
		Portfolio.VersionId
	FROM Core.Portfolio
	LEFT JOIN Core.Portfolio PortfolioParent
	ON PortfolioParent.PortfolioId = Portfolio.PortfolioId
	INNER JOIN Core.Instrument Currency
	ON Currency.InstrumentId = Portfolio.CurrencyId
	INNER JOIN Core.PortfolioType
	ON PortfolioType.PortfolioTypeId = Portfolio.PortfolioTypeId
	LEFT JOIN Core.Manager
	ON Manager.ManagerId = Portfolio.ManagerId
	LEFT JOIN Core.Portfolio Model
	ON Model.PortfolioId = Portfolio.ModelId
	LEFT JOIN Core.BookValueMethod
	ON BookValueMethod.BookValueMethodId = Portfolio.BookValueMethod
	LEFT JOIN Core.Portfolio BenchmarkPortfolio
	ON BenchmarkPortfolio.PortfolioId = Portfolio.PortfolioId
	LEFT JOIN Core.Instrument BenchmarkInstrument
	ON BenchmarkInstrument.InstrumentId = Portfolio.BenchmarkInstrumentId
	LEFT JOIN Core.Portfolio SavingsplanPortfolio
	ON SavingsplanPortfolio.PortfolioId = Portfolio.SavingsplanPortfolioId
	LEFT JOIN Core.Customer InsuredCustomer
	ON InsuredCustomer.CustomerId = Portfolio.InsuredCustomerId
	LEFT JOIN Core.Portfolio InvestmentProfilePortfolio
	ON InvestmentProfilePortfolio.PortfolioId = Portfolio.InvestmentProfilePortfolioId
	LEFT JOIN Core.Account TargetAccount
	ON TargetAccount.AccountId = Portfolio.TargetAccountId
	LEFT JOIN Authority.MifidReturnProfile
	ON MifidReturnProfile.MifidReturnProfileId = Portfolio.MifidReturnProfileId
	LEFT JOIN Authority.MifidTimeHorizon
	ON MifidTimeHorizon.MifidTimeHorizonId = Portfolio.MifidTimeHorizonId
	LEFT JOIN Authority.MifidRiskTolerance
	ON MifidRiskTolerance.MifidRiskToleranceId = Portfolio.MifidRiskToleranceId
	LEFT JOIN Authority.MifidDistributionStrategy
	ON MifidDistributionStrategy.MifidDistributionStrategyId = Portfolio.MifidDistributionStrategyId
	LEFT JOIN Core.DiscountTemplate
	ON DiscountTemplate.DiscountTemplateId = Portfolio.DiscountTemplateId
GO

EXEC PublicApi.GenerateGenericTriggers 'Portfolio';

DROP VIEW IF EXISTS PublicApi.AccountPortfolioMapping ;
GO
CREATE OR ALTER VIEW PublicApi.AccountPortfolioMapping
AS
	SELECT
		AccountPortfolioMapping.AccountPortfolioMappingId,
		AccountPortfolioMapping.PortfolioId,
		Account.Code AS AccountCode
	FROM Core.AccountPortfolioMapping
	INNER JOIN Core.Account
	ON Account.AccountId = AccountPortfolioMapping.AccountId
GO

EXEC PublicApi.GenerateGenericTriggers 'AccountPortfolioMapping';


