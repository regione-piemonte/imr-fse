
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
