create or replace TRIGGER "MOVESTUDYHISTORYUPDPKTRG" BEFORE
  UPDATE ON MoveStudyHistory FOR EACH ROW
BEGIN

		-- se 'endmov' della stadymove è nullo, incrementa i retry: significa che ad ogni update corrisponde un retry.

        IF :new.startMov IS NOT NULL and (:old.startMov IS NULL or :old.startMov != :new.startMov) AND :old.endMov is null THEN
               :new.idRetry := nvl(:old.idRetry,0) + 1;
        END IF;  


        IF ISMOVECOMPLETED(:new.ACCESSIONNUMBER) = 1 THEN
            
			-- move fisicamente ultimata
			
			if :old.endMov is null AND :new.endMov is null then
                :new.endMov := SYSTIMESTAMP ;
            end if;
			
            :new.ERRORMESSAGE := 'MOVECOMPLETED’;
			
        ELSE
-- move fisicamente non ultimata
		
            IF :new.ERRORMESSAGE IS NOT NULL and (:old.ERRORMESSAGE IS NULL or instr(:old.ERRORMESSAGE, :new.ERRORMESSAGE) = 0 ) THEN
			
					-- se c'è un nuovo messaggio e non è già presente in 'errorMessage', lo memorizziamo
			
                    :new.ERRORMESSAGE := :old.ERRORMESSAGE || ' - ' || :new.ERRORMESSAGE;
            END IF;  
			
        END IF;  
END;
