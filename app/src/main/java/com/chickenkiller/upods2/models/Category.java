package com.chickenkiller.upods2.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 10/7/15.
 */
public class Category {

    private String name;

    public Category(String name) {
        this.name = name;
    }

    public static List<Category> getTestCategoriesList() {
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            categories.add(new Category("Category " + String.valueOf(i)));
        }
        return categories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
