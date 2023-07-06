/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import dao.PdiRetrieveManager;
import helpers.ConfigurationSettings;

public class CommandUtils {
	
	private static final Logger log = Logger.getLogger(CommandUtils.class);
	
	/**
	 * Trasferisce il file ZIP all'ASL di riferimento.
	 * @param zipDirectory la directory contenente il file da trasferire.
	 * @param fileName il file da trasferire.
	 * @param dist la directory dove trasferire il file.
	 */
	public static boolean execCommandMove(String zipDirectory, String fileName, String dist, String command, String numRetries) {
		StreamUtils.makeDir(dist, false);
		
		log.info("Executing command move...");
		PdiRetrieveManager 		prm 					= new PdiRetrieveManager();
		boolean 				commandResult 			= false;
		Boolean					isCopyStandardActivate 	= false;
		String					isCopyStandardActivateStr	= prm.getConfigParamPDI(ConfigurationSettings.IS_COPY_STANDARD_ACTIVATE);
		isCopyStandardActivate	= isCopyStandardActivateStr != null && !isCopyStandardActivateStr.equals("") ? Boolean.valueOf(isCopyStandardActivateStr) : false;
		String					from					= zipDirectory + fileName + ".zip";
		String					to						= dist + fileName + ".zip";
		
		
		if(isCopyStandardActivate) {
			log.info("Copy standard is activate for copy viewer from " + from + " to " + to + "...");
			try {
				File fFrom 	= new File(from);
				File fTo	= new File(to);
				if(fFrom != null && fFrom.exists() && fFrom.isFile()) {
					FileUtils.copyFile(fFrom, fTo);
					if(fTo != null && fTo.exists() && fTo.isFile()) {
						commandResult	= true;
					}
				}
				
				StreamUtils.cleanLocalFile(from);
				StreamUtils.cleanLocalDirectory(new File(zipDirectory + fileName));
				
			} catch (IOException e) {
				log.error("Cannot execute copy viewer due to: " + e.getMessage(), e);
			}
		
		} else {
			String zippedTo = zipDirectory + fileName + ".zip";
			String commandMove = buildCommandMove(command, zipDirectory + fileName + ".zip", dist + fileName + ".zip");
			int retries = Integer.parseInt(numRetries);
			for (int retry = 1; retry <= retries; retry++) {
				Runtime rt = Runtime.getRuntime();
				Process pr = null;
				int codeOfExit = -1;
				try {
					pr = rt.exec(commandMove);
					codeOfExit = pr.waitFor();
					if (codeOfExit == 0) {
						log.info("Command move succeeded");
						commandResult = true;
						break;
					}
				} catch (Exception e) {					 
					throw new RuntimeException(e);
				} finally {
					if (codeOfExit != 0) {
						if (retry == retries) {
							log.info("Command move failed");
							commandResult = false;
						}
					}
					
					if (pr != null) {
						pr.destroy();
					}
				}
			}
			
			if(commandResult) {
				StreamUtils.cleanLocalFile(zippedTo);
				StreamUtils.cleanLocalDirectory(new File(zipDirectory + fileName));
			}
		}
	
		
		
		return commandResult;
	}

	/**
	 * Fornisce il comando da eseguire per la movimnetazione del file ZIP.
	 * @param commandMove il template del comando
	 * @param from directory sorgente
	 * @param to directory destinazione
	 * @return il comando da eseguire per la movimnetazione del file ZIP
	 */
	private static String buildCommandMove(String commandMove, String from, String to) {
		String commandMoveToExecute = commandMove.replace(ConfigurationSettings.FILE_TO_MOVE_TEMPLATE, from);
		commandMoveToExecute = commandMoveToExecute.replace(ConfigurationSettings.DIST_TEMPLATE, to);
		log.info("Command to execute for make move: " + commandMoveToExecute);
		
		return commandMoveToExecute;
	}
	
