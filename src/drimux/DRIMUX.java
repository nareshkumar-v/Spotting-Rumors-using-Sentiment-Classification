/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drimux;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import weka.core.*;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Attribute;
import weka.classifiers.*;
import weka.classifiers.Classifier;
import weka.filters.unsupervised.attribute.StringToWordVector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Elcot
 */
public class DRIMUX {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        System.out.println("\t\t\t****************");
        System.out.println("\t\t\t    DRIMUX");
        System.out.println("\t\t\t****************");                           
        
        /****************************************************************************************************************/
        System.out.println("===================================");
        System.out.println("\t1) Extract Tweets");
        System.out.println("===================================");
        
        ArrayList alltweets=new ArrayList();
        
        String qry=JOptionPane.showInputDialog(new JFrame(),"Enter the Keyword: ");
        
        try
        {                       
            String str="";
            Twitter twitter1 = new TwitterFactory().getInstance();
            Query query = new Query(qry);
            query.setCount(200);
            QueryResult result = twitter1.search(query);
            for (Status status : result.getTweets())
            {                                        
                String sg=status.getText().trim();                    
                if(!alltweets.contains(sg.trim()))
                {
                    String time=status.getCreatedAt().toString().trim();
                    System.out.println(sg.trim());
                    str=str+ time.trim() + " --> " +"@"+  status.getUser().getScreenName() + ":" + status.getText()+"\n\n";
                    alltweets.add(status.getText().trim());                        
                }
            }
            System.out.println("=====================================================");
            System.out.println("\t Extracted Tweets of Keyword - "+qry.trim());
            System.out.println("=====================================================");
            System.out.println(str.trim());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        /****************************************************************************************************************/
        
        try
        {
            System.out.println("===================================");
            System.out.println("\t PoS Tagging");
            System.out.println("===================================");
            edu.stanford.nlp.tagger.maxent.MaxentTagger ob=new edu.stanford.nlp.tagger.maxent.MaxentTagger(".\\models\\left3words-distsim-wsj-0-18.tagger");
            for(int i=0;i<alltweets.size();i++)
            {
                String tweet=alltweets.get(i).toString().trim().replaceAll("[^\\w\\s]", "");
                String ret=ob.tagString(tweet);
                System.out.println(ret.trim());
            }
            System.out.println();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        System.out.println();
        System.out.println("=========================================");
        System.out.println("\t2) Extract Hashtags from Tweets");
        System.out.println("=========================================");
        
        ArrayList allHashtags=new ArrayList();
        for(int i=0;i<alltweets.size();i++)
        {
            String s=alltweets.get(i).toString().trim();
            String sp[]=s.trim().replaceAll("\n"," ").split(" ");
            for(int j=0;j<sp.length;j++)
            {
                if(sp[j].trim().startsWith("#"))
                {
                    if(!(allHashtags.contains(sp[j].trim())))
                    {
                        allHashtags.add(sp[j].trim());
                        System.out.println(sp[j].trim());
                    }
                }
            }
        }
        
        System.out.println();
        System.out.println("=====================================");
        System.out.println("\t3) Retweet Extraction");
        System.out.println("=====================================");
        
        ArrayList allretweets=new ArrayList();
        for(int i=0;i<alltweets.size();i++)
        {
            String tweet=alltweets.get(i).toString().trim();
            if(tweet.trim().contains("RT"))
            {
                allretweets.add(tweet.trim());
                System.out.println(tweet.trim());
            }
        }
        
        System.out.println();
        System.out.println("===================================================");
        System.out.println("\t4) Extract Hashtags from Retweet");
        System.out.println("===================================================");
        
        ArrayList allHashtagsaftRe=new ArrayList();
        
        for(int i=0;i<allretweets.size();i++)
        {
            String s=allretweets.get(i).toString().trim();
            String sp[]=s.trim().split(" ");
            for(int j=0;j<sp.length;j++)
            {
                if(sp[j].trim().startsWith("#"))
                {
                    String hashtag=sp[j].trim();
                    if(!(hashtag.trim().equals("")))
                    {
                        if(!(allHashtagsaftRe.contains(hashtag.trim())))
                        {
                            allHashtagsaftRe.add(hashtag.trim());
                            System.out.println(hashtag.trim());
                        }
                    }
                }
            }
        }
        
        System.out.println();
        System.out.println("===================================================");
        System.out.println("\t5) Calcuate Hastags Influence");
        System.out.println("===================================================");
        
        ArrayList allWords=new ArrayList();
        for(int i=0;i<alltweets.size();i++)
        {
            String s=alltweets.get(i).toString().trim();
            String sp[]=s.trim().split(" ");
            for(int j=0;j<sp.length;j++)
            {
                String word=sp[j].trim().replaceAll("[^\\w\\s]", "");
                allWords.add(word.toLowerCase().trim());
            }
        }
        
        ArrayList allHashtagsinfluence=new ArrayList();
        System.out.println("Hashtag"+"\t-->\t"+"Influence");
        for(int i=0;i<allHashtagsaftRe.size();i++)
        {
            String hashtag=allHashtagsaftRe.get(i).toString().trim();
            String topic=hashtag.trim().replaceAll("#","").replaceAll("[^\\w\\s]", "");
            int influence=Collections.frequency(allWords,topic.toLowerCase().trim());
            allHashtagsinfluence.add(influence);
            System.out.println(hashtag.trim()+"\t-->\t"+influence);
        }
        
        System.out.println();
        System.out.println("=================================================================================");
        System.out.println("\t6) Greedy & Dynamic Blocking Algorithms (for Detect & Block Rumours)");
        System.out.println("=================================================================================");
          
        long start=System.currentTimeMillis();
        int Threshold=1;
        
        ArrayList secureTweets=new ArrayList();     // VB
        
        for(int i=0;i<alltweets.size();i++)     // Initial Edge Matrix A0
        {
            String tweet=alltweets.get(i).toString().trim().replaceAll("\n", " ");
            //System.out.println("tweet: "+tweet);
            
            ArrayList availableHashtags=new ArrayList();
            String sp[]=tweet.trim().split(" ");
            for(int j=0;j<sp.length;j++)
            {
                if(sp[j].trim().contains("#"))
                {
                    if(!(availableHashtags.contains(sp[j].trim())))
                    {
                        availableHashtags.add(sp[j].trim());
                    }
                }
            }
            
            double val=0;
            int sz=0;
            for(int j=0;j<availableHashtags.size();j++)
            {
                String hash=availableHashtags.get(j).toString().trim();
                int index=allHashtagsaftRe.indexOf(hash.trim());
                if(index>=0)
                {
                    String influ=allHashtagsinfluence.get(index).toString().trim();
                    double inf=Double.parseDouble(influ.trim());
                    if(inf>Threshold)
                    {
                        val=val+inf;
                        sz++;
                    }
                }
            }
            
            //System.out.println("val: "+val);
            //System.out.println("sz: "+sz);
            //double totinfluence=val/(double)sz;
            //System.out.println("totinfluence: "+totinfluence);
            String mainResult="Secure";
            if(val==0)
            {
                if(!(tweet.trim().contains("#")))
                {
                    mainResult="Secure";
                }
                else
                {
                    mainResult="Rumour";
                }
            }           
            //System.out.println("maniResult: "+maniResult);         
            System.out.println(tweet.trim()+" --> "+mainResult.trim());
            if(mainResult.trim().equals("Secure"))
            {
                secureTweets.add(tweet.trim());
            }
        }
        
        System.out.println();
        System.out.println("===================================================");
        System.out.println("\t7) Tweets after Blocking Rumours");
        System.out.println("===================================================");
        
        for(int i=0;i<secureTweets.size();i++)
        {
            String s=secureTweets.get(i).toString().trim();
            System.out.println(s.trim());
        }
        
        long stop=System.currentTimeMillis();
        long rumourBlockingtime=stop-start;
        System.out.println();
        System.out.println();
        System.out.println("Rumour Blocking Time: "+rumourBlockingtime+" ms");
        
        int rumourTweetsSize=alltweets.size()-secureTweets.size();
        double infectionRatio=(double)((double)rumourTweetsSize/(double)alltweets.size())*100;        
        System.out.println("Infection Ratio: "+infectionRatio+" %");  
        
        System.out.println();
        System.out.println("===================================================");
        System.out.println("\t8) Sentiment Classification");
        System.out.println("===================================================");
        
        ArrayList posWd=new ArrayList();
        ArrayList negWd=new ArrayList();
        ArrayList slang1=new ArrayList();
        ArrayList slang2=new ArrayList();
        ArrayList stop1=new ArrayList();
        
        try
        {
             //// Read Posivie words
             
            File fe1=new File("Positive.txt");
            FileInputStream fis1=new FileInputStream(fe1);
            byte data1[]=new byte[fis1.available()];
            fis1.read(data1);
            fis1.close();
            
            String sg1[]=new String(data1).split("\n");
               
            for(int i=0;i<sg1.length;i++)
                posWd.add(sg1[i].trim());
             
             ///// Read negative word
             
            File fe2=new File("Negative.txt");
            FileInputStream fis2=new FileInputStream(fe2);
            byte data2[]=new byte[fis2.available()];
            fis2.read(data2);
            fis2.close();
            
            String sg2[]=new String(data2).split("\n");
               
            for(int i=0;i<sg2.length;i++)
                negWd.add(sg2[i].trim());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        try
        {
            File fe=new File("Slang.txt");
            FileInputStream fis=new FileInputStream(fe);
            byte data[]=new byte[fis.available()];
            fis.read(data);
            fis.close();
              
            String s1[]=new String(data).split("\n");            
            
            for(int i=0;i<s1.length;i++)
            {
                String g1[]=s1[i].trim().split("#");
                slang1.add(g1[0].trim());
                slang2.add(g1[1].trim());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        try
        {
            File fe2=new File("stopwords1.txt");
            FileInputStream fis2=new FileInputStream(fe2);
            byte data2[]=new byte[fis2.available()];
            fis2.read(data2);
            fis2.close();
                
            String sg2[]=new String(data2).split("\n");
               
            for(int i=0;i<sg2.length;i++)
                stop1.add(sg2[i].trim());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        ArrayList forResults=new ArrayList();
        for(int i=0;i<secureTweets.size();i++)
        {
            String s=secureTweets.get(i).toString().trim();
            String sentence=s.trim().toLowerCase().trim().replaceAll("[^\\w\\s]", " ");
            String status="Positive";
            int pos=0; int neg=0;
            String sen[]=sentence.trim().split(" ");
            for(int j=0;j<sen.length;j++)
            {
                if(!(stop1.contains(sen[j].trim())))          // stopwords removal
                {
                    if(slang1.contains(sen[j].trim()))        // slang word removal
                    {
                        int ind1=slang1.indexOf(sen[j].trim());
                        sen[j]=slang2.get(ind1).toString().trim();                    
                    }
                    
                    if(posWd.contains(sen[j].trim()))
                    {
                        pos++;
                    }
                    if(negWd.contains(sen[j].trim()))
                    {
                        neg++;
                    }
                }
            }
            if(pos>neg)
            {
                status="Positive";                    
            }
            else
            {
                status="Negative";                    
            }
            System.out.println(s.trim()+" --> "+status.trim());
            forResults.add(s.trim()+" --> "+status.trim());
        }
        System.out.println("===================================================");
        
        System.out.println();
        System.out.println("===================================================");
        System.out.println("\t8) Results");
        System.out.println("===================================================");
        try
        {
            long nbStartTime=System.currentTimeMillis();
        
            //String thisClassString = "weka.classifiers.bayes.NaiveBayes";        
            String thisClassString = "weka.classifiers.functions.SMO";        

            String[] inputText = new String[forResults.size()];
            String[] inputClasses = new String[forResults.size()];
            for(int i=0;i<forResults.size();i++)
            {
                String s=forResults.get(i).toString().trim();
                String sp[]=s.trim().split(" --> ");

                inputText[i]=sp[0].trim();
                inputClasses[i]=sp[1].trim();            
            }

            //String[] testText = {"Subject calpine daily gas nomination calpine daily gas nomination 1 doc","Subject christmas tree farm pictures", "buy it now!","buy, buy, buy!","you are the best, buy!","it is spring in the air"};

            HashSet classSet = new HashSet(Arrays.asList(inputClasses));
            classSet.add("?");
            String[] classValues = (String[])classSet.toArray(new String[0]);       
            FastVector classAttributeVector = new FastVector();
            for (int i = 0; i < classValues.length; i++) {
                classAttributeVector.addElement(classValues[i]);
            }
            Attribute thisClassAttribute = new Attribute("@@class@@", classAttributeVector);       
            FastVector inputTextVector = null;
            Attribute thisTextAttribute = new Attribute("text", inputTextVector);
            for (int i = 0; i < inputText.length; i++) {
                thisTextAttribute.addStringValue(inputText[i]);
            }                       
                  
            FastVector thisAttributeInfo = new FastVector(2);
            thisAttributeInfo.addElement(thisTextAttribute);
            thisAttributeInfo.addElement(thisClassAttribute);
            TextClassifier classifier = new TextClassifier(inputText, inputClasses, thisAttributeInfo, thisTextAttribute, thisClassAttribute, thisClassString);        
            System.out.println(classifier.classify(thisClassString).toString().trim());
            
            long nbStopTime=System.currentTimeMillis();

            long NbExeTime=nbStopTime-nbStartTime;
            System.out.println("Execution Time: "+NbExeTime+" ms");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }    

    public static class TextClassifier 
    {
        private String[]   inputText       = null;
        private String[]   inputClasses    = null;
        private String     classString     = null;
        private Attribute  classAttribute  = null;
        private Attribute  textAttribute   = null;
        private FastVector attributeInfo   = null;
        private Instances  instances       = null;
        private Classifier classifier      = null;
        private Instances  filteredData    = null;
        private Evaluation evaluation      = null;
        private Set        modelWords      = null;        
        public static String delimitersStringToWordVector = "\\s.,:'\\\"()?!";        

        TextClassifier(String[] inputText, String[] inputClasses, FastVector attributeInfo, Attribute textAttribute, Attribute classAttribute, String classString) {
            this.inputText      = inputText;
            this.inputClasses   = inputClasses;
            this.classString    = classString;
            this.attributeInfo  = attributeInfo;
            this.textAttribute  = textAttribute;
            this.classAttribute = classAttribute;
        }
 
        public StringBuffer classify() {
            if (classString == null || "".equals(classString)) {
                return(new StringBuffer());
            }
            return classify(classString);
        }

        public StringBuffer classify(String classString) {
            this.classString = classString;
            StringBuffer result = new StringBuffer();            
            instances = new Instances("data set", attributeInfo, 100);           
            instances.setClass(classAttribute);
            try 
            {
                instances = populateInstances(inputText, inputClasses, instances, classAttribute, textAttribute);
                result.append("DATA SET:\n" + instances + "\n");               
                filteredData = filterText(instances);               
                modelWords = new HashSet();
                Enumeration enumx = filteredData.enumerateAttributes();
                while (enumx.hasMoreElements()) {
                    Attribute att = (Attribute)enumx.nextElement();
                    String attName = att.name().toLowerCase();
                    modelWords.add(attName);
                }
                classifier = Classifier.forName(classString,null);
                classifier.buildClassifier(filteredData);
                evaluation = new Evaluation(filteredData);
                evaluation.evaluateModel(classifier, filteredData);
                result.append(printClassifierAndEvaluation(classifier, evaluation) + "\n");               
                int startIx = 0;
                result.append(checkCases(filteredData, classifier, classAttribute, inputText, "not test", startIx)  + "\n");
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
                result.append("\nException (sorry!):\n" + e.toString());
            }
            return result;
        }

        public StringBuffer classifyNewCases(String[] tests) {
            StringBuffer result = new StringBuffer();           
            Instances testCases = new Instances(instances);
            testCases.setClass(classAttribute);
            String[] testsWithModelWords = new String[tests.length];
            int gotModelWords = 0;
            for (int i = 0; i < tests.length; i++) {                
                StringBuffer acceptedWordsThisLine = new StringBuffer();               
                String[] splittedText = tests[i].split("["+delimitersStringToWordVector+"]");                
                for (int wordIx = 0; wordIx < splittedText.length; wordIx++) {
                    String sWord = splittedText[wordIx];
                    if (modelWords.contains((String)sWord)) {
                        gotModelWords++;
                        acceptedWordsThisLine.append(sWord + " ");
                    }
                }
                testsWithModelWords[i] = acceptedWordsThisLine.toString();
            }          
            if (gotModelWords == 0) {
                result.append("\nWarning!\nThe text to classify didn't contain a single\nword from the modelled words. This makes it hard for the classifier to\ndo something usefull.\nThe result may be weird.\n\n");
            }
            try 
            {               
                String[] tmpClassValues = new String[tests.length];
                for (int i = 0; i < tmpClassValues.length; i++) {
                    tmpClassValues[i] = "?";
                }
                testCases = populateInstances(testsWithModelWords, tmpClassValues, testCases, classAttribute, textAttribute);
                Instances filteredTests = filterText(testCases);
                int startIx = instances.numInstances();
                result.append(checkCases(filteredTests, classifier, classAttribute, tests, "newcase", startIx) + "\n");
            } catch (Exception e) {
                e.printStackTrace();
                result.append("\nException (sorry!):\n" + e.toString());
            }
            return result;
        }

        public static Instances populateInstances(String[] theseInputTexts, String[] theseInputClasses, Instances theseInstances, Attribute classAttribute, Attribute textAttribute) {
            for (int i = 0; i < theseInputTexts.length; i++) {
                Instance inst = new Instance(2);
                inst.setValue(textAttribute,theseInputTexts[i]);
                if (theseInputClasses != null && theseInputClasses.length > 0) {
                    inst.setValue(classAttribute, theseInputClasses[i]);
                }
                theseInstances.add(inst);
            }
            return theseInstances;
        }

        public static StringBuffer checkCases(Instances theseInstances, Classifier thisClassifier, Attribute thisClassAttribute, String[] texts, String testType, int startIx) {
            StringBuffer result = new StringBuffer();
            try 
            {
                //result.append("\nCHECKING ALL THE INSTANCES:\n");
                Enumeration enumClasses = thisClassAttribute.enumerateValues();
                //result.append("Class values (in order): ");
                while (enumClasses.hasMoreElements()) {
                    String classStr = (String)enumClasses.nextElement();
                    //result.append("'" + classStr + "'  ");
                }
                //result.append("\n");                
                for (int i = startIx; i < theseInstances.numInstances(); i++) {
                    SparseInstance sparseInst = new SparseInstance(theseInstances.instance(i));
                    sparseInst.setDataset(theseInstances);                    
                    double correctValue = (double)sparseInst.classValue();
                    double predictedValue = thisClassifier.classifyInstance(sparseInst);
                    String predictString = thisClassAttribute.value((int)predictedValue) + " (" + predictedValue + ")";                    
                    if(predictString.trim().contains("?"))
                    {
                        predictString="Spam (2.0)";
                    }
                    if(!"newcase".equals(testType)) {
                        //result.append("\nTraining: '" + texts[i-startIx] + "'\n");                    
                        //result.append("predicted: " + predictString);
                        String correctString = thisClassAttribute.value((int)correctValue) + " (" + correctValue + ")";
                        String testString = ((predictedValue == correctValue) ? "OK!" : "NOT OK!") + "!";
                        //result.append("' real class: '" + correctString +  "' ==> " +  testString);
                    }
                    else
                    {
                        String testingemail=texts[i-startIx];
                        //result.append("\nTesting: '" + testingemail + "'\n");                    
                        //result.append("predicted: " + predictString);
                        
                        //naiveBayesTestingResults.add(testingemail+" --> "+predictString.trim());
                    }
                    //result.append("\n");
                    //result.append("\n");                    
                }
            } catch (Exception e) {
                e.printStackTrace();
                //result.append("\nException (sorry!):\n" + e.toString());
            }
            return result;
        }
       
        public static Instances filterText(Instances theseInstances) {
            StringToWordVector filter = null;            
            int wordsToKeep = 1000;
            Instances filtered = null;
            try 
            {
                filter = new StringToWordVector(wordsToKeep);                
                filter.setOutputWordCounts(true);
                filter.setSelectedRange("1");
                filter.setInputFormat(theseInstances);                
                filtered = weka.filters.Filter.useFilter(theseInstances,filter);                
            } catch (Exception e) {
                e.printStackTrace();
            }
            return filtered;
        }

        public static StringBuffer printClassifierAndEvaluation(Classifier thisClassifier, Evaluation thisEvaluation) {
            StringBuffer result = new StringBuffer();
            try 
            {
                //result.append("\n\nINFORMATION ABOUT THE CLASSIFIER AND EVALUATION:\n");
                //result.append("\nClassifier:\n" + "Naive Bayes" + "\n");
                result.append("\nevaluation.toSummaryString():\n" + thisEvaluation.toSummaryString("Summary",false)  + "\n");
                result.append("\nConfusion Matrix:\n" + thisEvaluation.toMatrixString()  + "\n");
                result.append("\nevaluation.toClassDetailsString():\n" + thisEvaluation.toClassDetailsString("Details")  + "\n");
                //result.append("\nevaluation.toCumulativeMarginDistribution:\n" + thisEvaluation.toCumulativeMarginDistributionString()  + "\n");
            } catch (Exception e) {
                e.printStackTrace();
                result.append("\nException (sorry!):\n" + e.toString());
            }
            return result;
        }
        public void setClassifierString(String classString) {
            this.classString = classString;
        }
    }
}
