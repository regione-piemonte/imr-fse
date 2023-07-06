INSERT INTO GLOBALCONFIGURATION
(PARAMKEY, PARAMVALUE, ENABLED)
VALUES('deleteEmptyPath', 'true', 1);


CREATE OR REPLACE PROCEDURE TESTO3.getStudiesToDelete(
    p_toleranceInHours NUMBER,
    p_studyToleranceInHours NUMBER,
    resultset OUT Types.ResultSet
) AS
BEGIN

  OPEN resultSet FOR
  SELECT St.studyInstanceUID, St.fastestAccess
  FROM DeprecationEvents de
  INNER JOIN Studies St ON de.currentUid=St.studyInstanceUID
  Where St.deprecated=1 AND St.lastStatusChangeDate + NUMTODSINTERVAL(p_studyToleranceInHours, 'HOUR') < systimestamp 
  AND de.recoveredOn IS NULL AND de.deprecatedOn + NUMTODSINTERVAL(p_toleranceInHours, 'HOUR') < systimestamp;
 -- jira O3DPACS-32 
 --SELECT St.studyInstanceUID, St.fastestAccess
  --FROM DeprecationEvents de
  --INNER JOIN Studies St ON de.currentUid=St.studyInstanceUID
  --Where St.deprecated=1 and de.eventType='DEL' AND de.recoveredOn IS NULL AND de.deprecatedOn + NUMTODSINTERVAL(p_toleranceInHours, 'HOUR') < systimestamp;

END getStudiesToDelete;


CREATE OR REPLACE PROCEDURE TESTO3.deleteStudy(
  p_studyUid VARCHAR2,
  r_studySize OUT NUMBER,
  r_physicalMediaPk OUT NUMBER,
  r_originalUid OUT VARCHAR2
)
AS
  l_pk NUMBER(19);
BEGIN

  SELECT pk, originalUid INTO l_pk, r_originalUid FROM DeprecationEvents WHERE currentUID=p_studyUid; -- AND eventType='DEL';  -- jira O3DPACS-32
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

  DELETE FROM Studies WHERE studyInstanceUID=p_studyUid;

END deleteStudy;