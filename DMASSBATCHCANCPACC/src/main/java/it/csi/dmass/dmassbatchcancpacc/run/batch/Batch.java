/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.run.batch;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.csi.dmass.dmassbatchcancpacc.env.Context;
import it.csi.dmass.dmassbatchcancpacc.logging.DmassBatchCancpaccLogger;
import it.csi.dmass.dmassbatchcancpacc.run.command.Command;

@Component
public class Batch {

	private Command[] commands;

	@Autowired
	DmassBatchCancpaccLogger dmassBatchCancpaccLogger;

	public void init() throws Exception {
		try {
			dmassBatchCancpaccLogger.info("inizio esecuzione batch");
			this.setUpExecution();
		} catch (Exception e) {
			dmassBatchCancpaccLogger.error(e);
			throw e;
		}
	}

	public void run() throws Exception {
		try {
			Command[] commands;
			// inizio attività
			for (int length = (commands = this.commands).length, i = 0; i < length-1; ++i) {
				final Command command = commands[i];
				this.execute(command);
			}
			// fine attività
		
		} catch (Exception e) {
			dmassBatchCancpaccLogger.error(e);
			throw e;
		} finally {
			//LogFineCancellazione
			commands[this.commands.length-1].execute();
			Context.setCurrentStep("");
			dmassBatchCancpaccLogger.info("fine esecuzione batch");
		}
	}

	private void execute(final Command command) throws Exception {
		Context.setCurrentStep(command.step());
		dmassBatchCancpaccLogger.info("inizio");
		command.execute();
		dmassBatchCancpaccLogger.info("fine");
	}

	private void setUpExecution() throws Exception {
		this.setUpCommands();
	}

	protected void setUpCommands() throws Exception {								
		
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document doc = builder.parse(ClassLoader.getSystemResourceAsStream("flow.xml"));
														
			final NodeList tasks = doc.getElementsByTagName("task");
			this.commands = new Command[tasks.getLength()];
			for (int j = 0; j < tasks.getLength(); ++j) {
				final Node task = tasks.item(j);
				final String className = task.getTextContent();
				this.commands[j] = (Command) Context.getBean(Class.forName(className));
			}
			return;							

		} catch (Exception ex) {
			dmassBatchCancpaccLogger.error(ex);
			throw ex;
		}
	}

}
