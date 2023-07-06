
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
