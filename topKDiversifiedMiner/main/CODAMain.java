package main;

//import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CODAMain {
    public static void main(String[] args) throws IOException {
    	Long Time1 = System.currentTimeMillis();	
        Arguments arguments = Arguments.getInstance(args);
        File inFile = new File(arguments.inFilePath);
		File coreFile = new File(arguments.coreFilePath) ;
        File outFile = new File(arguments.outFilePath);
        try (FileReader reader = new FileReader(inFile)) {
			try(FileReader coreReader = new FileReader(coreFile)) {
			try (FileWriter writer = new FileWriter(outFile)) {
				if (arguments.isSimplified) {
					CODASimplifiedProcessor processor = new CODASimplifiedProcessor();
					System.out.println("Start CODASimplifiedProcessor...");
					processor.run(reader, writer, arguments);
					System.out.println("Finished CODASimplifiedProcessor...");

				} else {
					if (arguments.isLightVersion) {
//						CODALightProcessor processor = new CODALightProcessor();
						System.out.println("Start CODALightProcessor...");
//						processor.run(reader, writer, arguments);
						System.out.println("Finished CODALightProcessor...");
					} else {
						CODAProcessor processor = new CODAProcessor();
						System.out.println("Start CODAProcessor...");
						processor.run(reader, coreReader, writer, arguments);
						System.out.println("Finished CODAProcessor...");
					}
				}

			}
		}
	}
        Long Time2 = System.currentTimeMillis();
		System.out.println("Running Time(s) : " + (Time2 - Time1)*1.0 /1000);
    }
}