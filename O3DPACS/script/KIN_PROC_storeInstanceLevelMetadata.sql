
CREATE OR REPLACE PROCEDURE storeInstanceLevelMetadata(
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
    WHEN l_table='KeyObjects' THEN
      INSERT INTO KeyObjects(sopInstanceUID, sopClassUID, instanceNumber, seriesFK) VALUES(p_sopInstanceUid, p_sopClassUid, p_instanceNumber, p_seriesInstanceUid);
    ELSE
      INSERT INTO NonImages(sopInstanceUID, sopClassUID, instanceNumber, seriesFK) VALUES(p_sopInstanceUid, p_sopClassUid, p_instanceNumber, p_seriesInstanceUid);
  END CASE;

  INSERT INTO HashTable(sopInstanceUID,hash) VALUES(p_sopInstanceUid,'METADATAONLY');

  UPDATE Studies SET numberOfStudyRelatedInstances=numberOfStudyRelatedInstances+1 WHERE studyInstanceUID=p_studyInstanceUid;
  UPDATE Series SET numberOfSeriesRelatedInstances=numberOfSeriesRelatedInstances+1 WHERE seriesInstanceUID=p_seriesInstanceUid;
  r_outcome:=1;

END storeInstanceLevelMetadata;