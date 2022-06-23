CREATE TABLE dmass.dmass_l_servizi (
	id_ser int8 NOT NULL,
	nome_servizio varchar NOT NULL,
	data_richiesta timestamp(0) NULL,
	data_risposta timestamp(0) NULL,
	cf_utente varchar NOT NULL,
	ruolo_utente varchar NULL,
	cf_assistito varchar NULL,
	applicazione varchar NULL,
	appl_verticale varchar NULL,
	regime varchar NULL,
	id_collocazione int8 NULL,
	cod_esito_risposta_servizio varchar NULL,
	ip_richiedente varchar NULL,
	codice_servizio varchar NULL,
	data_ins timestamp(0) NOT NULL,
	data_mod timestamp(0) NULL,
	CONSTRAINT dmass_l_servizi_pk PRIMARY KEY (id_ser)
);

CREATE TABLE dmass.dmass_l_chiamate_servizi (
	id int8 NOT NULL,
	id_ser int8 NOT NULL,
	request text NULL,
	response text NULL,
	data_ins timestamp(0) NOT NULL,
	data_mod timestamp(0) NULL,
	CONSTRAINT l_chiamate_servizi_pk PRIMARY KEY (id),
	CONSTRAINT dmass_l_chiamate_servizi_fk FOREIGN KEY (id_ser) REFERENCES dmass.dmass_l_servizi(id_ser)
);

CREATE TABLE dmass.dmass_l_errori_servizi (
	id_err int8 NOT NULL,
	id_ser int8 NOT NULL,
	cod_errore varchar NOT NULL,
	descr_errore varchar NOT NULL,
	tipo_errore varchar NULL,
	info_aggiuntive text NULL,
	data_ins timestamp(0) NOT NULL,
	CONSTRAINT dmass_l_errori_servizi_pk PRIMARY KEY (id_err),
	CONSTRAINT dmass_l_errori_servizi_fk FOREIGN KEY (id_ser) REFERENCES dmass.dmass_l_servizi(id_ser)
);


CREATE TABLE dmass.dmass_t_audit (
	id int8 NOT NULL,
	cod_audit varchar NOT NULL,
	id_transazione varchar NULL,
	cf_utente varchar NOT NULL,
	ruolo_utente varchar NULL,
	regime varchar NULL,
	cf_assistito varchar NULL,
	applicazione varchar NULL,
	appl_verticale varchar NULL,
	ip varchar NULL,
	id_collocazione int8 NULL,
	codice_servizio varchar NULL,
	data_ins timestamp(0) NULL,
	CONSTRAINT dmass_t_audit_pk PRIMARY KEY (id)
);

CREATE TABLE dmass_d_ruolo (
	id int8 NOT NULL,
	codice_ruolo varchar(10) NOT NULL,
	descrizione_ruolo varchar(150) NOT NULL,
	data_inserimento timestamp NOT NULL,
	flag_visibile_per_consenso varchar(1) NULL,
	dataaggiornamento timestamp NULL,
	codice_ruolo_ini varchar(10) NULL,
	descrizione_ruolo_ini varchar(150) NULL,
	ruolo_dpcm varchar(150) NULL,
	categoria_ruolo varchar(50) NULL,
	CONSTRAINT pk_dmacc_d_ruolo PRIMARY KEY (id),
	CONSTRAINT uk_dmacc_d_ruolo_01 UNIQUE (codice_ruolo)
);
CREATE INDEX ie_dmass_d_ruolo_01 ON dmass_d_ruolo USING btree (codice_ruolo, flag_visibile_per_consenso);

INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(1, 'CIT', 'Assistito', '2018-11-12 00:00:00.000', 'S', NULL, 'ASS', 'Assistito', 'Assistito', 'Assistito');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(2, 'MMG', 'Medico di Medicina Generale', '2012-03-16 18:39:31.251', 'S', NULL, 'APR', 'Medico di Medicina Generale /pediatra di Libera scelta', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(3, 'PLS', 'pediatra di Libera scelta', '2012-03-16 18:32:16.689', 'S', NULL, 'APR', 'Medico di Medicina Generale /pediatra di Libera scelta', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(4, 'INF', 'Personale infermieristico', '2012-03-16 18:32:16.708', 'S', NULL, 'INF', 'Personale infermieristico', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(5, 'MEDOSP', 'Dirigente Sanitario', '2018-03-07 00:00:00.000', 'S', NULL, 'DRS', 'Dirigente Sanitario', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(6, 'MEDRSA', 'Medico RSA', '2012-03-16 18:32:16.796', 'S', NULL, 'RSA', 'Medico RSA', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(7, 'MEDRP', 'Medico Rete di Patologia', '2015-03-09 00:00:00.000', 'S', NULL, 'MRP', 'Medico Rete di Patologia', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(8, 'IMPAMMAS', 'Operatore Amministrativo', '2018-02-27 00:00:00.000', 'S', NULL, 'OAM', 'Operatore Amministrativo', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(9, 'IMPAMMSR', 'Operatore ROL', '2015-02-06 00:00:00.000', 'N', NULL, 'OAM', 'Operatore ROL', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(10, 'CCMED', 'Mediator CC', '2013-01-17 00:00:00.000', 'N', NULL, NULL, NULL, NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(16, 'NAZ', 'Ruolo unico regionale che viene sostituito al ruolo INI/AgID inviato nelle richieste dei servizi (PI)', '2018-10-19 11:58:22.510', 'N', NULL, NULL, NULL, NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(22, 'DEL', 'Delegato', '2020-01-10 13:45:04.883', 'S', NULL, 'TUT', 'Tutore', 'Assistito', 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(50, 'FSE', 'Applicazione FSE', '2012-03-16 18:32:16.878', 'N', NULL, NULL, NULL, NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(51, 'ROPVA', 'Applicazione ROPVA', '2012-03-16 18:32:16.899', 'N', NULL, NULL, NULL, NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(52, 'IMR', 'Applicazione IMR', '2012-03-16 18:32:16.922', 'N', NULL, NULL, NULL, NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(53, 'ROPVABatch', 'ROPVABatch', '2012-02-16 00:00:00.000', 'N', NULL, NULL, NULL, NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(54, 'FAR', 'Farmacista', '2014-04-11 15:04:46.731', 'S', NULL, 'FAR', 'Farmacista', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(55, 'OPSOCSA', 'Professionista del sociale', '2014-04-11 15:04:50.058', 'S', NULL, 'PSS', 'Professionista del sociale', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(57, 'AAS', 'Personale di assistenza ad alta specializzazione (Medico)', '2014-04-11 15:04:50.058', 'S', NULL, 'AAS', 'Personale di assistenza ad alta specializzazione (Medico)', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(59, 'DAM', 'Direttore Amministrativo', '2014-04-11 15:04:50.058', 'S', NULL, 'DAM', 'Direttore Amministrativo', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(60, 'TUT', 'Tutore', '2014-04-11 15:04:50.058', 'N', NULL, 'TUT', 'Tutore', 'Assistito', 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(61, 'NOR', 'Nodo Regionale', '2014-04-11 15:04:50.058', 'N', NULL, 'NOR', 'Nodo Regionale', NULL, 'Sistema');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(62, 'INI', 'Infrastruttura Nazionale Interoperabilità', '2014-04-11 15:04:50.058', 'N', NULL, 'INI', 'Infrastruttura Nazionale Interoperabilità', NULL, 'Sistema');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(63, 'OPI', 'Operatore Informativa', '2014-04-11 15:04:50.058', 'S', NULL, 'OPI', 'Operatore Informativa', NULL, 'Sistema');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(64, 'ING', 'Informal giver', '2000-01-01 00:00:00.000', 'S', NULL, 'ING', 'Informal giver', 'Assistito', 'Sistema');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(65, 'GEN', 'Genitore', '2000-01-01 00:00:00.000', 'N', NULL, 'GEN', 'Genitore', 'Assistito', 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(66, 'OGC', 'Operatore per la gestione dei consensi', '2000-01-01 00:00:00.000', 'N', NULL, 'OGC', 'Operatore per la gestione dei consensi', NULL, 'Sistema');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(88, 'MEDAPRFSE', 'Medico per apertura ROL', '2018-02-27 00:00:00.000', 'N', NULL, '', 'Dirigente Sanitario', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(89, 'MEDOSP2', 'Dirigente Sanitario - Gestione Consensi', '2018-03-19 00:00:00.000', 'N', '2019-03-19 00:00:00.000', '', 'Dirigente Sanitario', NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(90, 'MEDRSA2', 'Medico RSA - Gestione Consensi', '2018-03-19 00:00:00.000', 'N', '2019-03-19 00:00:00.000', 'RSA', 'Medico RSA', NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(91, 'MEDRP2', 'Medico Rete di Patologia - Gestione Consensi', '2018-03-19 00:00:00.000', 'N', '2019-03-19 00:00:00.000', 'MRP', 'Medico Rete di Patologia', NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(92, 'FAR2', 'Farmacista - Gestione Consensi', '2018-03-19 00:00:00.000', 'N', '2019-03-19 00:00:00.000', 'FAR', 'Farmacista', NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(93, 'AAS2', 'Personale di ass ad alta speciali - Gestione Consensi', '2018-03-19 00:00:00.000', 'N', '2019-03-19 00:00:00.000', 'AAS', 'Personale di assist ad alta special', NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(94, 'DAM2', 'Direttore Amministrativo - Gestione Consensi', '2018-03-19 00:00:00.000', 'N', '2019-03-19 00:00:00.000', 'DAM', 'Direttore Amministrativo', NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(95, 'MMG2', 'Medico di Medicina Generale /PdL - Gestione Consensi', '2018-03-19 00:00:00.000', 'N', '2019-03-19 00:00:00.000', 'APR', 'Medico di Medicina generale', NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(100, 'DSA', 'Direttore sanitario', '2018-11-12 00:00:00.000', 'S', NULL, 'DSA', 'pippo', NULL, 'Autenticazione');
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(101, 'MDS', 'Ministero della Salute', '2018-11-12 00:00:00.000', 'N', NULL, 'MDS', 'MDS', NULL, NULL);
INSERT INTO dmass_d_ruolo
(id, codice_ruolo, descrizione_ruolo, data_inserimento, flag_visibile_per_consenso, dataaggiornamento, codice_ruolo_ini, descrizione_ruolo_ini, ruolo_dpcm, categoria_ruolo)
VALUES(102, 'XXX', 'Ruolo non definito per Gestione Pazienti', '2018-11-12 00:00:00.000', 'N', NULL, NULL, NULL, NULL, 'Sistema');

CREATE SEQUENCE dmass.seq_dmass_l_chiamate_servizi_xml
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

CREATE SEQUENCE dmass.seq_dmass_l_errori_servizi_xml
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

CREATE SEQUENCE dmass.seq_dmass_l_servizi
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;
	
CREATE SEQUENCE dmass.seq_dmass_t_audit
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

--su dmacc
ALTER TABLE dmacc_rti.richiesta_scarico ADD datacreazionepac timestamp NULL;
-- da mettere la descrizione
INSERT INTO dmacc_rti.dmacc_d_catalogo_log
(id, codice, descrizione_errore, data_inserimento, descrizione_log, descrizione_codice, flg_errore, flg_ccbo, cod_fse)
VALUES(nextval('seq_dmacc_d_catalogo_log'), 'CC_ERR_300', 'CC_ERR_300', current_timestamp, 'CC_ERR_300', NULL, 'S', 'N', NULL);

INSERT INTO dmacc_rti.dmacc_d_catalogo_log
(id, codice, descrizione_errore, data_inserimento, descrizione_log, descrizione_codice, flg_errore, flg_ccbo, cod_fse)
VALUES(nextval('seq_dmacc_d_catalogo_log'), 'FSE_ER_558', 'Utente non abilitato alla consultazione del FSE’', current_timestamp, 'Utente non presente in t_utente o con ruolo non presente in r_utemte_ruolo', NULL, 'S', 'N', NULL);
INSERT INTO dmacc_rti.dmacc_d_catalogo_log
(id, codice, descrizione_errore, data_inserimento, descrizione_log, descrizione_codice, flg_errore, flg_ccbo, cod_fse)
VALUES(nextval('seq_dmacc_d_catalogo_log'), 'FSE_ER_559', 'La richiesta prenotazione immagine è già presente ', current_timestamp, 'La richiesta prenotazione immagine è già presente ', NULL, 'S', 'N', NULL);
INSERT INTO dmacc_rti.dmacc_d_catalogo_log
(id, codice, descrizione_errore, data_inserimento, descrizione_log, descrizione_codice, flg_errore, flg_ccbo, cod_fse)
VALUES(nextval('seq_dmacc_d_catalogo_log'), 'FSE_ER_560', 'Non è stato possibile aggiornare lo stato delpacchetto immagini a ''cancellato''', current_timestamp, 'Non è stato possibile aggiornare lo stato delpacchetto immagini a ''cancellato''', NULL, 'S', 'N', NULL);
INSERT INTO dmacc_rti.dmacc_d_catalogo_log
(id, codice, descrizione_errore, data_inserimento, descrizione_log, descrizione_codice, flg_errore, flg_ccbo, cod_fse)
VALUES(nextval('seq_dmacc_d_catalogo_log'), 'FSE_ER_561', 'Non è stato possibile leggere i pacchetti scaduti', current_timestamp, 'Non è stato possibile leggere i pacchetti scaduti', NULL, 'S', 'N', NULL);

--Batchg Cancellazione Pacchetto
CREATE TABLE dmass.dmass_l_servizi_batch (
	id_ser int8 NOT NULL,
	nome_servizio varchar NULL,
	data_inizio timestamp(0) NULL,
	data_fine timestamp(0) NULL,
	stato_fine varchar NULL,
	CONSTRAINT dmass_l_servizi_batch_pk PRIMARY KEY (id_ser)
);

CREATE TABLE dmass.dmass_l_servizi_batch_info (
	id_info int8 NOT NULL,
	id_ser int8 NULL,
	info text NULL,
	info_dettaglio text NULL,
	tipo_info varchar NULL,
	data_ins timestamp(0) NULL,
	CONSTRAINT dmass_l_servizi_batch_info_pk PRIMARY KEY (id_info)
);


-- dmass.dmass_l_servizi_batch_info foreign keys

ALTER TABLE dmass.dmass_l_servizi_batch_info ADD CONSTRAINT dmass_l_servizi_batch_info_fk FOREIGN KEY (id_ser) REFERENCES dmass_l_servizi_batch(id_ser);

CREATE TABLE dmass.dmass_t_lock_cancella_pacc (
	data_ins timestamp(0) NULL
);

CREATE SEQUENCE dmass.seq_dmass_l_servizio_batch_info
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;
	

CREATE SEQUENCE dmass.seq_dmass_l_servizio_batch
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

CREATE TABLE dmass.dmass_t_configurazione (
	"key" varchar NULL,
	value varchar NULL
);

INSERT INTO dmass.dmass_t_configurazione
("key", value)
VALUES('imr_cancella_pac_timelock', '45');

--su componente centrale
INSERT INTO dmacc_d_credenziali_servizi
(id, codice_servizio, username, "password", data_inizio_validita, data_fine_validita, data_inserimento)
VALUES(69, 'ScaricoStudi', 'dmauser', pgp_sym_encrypt('dmauser','mypass'), '2022-03-07 12:21:04.000', NULL, '2022-03-07 12:21:04.000');

