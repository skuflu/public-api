--liquibase formatted sql

--changeset seku:public_api_schema runOnChange:true
IF (SCHEMA_ID('PublicApi') IS NULL) 
BEGIN
    EXEC ('CREATE SCHEMA [PublicApi]');
END