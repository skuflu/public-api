--liquibase formatted sql

--changeset seku:instrument_vw runOnChange:true endDelimiter:GO
DROP VIEW IF EXISTS PublicApi.Instrument ;
GO
CREATE OR ALTER VIEW PublicApi.Instrument
AS
	SELECT 
		Instrument.InstrumentId,
		Instrument.Code,
		Instrument.Description,
		InstrumentType.Code AS InstrumentTypeCode,
		InstrumentParent.Code AS InstrumentParentCode,
		Issuer.Code AS IssuerCode,
		PrincipalCurrency.Code AS PrincipalCurrencyCode,
		IncomeCurrency.Code AS IncomeCurrencyCode,
		Region.Code AS RegionCode,
		TaxRegion.Code AS TaxRegionCode,
		Sector.Code AS SectorCode,
		Exchange.Code AS ExchangeCode,
		Instrument.PriceMultiplier,
		Instrument.StrikePrice,
		Instrument.ShareRatio,
		Instrument.IssueDate,
		Instrument.ExpireDate,
		Instrument.SharesPerUnit,
		Portfolio.Code AS PortfolioCode,
		Instrument.PriceDeviation,
		Instrument.Ethical,
		Instrument.Environmental,
		AssetClass.Code AS AssetClassCode,
		Instrument.SubscriptionInUnits,
		Instrument.SubscriptionInAmount,
		Instrument.NextSubscriptionTradeDeadline,
		Instrument.RedemptionInUnits,
		Instrument.RedemptionInAmount,
		Instrument.NextRedemptionTradeDeadline,
		Instrument.TradeLot,
		Instrument.Manager,
		BenchmarkInstrument.Code AS BenchmarkInstrumentCode,
		Instrument.OrganizationNumber,
		Instrument.ISIN,
		MarketCalendarType.Code AS MarketCalendarTypeCode,
		PriceCalendarType.Code AS PriceCalendarTypeCode,
		TradeFrequency.Code AS TradeFrequencyCode,
		Instrument.CutOffTime,
		Instrument.SubscriptionFeeAmount,
		Instrument.SubscriptionFeePercent,
		Instrument.RedemptionFeeAmount,
		Instrument.RedemptionFeePercent,
		Instrument.AdditionalFeeDescription,
		Instrument.MinimumInitialSubscription,
		Instrument.MinimumMonthlySubscription,
		Instrument.MinimumSubscription,
		Instrument.CalculationFractions,
		Instrument.ExtendedDescription,
		Instrument.SubscriptionStartDate,
		Instrument.SubscriptionEndDate,
		Instrument.SubscriptionSettlementDate,
		Instrument.PriceIncludeInterest,
		Instrument.Leverage,
		Instrument.OptionRight,
		Instrument.VotingRightRatio,
		Instrument.LeveragedFinancialInstrumentOrContingentLiabilityInstrument,
		Instrument.SettlementDaysSell,
		Instrument.SettlementDaysBuy,
		CouponDateStepConvention.Code AS CouponDateStepConventionCode,
		CouponSettlementDateStepConvention.Code AS CouponSettlementDateStepConventionCode,
		Instrument.RecordDays,
		Instrument.RedemptionPrice,
		DayCountMethod.Code AS DayCountMethodCode,
		InterestFrequency.Code AS InterestFrequencyCode,
		Instrument.FirstCouponDate,
		Instrument.CouponRate,
		Instrument.InvestorTypeRetail,
		Instrument.InvestorTypeProfessional,
		Instrument.InvestorTypePerSeProfessional,
		Instrument.InvestorTypeElectiveProfessional,
		Instrument.InvestorTypeEligibleCounterparty,
		Instrument.KnowledgeAndExperienceBasic,
		Instrument.KnowledgeAndExperienceInformed,
		Instrument.KnowledgeAndExperienceAdvanced,
		Instrument.AbilityToBearLimitedCapitalLossLevel,
		Instrument.AbilityToBearLossesLimitedCapitalLoss,
		Instrument.AbilityToBearLossesLossBeyondCapital,
		Instrument.AbilityToBearLossesNoCapitalGuarantee,
		Instrument.AbilityToBearLossesNoCapitalLoss,
		Instrument.ReturnProfilePreservation,
		Instrument.ReturnProfileGrowth,
		Instrument.ReturnProfileIncome,
		Instrument.ReturnProfileHedging,
		Instrument.ReturnProfileOptionOrLeveraged,
		Instrument.ReturnProfileOther,
		Instrument.ReturnProfileSpecificInvestmentNeed,
		MifidTimeHorizon.Code AS MifidTimeHorizonCode,
		MifidRiskTolerance.Code AS MifidRiskToleranceCode,
		Instrument.MifidDistributionStrategyExecutionOnlyRetail,
		Instrument.MifidDistributionStrategyExecutionOnlyProfessional,
		Instrument.MifidDistributionStrategyExecutionAppropriatenessAssessmentRetail,
		Instrument.MifidDistributionStrategyExecutionAppropriatenessAssessmentProfessional,
		Instrument.MifidDistributionStrategyInvestmentAdviceRetail,
		Instrument.MifidDistributionStrategyInvestmentAdviceProfessional,
		Instrument.MifidDistributionStrategyPortfolioManagementRetail,
		Instrument.MifidDistributionStrategyPortfolioManagementProfessional,
		ReferenceRateInstrument.Code AS ReferenceRateInstrumentCode,
		Instrument.ReferenceRateMargin,
		Instrument.ReferenceRateDeemedToZero,
		
		Instrument.CreateDate,
		Instrument.UpdateDate,
		Instrument.VersionId
	FROM Core.Instrument
	LEFT JOIN Core.Instrument InstrumentParent
	ON InstrumentParent.InstrumentId = Instrument.InstrumentId
	INNER JOIN Core.InstrumentType
	ON InstrumentType.InstrumentTypeId = Instrument.InstrumentTypeId
	INNER JOIN Core.Instrument PrincipalCurrency
	ON PrincipalCurrency.InstrumentId = Instrument.PrincipalCurrencyId
	INNER JOIN Core.Instrument IncomeCurrency
	ON IncomeCurrency.InstrumentId = Instrument.IncomeCurrencyId
	INNER JOiN Core.Issuer
	ON Issuer.IssuerId = Instrument.IssuerId
	INNER JOIN Core.Region
	ON Region.RegionId = Instrument.RegionId
	INNER JOIN Core.Region TaxRegion
	ON TaxRegion.RegionId = Instrument.RegionId
	INNER JOIN Core.Sector
	ON Sector.SectorId = Instrument.SectorId
	LEFT JOIN Core.Exchange
	ON Exchange.ExchangeId = Instrument.ExchangeId
	LEFT JOIN Core.Portfolio
	ON Portfolio.PortfolioId = Instrument.PortfolioId
	LEFT JOIN Core.AssetClass
	ON AssetClass.AssetClassId = Instrument.AssetClassId
	LEFT JOIN Core.Instrument BenchmarkInstrument
	ON BenchmarkInstrument.InstrumentId = Instrument.BenchmarkInstrumentId
	INNER JOIN Core.CalendarType MarketCalendarType
	ON MarketCalendarType.CalendarTypeId = Instrument.MarketCalendarTypeId
	INNER JOIN Core.CalendarType PriceCalendarType
	ON PriceCalendarType.CalendarTypeId = Instrument.PriceCalendarTypeId
	LEFT JOIN Core.TradeFrequency
	ON TradeFrequency.TradeFrequencyId = Instrument.TradeFrequencyId
	LEFT JOIN Core.DateStepConvention CouponDateStepConvention
	ON CouponDateStepConvention.DateStepConventionId = Instrument.CouponDateStepConventionId
	LEFT JOIN Core.DateStepConvention CouponSettlementDateStepConvention
	ON CouponSettlementDateStepConvention.DateStepConventionId = Instrument.CouponSettlementDateStepConventionId
	LEFT JOIN Core.DayCountMethod DayCountMethod
	ON DayCountMethod.DayCountMethodId = Instrument.DayCountMethodId
	LEFT JOIN Core.InterestFrequency
	ON InterestFrequency.InterestFrequencyId = Instrument.InterestFrequencyId
	LEFT JOIN Authority.MifidTimeHorizon
	ON MifidTimeHorizon.MifidTimeHorizonId = Instrument.MifidTimeHorizonId
	LEFT JOIN Authority.MifidRiskTolerance
	ON MifidRiskTolerance.MifidRiskToleranceId = Instrument.MifidRiskToleranceId
	LEFT JOIN Core.Instrument ReferenceRateInstrument
	ON ReferenceRateInstrument.InstrumentId = Instrument.ReferenceRateInstrumentId
	
;
GO
EXEC PublicApi.GenerateGenericTriggers 'Instrument';


