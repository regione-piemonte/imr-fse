--UPDATE TO O3-DPACS VERSION 2.23.0

--PROMPT ALTER TABLE MOVESTUDYHISTORY
ALTER TABLE MOVESTUDYHISTORY ADD
idIssuer   VARCHAR2(64 CHAR);


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
  r_patientID OUT VARCHAR2,
  r_idIssuer OUT VARCHAR2
)
AS
BEGIN
  BEGIN
--setlog( 'INFO', 'getParamsToRetrieveAccNum', 'IN: p_id = '||p_id, NULL, NULL);

    SELECT msh.calledAet, msh.callingAet, msh.moveAet, msh.accessionNumber, kn.ip, kn.port, kn.pk, msh.patientID, msh.idIssuer
           INTO r_calledAet, r_callingAet, r_moveAet, r_accNUm, r_ip, r_port, r_knownNode, r_patientID, r_idIssuer
    FROM MoveStudyHistory msh, KnownNodes kn
    WHERE msh.id=p_id AND kn.pk = msh.knownNodeFk;

  EXCEPTION WHEN NO_DATA_FOUND THEN

    SELECT NULL, NULL, NULL, NULL, NULL, NULL
    INTO r_calledAet, r_callingAet, r_moveAet, r_accNUm, r_ip, r_port
    FROM DUAL;

  END;
--setlog( 'INFO', 'getParamsToRetrieveAccNum', 'OUT: r_accNUm = '||r_accNUm|| ' r_calledAet = '||r_calledAet|| ' r_callingAet = '||r_callingAet|| ' r_moveAet = '||r_moveAet, NULL, NULL);
END getParamsToRetrieveAccNum;
