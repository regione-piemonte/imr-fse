
CREATE OR REPLACE PROCEDURE queryInstanceLevel(
  p_seriesInstanceUid VARCHAR2,
  p_multipleInstanceUids NUMBER,    -- If 1-> p_sopInstanceUID is a list and should be used in an IN statement. Ignored if p_sopInstanceUID is NULL
  p_multipleSopClasses NUMBER,    -- If 1-> p_sopClassUid is a list and should be used in an IN statement. Ignored if p_sopClassUid is NULL

  p_sopInstanceUID VARCHAR2, -- as list, potentially. Separated by backslash in DICOM, passed in this form: '1.2.3','1.2.3.4','5.6.7'
  p_sopClassUid VARCHAR2, -- as list, potentially. Separated by backslash in DICOM, passed in this form: '1.2.3','1.2.3.4','5.6.7'
  p_instanceNumber NUMBER,

  p_samplesPerPixel NUMBER,
  p_rowsNum NUMBER,
  p_columnsNum NUMBER,
  p_bitsAllocated NUMBER,
  p_bitsStored NUMBER,
  p_highBit NUMBER,
  p_pixelRepresentation NUMBER,
  p_numberOfFrames NUMBER,    -- IF NOT NULL THE numberOfFrames IS RETURNED

  p_presentationLabel VARCHAR2,	-- UPPER
  p_presentationDescription VARCHAR2,	-- UPPER
  p_presentationCreationDate DATE,
  p_presentationCreationDateLATE DATE,
  p_presentationCreationTime DATE,
  p_presentationCreationTimeLATE DATE,
  p_presentationCreatorsName VARCHAR2, --UPPER
  p_recommendedViewingMode VARCHAR2, -- UPPER

  p_completionFlag VARCHAR2,	-- LIKE UPPER. The wildcards must be already present in the parameter
  p_verificationFlag VARCHAR2,	-- LIKE UPPER. The wildcards must be already present in the parameter
  p_contentDate DATE,
  p_contentDateLATE DATE,
  p_contentTime DATE,
  p_contentTimeLATE DATE,
  p_observationDateTime DATE,
  p_observationDateTimeLATE DATE,

  p_overlayNumber NUMBER,
  p_overlayRows NUMBER,
  p_overlayColumns NUMBER,
  p_overlayType VARCHAR2,	-- UPPER
  p_overlayBitsAllocated NUMBER,


  r_studyInstanceUID OUT VARCHAR2,
  r_fastestAccess OUT VARCHAR2,
  r_studyStatus OUT CHAR,
  r_specificCharSet OUT VARCHAR2,
  resultset OUT Types.ResultSet
)
AS

  l_query VARCHAR2(4000 CHAR);
  l_commonJoin VARCHAR2(200 CHAR);
  l_commonWhere VARCHAR2(200 CHAR);
  l_instanceType NUMBER(1);

