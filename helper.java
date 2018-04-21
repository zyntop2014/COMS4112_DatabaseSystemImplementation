/*
 * COMS4112 Project2
 * Utilities functions to help get the optimal solution
 */

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
/**
 *
 * @author yanan, Li-Chieh Liu 
 */
public class helper {
    public static Properties props;

    /**
    * This is the function to load the configure file and set the properties 
    */
    public static void loadProps(Properties configs, String configFileName) {
        props = configs;
        try {
            File configFile = new File("config.txt");
            props.load(new FileInputStream(configFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
    * This is the function to get the combined selectivities for logic and terms
    */           
    public static float getlogicProductivity(HashSet<Integer> elements, ArrayList<Float> products){
        float p = 1;
        for (Integer i : elements) {
            p = p * products.get(i-1);
        }
        return p;
    }

    /**
    * This is the function to get the cost of logic-and on basic terms
    * as example 4.5 in paper
    */
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
   
    /**
    * This is the function to get the cost of logic-and No-branch on basic terms
    * as example 4.4 in paper
    */
    public static float getlogicNonBranchingCost(HashSet<Integer> elements, ArrayList<Float> products){
        float cost;
        int k = elements.size();

        cost =  k*Float.parseFloat(props.getProperty("r")) +
                (k - 1)*Float.parseFloat(props.getProperty("l")) +
                k*Float.parseFloat(props.getProperty("f")) +
                helper.getAval();
        return cost;
    }

    /**
    * This is the function to get the combinations for the subsets in an increasing order 
    * This is used to generate the basic subsets space for the query optimization
    * each subset contails level of elements. 
    */
    public static void getCombinations(ArrayList<SubsetRecord> plans, ArrayList<Float> products, int level) {
        ArrayList<HashSet<Integer>> selects = new ArrayList<>();
        HashSet<Integer> elements = new HashSet<>();
        backtrack(plans, selects, products, level, elements);
    }

    /**
    * This is the backtrack function to do recursion to get the subsets combinations
    */
    public static void backtrack(ArrayList<SubsetRecord> plans, ArrayList<HashSet<Integer>> selects, 
                                ArrayList<Float> products, int level, HashSet<Integer> ele) {
        HashSet<Integer> elements = new HashSet<>(ele);
        if (elements.size() == level) {        
            if (selects.contains(elements)== false){
                SubsetRecord term = new SubsetRecord();
                term.noBranching = 0;
                float bestcost1 = getlogicTermCost(elements, products);
                float bestcost2 = getlogicNonBranchingCost(elements, products);
                //update the bestcost if the non branch 
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
        //if the elements already constains the element, then skip it 
        for (int i = 1; i < products.size()+1; i++) {
            if (elements.contains(i)) {
                continue;
            }
            elements.add(i);
            backtrack(plans, selects, products, level, elements);
            elements.remove(i);
        }
    }

    /**
    * This is the function to genearte the total basic subsets space for the query optimization
    * It contains all the combinations with k-1 elements. 
    * This will generate 2^k-1 subsets space for a inreaseing order
    */
    public static ArrayList<SubsetRecord> generateBasicPlans(ArrayList<Float> products) {
        ArrayList<SubsetRecord> plans = new ArrayList<>();
        int k = products.size();
        int n = (int) Math.pow(2, k)-1;
        for (int i = 1; i < k+1; i++) {
            getCombinations(plans, products, i);
        }
        return plans;
    }

    /**
    * This is the function to get m value (cost of branch misprediction) 
    * from the config
    */
    public static float getMval() {
        return Float.parseFloat(props.getProperty("m"));
    }

    /**
    * This is the function to get a value (cost of wrting an answer to the answer array
    * from the config
    */
    public static float getAval() {
        return Float.parseFloat(props.getProperty("a"));
    }

    /**
    * This is the function to get a fixed cost of a &-term as defination 4.7 shows
    */
    public static float getFcost(SubsetRecord s) {
        float cost;
        cost = Float.parseFloat(props.getProperty("t")) +
                    s.number*Float.parseFloat(props.getProperty("r")) +
                    (s.number - 1)*Float.parseFloat(props.getProperty("l")) +
                    s.number*Float.parseFloat(props.getProperty("f"));
        return cost;  
    }

    /**
    * This is the function to get the left most &-term of one expression
    */
    public static SubsetRecord getLeftMostlogicTerm(SubsetRecord s) {
        SubsetRecord s2 = s;
        while (s2.left != null) {
            s2 = s2.left;
        }
        return s2;
    }
    
    /**
    * This is the function to calculate the cmetric of Expression E1(s1) and E2(s2) in lemma 4.8
    * And then check the condition if such plan cann't be optimal based on their cmetrics
    */
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

    /**
    * This is the function to calculate the dmetric of Expression E1(s1) and leftmost logic term of E2(s2) 
    * in lemma 4.9
    * And then check the condition if such plan cann't be optimal based on their dmetrics
    */
    public static boolean dominateDmetrics(SubsetRecord s1, SubsetRecord s2) {
        float p2;
        float dmetric2;
        float p1 = s1.selectivity;
        float dmetric1 = getFcost(s1);
        SubsetRecord sleft = getLeftMostlogicTerm(s2);

        p2 = sleft.selectivity;
        dmetric2 = getFcost(sleft);
        if (p1 <= 0.5 && p2 < p1 && dmetric2 < dmetric1) {
            return true;
        }   
        return false;
    }

    /**
    * This is the function to get all the &-terms from left to right from a subset and store them in a 
    * SubsetRecord array using a recusion
    * This function is used for printing the optimal resutls to the console and output file
    */
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

    /**
    * This is the function to find an index in 2^k -1 subsets space for a unioned subset of s1 and s2 
    *
    */
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
