
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
