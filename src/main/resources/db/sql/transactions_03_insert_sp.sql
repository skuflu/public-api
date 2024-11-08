--liquibase formatted sql
--changeset seku:customer_insert_procedure runOnChange:true endDelimiter:GO

IF TYPE_ID('PublicApi.InsertTransactionsParameter') IS NOT NULL 
BEGIN
	DROP PROCEDURE IF EXISTS PublicApi.spInsertTransactions;
	DROP TYPE PublicApi.InsertTransactionsParameter;
END
GO

CREATE TYPE PublicApi.InsertTransactionsParameter AS TABLE (
	ItemIndex INTEGER,
	TransactionId INTEGER,
	ParentTransactionId INTEGER,
	OriginId VARCHAR(100),
	PortfolioCode VARCHAR(100),
	AccountCode VARCHAR(100),
	CashAccountCode VARCHAR(100),
	ReferenceAccountCode VARCHAR(100),
	InstrumentCode VARCHAR(100),
	ReferenceInstrumentCode VARCHAR(100),
	TransactionTypeCode VARCHAR(100),
	TradeDate DATE,
	SettlementDate DATE,
	CurrencyCode VARCHAR(100),
	Quantity NUMERIC(22,9), 
	Price NUMERIC(16,9),
	Interest NUMERIC(22,9),
	LocalTotalAmount NUMERIC(19,2),
	FxRate NUMERIC(16,9),
	TransactionTotalAmount NUMERIC(19,2),
	PortfolioFxRate NUMERIC(16,9),
	PortfolioTotalAmount NUMERIC(19,2),
	BrokerCode VARCHAR(100),
	SourceCode VARCHAR(100),
	TransactionText VARCHAR(512),
	Cancel BIT,
	BookDate DATE,
	BookValueOrder INTEGER,
	BookValueUncertain BIT,

	BookValue NUMERIC(19,2),
	BookValueLocal NUMERIC(19,2),

	FourEyeApproverCode VARCHAR(100),
	FourEyeDate DATE,

	VersionId INTEGER,

	ChildrenAsJson VARCHAR(MAX)
)
GO

EXEC PublicApi.GenerateBatchValidation 'Transactions', '#InsertTransactionsParameter', 'ItemIndex', 'ValidationErrorMessage'
GO

CREATE OR ALTER PROCEDURE PublicApi.spInsertTransactions
	@InsertTransactionsParameter PublicApi.InsertTransactionsParameter READONLY,
	@ReducePriceWithInterestRate BIT = 0,
	@AdjustPortfolioAccountStartDate BIT = 0,
	@CheckOriginIdIsUnique BIT = 0
