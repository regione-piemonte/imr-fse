CREATE OR REPLACE TRIGGER "SERIESALTER" AFTER
  UPDATE OF SERIESINSTANCEUID ON SERIES REFERENCING NEW AS NEWROW OLD AS OLDROW FOR EACH ROW BEGIN
  UPDATE IMAGES SET SERIESFK =:newRow.SERIESINSTANCEUID WHERE SERIESFK=:oldRow.SERIESINSTANCEUID;
  UPDATE IMAGESNEARLINE SET SERIESFK =:newRow.SERIESINSTANCEUID WHERE SERIESFK=:oldRow.SERIESINSTANCEUID;
  UPDATE IMAGESOFFLINE SET SERIESFK =:newRow.SERIESINSTANCEUID WHERE SERIESFK=:oldRow.SERIESINSTANCEUID;
  UPDATE NONIMAGES SET SERIESFK =:newRow.SERIESINSTANCEUID WHERE SERIESFK=:oldRow.SERIESINSTANCEUID;
  UPDATE OVERLAYS SET SERIESFK =:newRow.SERIESINSTANCEUID WHERE SERIESFK=:oldRow.SERIESINSTANCEUID;
  UPDATE PRESSTATES SET SERIESFK =:newRow.SERIESINSTANCEUID WHERE SERIESFK=:oldRow.SERIESINSTANCEUID;
  UPDATE STRUCTREPS SET SERIESFK =:newRow.SERIESINSTANCEUID WHERE SERIESFK=:oldRow.SERIESINSTANCEUID;
  UPDATE KEYOBJECTS SET SERIESFK =:newRow.SERIESINSTANCEUID WHERE SERIESFK=:oldRow.SERIESINSTANCEUID;
END SERIESALTER;


CREATE OR REPLACE PROCEDURE JBOSSPACSDB.deleteStudy(
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
  EXCEPTION WHEN OTHERS THEN
    r_physicalMediaPk:=0;
  END;


END deleteStudy;