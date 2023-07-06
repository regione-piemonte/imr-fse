-- Creazione tabella conf

CREATE TABLE To3pdi_Conf( 
	paramKey VARCHAR(24) NOT NULL, 
	paramValue VARCHAR(1024), 
	enabled NUMBER(3) DEFAULT '1' NOT NULL , 
	PRIMARY KEY(paramKey,enabled)
);


INSERT INTO To3pdi_Conf (paramkey,paramvalue,enabled) VALUES ('DacspdiConfiguration',
'<?xml version="1.0" encoding="UTF-8 " ?>
<configurazioni>
	<asl id ="1">
	  <nome>NONE</nome>
	  <endpoint>http://localhost:7080</endpoint>
	  <nume_retry>5</nume_retry>
	  <time_lapse>5000</time_lapse>	  
	</asl>
	<asl id ="2">
	  <nome>ASL1</nome>
	  <endpoint></endpoint>
	  <nume_retry></nume_retry>
	  <time_lapse></time_lapse>	  
	</asl>
	<asl id ="3">
	  <nome>ASL2</nome>
	  <endpoint></endpoint>
	  <nume_retry></nume_retry>
	  <time_lapse></time_lapse>	  
	</asl>
</configurazioni>',1);

CREATE OR REPLACE PROCEDURE getTo3pdi_Conf(
    p_paramKey IN VARCHAR2,
    resultset OUT Types.ResultSet
)
AS
BEGIN
  OPEN resultset FOR SELECT paramValue FROM To3pdi_Conf WHERE paramKey=p_paramKey AND enabled=1;
END getTo3pdi_Conf;


INSERT INTO To3pdi_Conf (paramkey,paramvalue,enabled) VALUES ('CreatePdiRequestEnabled','TRUE',1);
INSERT INTO To3pdi_Conf (paramkey,paramvalue,enabled) VALUES ('DEFAULT_ID_ASL','NONE',1);
INSERT INTO To3pdi_Conf (paramkey,paramvalue,enabled) VALUES ('DEFAULT_ID_STRUTTURA','NONE',1);
INSERT INTO To3pdi_Conf (paramkey,paramvalue,enabled) VALUES ('WadoUrl','/o3-dpacs-wado/wado',1);
INSERT INTO To3pdi_Conf (paramkey,paramvalue,enabled) VALUES ('PatientInfo','/o3-dpacs-wado/getPatientInfo',1);
INSERT INTO To3pdi_Conf (paramkey,paramvalue,enabled) VALUES ('Workarea','C:/Directory/pacs/',1);
INSERT INTO To3pdi_Conf (paramkey,paramvalue,enabled) VALUES ('Assets','C:/Conf/assets/',1);
  
 -- Creazione tabella JOB
 
  CREATE SEQUENCE TO3PDI_JOB_PK_SEQ NOCACHE ORDER;
  CREATE TABLE TO3PDI_JOB 
   (	
    PK NUMBER(18,0), 
	JOB_ID  VARCHAR2(20 CHAR),
	DATAINIZIO TIMESTAMP (6) NOT NULL ENABLE, 
	DATAFINE TIMESTAMP (6), 
	CODICE NUMBER(18,0), 
	DESCRIZIONE VARCHAR2(128 CHAR),
    OPERAZIONE VARCHAR2(64 CHAR),
    STATO VARCHAR2(64 CHAR),
	CONSTRAINT PRIMARY_TO3PDI_PK PRIMARY KEY (PK)
	);

CREATE OR REPLACE TRIGGER TO3PDI_JOBPK_trg BEFORE
  INSERT ON TO3PDI_JOB FOR EACH ROW BEGIN 
	  IF :NEW.PK IS NULL THEN
  SELECT TO3PDI_JOBPKPK_SEQ.NEXTVAL INTO :NEW.PK FROM DUAL;
END IF;
END;

