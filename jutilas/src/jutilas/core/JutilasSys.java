package jutilas.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class with utils for the system
 * @author Andrea Serra
 *
 */
public class JutilasSys {
	private static JutilasSys jutilasSys;
	private final String RUN_PATH = System.getProperty("user.dir");
	private final String OS_NAME = System.getProperty("os.name").toLowerCase();
	private final String OS_ARCH = System.getProperty("os.arch").toLowerCase();
	private final String OS_USR = System.getProperty("user.name");
	private final String PATH_USR_HOME = isLinux() ? "/home/" + OS_USR :
										isMac() ? "/Users/" + OS_USR :
										isWindows() ? "C:\\Users\\" + OS_USR : "";
	private BufferedReader br;
	private int prevActv;
	private int prevTotal;
	private int i;

	/* CONTRUCTOR */
	private JutilasSys() {
	}

	/* SINGLETON */
	public static JutilasSys getInstance() {
		return jutilasSys = jutilasSys == null ? new JutilasSys() : jutilasSys;
	}

	/* ################################################################################# */
	/* START GET */
	/* ################################################################################# */

	/**
	 * method that get the run path
	 * @return run path
	 */
	public String getRunPath() {
		return RUN_PATH;
	}

	/**
	 * method that get the OS name
	 * @return OS name
	 */
	public String getOsName() {
		return OS_NAME;
	}

	/**
	 * method that get the OS architecture
	 * @return OS architecture
	 */
	public String getOsArch() {
		return OS_ARCH;
	}

	/**
	 * method that get name of user who run the application
	 * @return name of user
	 */
	public String getOsUser() {
		return OS_USR;
	}

	/**
	 * method that get the path of user home
	 * @return path of user home
	 */
	public String getPathUsrHome() {
		return PATH_USR_HOME;
	}

	/* ################################################################################# */
	/* END GET */
	/* ################################################################################# */

	/* metodo che ritorna la media di carico sulla cpu in base al tempo inserito */
	/**
	 * method that return the cpu load average, based on timesleep
	 * @param timeSleep time sleep
	 * @return cpu load average
	 * @throws IOException
	 */
	public double getSystemLoadAverage(int timeSleep) throws IOException {
		String regex;
		String[] cmnd;
		Function<String, Double> readLineFunction = null;
		/* preset for windows and linux */
		if (isWindows()) {
			/* regex for windows */
			regex = ".*[\\d]{1,}[.]{0,1}[\\d]{0,}.*";
			/* comand for windows */
			cmnd = new String[] {"wmic", "cpu", "get", "loadpercentage"};
			/* function to read output in windows */
			readLineFunction = new Function<String, Double>() {
				@Override
				public Double apply(String stdo) {
					if (Pattern.matches(regex, stdo)) {
						try {
							br.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						return Double.valueOf(stdo);
					}
					return null;
				}
				
			};
		} else {
			/* regex for linux */
			regex = ".*cpu[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,}).*";
			/* command for linux */
			cmnd = new String[] {"grep", "cpu ", "/proc/stat"};
			/* function to read output and calculate the load average in linux */ 
			readLineFunction = new Function<String, Double>() {
				@Override
				public Double apply(String stdo) {
					Matcher matcher = Pattern.compile(regex).matcher(stdo);
					if (matcher.find()) {
						int user = Integer.valueOf(matcher.group(1));
						int nice = Integer.valueOf(matcher.group(2));
						int system = Integer.valueOf(matcher.group(3));
						int idle = Integer.valueOf(matcher.group(4));
						int iowait = Integer.valueOf(matcher.group(5));
						int irq = Integer.valueOf(matcher.group(6));
						int softirq = Integer.valueOf(matcher.group(7));
						int steal = Integer.valueOf(matcher.group(8));
						
						int actv = user + nice + system + irq + softirq + steal;
						int total = actv + idle + iowait; 
						
						if (i == 0) {
							prevActv = actv;
							prevTotal = total;
						} else {
							int totalDiff = (total - prevTotal);
							int actvDiff = (actv - prevActv);
							
							try {
								br.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							return Double.valueOf((100 * actvDiff)/totalDiff);
						}
						
					}
					return null;
				}
			};
		}

		/* start process */
		ProcessBuilder pb;
		Process prcss;
		InputStreamReader isr;
		String stdo;
		for (; i < 2; i++ ) {
			try {
				Thread.sleep(timeSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			pb = new ProcessBuilder(cmnd);
			pb.redirectErrorStream(true);
			prcss = pb.start();
			isr = new InputStreamReader(prcss.getInputStream());
			br = new BufferedReader(isr);
			stdo = null;
			while ((stdo = br.readLine()) != null) {
				Double ret = readLineFunction.apply(stdo);
				if (ret != null) return ret;
			}
			br.close();
		}
		return -1;
	}

	/**
	 * method that check if OS is GNU/Linux or Unix-like
	 * @return true if is GNU/Linux or Unix-like, else false
	 */
	public boolean isLinux() {
		return OS_NAME.contains("linux") || OS_NAME.contains("nix") ? true : false;
	}

	/**
	 * method that check if OS is Mac
	 * @return true if is Mac, else false
	 */
	public boolean isMac() {
		return OS_NAME.contains("mac") ? true : false;
	}

	/**
	 * method that check if OS is Winzozz
	 * @return true if is Winzzoz, else false
	 */
	public boolean isWindows() {
		return OS_NAME.contains("win") ? true : false;
	}
}