	/**
	 * Copia il contenuto del viewer
	 * @param from directory da cui copiare il viewer
	 * @param to directory dove copiare il viewer
	 * @param template template per il comando
	 * @param numRetries numero di tentativi
	 * @return true se la copia va a buon fine, false altrimenti
	 */
	public static boolean execCommandViewer(String from, String to, String template, String numRetries) {
		log.info("Executing command for viewer from " + from + " to " + to + "...");
		PdiRetrieveManager 		prm 					= new PdiRetrieveManager();
		boolean 				commandResult 			= false;
		Boolean					isCopyStandardActivate 	= false;
		Runtime rt 		= 		Runtime.getRuntime();
		Process pr 		= 		null;
		
		int count		= 		0;
		String					isCopyStandardActivateStr	= prm.getConfigParamPDI(ConfigurationSettings.IS_COPY_STANDARD_ACTIVATE);
		isCopyStandardActivate	= isCopyStandardActivateStr != null && !isCopyStandardActivateStr.equals("") ? Boolean.valueOf(isCopyStandardActivateStr) : false;
		
		if(StringUtils.isEmpty(from)) {
			log.info("from is empty...");
			int codeOfExitReadme 	= 		0;
			do {
				log.info("count: " + count);
				String command = buildCommandViewer(prm.getConfigParamPDI("Readme"), to, template);
				log.info("Command for RADME: " + command);
				try {
					pr = rt.exec(command);
					log.info("README spostato");
					codeOfExitReadme = pr.waitFor();
					log.info("codeOfExitReadme = " + codeOfExitReadme);
					if (codeOfExitReadme == 0) {
						log.info("Command for README succeeded");
						commandResult = true;
						break;
					} else {
						log.info("Command for README not succeeded");
						
						InputStream is = pr.getErrorStream();
						String error = is != null ? IOUtils.toString(is, "UTF-8") : "Non ci sono errori"; 
						 
						log.info("Error for command README:" + error + "codeOfExitReadme: " + codeOfExitReadme);
					}
					
					
				} catch (Exception e) {	
					log.error("README non spostato, errore:" + e.getMessage(), e);
				} finally {
					if (codeOfExitReadme != 0) {
						log.error("README non spostato");
						if (count > 10) {
							log.info("Command for README failed");
							commandResult = true;
						} else {
							log.info("Retrying command for README...");
							commandResult = false;
						}
					}
					
					if (pr != null) {
						pr.destroy();
					}
					count++;
				}
			} while(!commandResult);
			
			return true;
		}
		
		if(isCopyStandardActivate) {
			log.info("Copy standard is activate for copy viewer from " + from + " to " + to + "...");
			try {
				int pos	= from.lastIndexOf(File.separator);
				String nameOfViewer = from.substring(pos+1);
				File fFrom 	= new File(from);
				File fTo	= new File(to+File.separator+nameOfViewer);
				if(fFrom != null && fFrom.exists() && fFrom.isFile()) {
					FileUtils.copyFile(fFrom, fTo);
					if(fTo != null && fTo.exists() && fTo.isFile()) {
						commandResult	= true;
						ZipUtils.unzip(from, to);
						StreamUtils.cleanLocalFile(fTo.getAbsolutePath());
					}
				}
				
			} catch (IOException e) {
				log.error("Cannot execute copy viewer due to: " + e.getMessage(), e);
			}
		
		} else {
			
			String fileName = from.substring(from.lastIndexOf("/"));
			String command = buildCommandViewer(from, to, template);
			int retries = Integer.parseInt(numRetries);
			for (int retry = 1; retry <= retries; retry++) {
				
				int codeOfExit = -1;
				try {
					pr = rt.exec(command);
					codeOfExit = pr.waitFor();
					if (codeOfExit == 0) {
						log.info("Command for viewer succeeded");
						commandResult = true;
						break;
					}
				} catch (Exception e) {	
					log.error("Cannot execute command for viewer due to: " + e.getMessage(), e);
				} finally {
					if (codeOfExit != 0) {
						if (retry == retries) {
							log.info("Command for viewer failed");
							commandResult = false;
						} else {
							log.info("Retrying command for viewer...");
						}
					}
					
					if (pr != null) {
						pr.destroy();
					}
				}
			}
			
			if (!commandResult) {
				log.info("Trying to execute command for viewer by file...");
				commandResult = execCommandViewerByFile(command, to, retries);
				if (!commandResult) {
					log.info("Trying to execute command for viewer by process builder...");
					commandResult = execCommandViewerByProcessBuilder(retries, to, from);
				}
			}
			
			if (commandResult) {
				try {
					log.info("Unzipping viewer...");
					ZipUtils.unzip(from, to);
					StreamUtils.cleanLocalFile(to + fileName);
					log.info("Viewer unzipped and cleaned successfully");
					
					command = buildCommandViewer(prm.getConfigParamPDI("Readme"), to, template);
				
					int codeOfExit = -1;
					try {
						pr = rt.exec(command);
						codeOfExit = pr.waitFor();
						if (codeOfExit == 0) {
							log.info("****Command for README succeeded******");
							commandResult = true;
							
						}
					} catch (Exception e) {	
						log.error("*****Cannot execute command for README due to: " + e.getMessage(), e);
					} finally {
						if (pr != null) {
							pr.destroy();
						}
					}
					//StreamUtils.copyFile(prm.getConfigParamPDI("Assets") + "README.txt", to + "/"+ "README.txt");
				} catch (IOException e) {
					log.info("Cannot unzip viewer due to: " + e.getMessage(), e);
					return false;
				}
			} else {
				log.info("All tries of command for viewer failed");
			}
			
			log.info("Execution for command viewer has been terminated");
			
			
		}
		
		return commandResult;
	}
	
