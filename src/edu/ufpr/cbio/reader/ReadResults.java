/*
 * Copyright 2016, Charter Communications,  All rights reserved.
 */
package edu.ufpr.cbio.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 *
 * @author vfontoura
 */
public class ReadResults {

	public static void main(String[] args) throws IOException {
		String fileName = "C:\\results\\sq11";
		File dir = new File(fileName);

		System.out.println(dir.exists());

		File[] listFiles = dir.listFiles();
		for (File file : listFiles) {
			try (FileReader fileReader = new FileReader(file); BufferedReader br = new BufferedReader(fileReader)) {

				String readline = null;
				while ((readline = br.readLine()) != null) {
					if (readline.contains(":"))
						System.out.println(readline.split(":")[1].replace(".", ","));
				}

			}

		}
	}

}
