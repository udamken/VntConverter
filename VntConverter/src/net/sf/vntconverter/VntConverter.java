package net.sf.vntconverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.internet.MimeUtility;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;

/**
 * <p>
 * Mit dem VntConverter können vNote-Dateien (Endung vnt) in Textdateien (Endung txt) und zurückkonvertiert
 * werden. Solche vNote-Dateien werden bei PIMs eingesetzt. Beim Samsung Wave S8500 werden vNote-Dateien aus
 * Memos über "Einstellungen / Memo / Auf Speicherkarte exportieren" erstellt. Nach der Umwandlung in
 * Textdateien und Bearbeitung am PC, z.B. mit Notepad++, können sie dann zurück in das vNote-Format
 * konvertiert und über "Einstellungen / Memo / Von Speicherkarte" importieren wieder in der Anwendung Memo
 * zur Verfügung gestellt werden.
 * </p>
 * <p>
 * TODO Eventuell lässt sich die Behandlung des vNote-Formats über OpenSync oder eine andere Bibliothek zur
 * Verarbeitung von SyncML bewerkstelligen.
 * </p>
 * <p>
 * TODO Die CR/LF-Behandlung ist noch etwas hemdsärmlig, vielleicht lässt sich hier noch etwas über Properties
 * besser machen?
 * </p>
 * <p>
 * TODO Der Code funktioniert vermutlich nur für VNOTE-1.1 mit UTF-8- und QUOTED-PRINTABLE-Kodierung,
 * eventuell lässt sich das generischer lösen.
 * </p>
 * <p>
 * (c) 2012, by Uwe Damken
 * </p>
 */
public class VntConverter {

	private static final DateFormat VNT_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

	/**
	 * Liest alle Bytes vom InputStream und schreibt sie in den OutputStream, anschließend werden beide
	 * Streams geschlossen.
	 */
	private void copyAndClose(InputStream input, OutputStream output) throws IOException {
		int abyte;
		while ((abyte = input.read()) != -1) {
			output.write(abyte);
		}
		input.close();
		output.close();
	}

	/**
	 * Dekodiert einen UTF-8-QUOTED-PRINTABLE-String in einen Java-Unicode-String.
	 */
	public String decode(String in) {
		try {
			InputStream input = MimeUtility.decode(new ReaderInputStream(new StringReader(in), "UTF-8"),
					"quoted-printable");
			StringWriter sw = new StringWriter();
			OutputStream output = new WriterOutputStream(sw, "UTF-8");
			copyAndClose(input, output);
			return sw.toString();
		} catch (Exception e) {
			throw new RuntimeException("Exception caught in VntConverter.encode(in):", e);
		}

	}

	/**
	 * Enkodiert einen Java-Unicode-String in einen UTF-8-QUOTED-PRINTABLE-String.
	 */
	public String encode(String in) {
		try {
			InputStream input = new ReaderInputStream(new StringReader(in), "UTF-8");
			StringWriter sw = new StringWriter();
			OutputStream output = MimeUtility.encode(new WriterOutputStream(sw), "quoted-printable");
			copyAndClose(input, output);
			return sw.toString().replaceAll("=\\x0D\\x0A", "").replaceAll("\\x0D\\x0A", "=0D=0A");
		} catch (Exception e) {
			throw new RuntimeException("Exception caught in VntConverter.encode(in):", e);
		}
	}

	/**
	 * Dekodiert eine vNote-Datei in eine UTF-8-Textdatei.
	 */
	private void decode(File in, File out) {
		FileInputStream inStream = null;
		try {
			inStream = new FileInputStream(in);
			BufferedReader br = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
			String line = null;
			String content = "";
			while ((line = br.readLine()) != null) {
				if (line.startsWith("BEGIN:")) {
				} else if (line.startsWith("VERSION:")) {
				} else if (line.startsWith("DCREATED:")) {
				} else if (line.startsWith("LAST-MODIFIED:")) {
				} else if (line.startsWith("END:VNOTE")) {
				} else if (line.startsWith("BODY;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:")) {
					content = line.substring("BODY;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:".length());
				} else {
					content += line;
				}
				if (content.endsWith("=")) {
					content = content.substring(0, content.length() - 1);
				}
			}
			br.close();
			PrintStream ps = new PrintStream(out, "UTF-8");
			ps.print(decode(content));
			ps.close();
		} catch (Exception e) {
			throw new RuntimeException("Exception caught in VntConverter.decode(in, out):", e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					// Einfach ignorieren ist hier ok
				}
			}
		}
	}

	/**
	 * Liefert den gesamten Inhalt der übergebenen UTF-8-Textdatei als String zurück.
	 */
	private String getFileContent(File in) {
		FileInputStream inStream = null;
		try {
			inStream = new FileInputStream(in);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
			StringBuffer fileData = new StringBuffer();
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
			return fileData.toString();
		} catch (Exception e) {
			throw new RuntimeException("Exception caught in VntConverter.getFileContent(in):", e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					// Einfach ignorieren ist hier ok
				}
			}
		}
	}

	/**
	 * Enkodiert eine UTF-8-Textdatei in eine vNote-Datei.
	 */
	private void encode(File in, File out) {
		PrintStream ps = null;
		try {
			ps = new PrintStream(out, "UTF-8");
			ps.println("BEGIN:VNOTE");
			ps.println("VERSION:1.1");
			String content = getFileContent(in);
			String encodedContent = "BODY;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:" + encode(content);
			while (encodedContent.length() > 0) {
				if (encodedContent.length() > 74) {
					ps.println(encodedContent.substring(0, 74) + "=");
					encodedContent = encodedContent.substring(74);
				} else {
					ps.println(encodedContent);
					encodedContent = "";
				}
			}
			String lastModified = VNT_DATE_FORMAT.format(new Date(out.lastModified()));
			ps.println("DCREATED:" + lastModified);
			ps.println("LAST-MODIFIED:" + lastModified);
			ps.println("END:VNOTE");
		} catch (Exception e) {
			throw new RuntimeException("Exception caught in VntConverter.encode(in, out):", e);
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	/**
	 * Führt den VntConverter mit den angegebnen Optionen aus.
	 */
	public static void main(String[] args) {
		try {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				// Wenn nicht, dann nicht ...
			}
			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"Text files (txt) and memo files (vnt)", "txt", "vnt");
			fileChooser.setCurrentDirectory(new java.io.File("."));
			fileChooser.setDialogTitle("Choose files to convert ...");
			fileChooser.setFileFilter(filter);
			fileChooser.setMultiSelectionEnabled(true);
			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				JFileChooser directoryChooser = new JFileChooser();
				directoryChooser.setCurrentDirectory(new java.io.File("."));
				directoryChooser.setDialogTitle("Choose target directory ...");
				directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				directoryChooser.setAcceptAllFileFilterUsed(false);
				if (directoryChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					VntConverter converter = new VntConverter();
					String targetDirectoy = directoryChooser.getSelectedFile().getAbsolutePath();
					System.out.println(targetDirectoy);
					for (File file : fileChooser.getSelectedFiles()) {
						if (file.getName().endsWith(".txt")) {
							converter.encode(file,
									new File(targetDirectoy + "\\" + file.getName().replace(".txt", ".vnt")));
						} else if (file.getName().endsWith(".vnt")) {
							converter.decode(file,
									new File(targetDirectoy + "\\" + file.getName().replace(".vnt", ".txt")));
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception caught", e);
		}
	}
}
