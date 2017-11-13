/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maweiming;

/**
 *
 * @author azmah
 */
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;

import java.util.ArrayList;
import java.util.List;


public class AnsjUtils {

    public static List<String> participle(String text){
        List<String> wordList = new ArrayList<>();
        Result result = NlpAnalysis.parse(text);
        List<Term> terms = result.getTerms();
        for(Term term:terms){
            String name = term.getName();
            wordList.add(name);
        }
        return wordList;
    }

    public static String participleText(String text) {
        StringBuilder wordText = new StringBuilder();
        Result result = NlpAnalysis.parse(text);
        List<Term> terms = result.getTerms();

        for (int i = 0; i < terms.size(); i++) {
            Term term = terms.get(i);
            String word = term.getName();
            if(i!=0){
                wordText.append(" ");
            }
            wordText.append(word);
        }

        return wordText.toString();
    }
}
