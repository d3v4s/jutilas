package jutilas.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jutilas.interfaces.JutilasFunction;

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
	private String regex;
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

	/* ################################################################################# */
	/* START OS TYPE */
	/* ################################################################################# */

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

	/* ################################################################################# */
	/* END OS TYPE */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START OTHER METHODS */
	/* ################################################################################# */

	/**
	 * method that get a string with the system info
	 * @param sysAvrg cpu load average
	 * @return system info
	 */
	public String getSystemInfo(Double sysAvrg) {
		/* if average is null try to get it */
		if (sysAvrg == null) {
			try {
				sysAvrg = JutilasSys.getInstance().getSystemLoadAverage(1000);
			} catch (IOException e) {
				e.printStackTrace();
				sysAvrg = -1d;
			}
		}

		/* build string with info */
		String sysInfo = MessageFormat.format("OS: {0}\nArch: {1}\nUser: {2}\nCPU: {3}%",
				JutilasSys.getInstance().getOsName(),
				JutilasSys.getInstance().getOsArch(),
				JutilasSys.getInstance().getOsUser(),
				String.format("%.0f", sysAvrg)
		);

		return sysInfo;
	}

	/* metodo che ritorna la media di carico sulla cpu in base al tempo inserito */
	/**
	 * method that return the cpu load average, based on timesleep
	 * @param timeSleep time sleep
	 * @return cpu load average
	 * @throws IOException
	 */
	public double getSystemLoadAverage(int timeSleep) throws IOException {
		/* LINUX PRESET */
		/* regex and command for linux */
		regex = ".*cpu[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,})[\\s]{1,}([\\d]{1,}).*";
		String[] cmnd = new String[] {"grep", "cpu ", "/proc/stat"};
		/* function to read output and calculate the load average in linux */
		Function<String, Double> readLineFunction = new Function<String, Double>() {
			@Override
			public Double apply(String stdo) {
				Matcher matcher = Pattern.compile(regex).matcher(stdo);
				if (matcher.find()) {
					/* get memory info */
					int user = Integer.valueOf(matcher.group(1));
					int nice = Integer.valueOf(matcher.group(2));
					int system = Integer.valueOf(matcher.group(3));
					int idle = Integer.valueOf(matcher.group(4));
					int iowait = Integer.valueOf(matcher.group(5));
					int irq = Integer.valueOf(matcher.group(6));
					int softirq = Integer.valueOf(matcher.group(7));
					int steal = Integer.valueOf(matcher.group(8));

					/* calculate active and total */
					int actv = user + nice + system + irq + softirq + steal;
					int total = actv + idle + iowait; 

					/* if is second iteration calculate load average and return it */
					if (i == 1) {
						int totalDiff = (total - prevTotal);
						int actvDiff = (actv - prevActv);
						return Double.valueOf((100 * actvDiff)/totalDiff);
					}

					/* save activate and total calculate */
					prevActv = actv;
					prevTotal = total;

					/* sleep */
					try {
						Thread.sleep(timeSleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		};
		/* function to make the sleep for windows */
		/* not used for linux */
		JutilasFunction windowsSleep = () -> {};

		/* WINDOWS PRESET */
		/* if is windows change preset */
		if (isWindows()) {
			/* regex and command for windows */
			regex = ".*[\\d]{1,}[.]{0,1}[\\d]{0,}.*";
			cmnd = new String[] {"wmic", "cpu", "get", "loadpercentage"};
			/* function to read output in windows */
			readLineFunction = new Function<String, Double>() {
				@Override
				public Double apply(String stdo) {
					if (Pattern.matches(regex, stdo)) return Double.valueOf(stdo);
					return null;
				}
			};
			/* function to windows sleep */
			windowsSleep = () -> {
				try {
					Thread.sleep(timeSleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}

		/* start process */
		ProcessBuilder pb;
		Process prcss;
		InputStreamReader isr;
		BufferedReader br;
		String stdo;
		Double loadAverage;
		for (i = 0; i < 2; i++) {
			windowsSleep.execute();
			pb = new ProcessBuilder(cmnd);
			pb.redirectErrorStream(true);
			prcss = pb.start();
			isr = new InputStreamReader(prcss.getInputStream());
			br = new BufferedReader(isr);
			while ((stdo = br.readLine()) != null) {
				loadAverage = readLineFunction.apply(stdo);
				if (loadAverage != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return loadAverage;
				}
			}
			br.close();
		}
		return -1;
	}

	/* ################################################################################# */
	/* END OTHER METHODS */
	/* ################################################################################# */

}
