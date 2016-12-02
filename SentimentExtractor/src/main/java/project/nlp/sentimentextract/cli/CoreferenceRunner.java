package project.nlp.sentimentextract.cli;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import project.nlp.sentimentextract.CoreferenceParser;

public class CoreferenceRunner {
	
	private static final Logger logger = LogManager.getLogger(CoreferenceRunner.class);
			
	public static void main(String[] args){
		Options options = new Options();
		
		Option directoryOption = new Option("d","directory", true, "directory contains .json files");
		Option directoryOutputOption = new Option("o","output_directory", true, "directory to store processed .json files");
		directoryOption.setRequired(true);
		directoryOutputOption.setRequired(true);
		
		options.addOption(directoryOption);
		options.addOption(directoryOutputOption);
		
		CommandLineParser cmdParser = new DefaultParser();
		CommandLine cmd = null;
		
		try{
			cmd = cmdParser.parse(options, args);
		}catch(Exception e){
			System.out.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("CoreferenceRunner", options);
			System.exit(0);
		}
		
		File inputFolder = new File(cmd.getOptionValue("d"));
		File outputFolder = new File(cmd.getOptionValue("o"));
		if(!outputFolder.exists()){
			System.out.println("output folder not exists!!");
			System.exit(0);
		}
		JSONParser jsonParser = new JSONParser();
		
		for(File file: inputFolder.listFiles()){
			
			if(!file.getName().endsWith(".json"))
					continue;

			logger.info("Processing file:" + file.getName());
			//filename format: reviews_{titleId}.json
			String titleId = file.getName().replace(".json", "").split("_")[1];
			try( 
				FileReader fr = new FileReader(file);
				FileWriter fw = new FileWriter(outputFolder + "/" + "processed_" + titleId +".json");
				)
			{
				JSONArray reviews = (JSONArray)jsonParser.parse(fr);
				JSONArray parsedReviews = new JSONArray();
				
				Iterator<JSONObject> iterator = reviews.iterator();
				int reviewIndex = 1;
				while(iterator.hasNext()){
					logger.info("Processing review index:" + reviewIndex);
					JSONObject review = iterator.next();
					String reviewContent = (String)review.get("review");
					reviewContent = reviewContent.replace("\n", " ").replace("<br>", " ");
					String parsedReviewContent = CoreferenceParser.parseContent(reviewContent);
					JSONObject parsedReview = new JSONObject();
					parsedReview.put("review", parsedReviewContent);
					parsedReviews.add(parsedReview);
					reviewIndex += 1;
				}
				
				fw.write(parsedReviews.toJSONString());
			}catch(Exception e){
				logger.error("Error processing file:" + file.getName(),e);
			}
		}
		
	}
}
