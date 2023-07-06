CREATE OR REPLACE PROCEDURE getTo3pdi_Conf(
    p_paramKey IN VARCHAR2,
    resultset OUT Types.ResultSet
)
AS
BEGIN
  OPEN resultset FOR SELECT paramValue FROM To3pdi_Conf WHERE paramKey=p_paramKey AND enabled=1;
END getTo3pdi_Conf;