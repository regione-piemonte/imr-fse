CREATE OR REPLACE PROCEDURE JBOSSPACSDB."ADDNEWPATIENT" (
  p_lastName VARCHAR2,
  p_firstName VARCHAR2,
  p_middleName VARCHAR2,
  p_prefix VARCHAR2,
  p_suffix VARCHAR2,
  p_birthDate DATE,
  p_birthTime DATE,
  p_sex CHAR,
  p_patientId  VARCHAR2,
  p_idIssuer VARCHAR2,

  p_ethnicGroup VARCHAR2,
  p_patientComments VARCHAR2,
  p_race VARCHAR2,
  p_patientAddress VARCHAR2,
  p_patientAccountNumber VARCHAR2,
  p_patientIdentifierList VARCHAR2,
  p_patientCity VARCHAR2,
  p_patPk OUT NUMBER
)
AS
BEGIN
setlog( 'INFO', 'addNewPatient', 'p_lastName = '||p_lastName||' p_firstName = '||p_firstName||' p_birthDate = '||p_birthDate, NULL, NULL);

  INSERT INTO Patients(lastName, firstName, middleName, prefix, suffix, birthDate, birthTime, sex, patientID, idIssuer)
  VALUES(p_lastName, p_firstName, p_middleName, p_prefix, p_suffix, p_birthDate, p_birthTime, p_sex, p_patientID, p_idIssuer);

  SELECT PATIENTS_PK_SEQ.CURRVAL INTO p_patPk FROM DUAL;

  INSERT INTO PatientDemographics(ethnicGroup, patientComments, race, patientAddress, patientAccountNumber, patientIdentifierList, patientFK, patientCity)
  VALUES(p_ethnicGroup, p_patientComments, p_race, p_patientAddress, p_patientAccountNumber, p_patientIdentifierList, p_patPk, p_patientCity);

END addNewPatient;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."ADDNEWSTUDY" (
  p_studyInstanceUID VARCHAR2,
  p_accessionNumber VARCHAR2,
  p_patientFK NUMBER,
  p_studyID VARCHAR2,
  p_studyDescription VARCHAR2,
  p_procedureCodeSequenceFK NUMBER,
  p_referringPhysiciansName VARCHAR2,
  p_toReconcile NUMBER
)
AS
BEGIN
setlog( 'INFO', 'addNewStudy', 'p_studyInstanceUID = '||p_studyInstanceUID||' p_accessionNumber = '||p_accessionNumber||' p_patientFK = '||p_patientFK, NULL, NULL);

  -- Add Study
  INSERT INTO Studies(studyInstanceUID, accessionNumber, patientFK, studyID, studyDescription, procedureCodeSequenceFK, referringPhysiciansName, toReconcile)
  VALUES(p_studyInstanceUID, p_accessionNumber, p_patientFK, p_studyID, p_studyDescription, p_procedureCodeSequenceFK, p_referringPhysiciansName, p_toReconcile);

  -- UPDATE PatientDemopgraphics
  UPDATE PatientDemographics SET numberOfPatientRelatedStudies=numberOfPatientRelatedStudies+1 WHERE patientFK=p_patientFK;

END addNewStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."ADDPATIENTVISIT" (
  p_studyInstanceUid VARCHAR2,
  p_patientFK NUMBER,
  p_visitNumber VARCHAR2,
  p_patientClass CHAR,
  p_assignedPatientLocation VARCHAR2,
  r_pk OUT NUMBER
)
AS
BEGIN

  BEGIN

    SELECT pk INTO r_pk FROM WLPatientDataPerVisit WHERE patientFK=p_patientFK AND studyFK=p_studyInstanceUid;
    UPDATE WLPatientDataPerVisit SET visitNumber=p_visitNumber, patientClass=p_patientClass, assignedPatientLocation=p_assignedPatientLocation WHERE pk=r_pk;

  EXCEPTION WHEN NO_DATA_FOUND THEN

    INSERT INTO WLPatientDataPerVisit(patientClass, assignedPatientLocation, visitNumber, patientFK, studyFK)
      VALUES(p_patientClass, p_assignedPatientLocation, p_visitNumber, p_patientFK, p_studyInstanceUid);
    SELECT WLPATIENTDATAPERVISIT_PK_SEQ.CURRVAL INTO r_pk FROM DUAL;

  END;


END addPatientVisit;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."ADDSPECIFICCHARSET" (
  p_studyInstanceUid VARCHAR2,
  p_charset VARCHAR2
)
AS
BEGIN

  UPDATE Studies SET specificCharSet=p_charset WHERE studyInstanceUID=p_studyInstanceUid;

END addSpecificCharSet;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."ADDSTUDYTRACKING" (
  p_studyInstanceUid VARCHAR2,
  p_insertedBy CHAR,
  p_updatedRows OUT NUMBER
)
AS
  l_curProc CHAR(1);
BEGIN

  BEGIN
    SELECT insertedByProcess INTO l_curProc
    FROM StudyAvailability
    WHERE studyInstanceUid = p_studyInstanceUid;
  EXCEPTION WHEN NO_DATA_FOUND THEN
    l_curProc:=NULL;
  END;

  IF(l_curProc IS NULL) THEN

    INSERT INTO StudyAvailability(studyInstanceUid,insertedByProcess,insertedOn)
    VALUES (p_studyInstanceUid, p_insertedBy, systimestamp);

  ELSIF(l_curProc = 'S' AND p_insertedBy<>'S') THEN

    UPDATE StudyAvailability
    SET insertedByProcess=p_insertedBy, insertedOn=systimestamp
    WHERE studyInstanceUid = p_studyInstanceUid;

  END IF;

  p_updatedRows := SQL%ROWCOUNT;

END addStudyTracking;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."ARCHIVESTUDY" (
  p_studyUid VARCHAR2,
  p_urlToStudy OUT VARCHAR2,
  p_physicalMediaId OUT NUMBER
)
AS
BEGIN
setlog( 'INFO', 'archiveStudy', 'p_studyUid = '||p_studyUid, NULL, NULL);

  SELECT ST.fastestAccess, PM.pk INTO p_urlToStudy, p_physicalMediaId
  FROM Studies ST
  INNER JOIN PhysicalMedia PM ON ST.fastestAccess LIKE (PM.urlToStudy||'%')
  WHERE ST.studyInstanceUID=p_studyUid;

  UPDATE Studies
  SET studyStatus='a',fastestAccess=NULL,studyDescription=SUBSTR(CONCAT('FWDD - ', studyDescription),1,64)
  WHERE studyInstanceUID=p_studyUid;

END archiveStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."BACKUPOBJECT" (
  l_obj VARCHAR2,
  l_objType VARCHAR2
)
AS
  l_object VARCHAR2(64 CHAR);
  l_objectType VARCHAR2(16 CHAR);
BEGIN

  l_object:=UPPER(l_obj);
  l_objectType:=UPPER(l_objType);

  BEGIN
    CASE
      WHEN l_objectType='PROCEDURE' THEN
        BEGIN
          INSERT INTO UNDO(key, type, code) VALUES(l_object, l_objectType, 'CREATE OR REPLACE ');
          FOR c IN (SELECT name, type, Text FROM USER_Source WHERE TYPE=l_objectType and NAME = l_object ORDER BY LINE ASC)
          LOOP
            UPDATE UNDO SET code=code||c.Text WHERE UPPER(key)=UPPER(c.name);
          END LOOP;
        END;
      WHEN l_objectType='SERVICE' THEN
          INSERT INTO UNDO(key, type, code)
          SELECT l_obj, l_objectType, configuration FROM ServicesConfiguration WHERE serviceName=l_obj;
      WHEN l_objectType='GLOBALCONF' THEN
          INSERT INTO UNDO(key, type, code)
          SELECT l_obj, l_objectType, paramValue FROM GlobalConfiguration WHERE paramKey=l_obj;
      ELSE
        dbms_output.put_line('>>>>>>>>>>>>UNKNOWN TYPE: '||l_objectType);
    END CASE;
  EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
      dbms_output.put_line('Unique Constraint Violated: '||l_objectType||' '||l_object);
  END;

END backupObject;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."CANCELDISCONTINUESTUDY" (
    p_studyUid IN VARCHAR2,
    p_patientId VARCHAR2
)AS
  l_patPk NUMBER(19);
  l_count NUMBER(10);
BEGIN
  BEGIN
    SELECT pk INTO l_patPk FROM Patients WHERE patientID=p_patientId;
  EXCEPTION WHEN NO_DATA_FOUND THEN
    l_patPk:=0;
    l_count:=0;
  END;

  IF(l_patPk>0)THEN
    UPDATE Studies SET deprecated=1 WHERE studyInstanceUID=p_studyUid AND patientFK=l_patPk and deprecated=0;
    l_count:=SQL%ROWCOUNT;
  END IF;

  IF(l_count>0)THEN
    UPDATE PatientDemographics SET numberOfPatientRelatedStudies=numberOfPatientRelatedStudies-1 WHERE PatientDemographics.patientFK = l_patPk;
  END IF;

END cancelDiscontinueStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."CLEARINSTANCESINPROGRESS" (
  r_outcome OUT NUMBER
)
AS

BEGIN

  DELETE FROM InstancesInProgress;

  r_outcome:=SQL%ROWCOUNT;


END clearInstancesInProgress;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB.CLEARLOG(p_days number DEFAULT 20) 
as
BEGIN
    setlog('INFO', 'CLEARLOG', 'pulizia log', NULL, NULL);

    DELETE logs
    WHERE
        insdate < SYSDATE - p_days;
    COMMIT;

    DELETE logs
    WHERE
        insdate < SYSDATE - p_days and class in ('getStudiesToMark','getStudiesToDelete');
    COMMIT;  

    DELETE scheduleprocesses
    WHERE
        startedonutc < SYSDATE - p_days;
    COMMIT;

END clearlog;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."COMPLETEFORWARDPROCESS" (
  p_processId NUMBER
)
AS
BEGIN

  UPDATE ScheduleProcesses SET finishedOnUtc=SYS_EXTRACT_UTC(current_timestamp) WHERE pk=p_processId;

END completeForwardProcess;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."COMPLETEMFCREATION" (
  p_studyUid VARCHAR2,
  p_newSeriesUid VARCHAR2,
  p_newInstanceUid VARCHAR2,
  p_newInstanceHash VARCHAR2,
  p_newInstanceSopClass VARCHAR2,
  p_instanceSizeInBytes NUMBER,
  p_pacsAeTitle VARCHAR2,
  p_callingAeTitle VARCHAR2,
  p_modality VARCHAR2,
  p_done OUT NUMBER
)
AS
  l_physMediaId NUMBER(19);
BEGIN

  p_done:=0;
  SELECT preferredStorageFK INTO l_physMediaId FROM KnownNodes WHERE aeTitle=p_callingAeTitle;

  INSERT INTO Series(seriesInstanceUID, seriesNumber, modality, studyFK, numberOfSeriesRelatedInstances, equipmentFK)
    SELECT p_newSeriesUid, 1, p_modality, p_studyUid, 1, KN.equipmentFK
    FROM KnownNodes KN
    WHERE KN.aeTitle=p_pacsAeTitle;

  INSERT INTO Images(sopInstanceUID, sopClassUID, instanceNumber, seriesFK)
  VALUES (p_newInstanceUid, p_newInstanceSopClass, 1, p_newSeriesUid);

  INSERT INTO HashTable(sopInstanceUID,hash)
  VALUES(p_newInstanceUid, p_newInstanceHash);

  UPDATE Studies
  SET studySize=studySize+p_instanceSizeInBytes,
      numberOfStudyRelatedSeries=numberOfStudyRelatedSeries+1,
      numberOfStudyRelatedInstances=numberOfStudyRelatedInstances+1
  WHERE studyInstanceUID=p_studyUid;

  UPDATE PhysicalMedia SET filledBytes=filledBytes+p_instanceSizeInBytes WHERE pk=l_physMediaId;

  p_done:=1;

  COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;

END completeMfCreation;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."COMPLETEOLDSTUDIES" (
  p_insertedBy CHAR,
  p_minutes NUMBER,
  p_seconds OUT NUMBER
)
AS
  l_updatedRows NUMBER(19);
BEGIN

  SELECT UNIX_TIMESTAMP() INTO p_seconds FROM DUAL;

  UPDATE StudyAvailability
  SET completed=1, completedOnSeconds=p_seconds
  WHERE insertedByProcess=p_insertedBy AND completed=0 AND insertedOn <= systimestamp - NUMTODSINTERVAL(p_minutes, 'MINUTE');

  l_updatedRows := SQL%ROWCOUNT;

  IF(l_updatedRows=0)THEN
    p_seconds:=0;
  END IF;

END completeOldStudies;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."COMPLETESTUDY" (
  p_studyInstanceUid VARCHAR2,
  p_insertedBy CHAR,
  p_updatedRows OUT NUMBER
)
AS
BEGIN
setlog( 'INFO', 'completeStudy', 'p_studyInstanceUid = '||p_studyInstanceUid, NULL, NULL);

  UPDATE StudyAvailability
  SET completed=1
  WHERE (insertedByProcess=p_insertedBy OR insertedByProcess='S') AND completed=0 AND studyInstanceUid=p_studyInstanceUid;

  p_updatedRows := SQL%ROWCOUNT;

END completeStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."DELETEINSTANCES" (
  p_studyUid VARCHAR2
)
AS
BEGIN
setlog( 'INFO', 'deleteInstances', 'p_studyUid = '||p_studyUid, NULL, NULL);

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
   );

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

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."DELETESTUDY" (
  p_studyUid VARCHAR2,
  r_studySize OUT NUMBER,
  r_physicalMediaPk OUT NUMBER,
  r_originalUid OUT VARCHAR2
)
AS
  l_pk NUMBER(19);
