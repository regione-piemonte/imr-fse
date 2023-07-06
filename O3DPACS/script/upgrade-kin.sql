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
	TABLESPACE O3DPACSDB18_TBL;

------------------------------------------------------------------------------------------------------------------------

CREATE TRIGGER KeyObjectsAlter AFTER UPDATE OF sopInstanceUID ON KeyObjects
  REFERENCING NEW AS NEWROW OLD AS OLDROW FOR EACH ROW BEGIN
    UPDATE KeyObjectReferences SET KeyObjectsFK =:newRow.sopInstanceUID WHERE KeyObjectsFK=:oldRow.sopInstanceUID;
END PresStatesAlter;

------------------------------------------------------------------------------------------------------------------------

CREATE TABLE KEYOBJECTREFERENCES (
	KEYOBJECTSFK VARCHAR2(64 CHAR) NOT NULL ENABLE, 
	REFSERIESINSTANCEUID VARCHAR2(64 CHAR), 
	REFSOPINSTANCEUID VARCHAR2(64 CHAR), 
	REFSOPCLASSUID VARCHAR2(64 CHAR), 
	PRIMARY KEY (KEYOBJECTSFK, REFSERIESINSTANCEUID, REFSOPINSTANCEUID) ENABLE, 
	FOREIGN KEY (KEYOBJECTSFK) REFERENCES KEYOBJECTS (SOPINSTANCEUID) ENABLE
)
TABLESPACE O3DPACSDB18_TBL;

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


-- CREATE PL/SQL ------------------------------------------------------------------------------------------------------- 
------------------------------------------------------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE getKOReferencedInstances(
  p_sopInstanceUid VARCHAR2,
  p_studyUID VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
  l_instanceTable VARCHAR2(24 CHAR);
BEGIN

    select case
      when exists (select 1 from Images i INNER JOIN KEYOBJECTREFERENCES k ON i.SOPINSTANCEUID = k.REFSOPINSTANCEUID 
      				where k.KEYOBJECTSFK = p_sopInstanceUid and rownum=1) then 'Images'
      when exists (select 5 from NonImages ni INNER JOIN KEYOBJECTREFERENCES k ON ni.SOPINSTANCEUID = k.REFSOPINSTANCEUID 
      				where k.KEYOBJECTSFK = p_sopInstanceUid and rownum=1) then 'NonImages'
      when exists (select 3 from StructReps sr INNER JOIN KEYOBJECTREFERENCES k ON sr.SOPINSTANCEUID = k.REFSOPINSTANCEUID 
      				where k.KEYOBJECTSFK = p_sopInstanceUid and rownum=1) then 'StructReps'
      when exists (select 4 from Overlays o INNER JOIN KEYOBJECTREFERENCES k ON o.SOPINSTANCEUID = k.REFSOPINSTANCEUID 
      				where k.KEYOBJECTSFK = p_sopInstanceUid and rownum=1) then 'Overlays'
      when exists (select 6 from ImagesNearline ine INNER JOIN KEYOBJECTREFERENCES k ON ine.SOPINSTANCEUID = k.REFSOPINSTANCEUID 
      				where k.KEYOBJECTSFK = p_sopInstanceUid and rownum=1) then 'ImagesNearline'
      when exists (select 7 from ImagesOffline io INNER JOIN KEYOBJECTREFERENCES k ON io.SOPINSTANCEUID = k.REFSOPINSTANCEUID 
      				where k.KEYOBJECTSFK = p_sopInstanceUid and rownum=1) then 'ImagesOffline'
      else 'PresStates'
    end INTO l_instanceTable
    from dual;

  OPEN resultset FOR
        'SELECT T.sopInstanceUID, T.sopClassUID, T.instanceNumber, T.seriesFK, MT.name FROM '||l_instanceTable||' T
        INNER JOIN SupportedSOPClasses SOP ON SOP.sopClassUid=T.sopClassUid
        INNER JOIN MimeTypes MT ON MT.id=SOP.mimeType
        INNER JOIN KeyObjectReferences K ON K.REFSOPINSTANCEUID = T.sopInstanceUID
		INNER JOIN Series S ON S.SERIESINSTANCEUID = K.REFSERIESINSTANCEUID
        WHERE K.KEYOBJECTSFK='''||p_sopInstanceUid||''' 
		AND S.STUDYFK ='''||p_studyUID||'''';

END getKOReferencedInstances;
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------


-- UPDATE PL/SQL -------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE deleteInstances(
  p_studyUid VARCHAR2
)
AS
BEGIN

   DELETE FROM HashTable WHERE sopInstanceUID IN (
      SELECT sopInstanceUID FROM PresStates ps INNER JOIN Series se ON ps.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT sopInstanceUID FROM Overlays o INNER JOIN Series se ON o.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT sopInstanceUID FROM NonImages ni INNER JOIN Series se ON ni.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT sopInstanceUID FROM StructReps sr INNER JOIN Series se ON sr.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT sopInstanceUID FROM Images i INNER JOIN Series se ON i.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT sopInstanceUID FROM ImagesNearline i INNER JOIN Series se ON i.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT sopInstanceUID FROM ImagesOffline i INNER JOIN Series se ON i.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT sopInstanceUID FROM KeyObjects k INNER JOIN Series se ON k.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
   );

  
  DELETE FROM KeyObjectReferences WHERE RefSopInstanceUID IN (
      SELECT sopInstanceUID FROM PresStates ps INNER JOIN Series se ON ps.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT sopInstanceUID FROM Overlays o INNER JOIN Series se ON o.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT sopInstanceUID FROM NonImages ni INNER JOIN Series se ON ni.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT sopInstanceUID FROM StructReps sr INNER JOIN Series se ON sr.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT sopInstanceUID FROM Images i INNER JOIN Series se ON i.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT sopInstanceUID FROM ImagesNearline i INNER JOIN Series se ON i.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT sopInstanceUID FROM ImagesOffline i INNER JOIN Series se ON i.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
  );
  DELETE FROM KeyObjects WHERE seriesFK IN (SELECT seriesInstanceUID FROM Series se WHERE se.studyFK=p_studyUid);
  
  DELETE FROM PresStatesToImages WHERE imageFK IN (
      SELECT im.sopInstanceUID FROM Images im INNER JOIN Series se ON im.seriesFK=se.seriesInstanceUID
      WHERE se.studyFK=p_studyUid
      UNION
      SELECT im.sopInstanceUID FROM ImagesNearline im INNER JOIN Series se ON im.seriesFK=se.seriesInstanceUID
      WHERE se.studyFK=p_studyUid
      UNION
      SELECT im.sopInstanceUID FROM ImagesOffline im INNER JOIN Series se ON im.seriesFK=se.seriesInstanceUID
      WHERE se.studyFK=p_studyUid
  );
  DELETE FROM PresStates WHERE seriesFK IN (SELECT seriesInstanceUID FROM Series se WHERE se.studyFK=p_studyUid);

  DELETE FROM OverlaysToImages WHERE imageFK IN(
      SELECT im.sopInstanceUID FROM Images im INNER JOIN Series se ON im.seriesFK=se.seriesInstanceUID
      WHERE se.studyFK=p_studyUid
      UNION
      SELECT im.sopInstanceUID FROM ImagesNearline im INNER JOIN Series se ON im.seriesFK=se.seriesInstanceUID
      WHERE se.studyFK=p_studyUid
      UNION
      SELECT im.sopInstanceUID FROM ImagesOffline im INNER JOIN Series se ON im.seriesFK=se.seriesInstanceUID
      WHERE se.studyFK=p_studyUid
  );
  DELETE FROM Overlays WHERE seriesFK IN (SELECT seriesInstanceUID FROM Series se WHERE se.studyFK=p_studyUid);

  DELETE FROM NonImages WHERE seriesFK IN (SELECT seriesInstanceUID FROM Series se WHERE se.studyFK=p_studyUid);
  DELETE FROM StructReps WHERE seriesFK IN (SELECT seriesInstanceUID FROM Series se WHERE se.studyFK=p_studyUid);

  DELETE FROM ImageNumberOfFrames WHERE sopInstanceUid IN(
      SELECT im.sopInstanceUID FROM Images im INNER JOIN Series se ON im.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT im.sopInstanceUID FROM ImagesNearline im INNER JOIN Series se ON im.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
      UNION
      SELECT im.sopInstanceUID FROM ImagesOffline im INNER JOIN Series se ON im.seriesFK=se.seriesInstanceUID WHERE se.studyFK=p_studyUid
  );
  DELETE FROM Images WHERE seriesFK IN (SELECT seriesInstanceUID FROM Series se WHERE se.studyFK=p_studyUid);
  DELETE FROM ImagesNearline WHERE seriesFK IN (SELECT seriesInstanceUID FROM Series se WHERE se.studyFK=p_studyUid);
  DELETE FROM ImagesOffline WHERE seriesFK IN (SELECT seriesInstanceUID FROM Series se WHERE se.studyFK=p_studyUid);
 

  COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;

END deleteInstances;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE deleteStudy(
  p_studyUid VARCHAR2,
  r_studySize OUT NUMBER,
  r_physicalMediaPk OUT NUMBER,
  r_originalUid OUT VARCHAR2
)
AS
  l_pk NUMBER(19);
BEGIN

  SELECT pk, originalUid INTO l_pk, r_originalUid FROM DeprecationEvents WHERE currentUID=p_studyUid AND eventType='DEL';
  Select studySize INTO r_studySize FROM Studies where studyInstanceUID=p_studyUid;

  DELETE FROM PresStatesToImages WHERE presStateFK IN (SELECT I.sopInstanceUID FROM PresStates I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='PS');
  DELETE FROM OverlaysToImages WHERE overlayFK IN (SELECT I.sopInstanceUID FROM Overlays I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='OV');
  DELETE FROM ImageNumberOfFrames WHERE sopInstanceUid IN (SELECT I.sopInstanceUID FROM Images I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='IM');
  DELETE FROM ImageNumberOfFrames WHERE sopInstanceUid IN (SELECT I.sopInstanceUID FROM ImagesNearline I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='IM');
  DELETE FROM ImageNumberOfFrames WHERE sopInstanceUid IN (SELECT I.sopInstanceUID FROM ImagesOffline I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='IM');
  DELETE FROM KeyObjectReferences WHERE keyObjectsFK IN (SELECT I.sopInstanceUID FROM KeyObjects I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='KO');

  DELETE FROM Images WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM Images I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='IM');
  DELETE FROM ImagesNearline WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM ImagesNearline I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='IM');
  DELETE FROM ImagesOffline WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM ImagesOffline I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='IM');
  DELETE FROM NonImages WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM NonImages I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='NI');
  DELETE FROM StructReps WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM StructReps I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='SR');
  DELETE FROM Overlays WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM Overlays I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='OV');
  DELETE FROM PresStates WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM PresStates I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='PS');
  DELETE FROM KeyObjects WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM KeyObjects I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='KO');

  DELETE FROM Series WHERE seriesInstanceUID IN (SELECT currentUid FROM DeprecationEvents WHERE parentDeprecationFK=l_pk AND deprecatedObjType='SE');

  DELETE FROM PhysiciansToStudies WHERE studyFK = p_studyUid;

  DELETE FROM WLRequestedProcedures WHERE studyFK=p_studyUid;
  DELETE FROM WLPatientDataPerVisit WHERE studyFK=p_studyUid;

  DELETE FROM StudyLocations WHERE studyFK=p_studyUid;

  DELETE FROM DeprecationEvents WHERE parentDeprecationFK=l_pk;
  UPDATE DeprecationEvents SET recoveredOn=systimestamp WHERE pk=l_pk;

  BEGIN
    Select PM.pk INTO r_physicalMediaPk
    from PhysicalMedia PM
    INNER JOIN Studies St ON St.fastestAccess LIKE CONCAT(PM.urlToStudy,'%')
    WHERE St.studyInstanceUID=p_studyUid;
  EXCEPTION WHEN TOO_MANY_ROWS THEN
    r_physicalMediaPk:=0;
  END;


