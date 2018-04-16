/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.FileInputStream;
import java.util.*;



/**
 *
 * @author yanan
 */
public class helper {
    public static Properties props;

    public static void loadProps(Properties configs, String configFileName) {
        props = configs;
        try {
            File configFile = new File("config.txt");
            props.load(new FileInputStream(configFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
                  
    public static float getlogicProductivity(HashSet<Integer> elements, ArrayList<Float> products){
        float p = 1;
        for (Integer i : elements) {
            p = p * products.get(i-1);
        }
        return p;
    }

    public static float getlogicTermCost(HashSet<Integer> elements, ArrayList<Float> products){
        float cost;
        int k = elements.size();
        float p = getlogicProductivity(elements, products);
        float q = p >=0.5 ? p :(1-p);

        cost = Float.parseFloat(props.getProperty("t")) +
                    k*Float.parseFloat(props.getProperty("r")) +
                    (k - 1)*Float.parseFloat(props.getProperty("l")) +
                    k*Float.parseFloat(props.getProperty("f")) +
                    helper.getMval()*q + p*helper.getAval();
        return cost;
    }
   

    public static float getlogicNonBranchingCost(HashSet<Integer> elements, ArrayList<Float> products){
        float cost;
        int k = elements.size();

        cost =  k*Float.parseFloat(props.getProperty("r")) +
                (k - 1)*Float.parseFloat(props.getProperty("l")) +
                k*Float.parseFloat(props.getProperty("f")) +
                helper.getAval();
        return cost;
    }

    public static void getCombinations(ArrayList<SubsetRecord> plans, ArrayList<Float> products, int level) {
        ArrayList<HashSet<Integer>> selects = new ArrayList<>();
        HashSet<Integer> elements = new HashSet<>();
        backtrack(plans, selects, products, level, elements);
    }

    public static void backtrack(ArrayList<SubsetRecord> plans, ArrayList<HashSet<Integer>> selects, 
                                ArrayList<Float> products, int level, HashSet<Integer> ele) {
        HashSet<Integer> elements = new HashSet<>(ele);
        if (elements.size() == level) {        
            if (selects.contains(elements)== false){
                SubsetRecord term = new SubsetRecord();
                term.noBranching = 0;
                float bestcost1 = getlogicTermCost(elements, products);
                float bestcost2 = getlogicNonBranchingCost(elements, products);
                if (bestcost2 < bestcost1) {
                    term.bestcost = bestcost2;
                    term.noBranching = 1;
                } else {
                    term.bestcost = bestcost1;
                }
                term.selectivity= getlogicProductivity(elements, products);
                term.left = null;
                term.right = null;
                term.number = elements.size();
                term.elements = elements;   
                plans.add(term); 
                selects.add(elements);  
            }
            return;
        }
        for (int i = 1; i < products.size()+1; i++) {
            if (elements.contains(i)) {
                continue;
            }
            elements.add(i);
            backtrack(plans, selects, products, level, elements);
            elements.remove(i);
        }
    }
    
    public static ArrayList<SubsetRecord> generateBasicPlans(ArrayList<Float> products) {
        ArrayList<SubsetRecord> plans = new ArrayList<>();
        int k = products.size();
        int n = (int) Math.pow(2, k)-1;
        // System.out.println(products);
        // getCombinations(plans, products, 4);
        //to do the combination cal
        for (int i = 1; i < k+1; i++) {
            getCombinations(plans, products, i);
        }
        
        return plans;
    }

    public static float getMval() {
        return Float.parseFloat(props.getProperty("m"));
    }

    public static float getAval() {
        return Float.parseFloat(props.getProperty("a"));
    }
    
    public static float getFcost(SubsetRecord s) {
            float cost;
            cost = Float.parseFloat(props.getProperty("t")) +
                    s.number*Float.parseFloat(props.getProperty("r")) +
                    (s.number - 1)*Float.parseFloat(props.getProperty("l")) +
                    s.number*Float.parseFloat(props.getProperty("f"));
            return cost;  
    }

   
    public static SubsetRecord getLeftMostlogicTerm(SubsetRecord s) {
        SubsetRecord s2 = s;
        while (s2.left != null) {
            s2 = s2.left;
        }
        return s2;
    }
    
    public static boolean dominateCmetrics(SubsetRecord s1, SubsetRecord s2) {
        SubsetRecord sleft = getLeftMostlogicTerm(s2); 
        float p2 = sleft.selectivity;
        float p1 = s1.selectivity;
        float cmetric2 = (p2 - 1)/getFcost(sleft);
        float cmetric1 = (p1 - 1)/getFcost(s1);

        if (p2 <= p1 && cmetric2 < cmetric1) {
            return true;
        }
        return false;
    }

        public static boolean dominateDmetrics(SubsetRecord s1, SubsetRecord s2) {
        float p2;
        float dmetric2;
        float p1 = s1.selectivity;
        float dmetric1 = getFcost(s1);
        // ArrayList<SubsetRecord> logicTerms = new ArrayList<>();
        // getLogicAndTerms(s2, logicTerms);
        SubsetRecord sleft = getLeftMostlogicTerm(s2);

   
        p2 = sleft.selectivity;
        dmetric2 = getFcost(sleft);
        if (p1 <= 0.5 && p2 < p1 && dmetric2 < dmetric1) {
            return true;
        }   
        

        // for (int i = 0; i < logicTerms.size(); i++) {
        //     SubsetRecord record = logicTerms.get(i);
        //     //check if it is the left most logic term
        //     if (!record.elements.containsAll(sleft.elements)) {
        //         p2 = record.selectivity;
        //         dmetric2 = getFcost(record);
        //         if (p1 <= 0.5 && p2 < p1 && dmetric2 < dmetric1) {
        //             return true;
        //         }       
        //     }
            
        // } 
        // return p2 <= p1 && dmetric2 < dmetric1; 
        return false;
    }

    // public static ArrayList<SubsetRecord> getLogicAndTerms (SubsetRecord s) {
    //     ArrayList<SubsetRecord> logicTerms = new ArrayList<>();
    //     Queue<SubsetRecord> queue = new LinkedList<>();

    //     if (s.left == null && s.right == null) {
    //         return logicTerms;
    //     }

    //     queue.offer(s);
    //     while (queue.size() != 0) {
    //         SubsetRecord record =  queue.poll();
    //         if (s.left ==null && s.right == null) {
    //             logicTerms.add(record);
    //             continue;
    //         }  

    //         if (s.left != null) {
    //             queue.offer(s.left);
    //         }

    //         if (s.right != null) {
    //             queue.offer(s.right);
    //         }
    //     }

    //     return logicTerms;

    // }

    public static void getLogicAndTerms (SubsetRecord s, ArrayList<SubsetRecord> logicTerms) {
        
        if (s.left == null && s.right == null) {
            logicTerms.add(s);
            return;
        }

      

        if (s.left != null) {
            getLogicAndTerms(s.left, logicTerms);
            s.left.noBranching = 0;
        }

        if (s.right != null) {
            getLogicAndTerms(s.right, logicTerms);
        
        }

    }


    public static  int findUnionIndex(SubsetRecord s, SubsetRecord s2, ArrayList<SubsetRecord> basicplans) {
        HashSet<Integer> set1 = s.elements;
        HashSet<Integer> set2 = s2.elements;
        HashSet<Integer> union = new HashSet<Integer>();
        union.addAll(set2);
        union.addAll(set1);

        for (int i =0; i < basicplans.size(); i++) {
            SubsetRecord sub = basicplans.get(i);
            if (sub.elements.containsAll(union)) {
                return i;
            }
        }
        return -1;
    }

    
}
