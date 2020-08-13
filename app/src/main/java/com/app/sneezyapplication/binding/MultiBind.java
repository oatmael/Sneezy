package com.app.sneezyapplication.binding;

public class MultiBind {

    public String multiText = "1X";
    public Integer multiNum;
    public boolean indicator;


        public String getMulti(int indicator) {
        //Reset Indicator
        if (indicator == 1) {
            multiNum = 1;
            multiText = "1X";
        }
        //Plus Indicator
        else if (indicator == 2 && multiNum < 20) {
            multiNum++;
            multiText = multiNum + "X";
        }
        //Minus Indicator
        else if (indicator == 3 && multiNum > 1) {
            multiNum--;
            multiText = multiNum + "X";
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
