-- CREATE DDL ----------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
CREATE TABLE KEYOBJECTS (
	SOPINSTANCEUID VARCHAR2(64 CHAR), 
	SOPCLASSUID VARCHAR2(64 CHAR), 
	INSTANCENUMBER NUMBER(19,0), 
	SERIESFK VARCHAR2(64 CHAR),
	CODESEQUENCESFK NUMBER(19,0), 
	CONTENTDATE DATE, 
	CONTENTTIME DATE, 
	DEPRECATED NUMBER(3,0) DEFAULT 0,
	STGCOMMITTED NUMBER(3,0) DEFAULT 0,
	PRIMARY KEY (SOPINSTANCEUID) ENABLE, 
	FOREIGN KEY (CODESEQUENCESFK) REFERENCES CODESEQUENCES (PK) ENABLE, 
	FOREIGN KEY (SERIESFK) REFERENCES SERIES (SERIESINSTANCEUID) ENABLE)
	TABLESPACE O3DPACSDB9_TBL;
------------------------------------------------------------------------------------------------------------------------

CREATE TABLE KEYOBJECTREFERENCES (
	KEYOBJECTSFK VARCHAR2(64 CHAR) NOT NULL ENABLE, 
	REFSERIESINSTANCEUID VARCHAR2(64 CHAR), 
	REFSOPINSTANCEUID VARCHAR2(64 CHAR), 
	REFSOPCLASSUID VARCHAR2(64 CHAR), 
	PRIMARY KEY (KEYOBJECTSFK, REFSERIESINSTANCEUID, REFSOPINSTANCEUID) ENABLE, 
	FOREIGN KEY (KEYOBJECTSFK) REFERENCES KEYOBJECTS (SOPINSTANCEUID) ENABLE
)
TABLESPACE O3DPACSDB9_TBL;



------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------


-- UPDATE DDL ----------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
UPDATE SUPPORTEDSOPCLASSES
SET TABLENAMEIFINSTANCE='KeyObjects', MIMETYPE=3
WHERE SOPCLASSUID='1.2.840.10008.5.1.4.1.1.88.59';

------------------------------------------------------------------------------------------------------------------------

UPDATE GLOBALCONFIGURATION
SET PARAMVALUE='
<root>
	<!--
		<criteria type="" order="" />
		type:
			CREATION_TIME
			INSTANCE_NUMBER
			X_AXIS
			Y_AXIS
			Z_AXIS
			ECHO_TIME
		order:
			DESCENDING
			ASCENDING
	-->
	<modality code="default">
		<criteria type="INSTANCE_NUMBER" order="ASCENDING" />
	</modality>
	<modality code="CT">
		<criteria type="Z_AXIS" order="DESCENDING" />
		<criteria type="INSTANCE_NUMBER" order="DESCENDING" />
	</modality>
	<modality code="MR">
		<criteria type="ECHO_TIME" order="DESCENDING" />
		<criteria type="Z_AXIS" order="DESCENDING" />
	</modality>
	<modality code="KO">
		<criteria type="CREATION_TIME" order="DESCENDING" />
		<criteria type="INSTANCE_NUMBER" order="ASCENDING" />
	</modality>
</root>'
WHERE PARAMKEY='seriesSortCriteria' AND ENABLED=1;

------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------


-- INSERT DDL ----------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
INSERT INTO GLOBALCONFIGURATION
(PARAMKEY, PARAMVALUE, ENABLED)
VALUES('KeyImagesOnly', 'true', 1);

------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
