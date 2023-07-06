
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
