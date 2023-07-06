
CREATE OR REPLACE PROCEDURE getInstancesOfVerifiedStudy(
    studyUID IN VARCHAR2,
    resultset OUT Types.ResultSet
)
AS
BEGIN
  OPEN resultset FOR
SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, I.sopInstanceUID, I.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
FROM studiesToVerify STV
INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
INNER JOIN Patients P ON P.pk = Std.patientFk
INNER JOIN Series Se ON stv.studyfk = se.studyFK
INNER JOIN Images I ON Se.seriesInstanceUID = I.seriesFK
WHERE stv.verifiedDate IS NOT NULL AND
(std.studyinstanceuid = studyUID AND stv.jobfinishedon IS NULL)
UNION ALL
SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, NI.sopInstanceUID, NI.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
FROM studiesToVerify STV
INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
INNER JOIN Patients P ON P.pk = Std.patientFk
INNER JOIN Series Se ON stv.studyfk = se.studyFK
INNER JOIN NonImages NI ON Se.seriesInstanceUID = NI.seriesFK
WHERE stv.verifiedDate IS NOT NULL and
(std.studyinstanceuid = studyUID AND stv.jobfinishedon IS NULL)
UNION ALL
SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, SR.sopInstanceUID, SR.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
FROM studiesToVerify STV
INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
INNER JOIN Patients P ON P.pk = Std.patientFk
INNER JOIN Series Se ON stv.studyfk = se.studyFK
INNER JOIN StructReps SR ON Se.seriesInstanceUID = SR.seriesFK
WHERE stv.verifiedDate IS NOT NULL and
(std.studyinstanceuid = studyUID AND stv.jobfinishedon IS NULL)
UNION ALL
SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, O.sopInstanceUID, O.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
FROM studiesToVerify STV
INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
INNER JOIN Patients P ON P.pk = Std.patientFk
INNER JOIN Series Se ON stv.studyfk = se.studyFK
INNER JOIN Overlays O ON Se.seriesInstanceUID = O.seriesFK
WHERE stv.verifiedDate IS NOT NULL AND
(std.studyinstanceuid = studyUID AND stv.jobfinishedon IS NULL)
UNION ALL
SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, PS.sopInstanceUID, PS.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
FROM studiesToVerify STV
INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
INNER JOIN Patients P ON P.pk = Std.patientFk
INNER JOIN Series Se ON stv.studyfk = se.studyFK
INNER JOIN PresStates PS ON Se.seriesInstanceUID = PS.seriesFK
WHERE stv.verifiedDate IS NOT NULL AND
(std.studyinstanceuid = studyUID AND stv.jobfinishedon IS NULL)
UNION ALL
SELECT P.patientId, P.idIssuer, P.lastName||'^'||P.firstName||'^'||P.middleName||'^'||P.prefix||'^'||P.suffix, STV.studyFK, Se.seriesInstanceUID, KO.sopInstanceUID, KO.sopClassUID, Std.accessionNumber, Stv.xdsMessageId, Stv.attemptsCounter
FROM studiesToVerify STV
INNER JOIN Studies Std ON Stv.studyFK = Std.studyinstanceuid
INNER JOIN Patients P ON P.pk = Std.patientFk
INNER JOIN Series Se ON stv.studyfk = se.studyFK
INNER JOIN KeyObjects KO ON Se.seriesInstanceUID = KO.seriesFK
WHERE stv.verifiedDate IS NOT NULL and
(std.studyinstanceuid = studyUID AND stv.jobfinishedon IS NULL);
END getInstancesOfVerifiedStudy;

------