package project.nlp.sentimentextract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class CoreferenceParser {
	
	public static String parseContent(String content){
		Document document = new Document(content);
		//collect coreference chain resolve into dictionary
		//noted: -1 for sentenceIndex,startIndex,endIndex because corref module start its indexs as 1...
		//{(sentenceIndex,startIndex):(representString,endIndex),...}
		Map<Pair<Integer, Integer>,Pair<String, Integer>> chainDict = new HashMap<Pair<Integer, Integer>,Pair<String, Integer>>();
	    //sentenceIndex map for fast checking
		HashSet<Integer> sentenceIndexMap = new HashSet<Integer>();
	    
		Map<Integer, CorefChain> maps = document.coref();
	    for(CorefChain chain : maps.values()){
	    	String representToken = chain.getRepresentativeMention().mentionSpan;
	    	
	    	for(CorefMention corefMention: chain.getMentionsInTextualOrder()){
	    		chainDict.put(new Pair<Integer, Integer>(corefMention.sentNum-1, corefMention.startIndex-1), new Pair<String, Integer>(representToken, corefMention.endIndex-1));
	    		sentenceIndexMap.add(corefMention.sentNum-1);
	    	}
	    	
	    }
	    
	    StringBuilder parsedString = new StringBuilder();
	    
	    for(Sentence sentence:document.sentences()){
	    	int sentenceIndex = sentence.sentenceIndex();
	    	
	    	//if the sentence doesn't contain any corref chain, copy the whole sentence and skip it
	    	if(!sentenceIndexMap.contains(sentenceIndex)){
	    		parsedString.append(" " + sentence.text());
	    		continue;
	    	}
	    	
	    	int wordIndex = 0;

	    	while(wordIndex < sentence.words().size()){
	    		String word = sentence.originalText(wordIndex);
	    		Pair<Integer,Integer> key = new Pair<Integer,Integer>(sentenceIndex,wordIndex);
	    		if(chainDict.containsKey(key)){
	    			Pair<String,Integer> value = chainDict.get(key);
	    			String representToken = value.getValue0();
	    			int endIndex = value.getValue1();
	    			parsedString.append(" " + representToken);
	    			wordIndex = endIndex;
	    		}else{
	    			parsedString.append(" " + word);
	    			wordIndex += 1;
	    		}
	    	}
	    	
	    }
	    return(parsedString.toString());
	}
}
