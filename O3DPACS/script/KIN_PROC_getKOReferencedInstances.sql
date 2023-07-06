
-- CREATE PL/SQL ------------------------------------------------------------------------------------------------------- 
------------------------------------------------------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE getKOReferencedInstances(
  p_sopInstanceUid VARCHAR2,
  p_studyUID VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
  l_instanceTable VARCHAR2(24 CHAR);
BEGIN

    select case
      when exists (select 1 from Images i INNER JOIN KEYOBJECTREFERENCES k ON i.SOPINSTANCEUID = k.REFSOPINSTANCEUID 
      				where k.KEYOBJECTSFK = p_sopInstanceUid and rownum=1) then 'Images'
      when exists (select 5 from NonImages ni INNER JOIN KEYOBJECTREFERENCES k ON ni.SOPINSTANCEUID = k.REFSOPINSTANCEUID 
      				where k.KEYOBJECTSFK = p_sopInstanceUid and rownum=1) then 'NonImages'
      when exists (select 3 from StructReps sr INNER JOIN KEYOBJECTREFERENCES k ON sr.SOPINSTANCEUID = k.REFSOPINSTANCEUID 
      				where k.KEYOBJECTSFK = p_sopInstanceUid and rownum=1) then 'StructReps'
      when exists (select 4 from Overlays o INNER JOIN KEYOBJECTREFERENCES k ON o.SOPINSTANCEUID = k.REFSOPINSTANCEUID 
      				where k.KEYOBJECTSFK = p_sopInstanceUid and rownum=1) then 'Overlays'
      when exists (select 6 from ImagesNearline ine INNER JOIN KEYOBJECTREFERENCES k ON ine.SOPINSTANCEUID = k.REFSOPINSTANCEUID 
      				where k.KEYOBJECTSFK = p_sopInstanceUid and rownum=1) then 'ImagesNearline'
      when exists (select 7 from ImagesOffline io INNER JOIN KEYOBJECTREFERENCES k ON io.SOPINSTANCEUID = k.REFSOPINSTANCEUID 
      				where k.KEYOBJECTSFK = p_sopInstanceUid and rownum=1) then 'ImagesOffline'
      else 'PresStates'
    end INTO l_instanceTable
    from dual;

  OPEN resultset FOR
        'SELECT T.sopInstanceUID, T.sopClassUID, T.instanceNumber, T.seriesFK, MT.name FROM '||l_instanceTable||' T
        INNER JOIN SupportedSOPClasses SOP ON SOP.sopClassUid=T.sopClassUid
        INNER JOIN MimeTypes MT ON MT.id=SOP.mimeType
        INNER JOIN KeyObjectReferences K ON K.REFSOPINSTANCEUID = T.sopInstanceUID
		INNER JOIN Series S ON S.SERIESINSTANCEUID = K.REFSERIESINSTANCEUID
        WHERE K.KEYOBJECTSFK='''||p_sopInstanceUid||''' 
		AND S.STUDYFK ='''||p_studyUID||'''';

END getKOReferencedInstances;
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
