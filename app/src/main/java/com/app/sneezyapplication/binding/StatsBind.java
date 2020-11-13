package com.app.sneezyapplication.binding;

import com.app.sneezyapplication.MainActivity;
import com.app.sneezyapplication.data.SneezeItem;
import com.app.sneezyapplication.data.SneezeRepository;

import java.util.List;

import io.realm.RealmResults;

public class StatsBind {

    public int totalSneezes;
    public float averageSneezes;

    public String getTotalSneezes() {
        totalSneezes = 0;
        List<SneezeItem> items = MainActivity.repo.getAllUserSneezeItems();
        for (int i = 0; i < items.size(); i++){
            totalSneezes += items.get(i).getSneezes().size();
        }

        return "Total Sneezes: " + totalSneezes;
    }

    public String getAverageSneezes() {
        List<SneezeItem> items = MainActivity.repo.getAllUserSneezeItems();
        averageSneezes = SneezeRepository.sneezeItemAverage((RealmResults<SneezeItem>) items, true);

        return "Average Sneezes per Day: " + averageSneezes;
    }

    public String getMostSneezeDay() {
        List<SneezeItem> items = MainActivity.repo.getAllUserSneezeItems();
        int[] sneezeDays = SneezeRepository.sneezeItemDays((RealmResults<SneezeItem>) items);
        String daySneezed = "Monday";
        int largest = 0;
        for (int i = 0; i < sneezeDays.length; i++){
            if (sneezeDays[i] > sneezeDays[largest]) largest = i;
        }
        switch (largest) {
            case 0:
                daySneezed = "Monday";
                break;
            case 1:
                daySneezed = "Tuesday";
                break;
            case 2:
                daySneezed = "Wednesday";
                break;
            case 3:
                daySneezed = "Thursday";
                break;
            case 4:
                daySneezed = "Friday";
                break;
            case 5:
                daySneezed = "Saturday";
                break;
            case 6:
                daySneezed = "Sunday";
                break;
        }

        return "Day most sneezed: " + daySneezed;
    }

    public String getDayBreakdown() {
        List<SneezeItem> items = MainActivity.repo.getAllUserSneezeItems();
        int[] sneezeDays = SneezeRepository.sneezeItemDays((RealmResults<SneezeItem>) items);
        return "Monday: " + sneezeDays[0] +
                "\nTuesday: " + sneezeDays[1] +
                "\nWednesday: " + sneezeDays[2] +
                "\nThursday: " + sneezeDays[3] +
                "\nFriday: " + sneezeDays[4] +
                "\nSaturday: " + sneezeDays[5] +
                "\nSunday: " + sneezeDays[6];
    }


    public String getNonUserAverageSneezes() {
        List<SneezeItem> items = MainActivity.repo.getAllSneezeItems();
        items = SneezeRepository.outlierCull((RealmResults<SneezeItem>) items);
        averageSneezes = SneezeRepository.sneezeItemAverage((RealmResults<SneezeItem>) items, true);

        return "Global Average Sneezes per Day: " + averageSneezes;
    }

    public String getNonUserMostSneezeDay() {
        List<SneezeItem> items = MainActivity.repo.getAllSneezeItems();
        int[] sneezeDays = SneezeRepository.sneezeItemDays((RealmResults<SneezeItem>) items);
        String daySneezed = "Monday";
        int largest = 0;
        for (int i = 0; i < sneezeDays.length; i++){
            if (sneezeDays[i] > sneezeDays[largest]) largest = i;
        }
        switch (largest) {
            case 0:
                daySneezed = "Monday";
                break;
            case 1:
                daySneezed = "Tuesday";
                break;
            case 2:
                daySneezed = "Wednesday";
                break;
            case 3:
                daySneezed = "Thursday";
                break;
            case 4:
                daySneezed = "Friday";
                break;
            case 5:
                daySneezed = "Saturday";
                break;
            case 6:
                daySneezed = "Sunday";
                break;
        }

        return "Global Day most sneezed: " + daySneezed;
    }

    public String getNonUserDayBreakdown() {
        List<SneezeItem> items = MainActivity.repo.getAllSneezeItems();
        int[] sneezeDays = SneezeRepository.sneezeItemDays((RealmResults<SneezeItem>) items);
        return "Monday: " + sneezeDays[0] +
                "\nTuesday: " + sneezeDays[1] +
                "\nWednesday: " + sneezeDays[2] +
                "\nThursday: " + sneezeDays[3] +
                "\nFriday: " + sneezeDays[4] +
                "\nSaturday: " + sneezeDays[5] +
                "\nSunday: " + sneezeDays[6];
    }

}
