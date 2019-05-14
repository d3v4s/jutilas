package it.jutilas.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JutilasSys {
	private static JutilasSys jutilasSys;
	private final String RUN_PATH = System.getProperty("user.dir");
	private final String OS_NAME = System.getProperty("os.name").toLowerCase();
	private final String OS_ARCH = System.getProperty("os.arch").toLowerCase();
	private final String OS_USR = System.getProperty("user.name");
	private final String HOME_USR_PATH = isLinux() ? "/home/" + OS_USR :
										isMac() ? "/Users/" + OS_USR :
										isWindows() ? "C:\\Users\\" + OS_USR :"";

	private JutilasSys() {
	}

	/* singleton */
	public static JutilasSys getInstance() {
		return jutilasSys = jutilasSys == null ? new JutilasSys() : jutilasSys;
	}

	/* get */
	public String getRunPath() {
		return RUN_PATH;
	}
	public String getOsName() {
		return OS_NAME;
	}
	public String getOsArch() {
		return OS_ARCH;
	}
	public String getOsUser() {
		return OS_USR;
	}
	public String getUsrHomePath() {
		return HOME_USR_PATH;
	}

	/* metodo che ritorna la percentuale di carico sulla cpu in base al tempo inserito */
	public double getSystemLoadAdverage(int timeSleep) throws IOException {
		String regex;
		String[] cmnd;
		if (isWindows()) {
			regex = ".*[\\d]{1,}[.]{0,1}[\\d]{0,}.*";
			cmnd = new String[] {"wmic", "cpu", "get", "loadpercentage"};
		} else {
			regex = ".*cpu[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,}).*";
			cmnd = new String[] {"grep", "cpu ", "/proc/stat"};
		}
		int idle;
		int iowait;
		int user;
		int nice;
		int system;
		int irq;
		int softirq;
		int steal;
		int total;
		int prevTotal = 0;
		int totalDiff;
		int actv;
		int prevActv = 0;
		int actvDiff;
		ProcessBuilder pb;
		Process prcss;
		InputStreamReader isr;
		BufferedReader br;
		String stdo;
		for (int i = 0; i < 2; i++ ) {
			pb = new ProcessBuilder(cmnd);
			pb.redirectErrorStream(true);
			if (isWindows()) {
				try {
					Thread.sleep(timeSleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			prcss = pb.start();
			isr = new InputStreamReader(prcss.getInputStream());
			br = new BufferedReader(isr);
			stdo = null;
			while ((stdo = br.readLine()) != null) {
				if (isWindows()) {
					if (Pattern.matches(regex, stdo)) {
						br.close();
						return Double.valueOf(stdo);
					}
				} else {
					Matcher matcher = Pattern.compile(regex).matcher(stdo);
					if (matcher.find()) {
						user = Integer.valueOf(matcher.group(1));
						nice = Integer.valueOf(matcher.group(2));
						system = Integer.valueOf(matcher.group(3));
						idle = Integer.valueOf(matcher.group(4));
						iowait = Integer.valueOf(matcher.group(5));
						irq = Integer.valueOf(matcher.group(6));
						softirq = Integer.valueOf(matcher.group(7));
						steal = Integer.valueOf(matcher.group(8));
						
						actv = user + nice + system + irq + softirq + steal;
						total = actv + idle + iowait; 
						
						if (i == 0) {
							prevActv = actv;
							prevTotal = total;
						} else {
							totalDiff = (total - prevTotal);
							actvDiff = (actv - prevActv);
							
							br.close();
							return (100 * actvDiff)/totalDiff;
						}
						
					}
				}
			}
			br.close();
			try {
				Thread.sleep(timeSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	public boolean isLinux() {
		return OS_NAME.contains("linux") || OS_NAME.contains("nix") ? true : false;
	}

	public boolean isMac() {
		return OS_NAME.contains("mac") ? true : false;
	}

	public boolean isWindows() {
		return OS_NAME.contains("win") ? true : false;
	}
}