END deleteStudy;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE deprecateSeries(
  p_originalUid VARCHAR2,
  p_newUid VARCHAR2,
  p_eventType VARCHAR2,
  p_reason VARCHAR2,
  p_userFk NUMBER,
  p_seriesSize NUMBER,
  r_deprecationId OUT NUMBER
)
AS

  l_numSeriesInstances NUMBER(19);
  l_studyInstanceUID VARCHAR2(64 CHAR);

BEGIN

  SELECT COUNT(seriesInstanceUID) INTO r_deprecationId FROM Series WHERE seriesInstanceUID=p_originalUid;
  IF(r_deprecationId=0) THEN
    RETURN;
  END IF;

  UPDATE Series SET seriesInstanceUID=p_newUid, deprecated=1 WHERE seriesInstanceUID=p_originalUid;

  INSERT INTO DeprecationEvents(deprecatedObjType,eventType,currentUid,originalUid,reason,deprecatedBy,deprecatedOn)
  VALUES('SE',p_eventType,p_newUid,p_originalUid,p_reason,p_userFk,SYS_EXTRACT_UTC(current_timestamp));

  SELECT DeprecationEvents_PK_SEQ.CURRVAL INTO r_deprecationId FROM DUAL;

  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'IM', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, r_deprecationId, HT.hash FROM Images I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM Images WHERE seriesFK=p_newUid);
  UPDATE Images SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;

  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'IM', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, r_deprecationId, HT.hash FROM ImagesNearline I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM ImagesNearline WHERE seriesFK=p_newUid);
  UPDATE ImagesNearline SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;

  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'IM', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, r_deprecationId, HT.hash FROM ImagesOffline I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM ImagesOffline WHERE seriesFK=p_newUid);
  UPDATE ImagesOffline SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;


  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'NI', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, r_deprecationId, HT.hash FROM NonImages I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM NonImages WHERE seriesFK=p_newUid);
  UPDATE NonImages SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;


  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'OV', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, r_deprecationId, HT.hash FROM Overlays I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM Overlays WHERE seriesFK=p_newUid);
  UPDATE Overlays SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;


  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'PS', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, r_deprecationId, HT.hash FROM PresStates I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM PresStates WHERE seriesFK=p_newUid);
  UPDATE PresStates SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;


  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'SR', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, r_deprecationId, HT.hash FROM StructReps I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM StructReps WHERE seriesFK=p_newUid);
  UPDATE StructReps SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;

  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'KO', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, r_deprecationId, HT.hash FROM KeyObjects I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM KeyObjects WHERE seriesFK=p_newUid);
  UPDATE KeyObjects SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;

 
  SELECT studyFK, numberOfSeriesRelatedInstances INTO l_studyInstanceUID, l_numSeriesInstances FROM Series where seriesInstanceUID=p_newUid;

  UPDATE Studies SET studySize=studySize-p_seriesSize, numberOfStudyRelatedSeries=numberOfStudyRelatedSeries-1, numberOfStudyRelatedInstances=numberOfStudyRelatedInstances-l_numSeriesInstances
  WHERE studyInstanceUID=l_studyInstanceUID;

END deprecateSeries;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE deprecateSeriesOfStudy(
  p_oldSeriesUid VARCHAR2,
  p_newUid VARCHAR2,
  p_parentDeprecation NUMBER
)
AS
BEGIN


  UPDATE Series SET seriesInstanceUID=p_newUid, deprecated=1 WHERE seriesInstanceUID=p_oldSeriesUid;

  INSERT INTO DeprecationEvents(deprecatedObjType,currentUid,originalUid,parentDeprecationFK)
  VALUES('SE',p_newUid,p_oldSeriesUid,p_parentDeprecation);

  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'IM', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, p_parentDeprecation, HT.hash FROM Images I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM Images WHERE seriesFK=p_newUid);
  UPDATE Images SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;

  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'IM', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, p_parentDeprecation, HT.hash FROM ImagesNearline I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM ImagesNearline WHERE seriesFK=p_newUid);
  UPDATE ImagesNearline SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;

  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'IM', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, p_parentDeprecation, HT.hash FROM ImagesOffline I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM ImagesOffline WHERE seriesFK=p_newUid);
  UPDATE ImagesOffline SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;

  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'NI', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, p_parentDeprecation, HT.hash FROM NonImages I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM NonImages WHERE seriesFK=p_newUid);
  UPDATE NonImages SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;


  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'OV', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, p_parentDeprecation, HT.hash FROM Overlays I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM Overlays WHERE seriesFK=p_newUid);
  UPDATE Overlays SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;


  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'PS', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, p_parentDeprecation, HT.hash FROM PresStates I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM PresStates WHERE seriesFK=p_newUid);
  UPDATE PresStates SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;


  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'SR', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, p_parentDeprecation, HT.hash FROM StructReps I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM StructReps WHERE seriesFK=p_newUid);
  UPDATE StructReps SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;

  INSERT INTO DeprecationEvents(deprecatedObjType, currentUid, originalUid, parentDeprecationFK, reason)
    SELECT 'KO', I.seriesFK||'_'||I.instanceNumber, I.sopInstanceUID, p_parentDeprecation, HT.hash FROM KeyObjects I
    INNER JOIN HashTable HT ON I.sopInstanceUID=HT.sopInstanceUID
    where I.seriesFK=p_newUid;
  DELETE FROM HashTable WHERE sopInstanceUID IN
    (SELECT sopInstanceUID FROM KeyObjects WHERE seriesFK=p_newUid);
  UPDATE KeyObjects SET sopInstanceUID=seriesFK||'_'||instanceNumber||'_'||FLOOR(DBMS_RANDOM.value(10000,100000)), deprecated=1 WHERE seriesFK=p_newUid;

 
