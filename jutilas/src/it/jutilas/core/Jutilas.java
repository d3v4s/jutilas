package it.jutilas.core;

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

import it.jutilas.exception.FileException;

public class Jutilas {
	private static Jutilas jutilas;

	private Jutilas() {
	}
	
	public static Jutilas getInstance() {
		return (jutilas = (jutilas == null) ? new Jutilas() : jutilas);
	}


	/* metodo che ordina lista string in ignorando maiuscole e minuscole */
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
	public boolean openBorwser(String URL) throws IOException, URISyntaxException {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			desktop.browse(new URI(URL));
			return true;
		} else
			return false;
	}

	/* metodo per settare un' impostazione nel file di conf */
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
			throw new FileException("Errore!!! Impossibile lavorare sul file di configurazione: " + filePath + "\n"
												+ "Error message: " + e.getMessage());
		}
	}

	/* metodo che ritorna un' impostazione del file di conf */
	public String getConf(String filePath, String param) throws FileException {
		Properties prop = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream(new File(filePath));
			prop.load(fis);
			return prop.getProperty(param);
		} catch (IOException e) {
			throw new FileException("Errore!!! Impossibile lavorare sul file di configurazione: " + filePath + "\n"
												+ "Error message: " + e.getMessage());
		}
	}
	/* metodo per eliminare file o directory con sub directory */
	public void recursiveDelete(String filePath) {
		File f = new File(filePath);
		/* se l'istanza del file e' un file o una directory vuota, viene eliminato */
		if (f.exists() && (f.isFile() || (f.isDirectory() && isEmptyDirectory(f)))) {
			f.delete();
		/* invece se e' una directory che contiene file listiamo il file contenuti al suo interno */
		} else if(f.isDirectory() && !(isEmptyDirectory(f))) {
			File[] files = f.listFiles();
			for(File file : files) {
				recursiveDelete(file.getPath());
			}
			f.delete();
		}
	}
	
	/* metodo che ci dice se la directory e' vuota o contiene dei file */
	public boolean isEmptyDirectory(File f) {
        if (f.isDirectory()) {
        	File[] files = f.listFiles();
        	if(!(files.length > 0))
        		return true;
        }
        return false;
	}

	/* metodo che ritorna le ultime righe di un file */
	public String getLastRowFile(String filePath, int numRow) throws FileException {
		File file = new File(filePath);
		RandomAccessFile raf = null;
		if (!(file.exists() && file.isFile() && file.canRead()))
			throw new FileException("Errore!!! Impossibile lavorare sul file: " + filePath);
		try {
			raf = new RandomAccessFile(file, "r");
			long fileLenght = raf.length() - 1;
			StringBuilder sb = new StringBuilder();
			int line = 0;
			for (long filePointer = fileLenght; filePointer != -1; filePointer--) {
				raf.seek(filePointer);
				int readByte = raf.readByte();

				if (readByte == 0xA)
					line = (filePointer < fileLenght) ? line + 1 : line;
				else if (readByte == 0xD)
					line = (filePointer < fileLenght-1) ? line + 1 : line;
				
				if (line >= numRow)
					break;
				
				sb.append((char) readByte);
			}
			if (raf != null) {
				raf.close();
			}
			return sb.reverse().toString();
			
		} catch (IOException e) {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e1) {
					throw new FileException("Errore!!! File: " + filePath + "\n"
														+ "Messaggio: " + e.getMessage());
				}
			}
			throw new FileException("Errore!!! Impossibile lavorare sul file: " + filePath + "\n"
									+ "Messaggio: " + e.getMessage());
		}
	}

	/* metodo per copiare file */
	public void copyFile(String filePath, String toPath, CopyOption... copyOptions) throws FileException {
		try {
			Files.copy(Paths.get(filePath), Paths.get(toPath), copyOptions);
		} catch (IOException e) {
			throw new FileException("Errore!!! Impossibile lavorare sul file: " + filePath + "\n"
									+ "Messaggio: " + e.getMessage());
		}
	}

	/* metodo per rinominare file */
	public void renameFile(String filePath, String newName) throws FileException {
		Path file = Paths.get(filePath);
		if (!file.toFile().exists())
			throw new FileException("Errore!!! Il file \"" + filePath + "\" non esiste.");
		String newPathFile = file.toString().replaceFirst(file.getFileName() + "$", newName);
		file.toFile().renameTo(new File(newPathFile));
	}

	/* metodo che restituisce stringa del contenuto del file */
	public String readFile(String filePath) throws FileException {
		return readFile(new File(filePath)); 
	}

	/* metodo che restituisce stringa del contenuto del file */
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
				throw new FileException("Impossibile lavorare sul file.\n"
						+ "Message error: " + e1.getMessage());
			}
			throw new FileException("Impossibile lavorare sul file.\n"
					+ "Message error: " + e.getMessage());
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				throw new FileException("Impossibile lavorare sul file.\n"
						+ "Message error: " + e.getMessage());
			}
		}
		return String.valueOf(textOut); 
	}

	/* metodo che restituisce stringa del contenuto del file */
	public String readRandomAccessFile(File file) throws FileException {
		StringBuffer textOut = new StringBuffer();
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "r");
			raf.seek(0);
			while(raf.getFilePointer() < raf.length())
				textOut = textOut.append(raf.readLine() + (raf.getFilePointer() == raf.length()-1 ? "" : "\n"));
		} catch (IOException e) {
			try {
				raf.close();
			} catch (IOException e1) {
			}
			throw new FileException("Impossibile lavorare sul file.\n"
										+ "Message error: " + e.getMessage());
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
			}
		}
		return textOut.toString();
	}

	/* metodo che restituisce stringa della path inserita in ingresso */
	public String getStringPath (String... path) {
		if (path.length == 1)
			return path[0];
		else {
			String[] pathNxt = new String[path.length - 1];
			for (int i = 1; i < path.length; i++) {
				pathNxt[i-1] = path[i];
			}
			return Paths.get(path[0], pathNxt).toString();
		}
	}

	/* metodo che apre file explorer di sistema */
	public void openFileExplorer(String... path) throws IOException {
		Desktop.getDesktop().open(new File(getStringPath(path)));
	}

	/* metodo che apre text editor di sistema */
	public void openTextEditor(String... path) throws IOException {
		Desktop.getDesktop().edit(new File(getStringPath(path)));
	}

	/* metodo che riavvia l'applicazione */
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
