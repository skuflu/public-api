--liquibase formatted sql

--changeset seku:transactions_vw runOnChange:true
DROP VIEW IF EXISTS PublicApi.Transactions ;
GO
CREATE OR ALTER VIEW PublicApi.Transactions
AS
	SELECT
		Transactions.TransactionId,
		Transactions.ParentId AS ParentTransactionId,
		Transactions.OriginId,
		Portfolio.Code AS PortfolioCode,
		Account.Code AS AccountCode,
		CashAccount.Code AS CashAccountCode,
		ReferenceAccount.Code AS ReferenceAccountCode,
		Instrument.Code AS InstrumentCode,
		ReferenceInstrument.Code AS ReferenceInstrumentCode,
		TransactionType.Code AS TransactionTypeCode,
		Transactions.TradeDate,
		Transactions.SettlementDate,
		Currency.Code AS CurrencyCode,
		Transactions.Quantity,
		Transactions.Price,
		ROUND(TransactionAmounts.InterestOfLocalTotalAmount, 2) AS Interest,
		TransactionAmounts.LocalTotalAmount,
		Transactions.FxRate,
		TransactionAmounts.TransactionTotalAmount,
		Transactions.PortfolioFxRate,
		TransactionAmounts.PortfolioTotalAmount,
		Broker.Code AS BrokerCode,
		Source.Code AS SourceCode,
		Transactions.TransactionText,
		Transactions.Cancel,
		Transactions.BookDate,
		Transactions.BookValueOrder,
		Transactions.BookValueUncertain,

		Transactions.BookValue,
		Transactions.BookValueLocal,

		Transactions.FourEyePending,
		FourEyeApprover.Code AS FourEyeApproverCode,
		Transactions.FourEyeDate,

		COALESCE(Transactions.UpdateDate, Transactions.CreateDate) AS LastModifiedDate,
		Transactions.VersionId AS Version,

		CONVERT(NVARCHAR(MAX), 'PlaceHolder') AS ChildrenAsJson
	FROM Core.Transactions
	INNER JOIN Core.Portfolio
	ON Portfolio.PortfolioId = Transactions.PortfolioId
	INNER JOIN Core.Instrument
	ON Instrument.InstrumentId = Transactions.InstrumentId
	LEFT JOIN Core.Instrument ReferenceInstrument
	ON ReferenceInstrument.InstrumentId = Transactions.ReferenceInstrumentId
	INNER JOIN Core.Account
	ON Account.AccountId = Transactions.AccountId
	INNER JOIN Core.Account CashAccount
    ON CashAccount.AccountId = Transactions.CashAccountId
	LEFT JOIN Core.Account ReferenceAccount
	ON ReferenceAccount.AccountId = Transactions.ReferenceAccountId
	INNER JOIN Core.TransactionType
	ON TransactionType.TransactionTypeId = Transactions.TransactionTypeId
	INNER JOIN Core.Instrument Currency
	ON Currency.InstrumentId = Transactions.CurrencyId
	INNER JOIN Core.Broker
	ON Broker.BrokerId = Transactions.BrokerId
	INNER JOIN Core.Source
	ON Source.SourceId = Transactions.SourceId
	LEFT JOIN SystemSecurity.SystemUser FourEyeApprover
	ON FourEyeApprover.SystemUserId = Transactions.FourEyeApproverId
	INNER JOIN Core.TransactionAmounts
	ON TransactionAmounts.TransactionId = Transactions.TransactionId
GO


CREATE OR ALTER VIEW PublicApi.TransactionsChildren
AS
	SELECT
		Transactions.TransactionId,
		Transactions.ParentId AS ParentTransactionId,
		Instrument.Code AS InstrumentCode,
		Currency.Code AS CurrencyCode,
		TransactionType.Code AS TransactionTypeCode,
		Transactions.Quantity,
		TransactionAmounts.LocalTotalAmount,
		Transactions.FxRate,
		TransactionAmounts.TransactionTotalAmount,
		Transactions.PortfolioFxRate,
		TransactionAmounts.PortfolioTotalAmount
	FROM Core.Transactions
	INNER JOIN Core.Instrument
	ON Instrument.InstrumentId = Transactions.InstrumentId
	INNER JOIN Core.Instrument Currency
	ON Currency.InstrumentId = Transactions.CurrencyId
	INNER JOIN Core.Account
	ON Account.AccountId = Transactions.AccountId
	INNER JOIN Core.TransactionAmounts
	ON TransactionAmounts.TransactionId = Transactions.TransactionId
	INNER JOIN Core.TransactionType
	ON TransactionType.TransactionTypeId = Transactions.TransactionTypeId
	AND TransactionType.Cash IS NULL
	WHERE Transactions.ParentId IS NOT NULL
