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
public class Result {

    private Long label;//text label

    private Long predict;//predict label

    public boolean isCorrect(){
        return label.equals(predict);
    }

    public Result() {
    }

    public Result(Long label, Long predict) {
        this.label = label;
        this.predict = predict;
    }

    public Long getLabel() {
        return label;
    }

    public void setLabel(Long label) {
        this.label = label;
    }

    public Long getPredict() {
        return predict;
    }

    public void setPredict(Long predict) {
        this.predict = predict;
}
}
