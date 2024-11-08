--liquibase formatted sql

--changeset seku:account_vw runOnChange:true endDelimiter:GO
DROP VIEW IF EXISTS PublicApi.Account ;
GO
CREATE OR ALTER VIEW PublicApi.Account
AS
	SELECT 
		Account.AccountId,
		Account.Code,
		Account.Description,
		AccountType.Code AS AccountTypeCode,
		Custodian.Code AS CustodianCode,
		Account.IBAN,
		Currency.Code AS CurrencyCode,
		Account.StartDate,
		Account.EndDate,
		Account.Collateral,
		Account.ReinvestFundDividend,
		
		Account.CreateDate,
		Account.UpdateDate,
		Account.VersionId
	FROM Core.Account
	INNER JOIN Core.AccountType
	ON AccountType.AccountTypeId = Account.AccountTypeId
	LEFT JOIN Core.Custodian
	ON Custodian.CustodianId = Account.CustodianId
	LEFT JOIN Core.Instrument Currency
	ON Currency.InstrumentId = Account.CurrencyId
;
GO
EXEC PublicApi.GenerateGenericTriggers 'Account';

DROP VIEW IF EXISTS PublicApi.AccountOwner ;
GO
CREATE OR ALTER VIEW PublicApi.AccountOwner
AS
	SELECT
		AccountOwner.AccountOwnerId,
		AccountOwner.AccountId,
		Customer.Code AS CustomerCode,
		Weight
	FROM Core.AccountOwner
	INNER JOIN Core.Customer
	ON Customer.CustomerId = AccountOwner.CustomerId
GO
EXEC PublicApi.GenerateGenericTriggers 'AccountOwner';

DROP VIEW IF EXISTS PublicApi.AccountOmniAccount ;
GO
CREATE OR ALTER VIEW PublicApi.AccountOmniAccount
AS
	SELECT
		AccountOmniAccount.AccountOmniAccountId,
		AccountOmniAccount.AccountId,
		Account.Code AS OmniAccountCode
	FROM Core.AccountOmniAccount
	INNER JOIN Core.Account
	ON Account.AccountId = AccountOmniAccount.OmniAccountId
GO
EXEC PublicApi.GenerateGenericTriggers 'AccountOmniAccount';