	/**
	 * Copia il contenuto del viewer usando un process builder
	 * @param from directory da cui copiare il viewer
	 * @param to directory dove copiare il viewer
	 * @param retries numero di tentativi
	 * @return true se la copia va a buon fine, false altrimenti
	 */
	private static boolean execCommandViewerByProcessBuilder(int retries, String to, String from) {
		String pbDir = from.substring(0, from.lastIndexOf("/"));
		String pbFile = from.substring(from.lastIndexOf("/") + 1);
		log.info("pbDir: " + pbDir + ", pbFile: " + pbFile);
		
		ProcessBuilder pb = new ProcessBuilder("scp", "viewer.zip", to);
		pb.directory(new File(pbDir));
		
		boolean commandResult = false;
		for (int retry = 1; retry <= retries; retry++) {
			int codeOfExit = -1;
			Process p = null;
			try {
				p = pb.start();
				codeOfExit = p.waitFor();
				if (codeOfExit == 0) {
					log.info("Command for viewer process builder succeeded");
					commandResult = true;
				}
				
				break;
			} catch (Exception e) {	
				log.error("Cannot execute command for viewer by process builder due to: " + e.getMessage(), e);
			} finally {
				if (codeOfExit != 0) {
					if (retry == retries) {
						log.info("Command for viewer process builder failed");
						commandResult = false;
					} else {
						log.info("Retrying command for viewer process builder...");
					}
				}
				
				if (p != null) {
					p.destroy();
				}
			}
		}
		
		return commandResult;
	}

	/**
	 * Copia il contenuto del viewer eseguendo uno script sulla base del SO.
	 * @param command comando da eseguire
	 * @param to directory dove copiare il viewer
	 * @param retries numero di tentativi
	 * @return true se la copia va a buon fine, false altrimenti
	 */
	private static boolean execCommandViewerByFile(String command, String to, int retries) {
		String scpFile = buildScpFile(to, command);
		if (scpFile == null || scpFile.isEmpty()) {
			log.info("Command for viewer by file failed");
			return false;
		}
		
		boolean commandResult = false;
		for (int retry = 1; retry <= retries; retry++) {
			Runtime rt = Runtime.getRuntime();
			Process pr = null;
			int codeOfExit = -1;
			try {
				pr = rt.exec(scpFile);
				codeOfExit = pr.waitFor();
				if (codeOfExit == 0) {
					log.info("Command for viewer by file succeeded");
					StreamUtils.cleanLocalFile(scpFile);
					commandResult = true;
				}
				
				break;
			} catch (Exception e) {	
				log.error("Cannot execute command for viewer by file due to: " + e.getMessage(), e);
			} finally {
				if (codeOfExit != 0) {
					if (retry == retries) {
						log.info("Command for viewer by file failed");
						commandResult = false;
					} else {
						log.info("Retrying command for viewer by file...");
					}
				}
				
				if (pr != null) {
					pr.destroy();
				}
			}
		}
		
		return commandResult;
	}

	/**
	 * Fornisce il file di script da lanciare
	 * @param command comando da eseguire
	 * @param to directory dove copiare il viewer
	 * @return il percorso del file di script creato
	 */
	private static String buildScpFile(String to, String command) {
		log.info("Checking SO...");
		try {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("win")) {
				log.info("OS Windows: " + os);
				String path = to + "/script.bat";
				File script = new File(path);
				BufferedWriter writer = new BufferedWriter(new FileWriter(script));
			    writer.write(command);
			    writer.close();
			    
			    return path;
			} else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
				log.info("OS Unix: " + os);
				String path = to + "/script.sh";
				File script = new File(path);
				BufferedWriter writer = new BufferedWriter(new FileWriter(script));
			    writer.write(command);
			    writer.close();
			    
			    return path;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Fornisce il comando da eseguire per copiare il file del viewer
	 * @param path path del file da copiare
	 * @param to directory dove copiare il viewer
	 * @param template template per il comando
	 * @return il comando da eseguire per copiare il file
	 */
	private static String buildCommandViewer(String path, String to, String template) {
		String command = template.replace(ConfigurationSettings.FROM_TEMPLATE, path);
		command = command.replace(ConfigurationSettings.DIST_TEMPLATE, to);
		log.info("Command for viewer: " + command + "...");
		
		return command;
	}
}
