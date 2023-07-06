create or replace PROCEDURE getStudy(
  p_patientFk IN NUMBER,
  p_filter IN VARCHAR2,
  resultset OUT Types.ResultSet
)
AS
BEGIN
IF (p_filter IS NOT NULL) THEN
OPEN resultset FOR
  	SELECT ST.studyDate, ST.studyTime, ST.studyDescription, ST.accessionNumber, ST.studyInstanceUID, ST.studyStatus
  	FROM Studies ST
 	WHERE
  ST.patientFk = p_patientFk AND(
  ST.accessionNumber = p_filter or st.studyInstanceUID = p_filter) AND
  -- jira 24 escludo studi la cui movimentazione puo' essere non terminata correttamente
  ST.accessionNumber NOT IN (
		SELECT mvinner.accessionNumber 
		FROM moveStudyHistory mvinner 
		WHERE mvinner.accessionNumber = ST.accessionNumber 
		AND mvinner.eventtime = ( select max(mv.eventtime) FROM MOVESTUDYHISTORY MV WHERE mv.accessionnumber = ST.accessionNumber)
        AND IDRETRY <= (SELECT paramValue FROM GLOBALCONFIGURATION WHERE paramKey = 'MoveStudyMaxRetry')
		AND endMov IS NULL
  )
  AND ST.deprecated = 0
  ORDER BY ST.studyDate DESC;
ELSE
OPEN resultset FOR
  	SELECT ST.studyDate, ST.studyTime, ST.studyDescription, ST.accessionNumber, ST.studyInstanceUID, ST.studyStatus
  	FROM Studies ST
 	WHERE
  ST.patientFk = p_patientFk AND
  -- jira 24 escludo studi la cui movimentazione puo' essere non terminata correttamente
  ST.accessionNumber NOT IN (SELECT accessionNumber FROM moveStudyHistory WHERE endMov IS NULL)
  ORDER BY ST.studyDate DESC;
END IF;

END getStudy;