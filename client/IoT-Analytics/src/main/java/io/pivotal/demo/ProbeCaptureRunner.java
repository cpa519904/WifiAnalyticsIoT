package io.pivotal.demo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

//@ComponentScan
public class ProbeCaptureRunner{ // implements CommandLineRunner {

	private GeodeClient client = new GeodeClient();
	
	Logger logger = Logger.getLogger(ProbeCaptureRunner.class.getName());

	public void run(String... args) throws Exception {
		
				
		logger.info("--------------------------------------");
				
		logger.info("Capturing tshark process output...");
		
	 	Process tshark = Runtime.getRuntime().exec("sudo tshark -i wlan1mon -I -l -f broadcast -R wlan.fc.subtype==4 -T fields -e frame.time_epoch -e wlan.sa -e radiotap.dbm_antsignal");				
		try{
			if (!tshark.isAlive()){
				
				logger.severe("Process exited with code "+tshark.exitValue());	
				logger.severe(new BufferedReader(new InputStreamReader(tshark.getInputStream())).readLine());
				
				BufferedReader errorStream = new BufferedReader(new InputStreamReader(tshark.getErrorStream()));
				String errorLine = null;
				while ((errorLine = errorStream.readLine())!=null){
					logger.severe(errorLine);
				}						
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(tshark.getInputStream()));
			while (tshark.isAlive()){
				String line = br.readLine();
				processLine(line);			
			}
			br.close();
			tshark.waitFor();
		}
		finally{
			if (tshark.isAlive()) tshark.destroyForcibly();
		}	
		
	}
	protected void processLine(String line){
		StringTokenizer st = new StringTokenizer(line);
		String timeepoch = st.nextToken();
		String deviceId = st.nextToken();
		int signal_dbm = Integer.parseInt(st.nextToken());
		
		ProbeRequest req = new ProbeRequest(timeepoch,deviceId,signal_dbm);
		client.putProbeReq(req);
	}

}
