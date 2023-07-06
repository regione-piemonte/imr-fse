
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
