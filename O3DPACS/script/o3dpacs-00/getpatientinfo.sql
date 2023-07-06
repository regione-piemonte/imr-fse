create or replace PROCEDURE getpatientinfo(
  p_patientId IN VARCHAR2,
  p_idIssuer IN VARCHAR2,
  p_accessionNumber IN VARCHAR2,
  p_studyUid IN VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN
  IF (p_patientId IS NOT NULL) THEN
    IF (p_idIssuer is not null) THEN
        OPEN resultset FOR
        SELECT PT.pk, PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
        FROM Patients PT
      WHERE PT.patientId = p_patientId and PT.idIssuer = p_idIssuer;
    ELSE
    OPEN resultset FOR
        SELECT PT.pk, PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
        FROM Patients PT
      WHERE PT.patientId = p_patientId;
    END IF;
  END IF;
  IF(p_studyUid IS NOT NULL) THEN
    OPEN resultset FOR
  	SELECT PT.pk, PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
    FROM Patients PT
  	INNER JOIN Studies ST ON ST.patientFK = PT.pk
    WHERE ST.studyInstanceUID = p_studyUid
      AND st.deprecated = 0;
  END IF;
  IF(p_accessionNumber IS NOT NULL) THEN
     IF (p_patientId is not null) THEN
		OPEN resultset FOR
	 	SELECT PT.pk, PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
		FROM Patients PT
		INNER JOIN Studies ST ON ST.patientFK = PT.pk
		WHERE st.accessionNumber = p_accessionNumber
          AND st.deprecated = 0
		AND PT.patientid = p_patientId;
     ELSE
		OPEN resultset FOR
		SELECT PT.pk, PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
		FROM Patients PT
		INNER JOIN Studies ST ON ST.patientFK = PT.pk
		WHERE st.accessionNumber = p_accessionNumber
          AND st.deprecated = 0;
	END IF;
  END IF;
END getPatientInfo;
