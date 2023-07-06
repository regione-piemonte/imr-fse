
------------------------------------------------------------------------------------------------------------------------

CREATE TRIGGER KeyObjectsAlter AFTER UPDATE OF sopInstanceUID ON KeyObjects
  REFERENCING NEW AS NEWROW OLD AS OLDROW FOR EACH ROW BEGIN
    UPDATE KeyObjectReferences SET KeyObjectsFK =:newRow.sopInstanceUID WHERE KeyObjectsFK=:oldRow.sopInstanceUID;
END PresStatesAlter;