BEGIN
setlog( 'INFO', 'deleteStudy', 'p_studyUid = '||p_studyUid, NULL, NULL);

  SELECT pk, originalUid INTO l_pk, r_originalUid FROM DeprecationEvents WHERE currentUID=p_studyUid AND eventType='DEL';
  Select studySize INTO r_studySize FROM Studies where studyInstanceUID=p_studyUid;

  DELETE FROM PresStatesToImages WHERE presStateFK IN (SELECT I.sopInstanceUID FROM PresStates I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='PS');
  DELETE FROM OverlaysToImages WHERE overlayFK IN (SELECT I.sopInstanceUID FROM Overlays I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='OV');
  DELETE FROM ImageNumberOfFrames WHERE sopInstanceUid IN (SELECT I.sopInstanceUID FROM Images I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='IM');
  DELETE FROM ImageNumberOfFrames WHERE sopInstanceUid IN (SELECT I.sopInstanceUID FROM ImagesNearline I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='IM');
  DELETE FROM ImageNumberOfFrames WHERE sopInstanceUid IN (SELECT I.sopInstanceUID FROM ImagesOffline I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='IM');

  DELETE FROM Images WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM Images I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='IM');
  DELETE FROM ImagesNearline WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM ImagesNearline I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='IM');
  DELETE FROM ImagesOffline WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM ImagesOffline I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='IM');
  DELETE FROM NonImages WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM NonImages I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='NI');
  DELETE FROM StructReps WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM StructReps I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='SR');
  DELETE FROM Overlays WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM Overlays I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='OV');
  DELETE FROM PresStates WHERE sopInstanceUID IN (SELECT I.sopInstanceUID FROM PresStates I INNER JOIN DeprecationEvents DE ON I.sopInstanceUID LIKE DE.currentUid||'%' WHERE parentDeprecationFK=l_pk AND DE.deprecatedObjType='PS');

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

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."DEPRECATESERIES" (
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
setlog( 'INFO', 'deprecateSeries', 'p_originalUid = '||p_originalUid||' p_newUid = '||p_newUid||p_originalUid||' p_eventType = '||p_eventType, NULL, NULL);

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

  SELECT studyFK, numberOfSeriesRelatedInstances INTO l_studyInstanceUID, l_numSeriesInstances FROM Series where seriesInstanceUID=p_newUid;

  UPDATE Studies SET studySize=studySize-p_seriesSize, numberOfStudyRelatedSeries=numberOfStudyRelatedSeries-1, numberOfStudyRelatedInstances=numberOfStudyRelatedInstances-l_numSeriesInstances
  WHERE studyInstanceUID=l_studyInstanceUID;

END deprecateSeries;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."DEPRECATESERIESOFSTUDY" (
  p_oldSeriesUid VARCHAR2,
  p_newUid VARCHAR2,
  p_parentDeprecation NUMBER
)
AS
BEGIN
setlog( 'INFO', 'deprecateSeriesOfStudy', 'p_oldSeriesUid = '||p_oldSeriesUid||' p_newUid = '||p_newUid, NULL, NULL);


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

END deprecateSeriesOfStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."DEPRECATESTUDY" (
  p_originalUid VARCHAR2,
  p_newUid VARCHAR2,
  p_eventType VARCHAR2,
  p_reason VARCHAR2,
  p_userFk NUMBER,
  r_deprecationId OUT NUMBER,
  resultset OUT Types.ResultSet
)
AS
  l_patientFK NUMBER(19);
BEGIN
setlog( 'INFO', 'deprecateStudy', 'p_originalUid = '||p_originalUid||' p_newUid = '||p_newUid||' p_eventType = '||p_eventType||' p_reason = '||p_reason||' p_userFk = '||p_userFk, NULL, NULL);
begin
  SELECT COUNT(studyInstanceUID) INTO r_deprecationId FROM Studies WHERE studyInstanceUID=p_originalUid;
  IF(r_deprecationId=0) THEN
    RETURN;
  END IF;

  UPDATE Studies SET studyInstanceUID=p_newUid, deprecated=1 WHERE studyInstanceUID=p_originalUid;

  SELECT patientFK INTO l_patientFK FROM Studies where studyInstanceUID=p_newUid;

  UPDATE PatientDemographics SET numberOfPatientRelatedStudies=numberOfPatientRelatedStudies-1 WHERE patientFK=l_patientFK;

  INSERT INTO DeprecationEvents(deprecatedObjType,eventType,currentUid,originalUid,reason,deprecatedBy,deprecatedOn)
  VALUES('ST',p_eventType,p_newUid,p_originalUid,p_reason,p_userFk,SYS_EXTRACT_UTC(current_timestamp));

  SELECT DeprecationEvents_PK_SEQ.CURRVAL INTO r_deprecationId FROM DUAL;

  UPDATE StudiesToVerify SET studyFK=p_newUid, toBeIgnored=1 WHERE studyFK=p_originalUid;

  OPEN resultset FOR
    SELECT seriesInstanceUID FROM Series WHERE studyFK=p_newUid AND deprecated = 0 ORDER BY seriesInstanceUID ASC;

  EXCEPTION
  WHEN OTHERS THEN
    setlog('ERROR','deprecateStudy',SQLERRM);
    --raise_application_error(SQLCODE, SQLERRM);
  --raise_application_error(-20001, SQLERRM);
  end;
END deprecateStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."DOCHANGEPASSWORD" (
    p_id    NUMBER,
    p_email VARCHAR2,
    p_newExpirationDate DATE,
    p_oldPassword VARCHAR2,
    p_newPassword VARCHAR2,
    p_updatedRows OUT NUMBER
)
AS
BEGIN
  UPDATE Users SET password = p_newPassword, pwdExpirationDate=p_newExpirationDate WHERE pk =p_id AND email =p_email AND password =p_oldPassword;
  p_updatedRows :=SQL%ROWCOUNT;
END doChangePassword;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETACCESSIONNUMBERTOMOVE" (
    p_toleranceInHours NUMBER,
    resultSet OUT Types.ResultSet
) AS
BEGIN

--setlog( 'INFO', 'getAccessionNumberToMove', 'p_toleranceInHours = '||p_toleranceInHours, NULL, NULL);

  OPEN resultSet FOR
  SELECT id
  FROM MoveStudyHistory a, KnownNodeToStructAsr b, GlobalConfiguration p
    WHERE a.endMov is null
      --and (a.startMov is null or a.startMov + NUMTODSINTERVAL(p_toleranceInHours, 'HOUR') < SYSTIMESTAMP)
      and b.structAsr  = a.structAsr
      and b.knownNodeFk = a.knownNodeFk
      and a.idRetry <= p.paramValue
      and to_char(sysdate,'HH24:MI:SS') >= B.startMoveTime
      and to_char(sysdate,'HH24:MI:SS') <= B.endMoveTime
      and p.paramKey = 'MoveStudyMaxRetry';

END getAccessionNumberToMove;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETALLSERIESFROMSTUDY" (
  p_studyUid IN VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN
setlog( 'INFO', 'getAllSeriesFromStudy', 'p_studyUid = '||p_studyUid, NULL, NULL);

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
          else
             NULL
          END AS sopInstanceUID
    FROM SERIES SE
    WHERE SE.STUDYFK = P_STUDYUID and SE.DEPRECATED = 0;
END getAllSeriesFromStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETALLSTUDIESFROMPATIENT" (
  p_patientId IN VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN
setlog( 'INFO', 'getAllStudiesFromPatient', 'p_patientId = '||p_patientId, NULL, NULL);

  OPEN resultset FOR
    SELECT ST.studyDate, ST.studyTime, ST.studyDescription, ST.accessionNumber, ST.studyInstanceUID
    FROM Studies ST
    INNER JOIN Patients PT ON PT.pk = ST.patientFK
    WHERE PT.patientId = p_patientId AND
    ST.deprecated = 0 ORDER BY ST.studyDate DESC, ST.studyTime ASC;
END getAllStudiesFromPatient;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETBASEURLOFSERIES" (
  p_seriesUid VARCHAR2,
  r_url OUT VARCHAR2
)
AS
BEGIN

  BEGIN

    SELECT St.fastestAccess||St.studyInstanceUID||'/' INTO r_url
    FROM Series Se
    INNER JOIN Studies St ON St.studyInstanceUID=Se.studyFK
    WHERE Se.seriesInstanceUID=p_seriesUid;

  EXCEPTION WHEN NO_DATA_FOUND THEN
    r_url:=NULL;
  END;

END getBaseUrlOfSeries;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETCOMPLETEDSTUDIES" (
 p_minute NUMBER,
 resultset OUT Types.ResultSet
)
AS
l_query VARCHAR2(4000);
l_includeOutOfWorklist CHAR(1);
BEGIN

select paramValue into l_includeOutOfWorklist from GlobalConfiguration where paramKey = 'notifyAlsoOutWorklist';

l_query:= 'select st.studyInstanceUID, sa.completed, sa.published from Studies st '||
' left join StudyAvailability sa on st.studyInstanceUID = sa.studyInstanceUid
 WHERE
  lastStatusChangeDate <= systimestamp - NUMTODSINTERVAL('||p_minute||', ''MINUTE'')
  completedOnSeconds IS NULL ';

  IF(l_includeOutOfWorklist = 0) THEN
    l_query := l_query || 'AND toReconcile = 0';
  END IF;
OPEN resultset FOR l_query;
END getCompletedStudies;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETCOUNTMOVERRORDELTAHOURS"
    --  La procedura controlla i moves iniziati entro un certo range orario e restituisce i seguenti valori:
    (
        ris_totMoves out number ,           --  numero di Moves nel periodo
        ris_totMovesInError out number,     --  numero di Moves in errore
        ris_percentuale out numeric,        --  percentuale di Moves in errore
        ris_Avviso out nvarchar2            --  avviso eventualmente da utilizzare nelle mails (Attenzione, sotto una certa soglia, Errore sopra una certa soglia)
    )
    is 

    DeltaHoursMin number :=0 ;              --  DELTA-ORARIO: n°ore dall'inizio della startMov dopo le quali prendere in considerazione la Move
    DeltaHoursMax number :=0;               --  DELTA-ORARIO: n°ore dall'inizio della startMov entro le quali prendere in considerazione la Move
    LivYellow number := 10;                 --  numero di errori oltre il quale si entra in zona gialla
    LivRed number := 20;                    --  numero di errori oltre il quale si entra in zona rossa 
    Adesso timestamp(6) := SYSTIMESTAMP;    --  costante del momento dal quale calcolare i totali

BEGIN

    --  recupero i parametri del delta-ore
    select paramValue into DeltaHoursMin from GlobalConfiguration where paramKey = 'MoveStudyMinDelayHour';     -- DA CREARE IN GLOBALCONFIGURATION
    select paramValue into DeltaHoursMax from GlobalConfiguration where paramKey = 'MoveStudyMaxDelayHour';     -- DA CREARE IN GLOBALCONFIGURATION
    --  recupero i parametri dei livelli di urgenza
    select paramValue into LivYellow from GlobalConfiguration where paramKey = 'MoveStudyLivYellow';            -- DA CREARE IN GLOBALCONFIGURATION
    select paramValue into LivRed from GlobalConfiguration where paramKey = 'MoveStudyMaxLivRed';               -- DA CREARE IN GLOBALCONFIGURATION

    select 

        a.totMoves, 
        a.totMovesInError, 

        -- calcolo la % di erorre
        cast(round((case when Totmoves = 0 then 0 else (TotMovesInError/Totmoves)*100 end),2) as Numeric(10,2)),
        --  calcolo l'avviso
        (case when totMovesInError >= LivYellow and totMovesInError < LivRed then 'ATTENZIONE' when totMovesInError >= LivRed then 'ERRORE' else ' ' end)

        into ris_totMoves, ris_totMovesInError, ris_percentuale, ris_Avviso

    from (select count (*) as Totmoves,
            --  conto i moves non completati
            count(case when ISMOVECOMPLETED(ACCESSIONNUMBER) =0 and endmov is null then 1 else 0 end) as totMovesInError
            from movestudyhistory where 
                --  nel periodo indicato
                (startMov + NUMTODSINTERVAL(DeltaHoursMin, 'HOUR')) <  systimestamp and
                (startMov + NUMTODSINTERVAL(DeltaHoursMax, 'HOUR')) > systimestamp
        ) a;
END;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETDATAFORCMOVE" (
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

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETEMAILCONFIGURATIONS" (p_key IN VARCHAR2, resultset OUT Types.ResultSet )
AS
BEGIN
  OPEN resultset FOR SELECT paramKey,
  paramValue FROM GlobalConfiguration WHERE (paramKey='EmailServer') OR (paramKey='EmailServerPort') OR (paramKey='EmailUser') OR (paramKey='EmailFormat') OR (paramKey='EmailProtocol') OR (paramKey='EmailAuthenticate')
  UNION
  SELECT paramKey, AES_DECRYPT(paramValue, p_key) FROM GlobalConfiguration WHERE (paramKey='EmailPassword') AND paramValue IS NOT NULL
  UNION
  SELECT paramKey, paramValue FROM GlobalConfiguration WHERE (paramKey='EmailPassword') and paramValue IS null;
END getEmailConfigurations;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETEMAILFROMROLEFK" (
    p_roleFk IN SMALLINT,
    resultset OUT Types.ResultSet
)
AS
BEGIN

  OPEN resultset FOR
  SELECT email
  FROM Users u
  WHERE u.roleFK = p_roleFk;

END getEmailFromRoleFk ;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETEVENTEMAIL" 
  (
    p_id       VARCHAR2,
    p_language VARCHAR2,
    resultset OUT Types.ResultSet
  )
AS
BEGIN
  OPEN resultset FOR
  SELECT subject, bodyText,targetRole FROM
  	(SELECT 1 toSort, subject, bodyText, targetRole FROM EmailMessages WHERE id =p_id AND language=p_language
    	UNION
    SELECT 2, subject, bodyText, targetRole FROM EmailMessages WHERE id =p_id AND language='en'
  	)
  A WHERE RowNum=1 ORDER by toSort ASC;
END getEventEmail;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETGATEWAYWADOFORSTUDY" (
  p_studyInstanceUid VARCHAR2,
  r_wadoUrl OUT VARCHAR2,
  r_jpegWadoUrl OUT VARCHAR2
)
AS
  l_gateway NUMBER(1);
BEGIN

    r_wadoUrl:=NULL;
    r_jpegWadoUrl:=NULL;

    Select COUNT(paramKey) INTO l_gateway
    FROM GlobalConfiguration
    WHERE paramKey='ActAsGateway' AND 'TRUE'=UPPER(paramValue);

    BEGIN
      IF(l_gateway=1)THEN
          SELECT KN.wadoUrl, KN.jpegWado INTO r_wadoUrl, r_jpegWadoUrl
          FROM KnownNodes KN
          INNER JOIN Studies S ON S.fastestAccess=KN.pk
          WHERE S.studyInstanceUID=p_studyInstanceUid;
      END IF;
    EXCEPTION WHEN NO_DATA_FOUND THEN
      r_wadoUrl:=NULL;
      r_jpegWadoUrl:=NULL;
    END;

END getGatewayWadoForStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETGLOBALCONFIGURATION" (
    p_paramKey IN VARCHAR2,
    resultset OUT Types.ResultSet
)
AS
BEGIN
  OPEN resultset FOR SELECT paramValue FROM GlobalConfiguration WHERE paramKey=p_paramKey AND enabled=1;
END getGlobalConfiguration;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETIMAGEMASKINGPARAMS" (
  p_aeTitle VARCHAR2,
  p_modality VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
  l_knownNodeFK NUMBER(19);
