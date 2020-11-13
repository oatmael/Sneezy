package com.app.sneezyapplication.binding;

public class MultiBind {

    public String multiText = "1X";
    public Integer multiNum;

    public String getMulti(int indicator) {


        //Reset Indicator
        if (indicator == 1) {
            multiNum = 1;
            multiText = "1X";
        }
        //Plus Indicator
        else if (indicator == 2 && multiNum < 20) {
            if(multiNum < -1){
                multiNum++;
                multiText = multiNum + "X";
            }
            else if(multiNum == -1) {
                multiNum++;
                multiNum++;
                multiText = multiNum + "X";
            }
            else {
                multiNum++;
                multiText = multiNum + "X";
            }
        }
        //Minus Indicator
        else if (indicator == 3/* && multiNum > 1*/ && multiNum > -9) {
            if(multiNum < 0){
                multiNum--;
                multiText = multiNum + "X";
            }
            else if (multiNum == 1){
                multiNum--;
                multiNum--;
                multiText = multiNum + "X";
            }
            else {
                multiNum--;
                multiText = multiNum + "X";
            }

        }
        //Return without change
        else if(indicator == 4){

        }
        return multiText;
    }

    public Integer getMultiNum() {
        return multiNum;
    }


}
