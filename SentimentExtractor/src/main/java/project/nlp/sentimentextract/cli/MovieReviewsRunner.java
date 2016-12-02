package project.nlp.sentimentextract.cli;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import project.nlp.sentimentextract.AspectSentimentTuple;
import project.nlp.sentimentextract.ReviewAnalyzer;
import project.nlp.sentimentextract.rule.RuleManager;

public class MovieReviewsRunner {
	private static final Logger logger = LogManager.getLogger(MovieReviewsRunner.class);
	
	public static void main(String []args){
		Options options = new Options();
		
		Option directoryOption = new Option("d","directory", true, "directory contains .json files");
		Option directoryOutputOption = new Option("o","output_directory", true, "directory to store .csv files");
		Option isPrintPOSOption = new Option("pos","print_pos", true, "is print POS along with word tokens?(Yes/No)");
		
		directoryOption.setRequired(true);
		directoryOutputOption.setRequired(true);
		isPrintPOSOption.setRequired(true);
		
		options.addOption(directoryOption);
		options.addOption(directoryOutputOption);
		options.addOption(isPrintPOSOption);
		
		CommandLineParser cmdParser = new DefaultParser();
		CommandLine cmd = null;
		
		try{
			cmd = cmdParser.parse(options, args);
		}catch(Exception e){
			System.out.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("MovieReviewsRunner", options);
			System.exit(0);
		}
		
		File inputFolder = new File(cmd.getOptionValue("d"));
		File outputFolder = new File(cmd.getOptionValue("o"));
		if(!outputFolder.exists()){
			System.out.println("output folder not exists!!");
			System.exit(0);
		}
		boolean isPrintPOS = cmd.getOptionValue("pos").toLowerCase().charAt(0) == 'y';
		
		JSONParser jsonParser = new JSONParser();
		RuleManager ruleManager = new RuleManager("extract_rules.test.properties");
		
		for(File file: inputFolder.listFiles()){
			
			if(!file.getName().endsWith(".json"))
					continue;

			logger.info("Processing file:" + file.getName());
			String titleId = file.getName().replace(".json", "").split("_")[1];
			try( 
				FileReader fr = new FileReader(file);
				FileWriter fw = new FileWriter(outputFolder + "/" + titleId +".csv");
				)
			{
				JSONArray reviews = (JSONArray)jsonParser.parse(fr);
				ArrayList<AspectSentimentTuple> tuples = new ArrayList<>();
				
				Iterator<JSONObject> iterator = reviews.iterator();
				int reviewIndex = 1;
				while(iterator.hasNext()){
					logger.info("Processing review index:" + reviewIndex);
					JSONObject review = iterator.next();
					String reviewContent = (String)review.get("review");
					reviewContent = reviewContent.replace("\n", " ").replace("<br>", " ");
					ReviewAnalyzer reviewAnalyzer = new ReviewAnalyzer(reviewIndex,reviewContent, ruleManager);
					reviewAnalyzer.setPrintPOS(isPrintPOS);
					ArrayList<AspectSentimentTuple> tuplesReview = reviewAnalyzer.extractAspectSentimentExpression();
					tuples.addAll(tuplesReview);
					reviewIndex += 1;
				}
				
				//write .csv
				writeCSVResult(tuples, fw);
			}catch(Exception e){
				logger.error("Error processing file:" + file.getName(),e);
			}
		}
	}
	
	private static void writeCSVResult(ArrayList<AspectSentimentTuple> tuples,FileWriter fw) throws Exception{
		CSVFormat csvFormat = CSVFormat.DEFAULT.
			withHeader("index","aspect","sentiment","conj","conjSentiment");
		try(CSVPrinter csvPrinter = new CSVPrinter(fw, csvFormat))
		{
			for(AspectSentimentTuple tuple:tuples){
				List<String> record = new ArrayList<>();
				record.add(tuple.getIndex() + "");
				record.add(tuple.getAspect());
				record.add(tuple.getSentiment());
				record.add(tuple.getConj());
				record.add(tuple.getConjSentiment());
				csvPrinter.printRecord(record);
			}
		}
		
	}
}