BEGIN

  SELECT pk INTO l_knownNodeFK FROM KnownNodes WHERE aeTitle=p_aeTitle;

  OPEN resultset FOR
    SELECT tagNumber, tagValue, modality, secondTagNumber, secondTagValue, maskCoords FROM ImageMasking WHERE enabled=1 AND knownNodeFK=l_knownNodeFK AND modality = p_modality AND secondTagNumber IS NOT NULL
    UNION
    SELECT tagNumber, tagValue, modality, secondTagNumber, secondTagValue, maskCoords FROM ImageMasking WHERE enabled=1 AND knownNodeFK=l_knownNodeFK AND modality = p_modality AND secondTagNumber IS NULL
    UNION
    SELECT tagNumber, tagValue, modality, secondTagNumber, secondTagValue, maskCoords FROM ImageMasking WHERE enabled=1 AND knownNodeFK=l_knownNodeFK AND modality IS NULL AND secondTagNumber IS NOT NULL
    UNION
    SELECT tagNumber, tagValue, modality, secondTagNumber, secondTagValue, maskCoords FROM ImageMasking WHERE enabled=1 AND knownNodeFK=l_knownNodeFK AND modality IS NULL AND secondTagNumber IS NULL;

END getImageMaskingParams;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETINSTANCELEVELMETADATA" (
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

    Select sopInstanceUID, sopClassUID, instanceNumber, seriesFK FROM StructReps WHERE seriesFK=p_seriesInstanceUid;

END getInstanceLevelMetadata;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETINSTANCES" (
  p_seriesUid VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN

  OPEN resultset FOR
  SELECT sopInstanceUID FROM Images WHERE seriesFK = p_seriesUid ORDER BY instanceNumber asc;

END getInstances;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETINSTANCESINPROGRESS" (
  resultset OUT Types.ResultSet
)
AS
BEGIN

  OPEN resultset FOR
    SELECT instanceUid FROM InstancesInProgress;

END getInstancesInProgress;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETINSTANCESOFVERIFIEDSTUDY" (
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
(std.studyinstanceuid = studyUID AND stv.jobfinishedon IS NULL);
END getInstancesOfVerifiedStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETMESSAGESFORSUBSCRIBER" (
p_node NUMBER,
resultset OUT Types.ResultSet
)AS
BEGIN
OPEN resultset FOR
SELECT msg.*, hl.retries FROM Hl7Messages msg
INNER JOIN Hl7MessageTypes mt ON msg.typeFk = mt.pk
INNER JOIN Hl7Subscriptions sb ON sb.typeFk = msg.typeFk
INNER JOIN Hl7Nodes nd ON nd.pk = sb.nodeFk
left join Hl7Logs hl on hl.nodeFk = nd.pk
WHERE
nd.pk = p_node
AND (
    ((msg.pk > hl.lastMessageFk) AND (hl.retries is null)) or
    ((msg.pk >= hl.lastMessageFk) AND (hl.retries is not null)) or
    hl.lastMessageFk is null)
ORDER BY msg.pk ASC;
END getMessagesForSubscriber;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETOPERATORSSTAT" (
  p_startDate IN DATE,
  p_endDate IN DATE,
  resultset OUT Types.ResultSet
)
AS
BEGIN

  IF(p_startDate IS NULL AND p_endDate IS NULL) THEN
  OPEN RESULTSET FOR
    SELECT se.operatorsName, count(se.seriesInstanceUID)
      FROM Studies st
      INNER JOIN Series se ON se.studyFK = st.studyinstanceUID
      WHERE
      se.operatorsName IS NOT NULL
      GROUP BY se.operatorsName;
  end if;

  IF(p_startDate IS NULL AND p_endDate IS NOT NULL) THEN
    OPEN RESULTSET FOR
    SELECT se.operatorsName, count(se.seriesInstanceUID)
      FROM Studies st
      INNER JOIN Series se ON se.studyFK = st.studyinstanceUID
      WHERE
      se.operatorsName IS NOT NULL AND
      (COALESCE(st.studyDate, st.lastStatusChangeDate) <= p_endDate)
      GROUP BY se.operatorsName;
  end if;

  IF(p_startDate IS NOT NULL AND p_endDate IS NULL) THEN
    OPEN RESULTSET FOR
    SELECT se.operatorsName, count(se.seriesInstanceUID)
      FROM Studies st
      INNER JOIN Series se ON se.studyFK = st.studyinstanceUID
      WHERE
      se.operatorsName IS NOT NULL AND
      (COALESCE(st.studyDate, st.lastStatusChangeDate) >= p_startDate)
      GROUP BY se.operatorsName;
  end if;

  IF(p_startDate IS NOT NULL AND p_endDate IS NOT NULL) THEN
    OPEN RESULTSET FOR
    SELECT se.operatorsName, count(se.seriesInstanceUID)
      FROM Studies st
      INNER JOIN Series se ON se.studyFK = st.studyinstanceUID
      WHERE
      se.operatorsName IS NOT NULL AND
      (COALESCE(st.studyDate, st.lastStatusChangeDate) >= p_startDate) AND
      (COALESCE(st.studyDate, st.lastStatusChangeDate) <= p_endDate)
      GROUP BY se.operatorsName;
  end if;
END getOperatorsStat;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETOPERATORSTATDETAILS" (
  p_opeName IN VARCHAR2,
  p_startDate IN DATE,
  p_endDate IN DATE,
  resultset OUT Types.ResultSet
)
AS
BEGIN
 IF(p_startDate IS NULL AND p_endDate IS NULL) THEN
OPEN RESULTSET FOR
SELECT COALESCE(st.studyDate, st.lastStatusChangeDate) as StudyDate,ST.ACCESSIONNUMBER,st.studyInstanceUID, se.seriesInstanceUID, se.modality, se.numberofseriesrelatedinstances, kn.aeTitle
FROM Studies st
INNER JOIN Series se ON se.studyfk = st.studyinstanceuid
INNER JOIN KnownNodes kn ON kn.pk = se.knownNodeFk
WHERE se.operatorsname = p_opeName;
END IF;
IF(p_startDate IS NULL AND p_endDate IS NOT NULL) THEN
OPEN RESULTSET FOR
  SELECT COALESCE(st.studyDate, st.lastStatusChangeDate) as StudyDate,ST.ACCESSIONNUMBER,st.studyInstanceUID, se.seriesInstanceUID, se.modality, se.numberofseriesrelatedinstances, kn.aeTitle
  FROM Studies st
  INNER JOIN Series se ON se.studyfk = st.studyinstanceuid
  INNER JOIN KnownNodes kn ON kn.pk = se.knownNodeFk
  WHERE
    se.operatorsname = p_opeName AND
    (COALESCE(st.studyDate, st.lastStatusChangeDate) <= p_endDate);
END IF;

IF(p_startDate IS NOT NULL AND p_endDate IS NULL) THEN
    OPEN RESULTSET FOR
     SELECT COALESCE(st.studyDate, st.lastStatusChangeDate) as StudyDate,ST.ACCESSIONNUMBER,st.studyInstanceUID, se.seriesInstanceUID, se.modality, se.numberofseriesrelatedinstances, kn.aeTitle
  FROM Studies st
  INNER JOIN Series se ON se.studyfk = st.studyinstanceuid
  INNER JOIN KnownNodes kn ON kn.pk = se.knownNodeFk
  WHERE
    se.operatorsname = p_opeName AND
      (COALESCE(st.studyDate, st.lastStatusChangeDate) >= p_startDate);
END IF;
 IF(p_startDate IS NOT NULL AND p_endDate IS NOT NULL) THEN
    OPEN RESULTSET FOR
    SELECT COALESCE(st.studyDate, st.lastStatusChangeDate) as StudyDate,ST.ACCESSIONNUMBER,st.studyInstanceUID, se.seriesInstanceUID, se.modality, se.numberofseriesrelatedinstances, kn.aeTitle
  FROM Studies st
  INNER JOIN Series se ON se.studyfk = st.studyinstanceuid
  INNER JOIN KnownNodes kn ON kn.pk = se.knownNodeFk
  WHERE
    se.operatorsname = p_opeName AND
      (COALESCE(st.studyDate, st.lastStatusChangeDate) >= p_startDate) AND
      (COALESCE(st.studyDate, st.lastStatusChangeDate) <= p_endDate);
END IF;
end getOperatorStatDetails;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETORCREATEPATIENT" (
  p_lastName varchar2,
  p_firstName varchar2,
  p_middleName varchar2,
  p_prefix varchar2,
  p_suffix varchar2,
  p_patientId varchar2,
  p_idIssuer varchar2,
  p_patientIdentifierList varchar2,
  p_patientAccountNumber IN OUT varchar2,
  p_visitNumber varchar2,
  p_patPk OUT NUMBER,
  p_patDemoPk OUT NUMBER,
  p_birthDate DATE,
  p_birthTime DATE,
  p_sex CHAR,
  p_race VARCHAR2,
  p_patientAddress VARCHAR2,
  p_patientCity VARCHAR2,
  p_ethnicGroup VARCHAR2
)
AS
  l_pan VARCHAR2(32);
BEGIN

  IF(p_patientAccountNumber='')THEN
    l_pan:=NULL;
  ELSE
    l_pan:=p_patientAccountNumber;
  END IF;

  BEGIN
      SELECT P.pk, PD.pk INTO p_patPk, p_patDemoPk
      FROM Patients P
      INNER JOIN PatientDemographics PD ON PD.patientFK=P.pk
      WHERE UPPER(P.patientID)=UPPER(p_patientId) AND (UPPER(P.idIssuer) IN (UPPER(p_idIssuer), 'NONE'))
            AND (UPPER(PD.patientAccountNumber) LIKE UPPER(CASE WHEN(l_pan IS NULL)THEN '%' ELSE l_pan END) OR PD.patientAccountNumber IS NULL)
            AND (p_lastName IS NULL OR UPPER(P.lastName)=UPPER(p_lastName) OR P.lastName IS NULL)
            AND (p_firstName IS NULL OR UPPER(P.firstName)=UPPER(p_firstName) OR P.firstName IS NULL);

      UPDATE Patients SET idIssuer=UPPER(p_idIssuer) WHERE pk=p_patPk AND UPPER(idIssuer) = 'NONE';


  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      INSERT INTO Patients(lastName, firstName, middleName, prefix, suffix, patientID, idIssuer, birthDate, birthTime, sex) VALUES(p_lastName, p_firstName, p_middleName, p_prefix, p_suffix, p_patientId, p_idIssuer, p_birthDate, p_birthTime, p_sex);

      SELECT PATIENTS_PK_SEQ.CURRVAL INTO p_patPk FROM DUAL;

      INSERT INTO PatientDemographics(patientIdentifierList, patientAccountNumber, patientFK, race, patientAddress, patientCity, ethnicGroup) VALUES(p_patientIdentifierList, l_pan, p_patPk, p_race, p_patientAddress, p_patientCity, p_ethnicGroup);
      SELECT PATIENTDEMOGRAPHICS_PK_SEQ.CURRVAL INTO p_patDemoPk FROM DUAL;

      IF(p_visitNumber IS NOT NULL)THEN
        INSERT INTO WLPatientDataPerVisit(visitNumber, patientFK) VALUES(p_visitNumber, p_patPk);
      END IF;
    WHEN TOO_MANY_ROWS THEN
      p_patPk:=-1;
      p_patDemoPk:=-1;

  END;


END getOrCreatePatient;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB.getparamstoretrieveaccnum (
  p_id NUMBER,
  r_calledAet OUT VARCHAR2,
  r_callingAet OUT VARCHAR2,
  r_moveAet OUT VARCHAR2,
  r_accNUm OUT VARCHAR2,
  r_ip OUT VARCHAR2,
  r_port OUT NUMBER,
  r_knownNode OUT NUMBER,
  r_patientID OUT VARCHAR2,
  r_idIssuer OUT VARCHAR2
)
AS
BEGIN
  BEGIN
--setlog( 'INFO', 'getParamsToRetrieveAccNum', 'IN: p_id = '||p_id, NULL, NULL);

    SELECT msh.calledAet, msh.callingAet, msh.moveAet, msh.accessionNumber, kn.ip, kn.port, kn.pk, msh.patientID, msh.idIssuer
           INTO r_calledAet, r_callingAet, r_moveAet, r_accNUm, r_ip, r_port, r_knownNode, r_patientID, r_idIssuer
    FROM MoveStudyHistory msh, KnownNodes kn
    WHERE msh.id=p_id AND kn.pk = msh.knownNodeFk;

  EXCEPTION WHEN NO_DATA_FOUND THEN

    SELECT NULL, NULL, NULL, NULL, NULL, NULL
    INTO r_calledAet, r_callingAet, r_moveAet, r_accNUm, r_ip, r_port
    FROM DUAL;

  END;
--setlog( 'INFO', 'getParamsToRetrieveAccNum', 'OUT: r_accNUm = '||r_accNUm|| ' r_calledAet = '||r_calledAet|| ' r_callingAet = '||r_callingAet|| ' r_moveAet = '||r_moveAet, NULL, NULL);
END getParamsToRetrieveAccNum;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETPASSWORDCONSTRAINTS" (
    resultset OUT Types.ResultSet )
AS
BEGIN
  OPEN resultset FOR SELECT paramKey,
  paramValue FROM GlobalConfiguration WHERE ((paramKey='PWD_CANREPEAT') OR (paramKey='PWD_MINIMUM_LENGTH') OR (paramKey='PWD_LETTERS_MAND') OR (paramKey='PWD_DIGITS_MAND') OR (paramKey='PWD_SYMBOLS_MAND') OR (paramKey='PWD_BOTHCASES_MAND') OR (paramKey='PWD_VALIDITY_DAYS') OR (paramKey='PWD_WARN_DAYS_B4_EXP')) AND enabled=1;
END getPasswordConstraints;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETPATIENTBASICINFO" (
  p_studyUid IN VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN
 OPEN resultset FOR
 	SELECT PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
	FROM Patients PT
	INNER JOIN Studies ST ON ST.patientFK = PT.pk
	WHERE st.studyInstanceUID = p_studyUid;
END getPatientBasicInfo;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETPATIENTBASICINFOAN" (
  p_accessionNumber IN VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN
 OPEN resultset FOR
 	SELECT PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
	FROM Patients PT
	INNER JOIN Studies ST ON ST.patientFK = PT.pk
	WHERE st.accessionNumber = p_accessionNumber;
END getPatientBasicInfoAN;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETPATIENTHL7CONTEXTSTUDIES" (
  p_patientId NUMBER,
  p_contextId NUMBER,
  resultset OUT Types.ResultSet
)
AS
BEGIN

  IF(p_contextId>0) THEN
    OPEN resultset FOR
      SELECT DISTINCT S.studyFK, St.fastestAccess
      FROM Series S
      INNER JOIN HL7ContextKnownNodes H ON H.knownNodeId=S.knownNodeFK
      INNER JOIN Studies St ON St.studyInstanceUID=S.studyFK
      WHERE H.contextId=p_contextId AND St.patientFK=p_patientId;
  ELSE
    OPEN resultset FOR
      SELECT studyInstanceUID, fastestAccess
      FROM Studies
      WHERE patientFK=p_patientId;
  END IF;