GO



SELECT TOP 100 * FROM PublicApi.Transactions
GO

CREATE OR ALTER TRIGGER PublicApi_Transactions_Insert_Trigger
ON PublicApi.Transactions
INSTEAD OF INSERT, UPDATE, DELETE AS
BEGIN
	SET NOCOUNT ON 
	DECLARE @TableTypeTransactionInsert AS Core.TransactionInsert
	SELECT * INTO #TransactionInsert FROM @TableTypeTransactionInsert	

	DECLARE
		@CostCurrency VARCHAR(50),
		@DefaultValueCOSTCURRENCY VARCHAR(255),
		@DefaultValueExternalShareSettlement VARCHAR(255)
	
	EXEC Core.spSystemDefaultGet @Code = 'TRANSACTION_COSTCURRENCY', @DefaultValue = @DefaultValueCOSTCURRENCY OUTPUT
	SELECT @CostCurrency = COALESCE(@DefaultValueCOSTCURRENCY, 'LOCAL')
	
	EXEC Core.spSystemDefaultGet @Code = 'ExternalShareSettlement', @DefaultValue = @DefaultValueExternalShareSettlement OUTPUT

	DECLARE
		@ErrorString VARCHAR(200),
 		@InsertCount INTEGER = 0,
		@CreatedTransactionId INTEGER

	INSERT INTO #TransactionInsert (
		--ParentId,
		OriginId,
		PortfolioId,
		InstrumentId,
		TransactionTypeId,
		TradeDate,
		SettlementDate,
		CurrencyId,
		Quantity,
		Price,
		FxRate,
		BrokerId,
		SourceId,
		TransactionText,
		Cancel,
		--CancelTransactionId,
		BookValue,
		BookValueLocal,
		BookValueUncertain,
		ReferenceInstrumentId,
		PortfolioFxRate,
		VersionId,
		FourEyePending,
		Preliminary,
		AccountId,
		CashAccountId,
		ReferenceAccountId,
		--ExternalBankAccountId,
		TransactionTotalAmount
		--TransactionImportReference,
		--TransactionSortOrder
	)
	SELECT
		--inserted.ParentId,
		inserted.OriginId,
		Portfolio.PortfolioId,
		Instrument.InstrumentId,
		TransactionType.TransactionTypeId,
		inserted.TradeDate,
		inserted.SettlementDate,
		Currency.InstrumentId AS CurrencyId,
		inserted.Quantity,
		inserted.Price,
		inserted.FxRate,
		Broker.BrokerId,
		(SELECT SourceId FROM Core.Source WHERE Code = 'PUBLIC_API'),
		inserted.TransactionText,
		inserted.Cancel,
		--inserted.CancelTransactionId,
		inserted.BookValue,
		inserted.BookValueLocal,
		inserted.BookValueUncertain,
		ReferenceInstrument.InstrumentId,
		inserted.PortfolioFxRate,
		0 AS VersionId,
		inserted.FourEyePending,
		0 AS Preliminary,
		Account.AccountId,
		CashAccount.AccountId AS CashAccountId,
		ReferenceAccount.AccountId AS ReferenceAccountId,
		--inserted.ExternalBankAccountId,
		inserted.TransactionTotalAmount
		--inserted.TransactionImportReference,
		--inserted.TransactionSortOrder
	FROM inserted
	INNER JOIN Core.Portfolio
	ON Portfolio.Code = inserted.PortfolioCode
	INNER JOIN Core.Instrument
	ON Instrument.Code = inserted.InstrumentCode
	INNER JOIN Core.TransactionType
	ON TransactionType.Code = inserted.TransactionTypeCode
	INNER JOIN Core.Instrument Currency
	ON Currency.Code = inserted.CurrencyCode
	LEFT JOIN Core.Broker
	ON Broker.Code = inserted.BrokerCode
	LEFT JOIN Core.Instrument ReferenceInstrument
	ON ReferenceInstrument.Code = inserted.ReferenceInstrumentCode
	INNER JOIN Core.Account
	ON Account.Code = inserted.AccountCode
	LEFT JOIN Core.Account CashAccount
	ON CashAccount.Code = inserted.CashAccountCode
	LEFT JOIN Core.Account ReferenceAccount
	ON ReferenceAccount.Code = inserted.ReferenceAccountCode



	--UPDATE #TransactionInsert 
	--	SET Price = Price.Price - InterestRate.InterestRate
	--FROM #TransactionInsert
	--CROSS APPLY Core.fn_PriceAsTable(#TransactionInsert.InstrumentId, (SELECT ScenarioId FROM Core.Scenario WHERE DefaultScenario = 1), #TransactionInsert.TradeDate)Price
	--CROSS APPLY Core.fn_InterestRateAsTable(#TransactionInsert.InstrumentId, #TransactionInsert.TradeDate)InterestRate

	UPDATE #TransactionInsert 
		SET Price = COALESCE(Price.Price, #TransactionInsert.Price),
			FxRate = COALESCE(FxRate.FxRate, #TransactionInsert.FxRate),
			PortfolioFxRate = COALESCE(PortfolioFxRate.FxRate, #TransactionInsert.PortfolioFxRate)
	FROM #TransactionInsert
	INNER JOIN Core.Portfolio
	ON Portfolio.PortfolioId = #TransactionInsert.PortfolioId
	INNER JOIN Core.Instrument
	ON Instrument.InstrumentId = #TransactionInsert.InstrumentId
	CROSS APPLY Core.fn_PriceAsTable(#TransactionInsert.InstrumentId, (SELECT ScenarioId FROM Core.Scenario WHERE DefaultScenario = 1), #TransactionInsert.TradeDate) Price
	CROSS APPLY Core.fn_FxRateAsTable(#TransactionInsert.CurrencyId, Instrument.PrincipalCurrencyId, Portfolio.ScenarioId, #TransactionInsert.TradeDate) FxRate
	CROSS APPLY Core.fn_FxRateAsTable(Portfolio.CurrencyId, Instrument.PrincipalCurrencyId, Portfolio.ScenarioId, #TransactionInsert.TradeDate) PortfolioFxRate
	WHERE #TransactionInsert.Price IS NULL 
	OR #TransactionInsert.FxRate IS NULL
	OR #TransactionInsert.PortfolioFxRate IS NULL

	INSERT INTO #TransactionInsert (
		TransactionInsertParentId,
		OriginId,
		PortfolioId,
		InstrumentId,
		TransactionTypeId,
		TradeDate,
		SettlementDate,
		CurrencyId,
		Quantity,
		Price,
		FxRate,
		BrokerId,
		SourceId,
		TransactionText,
		Cancel,
		BookValueUncertain,
		PortfolioFxRate,
		VersionId,
		FourEyePending,
		Preliminary,
		AccountId,
		CashAccountId,
		TransactionImportReference,
		TransactionSortOrder
	)
	SELECT
		TransactionInsertParentId = #TransactionInsert.TransactionInsertId,
		OriginId = #TransactionInsert.OriginId + '_' + CostsTransactionsJSON.transactionTypeCode,
		PortfolioId = #TransactionInsert.PortfolioId,
		InstrumentId = 
			CASE 
				WHEN @CostCurrency = 'LOCAL' THEN 
					Instrument.PrincipalCurrencyId
				WHEN @CostCurrency = 'TRANSACTION' THEN 
					#TransactionInsert.CurrencyId
				WHEN @CostCurrency = 'PORTFOLIO' THEN 
					Portfolio.CurrencyId
			END,
		TransactionTypeId = TransactionType.TransactionTypeId,
		TradeDate =	#TransactionInsert.TradeDate,
		SettlementDate = #TransactionInsert.SettlementDate,
		CurrencyId = #TransactionInsert.CurrencyId,
		Quantity = 
		ABS(
			CASE
				WHEN @CostCurrency = 'LOCAL' THEN 
					CostsTransactionsJSON.localTotalAmount / #TransactionInsert.FxRate
				WHEN @CostCurrency = 'TRANSACTION' THEN 
					CostsTransactionsJSON.localTotalAmount
				WHEN @CostCurrency = 'PORTFOLIO' THEN 
					CostsTransactionsJSON.localTotalAmount / #TransactionInsert.FxRate * #TransactionInsert.PortfolioFxRate
			END
		) * TransactionType.SignMultiplier,
		Price = 1.00,
		FxRate = 
			CASE 
				WHEN @CostCurrency = 'LOCAL' THEN 
					#TransactionInsert.FxRate 
				WHEN @CostCurrency = 'TRANSACTION' THEN 
					1
				WHEN @CostCurrency = 'PORTFOLIO' THEN 
					#TransactionInsert.FxRate / #TransactionInsert.PortfolioFxRate
			END,
		BrokerId = #TransactionInsert.BrokerId,
		SourceId = #TransactionInsert.SourceId,
		TransactionText = #TransactionInsert.TransactionText,
		Cancel = #TransactionInsert.Cancel,
		BookValueUncertain = #TransactionInsert.BookValueUncertain,
		PortfolioFxRate =
			CASE 
				WHEN @CostCurrency = 'LOCAL' THEN 
					#TransactionInsert.PortfolioFxRate
				WHEN @CostCurrency = 'TRANSACTION' THEN 
					CASE
						WHEN Instrument.PrincipalCurrencyId <> Portfolio.CurrencyId AND #TransactionInsert.CurrencyId <> Portfolio.CurrencyId AND Instrument.PrincipalCurrencyId <> #TransactionInsert.CurrencyId THEN
							#TransactionInsert.PortfolioFxRate / #TransactionInsert.FxRate
						WHEN #TransactionInsert.CurrencyId <> Portfolio.CurrencyId AND Instrument.PrincipalCurrencyId = Portfolio.CurrencyId THEN
							1 / #TransactionInsert.FxRate
						WHEN #TransactionInsert.CurrencyId <> Portfolio.CurrencyId AND Instrument.PrincipalCurrencyId = #TransactionInsert.CurrencyId THEN
							#TransactionInsert.PortfolioFxRate
						WHEN #TransactionInsert.CurrencyId = Portfolio.CurrencyId THEN
							1
					END 
				WHEN @CostCurrency = 'PORTFOLIO' THEN 
					1	
			END,
		VersionId = 0,
		FourEyePending = 0,
		Preliminary = 0, 
		AccountId = COALESCE(#TransactionInsert.CashAccountId, #TransactionInsert.AccountId),
		CashAccountId = #TransactionInsert.CashAccountId,
		TransactionImportReference = #TransactionInsert.TransactionImportReference,
		TransactionSortOrder = 1
	FROM inserted
	CROSS APPLY OPENJSON(inserted.childrenAsJson)
	WITH (
		transactionTypeCode VARCHAR(50),
		localTotalAmount NUMERIC(19,2)
	)CostsTransactionsJSON
	INNER JOIN Core.TransactionType
	ON TransactionType.Code = CostsTransactionsJSON.transactionTypeCode
	CROSS APPLY #TransactionInsert
	INNER JOIN Core.Portfolio
	ON Portfolio.PortfolioId = #TransactionInsert.PortfolioId
	INNER JOIN Core.Instrument
	ON Instrument.InstrumentId = #TransactionInsert.InstrumentId

	UPDATE Core.Account
		SET StartDate = #TransactionInsert.TradeDate
	FROM Core.Account
	INNER JOIN #TransactionInsert
	ON #TransactionInsert.AccountId = Account.AccountId
	WHERE #TransactionInsert.TradeDate < Account.StartDate

	UPDATE Core.Portfolio
		SET StartDate = #TransactionInsert.TradeDate
	FROM Core.Portfolio
	INNER JOIN #TransactionInsert
	ON #TransactionInsert.PortfolioId = Portfolio.PortfolioId 
	WHERE #TransactionInsert.TradeDate < Portfolio.StartDate

	--select * from #TransactionInsert
	--BEGIN TRY
		EXEC Core.spImportTransactionWrapper 	
			@Validate = 1,
			@CreateFSCashAdjustment = 0,
			@Create = 1,
			@CreateCompensation = 0
	--END TRY
	--BEGIN CATCH
	--	SELECT TOP 1
	--		@ErrorString = ValidationErrorMessage
	--	FROM #TransactionInsert
			
	--	IF @ErrorString IS NOT NULL
	--		GOTO theEnd
	--END CATCH
	--SELECT TOP 1 TransactionId FROM Core.Transactions ORDER BY TransactionId DESC

	--theEnd:
	--IF @ErrorString IS NOT NULL
	--BEGIN
	--	IF @@TRANCOUNT > 0
	--		ROLLBACK TRANSACTION
	--	RAISERROR(@ErrorString, 16, 1) WITH SETERROR
	--END
	--IF OBJECT_ID('tempdb..#TransactionInsert') IS NOT NULL
	--	DROP TABLE #TransactionInsert
	--IF @@TRANCOUNT > 0
	--		ROLLBACK TRANSACTION
END

