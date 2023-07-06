
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
