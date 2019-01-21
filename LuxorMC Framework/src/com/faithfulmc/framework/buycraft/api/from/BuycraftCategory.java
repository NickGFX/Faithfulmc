package com.faithfulmc.framework.buycraft.api.from;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonElement;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class BuycraftCategory {
    private int id;
    private int order;
    private String name;
    private boolean only_subcategories;
    private List<BuycraftCategory> subcategories;
    private List<BuycraftPackage> packages;

    public BuycraftCategory(int id, int order, String name, boolean only_subcategories, List<BuycraftCategory> subcategories, List<BuycraftPackage> packages) {
        this.id = id;
        this.order = order;
        this.name = name;
        this.only_subcategories = only_subcategories;
        this.subcategories = subcategories;
        this.packages = packages;
    }

    public BuycraftCategory(JsonObject jsonObject){
        id = jsonObject.get("id").getAsInt();
        order = jsonObject.get("order").getAsInt();
        name = jsonObject.get("name").getAsString();
        only_subcategories = jsonObject.has("only_subcategories") && jsonObject.get("only_subcategories").getAsBoolean();
        subcategories = new ArrayList<>();
        if(jsonObject.has("subcategories")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("subcategories")) {
                subcategories.add(new BuycraftCategory(jsonElement.getAsJsonObject()));
            }
        }
        packages = new ArrayList<>();
        if(jsonObject.has("packages")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("packages")) {
                packages.add(new BuycraftPackage(jsonElement.getAsJsonObject()));
            }
        }
    }

    public int getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    public boolean isOnly_subcategories() {
        return only_subcategories;
    }

    public List<BuycraftCategory> getSubcategories() {
        return subcategories;
    }

    public List<BuycraftPackage> getPackages() {
        return packages;
    }

    public List<BuycraftPackage> getPackagesRecursively(){
        List<BuycraftPackage> packages = new ArrayList<>();
        packages.addAll(this.packages);
        for(BuycraftCategory subCategory: subcategories){
            packages.addAll(subCategory.getPackagesRecursively());
        }
        return packages;
    }
}
