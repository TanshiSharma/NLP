package project.nlp.sentimentextract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;
import edu.stanford.nlp.simple.*;
import project.nlp.sentimentextract.rule.RuleManager;
public class ReviewAnalyzer {
	
	private static final Logger logger = LogManager.getLogger(ReviewAnalyzer.class);
	private Document reviewDocument;
	private RuleManager ruleManager;
	private boolean isPrintPOS = false;
	private final int index;
	
	private Comparator<Pair<Integer, IndexedWord>> customComparator = (Pair<Integer, IndexedWord> t1,Pair<Integer, IndexedWord> t2) -> t1.getValue0().compareTo(t2.getValue0());
	
	public ReviewAnalyzer(int index, String reviewContent,RuleManager ruleManager){
		reviewDocument = new Document(reviewContent);
		this.ruleManager = ruleManager; 
		this.index = index;
	}
	
	public void setPrintPOS(boolean isPrintPOS) {
		this.isPrintPOS = isPrintPOS;
	}

	public ArrayList<AspectSentimentTuple> extractAspectSentimentExpression(){
		ArrayList<AspectSentimentTuple> tupleList = new ArrayList<AspectSentimentTuple>();
		
		
		for(Sentence sentence: reviewDocument.sentences()){
			//Should skip this sentence if it doesn't contain any aspects
			if(Collections.disjoint(sentence.words(),ruleManager.getAspectList())) continue;
			
			SemanticGraph dependencyGraph = sentence.dependencyGraph();
			
			//filter out CONJ in the graph
			SemanticGraph dependencyGraphNoCONJ = new SemanticGraph(dependencyGraph);
			Iterator<SemanticGraphEdge> it = dependencyGraphNoCONJ.edgeIterable().iterator();
			ArrayList<SemanticGraphEdge> removeEdgeList = new ArrayList<>();
			while(it.hasNext()){
				SemanticGraphEdge edge = it.next();
				if("conj".equals(edge.getRelation().getShortName())){
					removeEdgeList.add(edge);
				}
			}
			for(SemanticGraphEdge edge:removeEdgeList){
				dependencyGraphNoCONJ.removeEdge(edge);
			}
			
			for(String aspect:ruleManager.getAspectList()){
				//skip if the sentence doesn't contain this aspect
				if(!sentence.text().contains(aspect)){
					continue;
				}
				for(String rule:ruleManager.getRuleList()){
					String finaleRule = ruleManager.parseRule(rule, aspect);
					SemgrexMatcher matcher = SemgrexPattern.compile(finaleRule).matcher(dependencyGraphNoCONJ);
					while(matcher.findNextMatchingNode()){

						IndexedWord matchNode = matcher.getMatch();
							
						String sentimentExpression = getTextRepresentation(embalishNode(matchNode, dependencyGraphNoCONJ));
						AspectSentimentTuple tuple = new AspectSentimentTuple(this.index,aspect, sentimentExpression);
						//handle conjunctive clause(if any...)
						Pair<IndexedWord, String> conj = getFirstConjunctiveConnectedNode(matchNode, dependencyGraph);
						if(conj != null){
							tuple.setConj(conj.getValue1());
							tuple.setConjSentiment( getTextRepresentation(embalishNode(conj.getValue0(), dependencyGraphNoCONJ)) );
						}
						logger.info(
								String.format("[Add tuple:%s]",tuple.toString())
								+ String.format("using [Sentence:%s,rule:%s]",sentence.text(),finaleRule)
								);
						tupleList.add(tuple);
					}
				}
			}
		}
		return tupleList;
	}
	
	private Pair<IndexedWord,String> getFirstConjunctiveConnectedNode(IndexedWord node,SemanticGraph dependencyGraph){
		for(SemanticGraphEdge edge:dependencyGraph.outgoingEdgeIterable(node)){
			if("conj".equals(edge.getRelation().getShortName())){
				return new Pair(edge.getDependent(),edge.getRelation().getSpecific());
			}
		}
		
		for(SemanticGraphEdge edge:dependencyGraph.incomingEdgeIterable(node)){
			if("conj".equals(edge.getRelation().getShortName())){
				return new Pair(edge.getGovernor(),edge.getRelation().getSpecific());
			}
		}
		return null;
	}
	
	private String getShortPOSTag(String posTag){
		return posTag.length() > 2 ? posTag.substring(0,2):posTag;
	}
	
	private List<Pair<Integer, IndexedWord>> embalishNode(IndexedWord node,SemanticGraph dependencyGraph ){
		List<Pair<Integer, IndexedWord>> storeNodes = new ArrayList<>();
		embalishNode(node, dependencyGraph, storeNodes);
		return storeNodes;
	}
	
