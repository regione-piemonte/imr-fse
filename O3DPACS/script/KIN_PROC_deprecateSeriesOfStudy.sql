
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
