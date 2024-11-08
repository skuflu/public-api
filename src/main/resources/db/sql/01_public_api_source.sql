--liquibase formatted sql

--changeset seku:public_api_schema runOnChange:true
EXEC SystemSecurity.spUtilSetUserSystemService 0;
INSERT INTO Core.Source(Code, Description, CreateCashTransactions)
SELECT 'CAIRO_PUBLIC_API', 'Cairo API', 1
WHERE NOT EXISTS (SELECT TOP 1 1 FROM Core.Source WHERE Code = 'CAIRO_PUBLIC_API');