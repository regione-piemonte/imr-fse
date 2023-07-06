
CREATE OR REPLACE PROCEDURE getDataForCMove(
  p_patientId IN VARCHAR2,
  p_studyUid IN VARCHAR2,
  p_seriesUid IN VARCHAR2,
  p_instanceUid IN VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
  l_query VARCHAR2(4000);
  l_queryEnd VARCHAR2(200);
BEGIN

  l_query:='SELECT  I.sopInstanceUID, I.sopClassUID,'||
           ' Se.seriesInstanceUID,'||
           ' St.studyInstanceUID,'||
           ' P.patientID, P.idIssuer, P.lastName, P.firstName, P.middleName, P.prefix, P.suffix, P.birthDate, P.sex, P.pk,'||
           ' I.instanceType, St.studyStatus,'||
           ' NULL, St.fastestAccess, NULL';

  l_queryEnd:=' AND I.deprecated=0 AND Se.deprecated=0'||
             ' ORDER BY St.studyInstanceUID ASC, Se.seriesInstanceUID, I.instanceNumber ASC, I.sopInstanceUID ASC';


  IF (p_instanceUid IS NOT NULL) THEN      -- INSTANCE LEVEL     SEVERAL InstanceUIDs, coming as 1.2.3   OR  1.2.3','9.8.7','56.786.445
    l_query:= l_query||' FROM ('||
                       ' Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM Images WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ' UNION ALL'||
                       ' Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM ImagesNearline WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ' UNION ALL'||
                       ' Select ''NI'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM NonImages WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ' UNION ALL'||
                       ' Select ''SR'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM StructReps WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ' UNION ALL'||
                       ' Select ''PS'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM PresStates WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ' UNION ALL'||
                       ' Select ''OV'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM Overlays WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ' UNION ALL'||
                       ' Select ''KO'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM KeyObjects WHERE sopInstanceUID IN ('''||p_instanceUid||''') AND deprecated=0'||
                       ') I'||
                       ' INNER JOIN Series Se ON Se.seriesInstanceUID=I.seriesFK'||
                       ' INNER JOIN Studies St ON St.studyInstanceUID=Se.studyFK'||
                       ' INNER JOIN Patients P ON P.pk=St.patientFK'||
--                       ' LEFT JOIN StoragePolicies SP ON SP.id= St.storagePolicyId'||
--                       ' LEFT JOIN NearlineStorageSettings NSS ON NSS.id=SP.nearlineStorageSettingsId'||
--                       ' LEFT JOIN StorageDeviceTypes SDT ON SDT.id=NSS.storageDeviceTypeId'||
--                       ' LEFT JOIN HashTable HT ON HT.sopInstanceUID=I.sopInstanceUID'||
                       ' WHERE St.studyStatus <> ''p'' AND I.sopInstanceUID IN ('''|| p_instanceUid||''')';
  ELSIF (p_seriesUid IS NOT NULL) THEN      -- SERIES LEVEL     SEVERAL SeriesUIDs, coming as 1.2.3   OR  1.2.3','9.8.7','56.786.445
    l_query:= l_query||' FROM Series Se'||
                       ' INNER JOIN ('||
                       '  Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM Images WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM ImagesNearline WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''NI'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM NonImages WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''SR'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM StructReps WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''PS'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM PresStates WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''OV'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM Overlays WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
					   '  UNION ALL'||
                       '  Select ''KO'', sopInstanceUID, sopClassUID, seriesFK, deprecated, instanceNumber FROM KeyObjects WHERE seriesFK IN ('''||p_seriesUid||''') AND deprecated=0'||
                       ')I ON I.seriesFK=Se.seriesInstanceUID'||
                       ' INNER JOIN Studies St ON St.studyInstanceUID=Se.studyFK'||
                       ' INNER JOIN Patients P ON P.pk=St.patientFK'||
--                       ' LEFT JOIN StoragePolicies SP ON SP.id= St.storagePolicyId'||
--                       ' LEFT JOIN NearlineStorageSettings NSS ON NSS.id=SP.nearlineStorageSettingsId'||
--                       ' LEFT JOIN StorageDeviceTypes SDT ON SDT.id=NSS.storageDeviceTypeId'||
--                       ' LEFT JOIN HashTable HT ON HT.sopInstanceUID=I.sopInstanceUID'||
                       ' WHERE St.studyStatus <> ''p'' AND Se.seriesInstanceUID IN ('''|| p_seriesUid||''')';
  ELSIF (p_studyUid IS NOT NULL) THEN      -- STUDY LEVEL     SEVERAL StudyUIDs, coming as 1.2.3   OR  1.2.3','9.8.7','56.786.445
    l_query:= l_query||' FROM Studies St'||
                       ' INNER JOIN Patients P ON P.pk=St.patientFK'||
                       ' INNER JOIN Series Se ON Se.studyFK=St.studyInstanceUID'||
                       ' INNER JOIN ('||
                       '  Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM Images U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM ImagesNearline U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''NI'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM NonImages U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''SR'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM StructReps U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''PS'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM PresStates U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''OV'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM Overlays U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''KO'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM KeyObjects U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '  WHERE Ser.studyFK IN ('''||p_studyUid||''') AND U.deprecated=0'||
                       ')I ON I.seriesFK=Se.seriesInstanceUID'||
--                       ' LEFT JOIN StoragePolicies SP ON SP.id= St.storagePolicyId'||
--                       ' LEFT JOIN NearlineStorageSettings NSS ON NSS.id=SP.nearlineStorageSettingsId'||
--                       ' LEFT JOIN StorageDeviceTypes SDT ON SDT.id=NSS.storageDeviceTypeId'||
--                       ' LEFT JOIN HashTable HT ON HT.sopInstanceUID=I.sopInstanceUID'||
                       ' WHERE St.studyStatus <> ''p'' AND St.studyinstanceUID IN('''|| p_studyUid||''')';
  ELSIF(p_patientId IS NOT NULL) THEN      -- PATIENT LEVEL      JUST ONE PatientID
    l_query:= l_query||' FROM Patients P'||
                       ' INNER JOIN Studies St ON  St.patientFK=P.pk'||
                       ' INNER JOIN Series Se ON Se.studyFK=St.studyInstanceUID'||
                       ' INNER JOIN ('||
                       '  Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM Images U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''I'' instanceType, sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM ImagesNearline U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''NI'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM NonImages U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''SR'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM StructReps U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''PS'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM PresStates U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''OV'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM Overlays U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       '  UNION ALL'||
                       '  Select ''KO'', sopInstanceUID, sopClassUID, seriesFK, U.deprecated, instanceNumber FROM KeyObjects U'||
                       '    INNER JOIN Series Ser ON Ser.seriesInstanceUID=U.seriesFK'||
                       '    INNER JOIN Studies Stu ON Stu.studyInstanceUID=Ser.studyFK'||
                       '    INNER JOIN Patients Pat ON Pat.pk=Stu.patientFK'||
                       '  WHERE Pat.patientID = '''||p_patientId||''' AND U.deprecated=0'||
                       ')I ON I.seriesFK=Se.seriesInstanceUID'||
--                       ' LEFT JOIN StoragePolicies SP ON SP.id= St.storagePolicyId'||
--                       ' LEFT JOIN NearlineStorageSettings NSS ON NSS.id=SP.nearlineStorageSettingsId'||
--                       ' LEFT JOIN StorageDeviceTypes SDT ON SDT.id=NSS.storageDeviceTypeId'||
--                       ' LEFT JOIN HashTable HT ON HT.sopInstanceUID=I.sopInstanceUID'||
                       ' WHERE St.studyStatus <> ''p'' AND P.patientID ='''|| p_patientId||'''';
  END IF;
  l_query:= l_query||l_queryEnd;
  OPEN resultSet FOR l_query;
END getDataForCMove;

------------------------------------------------------------------------------------------------------------------------
