package it.pdi.executor;

import java.io.IOException;

public class ExecutorPDI {
	
	public static void main(String[] args) throws IOException {
		String 	command = null; 
		if(args != null && args.length > 0 ) {
			command = args[0];
		}
		
		if(command != null && command.trim().length() >= 0) {
		
			Runtime rt = Runtime.getRuntime();
			Process pr = null;
			int codeOfExit = -1;
			try {

				pr = rt.exec(command);
				codeOfExit = pr.waitFor();
				if (codeOfExit == 0) {
					System.out.println("Command for viewer succeeded");
				} else {
					System.out.println("Command for viewer not succeeded");
				}
				
			
			} catch (Exception e) {	
				System.out.println("Cannot execute command for viewer due to: " + e.getMessage());
				
			} finally {
				
				
				if (pr != null) {
					pr.destroy();
				}
			}
		}
		
		System.out.println("USCITA");
	}

}
