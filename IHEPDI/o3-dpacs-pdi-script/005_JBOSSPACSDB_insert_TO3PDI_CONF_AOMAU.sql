delete from JBOSSPACSDB.TO3PDI_CONF;
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('Assets','/fs-dmass/ConfAll/AOMAU/Conf/assets/','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('Workarea','/fs-dmass/ConfAll/AOMAU/workarea/','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('PasswordFSE','caff110499d33e2fe67e9a8e12777baa06e2e210','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('Readme','/fs-dmass/ConfAll/AOMAU/Conf/README.txt','1');
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
	  <nome>908</nome>
	  <endpoint>http://10.138.137.4:11210</endpoint>
	  <nume_retry>3</nume_retry>
	  <time_lapse>10000</time_lapse>
	  <dist>/fs-dmass/ConfAll/AOMAU/dist/</dist>	
	  <xslt>/fs-dmass/ConfAll/AOMAU/Conf/assets/AOMAU/HTMLTransformation.xsl</xslt>  
	  <css>AOMAU/NONE.css</css>
	  <time_lapse_move>5000</time_lapse_move>
	  <js>AOMAU/utility.js</js>
      <commandMove>cp ${fileToMove} ${dist}</commandMove>
	  <viewer>/fs-dmass/ConfAll/AOMAU/Conf/viewer/viewer_MAU.zip</viewer>
	</asl>
</configurazioni>','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('NumCurrentThread','-1','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('ExpireAt','1','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('SleepRetryNotifica','10000','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('NumRetryNotifica','3','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('BatchSleep','5000','1');
Insert into JBOSSPACSDB.TO3PDI_CONF (PARAMKEY,PARAMVALUE,ENABLED) values ('NumThreadMax','6','1');