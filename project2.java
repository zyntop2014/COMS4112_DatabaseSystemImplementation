/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.*;
import java.io.*;





/**
 *
 * @author yanan
 */

public class project2 {

    /**
     * @param args the command line arguments
     */
    public static void printResults(ArrayList<SubsetRecord> basicplans, ArrayList<Float> products) {
        System.out.println("==================================================================");
        String s = "";
        for (Float p : products) {
            s += String.valueOf(p) + " ";
        }
        System.out.println(s);
        System.out.println("------------------------------------------------------------------");

        SubsetRecord optimal = basicplans.get(basicplans.size()-1);
        ArrayList<SubsetRecord> logicTerms = new ArrayList<>();
        helper.getLogicAndTerms(optimal, logicTerms);

        if (optimal.left == null && optimal.right == null && optimal.noBranching == 1) {
            System.out.println("answer[j] = i;");
            System.out.println("j += ");         
            System.out.println(getLogicTermStr(optimal));
        } else {
            printOptimalPlan(logicTerms);
        }
    
        System.out.println("------------------------------------------------------------------");
        System.out.print("cost:");
        System.out.println(optimal.bestcost);
    }

    public static String getLogicTermStr(SubsetRecord sub) {
        ArrayList<Integer> ele = new ArrayList<>(sub.elements);
        String str = "(";

        for (int i = 0; i < ele.size(); i++) {
            str += "t" + ele.get(i) + "[o" + ele.get(i) + "[i]"+ "]";
            if (i != ele.size()-1) {
                str+= " & ";
            }
            
        }
        str += ")";
        return str;

    }


    public static void printOptimalPlan(ArrayList<SubsetRecord> subsets) {
        String noBranch ="";
        System.out.print("if (");
        for (int i=0; i < subsets.size(); i++) {
            if (subsets.get(i).noBranching == 0) {
                String out = getLogicTermStr(subsets.get(i));
                System.out.print(out);
            }
           
            if (i != subsets.size()-1) {
                if (subsets.get(i+1).noBranching == 0) {
                    System.out.print(" && ");
                } else {
                    noBranch = getLogicTermStr(subsets.get(i+1));
                }
            }
            
        }
        System.out.println(" &) {") ;
        if (noBranch.length() == 0) {
            System.out.println("\tanswer[j++] = i;\n}") ;
        } else {
            String out = "\tanswer[j++] = i;\n\tj += " + noBranch;
            System.out.println(out);

        }




    }

    public static void optimize(ArrayList<Float> products) {
         //generate all basic plans
        ArrayList<SubsetRecord> basicplans = helper.generateBasicPlans(products);
        for (SubsetRecord s: basicplans) {
            for (SubsetRecord s2 : basicplans) {        
                HashSet<Integer> retain = new HashSet<Integer>(s2.elements);
                retain.retainAll(s.elements);
                if (retain.size()!= 0) {
                    continue;
                } 
                
                if (helper.dominateCmetrics(s2, s)) {
                    //do nothing by lemma 4.8
                    continue;
                } else if (s2.selectivity <=0.5 && helper.dominateDmetrics(s2, s)){
                    //do nothing by lemma 4.9
                    continue;
                } else {
                    //calculate the cost for (s'&&s)    
                    float p = s2.selectivity;
                    float q = Math.min(p, 1 - p);
                    float unionCost = helper.getFcost(s2) + helper.getMval()*q + p*s.bestcost;
                    //update Uninton SubsetRecord
                    int index = helper.findUnionIndex(s, s2, basicplans);
                    SubsetRecord unionSub = basicplans.get(index);
                    if (unionCost < unionSub.bestcost) {
                        unionSub.bestcost = unionCost;
                        unionSub.left = s2;
                        unionSub.right = s;  
                    } 
                }      
                    
            }
        }
        printResults(basicplans, products);    
    }
    public static void main(String[] args) {
        // TODO code application logic here
        
        int k = 3;
        ArrayList<ArrayList<Float>> products = new ArrayList<>();
        Properties props = new Properties();


        if (args.length != 2) {
            System.out.println("Please enter with query file name and config file name");
            return;
        }

        String configFile = args[1];
        String queryFile  = args[0];
        helper.loadProps(props, configFile);

        //parse query file to get all the productivity
        File query = new File(queryFile);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(query));
            String sr;
            while ((sr = reader.readLine()) != null) {
                // System.out.println(sr); 
                String[] strs = sr.split(" ");
                // System.out.println(sr); 
                ArrayList<Float> p = new ArrayList<Float>();
                for (String s : strs) {
                    p.add(Float.parseFloat(s));
                }
                products.add(p);
                // System.out.println(p);
                // System.out.println(products);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // System.out.println(products);

        for (ArrayList<Float> selectivities : products) {
            optimize(selectivities);
        }
        
       return;
        
    }
    
}