BEGIN

    BEGIN
    	SELECT St.studyInstanceUID, St.fastestAccess, St.studyStatus, St.specificCharSet INTO r_studyInstanceUID, r_fastestAccess, r_studyStatus, r_specificCharSet
    	FROM Series Se
    	INNER JOIN Studies St ON St.studyInstanceUID=Se.studyFK
    	WHERE Se.seriesInstanceUID=p_seriesInstanceUid;
    EXCEPTION WHEN NO_DATA_FOUND THEN
    	RETURN;
  	END;


    select case
                when exists (select 1 from Images where seriesFK = p_seriesInstanceUid and rownum=1) then 1
                when exists (select 1 from ImagesNearline where seriesFK = p_seriesInstanceUid and rownum=1) then 1
                when exists (select 1 from ImagesOffline where seriesFK = p_seriesInstanceUid and rownum=1) then 1
                when exists (select 5 from NonImages where seriesFK = p_seriesInstanceUid and rownum=1) then 5
                when exists (select 3 from StructReps where seriesFK = p_seriesInstanceUid and rownum=1) then 3
                when exists (select 4 from Overlays where seriesFK = p_seriesInstanceUid and rownum=1) then 4
                when exists (select 6 from KeyObjects where seriesFK = p_seriesInstanceUid and rownum=1) then 6
                else 2
            end INTO l_instanceType
    from dual;

    l_query:='SELECT '||l_instanceType||', I.sopInstanceUID, I.sopClassUID, I.instanceNumber';
    l_commonWhere:=' WHERE I.seriesFK='''||p_seriesInstanceUid||''' AND I.deprecated=0';

    IF(p_sopInstanceUID IS NOT NULL)THEN
      IF(p_multipleInstanceUids=1) THEN
        l_commonWhere:=l_commonWhere||' AND I.sopInstanceUID IN ('||p_sopInstanceUID||')';
      ELSE
        l_commonWhere:=l_commonWhere||' AND I.sopInstanceUID = '''||p_sopInstanceUID||'''';
      END IF;
    END IF;
    IF(p_sopClassUid IS NOT NULL)THEN
      IF(p_multipleSopClasses=1) THEN
        l_commonWhere:=l_commonWhere||' AND I.sopClassUID IN ('||p_sopClassUid||')';
      ELSE
        l_commonWhere:=l_commonWhere||' AND I.sopClassUID = '''||p_sopClassUid||'''';
      END IF;
    END IF;
    IF(p_instanceNumber IS NOT NULL)THEN
      l_commonWhere:=l_commonWhere||' AND I.instanceNumber = '||p_instanceNumber;
    END IF;

    CASE l_instanceType
      WHEN 1 THEN   -- Images
        BEGIN

        IF(p_presentationLabel IS NOT NULL OR p_presentationDescription IS NOT NULL OR p_presentationCreationDate IS NOT NULL OR p_presentationCreationDateLATE IS NOT NULL OR p_presentationCreationTime IS NOT NULL OR p_presentationCreationTimeLATE IS NOT NULL OR p_presentationCreatorsName IS NOT NULL OR p_recommendedViewingMode IS NOT NULL OR
           p_completionFlag IS NOT NULL OR p_verificationFlag IS NOT NULL OR p_contentDate IS NOT NULL OR p_contentDateLATE IS NOT NULL OR p_contentTime IS NOT NULL OR p_contentTimeLATE IS NOT NULL OR p_observationDateTime IS NOT NULL OR p_observationDateTimeLATE IS NOT NULL OR
           p_overlayNumber IS NOT NULL OR p_overlayRows IS NOT NULL OR p_overlayColumns IS NOT NULL OR p_overlayType IS NOT NULL OR p_overlayBitsAllocated IS NOT NULL) THEN
           RETURN;
        END IF;

          l_query:=l_query|| ', I.samplesPerPixel, I.rowsNum, I.columnsNum, I.bitsAllocated, I.bitsStored, I.highBit, I.pixelRepresentation';
          l_commonJoin:=' FROM Images'||CASE WHEN(r_studyStatus ='n')THEN 'Nearline' WHEN (r_studyStatus ='p') THEN 'Offline' ELSE '' END||' I';
          IF (p_numberOfFrames IS NOT NULL) THEN
            l_query:=l_query|| ', Inof.numberOfFrames';
            l_commonJoin:=l_commonJoin||' LEFT JOIN ImageNumberOfFrames Inof ON Inof.sopInstanceUid=I.sopInstanceUID';
          ELSE
            l_query:=l_query|| ', NULL';
          END IF;

          -- WHERE fields:
          IF(p_samplesPerPixel IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.samplesPerPixel = '||p_samplesPerPixel;
          END IF;
          IF(p_rowsNum IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.rowsNum = '||p_rowsNum;
          END IF;
          IF(p_columnsNum IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.columnsNum = '||p_columnsNum;
          END IF;
          IF(p_bitsAllocated IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.bitsAllocated = '||p_bitsAllocated;
          END IF;
          IF(p_bitsStored IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.bitsStored = '||p_bitsStored;
          END IF;
          IF(p_highBit IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.highBit = '||p_highBit;
          END IF;
          IF(p_pixelRepresentation IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.pixelRepresentation = '||p_pixelRepresentation;
          END IF;

        END;
      WHEN 2 THEN   -- PresStates
        BEGIN
          IF(p_samplesPerPixel IS NOT NULL OR p_rowsNum IS NOT NULL OR p_columnsNum IS NOT NULL OR p_bitsAllocated IS NOT NULL OR p_bitsStored IS NOT NULL OR p_highBit IS NOT NULL OR p_pixelRepresentation IS NOT NULL OR p_numberOfFrames IS NOT NULL OR
             p_completionFlag IS NOT NULL OR p_verificationFlag IS NOT NULL OR p_contentDate IS NOT NULL OR p_contentDateLATE IS NOT NULL OR p_contentTime IS NOT NULL OR p_contentTimeLATE IS NOT NULL OR p_observationDateTime IS NOT NULL OR p_observationDateTimeLATE IS NOT NULL OR
             p_overlayNumber IS NOT NULL OR p_overlayRows IS NOT NULL OR p_overlayColumns IS NOT NULL OR p_overlayType IS NOT NULL OR p_overlayBitsAllocated IS NOT NULL) THEN
             RETURN;
          END IF;

          l_query:=l_query|| ', I.presentationLabel, I.presentationDescription, I.presentationCreationDate, I.presentationCreationTime, I.presentationCreatorsName, I.recommendedViewingMode';
          l_commonJoin:=l_commonJoin||' FROM PresStates I';

          -- WHERE fields:
          IF(p_presentationLabel IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.presentationLabel = UPPER('''||p_presentationLabel||''')';
          END IF;
          IF(p_presentationDescription IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.presentationDescription = UPPER('''||p_presentationDescription||''')';
          END IF;

          IF(p_presentationCreationDate IS NOT NULL)THEN
            IF(p_presentationCreationDateLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.presentationCreationDate = '''||p_presentationCreationDate||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.presentationCreationDate >= '''||p_presentationCreationDate||''' AND I.presentationCreationDate <= '''||p_presentationCreationDateLATE||''')';
            END IF;
          END IF;
          IF(p_presentationCreationTime IS NOT NULL)THEN
            IF(p_presentationCreationTimeLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.presentationCreationTime = '''||p_presentationCreationTime||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.presentationCreationTime >= '''||p_presentationCreationTime||''' AND I.presentationCreationTime <= '''||p_presentationCreationTimeLATE||''')';
            END IF;
          END IF;

          IF(p_presentationCreatorsName IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.presentationCreatorsName = UPPER('''||p_presentationCreatorsName||''')';
          END IF;
          IF(p_recommendedViewingMode IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.recommendedViewingMode = UPPER('''||p_recommendedViewingMode||''')';
          END IF;

        END;
      WHEN 3 THEN   -- StructReps
        BEGIN

          IF(p_samplesPerPixel IS NOT NULL OR p_rowsNum IS NOT NULL OR p_columnsNum IS NOT NULL OR p_bitsAllocated IS NOT NULL OR p_bitsStored IS NOT NULL OR p_highBit IS NOT NULL OR p_pixelRepresentation IS NOT NULL OR p_numberOfFrames IS NOT NULL OR
             p_presentationLabel IS NOT NULL OR p_presentationDescription IS NOT NULL OR p_presentationCreationDate IS NOT NULL OR p_presentationCreationDateLATE IS NOT NULL OR p_presentationCreationTime IS NOT NULL OR p_presentationCreationTimeLATE IS NOT NULL OR p_presentationCreatorsName IS NOT NULL OR p_recommendedViewingMode IS NOT NULL OR
             p_overlayNumber IS NOT NULL OR p_overlayRows IS NOT NULL OR p_overlayColumns IS NOT NULL OR p_overlayType IS NOT NULL OR p_overlayBitsAllocated IS NOT NULL) THEN
             RETURN;
          END IF;

          l_query:=l_query|| ', I.completionFlag, I.verificationFlag, I.contentDate, I.contentTime, I.observationDateTime, I.conceptNameCodeSequence, CS.codeValue, CS.codingSchemeDesignator, CS.codingSchemeVersion, CS.codeMeaning';
          l_commonJoin:=l_commonJoin||' FROM StructReps I INNER JOIN CodeSequences CS ON I.conceptNameCodeSequence=CS.pk';

          -- WHERE fields:
          IF(p_completionFlag IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.completionFlag LIKE UPPER('''||p_completionFlag||''')';
          END IF;
          IF(p_verificationFlag IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.verificationFlag LIKE UPPER('''||p_verificationFlag||''')';
          END IF;
          IF(p_contentDate IS NOT NULL)THEN
            IF(p_contentDateLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.contentDate = '''||p_contentDate||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.contentDate >= '''||p_contentDate||''' AND I.contentDate <= '''||p_contentDateLATE||''')';
            END IF;
          END IF;
          IF(p_contentTime IS NOT NULL)THEN
            IF(p_contentTimeLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.contentTime = '''||p_contentTime||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.contentTime >= '''||p_contentTime||''' AND I.contentTime <= '''||p_contentTimeLATE||''')';
            END IF;
          END IF;
          IF(p_observationDateTime IS NOT NULL)THEN
            IF(p_observationDateTimeLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.observationDateTime = '''||p_observationDateTime||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.observationDateTime >= '''||p_observationDateTime||''' AND I.observationDateTime <= '''||p_observationDateTimeLATE||''')';
            END IF;
          END IF;

        END;
      WHEN 4 THEN   -- Overlays
        BEGIN

          IF(p_samplesPerPixel IS NOT NULL OR p_rowsNum IS NOT NULL OR p_columnsNum IS NOT NULL OR p_bitsAllocated IS NOT NULL OR p_bitsStored IS NOT NULL OR p_highBit IS NOT NULL OR p_pixelRepresentation IS NOT NULL OR p_numberOfFrames IS NOT NULL OR
             p_presentationLabel IS NOT NULL OR p_presentationDescription IS NOT NULL OR p_presentationCreationDate IS NOT NULL OR p_presentationCreationDateLATE IS NOT NULL OR p_presentationCreationTime IS NOT NULL OR p_presentationCreationTimeLATE IS NOT NULL OR p_presentationCreatorsName IS NOT NULL OR p_recommendedViewingMode IS NOT NULL OR
             p_completionFlag IS NOT NULL OR p_verificationFlag IS NOT NULL OR p_contentDate IS NOT NULL OR p_contentDateLATE IS NOT NULL OR p_contentTime IS NOT NULL OR p_contentTimeLATE IS NOT NULL OR p_observationDateTime IS NOT NULL OR p_observationDateTimeLATE IS NOT NULL) THEN
             RETURN;
          END IF;

          l_query:=l_query|| ', I.overlayNumber, I.overlayRows, I.overlayColumns, I.overlayBitsAllocated, I.overlayType';
          l_commonJoin:=l_commonJoin||' FROM Overlays I ';

          -- WHERE fields:
          IF(p_overlayNumber IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.overlayNumber = '||p_overlayNumber;
          END IF;
          IF(p_overlayRows IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.overlayRows = '||p_overlayRows;
          END IF;
          IF(p_overlayColumns IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.overlayColumns = '||p_overlayColumns;
          END IF;
          IF(p_overlayType IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.overlayType = UPPER('''||p_overlayType||''')';
          END IF;
          IF(p_overlayBitsAllocated IS NOT NULL)THEN
            l_commonWhere:=l_commonWhere||' AND I.overlayBitsAllocated = '||p_overlayBitsAllocated;
          END IF;

        END;
      WHEN 5 THEN   -- NonImages
        BEGIN

          IF(p_samplesPerPixel IS NOT NULL OR p_rowsNum IS NOT NULL OR p_columnsNum IS NOT NULL OR p_bitsAllocated IS NOT NULL OR p_bitsStored IS NOT NULL OR p_highBit IS NOT NULL OR p_pixelRepresentation IS NOT NULL OR p_numberOfFrames IS NOT NULL OR
               p_presentationLabel IS NOT NULL OR p_presentationDescription IS NOT NULL OR p_presentationCreationDate IS NOT NULL OR p_presentationCreationDateLATE IS NOT NULL OR p_presentationCreationTime IS NOT NULL OR p_presentationCreationTimeLATE IS NOT NULL OR p_presentationCreatorsName IS NOT NULL OR p_recommendedViewingMode IS NOT NULL OR
               p_completionFlag IS NOT NULL OR p_verificationFlag IS NOT NULL OR p_contentDate IS NOT NULL OR p_contentDateLATE IS NOT NULL OR p_contentTime IS NOT NULL OR p_contentTimeLATE IS NOT NULL OR p_observationDateTime IS NOT NULL OR p_observationDateTimeLATE IS NOT NULL OR
               p_overlayNumber IS NOT NULL OR p_overlayRows IS NOT NULL OR p_overlayColumns IS NOT NULL OR p_overlayType IS NOT NULL OR p_overlayBitsAllocated IS NOT NULL) THEN
               RETURN;
            END IF;

          l_commonJoin:=l_commonJoin||' FROM NonImages I ';
        END;
      WHEN 6 THEN   -- KeyObjects
        BEGIN

          IF(p_samplesPerPixel IS NOT NULL OR p_rowsNum IS NOT NULL OR p_columnsNum IS NOT NULL OR p_bitsAllocated IS NOT NULL OR p_bitsStored IS NOT NULL OR p_highBit IS NOT NULL OR p_pixelRepresentation IS NOT NULL OR p_numberOfFrames IS NOT NULL OR
             p_presentationLabel IS NOT NULL OR p_presentationDescription IS NOT NULL OR p_presentationCreationDate IS NOT NULL OR p_presentationCreationDateLATE IS NOT NULL OR p_presentationCreationTime IS NOT NULL OR p_presentationCreationTimeLATE IS NOT NULL OR p_presentationCreatorsName IS NOT NULL OR p_recommendedViewingMode IS NOT NULL OR
             p_overlayNumber IS NOT NULL OR p_overlayRows IS NOT NULL OR p_overlayColumns IS NOT NULL OR p_overlayType IS NOT NULL OR p_overlayBitsAllocated IS NOT NULL) THEN
             RETURN;
          END IF;

          l_query:=l_query|| ', I.contentDate, I.contentTime, I.codeSequencesFK, CS.codeValue, CS.codingSchemeDesignator, CS.codingSchemeVersion, CS.codeMeaning';
          l_commonJoin:=l_commonJoin||' FROM StructReps I INNER JOIN CodeSequences CS ON I.codeSequencesFK=CS.pk';

          -- WHERE fields:
          IF(p_contentDate IS NOT NULL)THEN
            IF(p_contentDateLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.contentDate = '''||p_contentDate||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.contentDate >= '''||p_contentDate||''' AND I.contentDate <= '''||p_contentDateLATE||''')';
            END IF;
          END IF;
          IF(p_contentTime IS NOT NULL)THEN
            IF(p_contentTimeLATE IS NULL) THEN
              l_commonWhere:=l_commonWhere||' AND I.contentTime = '''||p_contentTime||'''';
            ELSE
              l_commonWhere:=l_commonWhere||' AND (I.contentTime >= '''||p_contentTime||''' AND I.contentTime <= '''||p_contentTimeLATE||''')';
            END IF;
          END IF;

        END;
    END CASE;

    l_query:=l_query||l_commonJoin||l_commonWhere||' ORDER BY I.instanceNumber ASC, I.sopInstanceUID ASC';
    -- dbms_output.put_line(l_query); 	-- REMEMBER TO USE 			SET SERVEROUTPUT ON;
    OPEN resultSet FOR l_query;

END queryInstanceLevel;
