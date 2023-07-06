CREATE OR REPLACE TRIGGER "MOVESTUDYHISTORYUPDPKTRG" BEFORE
  UPDATE ON MoveStudyHistory FOR EACH ROW
BEGIN

		-- se 'endmov' della stadymove è nullo, incrementa i retry: significa che ad ogni update corrisponde un retry.

        IF :new.startMov IS NOT NULL and (:old.startMov IS NULL or :old.startMov != :new.startMov) AND :old.endMov is null THEN
               :new.idRetry := nvl(:old.idRetry,0) + 1;
        END IF;  

/*
        IF ISMOVECOMPLETED(:new.ACCESSIONNUMBER) = 1 THEN

			-- move fisicamente ultimata

			if :old.endMov is null AND :new.endMov is null then
                :new.endMov := SYSTIMESTAMP ;
            end if;

            :new.ERRORMESSAGE := 'MOVECOMPLETED - '||:new.ERRORMESSAGE;

        ELSE

			-- move fisicamente non ultimata

            IF :new.ERRORMESSAGE IS NOT NULL and (:old.ERRORMESSAGE IS NULL or instr(:old.ERRORMESSAGE, :new.ERRORMESSAGE) = 0 ) THEN

					-- se c'è un nuovo messaggio e non è già presente in 'errorMessage', lo memorizziamo

                    :new.ERRORMESSAGE := :old.ERRORMESSAGE || ' - ' || :new.ERRORMESSAGE;
            END IF;  

        END IF; */ 
END;


CREATE OR REPLACE PROCEDURE getStudy(
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
  ST.accessionNumber NOT IN (SELECT accessionNumber FROM moveStudyHistory WHERE endMov IS NULL)
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