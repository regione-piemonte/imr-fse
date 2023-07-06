--UPDATE TO O3-DPACS VERSION 2.0.0

--PROMPT ALTER TABLE MOVESTUDYHISTORY
ALTER TABLE MOVESTUDYHISTORY ADD
patientID   VARCHAR2(64 CHAR);
/

--PROMPT CREATE OR REPLACE PROCEDURE getparamstoretrieveaccnum
CREATE OR REPLACE PROCEDURE getparamstoretrieveaccnum (
  p_id NUMBER,
  r_calledAet OUT VARCHAR2,
  r_callingAet OUT VARCHAR2,
  r_moveAet OUT VARCHAR2,
  r_accNUm OUT VARCHAR2,
  r_ip OUT VARCHAR2,
  r_port OUT NUMBER,
  r_knownNode OUT NUMBER,
  r_patientID OUT VARCHAR2
)
AS
BEGIN
  BEGIN
--setlog( 'INFO', 'getParamsToRetrieveAccNum', 'IN: p_id = '||p_id, NULL, NULL);

    SELECT msh.calledAet, msh.callingAet, msh.moveAet, msh.accessionNumber, kn.ip, kn.port, kn.pk, msh.patientID
           INTO r_calledAet, r_callingAet, r_moveAet, r_accNUm, r_ip, r_port, r_knownNode, r_patientID
    FROM MoveStudyHistory msh, KnownNodes kn
    WHERE msh.id=p_id AND kn.pk = msh.knownNodeFk;

  EXCEPTION WHEN NO_DATA_FOUND THEN

    SELECT NULL, NULL, NULL, NULL, NULL, NULL
    INTO r_calledAet, r_callingAet, r_moveAet, r_accNUm, r_ip, r_port
    FROM DUAL;

  END;
--setlog( 'INFO', 'getParamsToRetrieveAccNum', 'OUT: r_accNUm = '||r_accNUm|| ' r_calledAet = '||r_calledAet|| ' r_callingAet = '||r_callingAet|| ' r_moveAet = '||r_moveAet, NULL, NULL);
END getParamsToRetrieveAccNum;
/

--PROMPT CREATE OR REPLACE PROCEDURE getpatientinfo
CREATE OR REPLACE PROCEDURE getpatientinfo(
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
    WHERE ST.studyInstanceUID = p_studyUid;
  END IF;
  IF(p_accessionNumber IS NOT NULL) THEN
     IF (p_patientId is not null) THEN
		OPEN resultset FOR
	 	SELECT PT.pk, PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
		FROM Patients PT
		INNER JOIN Studies ST ON ST.patientFK = PT.pk
		WHERE st.accessionNumber = p_accessionNumber
		AND PT.patientid = p_patientId;
     ELSE
		OPEN resultset FOR
		SELECT PT.pk, PT.lastName, PT.firstName, PT.middleName, PT.prefix, PT.suffix, PT.patientId, PT.idIssuer, PT.birthDate, PT.sex
		FROM Patients PT
		INNER JOIN Studies ST ON ST.patientFK = PT.pk
		WHERE st.accessionNumber = p_accessionNumber;
	END IF;
  END IF;
END getPatientInfo;
/

--PROMPT CREATE OR REPLACE PROCEDURE isUserAuthenticated
CREATE OR REPLACE PROCEDURE isUserAuthenticated(
  p_username VARCHAR2,
  p_password VARCHAR2,
  p_result OUT NUMBER
)
AS
BEGIN
  SELECT COUNT(pk) INTO p_result FROM Users WHERE userName=p_username AND password=p_password;
END isUserAuthenticated;
/
