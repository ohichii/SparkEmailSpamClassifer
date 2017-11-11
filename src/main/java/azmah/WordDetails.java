package azmah;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author azmah
 */
import java.io.Serializable;

/**
 * This class holds statistics about a specific word.
 *
 *
 */
@SuppressWarnings("serial")
public class WordDetails implements Serializable, Comparable<WordDetails> {

    // The word
    private String word;

    // Number of times the word appears in Spam messages
    private int cntSpam;

    // Number of times the word appears in Ham messages
    private int cntHam;

    // Probability that the word is Spam
    // (1 + cntSpam) / [cntTotalSpam + (cntTotalSpam + cntTotalHam)]
    private float pSpam;

    // Probability that the word is Ham
    // (1 + cntHam) / [cntTotalHam + (cntTotalSpam + cntTotalHam)]
    private float pHam;

    // Default Constructor
    public WordDetails() {
        cntSpam = 0;
        cntHam = 0;
        pSpam = 0.0f;
        pHam = 0.0f;
    }

    // Takes word and boolean to increment ham or spam count
    public WordDetails(String word, boolean spam) {
        this.word = word;
        cntSpam = 0;
        cntHam = 0;
        pSpam = 0.0f;
        pHam = 0.0f;
        updateCounts(spam);
    }

    /*
	 * Updates only spam & ham counts
     */
    public void updateCounts(boolean spam) {
        if (spam) {
            cntSpam++;
        } else {
            cntHam++;
        }
    }

    /*
	 * Updates probabilities
     */
    public void updateProbabilities(int cntWordsSpam, int cntWordsHam) {
        int cntWordsAll = cntWordsSpam + cntWordsHam;

        setpSpam(cntWordsSpam, cntWordsAll);
        setpHam(cntWordsHam, cntWordsAll);
    }

    public int getCntSpam() {
        return cntSpam;
    }

    public int getCntHam() {
        return cntHam;
    }

    public float getpSpam() {
        return pSpam;
    }

    public float getpHam() {
        return pHam;
    }
    public String getWord(){
        return word;
    }

    public void setWord(String w){
        word = w;
    }
    public void setCntSpam(int cntSpam) {
        this.cntSpam = cntSpam;
    }

    public void setCntHam(int cntHam) {
        this.cntHam = cntHam;
    }

    /*
	 * Calculate probability of the word being in Spam using
	 * cntWordsSpam and cntWordsAll
     */
    private void setpSpam(int cntWordsSpam, int cntWordsAll) {
        this.pSpam = (1 + (float) cntSpam) / (float) (cntWordsSpam + cntWordsAll);
    }

    /*
	 * Calculate probability of the word being in Ham using
	 * cntWordsHam and cntWordsAll
     */
    private void setpHam(int cntWordsHam, int cntWordsAll) {
        this.pHam = (1 + (float) cntHam) / (float) (cntWordsHam + cntWordsAll);
    }

    @Override
    public String toString() {
        return "WordDetails [word=" + word + ", cntSpam=" + cntSpam
                + ", cntHam=" + cntHam + ", pSpam=" + pSpam + ", pHam=" + pHam
                + "]";
    }

    @Override
    public int compareTo(WordDetails w){
        int f1 = cntHam + cntSpam;
        int f2 = w.getCntHam() + w.getCntSpam();
        return f1 - f2;
    }
}
