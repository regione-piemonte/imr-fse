set define off;
insert into "globalconfiguration" (PARAMKEY, PARAMVALUE) values ('MoveStudyMinDelayHour',5);
insert into "globalconfiguration" (PARAMKEY, PARAMVALUE) values ('MoveStudyMaxDelayHour',30);
insert into "globalconfiguration" (PARAMKEY, PARAMVALUE) values ('MoveStudyLivYellow',10);
insert into "globalconfiguration" (PARAMKEY, PARAMVALUE) values ('MoveStudyMaxLivRed',20);
INSERT INTO "GLOBALCONFIGURATION" (PARAMKEY, PARAMVALUE) VALUES ('EmailTitleYellow', ' ');
INSERT INTO "GLOBALCONFIGURATION" (PARAMKEY, PARAMVALUE) VALUES ('EmailTitlered', ' ');
INSERT INTO "GLOBALCONFIGURATION" (PARAMKEY) VALUES ('Emailbody');
INSERT INTO "GLOBALCONFIGURATION" (PARAMKEY) VALUES ('EmailDestinatari');


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



create or replace PROCEDURE "GETCOUNTMOVERRORDELTAHOURS"
    --  La procedura controlla i moves iniziati entro un certo range orario e restituisce i seguenti valori:
    (
        ris_totMoves out number ,           --  numero di Moves nel periodo
        ris_totMovesInError out number,     --  numero di Moves in errore
        ris_percentuale out numeric,        --  percentuale di Moves in errore
        ris_Avviso out nvarchar2            --  avviso eventualmente da utilizzare nelle mails (Attenzione, sotto una certa soglia, Errore sopra una certa soglia)
    )
    is 

    DeltaHoursMin number :=0 ;              --  DELTA-ORARIO: n°ore dall'inizio della startMov dopo le quali prendere in considerazione la Move
    DeltaHoursMax number :=0;               --  DELTA-ORARIO: n°ore dall'inizio della startMov entro le quali prendere in considerazione la Move
    LivYellow number := 10;                 --  numero di errori oltre il quale si entra in zona gialla
    LivRed number := 20;                    --  numero di errori oltre il quale si entra in zona rossa 
    Adesso timestamp(6) := SYSTIMESTAMP;    --  costante del momento dal quale calcolare i totali

BEGIN

    --  recupero i parametri del delta-ore
    select paramValue into DeltaHoursMin from GlobalConfiguration where paramKey = 'MoveStudyMinDelayHour';     -- DA CREARE IN GLOBALCONFIGURATION
    select paramValue into DeltaHoursMax from GlobalConfiguration where paramKey = 'MoveStudyMaxDelayHour';     -- DA CREARE IN GLOBALCONFIGURATION
    --  recupero i parametri dei livelli di urgenza
    select paramValue into LivYellow from GlobalConfiguration where paramKey = 'MoveStudyLivYellow';            -- DA CREARE IN GLOBALCONFIGURATION
    select paramValue into LivRed from GlobalConfiguration where paramKey = 'MoveStudyMaxLivRed';               -- DA CREARE IN GLOBALCONFIGURATION

    select 

        a.totMoves, 
        a.totMovesInError, 

        -- calcolo la % di erorre
        cast(round((case when Totmoves = 0 then 0 else (TotMovesInError/Totmoves)*100 end),2) as Numeric(10,2)),
        --  calcolo l'avviso
        (case when totMovesInError >= LivYellow and totMovesInError < LivRed then 'ATTENZIONE' when totMovesInError >= LivRed then 'ERRORE' else ' ' end)

        into ris_totMoves, ris_totMovesInError, ris_percentuale, ris_Avviso

    from (select count (*) as Totmoves,
            --  conto i moves non completati
            count(case when ISMOVECOMPLETED(ACCESSIONNUMBER) =0 and endmov is null then 1 else 0 end) as totMovesInError
            from movestudyhistory where 
                --  nel periodo indicato
                (startMov + NUMTODSINTERVAL(DeltaHoursMin, 'HOUR')) <  systimestamp and
                (startMov + NUMTODSINTERVAL(DeltaHoursMax, 'HOUR')) > systimestamp
        ) a;
END;
