
CREATE OR REPLACE PROCEDURE getRetrievalInfo(
  p_studyUid IN VARCHAR2,
  p_seriesUid IN VARCHAR2,
  p_instanceUid IN VARCHAR2,

  r_patientName OUT VARCHAR2,
  r_patientId OUT VARCHAR2,
  r_idIssuer OUT VARCHAR2,
  r_birthDate OUT DATE,
  r_sex OUT VARCHAR2,

  r_studyId OUT VARCHAR2,
  r_studyDate OUT DATE,
  r_studyTime OUT DATE,
  r_studyCompletionDate OUT DATE,
  r_studyCompletionTime OUT DATE,
  r_studyVerifiedDate OUT DATE,
  r_studyVerifiedTime OUT DATE,
  r_accessionNumber OUT VARCHAR2,

  resultSet OUT Types.ResultSet
)
AS
  l_query VARCHAR2(4000);
  l_query_instanceBranchOne VARCHAR2(4000);
  l_query_instanceBranchMany VARCHAR2(4000);
  l_queryMiddle VARCHAR2(4000);
  l_queryEnd VARCHAR2(200);
  l_studyStatus CHAR(1);
BEGIN


  BEGIN
    SELECT (P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix) ,P.patientID, P.idIssuer, P.birthDate, P.sex,
          St.studyID, St.studyDate, St.studyTime, St.studyCompletionDate, St.studyCompletionTime, St.studyVerifiedDate, St.studyVerifiedTime, St.accessionNumber, St.studyStatus
      INTO r_patientName, r_patientId, r_idIssuer, r_birthDate, r_sex,
           r_studyId, r_studyDate, r_studyTime, r_studyCompletionDate, r_studyCompletionTime, r_studyVerifiedDate, r_studyVerifiedTime, r_accessionNumber, l_studyStatus
    FROM Patients P
    INNER JOIN Studies St ON St.patientFK = P.pk
    WHERE St.studyInstanceUID = p_studyUid AND St.studyStatus<>'p';
  EXCEPTION WHEN NO_DATA_FOUND THEN
    RETURN; -- No ResultSet is returned when the study is not present or offline
  END;



  -- l_query:='SELECT St.studyInstanceUID, St.patientFK, St.studyStatus, Se.seriesInstanceUID, I.sopInstanceUID, SDT.name, St.fastestAccess, HT.tokenToFile'||
  l_query:='SELECT St.studyInstanceUID, St.patientFK, St.studyStatus, Se.seriesInstanceUID, I.sopInstanceUID, NULL, St.fastestAccess, NULL'||
           ' FROM Studies St'||
           ' INNER JOIN Series Se ON Se.studyFK = St.studyInstanceUID'||
           ' INNER JOIN ';

  l_query_instanceBranchOne:='('||
                             ' SELECT sopInstanceUID, seriesFK FROM Images'||CASE WHEN(l_studyStatus ='n')THEN 'Nearline' ELSE '' END||' WHERE seriesFK='''||p_seriesUid||''''||
                             ' UNION ALL'||
                             ' SELECT sopInstanceUID, seriesFK FROM NonImages WHERE seriesFK='''||p_seriesUid||''''||
                             ' UNION ALL'||
                             ' SELECT sopInstanceUID, seriesFK FROM StructReps WHERE seriesFK='''||p_seriesUid||''''||
                             ' UNION ALL'||
                             ' SELECT sopInstanceUID, seriesFK FROM PresStates WHERE seriesFK='''||p_seriesUid||''''||
                             ' UNION ALL'||
                             ' SELECT sopInstanceUID, seriesFK FROM Overlays WHERE seriesFK='''||p_seriesUid||''''||
                             ' UNION ALL'||
                             ' SELECT sopInstanceUID, seriesFK FROM KeyObjects WHERE seriesFK='''||p_seriesUid||''''||
                             ')';

  l_query_instanceBranchMany:='('||
                                 '  Select sopInstanceUID, seriesFK, instanceNumber FROM Images'||CASE WHEN(l_studyStatus ='n')THEN 'Nearline' ELSE '' END||' U'||
                                 '  INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                                 '  WHERE Ser.studyFK ='''||p_studyUid||''''||
                                 '  UNION ALL'||
                                 '  Select sopInstanceUID, seriesFK, instanceNumber FROM NonImages U'||
                                 '  INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                                 '  WHERE Ser.studyFK ='''||p_studyUid||''''||
                                 '  UNION ALL'||
                                 '  Select sopInstanceUID, seriesFK, instanceNumber FROM StructReps U'||
                                 '  INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                                 '  WHERE Ser.studyFK ='''||p_studyUid||''''||
                                 '  UNION ALL'||
                                 '  Select sopInstanceUID, seriesFK, instanceNumber FROM PresStates U'||
                                 '  INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                                 '  WHERE Ser.studyFK ='''||p_studyUid||''''||
                                 '  UNION ALL'||
                                 '  Select sopInstanceUID, seriesFK, instanceNumber FROM Overlays U'||
                                 '  INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                                 '  WHERE Ser.studyFK ='''||p_studyUid||''''||
                                 '  UNION ALL'||
                                 '  Select sopInstanceUID, seriesFK, instanceNumber FROM KeyObjects U'||
                                 '  INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                                 '  WHERE Ser.studyFK ='''||p_studyUid||''''||
                                 ')';

  -- l_queryMiddle:=' I ON I.seriesFK = Se.seriesInstanceUID'||
  --               ' LEFT JOIN StoragePolicies SP ON SP.id= St.storagePolicyId'||
  --               ' LEFT JOIN NearlineStorageSettings NSS ON NSS.id=SP.nearlineStorageSettingsId'||
  --               ' LEFT JOIN StorageDeviceTypes SDT ON SDT.id=NSS.storageDeviceTypeId'||
  --               ' LEFT JOIN HashTable HT ON HT.sopInstanceUID=I.sopInstanceUID'||
  --               ' WHERE St.studyInstanceUID='''||p_studyUid||'''';

  l_queryMiddle:=' I ON I.seriesFK = Se.seriesInstanceUID'||
                 ' WHERE St.studyInstanceUID='''||p_studyUid||'''';

  l_queryEnd:=' AND Se.seriesInstanceUID='''||p_seriesUid||''''||
              ' AND I.sopInstanceUID='''||p_instanceUid||'''';


  IF (p_instanceUid IS NOT NULL AND p_seriesUid IS NOT NULL) THEN     -- A definite instance was requested
    l_query:= l_query||l_query_instanceBranchOne||l_queryMiddle||l_queryEnd;
  ELSE
   l_query:= l_query||l_query_instanceBranchMany||l_queryMiddle||' ORDER BY St.studyInstanceUID ASC, Se.seriesInstanceUID, I.instanceNumber ASC, I.sopInstanceUID ASC';
  END IF;

  OPEN resultSet FOR l_query;

END getRetrievalInfo;
