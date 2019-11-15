package jutilas.core;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;

import jutilas.exception.FileException;

/**
 * Class with utils method for application development
 * @author Andrea Serra
 *
 */
public class Jutilas {
	private static Jutilas jutilas;

	/* CONSTRUCTOR */
	private Jutilas() {
	}

	/* SINGLETON */
	public static Jutilas getInstance() {
		return (jutilas = (jutilas == null) ? new Jutilas() : jutilas);
	}


	/* metodo che ordina lista string in ignorando maiuscole e minuscole */
	/* method that order a list ignoring case */
	/**
	 * method that order a list ignoring case
	 * @param list to order
	 */
	public void sortIgnoreCase(ArrayList<String> list) {
		Collections.sort(list, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				String s1 = (String) o1;
	            String s2 = (String) o2;
	            return s1.toLowerCase().compareTo(s2.toLowerCase());
			}
		});
	}

	/* metodo per aprire il browser di default con l'url passato */
	/* method that open the default browser with URL */
	/**
	 * method that open the default browser with URL
	 * @param URL to open on browser
	 * @return true if success, else false
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public boolean openBrowser(String URL) throws IOException, URISyntaxException {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			desktop.browse(new URI(URL));
			return true;
		} else return false;
	}

	/* metodo per settare un' impostazione nel file di conf */
	/**
	 * method for set configuration on file
	 * @param filePath to save configuration
	 * @param param of configuration
	 * @param value of configuration
	 * @param head of configuration file
	 * @throws FileException
	 */
	public void setConf(String filePath, String param, String value, String head) throws FileException {
		Properties prop = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream(new File(filePath));
			prop.load(fis);
			fis.close();
			prop.setProperty(param, value);
			FileOutputStream fos = new FileOutputStream(new File(filePath));
			prop.store(fos, head);
		} catch (IOException e) {
			throw new FileException("Error!!! Impossible to work on file: " + filePath + "\nError message: " + e.getMessage());
		}
	}

	/* metodo che ritorna un' impostazione del file di conf */
	/**
	 * method that get configuration from file
	 * @param filePath of configuration file
	 * @param param of configuration
	 * @return value of configuration
	 * @throws FileException
	 */
	public String getConf(String filePath, String param) throws FileException {
		Properties prop = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream(new File(filePath));
			prop.load(fis);
			return prop.getProperty(param);
		} catch (IOException e) {
			throw new FileException("Error!!! Impossible to work on file: " + filePath + "\nError message: " + e.getMessage());
		}
	}

	/* metodo per eliminare file o directory con sub directory */
	/**
	 * method that delete file and directory recursive
	 * @param filePath of file to want delete
	 */
	public void recursiveDelete(String filePath) {
		File f = new File(filePath);
		/* se l'istanza del file e' un file o una directory vuota, viene eliminato */
		if (f.exists() && (f.isFile() || (f.isDirectory() && isEmptyDirectory(f)))) f.delete();
		/* invece se e' una directory che contiene file listiamo il file contenuti al suo interno */
		else if(f.isDirectory() && !isEmptyDirectory(f)) {
			File[] files = f.listFiles();
			for(File file : files) recursiveDelete(file.getPath());
			f.delete();
		}
	}
	
	/* metodo che ci dice se la directory e' vuota o contiene dei file */
	/**
	 * method that check if is empty directory
	 * @param file to check
	 * @return true if empty, else false
	 */
	public boolean isEmptyDirectory(File file) {
        if (file.isDirectory()) {
        	File[] files = file.listFiles();
        	if(!(files.length > 0)) return true;
        }
        return false;
	}

	/* metodo che ritorna le ultime righe di un file */
	/**
	 * method that get last rows of a file
	 * @param filePath of file
	 * @param numRows to get
	 * @return last rows of file
	 * @throws FileException
	 */
	public String getLastRowsFile(String filePath, int numRows) throws FileException {
		File file = new File(filePath);
		RandomAccessFile raf = null;
		if (!(file.exists() && file.isFile() && file.canRead())) throw new FileException("Errore!!! Impossible to work on file: " + filePath);
		try {
			raf = new RandomAccessFile(file, "r");
			long fileLenght = raf.length()-1;
			StringBuilder sb = new StringBuilder();
			int line = 0;
			for (long filePointer = fileLenght; filePointer != -1; filePointer--) {
				raf.seek(filePointer);
				int readByte = raf.readByte();

				if (readByte == 0xA) line = (filePointer < fileLenght) ? line + 1 : line;
				else if (readByte == 0xD) line = (filePointer < fileLenght-1) ? line + 1 : line;

				if (line >= numRows) break;
				
				sb.append((char) readByte);
			}
			if (raf != null) raf.close();

			return sb.reverse().toString();
		} catch (IOException e) {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e1) {
					throw new FileException("Errore!!! File: " + filePath + "\nError message: " + e.getMessage());
				}
			}
			throw new FileException("Errore!!! Impossible to work on file: " + filePath + "\nError message: " + e.getMessage());
		}
	}

	/* metodo per copiare file */
	/**
	 * method for copy file
	 * @param filePath of file to be copied
	 * @param toPath destination file path
	 * @param copyOptions list options
	 * @throws FileException
	 */
	public void copyFile(String filePath, String toPath, CopyOption... copyOptions) throws FileException {
		try {
			Files.copy(Paths.get(filePath), Paths.get(toPath), copyOptions);
		} catch (IOException e) {
			throw new FileException("Errore!!! Impossible to work on file: " + filePath + "\nError message: " + e.getMessage());
		}
	}

	/* metodo per rinominare file */
	/**
	 * method that renames a file
	 * @param filePath of file to be renamed
	 * @param newName of file
	 * @throws FileException
	 */
	public void renameFile(String filePath, String newName) throws FileException {
		Path file = Paths.get(filePath);
		if (!file.toFile().exists()) throw new FileException("Errore!!! \"" + filePath + "\" file does not exist.");
		String newPathFile = file.toString().replaceFirst(file.getFileName() + "$", newName);
		file.toFile().renameTo(new File(newPathFile));
	}

	/* metodo che restituisce stringa del contenuto del file */
	/**
	 * method that return the contents of file by file path
	 * @param filePath of file to be read
	 * @return string with the contents of file
	 * @throws FileException
	 */
	public String readFile(String filePath) throws FileException {
		return readFile(new File(filePath));
	}

	/* metodo che restituisce stringa del contenuto del file */
	/**
	 * method that return the contents of file by class File
	 * @param file to be read
	 * @return string with the contents of file
	 * @throws FileException
	 */
	public String readFile(File file) throws FileException {
		BufferedReader br = null;
		char[] textOut = null;
		try {
			br = new BufferedReader(new FileReader(file));
			textOut = new char[(int) file.length()];
			br.read(textOut);
		} catch (IOException e) {
			try {
				br.close();
			} catch (IOException e1) {
				throw new FileException("Impossible to work on file:\nError message: " + e1.getMessage());
			}
			throw new FileException("Impossible to work on file.\nError message: " + e.getMessage());
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				throw new FileException("Impossible to work on file.\nError message: " + e.getMessage());
			}
		}
		return String.valueOf(textOut); 
	}

	/* metodo che restituisce stringa del contenuto del file */
	/**
	 * method that return the contents of file with class RandomAccessFile
	 * @param file to be read
	 * @return string with the contents of file
	 * @throws FileException
	 */
	public String readRandomAccessFile(File file) throws FileException {
		StringBuffer textOut = new StringBuffer();
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "r");
			raf.seek(0);
			while (raf.getFilePointer() < raf.length()) textOut = textOut.append(raf.readLine() + (raf.getFilePointer() == raf.length()-1 ? "" : "\n"));
		} catch (IOException e) {
			try {
				raf.close();
			} catch (IOException e1) {
			}
			throw new FileException("Impossible to work on file.\nError message: " + e.getMessage());
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
			}
		}
		return textOut.toString();
	}

	/* metodo che restituisce stringa della path inserita in ingresso */
	/**
	 * method that return the string of the path formatted for OS 
	 * @param path list
	 * @return string of the path
	 */
	public String getStringPath (String... path) {
		if (path.length == 1) return path[0];
		else {
			String[] pathNxt = new String[path.length - 1];
			for (int i = 1; i < path.length; i++) pathNxt[i-1] = path[i];
			return Paths.get(path[0], pathNxt).toString();
		}
	}

	/* metodo che apre file explorer di sistema */
	/**
	 * method that open the system file explorer  
	 * @param path to open in the file explorer
	 * @throws IOException
	 */
	public void openFileExplorer(String... path) throws IOException {
		Desktop.getDesktop().open(new File(getStringPath(path)));
	}

	/* metodo che apre text editor di sistema */
	/**
	 * methos that open the system text editor
	 * @param path of file to open in the text editor
	 * @throws IOException
	 */
	public void openTextEditor(String... path) throws IOException {
		Desktop.getDesktop().edit(new File(getStringPath(path)));
	}

	/* metodo che riavvia l'applicazione */
	/**
	 * method that reboot the application
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public void restartApp() throws URISyntaxException, IOException {
		final String javaBin = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
		
		final ArrayList<String> cmnd = new ArrayList<String>();
		cmnd.add(javaBin);
		String[] mainCommand = System.getProperty("sun.java.command").split(" ");
		if (mainCommand[0].endsWith(".jar")) {
			cmnd.add("-jar");
			cmnd.add(new File(mainCommand[0]).getPath());
		} else {
			cmnd.add("-cp");
			cmnd.add(System.getProperty("java.class.path"));
			cmnd.add(mainCommand[0]);
		}
		for (int i = 1; i < mainCommand.length; i++) {
			cmnd.add(mainCommand[i]);
		}
		
		final ProcessBuilder builder = new ProcessBuilder(cmnd);
		builder.start();
		System.exit(0);
	}
}