AS
	SET NOCOUNT ON 
	DECLARE @TableTypeTransactionInsert AS Core.TransactionInsert
	SELECT * INTO #TransactionInsert FROM @TableTypeTransactionInsert	

	ALTER TABLE #TransactionInsert
		ADD InsertReferenceId INT

	SELECT inserted.*,
		ItemIndex AS InsertReferenceId,
		CONVERT(VARCHAR(512), (SELECT NULL)) AS ValidationErrorMessage
	INTO #InsertTransactionsParameter
	FROM @InsertTransactionsParameter inserted

	--Generic validation
	EXEC PublicApi.spValidateTransactions

	IF @CheckOriginIdIsUnique = 1
	BEGIN
		UPDATE #InsertTransactionsParameter
			SET ValidationErrorMessage = 'Transaction with Origin ID already exists.'
		FROM #InsertTransactionsParameter
		INNER JOIN Core.Transactions
		ON Transactions.OriginId = InsertTransactionsParameter.OriginId
	END

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
		TransactionTotalAmount,
		InsertReferenceId,
		TransactionImportReference
	)
	SELECT
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
		IIF(SourceCode IS NULL, (SELECT SourceId FROM Core.Source WHERE Code = 'CAIRO_PUBLIC_API'), Source.SourceId),
		inserted.TransactionText,
		inserted.Cancel,
		inserted.BookValue,
		inserted.BookValueLocal,
		inserted.BookValueUncertain,
		ReferenceInstrument.InstrumentId,
		inserted.PortfolioFxRate,
		0 AS VersionId,
		0 AS FourEyePending,
		0 AS Preliminary,
		Account.AccountId,
		CashAccount.AccountId AS CashAccountId,
		ReferenceAccount.AccountId AS ReferenceAccountId,
		inserted.TransactionTotalAmount,
		inserted.InsertReferenceId,
		NEWID()
	FROM #InsertTransactionsParameter inserted
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
	LEFT JOIN Core.Source
	ON Source.Code = inserted.SourceCode
	LEFT JOIN Core.Instrument ReferenceInstrument
	ON ReferenceInstrument.Code = inserted.ReferenceInstrumentCode
	INNER JOIN Core.Account
	ON Account.Code = inserted.AccountCode
	LEFT JOIN Core.Account CashAccount
	ON CashAccount.Code = inserted.CashAccountCode
	LEFT JOIN Core.Account ReferenceAccount
	ON ReferenceAccount.Code = inserted.ReferenceAccountCode
	WHERE inserted.ValidationErrorMessage IS NULL

	IF @ReducePriceWithInterestRate = 1
	BEGIN
		UPDATE #TransactionInsert 
			SET Price = Price.Price - InterestRate.InterestRate
		FROM #TransactionInsert
		CROSS APPLY Core.fn_PriceAsTable(#TransactionInsert.InstrumentId, (SELECT ScenarioId FROM Core.Scenario WHERE DefaultScenario = 1), #TransactionInsert.TradeDate) Price
		CROSS APPLY Core.fn_InterestRateAsTable(#TransactionInsert.InstrumentId, #TransactionInsert.TradeDate) InterestRate
		WHERE #TransactionInsert.Price IS NULL
	END

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
					CostsTransactionsJSON.quantity / #TransactionInsert.FxRate
				WHEN @CostCurrency = 'TRANSACTION' THEN 
					CostsTransactionsJSON.quantity
				WHEN @CostCurrency = 'PORTFOLIO' THEN 
					CostsTransactionsJSON.quantity / #TransactionInsert.FxRate * #TransactionInsert.PortfolioFxRate
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
	FROM #InsertTransactionsParameter inserted
	CROSS APPLY OPENJSON(inserted.childrenAsJson)
	WITH (
		transactionTypeCode VARCHAR(50),
		quantity NUMERIC(19,2)
	) CostsTransactionsJSON
	INNER JOIN Core.TransactionType
	ON TransactionType.Code = CostsTransactionsJSON.transactionTypeCode
	INNER JOIN #TransactionInsert
	ON #TransactionInsert.InsertReferenceId = inserted.InsertReferenceId
	INNER JOIN Core.Portfolio
	ON Portfolio.PortfolioId = #TransactionInsert.PortfolioId
	INNER JOIN Core.Instrument
	ON Instrument.InstrumentId = #TransactionInsert.InstrumentId
	WHERE inserted.ValidationErrorMessage IS NULL

	IF @AdjustPortfolioAccountStartDate = 1
	BEGIN
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
	END

	CREATE TABLE #TransactionInsertError (
		InsertReferenceId INTEGER,
		ValidationErrorMessage VARCHAR(MAX)
	)

	DECLARE @Run BIT = 1,
		@Runs INT = 0

	WHILE @Run = 1 AND @Runs <= 2
	BEGIN
		PRINT CONCAT('Run: ', @Runs)
		SET @Run = 0
		BEGIN TRY
			EXEC Core.spImportTransactionWrapper 	
				@Validate = 1,
				@CreateFSCashAdjustment = 0,
				@Create = 1,
				@CreateCompensation = 0
		END TRY
		BEGIN CATCH
			SET @ErrorString = ERROR_MESSAGE()

			IF EXISTS (
				SELECT TOP 1 1 
				FROM #TransactionInsert 
				LEFT JOIN #TransactionInsertError
				ON #TransactionInsertError.InsertReferenceId = #TransactionInsert.InsertReferenceId
				WHERE #TransactionInsert.ValidationErrorMessage IS NOT NULL
				AND #TransactionInsertError.InsertReferenceId IS NULL)
			BEGIN
				PRINT 'Validation error found'
				SET @Run = 1
				INSERT INTO #TransactionInsertError (
					InsertReferenceId,
					ValidationErrorMessage
				)
				SELECT 
					InsertReferenceId,
					ValidationErrorMessage
				FROM #TransactionInsert
				WHERE ValidationErrorMessage IS NOT NULL

				DELETE #TransactionInsert 
				WHERE ValidationErrorMessage IS NOT NULL
			END
			ELSE
			BEGIN
				PRINT 'Error caught'

				IF @ErrorString IS NOT NULL
					GOTO theEnd
				ELSE 
					THROW;
			END
		END CATCH
		SET @Runs = @Runs + 1
	END

	PRINT 'Insert complete'

	SELECT
		InsertReferenceId,
		NULL AS TransactionImportReference, -- Always null when error
		ValidationErrorMessage
	FROM #TransactionInsertError
	UNION
	SELECT
		InsertReferenceId,
		NULL AS TransactionImportReference, -- Always null when error
		ValidationErrorMessage
	FROM #InsertTransactionsParameter
	WHERE ValidationErrorMessage IS NOT NULL
	UNION
	SELECT
		InsertReferenceId,
		TransactionImportReference,
		ValidationErrorMessage -- Always null
	FROM #TransactionInsert
	

	theEnd:
	IF @ErrorString IS NOT NULL
	BEGIN
		IF @@TRANCOUNT > 0
			ROLLBACK TRANSACTION
		RAISERROR(@ErrorString, 16, 1) WITH SETERROR
	END
GO