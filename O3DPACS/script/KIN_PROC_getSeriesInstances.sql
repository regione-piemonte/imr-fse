
CREATE OR REPLACE PROCEDURE getSeriesInstances(
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
      when exists (select 8 from KeyObjects where seriesFK = p_seriesInstanceUid and rownum=1) then 'KeyObjects'
      else 'PresStates'
    end INTO l_instanceTable
    from dual;

  OPEN resultset FOR
        'SELECT T.sopInstanceUID, T.sopClassUID, T.instanceNumber, MT.name FROM '||l_instanceTable||' T
        INNER JOIN SupportedSOPClasses SOP ON SOP.sopClassUid=T.sopClassUid
        INNER JOIN MimeTypes MT ON MT.id=SOP.mimeType
        WHERE T.seriesFK='''||p_seriesInstanceUid||'''';

END getSeriesInstances;