	private void embalishNode(IndexedWord node,SemanticGraph dependencyGraph,List<Pair<Integer, IndexedWord>> storeNodes ){
		String tag = getShortPOSTag(node.tag());
		switch(tag){
			case "JJ":
				this.embalishAdj(node, dependencyGraph,storeNodes);
				break;
			case "RB":
				this.embalishAdverb(node, dependencyGraph,storeNodes);
				break;
			case "NN":
				this.embalishNoun(node, dependencyGraph,storeNodes);
				break;
			case "VB":
				this.embalishVerb(node, dependencyGraph,storeNodes);
				break;
			default:
				storeNodes.add(new Pair(node.index(),node));
		}
	}
	
	private void embalishVerb(IndexedWord node,SemanticGraph dependencyGraph,List<Pair<Integer, IndexedWord>> storeNodes){
		for(SemanticGraphEdge edge:dependencyGraph.outgoingEdgeIterable(node)){
			String posTag = getShortPOSTag(edge.getDependent().tag());
			if("RB".equals(posTag)){
				this.embalishAdverb(edge.getDependent(), dependencyGraph,storeNodes);
			}else if(edge.getRelation().getShortName().contains("dobj")){
				this.embalishNode(edge.getDependent(), dependencyGraph,storeNodes);
			}else if(edge.getRelation().getShortName().contains("xcomp")){
				this.embalishNode(edge.getDependent(), dependencyGraph,storeNodes);
			}else if(edge.getRelation().getShortName().contains("aux")){
				this.embalishNode(edge.getDependent(), dependencyGraph,storeNodes);
			}
		}
		storeNodes.add(new Pair(node.index(),node));
	}
	
	private void embalishNoun(IndexedWord node,SemanticGraph dependencyGraph,List<Pair<Integer, IndexedWord>> storeNodes){
		for(SemanticGraphEdge edge:dependencyGraph.outgoingEdgeIterable(node)){
			String posTag = getShortPOSTag(edge.getDependent().tag());
			if("JJ".equals(posTag)){
				this.embalishAdj(edge.getDependent(), dependencyGraph,storeNodes);
			}else if("RB".equals(posTag)){
				this.embalishAdverb(edge.getDependent(), dependencyGraph,storeNodes);
			}else if(edge.getRelation().getShortName().contains("case")){
				this.embalishNode(edge.getDependent(), dependencyGraph,storeNodes);
			}else if(edge.getRelation().getShortName().contains("nmod")){
				this.embalishNode(edge.getDependent(), dependencyGraph,storeNodes);
			}else if(edge.getRelation().getShortName().contains("cc:preconj")){
				this.embalishNode(edge.getDependent(), dependencyGraph,storeNodes);
			}else if(edge.getRelation().getShortName().contains("neg")){
				this.embalishNode(edge.getDependent(), dependencyGraph,storeNodes);
			}
		}
		storeNodes.add(new Pair(node.index(),node));
	}
	
	private void embalishAdverb(IndexedWord node,SemanticGraph dependencyGraph,List<Pair<Integer, IndexedWord>> storeNodes){
		for(SemanticGraphEdge edge:dependencyGraph.outgoingEdgeIterable(node)){
			String posTag = getShortPOSTag(edge.getDependent().tag());
			//append only Adverb
			if("RB".equals(posTag)){
				this.embalishAdverb(edge.getDependent(), dependencyGraph,storeNodes);
			}
		}
		storeNodes.add(new Pair(node.index(),node));
	}

	private void embalishAdj(IndexedWord node,SemanticGraph dependencyGraph,List<Pair<Integer, IndexedWord>> storeNodes){
		for(SemanticGraphEdge edge:dependencyGraph.outgoingEdgeIterable(node)){
			String posTag = getShortPOSTag(edge.getDependent().tag());
			//append only Adverb
			if("RB".equals(posTag)){
				this.embalishAdverb(edge.getDependent(), dependencyGraph,storeNodes);
			}else if(edge.getRelation().getShortName().contains("case")){
				this.embalishNode(edge.getDependent(), dependencyGraph,storeNodes);
			}else if(edge.getRelation().getShortName().contains("cc:preconj")){
				this.embalishNode(edge.getDependent(), dependencyGraph,storeNodes);
			}else if(edge.getRelation().getShortName().contains("neg")){
				this.embalishNode(edge.getDependent(), dependencyGraph,storeNodes);
			}
		}
		storeNodes.add(new Pair(node.index(),node));
	}
	
	private String getTextRepresentation(List<Pair<Integer, IndexedWord>> storeNodes){
		storeNodes.sort(customComparator);
		String textRepresentation = "";
		for(Pair<Integer, IndexedWord> element: storeNodes){
			IndexedWord node = element.getValue1();
			String nodeText = isPrintPOS? String.format("%s/%s", node.originalText(),node.tag()) : node.originalText();
			textRepresentation = textRepresentation + nodeText + " ";
		}
		return textRepresentation.trim();
	}
}
