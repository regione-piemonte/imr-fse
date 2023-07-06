delete from JBOSSPACSDB.TO3PDI_CONF;
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('Assets','/fs-dmass/ConfAll/CN1/Conf/assets/','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('Workarea','/fs-dmass/ConfAll/CN1/workarea/','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('PasswordFSE','caff110499d33e2fe67e9a8e12777baa06e2e210','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('Readme','/fs-dmass/ConfAll/CN1/Conf/README.txt','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('UserNameFSE','dma','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('isCopyStandardActivate','false','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('CompleteNoticeUrl','https://be-dma.isan.csi.it/dmasssrv/CompleteNotice','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('DEFAULT_ID_STRUTTURA','NONE','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('DEFAULT_ID_ASL','NONE','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('PatientInfo','/o3-dpacs-wado/getPatientInfo','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('WadoUrl','/o3-dpacs-wado/wado','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('DPACSWeb','/o3-dpacs-web/','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('ScpViewer','cp ${from} ${dist}','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('CreatePdiRequestEnabled','TRUE','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('DpacspdiConfiguration','<?xml version="1.0" encoding="UTF-8 " ?>
<configurazioni>
	<asl id ="1">
	  <nome>210</nome>
	  <endpoint>http://10.138.137.8:10710</endpoint>
	  <nume_retry>3</nume_retry>
	  <time_lapse>10000</time_lapse>
	  <dist>/fs-dmass/ConfAll/CN1/dist/</dist>	
	  <xslt>/fs-dmass/ConfAll/CN1/Conf/assets/ASLCN1/HTMLTransformation.xsl</xslt>  
	  <css>ASLCN1/NONE.css</css>
	  <time_lapse_move>5000</time_lapse_move>
	  <js>ASLCN1/utility.js</js>
      <commandMove>cp ${fileToMove} ${dist}</commandMove>
	  <viewer></viewer>
	</asl>
</configurazioni>','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('NumCurrentThread','-1','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('ExpireAt','1','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('SleepRetryNotifica','10000','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('NumRetryNotifica','3','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('BatchSleep','5000','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('NumThreadMax','6','1');