END getPatientHl7ContextStudies;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB.getpatientinfo(
  p_patientId IN VARCHAR2,
  p_idIssuer IN VARCHAR2,
  p_accessionNumber IN VARCHAR2,
  p_studyUid IN VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN
  IF (p_patientId IS NOT NULL) THEN
    IF (p_idIssuer is not null) THEN
        OPEN resultset FOR
        SELECT PT.pk, PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
        FROM Patients PT
      WHERE PT.patientId = p_patientId and PT.idIssuer = p_idIssuer;
    ELSE
    OPEN resultset FOR
        SELECT PT.pk, PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
        FROM Patients PT
      WHERE PT.patientId = p_patientId;
    END IF;
  END IF;
  IF(p_studyUid IS NOT NULL) THEN
    OPEN resultset FOR
  	SELECT PT.pk, PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
    FROM Patients PT
  	INNER JOIN Studies ST ON ST.patientFK = PT.pk
    WHERE ST.studyInstanceUID = p_studyUid;
  END IF;
  IF(p_accessionNumber IS NOT NULL) THEN
     IF (p_patientId is not null) THEN
		OPEN resultset FOR
	 	SELECT PT.pk, PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
		FROM Patients PT
		INNER JOIN Studies ST ON ST.patientFK = PT.pk
		WHERE st.accessionNumber = p_accessionNumber
		AND PT.patientid = p_patientId;
     ELSE
		OPEN resultset FOR
		SELECT PT.pk, PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
		FROM Patients PT
		INNER JOIN Studies ST ON ST.patientFK = PT.pk
		WHERE st.accessionNumber = p_accessionNumber;
	END IF;
  END IF;

END getPatientInfo;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETPATIENTINFOFORRECO" 
  (
    p_studyUid      VARCHAR2,
    resultset OUT Types.ResultSet
  )
AS
BEGIN
  OPEN resultset FOR
  	SELECT (LASTNAME||'^'||FIRSTNAME||'^'||MIDDLENAME||'^'||PREFIX||'^'||SUFFIX) AS PATIENTNAME,PATIENTID, IDISSUER, BIRTHDATE, SEX
	FROM Patients PT
	INNER JOIN Studies ST ON ST.patientFK = PT.pk
	WHERE
    	ST.studyInstanceUID = p_studyUid;
END getPatientInfoForReco;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETPOSSIBLERECOVERIES" (
  p_objectType VARCHAR2,
  p_eventType VARCHAR2,
  resultset OUT Types.ResultSet
) AS
  l_query VARCHAR2(1024);
