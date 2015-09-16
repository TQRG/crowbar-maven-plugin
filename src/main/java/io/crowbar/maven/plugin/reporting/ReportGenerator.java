package io.crowbar.maven.plugin.reporting;

import java.io.File;
import java.io.IOException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class ReportGenerator {

	private static final String DATA_FILE = "report-data.zip";
	private static final String INDEX_FILE = "visualization.html";
	private static final String SEARCH_TOKEN = "window.data_ex=";
	
	private final String diagnosisMessage;
	
	public ReportGenerator(String diagnosisMessage) {
		this.diagnosisMessage = diagnosisMessage;
	}
	
	public void generate(File targetDir) throws IOException {
		writeVisualization(targetDir);
	}
	
	
	private void writeVisualization(File targetDir) throws IOException {

		ClassLoader classLoader = ReportGenerator.class.getClassLoader();

		File temp = File.createTempFile("aes-temp-file", ".zip"); 
		temp.deleteOnExit();
		FileUtils.copyInputStreamToFile(classLoader.getResourceAsStream(DATA_FILE), temp);
		try {
			ZipFile zipFile = new ZipFile(temp);
			zipFile.extractAll(targetDir.getAbsolutePath());
		} catch (ZipException e) {
			e.printStackTrace();
		}

		String indexData = IOUtils.toString(classLoader.getResourceAsStream(INDEX_FILE));
		int i = indexData.indexOf(SEARCH_TOKEN);
		if (i != -1) {
			StringBuilder sb = new StringBuilder(indexData);
			sb.insert(i + SEARCH_TOKEN.length(), diagnosisMessage);
			indexData = sb.toString();
		}

		File reportIndexDestination = new File(targetDir, INDEX_FILE);
		FileUtils.write(reportIndexDestination, indexData);
	}
}