END deprecateSeriesOfStudy;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE getAllSeriesFromStudy(
  p_studyUid IN VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN
  OPEN RESULTSET FOR
    SELECT SE.SERIESINSTANCEUID, SE.MODALITY, SE.NUMBEROFSERIESRELATEDINSTANCES, SE.SERIESDESCRIPTION ,

          CASE WHEN EXISTS(SELECT sopInstanceUID FROM IMAGES IM WHERE IM.SERIESFK = SE.SERIESINSTANCEUID) THEN
            (SELECT IM.SOPINSTANCEUID FROM IMAGES IM WHERE IM.SERIESFK = SE.SERIESINSTANCEUID and rownum=1)
          WHEN EXISTS(SELECT sopInstanceUID FROM ImagesNearline IM WHERE IM.SERIESFK = SE.SERIESINSTANCEUID) THEN
            (SELECT IM.SOPINSTANCEUID FROM ImagesNearline IM WHERE IM.SERIESFK = SE.SERIESINSTANCEUID and rownum=1)
          WHEN EXISTS(SELECT sopInstanceUID FROM NONIMAGES NIM WHERE NIM.SERIESFK = SE.SERIESINSTANCEUID) THEN
            (SELECT nIM.SOPINSTANCEUID FROM nonIMAGES nIM WHERE nIM.SERIESFK = SE.SERIESINSTANCEUID and rownum=1)
          WHEN EXISTS(SELECT sopInstanceUID FROM STRUCTREPS SR WHERE SR.SERIESFK = SE.SERIESINSTANCEUID) THEN
            (SELECT SR.SOPINSTANCEUID FROM STRUCTREPS SR WHERE SR.SERIESFK = SE.SERIESINSTANCEUID and rownum=1)
          WHEN EXISTS (SELECT sopInstanceUID FROM OVERLAYS OV WHERE OV.SERIESFK = SE.SERIESINSTANCEUID) THEN
            (SELECT OV.SOPINSTANCEUID FROM OVERLAYS OV WHERE OV.SERIESFK = SE.SERIESINSTANCEUID and rownum=1)
          WHEN EXISTS(SELECT sopInstanceUID FROM PRESSTATES PS WHERE PS.SERIESFK = SE.SERIESINSTANCEUID) THEN
            (SELECT PS.SOPINSTANCEUID FROM PRESSTATES PS WHERE PS.SERIESFK = SE.SERIESINSTANCEUID AND ROWNUM=1)
          WHEN EXISTS(SELECT sopInstanceUID FROM KEYOBJECTS K WHERE K.SERIESFK = SE.SERIESINSTANCEUID) THEN
            (SELECT K.SOPINSTANCEUID FROM KEYOBJECTS K WHERE K.SERIESFK = SE.SERIESINSTANCEUID AND ROWNUM=1)  
          else
             NULL
          END AS sopInstanceUID
    FROM SERIES SE
    WHERE SE.STUDYFK = P_STUDYUID and SE.DEPRECATED = 0;
END getAllSeriesFromStudy;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE getDataForCMove(
  p_patientId IN VARCHAR2,
  p_studyUid IN VARCHAR2,
  p_seriesUid IN VARCHAR2,
  p_instanceUid IN VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
  l_query VARCHAR2(4000);
  l_queryEnd VARCHAR2(200);
BEGIN

  l_query:='SELECT  I.sopInstanceUID, I.sopClassUID,'||
           ' Se.seriesInstanceUID,'||
           ' St.studyInstanceUID,'||
           ' P.patientID, P.idIssuer, P.lastName, P.firstName, P.middleName, P.prefix, P.suffix, P.birthDate, P.sex, P.pk,'||
           ' I.instanceType, St.studyStatus,'||
           ' NULL, St.fastestAccess, NULL';

  l_queryEnd:=' AND I.deprecated=0 AND Se.deprecated=0'||
             ' ORDER BY St.studyInstanceUID ASC, Se.seriesInstanceUID, I.instanceNumber ASC, I.sopInstanceUID ASC';


  IF (p_instanceUid IS NOT NULL) THEN      -- INSTANCE LEVEL     SEVERAL InstanceUIDs, coming as 1.2.3   OR  1.2.3','9.8.7','56.786.445
    l_query:= l_query||' FROM ('||
                       ' Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM Images WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ' UNION ALL'||
                       ' Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM ImagesNearline WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ' UNION ALL'||
                       ' Select ''NI'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM NonImages WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ' UNION ALL'||
                       ' Select ''SR'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM StructReps WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ' UNION ALL'||
                       ' Select ''PS'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM PresStates WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ' UNION ALL'||
                       ' Select ''OV'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM Overlays WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ' UNION ALL'||
                       ' Select ''KO'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM KeyObjects WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ') I'||
                       ' INNER JOIN Series Se ON Se.seriesInstanceUID=I.seriesFK'||
                       ' INNER JOIN Studies St ON St.studyInstanceUID=Se.studyFK'||
                       ' INNER JOIN Patients P ON P.pk=St.patientFK'||
--                       ' LEFT JOIN StoragePolicies SP ON SP.id= St.storagePolicyId'||
--                       ' LEFT JOIN NearlineStorageSettings NSS ON NSS.id=SP.nearlineStorageSettingsId'||
--                       ' LEFT JOIN StorageDeviceTypes SDT ON SDT.id=NSS.storageDeviceTypeId'||
--                       ' LEFT JOIN HashTable HT ON HT.sopInstanceUID=I.sopInstanceUID'||
                       ' WHERE St.studyStatus <> ''p'' AND I.sopInstanceUID IN ('''|| p_instanceUid||''')';
  ELSIF (p_seriesUid IS NOT NULL) THEN      -- SERIES LEVEL     SEVERAL SeriesUIDs, coming as 1.2.3   OR  1.2.3','9.8.7','56.786.445
    l_query:= l_query||' FROM Series Se'||
                       ' INNER JOIN ('||
                       '  Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM Images WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM ImagesNearline WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''NI'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM NonImages WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''SR'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM StructReps WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''PS'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM PresStates WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''OV'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM Overlays WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
					   '  UNION ALL'||
                       '  Select ''KO'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM KeyObjects WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
                       ')I ON I.seriesFK=Se.seriesInstanceUID'||
                       ' INNER JOIN Studies St ON St.studyInstanceUID=Se.studyFK'||
                       ' INNER JOIN Patients P ON P.pk=St.patientFK'||
--                       ' LEFT JOIN StoragePolicies SP ON SP.id= St.storagePolicyId'||
--                       ' LEFT JOIN NearlineStorageSettings NSS ON NSS.id=SP.nearlineStorageSettingsId'||
--                       ' LEFT JOIN StorageDeviceTypes SDT ON SDT.id=NSS.storageDeviceTypeId'||
--                       ' LEFT JOIN HashTable HT ON HT.sopInstanceUID=I.sopInstanceUID'||
                       ' WHERE St.studyStatus <> ''p'' AND Se.seriesInstanceUID IN ('''|| p_seriesUid||''')';
  ELSIF (p_studyUid IS NOT NULL) THEN      -- STUDY LEVEL     SEVERAL StudyUIDs, coming as 1.2.3   OR  1.2.3','9.8.7','56.786.445
    l_query:= l_query||' FROM Studies St'||
                       ' INNER JOIN Patients P ON P.pk=St.patientFK'||
                       ' INNER JOIN Series Se ON Se.studyFK=St.studyInstanceUID'||
                       ' INNER JOIN ('||
                       '  Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM Images U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM ImagesNearline U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''NI'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM NonImages U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''SR'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM StructReps U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''PS'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM PresStates U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''OV'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM Overlays U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''KO'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM KeyObjects U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       ')I ON I.seriesFK=Se.seriesInstanceUID'||
--                       ' LEFT JOIN StoragePolicies SP ON SP.id= St.storagePolicyId'||
--                       ' LEFT JOIN NearlineStorageSettings NSS ON NSS.id=SP.nearlineStorageSettingsId'||
--                       ' LEFT JOIN StorageDeviceTypes SDT ON SDT.id=NSS.storageDeviceTypeId'||
--                       ' LEFT JOIN HashTable HT ON HT.sopInstanceUID=I.sopInstanceUID'||
                       ' WHERE St.studyStatus <> ''p'' AND St.studyinstanceUID IN('''|| p_studyUid||''')';
  ELSIF(p_patientId IS NOT NULL) THEN      -- PATIENT LEVEL      JUST ONE PatientID
    l_query:= l_query||' FROM Patients P'||
                       ' INNER JOIN Studies St ON  St.patientFK=P.pk'||
                       ' INNER JOIN Series Se ON Se.studyFK=St.studyInstanceUID'||
                       ' INNER JOIN ('||
                       '  Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM Images U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM ImagesNearline U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''NI'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM NonImages U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''SR'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM StructReps U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''PS'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM PresStates U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''OV'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM Overlays U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''KO'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM KeyObjects U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       ')I ON I.seriesFK=Se.seriesInstanceUID'||
--                       ' LEFT JOIN StoragePolicies SP ON SP.id= St.storagePolicyId'||
--                       ' LEFT JOIN NearlineStorageSettings NSS ON NSS.id=SP.nearlineStorageSettingsId'||
--                       ' LEFT JOIN StorageDeviceTypes SDT ON SDT.id=NSS.storageDeviceTypeId'||
--                       ' LEFT JOIN HashTable HT ON HT.sopInstanceUID=I.sopInstanceUID'||
                       ' WHERE St.studyStatus <> ''p'' AND P.patientID ='''|| p_patientId||'''';
  END IF;
  l_query:= l_query||l_queryEnd;
  OPEN resultSet FOR l_query;
END getDataForCMove;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE GETINSTANCELEVELMETADATA (
  p_seriesInstanceUid VARCHAR2,
  resultSet OUT Types.ResultSet
)
AS
BEGIN

  OPEN resultSet FOR
    Select sopInstanceUID, sopClassUID, instanceNumber, seriesFK FROM Images WHERE seriesFK=p_seriesInstanceUid

    UNION

    Select sopInstanceUID, sopClassUID, instanceNumber, seriesFK FROM ImagesNearline WHERE seriesFK=p_seriesInstanceUid

    UNION

    Select sopInstanceUID, sopClassUID, instanceNumber, seriesFK FROM NonImages WHERE seriesFK=p_seriesInstanceUid

    UNION

    Select sopInstanceUID, sopClassUID, instanceNumber, seriesFK FROM PresStates WHERE seriesFK=p_seriesInstanceUid

    UNION

    Select sopInstanceUID, sopClassUID, instanceNumber, seriesFK FROM Overlays WHERE seriesFK=p_seriesInstanceUid

    UNION

    Select sopInstanceUID, sopClassUID, instanceNumber, seriesFK FROM StructReps WHERE seriesFK=p_seriesInstanceUid
   
    UNION

    Select sopInstanceUID, sopClassUID, instanceNumber, seriesFK FROM KeyObjects WHERE seriesFK=p_seriesInstanceUid;

END getInstanceLevelMetadata;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE getInstancesOfVerifiedStudy(
    studyUID IN VARCHAR2,
    resultset OUT Types.ResultSet
)
AS
BEGIN
  OPEN resultset FOR
SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, I.sopInstanceUID, I.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
FROM studiesToVerify STV
INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
INNER JOIN Patients P ON P.pk = Std.patientFk
INNER JOIN Series Se ON stv.studyfk = se.studyFK
INNER JOIN Images I ON Se.seriesInstanceUID = I.seriesFK
WHERE stv.verifiedDate IS NOT NULL AND
(std.studyinstanceuid = studyUID AND stv.jobfinishedon IS NULL)
UNION ALL
SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, NI.sopInstanceUID, NI.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
FROM studiesToVerify STV
INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
INNER JOIN Patients P ON P.pk = Std.patientFk
INNER JOIN Series Se ON stv.studyfk = se.studyFK
INNER JOIN NonImages NI ON Se.seriesInstanceUID = NI.seriesFK
WHERE stv.verifiedDate IS NOT NULL and
(std.studyinstanceuid = studyUID AND stv.jobfinishedon IS NULL)
UNION ALL
SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, SR.sopInstanceUID, SR.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
FROM studiesToVerify STV
INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
INNER JOIN Patients P ON P.pk = Std.patientFk
INNER JOIN Series Se ON stv.studyfk = se.studyFK
INNER JOIN StructReps SR ON Se.seriesInstanceUID = SR.seriesFK
WHERE stv.verifiedDate IS NOT NULL and
(std.studyinstanceuid = studyUID AND stv.jobfinishedon IS NULL)
UNION ALL
SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, O.sopInstanceUID, O.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
FROM studiesToVerify STV
INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
INNER JOIN Patients P ON P.pk = Std.patientFk
INNER JOIN Series Se ON stv.studyfk = se.studyFK
INNER JOIN Overlays O ON Se.seriesInstanceUID = O.seriesFK
WHERE stv.verifiedDate IS NOT NULL AND
(std.studyinstanceuid = studyUID AND stv.jobfinishedon IS NULL)
UNION ALL
SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, PS.sopInstanceUID, PS.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
FROM studiesToVerify STV
INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
INNER JOIN Patients P ON P.pk = Std.patientFk
INNER JOIN Series Se ON stv.studyfk = se.studyFK
INNER JOIN PresStates PS ON Se.seriesInstanceUID = PS.seriesFK
WHERE stv.verifiedDate IS NOT NULL AND
(std.studyinstanceuid = studyUID AND stv.jobfinishedon IS NULL)
UNION ALL
SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, KO.sopInstanceUID, KO.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
FROM studiesToVerify STV
INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
INNER JOIN Patients P ON P.pk = Std.patientFk
INNER JOIN Series Se ON stv.studyfk = se.studyFK
INNER JOIN KeyObjects KO ON Se.seriesInstanceUID = KO.seriesFK
WHERE stv.verifiedDate IS NOT NULL and
(std.studyinstanceuid = studyUID AND stv.jobfinishedon IS NULL);
END getInstancesOfVerifiedStudy;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE getRetrievalInfo(
  p_studyUid IN VARCHAR2,
  p_seriesUid IN VARCHAR2,
  p_instanceUid IN VARCHAR2,

  r_patientName OUT VARCHAR2,
  r_patientId OUT VARCHAR2,
  r_idIssuer OUT VARCHAR2,
  r_birthDate OUT DATE,
  r_sex OUT VARCHAR2,

  r_studyId OUT VARCHAR2,
  r_studyDate OUT DATE,
  r_studyTime OUT DATE,
  r_studyCompletionDate OUT DATE,
  r_studyCompletionTime OUT DATE,
  r_studyVerifiedDate OUT DATE,
  r_studyVerifiedTime OUT DATE,
  r_accessionNumber OUT VARCHAR2,

  resultSet OUT Types.ResultSet
)
AS
  l_query VARCHAR2(4000);
  l_query_instanceBranchOne VARCHAR2(4000);
  l_query_instanceBranchMany VARCHAR2(4000);
  l_queryMiddle VARCHAR2(4000);
  l_queryEnd VARCHAR2(200);
  l_studyStatus CHAR(1);
BEGIN


  BEGIN
    SELECT (P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix) ,P.patientID, P.idIssuer, P.birthDate, P.sex,
          St.studyID, St.studyDate, St.studyTime, St.studyCompletionDate, St.studyCompletionTime, St.studyVerifiedDate, St.studyVerifiedTime, St.accessionNumber, St.studyStatus
      INTO r_patientName, r_patientId, r_idIssuer, r_birthDate, r_sex,
           r_studyId, r_studyDate, r_studyTime, r_studyCompletionDate, r_studyCompletionTime, r_studyVerifiedDate, r_studyVerifiedTime, r_accessionNumber, l_studyStatus
    FROM Patients P
    INNER JOIN Studies St ON St.patientFK = P.pk
    WHERE St.studyInstanceUID = p_studyUid AND St.studyStatus<>'p';
  EXCEPTION WHEN NO_DATA_FOUND THEN
    RETURN; -- No ResultSet is returned when the study is not present or offline
  END;



  -- l_query:='SELECT St.studyInstanceUID, St.patientFK, St.studyStatus, Se.seriesInstanceUID, I.sopInstanceUID, SDT.name, St.fastestAccess, HT.tokenToFile'||
  l_query:='SELECT St.studyInstanceUID, St.patientFK, St.studyStatus, Se.seriesInstanceUID, I.sopInstanceUID, NULL, St.fastestAccess, NULL'||
           ' FROM Studies St'||
           ' INNER JOIN Series Se ON Se.studyFK = St.studyInstanceUID'||
           ' INNER JOIN ';

  l_query_instanceBranchOne:='('||
                             ' SELECT sopInstanceUID, seriesFK FROM Images'||CASE WHEN(l_studyStatus ='n')THEN 'Nearline' ELSE '' END||' WHERE seriesFK='''||p_seriesUid||''''||
                             ' UNION ALL'||
                             ' SELECT sopInstanceUID, seriesFK FROM NonImages WHERE seriesFK='''||p_seriesUid||''''||
                             ' UNION ALL'||
                             ' SELECT sopInstanceUID, seriesFK FROM StructReps WHERE seriesFK='''||p_seriesUid||''''||
                             ' UNION ALL'||
                             ' SELECT sopInstanceUID, seriesFK FROM PresStates WHERE seriesFK='''||p_seriesUid||''''||
                             ' UNION ALL'||
                             ' SELECT sopInstanceUID, seriesFK FROM Overlays WHERE seriesFK='''||p_seriesUid||''''||
                             ' UNION ALL'||
                             ' SELECT sopInstanceUID, seriesFK FROM KeyObjects WHERE seriesFK='''||p_seriesUid||''''||
                             ')';

  l_query_instanceBranchMany:='('||
                                 '  Select sopInstanceUID, seriesFK, instanceNumber FROM Images'||CASE WHEN(l_studyStatus ='n')THEN 'Nearline' ELSE '' END||' U'||
                                 '  INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                                 '  WHERE Ser.studyFK ='''||p_studyUid||''''||
                                 '  UNION ALL'||
                                 '  Select sopInstanceUID, seriesFK, instanceNumber FROM NonImages U'||
                                 '  INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                                 '  WHERE Ser.studyFK ='''||p_studyUid||''''||
                                 '  UNION ALL'||
                                 '  Select sopInstanceUID, seriesFK, instanceNumber FROM StructReps U'||
                                 '  INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                                 '  WHERE Ser.studyFK ='''||p_studyUid||''''||
                                 '  UNION ALL'||
                                 '  Select sopInstanceUID, seriesFK, instanceNumber FROM PresStates U'||
                                 '  INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                                 '  WHERE Ser.studyFK ='''||p_studyUid||''''||
                                 '  UNION ALL'||
                                 '  Select sopInstanceUID, seriesFK, instanceNumber FROM Overlays U'||
                                 '  INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                                 '  WHERE Ser.studyFK ='''||p_studyUid||''''||
                                 '  UNION ALL'||
                                 '  Select sopInstanceUID, seriesFK, instanceNumber FROM KeyObjects U'||
                                 '  INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                                 '  WHERE Ser.studyFK ='''||p_studyUid||''''||
                                 ')';

  -- l_queryMiddle:=' I ON I.seriesFK = Se.seriesInstanceUID'||
  --               ' LEFT JOIN StoragePolicies SP ON SP.id= St.storagePolicyId'||
  --               ' LEFT JOIN NearlineStorageSettings NSS ON NSS.id=SP.nearlineStorageSettingsId'||
  --               ' LEFT JOIN StorageDeviceTypes SDT ON SDT.id=NSS.storageDeviceTypeId'||
  --               ' LEFT JOIN HashTable HT ON HT.sopInstanceUID=I.sopInstanceUID'||
  --               ' WHERE St.studyInstanceUID='''||p_studyUid||'''';

  l_queryMiddle:=' I ON I.seriesFK = Se.seriesInstanceUID'||
                 ' WHERE St.studyInstanceUID='''||p_studyUid||'''';

  l_queryEnd:=' AND Se.seriesInstanceUID='''||p_seriesUid||''''||
              ' AND I.sopInstanceUID='''||p_instanceUid||'''';


  IF (p_instanceUid IS NOT NULL AND p_seriesUid IS NOT NULL) THEN     -- A definite instance was requested
    l_query:= l_query||l_query_instanceBranchOne||l_queryMiddle||l_queryEnd;
  ELSE
   l_query:= l_query||l_query_instanceBranchMany||l_queryMiddle||' ORDER BY St.studyInstanceUID ASC, Se.seriesInstanceUID, I.instanceNumber ASC, I.sopInstanceUID ASC';
  END IF;

  OPEN resultSet FOR l_query;

END getRetrievalInfo;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE getSeriesInstances(
  p_seriesInstanceUid VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
  l_instanceTable VARCHAR2(24 CHAR);
BEGIN

    select case
      when exists (select 1 from Images where seriesFK = p_seriesInstanceUid and rownum=1) then 'Images'
      when exists (select 5 from NonImages where seriesFK = p_seriesInstanceUid and rownum=1) then 'NonImages'
      when exists (select 3 from StructReps where seriesFK = p_seriesInstanceUid and rownum=1) then 'StructReps'
      when exists (select 4 from Overlays where seriesFK = p_seriesInstanceUid and rownum=1) then 'Overlays'
      when exists (select 6 from ImagesNearline where seriesFK = p_seriesInstanceUid and rownum=1) then 'ImagesNearline'
      when exists (select 7 from ImagesOffline where seriesFK = p_seriesInstanceUid and rownum=1) then 'ImagesOffline'
      when exists (select 8 from KeyObjects where seriesFK = p_seriesInstanceUid and rownum=1) then 'KeyObjects'
      else 'PresStates'
    end INTO l_instanceTable
    from dual;

  OPEN resultset FOR
        'SELECT T.sopInstanceUID, T.sopClassUID, T.instanceNumber, MT.name FROM '||l_instanceTable||' T
        INNER JOIN SupportedSOPClasses SOP ON SOP.sopClassUid=T.sopClassUid
        INNER JOIN MimeTypes MT ON MT.id=SOP.mimeType
        WHERE T.seriesFK='''||p_seriesInstanceUid||'''';

END getSeriesInstances;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE GETURLTOINSTANCE (
  p_sopInstanceUid VARCHAR2,
  p_url OUT VARCHAR2
)
AS
BEGIN

  BEGIN
    SELECT fastestAccess||studyInstanceUid||'/'||seriesInstanceUid||'/'||p_sopInstanceUid INTO p_url
      FROM Studies St
      INNER JOIN Series Se ON Se.studyFK=St.studyInstanceUID
      INNER JOIN (
        SELECT seriesFK FROM Images WHERE sopInstanceUID=p_sopInstanceUid
        UNION ALL
        SELECT seriesFK FROM NonImages WHERE sopInstanceUID=p_sopInstanceUid
        UNION ALL
        SELECT seriesFK FROM PresStates WHERE sopInstanceUID=p_sopInstanceUid
        UNION ALL
        SELECT seriesFK FROM StructReps WHERE sopInstanceUID=p_sopInstanceUid
        UNION ALL
        SELECT seriesFK FROM Overlays WHERE sopInstanceUID=p_sopInstanceUid
        UNION ALL
        SELECT seriesFK FROM KeyObjects WHERE sopInstanceUID=p_sopInstanceUid
      ) I ON I.seriesFK=Se.seriesInstanceUID;

  EXCEPTION WHEN NO_DATA_FOUND THEN
    p_url:=NULL;
  END;


END getUrlToInstance;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE getUrlToOnlineInstance(
  p_sopInstanceUid VARCHAR2,
  p_url OUT VARCHAR2
)
AS
BEGIN

  BEGIN
    SELECT fastestAccess||studyInstanceUid||'/'||seriesInstanceUid||'/'||p_sopInstanceUid INTO p_url
      FROM Studies St
      INNER JOIN Series Se ON Se.studyFK=St.studyInstanceUID
      INNER JOIN (
        SELECT seriesFK FROM Images WHERE sopInstanceUID=p_sopInstanceUid
        UNION ALL
        SELECT seriesFK FROM NonImages WHERE sopInstanceUID=p_sopInstanceUid
        UNION ALL
        SELECT seriesFK FROM PresStates WHERE sopInstanceUID=p_sopInstanceUid
        UNION ALL
        SELECT seriesFK FROM StructReps WHERE sopInstanceUID=p_sopInstanceUid
        UNION ALL
        SELECT seriesFK FROM Overlays WHERE sopInstanceUID=p_sopInstanceUid
        UNION ALL
        SELECT seriesFK FROM KeyObjects WHERE sopInstanceUID=p_sopInstanceUid
      ) I ON I.seriesFK=Se.seriesInstanceUID;

  EXCEPTION WHEN NO_DATA_FOUND THEN
    p_url:=NULL;
  END;


END getUrlToOnlineInstance;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE getVerifiedStudiesInstances
  (
    resultset OUT Types.ResultSet
  )
AS
BEGIN
  OPEN resultset FOR
	SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, I.sopInstanceUID, I.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
	FROM studiesToVerify STV
	INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
	INNER JOIN Patients P ON P.pk = Std.patientFk
	INNER JOIN Series Se ON stv.studyfk = se.studyFK
	INNER JOIN Images I ON Se.seriesInstanceUID = I.seriesFK
	WHERE stv.verifiedDate IS NOT NULL and
	(stv.toBeIgnored = 0 AND stv.jobfinishedon is null)
	UNION ALL
	SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, NI.sopInstanceUID, NI.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
	FROM studiesToVerify STV
	INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
	INNER JOIN Patients P ON P.pk = Std.patientFk
	INNER JOIN Series Se ON stv.studyfk = se.studyFK
	INNER JOIN NonImages NI ON Se.seriesInstanceUID = NI.seriesFK
	WHERE stv.verifiedDate IS NOT NULL and
	(stv.toBeIgnored = 0 AND stv.jobfinishedon is null)
	UNION ALL
	SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, SR.sopInstanceUID, SR.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
	FROM studiesToVerify STV
	INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
	INNER JOIN Patients P ON P.pk = Std.patientFk
	INNER JOIN Series Se ON stv.studyfk = se.studyFK
	INNER JOIN StructReps SR ON Se.seriesInstanceUID = SR.seriesFK
	WHERE stv.verifiedDate IS NOT NULL and
	(stv.toBeIgnored = 0 AND stv.jobfinishedon is null)
	UNION ALL
	SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, O.sopInstanceUID, O.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
	FROM studiesToVerify STV
	INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
	INNER JOIN Patients P ON P.pk = Std.patientFk
	INNER JOIN Series Se ON stv.studyfk = se.studyFK
	INNER JOIN Overlays O ON Se.seriesInstanceUID = O.seriesFK
	WHERE stv.verifiedDate IS NOT NULL and
	(stv.toBeIgnored = 0 AND stv.jobfinishedon is null)
	UNION ALL
	SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, PS.sopInstanceUID, PS.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
	FROM studiesToVerify STV
	INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
	INNER JOIN Patients P ON P.pk = Std.patientFk
	INNER JOIN Series Se ON stv.studyfk = se.studyFK
	INNER JOIN PresStates PS ON Se.seriesInstanceUID = PS.seriesFK
	WHERE stv.verifiedDate IS NOT NULL and
	(stv.toBeIgnored = 0 AND stv.jobfinishedon is null)
	UNION ALL
	SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, KO.sopInstanceUID, KO.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
	FROM studiesToVerify STV
	INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
	INNER JOIN Patients P ON P.pk = Std.patientFk
	INNER JOIN Series Se ON stv.studyfk = se.studyFK
	INNER JOIN KeyObjects KO ON Se.seriesInstanceUID = KO.seriesFK
	WHERE stv.verifiedDate IS NOT NULL and
	(stv.toBeIgnored = 0 AND stv.jobfinishedon is null);
END getVerifiedStudiesInstances;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE queryInstanceLevel(
  p_seriesInstanceUid VARCHAR2,
  p_multipleInstanceUids NUMBER,    -- If 1-> p_sopInstanceUID is a list and should be used in an IN statement. Ignored if p_sopInstanceUID is NULL
  p_multipleSopClasses NUMBER,    -- If 1-> p_sopClassUid is a list and should be used in an IN statement. Ignored if p_sopClassUid is NULL

  p_sopInstanceUID VARCHAR2, -- as list, potentially. Separated by backslash in DICOM, passed in this form: '1.2.3','1.2.3.4','5.6.7'
  p_sopClassUid VARCHAR2, -- as list, potentially. Separated by backslash in DICOM, passed in this form: '1.2.3','1.2.3.4','5.6.7'
  p_instanceNumber NUMBER,

  p_samplesPerPixel NUMBER,
  p_rowsNum NUMBER,
  p_columnsNum NUMBER,
  p_bitsAllocated NUMBER,
  p_bitsStored NUMBER,
  p_highBit NUMBER,
  p_pixelRepresentation NUMBER,
  p_numberOfFrames NUMBER,    -- IF NOT NULL THE numberOfFrames IS RETURNED

  p_presentationLabel VARCHAR2,	-- UPPER
  p_presentationDescription VARCHAR2,	-- UPPER
  p_presentationCreationDate DATE,
  p_presentationCreationDateLATE DATE,
  p_presentationCreationTime DATE,
  p_presentationCreationTimeLATE DATE,
  p_presentationCreatorsName VARCHAR2, --UPPER
  p_recommendedViewingMode VARCHAR2, -- UPPER

  p_completionFlag VARCHAR2,	-- LIKE UPPER. The wildcards must be already present in the parameter
  p_verificationFlag VARCHAR2,	-- LIKE UPPER. The wildcards must be already present in the parameter
  p_contentDate DATE,
  p_contentDateLATE DATE,
  p_contentTime DATE,
  p_contentTimeLATE DATE,
  p_observationDateTime DATE,
  p_observationDateTimeLATE DATE,

  p_overlayNumber NUMBER,
  p_overlayRows NUMBER,
  p_overlayColumns NUMBER,
  p_overlayType VARCHAR2,	-- UPPER
  p_overlayBitsAllocated NUMBER,


  r_studyInstanceUID OUT VARCHAR2,
  r_fastestAccess OUT VARCHAR2,
  r_studyStatus OUT CHAR,
  r_specificCharSet OUT VARCHAR2,
  resultset OUT Types.ResultSet
)
AS

  l_query VARCHAR2(4000 CHAR);
  l_commonJoin VARCHAR2(200 CHAR);
  l_commonWhere VARCHAR2(200 CHAR);
  l_instanceType NUMBER(1);

BEGIN

    BEGIN
    	SELECT St.studyInstanceUID, St.fastestAccess, St.studyStatus, St.specificCharSet INTO r_studyInstanceUID, r_fastestAccess, r_studyStatus, r_specificCharSet
    	FROM Series Se
    	INNER JOIN Studies St ON St.studyInstanceUID=Se.studyFK
    	WHERE Se.seriesInstanceUID=p_seriesInstanceUid;
    EXCEPTION WHEN NO_DATA_FOUND THEN
    	RETURN;
  	END;


    select case
                when exists (select 1 from Images where seriesFK = p_seriesInstanceUid and rownum=1) then 1
                when exists (select 1 from ImagesNearline where seriesFK = p_seriesInstanceUid and rownum=1) then 1
                when exists (select 1 from ImagesOffline where seriesFK = p_seriesInstanceUid and rownum=1) then 1
                when exists (select 5 from NonImages where seriesFK = p_seriesInstanceUid and rownum=1) then 5
                when exists (select 3 from StructReps where seriesFK = p_seriesInstanceUid and rownum=1) then 3
                when exists (select 4 from Overlays where seriesFK = p_seriesInstanceUid and rownum=1) then 4
                when exists (select 6 from KeyObjects where seriesFK = p_seriesInstanceUid and rownum=1) then 6
                else 2
            end INTO l_instanceType
    from dual;

    l_query:='SELECT '||l_instanceType||', I.sopInstanceUID, I.sopClassUID, I.instanceNumber';
    l_commonWhere:=' WHERE I.seriesFK='''||p_seriesInstanceUid||''' AND I.deprecated=0';

    IF(p_sopInstanceUID IS NOT NULL)THEN
      IF(p_multipleInstanceUids=1) THEN
        l_commonWhere:=l_commonWhere||' AND I.sopInstanceUID IN ('||p_sopInstanceUID||')';
      ELSE
        l_commonWhere:=l_commonWhere||' AND I.sopInstanceUID = '''||p_sopInstanceUID||'''';
      END IF;
    END IF;
    IF(p_sopClassUid IS NOT NULL)THEN
      IF(p_multipleSopClasses=1) THEN
        l_commonWhere:=l_commonWhere||' AND I.sopClassUID IN ('||p_sopClassUid||')';
      ELSE
        l_commonWhere:=l_commonWhere||' AND I.sopClassUID = '''||p_sopClassUid||'''';
      END IF;
    END IF;
    IF(p_instanceNumber IS NOT NULL)THEN
      l_commonWhere:=l_commonWhere||' AND I.instanceNumber = '||p_instanceNumber;
    END IF;

    CASE l_instanceType
      WHEN 1 THEN   -- Images
        BEGIN

        IF(p_presentationLabel IS NOT NULL OR p_presentationDescription IS NOT NULL OR p_presentationCreationDate IS NOT NULL OR p_presentationCreationDateLATE IS NOT NULL OR p_presentationCreationTime IS NOT NULL OR p_presentationCreationTimeLATE IS NOT NULL OR p_presentationCreatorsName IS NOT NULL OR p_recommendedViewingMode IS NOT NULL OR
           p_completionFlag IS NOT NULL OR p_verificationFlag IS NOT NULL OR p_contentDate IS NOT NULL OR p_contentDateLATE IS NOT NULL OR p_contentTime IS NOT NULL OR p_contentTimeLATE IS NOT NULL OR p_observationDateTime IS NOT NULL OR p_observationDateTimeLATE IS NOT NULL OR
           p_overlayNumber IS NOT NULL OR p_overlayRows IS NOT NULL OR p_overlayColumns IS NOT NULL OR p_overlayType IS NOT NULL OR p_overlayBitsAllocated IS NOT NULL) THEN
           RETURN;
        END IF;

          l_query:=l_query|| ', I.samplesPerPixel, I.rowsNum, I.columnsNum, I.bitsAllocated, I.bitsStored, I.highBit, I.pixelRepresentation';
          l_commonJoin:=' FROM Images'||CASE WHEN(r_studyStatus ='n')THEN 'Nearline' WHEN (r_studyStatus ='p') THEN 'Offline' ELSE '' END||' I';
          IF (p_numberOfFrames IS NOT NULL) THEN
            l_query:=l_query|| ', Inof.numberOfFrames';
            l_commonJoin:=l_commonJoin||' LEFT JOIN ImageNumberOfFrames Inof ON Inof.sopInstanceUid=I.sopInstanceUID';
          ELSE
            l_query:=l_query|| ', NULL';
          END IF;

          -- WHERE fields:
          IF(p_samplesPerPixel IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.samplesPerPixel = '||p_samplesPerPixel;
          END IF;
          IF(p_rowsNum IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.rowsNum = '||p_rowsNum;
          END IF;
          IF(p_columnsNum IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.columnsNum = '||p_columnsNum;
          END IF;
          IF(p_bitsAllocated IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.bitsAllocated = '||p_bitsAllocated;
          END IF;
          IF(p_bitsStored IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.bitsStored = '||p_bitsStored;
          END IF;
          IF(p_highBit IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.highBit = '||p_highBit;
          END IF;
          IF(p_pixelRepresentation IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.pixelRepresentation = '||p_pixelRepresentation;
          END IF;

        END;
      WHEN 2 THEN   -- PresStates
        BEGIN
          IF(p_samplesPerPixel IS NOT NULL OR p_rowsNum IS NOT NULL OR p_columnsNum IS NOT NULL OR p_bitsAllocated IS NOT NULL OR p_bitsStored IS NOT NULL OR p_highBit IS NOT NULL OR p_pixelRepresentation IS NOT NULL OR p_numberOfFrames IS NOT NULL OR
             p_completionFlag IS NOT NULL OR p_verificationFlag IS NOT NULL OR p_contentDate IS NOT NULL OR p_contentDateLATE IS NOT NULL OR p_contentTime IS NOT NULL OR p_contentTimeLATE IS NOT NULL OR p_observationDateTime IS NOT NULL OR p_observationDateTimeLATE IS NOT NULL OR
             p_overlayNumber IS NOT NULL OR p_overlayRows IS NOT NULL OR p_overlayColumns IS NOT NULL OR p_overlayType IS NOT NULL OR p_overlayBitsAllocated IS NOT NULL) THEN
             RETURN;
          END IF;

          l_query:=l_query|| ', I.presentationLabel, I.presentationDescription, I.presentationCreationDate, I.presentationCreationTime, I.presentationCreatorsName, I.recommendedViewingMode';
          l_commonJoin:=l_commonJoin||' FROM PresStates I';

          -- WHERE fields:
          IF(p_presentationLabel IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.presentationLabel = UPPER('''||p_presentationLabel||''')';
          END IF;
          IF(p_presentationDescription IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.presentationDescription = UPPER('''||p_presentationDescription||''')';
          END IF;

          IF(p_presentationCreationDate IS NOT NULL)THEN
            IF(p_presentationCreationDateLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.presentationCreationDate = '''||p_presentationCreationDate||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.presentationCreationDate >= '''||p_presentationCreationDate||''' AND I.presentationCreationDate <= '''||p_presentationCreationDateLATE||''')';
            END IF;
          END IF;
          IF(p_presentationCreationTime IS NOT NULL)THEN
            IF(p_presentationCreationTimeLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.presentationCreationTime = '''||p_presentationCreationTime||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.presentationCreationTime >= '''||p_presentationCreationTime||''' AND I.presentationCreationTime <= '''||p_presentationCreationTimeLATE||''')';
            END IF;
          END IF;

          IF(p_presentationCreatorsName IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.presentationCreatorsName = UPPER('''||p_presentationCreatorsName||''')';
          END IF;
          IF(p_recommendedViewingMode IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.recommendedViewingMode = UPPER('''||p_recommendedViewingMode||''')';
          END IF;

        END;
      WHEN 3 THEN   -- StructReps
        BEGIN

          IF(p_samplesPerPixel IS NOT NULL OR p_rowsNum IS NOT NULL OR p_columnsNum IS NOT NULL OR p_bitsAllocated IS NOT NULL OR p_bitsStored IS NOT NULL OR p_highBit IS NOT NULL OR p_pixelRepresentation IS NOT NULL OR p_numberOfFrames IS NOT NULL OR
             p_presentationLabel IS NOT NULL OR p_presentationDescription IS NOT NULL OR p_presentationCreationDate IS NOT NULL OR p_presentationCreationDateLATE IS NOT NULL OR p_presentationCreationTime IS NOT NULL OR p_presentationCreationTimeLATE IS NOT NULL OR p_presentationCreatorsName IS NOT NULL OR p_recommendedViewingMode IS NOT NULL OR
             p_overlayNumber IS NOT NULL OR p_overlayRows IS NOT NULL OR p_overlayColumns IS NOT NULL OR p_overlayType IS NOT NULL OR p_overlayBitsAllocated IS NOT NULL) THEN
             RETURN;
          END IF;

          l_query:=l_query|| ', I.completionFlag, I.verificationFlag, I.contentDate, I.contentTime, I.observationDateTime, I.conceptNameCodeSequence, CS.codeValue, CS.codingSchemeDesignator, CS.codingSchemeVersion, CS.codeMeaning';
          l_commonJoin:=l_commonJoin||' FROM StructReps I INNER JOIN CodeSequences CS ON I.conceptNameCodeSequence=CS.pk';

          -- WHERE fields:
          IF(p_completionFlag IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.completionFlag LIKE UPPER('''||p_completionFlag||''')';
          END IF;
          IF(p_verificationFlag IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.verificationFlag LIKE UPPER('''||p_verificationFlag||''')';
          END IF;
          IF(p_contentDate IS NOT NULL)THEN
            IF(p_contentDateLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.contentDate = '''||p_contentDate||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.contentDate >= '''||p_contentDate||''' AND I.contentDate <= '''||p_contentDateLATE||''')';
            END IF;
          END IF;
          IF(p_contentTime IS NOT NULL)THEN
            IF(p_contentTimeLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.contentTime = '''||p_contentTime||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.contentTime >= '''||p_contentTime||''' AND I.contentTime <= '''||p_contentTimeLATE||''')';
            END IF;
          END IF;
          IF(p_observationDateTime IS NOT NULL)THEN
            IF(p_observationDateTimeLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.observationDateTime = '''||p_observationDateTime||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.observationDateTime >= '''||p_observationDateTime||''' AND I.observationDateTime <= '''||p_observationDateTimeLATE||''')';
            END IF;
          END IF;

        END;
      WHEN 4 THEN   -- Overlays
        BEGIN

          IF(p_samplesPerPixel IS NOT NULL OR p_rowsNum IS NOT NULL OR p_columnsNum IS NOT NULL OR p_bitsAllocated IS NOT NULL OR p_bitsStored IS NOT NULL OR p_highBit IS NOT NULL OR p_pixelRepresentation IS NOT NULL OR p_numberOfFrames IS NOT NULL OR
             p_presentationLabel IS NOT NULL OR p_presentationDescription IS NOT NULL OR p_presentationCreationDate IS NOT NULL OR p_presentationCreationDateLATE IS NOT NULL OR p_presentationCreationTime IS NOT NULL OR p_presentationCreationTimeLATE IS NOT NULL OR p_presentationCreatorsName IS NOT NULL OR p_recommendedViewingMode IS NOT NULL OR
             p_completionFlag IS NOT NULL OR p_verificationFlag IS NOT NULL OR p_contentDate IS NOT NULL OR p_contentDateLATE IS NOT NULL OR p_contentTime IS NOT NULL OR p_contentTimeLATE IS NOT NULL OR p_observationDateTime IS NOT NULL OR p_observationDateTimeLATE IS NOT NULL) THEN
             RETURN;
          END IF;

          l_query:=l_query|| ', I.overlayNumber, I.overlayRows, I.overlayColumns, I.overlayBitsAllocated, I.overlayType';
          l_commonJoin:=l_commonJoin||' FROM Overlays I ';

          -- WHERE fields:
          IF(p_overlayNumber IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.overlayNumber = '||p_overlayNumber;
          END IF;
          IF(p_overlayRows IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.overlayRows = '||p_overlayRows;
          END IF;
          IF(p_overlayColumns IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.overlayColumns = '||p_overlayColumns;
          END IF;
          IF(p_overlayType IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.overlayType = UPPER('''||p_overlayType||''')';
          END IF;
          IF(p_overlayBitsAllocated IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.overlayBitsAllocated = '||p_overlayBitsAllocated;
          END IF;

        END;
      WHEN 5 THEN   -- NonImages
        BEGIN

          IF(p_samplesPerPixel IS NOT NULL OR p_rowsNum IS NOT NULL OR p_columnsNum IS NOT NULL OR p_bitsAllocated IS NOT NULL OR p_bitsStored IS NOT NULL OR p_highBit IS NOT NULL OR p_pixelRepresentation IS NOT NULL OR p_numberOfFrames IS NOT NULL OR
               p_presentationLabel IS NOT NULL OR p_presentationDescription IS NOT NULL OR p_presentationCreationDate IS NOT NULL OR p_presentationCreationDateLATE IS NOT NULL OR p_presentationCreationTime IS NOT NULL OR p_presentationCreationTimeLATE IS NOT NULL OR p_presentationCreatorsName IS NOT NULL OR p_recommendedViewingMode IS NOT NULL OR
               p_completionFlag IS NOT NULL OR p_verificationFlag IS NOT NULL OR p_contentDate IS NOT NULL OR p_contentDateLATE IS NOT NULL OR p_contentTime IS NOT NULL OR p_contentTimeLATE IS NOT NULL OR p_observationDateTime IS NOT NULL OR p_observationDateTimeLATE IS NOT NULL OR
               p_overlayNumber IS NOT NULL OR p_overlayRows IS NOT NULL OR p_overlayColumns IS NOT NULL OR p_overlayType IS NOT NULL OR p_overlayBitsAllocated IS NOT NULL) THEN
               RETURN;
            END IF;

          l_commonJoin:=l_commonJoin||' FROM NonImages I ';
        END;
      WHEN 6 THEN   -- KeyObjects
        BEGIN

          IF(p_samplesPerPixel IS NOT NULL OR p_rowsNum IS NOT NULL OR p_columnsNum IS NOT NULL OR p_bitsAllocated IS NOT NULL OR p_bitsStored IS NOT NULL OR p_highBit IS NOT NULL OR p_pixelRepresentation IS NOT NULL OR p_numberOfFrames IS NOT NULL OR
             p_presentationLabel IS NOT NULL OR p_presentationDescription IS NOT NULL OR p_presentationCreationDate IS NOT NULL OR p_presentationCreationDateLATE IS NOT NULL OR p_presentationCreationTime IS NOT NULL OR p_presentationCreationTimeLATE IS NOT NULL OR p_presentationCreatorsName IS NOT NULL OR p_recommendedViewingMode IS NOT NULL OR
             p_overlayNumber IS NOT NULL OR p_overlayRows IS NOT NULL OR p_overlayColumns IS NOT NULL OR p_overlayType IS NOT NULL OR p_overlayBitsAllocated IS NOT NULL) THEN
             RETURN;
          END IF;

          l_query:=l_query|| ', I.contentDate, I.contentTime, I.codeSequencesFK, CS.codeValue, CS.codingSchemeDesignator, CS.codingSchemeVersion, CS.codeMeaning';
          l_commonJoin:=l_commonJoin||' FROM StructReps I INNER JOIN CodeSequences CS ON I.codeSequencesFK=CS.pk';

          -- WHERE fields:
          IF(p_contentDate IS NOT NULL)THEN
            IF(p_contentDateLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.contentDate = '''||p_contentDate||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.contentDate >= '''||p_contentDate||''' AND I.contentDate <= '''||p_contentDateLATE||''')';
            END IF;
          END IF;
          IF(p_contentTime IS NOT NULL)THEN
            IF(p_contentTimeLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.contentTime = '''||p_contentTime||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.contentTime >= '''||p_contentTime||''' AND I.contentTime <= '''||p_contentTimeLATE||''')';
            END IF;
          END IF;

        END;
    END CASE;

    l_query:=l_query||l_commonJoin||l_commonWhere||' ORDER BY I.instanceNumber ASC, I.sopInstanceUID ASC';
    -- dbms_output.put_line(l_query); 	-- REMEMBER TO USE 			SET SERVEROUTPUT ON;
    OPEN resultSet FOR l_query;

END queryInstanceLevel;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE recoverSeries(
  p_pk NUMBER,
  p_currentUid VARCHAR2,
  p_userFk NUMBER,
  p_seriesSize NUMBER,
  r_result OUT NUMBER,
  r_seriesInstanceUID OUT VARCHAR2
) AS
  l_numSeriesInstances NUMBER(19);
  l_counter NUMBER(11);
  l_studyInstanceUID VARCHAR2(64 CHAR);

  l_studyStatus CHAR(1);

  cp_currentUid   VARCHAR2(65 CHAR);
  cp_originalUid  VARCHAR2(64 CHAR);
  CURSOR c_images IS
    SELECT currentUid||'%', originalUid from DeprecationEvents where parentDeprecationFK=p_pk AND deprecatedObjType = 'IM';
  CURSOR c_nonImages IS
    SELECT currentUid||'%', originalUid from DeprecationEvents where parentDeprecationFK=p_pk AND deprecatedObjType = 'NI';
  CURSOR c_overlays IS
    SELECT currentUid||'%', originalUid from DeprecationEvents where parentDeprecationFK=p_pk AND deprecatedObjType = 'OV';
  CURSOR c_presStates IS
    SELECT currentUid||'%', originalUid from DeprecationEvents where parentDeprecationFK=p_pk AND deprecatedObjType = 'PS';
  CURSOR c_structReps IS
    SELECT currentUid||'%', originalUid from DeprecationEvents where parentDeprecationFK=p_pk AND deprecatedObjType = 'SR';
  CURSOR c_keyObjects IS
    SELECT currentUid||'%', originalUid from DeprecationEvents where parentDeprecationFK=p_pk AND deprecatedObjType = 'KO';

BEGIN

-- CHECKS -----------------------------------------
  SELECT COUNT(pk) INTO l_counter FROM DeprecationEvents WHERE pk=p_pk AND currentUid=p_currentUid AND deprecatedObjType='SE';
  IF(l_counter<>1)THEN
    SELECT -1 INTO r_result FROM Dual;  -- -1=no matching results
    RETURN;
  END IF;

  SELECT originalUid INTO r_seriesInstanceUID FROM DeprecationEvents WHERE pk=p_pk;

  SELECT COUNT(seriesInstanceUID) INTO l_counter FROM Series WHERE seriesInstanceUID=r_seriesInstanceUID;
  IF(l_counter>0) THEN
    SELECT -2 INTO r_result FROM Dual;  -- -2=series present
    RETURN;
  END IF;

  SELECT COUNT(St.studyInstanceUID) INTO l_counter
  FROM Series Se
  INNER JOIN Studies St ON St.studyInstanceUID=Se.studyFK
  WHERE Se.seriesInstanceUID=p_currentUid AND St.deprecated=0;

  IF(l_counter<>1) THEN
    SELECT -3 INTO r_result FROM Dual;  -- -3=study deprecated
    RETURN;
  END IF;

  SELECT COUNT(ht.sopInstanceUID) INTO l_counter
  FROM DeprecationEvents de
  INNER JOIN HashTable ht ON ht.sopInstanceUID=de.originalUid
  WHERE de.parentDeprecationFK=p_pk;
  IF(l_counter>0) THEN
    SELECT -4 INTO r_result FROM Dual;  -- -4=some instances already present
    RETURN;
  END IF;

  -- RECOVERY
  SELECT St.studyStatus INTO l_studyStatus
  FROM Series Se
  INNER JOIN Studies St ON St.studyInstanceUID=Se.studyFK
  WHERE Se.seriesInstanceUID=p_currentUid;

  INSERT INTO HashTable(sopInstanceUID,hash)
    SELECT originalUID, reason FROM DeprecationEvents
    WHERE parentDeprecationFK=p_pk;

  OPEN c_images;
     LOOP
       FETCH c_images INTO cp_currentUid, cp_originalUid;
       CASE l_studyStatus
        WHEN 'p' THEN UPDATE ImagesOffline SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid;
        WHEN 'n' THEN UPDATE ImagesNearline SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid;
        ELSE UPDATE Images SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid;
      END CASE;
       EXIT WHEN c_images%NOTFOUND;
     END LOOP;
     CLOSE c_images;

     OPEN c_nonImages;
     LOOP
       FETCH c_nonImages INTO cp_currentUid, cp_originalUid;
       UPDATE NonImages SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid;
       EXIT WHEN c_nonImages%NOTFOUND;
     END LOOP;
     CLOSE c_nonImages;

     OPEN c_overlays;
     LOOP
       FETCH c_overlays INTO cp_currentUid, cp_originalUid;
       UPDATE Overlays SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid;
       EXIT WHEN c_overlays%NOTFOUND;
     END LOOP;
     CLOSE c_overlays;

     OPEN c_presStates;
     LOOP
       FETCH c_presStates INTO cp_currentUid, cp_originalUid;
       UPDATE PresStates SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid;
       EXIT WHEN c_presStates%NOTFOUND;
     END LOOP;
     CLOSE c_presStates;

     OPEN c_structReps;
     LOOP
       FETCH c_structReps INTO cp_currentUid, cp_originalUid;
       UPDATE StructReps SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid;
       EXIT WHEN c_structReps%NOTFOUND;
     END LOOP;
     CLOSE c_structReps;
     
     OPEN c_keyObjects;
     LOOP
       FETCH c_keyObjects INTO cp_currentUid, cp_originalUid;
       UPDATE KeyObjects SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid;
       EXIT WHEN c_keyObjects%NOTFOUND;
     END LOOP;
     CLOSE c_keyObjects;


  UPDATE Series SET deprecated=0, seriesInstanceUID=r_seriesInstanceUID WHERE seriesInstanceUID=p_currentUid;

  SELECT numberOfSeriesRelatedInstances, studyFK INTO l_numSeriesInstances, l_studyInstanceUID FROM Series WHERE seriesInstanceUID=r_seriesInstanceUID;

  UPDATE Studies SET studySize=studySize+p_seriesSize, numberOfStudyRelatedSeries=numberOfStudyRelatedSeries+1, numberOfStudyRelatedInstances=numberOfStudyRelatedInstances+l_numSeriesInstances
  WHERE studyInstanceUID=l_studyInstanceUID;

  DELETE FROM DeprecationEvents WHERE parentDeprecationFK=p_pk;

  UPDATE DeprecationEvents SET recoveredBy=p_userFk, recoveredOn=SYS_EXTRACT_UTC(current_timestamp) WHERE pk=p_pk;

  SELECT p_pk INTO r_result FROM Dual;

END recoverSeries;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE recoverStudy(
  p_pk NUMBER,
  p_currentUid VARCHAR2,
  p_userFk NUMBER,
  r_result OUT NUMBER,
  r_studyInstanceUID OUT VARCHAR2
) AS
  l_counter NUMBER(11);
  l_patientFk NUMBER(19);
  l_studyStatus CHAR(1);

  cp_currentUid   VARCHAR2(65 CHAR);
  cp_originalUid  VARCHAR2(64 CHAR);
  CURSOR c_images IS
    SELECT currentUid||'\_'||'%', originalUid from DeprecationEvents where parentDeprecationFK=p_pk AND deprecatedObjType = 'IM';
  CURSOR c_nonImages IS
    SELECT currentUid||'\_'||'%', originalUid from DeprecationEvents where parentDeprecationFK=p_pk AND deprecatedObjType = 'NI';
  CURSOR c_overlays IS
    SELECT currentUid||'\_'||'%', originalUid from DeprecationEvents where parentDeprecationFK=p_pk AND deprecatedObjType = 'OV';
  CURSOR c_presStates IS
    SELECT currentUid||'\_'||'%', originalUid from DeprecationEvents where parentDeprecationFK=p_pk AND deprecatedObjType = 'PS';
  CURSOR c_structReps IS
    SELECT currentUid||'\_'||'%', originalUid from DeprecationEvents where parentDeprecationFK=p_pk AND deprecatedObjType = 'SR';
  CURSOR c_keyObjects IS
    SELECT currentUid||'\_'||'%', originalUid from DeprecationEvents where parentDeprecationFK=p_pk AND deprecatedObjType = 'KO';

BEGIN

  -- CHECKS -----------------------------------------
    SELECT COUNT(pk) INTO l_counter FROM DeprecationEvents WHERE pk=p_pk AND currentUid=p_currentUid AND deprecatedObjType='ST';
  IF(l_counter<>1)THEN
    SELECT -1 INTO r_result FROM Dual;  -- -1=no matching results
    RETURN;
  END IF;

  SELECT originalUid INTO r_studyInstanceUID FROM DeprecationEvents WHERE pk=p_pk;

  SELECT COUNT(studyInstanceUID) INTO l_counter FROM Studies WHERE studyInstanceUID=r_studyInstanceUID;
  IF(l_counter>0) THEN
    SELECT -5 INTO r_result FROM Dual;  -- -5=study present
    RETURN;
  END IF;

  SELECT COUNT(Se.seriesInstanceUID) INTO l_counter
  FROM DeprecationEvents de
  INNER JOIN Series Se ON se.seriesInstanceUID=de.originalUid
  WHERE de.parentDeprecationFK=p_pk AND de.deprecatedObjType = 'SE';
  IF(l_counter>0) THEN
    SELECT -6 INTO r_result FROM Dual;  -- -6=some series already present
    RETURN;
  END IF;

  SELECT COUNT(ht.sopInstanceUID) INTO l_counter
  FROM DeprecationEvents de
  INNER JOIN HashTable ht ON ht.sopInstanceUID=de.originalUid
  WHERE de.parentDeprecationFK=p_pk AND de.deprecatedObjType <> 'SE';
  IF(l_counter>0) THEN
    SELECT -4 INTO r_result FROM Dual;  -- -4=some instances already present
    RETURN;
  END IF;


  -- RECOVERY
  SELECT studyStatus INTO l_studyStatus
  FROM Studies
  WHERE studyInstanceUID=p_currentUid;

  INSERT INTO HashTable(sopInstanceUID,hash)
    SELECT originalUid, reason FROM DeprecationEvents
    WHERE parentDeprecationFK=p_pk AND deprecatedObjType <> 'SE';

     OPEN c_images;
     LOOP
       FETCH c_images INTO cp_currentUid, cp_originalUid;
       CASE l_studyStatus
        WHEN 'p' THEN UPDATE ImagesOffline SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid ESCAPE '\';
        WHEN 'n' THEN UPDATE ImagesNearline SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid ESCAPE '\';
        ELSE UPDATE Images SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid ESCAPE '\';
       END CASE;
       EXIT WHEN c_images%NOTFOUND;
     END LOOP;
     CLOSE c_images;

     OPEN c_nonImages;
     LOOP
       FETCH c_nonImages INTO cp_currentUid, cp_originalUid;
       UPDATE NonImages SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid ESCAPE '\';
       EXIT WHEN c_nonImages%NOTFOUND;
     END LOOP;
     CLOSE c_nonImages;

     OPEN c_overlays;
     LOOP
       FETCH c_overlays INTO cp_currentUid, cp_originalUid;
       UPDATE Overlays SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid ESCAPE '\';
       EXIT WHEN c_overlays%NOTFOUND;
     END LOOP;
     CLOSE c_overlays;

     OPEN c_presStates;
     LOOP
       FETCH c_presStates INTO cp_currentUid, cp_originalUid;
       UPDATE PresStates SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid ESCAPE '\';
       EXIT WHEN c_presStates%NOTFOUND;
     END LOOP;
     CLOSE c_presStates;

     OPEN c_structReps;
     LOOP
       FETCH c_structReps INTO cp_currentUid, cp_originalUid;
       UPDATE StructReps SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid ESCAPE '\';
       EXIT WHEN c_structReps%NOTFOUND;
     END LOOP;
     CLOSE c_structReps;
    
     OPEN c_keyObjects;
     LOOP
       FETCH c_keyObjects INTO cp_currentUid, cp_originalUid;
       UPDATE KeyObjects SET deprecated=0,sopInstanceUID = cp_originalUid WHERE sopInstanceUID LIKE cp_currentUid ESCAPE '\';
       EXIT WHEN c_keyObjects%NOTFOUND;
     END LOOP;
     CLOSE c_keyObjects;

  UPDATE Series SET (deprecated,seriesInstanceUID) = (
    SELECT 0, originalUid
    FROM DeprecationEvents
    WHERE currentUid= seriesInstanceuid AND parentDeprecationFK=p_pk AND deprecatedObjType = 'SE'
  ) WHERE EXISTS( SELECT currentUid from DeprecationEvents where currentUid = seriesInstanceUID AND parentDeprecationFK=p_pk AND deprecatedObjType = 'SE');


  UPDATE Studies SET deprecated=0, studyInstanceUID=r_studyInstanceUID WHERE studyInstanceUID=p_currentUid;

  SELECT patientFK INTO l_patientFk FROM Studies WHERE studyInstanceUID=r_studyInstanceUID;

  UPDATE PatientDemographics SET numberOfPatientRelatedStudies=numberOfPatientRelatedStudies+1 WHERE patientFK=l_patientFk;

  DELETE FROM DeprecationEvents WHERE parentDeprecationFK=p_pk;

  UPDATE DeprecationEvents SET recoveredBy=p_userFk, recoveredOn=SYS_EXTRACT_UTC(current_timestamp) WHERE pk=p_pk;

  SELECT p_pk INTO r_result FROM Dual;

END recoverStudy;

------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE storeInstanceLevelMetadata(
  p_studyInstanceUid VARCHAR2,
  p_seriesInstanceUid VARCHAR2,
  p_sopInstanceUid VARCHAR2,
  p_sopClassUid VARCHAR2,
  p_instanceNumber NUMBER,
  r_outcome OUT NUMBER
)
AS
  l_table VARCHAR2(10 CHAR);
BEGIN

  r_outcome:=0;

  BEGIN
    Select tableNameIfInstance INTO l_table from SupportedSOPClasses WHERE sopClassUID=p_sopClassUid;
  EXCEPTION WHEN NO_DATA_FOUND THEN
    r_outcome:=-1;
    return;
  end;

  CASE
    WHEN l_table='Images' THEN
      INSERT INTO Images(sopInstanceUID, sopClassUID, instanceNumber, seriesFK) VALUES(p_sopInstanceUid, p_sopClassUid, p_instanceNumber, p_seriesInstanceUid);
    WHEN l_table='StructReps' THEN
      INSERT INTO StructReps(sopInstanceUID, sopClassUID, instanceNumber, seriesFK) VALUES(p_sopInstanceUid, p_sopClassUid, p_instanceNumber, p_seriesInstanceUid);
    WHEN l_table='PresStates' THEN
      INSERT INTO PresStates(sopInstanceUID, sopClassUID, instanceNumber, seriesFK) VALUES(p_sopInstanceUid, p_sopClassUid, p_instanceNumber, p_seriesInstanceUid);
    WHEN l_table='Overlays' THEN
      INSERT INTO Overlays(sopInstanceUID, sopClassUID, instanceNumber, seriesFK) VALUES(p_sopInstanceUid, p_sopClassUid, p_instanceNumber, p_seriesInstanceUid);
    WHEN l_table='KeyObjects' THEN
      INSERT INTO KeyObjects(sopInstanceUID, sopClassUID, instanceNumber, seriesFK) VALUES(p_sopInstanceUid, p_sopClassUid, p_instanceNumber, p_seriesInstanceUid);
    ELSE
      INSERT INTO NonImages(sopInstanceUID, sopClassUID, instanceNumber, seriesFK) VALUES(p_sopInstanceUid, p_sopClassUid, p_instanceNumber, p_seriesInstanceUid);
  END CASE;

  INSERT INTO HashTable(sopInstanceUID,hash) VALUES(p_sopInstanceUid,'METADATAONLY');

  UPDATE Studies SET numberOfStudyRelatedInstances=numberOfStudyRelatedInstances+1 WHERE studyInstanceUID=p_studyInstanceUid;
  UPDATE Series SET numberOfSeriesRelatedInstances=numberOfSeriesRelatedInstances+1 WHERE seriesInstanceUID=p_seriesInstanceUid;
  r_outcome:=1;

END storeInstanceLevelMetadata;

------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