BEGIN
  l_query   :='SELECT de.pk, de.deprecatedObjType, de.eventType, de.currentUid, de.originalUid, de.reason, u.userName, de.deprecatedOn, COALESCE(P.patientID, Pse.patientID) FROM DeprecationEvents de'||
              ' INNER JOIN Users u ON u.pk=de.deprecatedBy'||
              ' LEFT JOIN Studies St ON de.deprecatedObjType=''ST'' and de.currentUid=St.studyInstanceUID'||
              ' LEFT JOIN Patients P ON P.pk=St.patientFK'||
              ' LEFT JOIN Series Se ON de.deprecatedObjType=''SE'' and de.currentUid=Se.seriesInstanceUID'||
              ' LEFT JOIN Studies Stse ON Stse.studyInstanceUID=Se.studyFK'||
              ' LEFT JOIN Patients Pse ON Pse.pk=Stse.patientFK'||
              ' WHERE de.parentDeprecationFK IS NULL AND de.recoveredOn IS NULL ';


  IF(p_objectType IS NOT NULL) THEN
    l_query:=l_query||'AND de.deprecatedObjType='''||p_objectType||''' ';
  END IF;

  IF(p_eventType IS NOT NULL) THEN
    l_query:=l_query||'AND de.eventType='''||p_eventType||''' ';
  END IF;

  l_query:=l_query|| 'ORDER BY de.deprecatedOn DESC';

  OPEN resultSet FOR l_query;

END getPossibleRecoveries;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETREALSTUDYUID" 
(
  P_STUDYUID IN VARCHAR2
, O_STUDYUID OUT VARCHAR2
, O_DEPRECATED OUT number
) AS
BEGIN
  SELECT COALESCE(originalUid,studyinstanceuid)
  into O_STUDYUID
  FROM studies st
 LEFT JOIN deprecationevents dp ON dp.currentuid = st.studyinstanceuid
 where studyinstanceuid = P_STUDYUID;

 SELECT coalesce(deprecated,1) INTO O_DEPRECATED
 FROM studies
 WHERE studyinstanceuid = O_STUDYUID;

 EXCEPTION
    WHEN NO_DATA_FOUND THEN
   O_DEPRECATED := 0;
END GETREALSTUDYUID;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETRETRIEVALINFO" (
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

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSERIESBASICINFO" (
  p_studyInstanceUid VARCHAR2,
  p_seriesInstanceUid VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN

  OPEN resultset FOR
  SELECT Se.seriesInstanceUID, Se.modality, St.fastestAccess, St.studyStatus
  FROM Series Se
  INNER JOIN Studies St on St.studyInstanceUID=Se.studyFK
  WHERE Se.seriesInstanceUID=p_seriesInstanceUid AND St.studyInstanceUID=p_studyInstanceUid;

END getSeriesBasicInfo;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSERIESINSTANCES" (
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
      else 'PresStates'
    end INTO l_instanceTable
    from dual;

  OPEN resultset FOR
        'SELECT T.sopInstanceUID, T.sopClassUID, T.instanceNumber, MT.name FROM '||l_instanceTable||' T
        INNER JOIN SupportedSOPClasses SOP ON SOP.sopClassUid=T.sopClassUid
        INNER JOIN MimeTypes MT ON MT.id=SOP.mimeType
        WHERE T.seriesFK='''||p_seriesInstanceUid||'''';

END getSeriesInstances;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSERIESLEVELMETADATA" (
  p_studyInstanceUid VARCHAR2,
  resultSet OUT Types.ResultSet
)
AS
BEGIN

  OPEN resultSet FOR
  Select Se.seriesInstanceUID, Se.modality, Se.seriesDescription, Se.seriesNumber
  FROM Studies S
  INNER JOIN Series Se ON S.studyInstanceUID=Se.studyFK
  WHERE S.studyInstanceUID=p_studyInstanceUid;

END getSeriesLevelMetadata;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSTUDIESFORMOVEVISIT" (
  p_oldPatientPk NUMBER,
  p_oldVisitNumber VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN
setlog( 'INFO', 'getStudiesForMoveVisit', 'p_oldPatientPk = '||p_oldPatientPk||' p_oldVisitNumber = '||p_oldVisitNumber, NULL, NULL);

  OPEN resultset FOR
    SELECT S.studyInstanceUID FROM Studies S
    INNER JOIN WLPatientDataPerVisit W ON W.studyFK=S.studyInstanceUID AND W.patientFK=S.patientFK
    WHERE W.visitNumber=p_oldVisitNumber AND W.patientFK=p_oldPatientPk;

END getStudiesForMoveVisit;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSTUDIESOLDERTHAN" (
  p_days NUMBER,
  p_studyStatus CHAR,
  resultset OUT Types.ResultSet
)
AS
BEGIN
setlog( 'INFO', 'getStudiesOlderThan', 'p_days = '||p_days, NULL, NULL);

  OPEN resultset FOR
    SELECT studyInstanceUID from Studies
    WHERE studyStatus=p_studyStatus AND lastStatusChangeDate<SYSTIMESTAMP-p_days;

END getStudiesOlderThan;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSTUDIESTODELETE" (
    p_toleranceInHours NUMBER,
    resultset OUT Types.ResultSet
) AS
BEGIN
setlog( 'INFO', 'getStudiesToDelete', 'p_toleranceInHours = '||p_toleranceInHours, NULL, NULL);

  OPEN resultSet FOR
  SELECT St.studyInstanceUID, St.fastestAccess
  FROM DeprecationEvents de
  INNER JOIN Studies St ON de.currentUid=St.studyInstanceUID
  Where St.deprecated=1 and de.eventType='DEL' AND de.recoveredOn IS NULL AND de.deprecatedOn + NUMTODSINTERVAL(p_toleranceInHours, 'HOUR') < systimestamp;

END getStudiesToDelete;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSTUDIESTOMARK" (
    p_toleranceInHours NUMBER,
    resultset OUT Types.ResultSet
) AS
BEGIN
setlog( 'INFO', 'getStudiesToMark', 'p_toleranceInHours = '||p_toleranceInHours, NULL, NULL);

  OPEN resultSet FOR
  SELECT studyInstanceUID FROM Studies WHERE deprecated=0 AND lastStatusChangeDate + NUMTODSINTERVAL(p_toleranceInHours, 'HOUR') < systimestamp;

END getStudiesToMark;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSTUDIESTOVERIFY" (
    p_threshold IN TIMESTAMP,
    resultset OUT Types.ResultSet
)
AS
BEGIN
setlog( 'INFO', 'getStudiesToVerify', 'p_threshold = '||p_threshold, NULL, NULL);

  OPEN resultset FOR SELECT stv.studyFK,
  nvl(COUNT(DISTINCT IM.SOPINSTANCEUID ),0) + nvl(COUNT(DISTINCT SR.SOPINSTANCEUID ),0) numberOfStudyRelatedInstances,
  kn.aeTitle,
  kn.ip,
  kn.port,
  kn.prefCallingAet,
  st.numberOfStudyRelatedInstances numberOfStudyRelatedInstances2
  FROM StudiesToVerify stv
  INNER JOIN Studies st ON st.studyInstanceUID = stv.studyFK
  INNER JOIN KnownNodes kn ON kn.pk = stv.sourceAeTitleFK
  LEFT JOIN SERIES se ON se.STUDYFK =  st.STUDYINSTANCEUID
  LEFT JOIN IMAGES im ON im.SERIESFK = se.SERIESINSTANCEUID
  LEFT JOIN STRUCTREPS sr ON sr.SERIESFK = se.SERIESINSTANCEUID
  WHERE stv.verifiedDate IS NULL AND (stv.lastInsertedDate < p_threshold and ST.LASTSTATUSCHANGEDATE < p_threshold) and stv.tobeignored = 0
  group by stv.studyFK,
  kn.aeTitle,
  kn.ip,
  kn.port,
  kn.prefCallingAet, st.numberOfStudyRelatedInstances;

END getStudiesToVerify;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSTUDY" (
  p_patientFk IN NUMBER,
  p_filter IN VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN
setlog( 'INFO', 'getStudy', 'p_patientFk = '||p_patientFk, NULL, NULL);

IF (p_filter IS NOT NULL) THEN
OPEN resultset FOR
  	SELECT ST.studyDate, ST.studyTime, ST.studyDescription, ST.accessionNumber, ST.studyInstanceUID, ST.studyStatus
  	FROM Studies ST
 	WHERE
  ST.patientFk = p_patientFk AND(
  ST.accessionNumber = p_filter or st.studyInstanceUID = p_filter)
  ORDER BY ST.studyDate DESC;
ELSE
OPEN resultset FOR
  	SELECT ST.studyDate, ST.studyTime, ST.studyDescription, ST.accessionNumber, ST.studyInstanceUID, ST.studyStatus
  	FROM Studies ST
 	WHERE
  ST.patientFk = p_patientFk
  ORDER BY ST.studyDate DESC;
END IF;

END getStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSTUDYBASICINFO" (
  p_studyUid IN VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN
  OPEN resultset FOR
  	SELECT ST.studyDate, ST.studyTime, ST.studyDescription, ST.accessionNumber
  	FROM Studies ST
 	WHERE ST.studyInstanceUID = p_studyUid;
END getStudyBasicInfo;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSTUDYBASICINFOAN" (
  p_accessionNumber IN VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN
  OPEN resultset FOR
  	SELECT ST.studyDate, ST.studyTime, ST.studyDescription, ST.studyInstanceUID
  	FROM Studies ST
 	WHERE ST.accessionNumber = p_accessionNumber;
END getStudyBasicInfoAN;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSTUDYINFOFORRECO" (
    p_studyUid       VARCHAR2,
    resultset OUT Types.ResultSet
)
AS
BEGIN
  OPEN resultset FOR
  	SELECT
		studyInstanceUID,
		studyID,
		studyDate,
		studyTime,
		studyCompletionDate,
		studyCompletionTime,
		studyVerifiedDate,
		studyVerifiedTime,
		accessionNumber
	FROM Studies ST
	WHERE
    ST.studyInstanceUID = p_studyUid;
END getStudyInfoForReco;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSTUDYINFORMATION" (
p_studyUID varchar2 ,
resultset OUT Types.ResultSet
)AS
BEGIN
OPEN resultset FOR
select
pt.lastName,
pt.firstName,
pt.patientId,
pt.idIssuer,
pt.birthDate,
pt.sex,
st.accessionNumber,
st.studyDate,
st.studyDescription,
st.numberOfStudyRelatedInstances,
wlp.visitNumber,
cs.codeMeaning,
cs.codeValue

from Studies st
inner join Patients pt on pt.pk = st.patientFk
inner join CodeSequences cs on cs.pk = st.procedureCodeSequenceFK
inner join WLPatientDataPerVisit wlp  on wlp.studyFk = st.studyInstanceUID
where studyInstanceUID = p_studyUID;

END getStudyInformation;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSTUDYLEVELMETADATA" (
  p_studyInstanceUid VARCHAR2,
  resultSet OUT Types.ResultSet
)
AS
BEGIN

  OPEN resultSet FOR
  Select P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix,
         P.patientID,
         P.birthDate,
         P.sex,
         S.studyInstanceUID,
         S.accessionNumber,
         S.studyDate,
         S.studyDescription,
         Kn.wadoUrl
  FROM Studies S
  INNER JOIN Patients P ON S.patientFK=P.pk
  LEFT JOIN KnownNodes Kn ON Kn.pk=S.fastestAccess
  WHERE S.studyInstanceUID=p_studyInstanceUid;

END getStudyLevelMetadata;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSTUDYNUMBEROFIMAGES" 
(
  P_STUDYUID IN VARCHAR2
, O_NUMOFINSTANCES OUT NUMBER
) AS
BEGIN
  SELECT numberOfStudyRelatedInstances INTO O_NUMOFINSTANCES FROM Studies
  where studyinstanceuid = P_studyUID;
END getStudyNumberOfImages;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETSTUDYTOVERIFY" (
    studyUID IN VARCHAR2,
    resultset OUT Types.ResultSet
  )
AS
BEGIN
  OPEN resultset FOR SELECT stv.studyFK,
  st.numberOfStudyRelatedInstances,
  kn.aeTitle,
  kn.ip,
  kn.port,
  kn.prefCallingAet FROM StudiesToVerify stv
  INNER JOIN Studies st ON st.studyInstanceUID = stv.studyFK
  INNER JOIN KnownNodes kn ON kn.pk = stv.sourceAeTitleFK
  WHERE stv.verifiedDate IS NULL AND st.studyInstanceUID = studyUID;
END getStudyToVerify;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB.getTo3pdi_Conf(
    p_paramKey IN VARCHAR2,
    resultset OUT Types.ResultSet
)
AS
BEGIN
  OPEN resultset FOR SELECT paramValue FROM To3pdi_Conf WHERE paramKey=p_paramKey AND enabled=1;
END getTo3pdi_Conf;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETURLTOINSTANCE" (
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
      ) I ON I.seriesFK=Se.seriesInstanceUID;

  EXCEPTION WHEN NO_DATA_FOUND THEN
    p_url:=NULL;
  END;


END getUrlToInstance;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETURLTOONLINEINSTANCE" (
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
      ) I ON I.seriesFK=Se.seriesInstanceUID;

  EXCEPTION WHEN NO_DATA_FOUND THEN
    p_url:=NULL;
  END;


END getUrlToOnlineInstance;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETUSERFOREMAIL" (
    p_email VARCHAR2,
    resultset OUT Types.ResultSet
)
AS
BEGIN
  OPEN resultset FOR SELECT pk FROM Users WHERE email=p_email;
END getUserForEmail;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."GETVERIFIEDSTUDIESINSTANCES" 
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
	(stv.toBeIgnored = 0 AND stv.jobfinishedon is null);
END getVerifiedStudiesInstances;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."IAINSERTCORRECTSTUDIES" (
    p_completedOnSeconds NUMBER,
    p_stringForSet VARCHAR2,
    p_source VARCHAR2,
    p_targetApp VARCHAR2,
    p_updatedRows OUT NUMBER
)
AS
BEGIN

  INSERT INTO ImageEvents(patientId, accessionNumber, studyInstanceUid, event, source, targetApp)
  SELECT P.patientID, S.accessionNumber, S.studyInstanceUID, p_stringForSet, p_source, p_targetApp
  FROM StudyAvailability SA
  INNER JOIN Studies S ON S.studyInstanceUID=SA.studyInstanceUID AND S.toReconcile=0
  INNER JOIN Patients P ON P.pk=S.patientFK
  WHERE SA.completedOnSeconds=p_completedOnSeconds AND SA.published=0;

  p_updatedRows := SQL%ROWCOUNT;

  UPDATE StudyAvailability SET published=1
  WHERE completedOnSeconds=p_completedOnSeconds AND studyInstanceUID IN (SELECT studyInstanceUid FROM ImageEvents WHERE status=0);


END iaInsertCorrectStudies;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."IAINSERTCORRECTSTUDY" (
    p_studyUid VARCHAR2,
    p_accNum VARCHAR2,
    p_patId VARCHAR2,
    p_stringForSet VARCHAR2,
    p_source VARCHAR2,
    p_targetApp VARCHAR2,
    p_updatedRows OUT NUMBER
)
AS
BEGIN

  IF(p_accNum IS NULL OR p_patId IS NULL)THEN
    INSERT INTO ImageEvents(patientId, accessionNumber, studyInstanceUid, event, source, targetApp)
    SELECT P.patientID, S.accessionNumber, p_studyUid, p_stringForSet, p_source, p_targetApp
    FROM Studies S
    INNER JOIN Patients P on P.pk=S.patientFK
    WHERE S.studyInstanceUID=p_studyUid;
  ELSE
    INSERT INTO ImageEvents(patientId, accessionNumber, studyInstanceUid, event, source, targetApp)
    VALUES(p_patId, p_accNum, p_studyUid, p_stringForSet, p_source, p_targetApp);
  END IF;


  p_updatedRows := SQL%ROWCOUNT;

  UPDATE StudyAvailability SET published=1 WHERE studyInstanceUid=p_studyUid;

END iaInsertCorrectStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."IARECONCILEWRONGSTUDY" (
    p_studyUid VARCHAR2,
    p_accNum VARCHAR2,
    p_patId VARCHAR2,
    p_stringForSet VARCHAR2,
    p_stringForReset VARCHAR2,
    p_source VARCHAR2,
    p_targetApp VARCHAR2,
    p_updatedRows OUT NUMBER
)
AS
  l_sameSt NUMBER(19);
  l_sameAccNum NUMBER(19);
  l_toPublish NUMBER(11);
BEGIN

  SELECT COUNT(studyInstanceUid) INTO l_toPublish FROM StudyAvailability WHERE studyInstanceUid=p_studyUid AND published=1 and completed=1;
  IF l_toPublish=0 THEN
    p_updatedRows:=0;
    RETURN;
  END IF;

  Select MAX(id) INTO l_sameSt from ImageEvents where studyInstanceUID=p_studyUid AND event=p_stringForSet;

  IF(l_sameSt IS NOT NULL) THEN
    INSERT INTO ImageEvents(patientId, accessionNumber, studyInstanceUid, event, source, targetApp)
      SELECT patientId, accessionNumber, studyInstanceUid, p_stringForReset, p_source, p_targetApp
      FROM ImageEvents
      WHERE id=l_sameSt;
  END IF;

  IF(p_accNum IS NOT NULL) THEN
    Select MAX(id) INTO l_sameAccNum from ImageEvents where accessionNumber=p_accNum AND event=p_stringForSet;

    IF(l_sameAccNum IS NOT NULL AND l_sameAccNum<>l_sameSt) THEN
      INSERT INTO ImageEvents(patientId, accessionNumber, studyInstanceUid, event, source, targetApp)
        SELECT patientId, accessionNumber, studyInstanceUid, p_stringForReset, p_source, p_targetApp
        FROM ImageEvents
        WHERE id=l_sameAccNum;
    END IF;
  END IF;

  IF(p_accNum IS NULL OR p_patId IS NULL)THEN
    INSERT INTO ImageEvents(patientId, accessionNumber, studyInstanceUid, event, source, targetApp)
    SELECT P.patientID, S.accessionNumber, p_studyUid, p_stringForSet, p_source, p_targetApp
    FROM Studies S
    INNER JOIN Patients P on P.pk=S.patientFK
    WHERE S.studyInstanceUID=p_studyUid;
  ELSE
    INSERT INTO ImageEvents(patientId, accessionNumber, studyInstanceUid, event, source, targetApp)
    VALUES(p_patId, p_accNum, p_studyUid, p_stringForSet, p_source, p_targetApp);
  END IF;

  p_updatedRows := SQL%ROWCOUNT;

  UPDATE StudyAvailability SET published=1 WHERE studyInstanceUid=p_studyUid;

END iaReconcileWrongStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."IASWAPPATIENTASSOCIATION" (
    p_studyUid1 VARCHAR2,
    p_accNum1 VARCHAR2,
    p_patId1 VARCHAR2,
    p_studyUid2 VARCHAR2,
    p_accNum2 VARCHAR2,
    p_patId2 VARCHAR2,
    p_stringForSet VARCHAR2,
    p_stringForReset VARCHAR2,
    p_source VARCHAR2,
    p_targetApp VARCHAR2,
    p_updatedRows OUT NUMBER
)
AS
  l_lastCount NUMBER(11);
BEGIN

  p_updatedRows:=0;

  Select COUNT(id) INTO l_lastCount FROM ImageEvents WHERE accessionNumber=p_accNum1 AND studyInstanceUid=p_studyUid1 AND patientId=p_patId1 AND event=p_stringForSet;
  IF(l_lastCount<>1) THEN
    p_updatedRows:=-1;  -- The first triplet was not found
  END IF;

  Select COUNT(id) INTO l_lastCount FROM ImageEvents WHERE accessionNumber=p_accNum2 AND studyInstanceUid=p_studyUid2 AND patientId=p_patId2 AND event=p_stringForSet;
  IF(l_lastCount<>1) THEN
    p_updatedRows:=-2;  -- The second triplet was not found
  END IF;

  IF(p_updatedRows=0)THEN

    -- RESET --------------------------
    INSERT INTO ImageEvents(patientId, accessionNumber, studyInstanceUid, event, source, targetApp)
    VALUES(p_patId1, p_accNum1, p_studyUid1, p_stringForReset, p_source, p_targetApp);

    p_updatedRows:=p_updatedRows+1;

    INSERT INTO ImageEvents(patientId, accessionNumber, studyInstanceUid, event, source, targetApp)
    VALUES(p_patId2, p_accNum2, p_studyUid2, p_stringForReset, p_source, p_targetApp);

    p_updatedRows:=p_updatedRows+1;

    -- SET --------------------------
    INSERT INTO ImageEvents(patientId, accessionNumber, studyInstanceUid, event, source, targetApp)
    VALUES(p_patId2, p_accNum1, p_studyUid1, p_stringForSet, p_source, p_targetApp);

    p_updatedRows:=p_updatedRows+1;

    INSERT INTO ImageEvents(patientId, accessionNumber, studyInstanceUid, event, source, targetApp)
    VALUES(p_patId1, p_accNum2, p_studyUid2, p_stringForSet, p_source, p_targetApp);

    p_updatedRows:=p_updatedRows+1;

    UPDATE StudyAvailability SET published=1 WHERE studyInstanceUid IN (p_studyUid1, p_studyUid2);

  END IF;

END iaSwapPatientAssociation;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."IDENTIFYPATIENT" (
    p_patientId VARCHAR2,
    p_idIssuer VARCHAR2,
    p_lastName VARCHAR2,
    p_firstName VARCHAR2,
    p_birthDate DATE,
    p_updateIssuer NUMBER,
    r_pk OUT NUMBER,
    r_withDoubts OUT NUMBER
)AS
  l_count NUMBER(10);
BEGIN

  SELECT 0 INTO r_withDoubts FROM DUAL;

    SELECT COUNT(P.pk) INTO l_count
    FROM Patients P
    WHERE P.patientID=p_patientId AND P.idIssuer=p_idIssuer
      AND ((p_lastName IS NULL AND P.lastName IS NULL)OR(P.lastName LIKE CONCAT(UPPER(p_lastName),'%')))
      AND ((p_firstName IS NULL AND P.firstName IS NULL)OR(P.firstName LIKE CONCAT(UPPER(p_firstName),'%')))
      AND p_birthDate = P.birthDate;

    CASE l_count
      WHEN 0 THEN r_pk:=NULL;
      WHEN 1 THEN
        BEGIN
          SELECT P.pk INTO r_pk FROM Patients P
            WHERE P.patientID=p_patientId AND P.idIssuer=p_idIssuer
            AND ((p_lastName IS NULL AND P.lastName IS NULL)OR(P.lastName LIKE CONCAT(UPPER(p_lastName),'%')))
            AND ((p_firstName IS NULL AND P.firstName IS NULL)OR(P.firstName LIKE CONCAT(UPPER(p_firstName),'%')))
            AND p_birthDate = P.birthDate;
          RETURN;
        END;
      ELSE
        BEGIN
          r_pk:=-1;
          RETURN;
        END;
    END CASE;

    -- Here only if l_count was 0

    SELECT COUNT(P.pk) INTO l_count
    FROM Patients P
    WHERE P.patientID=p_patientId AND P.idIssuer='NONE' AND p_updateIssuer=1
      AND ((p_lastName IS NULL AND P.lastName IS NULL)OR(P.lastName LIKE CONCAT(UPPER(p_lastName),'%')))
      AND ((p_firstName IS NULL AND P.firstName IS NULL)OR(P.firstName LIKE CONCAT(UPPER(p_firstName),'%')))
      AND p_birthDate = P.birthDate;

    CASE l_count
      WHEN 0 THEN r_pk:=NULL;
      WHEN 1 THEN
        BEGIN
          SELECT P.pk INTO r_pk FROM Patients P
            WHERE P.patientID=p_patientId AND P.idIssuer ='NONE' AND ((p_lastName IS NULL AND P.lastName IS NULL)OR(P.lastName LIKE CONCAT(UPPER(p_lastName),'%'))) AND ((p_firstName IS NULL AND P.firstName IS NULL)OR(P.firstName LIKE CONCAT(UPPER(p_firstName),'%')))
            AND p_birthDate = P.birthDate;
          UPDATE Patients SET idIssuer=p_idIssuer WHERE pk=r_pk and p_updateIssuer=1;
          RETURN;
        END;
      ELSE
        BEGIN
          r_pk:=-1;
          RETURN;
        END;
    END CASE;

    -- Here only if l_count was 0

    SELECT 1 INTO r_withDoubts FROM DUAL;

    SELECT COUNT(P.pk) INTO l_count
    FROM Patients P
    WHERE P.patientID=p_patientId AND P.idIssuer=p_idIssuer;

    CASE l_count
      WHEN 0 THEN r_pk:=NULL;
      WHEN 1 THEN
        BEGIN
          SELECT P.pk INTO r_pk FROM Patients P WHERE P.patientID=p_patientId AND P.idIssuer=p_idIssuer;
          RETURN;
        END;
      ELSE
        BEGIN
          r_pk:=-1;
          RETURN;
        END;
    END CASE;

    -- Here only if l_count was 0
    SELECT COUNT(P.pk) INTO l_count
    FROM Patients P
    WHERE P.patientID=p_patientId AND P.idIssuer ='NONE' AND p_updateIssuer=1;

    CASE l_count
      WHEN 0 THEN r_pk:=NULL;
      WHEN 1 THEN
        BEGIN
          SELECT P.pk INTO r_pk FROM Patients P WHERE P.patientID=p_patientId AND P.idIssuer=p_idIssuer;
          UPDATE Patients SET idIssuer=p_idIssuer WHERE pk=r_pk and p_updateIssuer=1;
          RETURN;
        END;
      ELSE
        BEGIN
          r_pk:=-1;
          RETURN;
        END;
    END CASE;


END identifyPatient;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."IDENTIFYPERSONNEL" (
  p_lastName VARCHAR2,
  p_firstName VARCHAR2,
  p_middleName VARCHAR2,
  p_prefix VARCHAR2,
  p_suffix VARCHAR2,
  r_pk OUT NUMBER
)
AS
BEGIN

  BEGIN

    SELECT pk INTO r_pk FROM Personnel WHERE
      (lastName=UPPER(p_lastName) OR (lastName IS NULL AND p_lastName IS NULL)) AND
      (firstName=UPPER(p_firstName) OR (firstName IS NULL AND p_firstName IS NULL)) AND
      (middleName=UPPER(p_middleName) OR (middleName IS NULL AND p_middleName IS NULL)) AND
      (prefix=UPPER(p_prefix) OR (prefix IS NULL AND p_prefix IS NULL)) AND
      (suffix=UPPER(p_suffix) OR (suffix IS NULL AND p_suffix IS NULL)) AND
      ROWNUM<=1;

  EXCEPTION WHEN NO_DATA_FOUND THEN

    INSERT INTO Personnel(lastName, firstName, middleName, prefix, suffix)
    VALUES(UPPER(p_lastName), UPPER(p_firstName), UPPER(p_middleName), UPPER(p_prefix), UPPER(p_suffix))
    RETURNING pk INTO r_pk;

  END;

END identifyPersonnel;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."INITSCHEDULEPROCESS" (
  p_id OUT NUMBER
)
AS
BEGIN

  INSERT INTO ScheduleProcesses(startedOnUtc) VALUES(SYS_EXTRACT_UTC(current_timestamp));
  SELECT SCHEDULEPROCESSES_PK_SEQ.CURRVAL INTO p_id FROM DUAL;

END initScheduleProcess;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."INSERTHL7ALLERGY" (
  p_allergyType CHAR,
  p_allergyMnemonic VARCHAR2,
  p_allergySeverity CHAR,
  p_allergyReaction VARCHAR2,
  p_identificationDate DATE,
  p_patientFK NUMBER
)
AS
BEGIN
  INSERT INTO HL7Allergies(allergyType, allergyMnemonic, allergySeverity, allergyReaction, identificationDate, patientFK)
  VALUES (p_allergyType, p_allergyMnemonic, p_allergySeverity, p_allergyReaction, p_identificationDate, p_patientFK);
END insertHL7Allergy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."INSERTHL7OBSERVATION" (
  p_valueType VARCHAR2,
  p_observationValue CLOB,
  p_units VARCHAR2,
  p_observResultStatus CHAR,
  p_wlPatientDataPerVisitFk NUMBER
)
AS
BEGIN
  INSERT INTO HL7Observations(valueType, observationValue, units, observResultStatus, wlPatientDataPerVisitFK)
  VALUES(p_valuetype, p_observationvalue, p_units, p_observresultstatus, p_wlpatientdatapervisitfk);
END insertHL7Observation;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."INSERTKOSRELATIONSHIP" 
(
  p_OBJECTUID IN VARCHAR2,
  p_PARENTUID IN VARCHAR2
) AS
BEGIN
  INSERT INTO KosRelationships values (p_OBJECTUID, p_PARENTUID);
END insertKosRelationship;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."INSERTSTUDYAVLNOTIFICATION" (
    p_message CLOB,
    p_notificationType VARCHAR2,
    p_studyUID varchar2
) AS
BEGIN
if p_notificationType  = 'WN' then
    insert into Hl7Messages (message, typeFk) values (p_message,1);
elsif p_notificationType  = 'WU' then
    insert into Hl7Messages (message, typeFk) values (p_message,3);
elsif p_notificationType  = 'NWN' then
    insert into Hl7Messages (message, typeFk) values (p_message,7);
elsif p_notificationType  = 'NWU' then
    insert into Hl7Messages (message, typeFk) values (p_message,8);
else
    raise "CASE_NOT_FOUND";
  end if;
END insertStudyAvlNotification;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."INSERTSTUDYTOVERIFY" (
    p_studyInstanceUID IN VARCHAR2,
    p_aeTitle          IN VARCHAR2,
    p_lastInsertedDate IN TIMESTAMP,
    p_xdsMessageId     IN VARCHAR2,
    p_error OUT NUMBER
)
AS
  l_count NUMBER(11);
  l_pkNode NUMBER(19);
BEGIN
setlog( 'INFO', 'insertStudyToVerify', 'p_studyInstanceUID = '||p_studyInstanceUID||' p_aeTitle = '||p_aeTitle||' p_xdsMessageId = '||p_xdsMessageId, NULL, NULL);

  p_error := 1;
  SELECT COUNT(studyFK) INTO l_count FROM StudiesToVerify WHERE studyFK = p_studyInstanceUID ;
  IF(l_count = 0) THEN
    SELECT pk INTO l_pkNode FROM KnownNodes WHERE aeTitle = p_aeTitle;
    INSERT INTO studiesToVerify
      ( studyFK, sourceAeTitleFK, lastInsertedDate, xdsMessageId
      ) VALUES
      ( p_studyInstanceUID, l_pkNode, p_lastInsertedDate, p_xdsMessageId
      );
    p_error:=0;
  END IF;
END insertStudyToVerify;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."INSERTVERIFICATIONEVENT" (
  studyUid VARCHAR2,
  eventType NUMBER,
  eventDescription VARCHAR2)
  AS
  begin
  INSERT INTO StudiesVerifierEvents(STUDYFK,EVENTTYPE,EVENTMESSAGE) VALUES (studyUid,eventType,eventDescription);
  end insertVerificationEvent;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."INSERTVERIFIEDDATE" 
  (
    p_StudyUID     IN VARCHAR2,
    p_VerifiedDate IN TIMESTAMP
  )
AS
BEGIN
  UPDATE StudiesToVerify SET verifiedDate = p_VerifiedDate WHERE studyFK = p_StudyUID;
END insertVerifiedDate;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."ISCONVERTED" (
  p_seriesUid VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN

  OPEN resultset FOR
  SELECT Se.seriesInstanceUID, St.fastestAccess||St.studyInstanceUID||'/' directoryToStudy, Se.studyFK
	FROM Series Se
	INNER JOIN Studies St ON St.studyInstanceUID=Se.studyFK
	WHERE Se.seriesInstanceUID = p_seriesUid
		  AND Se.convertedToMf IS NULL
		  AND St.studyStatus='o';

	UPDATE Series SET convertedToMf=SYS_EXTRACT_UTC(current_timestamp) WHERE seriesInstanceUID=p_seriesUid
	AND convertedToMf IS NULL;

END isConverted;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."ISINSTANCEPRESENT" (
  p_instanceUid VARCHAR2,
  p_table VARCHAR2,
  r_outcome OUT NUMBER    -- 1:  instance present, 0: instance not present, -1: instance being processed
)
AS

  l_count NUMBER(1);

BEGIN

  r_outcome:=0;

  SELECT COUNT(instanceUid) INTO l_count FROM InstancesInProgress IIP WHERE instanceUid = p_instanceUid;

  IF(l_count=0) THEN

    INSERT INTO InstancesInProgress(instanceUid) VALUES (p_instanceUid);      -- <<<<<<<<<<------- The instance being processed is stored in the table
    EXECUTE IMMEDIATE 'SELECT COUNT(sopInstanceUID) FROM '|| p_table || ' WHERE sopInstanceUID = '''||p_instanceUid||'''' INTO l_count;
    IF(l_count=1) THEN
      SELECT COUNT(sopInstanceUID) INTO l_count FROM Hashtable WHERE sopInstanceUID = p_instanceUid;
      IF(l_count=1) THEN
        r_outcome:=1;
      ELSE
        EXECUTE IMMEDIATE 'DELETE FROM '|| p_table || ' WHERE sopInstanceUID = '''||p_instanceUid||'''';
      END IF;
    END IF;

  ELSE

    r_outcome:=-1;

  END IF;

END isInstancePresent;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB.isUserAuthenticated(
  p_username VARCHAR2,
  p_password VARCHAR2,
  p_result OUT NUMBER
)
AS
BEGIN

  SELECT COUNT(pk) INTO p_result FROM Users WHERE LOWER(userName)=LOWER(p_username) AND password=p_password;

END isUserAuthenticated;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."MAPSETTINGFROMMEDIATOSTUDY" (
    p_mediaPk  IN NUMBER ,
   	p_studyUid IN VARCHAR2
  )
AS
  l_storageSetting NUMBER(19);
  l_count NUMBER(1);
BEGIN
  SELECT storagePolicyId INTO l_storageSetting FROM PhysicalMedia WHERE pk=p_mediaPk;

  SELECT COUNT(studyInstanceUID) INTO l_count FROM Studies WHERE studyInstanceUid=p_studyUid AND storagePolicyId IS NULL;

  IF ( l_count = 1 )THEN
    UPDATE Studies SET lastStatusChangeDate=SYSTIMESTAMP, storagePolicyId=l_storageSetting WHERE studyInstanceUid=p_studyUid AND storagePolicyId IS NULL;
  ELSE
    UPDATE Studies SET lastStatusChangeDate=SYSTIMESTAMP WHERE studyInstanceUid=p_studyUid;
  END IF;


END mapSettingFromMediaToStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."MOVESTUDY" (
  p_studyUid VARCHAR2,
  p_oldPatientPk NUMBER,
  p_newPatientPk NUMBER,
  p_newVisitNumber VARCHAR2,
  p_visitPk NUMBER,
  p_howMany OUT NUMBER
)
AS
BEGIN
setlog( 'INFO', 'moveStudy', 'p_studyUid = '||p_studyUid, NULL, NULL);

  SELECT COUNT(S.studyInstanceUID) INTO p_howMany FROM Studies S
  INNER JOIN WLPatientDataPerVisit W ON W.studyFK=S.studyInstanceUID AND W.patientFK=S.patientFK
  WHERE W.pk=p_visitPk AND W.patientFK=p_oldPatientPk AND S.studyInstanceUID=p_studyUid;

  UPDATE Studies SET patientFK=p_newPatientPk, toReconcile=0 WHERE studyInstanceUID=p_studyUid;

  UPDATE WLPatientDataPerVisit SET patientFK= p_newPatientPk, visitNumber = CASE WHEN p_newVisitNumber IS NULL THEN visitNumber ELSE p_newVisitNumber END WHERE pk=p_visitPk;

  UPDATE PatientDemographics SET numberOfPatientRelatedStudies=numberOfPatientRelatedStudies-p_howMany WHERE patientFK=p_oldPatientPk;
  UPDATE PatientDemographics SET numberOfPatientRelatedStudies=numberOfPatientRelatedStudies+p_howMany WHERE patientFK=p_newPatientPk;

END moveStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."MOVESTUDYNEARLINE" (
  p_studyInstanceUID VARCHAR2,
  r_outcome OUT NUMBER  -- 0: study with no Images, -1: general exception, >1: number of images moved, -2: discrepancy in number of images inserted/deleted, -3: study not found
)
AS
  l_affectedRows NUMBER(11);

BEGIN

  r_outcome:=0;

  BEGIN
    UPDATE Studies SET studyStatus='n' WHERE studyInstanceUID=p_studyInstanceUID AND studyStatus='o';
    r_outcome:=SQL%ROWCOUNT;

    IF(r_outcome=0)THEN
      r_outcome:=-3;
    END IF;

    IF(r_outcome=1)THEN

      l_affectedRows:=0;
      r_outcome:=0;

      FOR c_series in (SELECT seriesInstanceUID FROM Series where studyFK=p_studyInstanceUID)
       LOOP
          INSERT INTO ImagesNearline(sopInstanceUID,sopClassUID,instanceNumber,seriesFK,deprecated,stgCommitted,samplesPerPixel,rowsNum,columnsNum,bitsAllocated,bitsStored,highBit,pixelRepresentation)
              Select I.sopInstanceUID,I.sopClassUID,I.instanceNumber,I.seriesFK,I.deprecated,I.stgCommitted,I.samplesPerPixel,I.rowsNum,I.columnsNum,I.bitsAllocated,I.bitsStored,I.highBit,I.pixelRepresentation
              FROM Images I
              WHERE I.seriesFK=c_series.seriesInstanceUID;
          l_affectedRows:=l_affectedRows+SQL%ROWCOUNT;
          DELETE FROM Images WHERE seriesFK=c_series.seriesInstanceUID;
          r_outcome:=r_outcome+SQL%ROWCOUNT;

       END LOOP;

      IF(r_outcome<>l_affectedRows)THEN
          r_outcome:=-2;
      END IF;

    END IF;

  EXCEPTION
    WHEN OTHERS THEN r_outcome:=-1;
  END;


END moveStudyNearline;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."MOVESTUDYOFFLINE" (
  p_studyInstanceUID VARCHAR2,
  r_outcome OUT NUMBER  -- 0: study with no Images, -1: general exception, >1: number of images moved, -2: discrepancy in number of images inserted/deleted, -3: study not found
)
AS
  l_affectedRows NUMBER(11);

BEGIN

  r_outcome:=0;

  BEGIN
    UPDATE Studies SET studyStatus='p' WHERE studyInstanceUID=p_studyInstanceUID AND studyStatus='n';
    r_outcome:=SQL%ROWCOUNT;

    IF(r_outcome=0)THEN
      r_outcome:=-3;
    END IF;

    IF(r_outcome=1)THEN

      INSERT INTO ImagesOffline(sopInstanceUID,sopClassUID,instanceNumber,seriesFK,deprecated,stgCommitted,samplesPerPixel,rowsNum,columnsNum,bitsAllocated,bitsStored,highBit,pixelRepresentation)
      Select I.sopInstanceUID,I.sopClassUID,I.instanceNumber,I.seriesFK,I.deprecated,I.stgCommitted,I.samplesPerPixel,I.rowsNum,I.columnsNum,I.bitsAllocated,I.bitsStored,I.highBit,I.pixelRepresentation
      from Series Se
      INNER JOIN ImagesNearline I ON Se.seriesInstanceUID=I.seriesFK
      where Se.studyFK=p_studyInstanceUID;
      l_affectedRows:=SQL%ROWCOUNT;

      DELETE FROM ImagesNearline WHERE sopInstanceUID IN(Select I.sopInstanceUID
      from Series Se
      INNER JOIN ImagesNearline I ON Se.seriesInstanceUID=I.seriesFK
      where Se.studyFK=p_studyInstanceUID);
      r_outcome:=SQL%ROWCOUNT;

      IF(r_outcome<>l_affectedRows)THEN
        r_outcome:=-2;
      END IF;

    END IF;

  EXCEPTION
    WHEN OTHERS THEN r_outcome:=-1;
  END;

END moveStudyOffline;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."MOVESTUDYTOPATIENT" (
  p_studyUid VARCHAR2,
  p_oldPatPk NUMBER,
  p_newPatPk NUMBER,
  p_oldPatDemoPk NUMBER,
  p_newPatDemoPk NUMBER
)
AS
  l_formerPatId VARCHAR2(64 CHAR);
BEGIN

  BEGIN
    SELECT patientID INTO l_formerPatId FROM Patients WHERE pk=p_oldPatPk;
  EXCEPTION WHEN NO_DATA_FOUND THEN
    l_formerPatId:=NULL;
  END;

  IF(l_formerPatId IS NOT NULL) THEN
    UPDATE Studies SET patientFK=p_newPatPk, toReconcile=0, mergedByPatientId=l_formerPatId WHERE studyInstanceUID=p_studyUid;
  ELSE
    UPDATE Studies SET patientFK=p_newPatPk, toReconcile=0 WHERE studyInstanceUID=p_studyUid;
  END IF;

  UPDATE WLPatientDataPerVisit SET patientFK=p_newPatPk WHERE studyFK=p_studyUid;

  UPDATE PatientDemographics SET numberOfPatientRelatedStudies=numberOfPatientRelatedStudies-1 WHERE pk=p_oldPatDemoPk;

  UPDATE PatientDemographics SET numberOfPatientRelatedStudies=numberOfPatientRelatedStudies+1 WHERE pk=p_newPatDemoPk;

END moveStudyToPatient;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."MOVEVISIT" (
  p_oldPatientPk NUMBER,
  p_newPatientPk NUMBER,
  p_oldVisitNumber VARCHAR2,
  p_newVisitNumber VARCHAR2,
  p_howMany OUT NUMBER
)
AS
BEGIN

  SELECT COUNT(S.studyInstanceUID) INTO p_howMany FROM Studies S
  INNER JOIN WLPatientDataPerVisit W ON W.studyFK=S.studyInstanceUID AND W.patientFK=S.patientFK
  WHERE W.visitNumber=p_oldVisitNumber AND W.patientFK=p_oldPatientPk;

  UPDATE Studies SET patientFK=p_newPatientPk, toReconcile=0 WHERE studyInstanceUID IN(
    SELECT studyInstanceUID FROM Studies S
    INNER JOIN WLPatientDataPerVisit W ON W.studyFK=S.studyInstanceUID AND W.patientFK=S.patientFK
    WHERE W.visitNumber=p_oldVisitNumber AND W.patientFK=p_oldPatientPk
  );

  UPDATE WLPatientDataPerVisit SET patientFK= p_newPatientPk, visitNumber = CASE WHEN p_newVisitNumber IS NULL THEN visitNumber ELSE p_newVisitNumber END
  WHERE visitNumber=p_oldVisitNumber AND patientFK=p_oldPatientPk;

  UPDATE PatientDemographics SET numberOfPatientRelatedStudies=numberOfPatientRelatedStudies-p_howMany WHERE patientFK=p_oldPatientPk;
  UPDATE PatientDemographics SET numberOfPatientRelatedStudies=numberOfPatientRelatedStudies+p_howMany WHERE patientFK=p_newPatientPk;

END moveVisit;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."QUERYINSTANCELEVEL" (
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
    END CASE;

    l_query:=l_query||l_commonJoin||l_commonWhere||' ORDER BY I.instanceNumber ASC, I.sopInstanceUID ASC';
    -- dbms_output.put_line(l_query); 	-- REMEMBER TO USE 			SET SERVEROUTPUT ON;
    OPEN resultSet FOR l_query;

END queryInstanceLevel;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."RECONCILESTUDY" (
  p_studyInstanceUid varchar2,
  p_accessionNumber varchar2,
  p_patientId varchar2,
  p_idIssuer varchar2,
  p_publishAvailability NUMBER,
  p_recSource VARCHAR2,
  p_stringToSet VARCHAR2,
  p_targetApp VARCHAR2
)
AS
  l_patPk NUMBER(19);
  l_studyCount INT;
  l_completed NUMBER(1);
  l_published NUMBER(1);
BEGIN

  BEGIN
    SELECT pk INTO l_patPk FROM Patients WHERE patientID=p_patientId AND idIssuer=p_idIssuer;
  EXCEPTION WHEN OTHERS THEN
    RETURN;
  END;

  UPDATE Studies SET toReconcile=0 WHERE studyInstanceUID=p_studyInstanceUid AND patientFK=l_patPk AND accessionNumber=p_accessionNumber;

  IF(p_publishAvailability=1)THEN
    SELECT COUNT(studyInstanceUID) INTO l_studyCount FROM Studies WHERE studyInstanceUID=p_studyInstanceUid AND patientFK=l_patPk AND accessionNumber=p_accessionNumber;

    IF(l_studyCount=1)THEN
      BEGIN
        SELECT completed, published INTO l_completed, l_published FROM StudyAvailability WHERE studyInstanceUid=p_studyInstanceUid;
      EXCEPTION WHEN OTHERS THEN
        RETURN;
      END;

      IF(l_completed=1 AND l_published=0)THEN
        INSERT INTO ImageEvents(patientId, accessionNumber, studyInstanceUid, event, status, source, targetApp)
        VALUES(p_patientId, p_accessionNumber, p_studyInstanceUid, p_stringToSet, 0, p_recSource, p_targetApp);

        UPDATE StudyAvailability SET published=1 WHERE studyInstanceUid=p_studyInstanceUid;
      END IF;


    END IF;
  END IF;

END reconcileStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."RECOVERSERIES" (
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


  UPDATE Series SET deprecated=0, seriesInstanceUID=r_seriesInstanceUID WHERE seriesInstanceUID=p_currentUid;

  SELECT numberOfSeriesRelatedInstances, studyFK INTO l_numSeriesInstances, l_studyInstanceUID FROM Series WHERE seriesInstanceUID=r_seriesInstanceUID;

  UPDATE Studies SET studySize=studySize+p_seriesSize, numberOfStudyRelatedSeries=numberOfStudyRelatedSeries+1, numberOfStudyRelatedInstances=numberOfStudyRelatedInstances+l_numSeriesInstances
  WHERE studyInstanceUID=l_studyInstanceUID;

  DELETE FROM DeprecationEvents WHERE parentDeprecationFK=p_pk;

  UPDATE DeprecationEvents SET recoveredBy=p_userFk, recoveredOn=SYS_EXTRACT_UTC(current_timestamp) WHERE pk=p_pk;

  SELECT p_pk INTO r_result FROM Dual;

END recoverSeries;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."RECOVERSTUDY" (
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

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."REMOVEKOSRELATIONSHIP" 
 (
 	p_OBJECTUID IN VARCHAR2
 )
 AS
 BEGIN
	DELETE FROM KosRelationships
	WHERE objectUID = p_OBJECTUID;
 END removeKosRelationship;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."RESETSTUDIESTORECONCILE" (
    p_studyInstanceUid VARCHAR2,
    p_patientPk NUMBER,
    p_affected OUT NUMBER
)
AS
BEGIN
  IF(p_studyInstanceUid IS NULL AND p_patientPk IS NULL) THEN

    p_affected:=0;

  ELSE

    UPDATE Studies SET toReconcile=0
    WHERE (patientFK=p_patientPk OR p_patientPk IS NULL) AND (studyInstanceUID=p_studyInstanceUid OR p_studyInstanceUid IS NULL);

    p_affected := SQL%ROWCOUNT;

  END IF;

END resetStudiesToReconcile;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."RESTOREOBJECT" (
  l_obj VARCHAR2,
  l_objType VARCHAR2
)
AS
  l_string1 VARCHAR2(4000 CHAR);
  l_string2 VARCHAR2(4000 CHAR);
  l_string3 VARCHAR2(4000 CHAR);
  l_string4 VARCHAR2(4000 CHAR);
  l_string5 VARCHAR2(4000 CHAR);
  l_string6 VARCHAR2(4000 CHAR);
  l_string7 VARCHAR2(4000 CHAR);
  l_string8 VARCHAR2(4000 CHAR);

  l_object VARCHAR2(64 CHAR);
  l_objectType VARCHAR2(16 CHAR);
  l_conf CLOB;
BEGIN

  -- GRANT QUERY REWRITE to Ultimate;    The user who runs the rollback must be granted this privilege to recreate objects

  l_object:=UPPER(l_obj);
  l_objectType:=UPPER(l_objType);

  CASE
  WHEN l_objectType='PROCEDURE' THEN
    BEGIN
      BEGIN
        for x in ( select code from UNDO where key=l_object AND type=l_objectType)
        loop
          l_string1:=DBMS_LOB.SUBSTR(x.code, 4000, 1);
          l_string2:=DBMS_LOB.SUBSTR(x.code, 4000, 4001);
          l_string3:=DBMS_LOB.SUBSTR(x.code, 4000, 8001);
          l_string4:=DBMS_LOB.SUBSTR(x.code, 4000, 12001);
          l_string5:=DBMS_LOB.SUBSTR(x.code, 4000, 16001);
          l_string6:=DBMS_LOB.SUBSTR(x.code, 4000, 20001);
          l_string7:=DBMS_LOB.SUBSTR(x.code, 4000, 24001);
          l_string8:=DBMS_LOB.SUBSTR(x.code, 4000, 28001);
        END LOOP;
        -- SET SERVEROUTPUT ON;  is needed before calling the procedure
        -- dbms_output.put_line('TO RESTORE:'||l_string1||l_string2||l_string3||l_string4||l_string5||l_string6||l_string7||l_string8);
        EXECUTE IMMEDIATE l_string1||l_string2||l_string3||l_string4||l_string5||l_string6||l_string7||l_string8;
      EXCEPTION
        WHEN OTHERS THEN
            dbms_output.put_line('AN EXCEPTION OCCURRED RESTORING '||l_objectType||' '||l_object||' '||DBMS_UTILITY.FORMAT_ERROR_STACK);
      END;
    END;
  WHEN l_objectType='SERVICE' THEN
    BEGIN
      BEGIN
        SELECT code INTO l_conf FROM Undo WHERE key=l_obj AND type=l_objectType;
        UPDATE ServicesConfiguration SET configuration = l_conf WHERE serviceName=l_obj;
      EXCEPTION
        WHEN OTHERS THEN
            dbms_output.put_line('AN EXCEPTION OCCURRED RESTORING '||l_objectType||' '||l_obj||' '||DBMS_UTILITY.FORMAT_ERROR_STACK);
      END;
    END;
  WHEN l_objectType='GLOBALCONF' THEN
    BEGIN
      BEGIN
        SELECT code INTO l_conf FROM Undo WHERE key=l_obj AND type=l_objectType;
        UPDATE GlobalConfiguration SET paramValue = l_conf WHERE paramKey=l_obj;
      EXCEPTION
        WHEN OTHERS THEN
            dbms_output.put_line('AN EXCEPTION OCCURRED RESTORING '||l_objectType||' '||l_obj||' '||DBMS_UTILITY.FORMAT_ERROR_STACK);
      END;
    END;
  ELSE
      dbms_output.put_line('>>>>>>>>>>>>UNKNOWN TYPE: '||l_objectType);
  END CASE;

END restoreObject;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."RETRIEVEVISITPERSTUDY" (
  p_studyUid VARCHAR2,
  p_patientPk NUMBER,
  p_visitNumber VARCHAR2,
  p_visitPk OUT NUMBER
)
AS
BEGIN

  BEGIN
    SELECT pk INTO p_visitPk FROM WLPatientDataPerVisit
    WHERE studyFK= p_studyUid AND patientFK=p_patientPk AND (p_visitNumber IS NULL OR visitNumber=p_visitNumber);
  EXCEPTION WHEN NO_DATA_FOUND THEN
    p_visitPk:=NULL;
  END;

END retrieveVisitPerStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."ROLLBACKCONVERSIONDATE" (
  p_seriesUid VARCHAR2
)
AS
BEGIN

	UPDATE Series SET convertedToMf=NULL WHERE seriesInstanceUID=p_seriesUid AND convertedToMf IS NOT NULL;


END rollbackConversionDate;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."SELECTFORWARDEDSTUDIES" (
  p_lowerProcess NUMBER,
  resultset OUT Types.ResultSet
)
AS
BEGIN

  OPEN resultset FOR
  SELECT FS.pk, FS.studyFK, KN.aeTitle, KN.ip, KN.port, KN.forwardEndConfirmation, S.numberOfStudyRelatedInstances
  FROM ForwardSchedule FS
  INNER JOIN KnownNodes KN ON FS.targetNodeFK=KN.pk
  INNER JOIN Studies S ON S.studyInstanceUID=FS.studyFK
  INNER JOIN ServicesConfiguration SC ON SC.enabled=1
  WHERE FS.forwardedOnUtc IS NOT NULL AND S.numberOfStudyRelatedInstances <> FS.movedInstances AND scheduleProcessFK>=p_lowerProcess AND SC.serviceName='Forwarder'
  ORDER BY FS.insertedOnUtc ASC, FS.pk ASC;

END selectForwardedStudies;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."SELECTSTUDIESTOARCHIVE" (
  p_spfk NUMBER,
  resultset OUT Types.ResultSet
)
AS
BEGIN

  OPEN resultset FOR
  SELECT DISTINCT sfk FROM (
    SELECT FS.studyFK sfk FROM ForwardSchedule FS
    INNER JOIN ServicesConfiguration SC ON SC.enabled=1
    INNER JOIN Studies S ON S.studyInstanceUID=FS.studyFK
    WHERE FS.forwardedOnUtc IS NOT NULL AND S.studyStatus <> 'a' AND scheduleProcessFK=p_spfk  AND SC.serviceName='Forwarder'
    ORDER BY FS.insertedOnUtc ASC, FS.pk ASC) A;

END selectStudiesToArchive;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."SELECTSTUDIESTOFORWARD" (
  p_forwardTolerance INT,
  resultset OUT Types.ResultSet
)
AS
BEGIN

  OPEN resultset FOR
  SELECT FS.pk, FS.studyFK, KN.aeTitle, KN.ip, KN.port, KN.forwardEndConfirmation, S.numberOfStudyRelatedInstances
  FROM ForwardSchedule FS
  INNER JOIN KnownNodes KN ON FS.targetNodeFK=KN.pk
  INNER JOIN Studies S ON S.studyInstanceUID=FS.studyFK
  INNER JOIN ServicesConfiguration SC ON SC.enabled=1
  WHERE FS.forwardedOnUtc IS NULL AND CAST(FS.insertedOnUtc + (p_forwardTolerance/(24*3600)) AS TIMESTAMP) < SYS_EXTRACT_UTC(current_timestamp)  AND SC.serviceName='Forwarder'
  ORDER BY FS.insertedOnUtc ASC, FS.pk ASC;


END selectStudiesToForward;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."SETEXPIRATIONDATE" (
    p_id NUMBER,
    p_newExpirationDate DATE,
    p_updatedRows OUT NUMBER
)
AS
BEGIN
  UPDATE Users SET pwdExpirationDate =p_newExpirationDate WHERE pk =p_id AND pwdExpirationDate IS NULL;
  p_updatedRows :=SQL%ROWCOUNT;
END setExpirationDate;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."SETLOG" (
    p_logLevel VARCHAR2,
    p_class    VARCHAR2,
    p_message  VARCHAR2,
    p_logger varchar2 := null,
    p_method  varchar2 := null)
IS
 PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
  INSERT
  INTO logs
    (
      log_level,
      class,
      message,
      logger,
      method
    )
    VALUES
    (
      p_logLevel,
      p_class,
      p_message,
      p_logger,
      p_method
    );
  COMMIT;
END SETLOG;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."STOPPROCESSINGINSTANCE" (
  p_instanceUid VARCHAR2,
  r_outcome OUT NUMBER
)
AS
BEGIN

  DELETE FROM InstancesInProgress WHERE instanceUid=p_instanceUid;

  r_outcome:=SQL%ROWCOUNT;

END stopProcessingInstance;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."STOREINSTANCELEVELMETADATA" (
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
    ELSE
      INSERT INTO NonImages(sopInstanceUID, sopClassUID, instanceNumber, seriesFK) VALUES(p_sopInstanceUid, p_sopClassUid, p_instanceNumber, p_seriesInstanceUid);
  END CASE;

  INSERT INTO HashTable(sopInstanceUID,hash) VALUES(p_sopInstanceUid,'METADATAONLY');

  UPDATE Studies SET numberOfStudyRelatedInstances=numberOfStudyRelatedInstances+1 WHERE studyInstanceUID=p_studyInstanceUid;
  UPDATE Series SET numberOfSeriesRelatedInstances=numberOfSeriesRelatedInstances+1 WHERE seriesInstanceUID=p_seriesInstanceUid;
  r_outcome:=1;

END storeInstanceLevelMetadata;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."STORESERIESLEVELMETADATA" (
  p_studyInstanceUid VARCHAR2,
  p_seriesInstanceUid VARCHAR2,
  p_modality VARCHAR2,
  p_seriesNumber NUMBER,
  p_seriesDescription VARCHAR2,
  r_outcome OUT NUMBER
)
AS
BEGIN

  r_outcome:=0;

  INSERT INTO Series(seriesInstanceUID, studyFK, modality, seriesNumber, seriesDescription)
  VALUES(p_seriesInstanceUid, p_studyInstanceUid, p_modality, p_seriesNumber, p_seriesDescription);

  UPDATE Studies SET numberOfStudyRelatedSeries=numberOfStudyRelatedSeries+1 WHERE studyInstanceUID=p_studyInstanceUid;
  r_outcome:=1;

END storeSeriesLevelMetadata;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."STORESTUDYLEVELMETADATA" (
  p_lastName VARCHAR2,
  p_firstName VARCHAR2,
  p_middleName VARCHAR2,
  p_prefix VARCHAR2,
  p_suffix VARCHAR2,
  p_dateOfBirth DATE,
  p_sex CHAR,
  p_patientId VARCHAR2,
  p_idIssuer VARCHAR2,
  p_studyInstanceUid VARCHAR2,
  p_studyId VARCHAR2,
  p_studyDate DATE,
  p_accessionNumber VARCHAR2,
  p_studyDescription VARCHAR2,
  p_knownNodePk NUMBER,
  r_outcome OUT NUMBER
)
AS
  l_patientPk NUMBER(19);
  l_presentLastName VARCHAR2(64 CHAR);
  l_procedureFK NUMBER(19);

BEGIN

  r_outcome:=0;

  BEGIN
    SELECT pk, lastName INTO l_patientPk, l_presentLastName
    FROM Patients
    WHERE patientID=p_patientId AND idIssuer=p_idIssuer;

    Select MIN(pk) INTO l_procedureFK FROM CodeSequences;

  EXCEPTION
  WHEN TOO_MANY_ROWS THEN
  	l_patientPk:=NULL;
    RETURN;
  WHEN NO_DATA_FOUND THEN
    l_patientPk:=NULL;
  END;

  IF(l_patientPk IS NOT NULL) THEN  -- Someone has been found, check his name

    IF((l_presentLastName NOT LIKE p_lastName||'%') AND (p_lastName NOT LIKE l_presentLastName||'%'))THEN
      r_outcome:=-1;
      RETURN;
    END IF;

  ELSE    -- Insert the new patient

    INSERT INTO Patients(lastName, firstName, middleName, prefix, suffix, birthDate, sex, patientID, idIssuer)
    VALUES(p_lastName, p_firstName, p_middleName, p_prefix, p_suffix, p_dateOfBirth, p_sex, p_patientId, p_idIssuer)
    RETURNING pk INTO l_patientPk;

    INSERT INTO PatientDemographics(patientIdentifierList, patientFK)
    VALUES(p_patientId, l_patientPk);

  END IF;

  -- Add study and visit

  INSERT INTO Studies(studyInstanceUID, studyID, studyDate, accessionNumber, studyDescription, procedureCodeSequenceFK, studyStatus, studySize, fastestAccess, patientFK, lastStatusChangeDate)
  VALUES(p_studyInstanceUid, p_studyId, p_studyDate, p_accessionNumber, p_studyDescription, l_procedureFK, 'o', 1, p_knownNodePk, l_patientPk, SYSTIMESTAMP);

  UPDATE PatientDemographics SET numberOfPatientRelatedStudies=numberOfPatientRelatedStudies+1 WHERE patientFK=l_patientPk;

  INSERT INTO WLPatientDataPerVisit(patientFK, studyFK)
  VALUES(l_patientPk, p_studyInstanceUid);

  r_outcome:=1;

END storeStudyLevelMetadata;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."UPDATEAFTERRECO" 
(
  p_srcStudy IN VARCHAR2,
  p_dstStudy IN VARCHAR2,
  p_recSource VARCHAR2,
  p_stringToSet VARCHAR2,
  p_targetApp VARCHAR2
) AS
  l_accNum VARCHAR2(16 CHAR);
  l_patId VARCHAR2(64 CHAR);
  l_idIssuer VARCHAR2(64 CHAR);
  l_patPk NUMBER(19);
BEGIN
  update studies set(studyID,studyStatusID,studyDate,studyTime,studyCompletionDate,
    studyCompletionTime, studyVerifiedDate,studyVerifiedTime,studyDescription,
    referringPhysiciansName,admittingDiagnosesDescription,lastStatusChangeDate,
    specificCharSet,procedureCodeSequenceFK) =
    (SELECT studyID,studyStatusID,studyDate,studyTime,studyCompletionDate,studyCompletionTime,
      studyVerifiedDate,studyVerifiedTime,studyDescription,referringPhysiciansName,
      admittingDiagnosesDescription,lastStatusChangeDate,specificCharSet,procedureCodeSequenceFK
      FROM Studies WHERE studyInstanceUID = p_srcStudy)
  WHERE studyInstanceUID = p_dstStudy;

   update studies set(studyID,studyStatusID,studyDate,studyTime,studyCompletionDate,
    studyCompletionTime, studyVerifiedDate,studyVerifiedTime,studyDescription,
    referringPhysiciansName,admittingDiagnosesDescription,lastStatusChangeDate,
    specificCharSet) =
    (SELECT NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL from dual)
  WHERE studyInstanceUID = p_srcStudy;

  UPDATE wlPatientDataPerVisit
  SET
    (patientstate,patientClass,assignedPatientLocation,visitNumber,pregnancyStatus,
      medicalAlerts,patientWeight,confidentialityConstOnPatData,specialNeeds)
      = (SELECT patientstate,patientClass,assignedPatientLocation,visitNumber,pregnancyStatus,
                medicalAlerts,patientWeight,confidentialityConstOnPatData,specialNeeds
        FROM wlPatientDataPerVisit WHERE studyFK = p_srcStudy)
  WHERE studyFk = p_dstStudy;

  UPDATE wlPatientDataPerVisit
  SET
    (patientstate,patientClass,assignedPatientLocation,visitNumber,pregnancyStatus,
      medicalAlerts,patientWeight,confidentialityConstOnPatData,specialNeeds)
      = (SELECT null,null,null,null,null,
                null,null,null,null
        FROM dual)
  WHERE studyFk = p_srcStudy;

  UPDATE StudyAvailability set studyInstanceUID=p_dstStudy where studyInstanceUID=p_srcStudy;

  SELECT accessionNumber, patientFK INTO l_accNum,l_patPk FROM Studies WHERE studyInstanceUID=p_dstStudy;

  SELECT patientID, idIssuer INTO l_patId, l_idIssuer FROM Patients WHERE pk=l_patPk;

  reconcileStudy (p_dstStudy,l_accNum,l_patId,l_idIssuer,1,p_recSource,p_stringToSet,p_targetApp);

END updateAfterReco;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."UPDATEFORWARDEDSCHEDULE" (
  p_relatedInstances NUMBER,
  p_currentProcess NUMBER,
  p_opPK NUMBER
)
AS
BEGIN

  UPDATE ForwardSchedule
  SET movedInstances=p_relatedInstances, scheduleProcessFK=p_currentProcess, forwardedOnUtc = SYS_EXTRACT_UTC(current_timestamp)
  WHERE pk = p_opPK;

END updateForwardedSchedule;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."UPDATEFORWARDSCHEDULE" (
  p_studyUid VARCHAR2,
  p_aeTitle VARCHAR2
)
AS
BEGIN

  INSERT INTO ForwardSchedule(targetNodeFK, studyFK, insertedOnUtc)
  SELECT NFM.targetNodeFK, p_studyUid, SYS_EXTRACT_UTC(current_timestamp)
  FROM NodesForwardMapping NFM
  INNER JOIN KnownNodes KN ON KN.pk=NFM.sourceNodeFK
  INNER JOIN ServicesConfiguration SC ON SC.enabled = 1  AND serviceName='Forwarder'
  LEFT JOIN ForwardSchedule FS ON FS.studyFK = p_studyUid
  WHERE KN.aeTitle=p_aeTitle AND FS.studyFK IS NULL ORDER BY sortOrder ASC;


END updateForwardSchedule;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."UPDATEHL7MESSAGERETRIES" (
p_nodeFk number,
p_messageFk number,
p_retries NUMBER
) AS
l_present number default null;
BEGIN

select max(lastMessageFk) into l_present from Hl7Logs
WHERE nodeFk = p_nodeFk;

if(l_present is null OR l_present < p_messageFk) then
    DELETE FROM Hl7Logs
    WHERE nodeFk = p_nodeFk and lastMessageFk = l_present;
end if;

IF(p_retries IS NOT NULL) THEN
    MERGE INTO Hl7Logs hll USING DUAL ON (hll.lastMessageFk = p_messageFk AND hll.nodeFk = p_nodeFk)
    WHEN MATCHED THEN UPDATE SET retries = p_retries
    WHEN NOT MATCHED THEN INSERT VALUES (p_nodeFk, p_messageFk,systimestamp,null);
else
    MERGE INTO Hl7Logs hll USING DUAL ON (hll.lastMessageFk = p_messageFk AND hll.nodeFk = p_nodeFk)
    WHEN MATCHED THEN UPDATE SET retries = NULL
    WHEN NOT MATCHED THEN INSERT VALUES (p_nodeFk, p_messageFk,systimestamp,null) ;

END IF;
END updateHl7MessageRetries;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."UPDATEMOVESTUDY" (
  p_historyId NUMBER,
  p_isEnd NUMBER
)
AS
BEGIN
setlog( 'INFO', 'updateMoveStudy', 'p_historyId = '||p_historyId||' p_isEnd = '||p_isEnd, NULL, NULL);

  IF(p_isEnd=1) THEN
    UPDATE MoveStudyHistory SET endMov = SYSTIMESTAMP WHERE id=p_historyId;
  ELSE
    UPDATE MoveStudyHistory SET startMov = SYSTIMESTAMP WHERE id=p_historyId;
  END IF;


END updateMoveStudy;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."UPDATEPASSWORD" (
    p_id          NUMBER,
    p_newPassword VARCHAR2,
    p_newExpirationDate DATE,
    p_updatedRows OUT NUMBER )
AS
BEGIN
  UPDATE Users SET pwdExpirationDate=p_newExpirationDate, password =p_newPassword WHERE pk =p_id;
  p_updatedRows :=SQL%ROWCOUNT;
END updatePassword;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."UPDATEPATIENTINFORMATION" (
  p_pk NUMBER,
  p_lastName VARCHAR2,
  p_firstName VARCHAR2,
  p_middleName VARCHAR2,
  p_prefix VARCHAR2,
  p_suffix VARCHAR2,
  p_sex CHAR,
  p_birthDate DATE,
  p_birthTime DATE,
  p_race VARCHAR2,
  p_patientAccountNumber VARCHAR2,
  p_patientAddress VARCHAR2,
  p_patientCity VARCHAR2,
  p_updatedRows OUT NUMBER
)
AS
BEGIN
  UPDATE Patients SET lastName =
    CASE WHEN (p_lastName = '""') THEN NULL
      WHEN (p_lastName IS NOT NULL) THEN p_lastName
      ELSE lastName
    END,
    firstName =
    CASE WHEN (p_firstName = '""') THEN NULL
      WHEN (p_firstName IS NOT NULL) THEN p_firstName
      ELSE firstName
    END,
    middleName =
    CASE WHEN (p_middleName = '""') THEN NULL
      WHEN (p_middleName IS NOT NULL) THEN p_middleName
      ELSE middleName
    END,
    prefix =
    CASE WHEN (p_prefix = '""') THEN NULL
      WHEN (p_prefix IS NOT NULL) THEN p_prefix
      ELSE prefix
    END,
    suffix =
    CASE WHEN (p_suffix = '""') THEN NULL
      WHEN (p_suffix IS NOT NULL) THEN p_suffix
      ELSE suffix
    END,
    sex =
    CASE WHEN (p_sex = '""') THEN NULL
      WHEN (p_sex IS NOT NULL) THEN p_sex
      ELSE sex
    END,
    birthDate =
    CASE
      WHEN (p_birthDate IS NOT NULL) THEN p_birthDate
      ELSE birthDate
    END,
    birthTime =
    CASE
      WHEN (p_birthTime IS NOT NULL) THEN p_birthTime
      ELSE birthTime
    END
  WHERE pk=p_pk;
  p_updatedRows :=SQL%ROWCOUNT;
  UPDATE PatientDemographics SET race =
    CASE WHEN (p_race = '""') THEN NULL
      WHEN (p_race IS NOT NULL) THEN p_race
      ELSE race
    END,
    patientAccountNumber =
    CASE WHEN (p_patientAccountNumber = '""') THEN NULL
      WHEN (p_patientAccountNumber IS NOT NULL) THEN p_patientAccountNumber
      ELSE patientAccountNumber
    END,
    patientAddress =
    CASE WHEN (p_patientAddress = '""') THEN NULL
      WHEN (p_patientAddress IS NOT NULL) THEN p_patientAddress
      ELSE patientAddress
    END,
    patientCity =
    CASE WHEN (p_patientCity = '""') THEN NULL
      WHEN (p_patientCity IS NOT NULL) THEN p_patientCity
      ELSE patientCity
    END
    WHERE patientfk=p_pk;
    p_updatedRows :=(SQL%ROWCOUNT+p_updatedrows);
END updatePatientInformation;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."UPDATEPHYSICALMEDIASIZE" (
  p_studyUid VARCHAR2,
  p_phMediaId NUMBER,
  p_size OUT NUMBER
)
AS
BEGIN

  BEGIN
    SELECT studySize INTO p_size
    FROM Studies
    WHERE studyStatus = 'a' AND studyInstanceUID=p_studyUid;
  EXCEPTION WHEN NO_DATA_FOUND THEN
    p_size:=0;
  END;

  UPDATE PhysicalMedia
  SET filledBytes=filledBytes-p_size
  WHERE pk=p_phMediaId;

END updatePhysicalMediaSize;

CREATE OR REPLACE PROCEDURE JBOSSPACSDB."UPDATESCHEDULE" (
  p_relatedInstances NUMBER,
  p_currentProcess NUMBER,
  p_opPK NUMBER
)
AS
BEGIN

  UPDATE ForwardSchedule
  SET movedInstances=p_relatedInstances, scheduleProcessFK=p_currentProcess, forwardedOnUtc = SYS_EXTRACT_UTC(current_timestamp)
  WHERE pk = p_opPK AND forwardedOnUtc IS NULL;

END updateSchedule;
